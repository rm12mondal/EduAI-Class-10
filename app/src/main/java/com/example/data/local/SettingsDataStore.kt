package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.InferenceMode
import com.example.model.LlamaInferenceConfig
import com.example.model.TtsVoiceSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.eduAiDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "eduai_class10_prefs"
)

data class StoredAppSettings(
    val inferenceMode: InferenceMode = InferenceMode.OFFLINE,
    val activeModelPath: String? = null,
    val llamaConfig: LlamaInferenceConfig = LlamaInferenceConfig(),
    val ttsSettings: TtsVoiceSettings = TtsVoiceSettings()
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
            )
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
}
