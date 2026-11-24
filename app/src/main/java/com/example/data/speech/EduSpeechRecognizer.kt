package com.example.data.speech

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class VoiceInputLanguage(
    val localeTag: String,
    val labelBn: String,
    val shortCode: String
) {
    BENGALI("bn-IN", "বাংলা (bn-IN)", "বাংলা"),
    ENGLISH("en-US", "English (en-US)", "EN")
}

data class SpeechRecognitionUiState(
    val isListening: Boolean = false,
    val language: VoiceInputLanguage = VoiceInputLanguage.BENGALI,
    val partialText: String = "",
    val rmsLevel: Float = 0f,
    val statusMessage: String? = null
)

/**
 * Offline-compatible Android SpeechRecognizer manager supporting Bengali (bn-IN) and English (en-US).
 * Requires RECORD_AUDIO permission before calling [startListening].
 */
class EduSpeechRecognizer(
    context: Context,
    private val onFinalTranscript: (String) -> Unit
) {
    private val appContext = context.applicationContext
    private val mainHandler = Handler(Looper.getMainLooper())
    private var recognizer: SpeechRecognizer? = null

    private val _state = MutableStateFlow(SpeechRecognitionUiState())
    val state: StateFlow<SpeechRecognitionUiState> = _state.asStateFlow()

    fun setLanguage(language: VoiceInputLanguage) {
        _state.update { it.copy(language = language, statusMessage = null) }
    }

    fun isAvailable(): Boolean = SpeechRecognizer.isRecognitionAvailable(appContext)

    fun startListening(language: VoiceInputLanguage = _state.value.language): Boolean {
        if (!isAvailable()) {
            _state.update {
                it.copy(
                    isListening = false,
                    statusMessage = "এই ডিভাইসে সরাসরি ভয়েস রেকগনিশন পাওয়া যায়নি।"
                )
            }
            return false
        }

        mainHandler.post {
            try {
                recognizer?.cancel()
                recognizer?.destroy()
                val sr = SpeechRecognizer.createSpeechRecognizer(appContext)
                recognizer = sr
                sr.setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _state.update {
                            it.copy(
                                isListening = true,
                                language = language,
                                partialText = "",
                                statusMessage = if (language == VoiceInputLanguage.BENGALI) {
                                    "শুনছি… বাংলায় প্রশ্ন বলো"
                                } else {
                                    "Listening… Speak in English"
                                }
                            )
                        }
                    }

                    override fun onBeginningOfSpeech() = Unit

                    override fun onRmsChanged(rmsdB: Float) {
                        val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
                        _state.update { it.copy(rmsLevel = normalized) }
                    }

                    override fun onBufferReceived(buffer: ByteArray?) = Unit

                    override fun onEndOfSpeech() {
                        _state.update { it.copy(rmsLevel = 0f) }
                    }

                    override fun onError(error: Int) {
                        val message = when (error) {
                            SpeechRecognizer.ERROR_AUDIO -> "মাইক্রোফোন অডিও সমস্যা হয়েছে।"
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "মাইক্রোফোন ব্যবহারের অনুমতি প্রয়োজন।"
                            SpeechRecognizer.ERROR_NO_MATCH -> "কোনো কথা বোঝা যায়নি — আবার স্পষ্ট করে বলো।"
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "কোনো শব্দ পাওয়া যায়নি — আবার চেষ্টা করো।"
                            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "ভয়েস ইঞ্জিন ব্যস্ত, এক মুহূর্ত পরে বলো।"
                            SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT ->
                                "অফলাইন ভয়েস প্যাক ডাউনলোড না থাকলে ইন্টারনেট প্রয়োজন হতে পারে।"
                            else -> "ভয়েস ইনপুট সম্পন্ন হয়নি (কোড $error)।"
                        }
                        _state.update {
                            it.copy(
                                isListening = false,
                                rmsLevel = 0f,
                                statusMessage = message
                            )
                        }
                    }

                    override fun onResults(results: Bundle?) {
                        val spoken = results
                            ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            ?.firstOrNull()
                            ?.trim()
                            .orEmpty()
                        _state.update {
                            it.copy(
                                isListening = false,
                                partialText = spoken,
                                rmsLevel = 0f,
                                statusMessage = if (spoken.isNotBlank()) "✅ চ্যাট বক্সে যোগ করা হয়েছে!" else null
                            )
                        }
                        if (spoken.isNotBlank()) {
                            onFinalTranscript(spoken)
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val partial = partialResults
                            ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            ?.firstOrNull()
                            ?.trim()
                            .orEmpty()
                        if (partial.isNotBlank()) {
                            _state.update { it.copy(partialText = partial) }
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) = Unit
                })

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, language.localeTag)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, language.localeTag)
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                }
                sr.startListening(intent)
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isListening = false,
                        statusMessage = "ভয়েস চালু করা যায়নি: ${e.message ?: "অজানা ত্রুটি"}"
                    )
                }
            }
        }
        return true
    }

    fun stopListening() {
        mainHandler.post {
            runCatching { recognizer?.stopListening() }
            _state.update { it.copy(isListening = false, rmsLevel = 0f) }
        }
    }

    fun clearStatus() {
        _state.update { it.copy(statusMessage = null) }
    }

    fun destroy() {
        mainHandler.post {
            runCatching {
                recognizer?.cancel()
                recognizer?.destroy()
            }
            recognizer = null
        }
    }
}
