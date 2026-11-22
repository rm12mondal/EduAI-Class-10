package com.example.data.gguf

import com.example.model.GgufFileMetadata
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Spec-compliant Little-Endian GGUF (v2 / v3) binary header parser and diagnostic file helper.
 * Reads magic ("GGUF"), format version, tensor count, KV count, and key Llama metadata fields
 * directly from local `.gguf` model files such as `Llama-3.2-1B-Instruct.Q4_K_M.gguf`.
 */
object GgufFileInspector {

    const val TARGET_MODEL_FILENAME = "Llama-3.2-1B-Instruct.Q4_K_M.gguf"
    private const val GGUF_MAGIC_LE = 0x46554747 // "GGUF" in little-endian

    // GGUF metadata value type constants per official GGML/GGUF specification
    private const val TYPE_UINT8 = 0
    private const val TYPE_INT8 = 1
    private const val TYPE_UINT16 = 2
    private const val TYPE_INT16 = 3
    private const val TYPE_UINT32 = 4
    private const val TYPE_INT32 = 5
    private const val TYPE_FLOAT32 = 6
    private const val TYPE_BOOL = 7
    private const val TYPE_STRING = 8
    private const val TYPE_ARRAY = 9
    private const val TYPE_UINT64 = 10
    private const val TYPE_INT64 = 11
    private const val TYPE_FLOAT64 = 12

    fun inspect(file: File): GgufFileMetadata {
        if (!file.exists() || !file.isFile) {
            return invalidMetadata(file, "ফাইলটি পাওয়া যায়নি (File not found)")
        }

        val sizeBytes = file.length()
        if (sizeBytes < 24L) {
            return invalidMetadata(file, "GGUF হেডার অসম্পূর্ণ (ফাইল সাইজ ২৪ বাইটের কম)")
        }

        val isTargetName = file.name.equals(TARGET_MODEL_FILENAME, ignoreCase = true) ||
            file.name.contains("Llama-3.2-1B", ignoreCase = true)

        return try {
            DataInputStream(BufferedInputStream(FileInputStream(file), 32 * 1024)).use { stream ->
                val magic = readIntLE(stream)
                if (magic != GGUF_MAGIC_LE) {
                    return invalidMetadata(
                        file,
                        "ভুল ফাইল ফরম্যাট: GGUF magic বাইট পাওয়া যায়নি।"
                    )
                }

                val version = readIntLE(stream)
                val tensorCount = readLongLE(stream)
                val kvCount = readLongLE(stream)

                var architecture = "unknown"
                var modelName = file.nameWithoutExtension
                var contextLength = 2048
                var fileTypeCode = -1

                // Read up to 64 metadata entries safely to avoid blocking on massive vocab arrays
                val maxEntriesToInspect = kvCount.coerceIn(0L, 64L).toInt()
                var parsingAbortedEarly = false

                for (i in 0 until maxEntriesToInspect) {
                    if (parsingAbortedEarly) break
                    val key = readGgufString(stream, maxBytes = 512) ?: break
                    val valueType = readIntLE(stream)

                    when (valueType) {
                        TYPE_STRING -> {
                            val strVal = readGgufString(stream, maxBytes = 4096)
                            if (strVal == null) {
                                parsingAbortedEarly = true
                            } else {
                                when (key) {
                                    "general.architecture" -> architecture = strVal
                                    "general.name" -> modelName = strVal
                                }
                            }
                        }

                        TYPE_UINT32, TYPE_INT32 -> {
                            val intVal = readIntLE(stream)
                            when (key) {
                                "llama.context_length",
                                "$architecture.context_length" -> if (intVal > 0) contextLength = intVal
                                "general.file_type" -> fileTypeCode = intVal
                            }
                        }

                        TYPE_UINT8, TYPE_INT8, TYPE_BOOL -> skipExact(stream, 1)
                        TYPE_UINT16, TYPE_INT16 -> skipExact(stream, 2)
                        TYPE_FLOAT32 -> skipExact(stream, 4)
                        TYPE_UINT64, TYPE_INT64, TYPE_FLOAT64 -> skipExact(stream, 8)
                        TYPE_ARRAY -> {
                            // Arrays (like tokenizer.ggml.tokens) can be huge; skip safely if small, else stop KV scan
                            val elemType = readIntLE(stream)
                            val count = readLongLE(stream)
                            val elemSize = fixedByteSize(elemType)
                            if (elemSize > 0 && count in 0L..8192L) {
                                skipExact(stream, elemSize * count)
                            } else {
                                parsingAbortedEarly = true
                            }
                        }

                        else -> parsingAbortedEarly = true
                    }
                }

                val quantHint = detectQuantization(file.name, fileTypeCode)
                val isDiagnosticStub = sizeBytes < 1_000_000L
                val statusNote = when {
                    isDiagnosticStub ->
                        "বৈধ GGUF v$version হেডার (ডায়াগনস্টিক টেস্ট ফাইল — পূর্ণ ওজন ~808 MB কপি করলে ফুল অফলাইন ইনফারেন্স চালু হবে)"
                    isTargetName ->
                        "বৈধ GGUF v$version মডেল ($TARGET_MODEL_FILENAME) সফলভাবে যাচাই হয়েছে!"
                    else ->
                        "বৈধ GGUF v$version মডেল ($architecture, $quantHint) সফলভাবে যাচাই হয়েছে।"
                }

                GgufFileMetadata(
                    fileName = file.name,
                    absolutePath = file.absolutePath,
                    fileSizeBytes = sizeBytes,
                    isValidMagic = true,
                    ggufVersion = version,
                    tensorCount = tensorCount,
                    metadataKvCount = kvCount,
                    architecture = architecture,
                    modelName = modelName,
                    contextLength = contextLength,
                    quantizationHint = quantHint,
                    isTargetLlama32Model = isTargetName || modelName.contains("Llama-3.2", ignoreCase = true),
                    validationMessage = statusNote
                )
            }
        } catch (e: Exception) {
            invalidMetadata(file, "GGUF হেডার পড়তে সমস্যা হয়েছে: ${e.localizedMessage ?: "Unknown error"}")
        }
    }

