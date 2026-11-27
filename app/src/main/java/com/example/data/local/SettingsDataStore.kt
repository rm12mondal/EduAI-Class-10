package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.example.model.FastLearningContextHelper
import com.example.model.InferenceMode
import com.example.model.LlamaInferenceConfig
import com.example.model.SyllabusPlanOption
import com.example.model.TtsVoiceSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.security.SecureRandom

private val Context.eduAiDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "eduai_class10_prefs"
)

data class StoredAppSettings(
    val inferenceMode: InferenceMode = InferenceMode.OFFLINE,
    val activeModelPath: String? = null,
    val llamaConfig: LlamaInferenceConfig = LlamaInferenceConfig(),
    val ttsSettings: TtsVoiceSettings = TtsVoiceSettings(),
    val fastLearningExamYear: Int = FastLearningContextHelper.defaultTargetExamYear(),
    val fastLearningPlanMonths: Float = SyllabusPlanOption.MONTHS_3.months,
    val fastLearningCompletedChapters: Set<String> = emptySet(),
    val secretRecoveryKey: String = "",
    val customAppTitle: String = "",
    val customLogoPath: String? = null,
    val launcherDisguiseId: String = ""
)

class SettingsDataStore(private val context: Context) {

    private object Keys {
        val INFERENCE_MODE = stringPreferencesKey("inference_mode")
        val ACTIVE_MODEL_PATH = stringPreferencesKey("active_model_path")
        val CONTEXT_LENGTH = intPreferencesKey("llama_context_length")
        val TEMPERATURE = floatPreferencesKey("llama_temperature")
        val MAX_TOKENS = intPreferencesKey("llama_max_tokens")
        val CPU_THREADS = intPreferencesKey("llama_cpu_threads")

        val TTS_BENGALI_VOICE = stringPreferencesKey("tts_bengali_voice_name")
        val TTS_ENGLISH_VOICE = stringPreferencesKey("tts_english_voice_name")
        val TTS_SPEECH_RATE = floatPreferencesKey("tts_speech_rate")
        val TTS_PITCH = floatPreferencesKey("tts_pitch")

        val FAST_LEARNING_EXAM_YEAR = intPreferencesKey("fast_learning_exam_year")
        val FAST_LEARNING_PLAN_MONTHS = floatPreferencesKey("fast_learning_plan_months")
        val FAST_LEARNING_COMPLETED_CHAPTERS = stringSetPreferencesKey("fast_learning_completed_chapters")
        val SECRET_RECOVERY_KEY = stringPreferencesKey("secret_recovery_key")
        val CUSTOM_APP_TITLE = stringPreferencesKey("custom_app_title")
        val CUSTOM_LOGO_PATH = stringPreferencesKey("custom_logo_path")
        val LAUNCHER_DISGUISE_ID = stringPreferencesKey("launcher_disguise_id")
    }

    val settingsFlow: Flow<StoredAppSettings> = context.eduAiDataStore.data.map { prefs ->
        StoredAppSettings(
            inferenceMode = InferenceMode.fromStorage(prefs[Keys.INFERENCE_MODE]),
            activeModelPath = prefs[Keys.ACTIVE_MODEL_PATH],
            llamaConfig = LlamaInferenceConfig(
                contextLength = prefs[Keys.CONTEXT_LENGTH] ?: 2048,
                temperature = prefs[Keys.TEMPERATURE] ?: 0.4f,
                maxTokens = prefs[Keys.MAX_TOKENS] ?: 512,
                cpuThreads = prefs[Keys.CPU_THREADS] ?: 4
            ),
            ttsSettings = TtsVoiceSettings(
                bengaliVoiceName = prefs[Keys.TTS_BENGALI_VOICE],
                englishVoiceName = prefs[Keys.TTS_ENGLISH_VOICE],
                speechRate = (prefs[Keys.TTS_SPEECH_RATE] ?: 1.0f).coerceIn(0.7f, 1.5f),
                pitch = (prefs[Keys.TTS_PITCH] ?: 1.0f).coerceIn(0.8f, 1.2f)
            ),
            fastLearningExamYear = prefs[Keys.FAST_LEARNING_EXAM_YEAR]
                ?: FastLearningContextHelper.defaultTargetExamYear(),
            fastLearningPlanMonths = prefs[Keys.FAST_LEARNING_PLAN_MONTHS]
                ?: SyllabusPlanOption.MONTHS_3.months,
            fastLearningCompletedChapters = prefs[Keys.FAST_LEARNING_COMPLETED_CHAPTERS]
                ?: emptySet(),
            secretRecoveryKey = prefs[Keys.SECRET_RECOVERY_KEY] ?: "",
            customAppTitle = prefs[Keys.CUSTOM_APP_TITLE] ?: "",
            customLogoPath = prefs[Keys.CUSTOM_LOGO_PATH],
            launcherDisguiseId = prefs[Keys.LAUNCHER_DISGUISE_ID] ?: ""
        )
    }

    suspend fun setInferenceMode(mode: InferenceMode) {
        context.eduAiDataStore.edit { prefs ->
            prefs[Keys.INFERENCE_MODE] = mode.storageValue
        }
    }

