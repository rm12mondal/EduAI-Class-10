package com.example.model

enum class InferenceMode(val storageValue: String) {
    OFFLINE("offline"),
    ONLINE("online");

    companion object {
        fun fromStorage(value: String?): InferenceMode =
            entries.firstOrNull { it.storageValue == value } ?: OFFLINE
    }
}

enum class GgufModelStatus {
    NOT_FOUND,
    VALIDATING,
    READY_LOADED,
    CORRUPT_OR_INVALID
}

data class GgufFileMetadata(
    val fileName: String,
    val absolutePath: String,
    val fileSizeBytes: Long,
    val isValidMagic: Boolean,
    val ggufVersion: Int,
    val tensorCount: Long,
    val metadataKvCount: Long,
    val architecture: String,
    val modelName: String,
    val contextLength: Int,
    val quantizationHint: String,
    val isTargetLlama32Model: Boolean,
    val validationMessage: String
)

data class LlamaInferenceConfig(
    val contextLength: Int = 2048,
    val temperature: Float = 0.4f,
    val maxTokens: Int = 512,
    val cpuThreads: Int = 4
)

data class DiscoveredGgufFile(
    val fileName: String,
    val absolutePath: String,
    val sizeBytes: Long,
    val isLoaded: Boolean,
    val isTargetLlama32: Boolean
)

enum class AppTab {
    CHAT,
    SUBJECTS,
    EXAMS,
    TOPPER,
    MODEL_HUB
}

data class TtsVoiceSettings(
    val bengaliVoiceName: String? = null,
    val englishVoiceName: String? = null,
    val speechRate: Float = 1.0f,
    val pitch: Float = 1.0f
)

data class TtsVoiceOption(
    val voiceName: String,
    val displayLabel: String,
    val localeTag: String,
    val isNetworkRequired: Boolean
)
