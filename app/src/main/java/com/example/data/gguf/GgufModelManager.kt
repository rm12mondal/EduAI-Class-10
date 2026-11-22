package com.example.data.gguf

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.example.model.DiscoveredGgufFile
import com.example.model.GgufFileMetadata
import com.example.model.GgufModelStatus
import com.example.model.LlamaInferenceConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

data class GgufManagerState(
    val status: GgufModelStatus = GgufModelStatus.NOT_FOUND,
    val activeMetadata: GgufFileMetadata? = null,
    val discoveredFiles: List<DiscoveredGgufFile> = emptyList(),
    val isImporting: Boolean = false,
    val importProgress: Float = 0f,
    val importStatusMessage: String? = null,
    val internalModelsDirPath: String = "",
    val externalModelsDirPath: String = "",
    val isNativeLlamaEngineBundled: Boolean = LlamaNativeBridge.isNativeRuntimeAvailable
)

/**
 * Handles local `.gguf` model files (`Llama-3.2-1B-Instruct.Q4_K_M.gguf`) safely using
 * zero-permission Android Storage Access Framework (SAF) import and app-scoped storage directories.
 */
class GgufModelManager(private val context: Context) {

    private val internalModelsDir: File
        get() = File(context.filesDir, "models").apply { if (!exists()) mkdirs() }

    private val externalModelsDir: File?
        get() = context.getExternalFilesDir("models")?.apply { if (!exists()) mkdirs() }

    private val _state = MutableStateFlow(
        GgufManagerState(
            internalModelsDirPath = internalModelsDir.absolutePath,
            externalModelsDirPath = externalModelsDir?.absolutePath ?: "Unavailable"
        )
    )
    val state: StateFlow<GgufManagerState> = _state.asStateFlow()

    suspend fun scanAndAutoDetect(
        savedActivePath: String?,
        config: LlamaInferenceConfig
    ): String? = withContext(Dispatchers.IO) {
        val dirs = listOfNotNull(internalModelsDir, externalModelsDir)
        val ggufFiles = dirs
            .flatMap { dir -> dir.listFiles()?.toList().orEmpty() }
            .filter { it.isFile && it.name.endsWith(".gguf", ignoreCase = true) }
            .distinctBy { it.absolutePath }
            .sortedWith(
                compareByDescending<File> {
                    it.name.equals(GgufFileInspector.TARGET_MODEL_FILENAME, ignoreCase = true)
                }.thenByDescending { it.length() }
            )

        val candidateToLoad = when {
            !savedActivePath.isNullOrBlank() && File(savedActivePath).exists() -> File(savedActivePath)
            ggufFiles.isNotEmpty() -> ggufFiles.first()
            else -> null
        }

        if (candidateToLoad != null) {
            loadModelInternal(candidateToLoad, ggufFiles, config)
            candidateToLoad.absolutePath
        } else {
            _state.update {
                it.copy(
                    status = GgufModelStatus.NOT_FOUND,
                    activeMetadata = null,
                    discoveredFiles = emptyList(),
                    internalModelsDirPath = internalModelsDir.absolutePath,
                    externalModelsDirPath = externalModelsDir?.absolutePath ?: "Unavailable"
                )
            }
            null
        }
    }

    suspend fun importFromSafUri(
        uri: Uri,
        config: LlamaInferenceConfig
    ): Result<GgufFileMetadata> = withContext(Dispatchers.IO) {
        _state.update {
            it.copy(
                isImporting = true,
                importProgress = 0f,
                importStatusMessage = "GGUF ফাইল পড়া ও কপি করা হচ্ছে…"
            )
        }

        try {
            val (rawName, totalSize) = queryDisplayNameAndSize(uri)
            val safeFileName = sanitizeFileName(rawName)
            if (!safeFileName.endsWith(".gguf", ignoreCase = true)) {
                _state.update {
                    it.copy(
                        isImporting = false,
                        importStatusMessage = "দয়া করে একটি বৈধ .gguf ফাইল (যেমন ${GgufFileInspector.TARGET_MODEL_FILENAME}) নির্বাচন করুন।"
                    )
                }
                return@withContext Result.failure(IllegalArgumentException("Not a .gguf file"))
            }

            val targetFile = File(internalModelsDir, safeFileName)
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(targetFile).use { output ->
                    val buffer = ByteArray(64 * 1024)
                    var copied = 0L
                    var read: Int
                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                        copied += read
                        if (totalSize > 0L) {
                            val progress = (copied.toFloat() / totalSize.toFloat()).coerceIn(0f, 1f)
                            _state.update { state -> state.copy(importProgress = progress) }
                        }
                    }
                    output.flush()
                }
            } ?: return@withContext Result.failure(IllegalStateException("Unable to open selected file"))

