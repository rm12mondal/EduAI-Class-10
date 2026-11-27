package com.example.data.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max

/**
 * Zero-storage-permission helper for creating FileProvider camera capture URIs,
 * automatic Smart Image Compression (max 1024px longest edge + ~80% JPEG quality,
 * targeting ~150-200 KB to save ~95% storage while keeping handwriting & diagrams crisp),
 * and persisting local chat uploads & AI diagrams.
 */
object NotebookImageHelper {
    private const val TAG = "NotebookImageHelper"
    const val SMART_MAX_DIMENSION_PX = 1024
    private const val SMART_JPEG_QUALITY_INITIAL = 80
    private const val SMART_JPEG_QUALITY_FLOOR = 66
    private const val TARGET_MAX_FILE_BYTES = 210 * 1024 // ~205 KB soft ceiling

    fun createTempCameraImageUri(context: Context): Uri {
        val photosDir = File(context.cacheDir, "notebook_photos").apply {
            if (!exists()) mkdirs()
        }
        val imageFile = File(photosDir, "notebook_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            imageFile
        )
    }

    /**
     * Loads and automatically resizes any camera or gallery image to max 1024px longest edge
     * so every chat section (Main Chat, Smart Learning, Brahmastram, Mock Exam, Mind Map,
     * Fast Learning, Step-by-Step Tutor) automatically benefits from Smart Compression.
     */
    suspend fun loadScaledBitmap(
        context: Context,
        uri: Uri,
        maxDimensionPx: Int = SMART_MAX_DIMENSION_PX
    ): Bitmap? = withContext(Dispatchers.IO) {
        val effectiveMax = maxDimensionPx.coerceIn(256, SMART_MAX_DIMENSION_PX)
        try {
            val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use { input ->
                BitmapFactory.decodeStream(input, null, boundsOptions)
            }

            val rawWidth = boundsOptions.outWidth
            val rawHeight = boundsOptions.outHeight
            if (rawWidth <= 0 || rawHeight <= 0) return@withContext null

            var sampleSize = 1
            val longestSide = max(rawWidth, rawHeight)
            while (longestSide / (sampleSize * 2) >= effectiveMax) {
                sampleSize *= 2
            }

            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }

            val decoded = context.contentResolver.openInputStream(uri)?.use { input ->
                BitmapFactory.decodeStream(input, null, decodeOptions)
            } ?: return@withContext null

            scaleBitmapIfNeeded(decoded, effectiveMax)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load scaled bitmap from uri=$uri", e)
            null
        }
    }

    private fun scaleBitmapIfNeeded(source: Bitmap, maxDimensionPx: Int): Bitmap {
        val maxCurrent = max(source.width, source.height)
        if (maxCurrent <= maxDimensionPx) return source
        val scale = maxDimensionPx.toFloat() / maxCurrent.toFloat()
        val targetW = (source.width * scale).toInt().coerceAtLeast(1)
        val targetH = (source.height * scale).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(source, targetW, targetH, true)
    }

    /**
     * Smart-compresses a bitmap to max 1024px and ~80% JPEG quality (targeting ~150-200 KB)
     * before writing to app-private chat_uploads storage.
     */
    suspend fun persistThumbnail(context: Context, bitmap: Bitmap): String? =
        withContext(Dispatchers.IO) {
            try {
                val uploadsDir = File(context.filesDir, "chat_uploads").apply {
                    if (!exists()) mkdirs()
                }
                val outFile = File(uploadsDir, "note_${System.currentTimeMillis()}.jpg")
                val resized = scaleBitmapIfNeeded(bitmap, SMART_MAX_DIMENSION_PX)
                try {
                    writeSmartCompressedJpeg(resized, outFile)
                } finally {
                    if (resized !== bitmap && !resized.isRecycled) {
                        resized.recycle()
                    }
                }
                pruneStaleCameraCache(context)
                outFile.absolutePath
            } catch (e: Exception) {
                Log.e(TAG, "Failed to persist smart-compressed photo", e)
                null
            }
        }

    /**
     * Persists AI-generated visual diagrams at max 1024px with sharp smart JPEG compression
     * to keep storage lean while preserving fine textbook labels and lines.
     */
    suspend fun persistVisualDiagram(context: Context, bitmap: Bitmap): String? =
        withContext(Dispatchers.IO) {
            try {
                val diagramsDir = File(context.filesDir, "ai_diagrams").apply {
                    if (!exists()) mkdirs()
                }
                val outFile = File(diagramsDir, "diagram_${System.currentTimeMillis()}.jpg")
                val resized = scaleBitmapIfNeeded(bitmap, SMART_MAX_DIMENSION_PX)
                try {
                    writeSmartCompressedJpeg(resized, outFile, initialQuality = 84)
                } finally {
                    if (resized !== bitmap && !resized.isRecycled) {
                        resized.recycle()
                    }
                }
                outFile.absolutePath
            } catch (e: Exception) {
                Log.e(TAG, "Failed to persist AI visual diagram", e)
                null
            }
        }

    private fun writeSmartCompressedJpeg(
        bitmap: Bitmap,
        outFile: File,
        initialQuality: Int = SMART_JPEG_QUALITY_INITIAL
    ) {
        val baos = ByteArrayOutputStream()
        var quality = initialQuality
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos)

        // Gently adapt quality if the image exceeds ~205 KB while keeping handwriting crisp
        while (baos.size() > TARGET_MAX_FILE_BYTES && quality > SMART_JPEG_QUALITY_FLOOR) {
            baos.reset()
            quality = (quality - 6).coerceAtLeast(SMART_JPEG_QUALITY_FLOOR)
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos)
        }

        FileOutputStream(outFile).use { fos ->
            fos.write(baos.toByteArray())
            fos.flush()
        }
    }

    private fun pruneStaleCameraCache(context: Context) {
        try {
            val cacheDir = File(context.cacheDir, "notebook_photos")
            if (!cacheDir.exists()) return
            val cutoff = System.currentTimeMillis() - 10 * 60 * 1000L // 10 minutes
            cacheDir.listFiles()?.forEach { f ->
                if (f.isFile && f.lastModified() < cutoff) {
                    f.delete()
                }
            }
        } catch (_: Exception) {
            // Non-critical cache cleanup
        }
    }

    private val thumbnailCache: android.util.LruCache<String, Bitmap> by lazy {
        val maxKb = (Runtime.getRuntime().maxMemory() / 1024L).toInt()
        val cacheKb = (maxKb / 12).coerceIn(4 * 1024, 16 * 1024)
        object : android.util.LruCache<String, Bitmap>(cacheKb) {
            override fun sizeOf(key: String, value: Bitmap): Int {
                return (value.allocationByteCount / 1024).coerceAtLeast(1)
            }
        }
    }

    private fun cacheKey(path: String, maxSizePx: Int): String = "$path#$maxSizePx"

    fun getCachedThumbnail(path: String, maxSizePx: Int = 480): Bitmap? {
        val cached = thumbnailCache.get(cacheKey(path, maxSizePx))
        return if (cached != null && !cached.isRecycled) cached else null
    }

    fun evictFromCache(path: String) {
        val snapshot = thumbnailCache.snapshot()
        snapshot.keys.forEach { key ->
            if (key.startsWith("$path#")) {
                thumbnailCache.remove(key)
            }
        }
    }

    /**
     * Asynchronously decodes a downsampled thumbnail on `Dispatchers.IO` and caches it
     * in the in-memory `LruCache` so scrolling through chat messages and the Media Vault
     * stays strictly off the UI thread.
     */
    suspend fun loadThumbnailAsync(path: String, maxSizePx: Int = 480): Bitmap? {
        getCachedThumbnail(path, maxSizePx)?.let { return it }
        return withContext(Dispatchers.IO) {
            getCachedThumbnail(path, maxSizePx)?.let { return@withContext it }
            val decoded = decodeFileThumbnail(path, maxSizePx)
            if (decoded != null) {
                thumbnailCache.put(cacheKey(path, maxSizePx), decoded)
            }
            decoded
        }
    }

    /**
     * Asynchronously decodes a high-clarity display bitmap (capped at [maxSizePx]) on `Dispatchers.IO`
     * for full-screen zoom dialogs, avoiding full-resolution UI-thread decodes.
     */
    suspend fun loadFullBitmapAsync(path: String, maxSizePx: Int = 1280): Bitmap? {
        return loadThumbnailAsync(path, maxSizePx)
    }

    fun decodeFileThumbnail(path: String, maxSizePx: Int = 480): Bitmap? {
        getCachedThumbnail(path, maxSizePx)?.let { return it }
        return try {
            val file = File(path)
            if (!file.exists()) return null
            val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(path, opts)
            val longest = max(opts.outWidth, opts.outHeight)
            if (longest <= 0) return null
            var sample = 1
            while (longest / (sample * 2) >= maxSizePx) sample *= 2
            val decoded = BitmapFactory.decodeFile(
                path,
                BitmapFactory.Options().apply {
                    inSampleSize = sample
                    inPreferredConfig = if (maxSizePx <= 640) Bitmap.Config.RGB_565 else Bitmap.Config.ARGB_8888
                }
            )
            if (decoded != null) {
                thumbnailCache.put(cacheKey(path, maxSizePx), decoded)
            }
            decoded
        } catch (e: Exception) {
            Log.e(TAG, "Failed to decode thumbnail $path", e)
            null
        }
    }

    fun decodeFullBitmap(path: String): Bitmap? {
        return decodeFileThumbnail(path, maxSizePx = 1280)
    }
}