    suspend fun setActiveModelPath(path: String?) {
        context.eduAiDataStore.edit { prefs ->
            if (path.isNullOrBlank()) {
                prefs.remove(Keys.ACTIVE_MODEL_PATH)
            } else {
                prefs[Keys.ACTIVE_MODEL_PATH] = path
            }
        }
    }

    suspend fun updateLlamaConfig(config: LlamaInferenceConfig) {
        context.eduAiDataStore.edit { prefs ->
            prefs[Keys.CONTEXT_LENGTH] = config.contextLength
            prefs[Keys.TEMPERATURE] = config.temperature
            prefs[Keys.MAX_TOKENS] = config.maxTokens
            prefs[Keys.CPU_THREADS] = config.cpuThreads
        }
    }

    suspend fun updateTtsSettings(settings: TtsVoiceSettings) {
        context.eduAiDataStore.edit { prefs ->
            if (settings.bengaliVoiceName.isNullOrBlank()) {
                prefs.remove(Keys.TTS_BENGALI_VOICE)
            } else {
                prefs[Keys.TTS_BENGALI_VOICE] = settings.bengaliVoiceName
            }
            if (settings.englishVoiceName.isNullOrBlank()) {
                prefs.remove(Keys.TTS_ENGLISH_VOICE)
            } else {
                prefs[Keys.TTS_ENGLISH_VOICE] = settings.englishVoiceName
            }
            prefs[Keys.TTS_SPEECH_RATE] = settings.speechRate.coerceIn(0.7f, 1.5f)
            prefs[Keys.TTS_PITCH] = settings.pitch.coerceIn(0.8f, 1.2f)
        }
    }

    suspend fun setFastLearningExamYear(year: Int) {
        context.eduAiDataStore.edit { prefs ->
            prefs[Keys.FAST_LEARNING_EXAM_YEAR] = year
        }
    }

    suspend fun setFastLearningPlanMonths(months: Float) {
        context.eduAiDataStore.edit { prefs ->
            prefs[Keys.FAST_LEARNING_PLAN_MONTHS] = months
        }
    }

    suspend fun toggleFastLearningChapter(chapterKey: String) {
        context.eduAiDataStore.edit { prefs ->
            val current = prefs[Keys.FAST_LEARNING_COMPLETED_CHAPTERS] ?: emptySet()
            prefs[Keys.FAST_LEARNING_COMPLETED_CHAPTERS] = if (chapterKey in current) {
                current - chapterKey
            } else {
                current + chapterKey
            }
        }
    }

    suspend fun restoreFastLearningProgress(
        examYear: Int,
        planMonths: Float,
        completedChapters: Set<String>
    ) {
        context.eduAiDataStore.edit { prefs ->
            prefs[Keys.FAST_LEARNING_EXAM_YEAR] = examYear
            prefs[Keys.FAST_LEARNING_PLAN_MONTHS] = planMonths
            prefs[Keys.FAST_LEARNING_COMPLETED_CHAPTERS] = completedChapters
        }
    }

    suspend fun getOrCreateSecretRecoveryKey(): String {
        val existing = settingsFlow.first().secretRecoveryKey
        if (existing.isNotBlank()) return existing
        val generated = generateReadableRecoveryKey()
        context.eduAiDataStore.edit { prefs ->
            val current = prefs[Keys.SECRET_RECOVERY_KEY]
            if (current.isNullOrBlank()) {
                prefs[Keys.SECRET_RECOVERY_KEY] = generated
            }
        }
        return settingsFlow.first().secretRecoveryKey.ifBlank { generated }
    }

    suspend fun saveSecretRecoveryKey(key: String) {
        val cleaned = key.trim().uppercase()
        if (cleaned.isNotBlank()) {
            context.eduAiDataStore.edit { prefs ->
                prefs[Keys.SECRET_RECOVERY_KEY] = cleaned
            }
        }
    }

    suspend fun setCustomAppTitle(title: String) {
        context.eduAiDataStore.edit { prefs ->
            val trimmed = title.trim()
            if (trimmed.isEmpty()) {
                prefs.remove(Keys.CUSTOM_APP_TITLE)
            } else {
                prefs[Keys.CUSTOM_APP_TITLE] = trimmed
            }
        }
    }

    suspend fun setCustomLogoPath(path: String?) {
        context.eduAiDataStore.edit { prefs ->
            if (path.isNullOrBlank()) {
                prefs.remove(Keys.CUSTOM_LOGO_PATH)
            } else {
                prefs[Keys.CUSTOM_LOGO_PATH] = path
            }
        }
    }

    suspend fun setLauncherDisguiseId(presetId: String) {
        context.eduAiDataStore.edit { prefs ->
            prefs[Keys.LAUNCHER_DISGUISE_ID] = presetId
        }
    }

    companion object {
        private const val KEY_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"

        fun generateReadableRecoveryKey(): String {
            val rng = SecureRandom()
            fun block(len: Int): String = buildString(len) {
                repeat(len) {
                    append(KEY_ALPHABET[rng.nextInt(KEY_ALPHABET.length)])
                }
            }
            return "EDU10-${block(4)}-${block(4)}"
        }
    }
}
