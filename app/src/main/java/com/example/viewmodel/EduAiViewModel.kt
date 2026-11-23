package com.example.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.NetworkMonitor
import com.example.data.ai.AiRepository
import com.example.data.ai.AiStreamEvent
import com.example.data.ai.HybridEduAiRepository
import com.example.data.gguf.GgufManagerState
import com.example.data.gguf.GgufModelManager
import com.example.data.local.ChatMessageEntity
import com.example.data.local.EduAiDatabase
import com.example.data.local.ExamAttemptEntity
import com.example.data.local.MasterDataEntity
import com.example.data.local.SettingsDataStore
import com.example.data.local.StoredAppSettings
import com.example.data.local.StudyRepository
import com.example.data.media.NotebookImageHelper
import com.example.data.speech.EduTtsManager
import com.example.data.speech.TtsUiState
import com.example.model.AppTab
import com.example.model.ExamFormat
import com.example.model.InferenceMode
import com.example.model.LlamaInferenceConfig
import com.example.model.OverallProgressStats
import com.example.model.SubjectProgressSummary
import com.example.model.SubjectiveEvaluationResult
import com.example.model.TtsVoiceSettings
import com.example.model.WbbseSubject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

data class StreamingDraft(
    val text: String,
    val sourceLabel: String,
    val mode: InferenceMode,
    val subject: WbbseSubject?
)

data class PendingPhotoAttachment(
    val uri: Uri,
    val previewBitmap: Bitmap
)

data class EduAiUiState(
    val activeTab: AppTab = AppTab.CHAT,
    val selectedChatSubject: WbbseSubject? = null,
    val selectedDetailSubject: WbbseSubject? = null,
    val subjectSearchQuery: String = "",
    val inferenceMode: InferenceMode = InferenceMode.OFFLINE,
    val isDeviceOnline: Boolean = false,
    val llamaConfig: LlamaInferenceConfig = LlamaInferenceConfig(),
    val ggufState: GgufManagerState = GgufManagerState(),
    val masterNotes: List<MasterDataEntity> = emptyList(),
    val chatMessages: List<ChatMessageEntity> = emptyList(),
    val isGenerating: Boolean = false,
    val streamingDraft: StreamingDraft? = null,
    val pendingChatPhoto: PendingPhotoAttachment? = null,
    val ttsState: TtsUiState = TtsUiState(),
    val ttsVoiceSettings: TtsVoiceSettings = TtsVoiceSettings(),
    val examAttempts: List<ExamAttemptEntity> = emptyList(),
    val progressStats: OverallProgressStats = computeProgressStats(emptyList()),
    val selectedExamSubject: WbbseSubject = WbbseSubject.PHYSICAL_SCIENCE,
    val selectedExamFormat: ExamFormat = ExamFormat.MCQ,
    val isEvaluatingExam: Boolean = false,
    val latestSubjectiveEvaluation: SubjectiveEvaluationResult? = null,
    val pendingExamPhoto: PendingPhotoAttachment? = null,
    val errorMessage: String? = null
)

private data class LocalUiNavigationState(
    val activeTab: AppTab = AppTab.CHAT,
    val selectedChatSubject: WbbseSubject? = null,
    val selectedDetailSubject: WbbseSubject? = null,
    val subjectSearchQuery: String = "",
    val isGenerating: Boolean = false,
    val streamingDraft: StreamingDraft? = null,
    val pendingChatPhoto: PendingPhotoAttachment? = null,
    val selectedExamSubject: WbbseSubject = WbbseSubject.PHYSICAL_SCIENCE,
    val selectedExamFormat: ExamFormat = ExamFormat.MCQ,
    val isEvaluatingExam: Boolean = false,
    val latestSubjectiveEvaluation: SubjectiveEvaluationResult? = null,
    val pendingExamPhoto: PendingPhotoAttachment? = null,
    val errorMessage: String? = null
)

class EduAiViewModel(application: Application) : AndroidViewModel(application) {

    private val appContext = application.applicationContext
    private val database = EduAiDatabase.getInstance(application)
    private val studyRepository = StudyRepository(
        masterDataDao = database.masterDataDao(),
        chatMessageDao = database.chatMessageDao(),
        examAttemptDao = database.examAttemptDao()
    )
    private val settingsDataStore = SettingsDataStore(application)
    private val ggufModelManager = GgufModelManager(application)
    private val networkMonitor = NetworkMonitor(application)
    private val ttsManager = EduTtsManager(application)
    private val aiRepository: AiRepository = HybridEduAiRepository(
        studyRepository = studyRepository,
        ggufModelManager = ggufModelManager
    )

