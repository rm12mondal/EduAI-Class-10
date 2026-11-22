package com.example.data.gguf

import android.util.Log
import com.example.model.LlamaInferenceConfig
import com.example.model.WbbseSubject

/**
 * Clean JNI bridge contract for running `llama.cpp` native GGUF inference (`libllama_jni.so`)
 * plus Llama-3.2-Instruct ChatML prompt formatting.
 *
 * When `libllama_jni.so` is compiled into the APK's `jniLibs/arm64-v8a/` folder, native token
 * generation executes directly against the loaded `Llama-3.2-1B-Instruct.Q4_K_M.gguf` file.
 * When absent (standard lightweight APK build), [isNativeRuntimeAvailable] cleanly reports `false`
 * so the offline engine seamlessly relies on the on-device WBBSE Class 10 Master Data engine.
 */
object LlamaNativeBridge {
    private const val TAG = "LlamaNativeBridge"

    val isNativeRuntimeAvailable: Boolean = try {
        System.loadLibrary("llama_jni")
        Log.i(TAG, "libllama_jni.so loaded successfully.")
        true
    } catch (_: UnsatisfiedLinkError) {
        Log.i(TAG, "libllama_jni.so not bundled; using WBBSE local retrieval + GGUF prompt pipeline.")
        false
    } catch (_: SecurityException) {
        false
    }

    private var nativeHandle: Long = 0L

    private external fun nativeLoadModel(modelPath: String, contextLength: Int, threads: Int): Long
    private external fun nativeGenerate(handle: Long, prompt: String, maxTokens: Int, temperature: Float): String
    private external fun nativeRelease(handle: Long)

    fun loadNativeModelIfSupported(modelPath: String, config: LlamaInferenceConfig): Boolean {
        if (!isNativeRuntimeAvailable) return false
        unloadNativeModel()
        nativeHandle = try {
            nativeLoadModel(modelPath, config.contextLength, config.cpuThreads)
        } catch (e: Throwable) {
            Log.e(TAG, "Native GGUF load failed", e)
            0L
        }
        return nativeHandle != 0L
    }

    fun generateNativeIfLoaded(prompt: String, config: LlamaInferenceConfig): String? {
        if (!isNativeRuntimeAvailable || nativeHandle == 0L) return null
        return try {
            nativeGenerate(nativeHandle, prompt, config.maxTokens, config.temperature)
                .takeIf { it.isNotBlank() }
        } catch (e: Throwable) {
            Log.e(TAG, "Native GGUF inference failed", e)
            null
        }
    }

    fun unloadNativeModel() {
        if (isNativeRuntimeAvailable && nativeHandle != 0L) {
            try {
                nativeRelease(nativeHandle)
            } catch (_: Throwable) {
            }
            nativeHandle = 0L
        }
    }

    /**
     * Formats a WBBSE Class 10 student query and retrieved Master Data context using the exact
     * Meta Llama-3.2-1B-Instruct chat template (`<|begin_of_text|>` / `<|start_header_id|>`).
     */
    fun buildLlama32Prompt(
        userQuery: String,
        subject: WbbseSubject?,
        ragContext: String
    ): String {
        val subjectHint = subject?.let { "${it.bengaliName} (${it.englishName})" }
            ?: "All 7 WBBSE Class 10 Subjects"
        return buildString {
            append("<|begin_of_text|><|start_header_id|>system<|end_header_id|>\n\n")
            append("You are EduAI Class 10, an expert West Bengal Board (WBBSE) Madhyamik tutor. ")
            append("Active subject focus: $subjectHint. ")
            append("Answer clearly, concisely, and accurately using the provided WBBSE Master Data.\n")
            if (ragContext.isNotBlank()) {
                append("\n[WBBSE Master Data Context]\n")
                append(ragContext)
                append("\n")
            }
            append("<|eot_id|><|start_header_id|>user<|end_header_id|>\n\n")
            append(userQuery.trim())
            append("<|eot_id|><|start_header_id|>assistant<|end_header_id|>\n\n")
        }
    }
}