            val metadata = GgufFileInspector.inspect(targetFile)
            val allFiles = collectDiscoveredFiles()
            if (metadata.isValidMagic) {
                loadModelInternal(targetFile, allFiles, config)
                _state.update {
                    it.copy(
                        isImporting = false,
                        importProgress = 1f,
                        importStatusMessage = "${targetFile.name} সফলভাবে ইম্পোর্ট ও যাচাই হয়েছে!"
                    )
                }
                Result.success(metadata)
            } else {
                _state.update {
                    it.copy(
                        isImporting = false,
                        importProgress = 0f,
                        status = GgufModelStatus.CORRUPT_OR_INVALID,
                        activeMetadata = metadata,
                        discoveredFiles = toDiscoveredList(allFiles, null),
                        importStatusMessage = metadata.validationMessage
                    )
                }
                Result.failure(IllegalStateException(metadata.validationMessage))
            }
        } catch (e: Exception) {
            _state.update {
                it.copy(
                    isImporting = false,
                    importProgress = 0f,
                    importStatusMessage = "ফাইল ইম্পোর্ট ব্যর্থ হয়েছে: ${e.localizedMessage ?: "Unknown error"}"
                )
            }
            Result.failure(e)
        }
    }

    suspend fun createAndLoadDiagnosticGguf(config: LlamaInferenceConfig): GgufFileMetadata =
        withContext(Dispatchers.IO) {
            val file = GgufFileInspector.createDiagnosticTestGguf(internalModelsDir)
            val allFiles = collectDiscoveredFiles()
            val metadata = loadModelInternal(file, allFiles, config)
            _state.update {
                it.copy(
                    importStatusMessage = "ডায়াগনস্টিক GGUF v3 হেডার তৈরি ও যাচাই সম্পন্ন হয়েছে।"
                )
            }
            metadata
        }

    suspend fun loadModelByPath(path: String, config: LlamaInferenceConfig): GgufFileMetadata? =
        withContext(Dispatchers.IO) {
            val file = File(path)
            if (!file.exists()) return@withContext null
            loadModelInternal(file, collectDiscoveredFiles(), config)
        }

    suspend fun unloadCurrentModel() = withContext(Dispatchers.IO) {
        LlamaNativeBridge.unloadNativeModel()
        val files = collectDiscoveredFiles()
        _state.update {
            it.copy(
                status = GgufModelStatus.NOT_FOUND,
                activeMetadata = null,
                discoveredFiles = toDiscoveredList(files, null),
                importStatusMessage = "মডেল আনলোড করা হয়েছে।"
            )
        }
    }

    suspend fun deleteModelFile(path: String) = withContext(Dispatchers.IO) {
        val target = File(path)
        if (_state.value.activeMetadata?.absolutePath == path) {
            LlamaNativeBridge.unloadNativeModel()
        }
        if (target.exists()) {
            target.delete()
        }
        val remaining = collectDiscoveredFiles()
        val activePath = _state.value.activeMetadata?.absolutePath?.takeIf { it != path }
        _state.update {
            it.copy(
                status = if (activePath == null) GgufModelStatus.NOT_FOUND else it.status,
                activeMetadata = if (activePath == null) null else it.activeMetadata,
                discoveredFiles = toDiscoveredList(remaining, activePath),
                importStatusMessage = "${target.name} মুছে ফেলা হয়েছে।"
            )
        }
    }

    fun clearStatusBanner() {
        _state.update { it.copy(importStatusMessage = null) }
    }

    private fun loadModelInternal(
        file: File,
        allFiles: List<File>,
        config: LlamaInferenceConfig
    ): GgufFileMetadata {
        _state.update { it.copy(status = GgufModelStatus.VALIDATING) }
        val metadata = GgufFileInspector.inspect(file)
        if (metadata.isValidMagic) {
            if (file.length() >= 1_000_000L) {
                LlamaNativeBridge.loadNativeModelIfSupported(file.absolutePath, config)
            }
            _state.update {
                it.copy(
                    status = GgufModelStatus.READY_LOADED,
                    activeMetadata = metadata,
                    discoveredFiles = toDiscoveredList(allFiles, file.absolutePath)
                )
            }
        } else {
            _state.update {
                it.copy(
                    status = GgufModelStatus.CORRUPT_OR_INVALID,
                    activeMetadata = metadata,
                    discoveredFiles = toDiscoveredList(allFiles, null)
                )
            }
        }
        return metadata
    }

    private fun collectDiscoveredFiles(): List<File> {
        val dirs = listOfNotNull(internalModelsDir, externalModelsDir)
        return dirs
            .flatMap { it.listFiles()?.toList().orEmpty() }
            .filter { it.isFile && it.name.endsWith(".gguf", ignoreCase = true) }
            .distinctBy { it.absolutePath }
    }

    private fun toDiscoveredList(files: List<File>, loadedPath: String?): List<DiscoveredGgufFile> =
        files.map { file ->
            DiscoveredGgufFile(
                fileName = file.name,
                absolutePath = file.absolutePath,
                sizeBytes = file.length(),
                isLoaded = file.absolutePath == loadedPath,
                isTargetLlama32 = file.name.equals(GgufFileInspector.TARGET_MODEL_FILENAME, ignoreCase = true) ||
                    file.name.contains("Llama-3.2-1B", ignoreCase = true)
            )
        }

    private fun queryDisplayNameAndSize(uri: Uri): Pair<String, Long> {
        var name = "imported_model.gguf"
        var size = -1L
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (nameIdx >= 0) {
                    cursor.getString(nameIdx)?.takeIf { it.isNotBlank() }?.let { name = it }
                }
                if (sizeIdx >= 0 && !cursor.isNull(sizeIdx)) {
                    size = cursor.getLong(sizeIdx)
                }
            }
        }
        return name to size
    }

    private fun sanitizeFileName(raw: String): String =
        raw.replace(Regex("[^a-zA-Z0-9._-]"), "_")
}