    /**
     * Generates a tiny, 100% binary-valid GGUF v3 header file carrying Llama-3.2-1B-Instruct
     * metadata so users can immediately test and verify the local GGUF parser and pipeline
     * before transferring the ~808 MB weights file onto the device.
     */
    fun createDiagnosticTestGguf(targetDir: File): File {
        if (!targetDir.exists()) targetDir.mkdirs()
        val outFile = File(targetDir, "Llama-3.2-1B-Instruct.Q4_K_M.diagnostic.gguf")
        val baos = ByteArrayOutputStream()

        fun writeIntLE(v: Int) {
            val buf = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(v)
            baos.write(buf.array())
        }

        fun writeLongLE(v: Long) {
            val buf = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putLong(v)
            baos.write(buf.array())
        }

        fun writeString(s: String) {
            val bytes = s.toByteArray(Charsets.UTF_8)
            writeLongLE(bytes.size.toLong())
            baos.write(bytes)
        }

        // GGUF v3 Header
        writeIntLE(GGUF_MAGIC_LE) // "GGUF"
        writeIntLE(3)             // Version 3
        writeLongLE(147L)         // Typical Llama-3.2-1B tensor count
        writeLongLE(4L)           // 4 Metadata KV pairs

        // KV 1: general.architecture = "llama"
        writeString("general.architecture")
        writeIntLE(TYPE_STRING)
        writeString("llama")

        // KV 2: general.name = "Llama-3.2-1B-Instruct"
        writeString("general.name")
        writeIntLE(TYPE_STRING)
        writeString("Llama-3.2-1B-Instruct")

        // KV 3: llama.context_length = 131072
        writeString("llama.context_length")
        writeIntLE(TYPE_UINT32)
        writeIntLE(131072)

        // KV 4: general.file_type = 15 (Q4_K_M)
        writeString("general.file_type")
        writeIntLE(TYPE_UINT32)
        writeIntLE(15)

        FileOutputStream(outFile).use { it.write(baos.toByteArray()) }
        return outFile
    }

    private fun invalidMetadata(file: File, message: String): GgufFileMetadata = GgufFileMetadata(
        fileName = file.name,
        absolutePath = file.absolutePath,
        fileSizeBytes = if (file.exists()) file.length() else 0L,
        isValidMagic = false,
        ggufVersion = 0,
        tensorCount = 0L,
        metadataKvCount = 0L,
        architecture = "N/A",
        modelName = file.name,
        contextLength = 0,
        quantizationHint = "Unknown",
        isTargetLlama32Model = false,
        validationMessage = message
    )

    private fun detectQuantization(fileName: String, fileTypeCode: Int): String {
        return when (fileTypeCode) {
            15 -> "Q4_K_M"
            14 -> "Q4_K_S"
            17 -> "Q5_K_M"
            18 -> "Q6_K"
            7 -> "Q8_0"
            2 -> "Q4_0"
            else -> {
                val upper = fileName.uppercase()
                when {
                    upper.contains("Q4_K_M") -> "Q4_K_M"
                    upper.contains("Q4_K_S") -> "Q4_K_S"
                    upper.contains("Q5_K_M") -> "Q5_K_M"
                    upper.contains("Q6_K") -> "Q6_K"
                    upper.contains("Q8_0") -> "Q8_0"
                    upper.contains("F16") -> "F16"
                    else -> "GGUF"
                }
            }
        }
    }

    private fun fixedByteSize(type: Int): Int = when (type) {
        TYPE_UINT8, TYPE_INT8, TYPE_BOOL -> 1
        TYPE_UINT16, TYPE_INT16 -> 2
        TYPE_UINT32, TYPE_INT32, TYPE_FLOAT32 -> 4
        TYPE_UINT64, TYPE_INT64, TYPE_FLOAT64 -> 8
        else -> -1
    }

    private fun readIntLE(stream: DataInputStream): Int {
        val bytes = ByteArray(4)
        stream.readFully(bytes)
        return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).int
    }

    private fun readLongLE(stream: DataInputStream): Long {
        val bytes = ByteArray(8)
        stream.readFully(bytes)
        return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).long
    }

    private fun readGgufString(stream: DataInputStream, maxBytes: Int): String? {
        val len = readLongLE(stream)
        if (len < 0 || len > maxBytes) return null
        val bytes = ByteArray(len.toInt())
        stream.readFully(bytes)
        return String(bytes, Charsets.UTF_8)
    }

    private fun skipExact(stream: DataInputStream, bytesToSkip: Long) {
        var remaining = bytesToSkip
        while (remaining > 0) {
            val skipped = stream.skip(remaining)
            if (skipped <= 0) {
                if (stream.read() == -1) break
                remaining -= 1
            } else {
                remaining -= skipped
            }
        }
    }
}
