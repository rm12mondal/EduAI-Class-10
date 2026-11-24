package com.example.data.speech

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.util.Log
import com.example.model.TtsVoiceOption
import com.example.model.TtsVoiceSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Locale

data class TtsUiState(
    val isEngineReady: Boolean = false,
    val speakingMessageId: Long? = null,
    val activeLocaleTag: String? = null,
    val availableBengaliVoices: List<TtsVoiceOption> = emptyList(),
    val availableEnglishVoices: List<TtsVoiceOption> = emptyList()
)

/**
 * Offline-capable Android TextToSpeech controller supporting:
 * - Automatic Bengali (`bn-IN`) and English (`en-US`) language detection
 * - Real device voice discovery via `TextToSpeech.getVoices()`
 * - Persistent user-selected Bengali/English voices, speech rate (`0.7x..1.5x`), and pitch (`0.8..1.2`)
 */
class EduTtsManager(context: Context) : TextToSpeech.OnInitListener {

    private val appContext = context.applicationContext
    private var tts: TextToSpeech? = null
    private var lastUtteranceIdForCurrentMessage: String? = null
    private var cachedVoiceByName: Map<String, Voice> = emptyMap()
    private var completionCallback: (() -> Unit)? = null

    private val _state = MutableStateFlow(TtsUiState())
    val state: StateFlow<TtsUiState> = _state.asStateFlow()