    private val localState = MutableStateFlow(LocalUiNavigationState())
    private var generationJob: Job? = null

    private val coreDataFlow = combine(
        settingsDataStore.settingsFlow,
        ggufModelManager.state,
        studyRepository.allMasterNotes,
        studyRepository.allChatMessages,
        networkMonitor.isOnline
    ) { settings, gguf, notes, messages, isOnline ->
        DataBundle(settings, gguf, notes, messages, isOnline)
    }

    private val extraDataFlow = combine(
        ttsManager.state,
        studyRepository.allExamAttempts
    ) { tts, attempts ->
        ExtraBundle(tts, attempts)
    }

    val uiState: StateFlow<EduAiUiState> = combine(
        localState,
        coreDataFlow,
        extraDataFlow
    ) { nav, data, extra ->
        EduAiUiState(
            activeTab = nav.activeTab,
            selectedChatSubject = nav.selectedChatSubject,
            selectedDetailSubject = nav.selectedDetailSubject,
            subjectSearchQuery = nav.subjectSearchQuery,
            inferenceMode = data.settings.inferenceMode,
            isDeviceOnline = data.isOnline,
            llamaConfig = data.settings.llamaConfig,
            ggufState = data.gguf,
            masterNotes = data.notes,
            chatMessages = data.messages,
            isGenerating = nav.isGenerating,
            streamingDraft = nav.streamingDraft,
            pendingChatPhoto = nav.pendingChatPhoto,
            ttsState = extra.tts,
            ttsVoiceSettings = data.settings.ttsSettings,
            examAttempts = extra.attempts,
            progressStats = computeProgressStats(extra.attempts),
            selectedExamSubject = nav.selectedExamSubject,
            selectedExamFormat = nav.selectedExamFormat,
            isEvaluatingExam = nav.isEvaluatingExam,
            latestSubjectiveEvaluation = nav.latestSubjectiveEvaluation,
            pendingExamPhoto = nav.pendingExamPhoto,
            errorMessage = nav.errorMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = EduAiUiState()
    )

    init {
        viewModelScope.launch {
            studyRepository.ensureSeeded()
            val stored = settingsDataStore.settingsFlow.first()
            val detectedPath = ggufModelManager.scanAndAutoDetect(
                savedActivePath = stored.activeModelPath,
                config = stored.llamaConfig
            )
            if (detectedPath != stored.activeModelPath) {
                settingsDataStore.setActiveModelPath(detectedPath)
            }
        }
    }

    override fun onCleared() {
        ttsManager.shutdown()
        super.onCleared()
    }

    fun selectTab(tab: AppTab) {
        if (localState.value.activeTab != tab) {
            ttsManager.stop()
        }
        localState.update { it.copy(activeTab = tab) }
    }

    fun selectChatSubject(subject: WbbseSubject?) {
        localState.update { it.copy(selectedChatSubject = subject) }
    }

    fun openSubjectDetail(subject: WbbseSubject?) {
        localState.update { it.copy(selectedDetailSubject = subject) }
    }

    fun updateSubjectSearchQuery(query: String) {
        localState.update { it.copy(subjectSearchQuery = query) }
    }

    fun toggleInferenceMode(isOnlineMode: Boolean) {
        val target = if (isOnlineMode) InferenceMode.ONLINE else InferenceMode.OFFLINE
        viewModelScope.launch {
            settingsDataStore.setInferenceMode(target)
        }
    }

    // ==================== Text-to-Speech ====================

    fun toggleSpeakMessage(messageId: Long, text: String) {
        val started = ttsManager.toggleSpeak(
            messageId = messageId,
            rawText = text,
            settings = uiState.value.ttsVoiceSettings
        )
        if (!started) {
            localState.update {
                it.copy(errorMessage = "ভয়েস প্লেব্যাক (TTS) প্রস্তুত হচ্ছে, একটু পরে আবার চেষ্টা করো।")
            }
        }
    }

    fun updateTtsVoiceSettings(settings: TtsVoiceSettings) {
        viewModelScope.launch {
            settingsDataStore.updateTtsSettings(settings)
        }
    }

    fun testTtsVoice(preferBengali: Boolean) {
        val started = ttsManager.toggleTestPreview(
            preferBengali = preferBengali,
            settings = uiState.value.ttsVoiceSettings
        )
        if (!started) {
            localState.update {
                it.copy(errorMessage = "ডিভাইসের ভয়েস ইঞ্জিন (TTS) এখনো প্রস্তুত হয়নি।")
            }
        }
    }

    fun refreshTtsVoices() {
        ttsManager.refreshVoices()
    }

    fun stopSpeech() {
        ttsManager.stop()
    }

    // ==================== Chat Photo Attachment ====================

    fun attachChatPhotoUri(uri: Uri) {
        viewModelScope.launch {
            val bitmap = NotebookImageHelper.loadScaledBitmap(appContext, uri)
            if (bitmap != null) {
                localState.update {
                    it.copy(pendingChatPhoto = PendingPhotoAttachment(uri = uri, previewBitmap = bitmap))
                }
            } else {
                localState.update {
                    it.copy(errorMessage = "ছবিটি লোড করা যায়নি। অন্য একটি পরিষ্কার ছবি চেষ্টা করো।")
                }
            }
        }
    }

    fun clearPendingChatPhoto() {
        localState.update { it.copy(pendingChatPhoto = null) }
    }

    // ==================== Chat Sending (Text + Multimodal Notebook Photo) ====================

    fun sendQuestion(rawQuestion: String, explicitSubject: WbbseSubject? = null) {
        val pendingPhoto = localState.value.pendingChatPhoto
        val trimmed = rawQuestion.trim()
        if ((trimmed.isEmpty() && pendingPhoto == null) || localState.value.isGenerating) return

        val effectiveQuery = trimmed.ifBlank {
            "আমার হাতের লেখা খাতার উত্তরটি WBBSE ক্লাস ১০ অনুযায়ী মূল্যায়ন করো, ভুলত্রুটি দেখাও এবং পূর্ণ নম্বর পাওয়ার পরামর্শ দাও।"
        }

        val snapshot = uiState.value
        val activeSubject = explicitSubject ?: snapshot.selectedChatSubject
        val activeMode = snapshot.inferenceMode
        val config = snapshot.llamaConfig

        ttsManager.stop()
        generationJob?.cancel()
        generationJob = viewModelScope.launch {
            localState.update {
                it.copy(
                    activeTab = AppTab.CHAT,
                    selectedChatSubject = activeSubject,
                    pendingChatPhoto = null,
                    isGenerating = true,
                    errorMessage = null,
                    streamingDraft = StreamingDraft(
                        text = "",
                        sourceLabel = when {
                            activeMode == InferenceMode.OFFLINE -> "অফলাইন WBBSE ও Llama ইঞ্জিন…"
                            pendingPhoto != null -> "খাতার ছবি মূল্যায়ন হচ্ছে (Gemini)…"
                            else -> "অনলাইন Gemini (gemini-3.8-flash)…"
                        },
                        mode = activeMode,
                        subject = activeSubject
                    )
                )
            }

            try {
                val savedImagePath = pendingPhoto?.let {
                    NotebookImageHelper.persistThumbnail(appContext, it.previewBitmap)
                }

                studyRepository.saveChatMessage(
                    ChatMessageEntity(
                        role = "user",
                        content = effectiveQuery,
                        subjectId = activeSubject?.id,
                        mode = activeMode.storageValue,
                        sourceLabel = activeSubject?.bengaliName ?: "সব বিষয়",
                        imagePath = savedImagePath
                    )
                )

                aiRepository.generateAnswerStream(
                    query = effectiveQuery,
                    selectedSubject = activeSubject,
                    mode = activeMode,
                    config = config,
                    imageBitmap = pendingPhoto?.previewBitmap
                ).collect { event ->
                    when (event) {
                        is AiStreamEvent.Partial -> {
                            localState.update { state ->
                                state.copy(
                                    streamingDraft = StreamingDraft(
                                        text = event.accumulatedText,
                                        sourceLabel = event.sourceLabel,
                                        mode = activeMode,
                                        subject = activeSubject
                                    )
                                )
                            }
                        }

                        is AiStreamEvent.Completed -> {
                            studyRepository.saveChatMessage(
                                ChatMessageEntity(
                                    role = "assistant",
                                    content = event.fullText,
                                    subjectId = event.resolvedSubject?.id,
                                    mode = activeMode.storageValue,
                                    sourceLabel = event.sourceLabel,
                                    latencyMs = event.latencyMs
                                )
                            )
                            localState.update { state ->
                                state.copy(
                                    isGenerating = false,
                                    streamingDraft = null
                                )
                            }
                        }
                    }
                }
            } catch (e: TimeoutCancellationException) {
                Log.e("EduAiViewModel", "Online AI response timed out", e)
                localState.update { state ->
                    state.copy(
                        isGenerating = false,
                        streamingDraft = null,
                        errorMessage = "অনলাইন উত্তর ৬০ সেকেন্ডের মধ্যে আসেনি (Timeout)। আবার চেষ্টা করো অথবা অফলাইন মোড ব্যবহার করো।"
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("EduAiViewModel", "AI generation failed", e)
                localState.update { state ->
                    state.copy(
                        isGenerating = false,
                        streamingDraft = null,
                        errorMessage = e.localizedMessage ?: "উত্তর তৈরি করতে সমস্যা হয়েছে।"
                    )
                }
            }
        }
    }

    fun askFromMasterNote(note: MasterDataEntity) {
        val subject = WbbseSubject.fromId(note.subjectId)
        val prompt = "${note.topicTitle} (${note.chapterName}) সহজভাবে বুঝিয়ে দাও এবং পরীক্ষার মূল পয়েন্ট বলো।"
        localState.update {
            it.copy(
                activeTab = AppTab.CHAT,
                selectedChatSubject = subject,
                selectedDetailSubject = null
            )
        }
        sendQuestion(prompt, explicitSubject = subject)
    }

    fun clearChatHistory() {
        ttsManager.stop()
        generationJob?.cancel()
        viewModelScope.launch {
            studyRepository.clearChatHistory()
            localState.update {
                it.copy(
                    isGenerating = false,
                    streamingDraft = null,
                    pendingChatPhoto = null,
                    errorMessage = null
                )
            }
        }
    }

    // ==================== Exam & Score Tracker ====================

    fun selectExamSubject(subject: WbbseSubject) {
        localState.update {
            it.copy(
                selectedExamSubject = subject,
                latestSubjectiveEvaluation = null,
                pendingExamPhoto = null
            )
        }
    }

    fun selectExamFormat(format: ExamFormat) {
        localState.update {
            it.copy(
                selectedExamFormat = format,
                latestSubjectiveEvaluation = null,
                pendingExamPhoto = null
            )
        }
    }

    fun attachExamPhotoUri(uri: Uri) {
        viewModelScope.launch {
            val bitmap = NotebookImageHelper.loadScaledBitmap(appContext, uri)
            if (bitmap != null) {
                localState.update {
                    it.copy(pendingExamPhoto = PendingPhotoAttachment(uri = uri, previewBitmap = bitmap))
                }
            } else {
                localState.update {
                    it.copy(errorMessage = "খাতার ছবিটি লোড করা যায়নি। আবার চেষ্টা করো।")
                }
            }
        }
    }

    fun clearPendingExamPhoto() {
        localState.update { it.copy(pendingExamPhoto = null) }
    }

    fun clearLatestExamEvaluation() {
        localState.update { it.copy(latestSubjectiveEvaluation = null) }
    }

    fun recordMcqResult(
        subject: WbbseSubject,
        correctCount: Int,
        totalQuestions: Int
    ) {
        val safeTotal = totalQuestions.coerceAtLeast(1)
        val accuracy = ((correctCount.toFloat() / safeTotal.toFloat()) * 100f).roundToInt()
        viewModelScope.launch {
            studyRepository.saveExamAttempt(
                ExamAttemptEntity(
                    subjectId = subject.id,
                    examType = ExamFormat.MCQ.id,
                    questionSummary = "${subject.bengaliName} MCQ টেস্ট ($totalQuestions প্রশ্ন)",
                    scoreObtained = correctCount.toFloat(),
                    maxMarks = safeTotal.toFloat(),
                    accuracyPercent = accuracy,
                    feedbackSummary = "সঠিক উত্তর: $correctCount/$safeTotal ($accuracy%)"
                )
            )
        }
    }

    fun evaluateSubjectiveExam(
        subject: WbbseSubject,
        examFormat: ExamFormat,
        questionText: String,
        maxMarks: Int,
        modelAnswer: String,
        keyTerms: List<String>,
        typedAnswer: String
    ) {
        val attachedPhoto = localState.value.pendingExamPhoto
        if (typedAnswer.isBlank() && attachedPhoto == null) {
            localState.update {
                it.copy(errorMessage = "অনুগ্রহ করে উত্তর লেখো অথবা খাতার ছবি যুক্ত করো।")
            }
            return
        }
        if (localState.value.isEvaluatingExam) return

        val mode = uiState.value.inferenceMode
        viewModelScope.launch {
            localState.update {
                it.copy(
                    isEvaluatingExam = true,
                    latestSubjectiveEvaluation = null,
                    errorMessage = null
                )
            }
            try {
                val result = aiRepository.evaluateSubjectiveAnswer(
                    subject = subject,
                    question = questionText,
                    maxMarks = maxMarks,
                    modelAnswer = modelAnswer,
                    keyTerms = keyTerms,
                    studentTypedAnswer = typedAnswer,
                    handwrittenImage = attachedPhoto?.previewBitmap,
                    mode = mode
                )

                val accuracy = if (result.maxMarks > 0f) {
                    ((result.scoreObtained / result.maxMarks) * 100f).roundToInt().coerceIn(0, 100)
                } else {
                    0
                }

                studyRepository.saveExamAttempt(
                    ExamAttemptEntity(
                        subjectId = subject.id,
                        examType = examFormat.id,
                        questionSummary = questionText.take(72),
                        scoreObtained = result.scoreObtained,
                        maxMarks = result.maxMarks,
                        accuracyPercent = accuracy,
                        feedbackSummary = "${result.verdict} (${result.evaluatorSource})"
                    )
                )

                localState.update {
                    it.copy(
                        isEvaluatingExam = false,
                        latestSubjectiveEvaluation = result
                    )
                }
            } catch (e: TimeoutCancellationException) {
                Log.e("EduAiViewModel", "Exam evaluation timed out", e)
                localState.update {
                    it.copy(
                        isEvaluatingExam = false,
                        errorMessage = "মূল্যায়ন ৬০ সেকেন্ডের মধ্যে শেষ হয়নি। আবার চেষ্টা করো।"
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("EduAiViewModel", "Exam evaluation failed", e)
                localState.update {
                    it.copy(
                        isEvaluatingExam = false,
                        errorMessage = e.localizedMessage ?: "উত্তর মূল্যায়ন করতে সমস্যা হয়েছে।"
                    )
                }
            }
        }
    }

    fun clearExamHistory() {
        viewModelScope.launch {
            studyRepository.clearExamHistory()
            localState.update { it.copy(latestSubjectiveEvaluation = null) }
        }
    }

    // ==================== Master Data & GGUF Model Hub ====================

    fun toggleBookmark(note: MasterDataEntity) {
        viewModelScope.launch {
            studyRepository.toggleBookmark(note.id, !note.isBookmarked)
        }
    }

    fun addCustomNote(
        subject: WbbseSubject,
        chapterName: String,
        topicTitle: String,
        content: String,
        keyFact: String
    ) {
        if (topicTitle.isBlank() || content.isBlank()) return
        viewModelScope.launch {
            studyRepository.addCustomStudyNote(
                MasterDataEntity(
                    subjectId = subject.id,
                    chapterName = chapterName.ifBlank { "আমার নোট (${subject.bengaliName})" },
                    topicTitle = topicTitle.trim(),
                    keywords = "$topicTitle, $chapterName, ${subject.bengaliName}, ${subject.englishName}",
                    content = content.trim(),
                    keyFormulaOrFact = keyFact.ifBlank { "নিজস্ব স্টাডি নোট" },
                    examTip = "নিজের তৈরি নোট নিয়মিত রিভিশন করলে মাধ্যমিকে ভালো ফল হয়।",
                    isUserCreated = true
                )
            )
        }
    }

    fun deleteCustomNote(noteId: Long) {
        viewModelScope.launch {
            studyRepository.deleteCustomStudyNote(noteId)
        }
    }

    fun importGgufFromUri(uri: Uri) {
        val config = uiState.value.llamaConfig
        viewModelScope.launch {
            val result = ggufModelManager.importFromSafUri(uri, config)
            result.onSuccess { metadata ->
                settingsDataStore.setActiveModelPath(metadata.absolutePath)
            }
        }
    }

    fun scanGgufModels() {
        val snapshot = uiState.value
        viewModelScope.launch {
            val loadedPath = ggufModelManager.scanAndAutoDetect(
                savedActivePath = snapshot.ggufState.activeMetadata?.absolutePath,
                config = snapshot.llamaConfig
            )
            settingsDataStore.setActiveModelPath(loadedPath)
        }
    }

    fun createDiagnosticGgufTestFile() {
        val config = uiState.value.llamaConfig
        viewModelScope.launch {
            val metadata = ggufModelManager.createAndLoadDiagnosticGguf(config)
            settingsDataStore.setActiveModelPath(metadata.absolutePath)
        }
    }

    fun loadModelFile(path: String) {
        val config = uiState.value.llamaConfig
        viewModelScope.launch {
            val meta = ggufModelManager.loadModelByPath(path, config)
            if (meta != null && meta.isValidMagic) {
                settingsDataStore.setActiveModelPath(meta.absolutePath)
            }
        }
    }

    fun unloadModel() {
        viewModelScope.launch {
            ggufModelManager.unloadCurrentModel()
            settingsDataStore.setActiveModelPath(null)
        }
    }

    fun deleteModelFile(path: String) {
        viewModelScope.launch {
            ggufModelManager.deleteModelFile(path)
            if (uiState.value.ggufState.activeMetadata?.absolutePath == path) {
                settingsDataStore.setActiveModelPath(null)
            }
        }
    }

    fun updateLlamaConfig(config: LlamaInferenceConfig) {
        viewModelScope.launch {
            settingsDataStore.updateLlamaConfig(config)
        }
    }

    fun dismissError() {
        localState.update { it.copy(errorMessage = null) }
    }

    fun dismissModelStatusBanner() {
        ggufModelManager.clearStatusBanner()
    }

    private data class DataBundle(
        val settings: StoredAppSettings,
        val gguf: GgufManagerState,
        val notes: List<MasterDataEntity>,
        val messages: List<ChatMessageEntity>,
        val isOnline: Boolean
    )

    private data class ExtraBundle(
        val tts: TtsUiState,
        val attempts: List<ExamAttemptEntity>
    )
}

private fun computeProgressStats(attempts: List<ExamAttemptEntity>): OverallProgressStats {
    val totalTests = attempts.size
    val totalObtained = attempts.sumOf { it.scoreObtained.toDouble() }.toFloat()
    val totalPossible = attempts.sumOf { it.maxMarks.toDouble() }.toFloat()
    val overallAccuracy = if (totalPossible > 0f) {
        ((totalObtained / totalPossible) * 100f).roundToInt().coerceIn(0, 100)
    } else {
        0
    }

    val summaries = WbbseSubject.entries.map { subject ->
        val subjectAttempts = attempts.filter { it.subjectId == subject.id }
        val obt = subjectAttempts.sumOf { it.scoreObtained.toDouble() }.toFloat()
        val max = subjectAttempts.sumOf { it.maxMarks.toDouble() }.toFloat()
        val acc = if (max > 0f) ((obt / max) * 100f).roundToInt().coerceIn(0, 100) else 0
        SubjectProgressSummary(
            subject = subject,
            attemptsCount = subjectAttempts.size,
            totalObtained = obt,
            totalPossible = max,
            accuracyPercent = acc
        )
    }

    val attemptedWeak = summaries.filter { it.attemptsCount > 0 && it.accuracyPercent < 65 }
        .sortedBy { it.accuracyPercent }
    val unpracticed = summaries.filter { it.attemptsCount == 0 }
    val weakSubjects = if (attemptedWeak.isNotEmpty()) attemptedWeak else unpracticed.take(3)

    return OverallProgressStats(
        totalTestsTaken = totalTests,
        overallAccuracyPercent = overallAccuracy,
        totalMarksObtained = totalObtained,
        totalMarksPossible = totalPossible,
        subjectSummaries = summaries,
        weakSubjects = weakSubjects
    )
}