    init {
        tts = TextToSpeech(appContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) = Unit

                override fun onDone(utteranceId: String?) {
                    if (utteranceId == lastUtteranceIdForCurrentMessage) {
                        val callback = completionCallback
                        completionCallback = null
                        _state.update { it.copy(speakingMessageId = null, activeLocaleTag = null) }
                        callback?.invoke()
                    }
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    completionCallback = null
                    _state.update { it.copy(speakingMessageId = null, activeLocaleTag = null) }
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    Log.w(TAG, "TTS utterance error code=$errorCode for $utteranceId")
                    completionCallback = null
                    _state.update { it.copy(speakingMessageId = null, activeLocaleTag = null) }
                }
            })
            _state.update { it.copy(isEngineReady = true) }
            refreshVoices()
        } else {
            Log.e(TAG, "TextToSpeech initialization failed with status=$status")
            _state.update { it.copy(isEngineReady = false) }
        }
    }

    /**
     * Queries installed voices from `TextToSpeech.getVoices()` and categorizes Bengali (`bn-IN` / `bn`)
     * and English (`en-US` / `en`) voices with readable labels.
     */
    fun refreshVoices() {
        val engine = tts ?: return
        if (!_state.value.isEngineReady) return

        val rawVoices: Set<Voice> = try {
            engine.voices.orEmpty()
        } catch (e: Exception) {
            Log.w(TAG, "Unable to query TextToSpeech.getVoices()", e)
            emptySet()
        }

        cachedVoiceByName = rawVoices.associateBy { it.name }

        val bengaliVoices = rawVoices
            .filter { voice ->
                val lang = voice.locale?.language?.lowercase(Locale.ROOT)
                lang == "bn"
            }
            .sortedWith(
                compareByDescending<Voice> { it.locale?.country.equals("IN", ignoreCase = true) }
                    .thenBy { it.isNetworkConnectionRequired }
                    .thenBy { it.name }
            )
            .mapIndexed { idx, voice -> toVoiceOption(voice, index = idx + 1, langLabel = "বাংলা") }

        val englishVoices = rawVoices
            .filter { voice ->
                val lang = voice.locale?.language?.lowercase(Locale.ROOT)
                val country = voice.locale?.country?.uppercase(Locale.ROOT)
                lang == "en" && (country == "US" || country == "IN" || country == "GB")
            }
            .sortedWith(
                compareByDescending<Voice> { it.locale?.country.equals("US", ignoreCase = true) }
                    .thenBy { it.isNetworkConnectionRequired }
                    .thenBy { it.name }
            )
            .take(24)
            .mapIndexed { idx, voice -> toVoiceOption(voice, index = idx + 1, langLabel = "English") }

        _state.update {
            it.copy(
                availableBengaliVoices = bengaliVoices,
                availableEnglishVoices = englishVoices
            )
        }
    }

    /**
     * Toggles voice playback for [messageId] using the user's saved [settings].
     */
    fun toggleSpeak(
        messageId: Long,
        rawText: String,
        settings: TtsVoiceSettings
    ): Boolean {
        val engine = tts ?: return false
        if (!_state.value.isEngineReady) return false

        if (_state.value.speakingMessageId == messageId) {
            stop()
            return true
        }

        val cleaned = sanitizeForSpeech(rawText)
        if (cleaned.isBlank()) return false

        val isBengali = detectBengaliScript(cleaned)
        return speakInternal(
            engine = engine,
            messageId = messageId,
            cleanedText = cleaned,
            preferBengali = isBengali,
            settings = settings
        )
    }

    /**
     * Plays or stops a short sample voice preview in Bengali (`bn-IN`) or English (`en-US`)
     * using the currently selected voice, speech rate, and pitch.
     */
    fun toggleTestPreview(
        preferBengali: Boolean,
        settings: TtsVoiceSettings
    ): Boolean {
        val engine = tts ?: return false
        if (!_state.value.isEngineReady) return false

        val previewId = if (preferBengali) TEST_PREVIEW_BN_ID else TEST_PREVIEW_EN_ID
        if (_state.value.speakingMessageId == previewId) {
            stop()
            return true
        }

        val sampleText = if (preferBengali) {
            "নমস্কার! আমি EduAI ক্লাস ১০। পশ্চিমবঙ্গ বোর্ডের মাধ্যমিক প্রস্তুতিতে আমি তোমার সাথে আছি।"
        } else {
            "Hello! I am EduAI Class 10, ready to help you prepare for your West Bengal Madhyamik exams."
        }

        return speakInternal(
            engine = engine,
            messageId = previewId,
            cleanedText = sampleText,
            preferBengali = preferBengali,
            settings = settings
        )
    }

    /**
     * Plays an audio revision track with custom [speedOverride] (e.g., 0.8x, 1.0x, 1.25x)
     * and invokes [onComplete] automatically when finished for continuous podcast-style listening.
     */
    fun speakRevisionTrack(
        trackMessageId: Long,
        rawText: String,
        settings: TtsVoiceSettings,
        speedOverride: Float,
        onComplete: () -> Unit
    ): Boolean {
        val engine = tts ?: return false
        if (!_state.value.isEngineReady) return false

        val cleaned = sanitizeForSpeech(rawText)
        if (cleaned.isBlank()) return false

        val isBengali = detectBengaliScript(cleaned)
        val effectiveSettings = settings.copy(speechRate = speedOverride.coerceIn(0.7f, 1.5f))
        return speakInternal(
            engine = engine,
            messageId = trackMessageId,
            cleanedText = cleaned,
            preferBengali = isBengali,
            settings = effectiveSettings,
            onComplete = onComplete
        )
    }

    fun stop() {
        completionCallback = null
        tts?.stop()
        lastUtteranceIdForCurrentMessage = null
        _state.update { it.copy(speakingMessageId = null, activeLocaleTag = null) }
    }

    fun shutdown() {
        stop()
        tts?.shutdown()
        tts = null
    }

    private fun speakInternal(
        engine: TextToSpeech,
        messageId: Long,
        cleanedText: String,
        preferBengali: Boolean,
        settings: TtsVoiceSettings,
        onComplete: (() -> Unit)? = null
    ): Boolean {
        val appliedLocaleTag = applyVoiceOrLocale(
            engine = engine,
            preferBengali = preferBengali,
            preferredVoiceName = if (preferBengali) settings.bengaliVoiceName else settings.englishVoiceName
        )

        engine.setSpeechRate(settings.speechRate.coerceIn(0.7f, 1.5f))
        engine.setPitch(settings.pitch.coerceIn(0.8f, 1.2f))

        val chunks = splitIntoChunks(cleanedText, maxChunkChars = 900)
        if (chunks.isEmpty()) return false

        completionCallback = null
        engine.stop()
        val lastId = "msg_${messageId}_chunk_${chunks.lastIndex}"
        lastUtteranceIdForCurrentMessage = lastId
        completionCallback = onComplete
        _state.update {
            it.copy(
                speakingMessageId = messageId,
                activeLocaleTag = appliedLocaleTag
            )
        }

        chunks.forEachIndexed { idx, chunk ->
            val mode = if (idx == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
            val uid = "msg_${messageId}_chunk_$idx"
            engine.speak(chunk, mode, null, uid)
        }
        return true
    }

    private fun applyVoiceOrLocale(
        engine: TextToSpeech,
        preferBengali: Boolean,
        preferredVoiceName: String?
    ): String {
        if (!preferredVoiceName.isNullOrBlank()) {
            val targetVoice = cachedVoiceByName[preferredVoiceName]
                ?: runCatching { engine.voices?.firstOrNull { it.name == preferredVoiceName } }.getOrNull()
            if (targetVoice != null) {
                val res = runCatching { engine.setVoice(targetVoice) }.getOrDefault(TextToSpeech.ERROR)
                if (res == TextToSpeech.SUCCESS) {
                    return targetVoice.locale?.toLanguageTag() ?: preferredVoiceName
                }
            }
        }
        return configureBestLocale(engine, preferBengali)
    }

    private fun toVoiceOption(voice: Voice, index: Int, langLabel: String): TtsVoiceOption {
        val localeTag = voice.locale?.toLanguageTag() ?: "default"
        val lower = voice.name.lowercase(Locale.ROOT)
        val styleHint = when {
            lower.contains("female") || lower.contains("bnf") || lower.contains("sfg") || lower.contains("tpf") -> "Female"
            lower.contains("male") || lower.contains("bnm") || lower.contains("iom") || lower.contains("tpd") -> "Male"
            else -> "Voice $index"
        }
        val modeTag = if (voice.isNetworkConnectionRequired) "Online" else "Offline"
        val shortCode = voice.name.substringAfter("-x-", voice.name).take(14)
        return TtsVoiceOption(
            voiceName = voice.name,
            displayLabel = "$langLabel ($localeTag) • $styleHint [$shortCode] ($modeTag)",
            localeTag = localeTag,
            isNetworkRequired = voice.isNetworkConnectionRequired
        )
    }

    private fun detectBengaliScript(text: String): Boolean {
        var bengaliCount = 0
        var latinCount = 0
        for (ch in text) {
            when {
                ch in '\u0980'..'\u09FF' -> bengaliCount++
                ch in 'A'..'Z' || ch in 'a'..'z' -> latinCount++
            }
        }
        val totalLetters = bengaliCount + latinCount
        if (totalLetters == 0) return false
        return bengaliCount >= 4 && (bengaliCount.toFloat() / totalLetters.toFloat()) >= 0.15f
    }

    private fun configureBestLocale(engine: TextToSpeech, preferBengali: Boolean): String {
        val candidates = if (preferBengali) {
            listOf(
                Locale.Builder().setLanguage("bn").setRegion("IN").build(),
                Locale.Builder().setLanguage("bn").setRegion("BD").build(),
                Locale.US
            )
        } else {
            listOf(
                Locale.US,
                Locale.Builder().setLanguage("en").setRegion("IN").build()
            )
        }

        for (locale in candidates) {
            val result = engine.setLanguage(locale)
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                return locale.toLanguageTag()
            }
        }
        return Locale.getDefault().toLanguageTag()
    }

    private fun sanitizeForSpeech(raw: String): String {
        return raw
            .replace(Regex("[*#`_~•🔹📘📌🎯📎💡🌐ℹ️✅❌🔊📷✍️📊📝⚠️]"), " ")
            .replace("|", ", ")
            .replace("→", " থেকে ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun splitIntoChunks(text: String, maxChunkChars: Int): List<String> {
        if (text.length <= maxChunkChars) return listOf(text)
        val result = mutableListOf<String>()
        var start = 0
        while (start < text.length) {
            val end = (start + maxChunkChars).coerceAtMost(text.length)
            if (end == text.length) {
                result.add(text.substring(start, end).trim())
                break
            }
            val splitAt = text.lastIndexOfAny(charArrayOf('।', '.', '!', '?', '\n', ' '), end)
                .takeIf { it > start + 100 } ?: end
            result.add(text.substring(start, splitAt).trim())
            start = (splitAt + 1).coerceAtMost(text.length)
        }
        return result.filter { it.isNotBlank() }
    }

    companion object {
        private const val TAG = "EduTtsManager"
        const val TEST_PREVIEW_BN_ID = -101L
        const val TEST_PREVIEW_EN_ID = -102L
    }
}
