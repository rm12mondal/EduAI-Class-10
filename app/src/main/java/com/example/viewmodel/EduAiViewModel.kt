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
import com.example.data.ai.ExamIntentRouter
import com.example.data.ai.HybridEduAiRepository
import com.example.data.ai.SmartLearningService
import com.example.data.gguf.GgufManagerState
import com.example.data.gguf.GgufModelManager
import com.example.data.local.ChatMessageEntity
import com.example.data.local.DailyPlanTaskEntity
import com.example.data.local.EduAiDatabase
import com.example.data.local.ExamAttemptEntity
import com.example.data.local.MasterDataEntity
import com.example.data.local.SettingsDataStore
import com.example.data.local.StoredAppSettings
import com.example.data.local.StudyRepository
import com.example.data.media.NotebookImageHelper
import com.example.data.speech.EduSpeechRecognizer
import com.example.data.speech.EduTtsManager
import com.example.data.speech.PodcastUiState
import com.example.data.speech.SpeechRecognitionUiState
import com.example.data.speech.TtsUiState
import com.example.data.speech.VoiceInputLanguage
import com.example.ui.hub.MindMapParser
import com.example.ui.hub.MindMapTree
import com.example.model.ActiveExamSession
import com.example.model.AppTab
import com.example.model.AudioRevisionUiState
import com.example.model.ChatPromptOptions
import com.example.model.DailyPlannerUiState
import com.example.model.ExamChatIntent
import com.example.model.ExamChatMessage
import com.example.model.ExamFormat
import com.example.model.ExamSessionMode
import com.example.model.InferenceMode
import com.example.model.LlamaInferenceConfig
import com.example.model.MapBoardDomain
import com.example.model.MapBoardUiState
import com.example.model.OverallProgressStats
import com.example.model.PlanActionType
import com.example.model.RapidFireTimerMode
import com.example.model.RapidFireUiState
import com.example.model.SmartLearningMessage
import com.example.model.SmartLearningUiState
import com.example.model.SubjectProgressSummary
import com.example.model.SubjectiveEvaluationResult
import com.example.model.TimingBucket
import com.example.data.local.BrahmastramProgressEntity
import com.example.data.local.SpacedRevisionEntity
import com.example.data.master.StrictWbbsePrecisionEngine
import com.example.data.pdf.SavedPdfItem
import com.example.model.AiVivaUiState
import com.example.model.BrahmastramUiState
import com.example.model.ChatSection
import com.example.model.ConfusionCleanerUiState
import com.example.model.DiagramLabUiState
import com.example.model.FastLearningChapterItem
import com.example.model.FastLearningContextHelper
import com.example.model.FastLearningUiState
import com.example.model.LanguageLabMode
import com.example.model.LanguageLabUiState
import com.example.model.MathSolverModule
import com.example.model.MathSolverUiState
import com.example.model.PocketBookCategory
import com.example.model.PocketBookUiState
import com.example.model.SavedPdfLibraryUiState
import com.example.model.ScorePredictorSummary
import com.example.model.SectionChatUiState
import com.example.model.SpacedRevisionUiState
import com.example.model.SyllabusPlanOption
import com.example.model.TopperSectionType
import com.example.model.TopperSubjectTrack
import com.example.model.TtsVoiceSettings
import com.example.model.WbbseSubject
import com.example.model.WritingFormatType
import com.example.model.WritingLanguage
import com.example.model.WritingStudioUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
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
    val subject: WbbseSubject?,
    val isGeneratingDiagram: Boolean = false
)

data class PendingPhotoAttachment(
    val uri: Uri,
    val previewBitmap: Bitmap
)

data class ExamChatUiState(
    val messages: List<ExamChatMessage> = emptyList(),
    val streamingDraft: StreamingDraft? = null,
    val isGenerating: Boolean = false,
    val pendingPhoto: PendingPhotoAttachment? = null,
    val smartToThePointMode: Boolean = true,
    val visualDiagramMode: Boolean = false,
    val translateMode: Boolean = false,
    val targetMarks: Int? = null,
    val activeSession: ActiveExamSession? = null
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
    val chatSmartToThePoint: Boolean = false,
    val chatVisualMode: Boolean = false,
    val chatTranslateMode: Boolean = false,
    val exactTextbookMode: Boolean = false,
    val chatTargetMarks: Int? = null,
    val ttsState: TtsUiState = TtsUiState(),
    val ttsVoiceSettings: TtsVoiceSettings = TtsVoiceSettings(),
    val examAttempts: List<ExamAttemptEntity> = emptyList(),
    val progressStats: OverallProgressStats = computeProgressStats(emptyList()),
    val scorePredictor: ScorePredictorSummary = StrictWbbsePrecisionEngine.computeScorePredictor(emptyList()),
    val selectedExamSubject: WbbseSubject = WbbseSubject.PHYSICAL_SCIENCE,
    val selectedExamFormat: ExamFormat = ExamFormat.MCQ,
    val isEvaluatingExam: Boolean = false,
    val latestSubjectiveEvaluation: SubjectiveEvaluationResult? = null,
    val pendingExamPhoto: PendingPhotoAttachment? = null,
    val examChat: ExamChatUiState = ExamChatUiState(),
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
    val chatSmartToThePoint: Boolean = false,
    val chatVisualMode: Boolean = false,
    val chatTranslateMode: Boolean = false,
    val exactTextbookMode: Boolean = false,
    val chatTargetMarks: Int? = null,
    val selectedExamSubject: WbbseSubject = WbbseSubject.PHYSICAL_SCIENCE,
    val selectedExamFormat: ExamFormat = ExamFormat.MCQ,
    val isEvaluatingExam: Boolean = false,
    val latestSubjectiveEvaluation: SubjectiveEvaluationResult? = null,
    val pendingExamPhoto: PendingPhotoAttachment? = null,
    val examChat: ExamChatUiState = ExamChatUiState(),
    val errorMessage: String? = null
)

private data class FastLearningLocalNav(
    val isOpen: Boolean = false,
    val activeTabIndex: Int = 0,
    val selectedSubject: WbbseSubject? = null
)

class EduAiViewModel(application: Application) : AndroidViewModel(application) {

    private val appContext = application.applicationContext
    private val database = EduAiDatabase.getInstance(application)
    private val studyRepository = StudyRepository(
        masterDataDao = database.masterDataDao(),
        chatMessageDao = database.chatMessageDao(),
        examAttemptDao = database.examAttemptDao(),
        topperDao = database.topperDao(),
        dailyPlannerDao = database.dailyPlannerDao(),
        masterUpgradeDao = database.masterUpgradeDao()
    )
    private val settingsDataStore = SettingsDataStore(application)
    private val ggufModelManager = GgufModelManager(application)
    private val networkMonitor = NetworkMonitor(application)
    private val ttsManager = EduTtsManager(application)
    val podcastState: StateFlow<PodcastUiState> = ttsManager.podcastState

    private val _voiceInjectedText = MutableStateFlow<String?>(null)
    val voiceInjectedText: StateFlow<String?> = _voiceInjectedText

    private val _mindMapTree = MutableStateFlow<MindMapTree?>(null)
    val mindMapTree: StateFlow<MindMapTree?> = _mindMapTree

    private val _fastLearningLocalNav = MutableStateFlow(FastLearningLocalNav())
    val fastLearningState: StateFlow<FastLearningUiState> = combine(
        _fastLearningLocalNav,
        settingsDataStore.settingsFlow
    ) { nav, stored ->
        FastLearningUiState(
            isOpen = nav.isOpen,
            activeTabIndex = nav.activeTabIndex,
            selectedSubject = nav.selectedSubject,
            targetExamYear = stored.fastLearningExamYear,
            availableExamYears = FastLearningContextHelper.availableExamYears(),
            syllabusPlan = SyllabusPlanOption.fromMonths(stored.fastLearningPlanMonths),
            completedChapterKeys = stored.fastLearningCompletedChapters
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = FastLearningUiState()
    )

    val allBrahmastramProgress: StateFlow<List<BrahmastramProgressEntity>> =
        studyRepository.allBrahmastramProgress.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = emptyList()
        )

    private val speechRecognizer = EduSpeechRecognizer(
        context = application,
        onFinalTranscript = { spoken ->
            _voiceInjectedText.value = spoken
            val inActiveChatContext = fastLearningState.value.isOpen ||
                brahmastramState.value.isOpen ||
                mindMapTree.value != null ||
                smartLearningState.value.isOpen ||
                localState.value.activeTab == AppTab.EXAMS ||
                localState.value.activeTab == AppTab.CHAT
            if (!inActiveChatContext) {
                closeAllOverlaysForShortcut()
                localState.update { it.copy(activeTab = AppTab.CHAT) }
            }
        }
    )
    val speechState: StateFlow<SpeechRecognitionUiState> = speechRecognizer.state

    private val aiRepository: AiRepository = HybridEduAiRepository(
        studyRepository = studyRepository,
        ggufModelManager = ggufModelManager
    )

    private val sectionChatController = SectionChatController(
        appContext = appContext,
        scope = viewModelScope,
        studyRepository = studyRepository,
        aiRepository = aiRepository,
        getInferenceMode = { uiState.value.inferenceMode },
        getLlamaConfig = { uiState.value.llamaConfig },
        getExactTextbookMode = { uiState.value.exactTextbookMode },
        getTargetExamYear = { fastLearningState.value.targetExamYear },
        getSyllabusPlanLabel = { fastLearningState.value.syllabusPlan.labelEn }
    )

    val brahmastramChatState: StateFlow<SectionChatUiState> = sectionChatController.brahmastramChatState
    val mindMapChatState: StateFlow<SectionChatUiState> = sectionChatController.mindMapChatState
    val fastLearningChatState: StateFlow<SectionChatUiState> = sectionChatController.fastLearningChatState

    private val topperController = TopperController(
        appContext = appContext,
        scope = viewModelScope,
        studyRepository = studyRepository,
        ggufModelManager = ggufModelManager
    )

    val topperState: StateFlow<TopperUiState> = topperController.uiState

    private val smartLearningService = SmartLearningService(
        studyRepository = studyRepository,
        ggufModelManager = ggufModelManager
    )

    private val smartLearningController = SmartLearningController(
        appContext = appContext,
        scope = viewModelScope,
        studyRepository = studyRepository,
        aiRepository = aiRepository,
        smartLearningService = smartLearningService,
        getTargetExamYear = { fastLearningState.value.targetExamYear },
        getSyllabusPlanLabel = { fastLearningState.value.syllabusPlan.labelEn }
    )

    val smartLearningState: StateFlow<SmartLearningUiState> = smartLearningController.uiState

    private val topperExtrasController = TopperExtrasController(
        scope = viewModelScope,
        studyRepository = studyRepository,
        ggufModelManager = ggufModelManager,
        ttsManager = ttsManager,
        getCurrentVoiceSettings = { uiState.value.ttsVoiceSettings },
        getCurrentInferenceMode = { uiState.value.inferenceMode }
    )

    val plannerState: StateFlow<DailyPlannerUiState> = topperExtrasController.plannerState
    val mapBoardState: StateFlow<MapBoardUiState> = topperExtrasController.mapBoardState
    val rapidFireState: StateFlow<RapidFireUiState> = topperExtrasController.rapidFireState
    val audioRevisionState: StateFlow<AudioRevisionUiState> = topperExtrasController.audioRevisionState

    private val masterUpgradeController = MasterUpgradeController(
        appContext = appContext,
        scope = viewModelScope,
        studyRepository = studyRepository,
        ggufModelManager = ggufModelManager,
        ttsManager = ttsManager,
        getMasterNotes = { uiState.value.masterNotes },
        getInferenceMode = { uiState.value.inferenceMode },
        getVoiceSettings = { uiState.value.ttsVoiceSettings }
    )

    val pdfState: StateFlow<SavedPdfLibraryUiState> = masterUpgradeController.pdfState
    val brahmastramState: StateFlow<BrahmastramUiState> = masterUpgradeController.brahmastramState
    val pocketBookState: StateFlow<PocketBookUiState> = masterUpgradeController.pocketBookState
    val vivaState: StateFlow<AiVivaUiState> = masterUpgradeController.vivaState
    val confusionState: StateFlow<ConfusionCleanerUiState> = masterUpgradeController.confusionState
    val spacedRevisionState: StateFlow<SpacedRevisionUiState> = masterUpgradeController.spacedRevisionState
    val diagramLabState: StateFlow<DiagramLabUiState> = masterUpgradeController.diagramLabState
    val writingStudioState: StateFlow<WritingStudioUiState> = masterUpgradeController.writingStudioState
    val languageLabState: StateFlow<LanguageLabUiState> = masterUpgradeController.languageLabState
    val mathSolverState: StateFlow<MathSolverUiState> = masterUpgradeController.mathSolverState
    val backupStatusMessage: StateFlow<String?> = masterUpgradeController.backupStatusMessage

    private val localState = MutableStateFlow(LocalUiNavigationState())
    private var generationJob: Job? = null
    private var examChatJob: Job? = null
    private var examHallTimerJob: Job? = null
    private var nextExamChatMsgId: Long = 10_000L

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
            chatSmartToThePoint = nav.chatSmartToThePoint,
            chatVisualMode = nav.chatVisualMode,
            chatTranslateMode = nav.chatTranslateMode,
            exactTextbookMode = nav.exactTextbookMode,
            chatTargetMarks = nav.chatTargetMarks,
            ttsState = extra.tts,
            ttsVoiceSettings = data.settings.ttsSettings,
            examAttempts = extra.attempts,
            progressStats = computeProgressStats(extra.attempts),
            scorePredictor = StrictWbbsePrecisionEngine.computeScorePredictor(extra.attempts),
            selectedExamSubject = nav.selectedExamSubject,
            selectedExamFormat = nav.selectedExamFormat,
            isEvaluatingExam = nav.isEvaluatingExam,
            latestSubjectiveEvaluation = nav.latestSubjectiveEvaluation,
            pendingExamPhoto = nav.pendingExamPhoto,
            examChat = nav.examChat,
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
            // Restore persisted Mock Exam chat messages from Room
            val savedExamMsgs = studyRepository.getSectionMessagesSnapshot(ChatSection.MOCK_EXAM)
            if (savedExamMsgs.isNotEmpty()) {
                val restoredExamMsgs = savedExamMsgs.map { entity ->
                    val restoredIntent = ExamChatIntent.entries.firstOrNull { it.name == entity.topicLabel }
                        ?: ExamChatIntent.DIRECT_STUDY
                    ExamChatMessage(
                        id = entity.id,
                        role = entity.role,
                        content = entity.content,
                        intent = restoredIntent,
                        subject = WbbseSubject.fromId(entity.subjectId),
                        mode = InferenceMode.fromStorage(entity.mode),
                        sourceLabel = entity.sourceLabel,
                        userPhotoPath = entity.imagePath,
                        generatedDiagramPath = entity.diagramPath,
                        latencyMs = entity.latencyMs,
                        timestamp = entity.timestamp
                    )
                }
                localState.update { state ->
                    state.copy(examChat = state.examChat.copy(messages = restoredExamMsgs))
                }
            }
            val allNotesSnapshot = studyRepository.getMasterDataSnapshot(null)
            topperExtrasController.ensureTodayPlanInitialized(allNotesSnapshot)
            masterUpgradeController.ensureSpacedRevisionSeeded(allNotesSnapshot)
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
        examHallTimerJob?.cancel()
        sectionChatController.stopAll()
        topperController.stopTimer()
        smartLearningController.stopAll()
        topperExtrasController.stopAll()
        masterUpgradeController.stopAll()
        speechRecognizer.destroy()
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

    // ==================== Main Chat Smart / Visual / Translate Toggles ====================

    fun toggleChatSmartToThePoint() {
        localState.update { it.copy(chatSmartToThePoint = !it.chatSmartToThePoint) }
    }

    fun toggleChatVisualMode() {
        localState.update { it.copy(chatVisualMode = !it.chatVisualMode) }
    }

    fun toggleChatTranslateMode() {
        localState.update { it.copy(chatTranslateMode = !it.chatTranslateMode) }
    }

    fun toggleExactTextbookMode() {
        localState.update { it.copy(exactTextbookMode = !it.exactTextbookMode) }
    }

    fun setChatTargetMarks(marks: Int?) {
        localState.update { it.copy(chatTargetMarks = marks) }
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

    // ==================== Main Chat Sending (Text + Photo + Smart Answer + AI Visual Diagram) ====================

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
        val flSnapshot = fastLearningState.value
        val options = ChatPromptOptions(
            smartToThePoint = snapshot.chatSmartToThePoint,
            visualDiagramRequested = snapshot.chatVisualMode,
            translateMode = snapshot.chatTranslateMode,
            targetMarks = snapshot.chatTargetMarks,
            exactTextbookDefinition = snapshot.exactTextbookMode,
            targetExamYear = flSnapshot.targetExamYear,
            syllabusPlanLabel = flSnapshot.syllabusPlan.labelEn
        )
        val analysis = ExamIntentRouter.analyzeForMainChat(effectiveQuery, activeSubject, options)
        val shouldGenerateDiagram = analysis.effectiveVisualDiagram && activeMode == InferenceMode.ONLINE

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
                            shouldGenerateDiagram -> "🎨 AI ভিজ্যুয়াল ব্যাখ্যা ও ডায়াগ্রাম তৈরি হচ্ছে…"
                            analysis.effectiveSmartAnswer -> "🎯 টু-দ্য-পয়েন্ট বোর্ড উত্তর তৈরি হচ্ছে…"
                            else -> "অনলাইন Gemini (gemini-3.8-flash)…"
                        },
                        mode = activeMode,
                        subject = activeSubject,
                        isGeneratingDiagram = shouldGenerateDiagram
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

                var completedEvent: AiStreamEvent.Completed? = null

                aiRepository.generateAnswerStream(
                    query = effectiveQuery,
                    selectedSubject = activeSubject,
                    mode = activeMode,
                    config = config,
                    imageBitmap = pendingPhoto?.previewBitmap,
                    options = options
                ).collect { event ->
                    when (event) {
                        is AiStreamEvent.Partial -> {
                            localState.update { state ->
                                state.copy(
                                    streamingDraft = StreamingDraft(
                                        text = event.accumulatedText,
                                        sourceLabel = event.sourceLabel,
                                        mode = activeMode,
                                        subject = activeSubject,
                                        isGeneratingDiagram = shouldGenerateDiagram
                                    )
                                )
                            }
                        }

                        is AiStreamEvent.Completed -> {
                            completedEvent = event
                        }
                    }
                }

                val finalEvent = completedEvent ?: throw IllegalStateException("উত্তর পাওয়া যায়নি।")
                var diagramPath: String? = null
                var finalContent = finalEvent.fullText
                var finalSourceLabel = finalEvent.sourceLabel

                if (shouldGenerateDiagram) {
                    localState.update { state ->
                        state.copy(
                            streamingDraft = StreamingDraft(
                                text = finalContent,
                                sourceLabel = "🎨 লেবেলযুক্ত ২ডি ডায়াগ্রাম আঁকা হচ্ছে (Gemini Nano Banana 2.1)…",
                                mode = activeMode,
                                subject = finalEvent.resolvedSubject ?: activeSubject,
                                isGeneratingDiagram = true
                            )
                        )
                    }
                    try {
                        val diagramBitmap = aiRepository.generateVisualDiagram(
                            conceptQuery = effectiveQuery,
                            subject = finalEvent.resolvedSubject ?: activeSubject,
                            mode = activeMode
                        )
                        diagramPath = NotebookImageHelper.persistVisualDiagram(appContext, diagramBitmap)
                        finalSourceLabel = "🎨 AI ভিজ্যুয়াল ডায়াগ্রাম • Gemini"
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        Log.e("EduAiViewModel", "AI Visual Diagram failed", e)
                        finalContent += "\n\n⚠️ ডায়াগ্রাম জেনারেশন ত্রুটি: ${e.localizedMessage ?: "ছবি তৈরি করা যায়নি।"}"
                    }
                }

                studyRepository.saveChatMessage(
                    ChatMessageEntity(
                        role = "assistant",
                        content = finalContent,
                        subjectId = finalEvent.resolvedSubject?.id,
                        mode = activeMode.storageValue,
                        sourceLabel = finalSourceLabel,
                        latencyMs = finalEvent.latencyMs,
                        imagePath = diagramPath
                    )
                )
                localState.update { state ->
                    state.copy(
                        isGenerating = false,
                        streamingDraft = null
                    )
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

    // ==================== 3-in-1 Smart Exam Chat Box (Intent A / B / C + Timer + Camera + Visual) ====================

    fun toggleExamSmartMode() {
        localState.update {
            it.copy(examChat = it.examChat.copy(smartToThePointMode = !it.examChat.smartToThePointMode))
        }
    }

    fun toggleExamVisualMode() {
        localState.update {
            it.copy(examChat = it.examChat.copy(visualDiagramMode = !it.examChat.visualDiagramMode))
        }
    }

    fun toggleExamTranslateMode() {
        localState.update {
            it.copy(examChat = it.examChat.copy(translateMode = !it.examChat.translateMode))
        }
    }

    fun setExamTargetMarks(marks: Int?) {
        localState.update {
            it.copy(examChat = it.examChat.copy(targetMarks = marks))
        }
    }

    fun attachExamChatPhotoUri(uri: Uri) {
        viewModelScope.launch {
            val bitmap = NotebookImageHelper.loadScaledBitmap(appContext, uri)
            if (bitmap != null) {
                localState.update {
                    it.copy(
                        examChat = it.examChat.copy(
                            pendingPhoto = PendingPhotoAttachment(uri = uri, previewBitmap = bitmap)
                        )
                    )
                }
            } else {
                localState.update {
                    it.copy(errorMessage = "খাতার ছবি লোড করা যায়নি। আবার চেষ্টা করো।")
                }
            }
        }
    }

    fun clearPendingExamChatPhoto() {
        localState.update {
            it.copy(examChat = it.examChat.copy(pendingPhoto = null))
        }
    }

    fun toggleExamSessionPause() {
        localState.update { state ->
            val session = state.examChat.activeSession ?: return@update state
            state.copy(
                examChat = state.examChat.copy(
                    activeSession = session.copy(isPaused = !session.isPaused)
                )
            )
        }
    }

    fun endExamSession() {
        examHallTimerJob?.cancel()
        localState.update { state ->
            state.copy(examChat = state.examChat.copy(activeSession = null))
        }
    }

    fun clearExamChat() {
        ttsManager.stop()
        examChatJob?.cancel()
        examHallTimerJob?.cancel()
        viewModelScope.launch {
            studyRepository.clearSectionChat(ChatSection.MOCK_EXAM)
            localState.update { state ->
                state.copy(
                    examChat = state.examChat.copy(
                        messages = emptyList(),
                        streamingDraft = null,
                        isGenerating = false,
                        pendingPhoto = null,
                        activeSession = null
                    )
                )
            }
        }
    }

    fun sendExamChatMessage(rawText: String) {
        val currentExamChat = localState.value.examChat
        val pendingPhoto = currentExamChat.pendingPhoto
        val trimmed = rawText.trim()
        if ((trimmed.isEmpty() && pendingPhoto == null) || currentExamChat.isGenerating) return

        val effectiveQuery = trimmed.ifBlank {
            val m = currentExamChat.targetMarks ?: 5
            "আমার হাতের লেখা এই ${m} নম্বরের উত্তরটি WBBSE মাধ্যমিক মান অনুযায়ী মূল্যায়ন করো, কতটুকু ঠিক/ভুল হয়েছে বলো এবং সঠিক নম্বর দাও।"
        }

        val snapshot = uiState.value
        val flSnapshot = fastLearningState.value
        val activeMode = snapshot.inferenceMode
        val options = ChatPromptOptions(
            smartToThePoint = currentExamChat.smartToThePointMode,
            visualDiagramRequested = currentExamChat.visualDiagramMode,
            translateMode = currentExamChat.translateMode,
            targetMarks = currentExamChat.targetMarks,
            exactTextbookDefinition = snapshot.exactTextbookMode,
            targetExamYear = flSnapshot.targetExamYear,
            syllabusPlanLabel = flSnapshot.syllabusPlan.labelEn
        )
        val analysis = ExamIntentRouter.analyzeForExamChat(
            query = effectiveQuery,
            selectedSubject = snapshot.selectedExamSubject,
            hasPhotoAttachment = pendingPhoto != null,
            activeSession = currentExamChat.activeSession,
            options = options
        )
        val resolvedSubject = analysis.detectedSubject ?: snapshot.selectedExamSubject
        val shouldGenerateDiagram = analysis.effectiveVisualDiagram && activeMode == InferenceMode.ONLINE

        // Activate or update Normal Test / Madhyamik Exam Hall session if triggered
        val isStartingNewNormal = analysis.intent == ExamChatIntent.NORMAL_TEST &&
            (currentExamChat.activeSession == null || currentExamChat.activeSession.mode != ExamSessionMode.NORMAL_UNTIMED ||
                effectiveQuery.contains("টেস্ট") || effectiveQuery.lowercase().contains("test"))
        val isStartingNewMadhyamik = analysis.intent == ExamChatIntent.MADHYAMIK_TEST &&
            (currentExamChat.activeSession == null || currentExamChat.activeSession.mode != ExamSessionMode.MADHYAMIK_TIMED ||
                effectiveQuery.contains("মাধ্যমিক") || effectiveQuery.lowercase().contains("madhyamik") ||
                effectiveQuery.lowercase().contains("previous year") || effectiveQuery.contains("বিগত"))

        if (isStartingNewNormal) {
            examHallTimerJob?.cancel()
            val session = ActiveExamSession(
                mode = ExamSessionMode.NORMAL_UNTIMED,
                subject = resolvedSubject,
                title = "নরমাল টেস্ট — ${resolvedSubject.bengaliName}",
                chapterHint = analysis.chapterHint,
                pageHint = analysis.pageHint,
                fullMarks = 15,
                totalDurationSeconds = 0L,
                remainingSeconds = 0L
            )
            localState.update { it.copy(examChat = it.examChat.copy(activeSession = session)) }
        } else if (isStartingNewMadhyamik) {
            val totalSeconds = analysis.recommendedMadhyamikDurationMinutes * 60L
            val session = ActiveExamSession(
                mode = ExamSessionMode.MADHYAMIK_TIMED,
                subject = resolvedSubject,
                title = "মাধ্যমিক পরীক্ষা হল — ${resolvedSubject.bengaliName}",
                chapterHint = analysis.chapterHint,
                pageHint = analysis.pageHint,
                fullMarks = if (analysis.recommendedMadhyamikDurationMinutes >= 180) 90 else 40,
                totalDurationSeconds = totalSeconds,
                remainingSeconds = totalSeconds,
                isPaused = false,
                isTimeUp = false
            )
            startExamHallCountdown(session)
        }

        ttsManager.stop()
        examChatJob?.cancel()
        examChatJob = viewModelScope.launch {
            val savedPhotoPath = pendingPhoto?.let {
                NotebookImageHelper.persistThumbnail(appContext, it.previewBitmap)
            }

            val savedUserId = studyRepository.saveChatMessage(
                ChatMessageEntity(
                    role = "user",
                    content = effectiveQuery,
                    subjectId = resolvedSubject.id,
                    mode = activeMode.storageValue,
                    sourceLabel = analysis.intent.badgeBn,
                    imagePath = savedPhotoPath,
                    sectionId = ChatSection.MOCK_EXAM.id,
                    topicLabel = analysis.intent.name
                )
            )

            val userMsg = ExamChatMessage(
                id = if (savedUserId > 0L) savedUserId else ++nextExamChatMsgId,
                role = "user",
                content = effectiveQuery,
                intent = analysis.intent,
                subject = resolvedSubject,
                mode = activeMode,
                sourceLabel = analysis.intent.badgeBn,
                userPhotoPath = savedPhotoPath
            )

            localState.update { state ->
                state.copy(
                    errorMessage = null,
                    examChat = state.examChat.copy(
                        messages = state.examChat.messages + userMsg,
                        pendingPhoto = null,
                        isGenerating = true,
                        streamingDraft = StreamingDraft(
                            text = "",
                            sourceLabel = "${analysis.intent.badgeBn} তৈরি হচ্ছে…",
                            mode = activeMode,
                            subject = resolvedSubject,
                            isGeneratingDiagram = shouldGenerateDiagram
                        )
                    )
                )
            }

            try {
                var completedEvent: AiStreamEvent.Completed? = null

                aiRepository.generateExamAssistantStream(
                    query = effectiveQuery,
                    analysis = analysis,
                    activeSession = localState.value.examChat.activeSession,
                    mode = activeMode,
                    config = snapshot.llamaConfig,
                    handwrittenImage = pendingPhoto?.previewBitmap
                ).collect { event ->
                    when (event) {
                        is AiStreamEvent.Partial -> {
                            localState.update { state ->
                                state.copy(
                                    examChat = state.examChat.copy(
                                        streamingDraft = StreamingDraft(
                                            text = event.accumulatedText,
                                            sourceLabel = event.sourceLabel,
                                            mode = activeMode,
                                            subject = resolvedSubject,
                                            isGeneratingDiagram = shouldGenerateDiagram
                                        )
                                    )
                                )
                            }
                        }

                        is AiStreamEvent.Completed -> completedEvent = event
                    }
                }

                val finalEvent = completedEvent ?: throw IllegalStateException("উত্তর পাওয়া যায়নি।")
                var diagramPath: String? = null
                var diagramErr: String? = null

                if (shouldGenerateDiagram) {
                    localState.update { state ->
                        state.copy(
                            examChat = state.examChat.copy(
                                streamingDraft = StreamingDraft(
                                    text = finalEvent.fullText,
                                    sourceLabel = "🎨 লেবেলযুক্ত ২ডি ডায়াগ্রাম আঁকা হচ্ছে (Gemini Nano Banana 2.1)…",
                                    mode = activeMode,
                                    subject = finalEvent.resolvedSubject ?: resolvedSubject,
                                    isGeneratingDiagram = true
                                )
                            )
                        )
                    }
                    try {
                        val bmp = aiRepository.generateVisualDiagram(
                            conceptQuery = effectiveQuery,
                            subject = finalEvent.resolvedSubject ?: resolvedSubject,
                            mode = activeMode
                        )
                        diagramPath = NotebookImageHelper.persistVisualDiagram(appContext, bmp)
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        Log.e("EduAiViewModel", "Exam Chat Visual Diagram failed", e)
                        diagramErr = e.localizedMessage ?: "ভিজ্যুয়াল ডায়াগ্রাম তৈরি করা যায়নি।"
                    }
                }

                val savedAssistantId = studyRepository.saveChatMessage(
                    ChatMessageEntity(
                        role = "assistant",
                        content = finalEvent.fullText,
                        subjectId = (finalEvent.resolvedSubject ?: resolvedSubject).id,
                        mode = activeMode.storageValue,
                        sourceLabel = finalEvent.sourceLabel,
                        latencyMs = finalEvent.latencyMs,
                        sectionId = ChatSection.MOCK_EXAM.id,
                        diagramPath = diagramPath,
                        topicLabel = analysis.intent.name
                    )
                )

                val assistantMsg = ExamChatMessage(
                    id = if (savedAssistantId > 0L) savedAssistantId else ++nextExamChatMsgId,
                    role = "assistant",
                    content = finalEvent.fullText,
                    intent = analysis.intent,
                    subject = finalEvent.resolvedSubject ?: resolvedSubject,
                    mode = activeMode,
                    sourceLabel = finalEvent.sourceLabel,
                    generatedDiagramPath = diagramPath,
                    diagramError = diagramErr,
                    latencyMs = finalEvent.latencyMs
                )

                localState.update { state ->
                    state.copy(
                        examChat = state.examChat.copy(
                            messages = state.examChat.messages + assistantMsg,
                            isGenerating = false,
                            streamingDraft = null
                        )
                    )
                }

                // Auto-record graded score if AI returned "প্রাপ্ত নম্বর: X/Y"
                maybeSaveExtractedExamChatScore(
                    subject = finalEvent.resolvedSubject ?: resolvedSubject,
                    intent = analysis.intent,
                    responseText = finalEvent.fullText
                )
            } catch (e: TimeoutCancellationException) {
                Log.e("EduAiViewModel", "Exam Chat timed out", e)
                localState.update { state ->
                    state.copy(
                        examChat = state.examChat.copy(isGenerating = false, streamingDraft = null),
                        errorMessage = "অনলাইন উত্তর ৬০ সেকেন্ডের মধ্যে আসেনি। আবার চেষ্টা করো।"
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("EduAiViewModel", "Exam Chat failed", e)
                localState.update { state ->
                    state.copy(
                        examChat = state.examChat.copy(isGenerating = false, streamingDraft = null),
                        errorMessage = e.localizedMessage ?: "পরীক্ষা সহায়ক উত্তর তৈরি করতে সমস্যা হয়েছে।"
                    )
                }
            }
        }
    }

    private fun startExamHallCountdown(session: ActiveExamSession) {
        examHallTimerJob?.cancel()
        localState.update { it.copy(examChat = it.examChat.copy(activeSession = session)) }
        examHallTimerJob = viewModelScope.launch {
            while (true) {
                delay(1_000L)
                val active = localState.value.examChat.activeSession ?: break
                if (active.mode != ExamSessionMode.MADHYAMIK_TIMED) break
                if (active.isPaused || active.isTimeUp) continue

                val nextSec = (active.remainingSeconds - 1L).coerceAtLeast(0L)
                localState.update { state ->
                    state.copy(
                        examChat = state.examChat.copy(
                            activeSession = active.copy(
                                remainingSeconds = nextSec,
                                isTimeUp = nextSec == 0L
                            )
                        )
                    )
                }
                if (nextSec == 0L) break
            }
        }
    }

    private suspend fun maybeSaveExtractedExamChatScore(
        subject: WbbseSubject,
        intent: ExamChatIntent,
        responseText: String
    ) {
        if (intent != ExamChatIntent.HANDWRITTEN_GRADING &&
            intent != ExamChatIntent.MADHYAMIK_TEST &&
            intent != ExamChatIntent.NORMAL_TEST
        ) return

        val normalized = ExamIntentRouter.convertBengaliDigits(responseText)
        val match = Regex("""(?:প্রাপ্ত\s*নম্বর|marks?\s*awarded|score)\s*[:=-]?\s*(\d+(?:\.\d+)?)\s*/\s*(\d+(?:\.\d+)?)""", RegexOption.IGNORE_CASE)
            .find(normalized) ?: return

        val obtained = match.groupValues[1].toFloatOrNull() ?: return
        val max = match.groupValues[2].toFloatOrNull()?.takeIf { it > 0f } ?: return
        val clampedObtained = obtained.coerceIn(0f, max)
        val accuracy = ((clampedObtained / max) * 100f).roundToInt().coerceIn(0, 100)

        studyRepository.saveExamAttempt(
            ExamAttemptEntity(
                subjectId = subject.id,
                examType = intent.name,
                questionSummary = "${subject.bengaliName} • ${intent.badgeBn}",
                scoreObtained = clampedObtained,
                maxMarks = max,
                accuracyPercent = accuracy,
                feedbackSummary = "AI মূল্যায়নকৃত স্কোর: $clampedObtained/$max ($accuracy%)"
            )
        )
    }

    // ==================== Flagship TOPPER Mode Delegates ====================

    fun selectTopperTrack(track: TopperSubjectTrack) = topperController.selectTrack(track)
    fun toggleTopperTwistedChallenge(enabled: Boolean) = topperController.toggleTwistedChallenge(enabled)
    fun toggleTopperFullBlueprint(enabled: Boolean) = topperController.toggleFullBlueprint(enabled)
    fun dismissTopperError() = topperController.dismissError()

    fun startDailyTopperSet(track: TopperSubjectTrack) {
        val snap = uiState.value
        topperController.startDailyTopperSet(
            track = track,
            inferenceMode = snap.inferenceMode,
            llamaConfig = snap.llamaConfig
        )
    }

    fun start15MinOfflineCapsule(track: TopperSubjectTrack) =
        topperController.start15MinOfflineCapsule(track)

    fun startMistakeRecoveryTest(trackFilter: TopperSubjectTrack? = null) =
        topperController.startMistakeRecoveryTest(trackFilter)

    fun setTopperTimingBucket(bucket: TimingBucket) = topperController.setActiveTimingBucket(bucket)
    fun toggleTopperTimerPause() = topperController.toggleTimerPause()
    fun exitTopperActivePaper() = topperController.exitActivePaper()
    fun selectTopperMcqAnswer(questionId: String, optionIndex: Int) =
        topperController.selectMcqAnswer(questionId, optionIndex)
    fun updateTopperTypedAnswer(questionId: String, section: TopperSectionType, text: String) =
        topperController.updateTypedAnswer(questionId, section, text)
    fun addTopperQuestionPhotoUri(questionId: String, section: TopperSectionType, uri: Uri) =
        topperController.addQuestionPhotoUri(questionId, section, uri)
    fun removeTopperQuestionPhoto(questionId: String, pageIndex: Int) =
        topperController.removeQuestionPhoto(questionId, pageIndex)
    fun submitTopperPaper() = topperController.submitTopperPaper(uiState.value.inferenceMode)

    // ==================== Flagship Smart Learning ("স্মার্ট লার্নিং") Delegates ====================

    fun openSmartLearning(
        subject: WbbseSubject? = null,
        chapterName: String? = null,
        autoExplainNote: MasterDataEntity? = null
    ) {
        val snap = uiState.value
        smartLearningController.openSmartLearning(
            subject = subject,
            chapterName = chapterName,
            autoExplainNote = autoExplainNote,
            inferenceMode = snap.inferenceMode,
            llamaConfig = snap.llamaConfig
        )
    }

    fun closeSmartLearning() {
        ttsManager.stop()
        smartLearningController.closeSmartLearning()
    }

    fun selectSmartLearningSubject(subject: WbbseSubject?) =
        smartLearningController.selectSubject(subject)

    fun selectSmartLearningChapter(chapterName: String?) =
        smartLearningController.selectChapter(chapterName)

    fun toggleSmartLearningDiagramMode() = smartLearningController.toggleGenerateDiagram()
    fun showSmartLearningOfflinePhotoNotice() = smartLearningController.showOfflinePhotoPrompt()
    fun dismissSmartLearningOfflinePhotoNotice() = smartLearningController.dismissOfflinePhotoNotice()
    fun dismissSmartLearningError() = smartLearningController.dismissError()
    fun attachSmartLearningPhotoUri(uri: Uri) = smartLearningController.attachPhotoUri(uri)
    fun clearSmartLearningPendingPhoto() = smartLearningController.clearPendingPhoto()
    fun clearSmartLearningSession() {
        ttsManager.stop()
        smartLearningController.clearSession()
    }

    fun sendSmartLearningMessage(rawText: String) {
        val snap = uiState.value
        ttsManager.stop()
        smartLearningController.sendMessage(
            rawText = rawText,
            inferenceMode = snap.inferenceMode,
            llamaConfig = snap.llamaConfig
        )
    }

    fun generateSmartLearningDiagramForMessage(message: SmartLearningMessage) =
        smartLearningController.generateDiagramForMessage(message, uiState.value.inferenceMode)

    fun startSmartLearningQuickQuiz(message: SmartLearningMessage) =
        smartLearningController.startTwoMinuteQuiz(message, uiState.value.inferenceMode)

    fun selectSmartLearningQuizOption(questionId: String, optionIndex: Int) =
        smartLearningController.selectQuizOption(questionId, optionIndex)

    fun submitSmartLearningQuickQuiz() = smartLearningController.submitQuickQuiz()
    fun closeSmartLearningQuickQuiz() = smartLearningController.closeQuickQuiz()

    fun startSmartLearningMemorizer(message: SmartLearningMessage) =
        smartLearningController.startSmartMemorizer(message, uiState.value.inferenceMode)

    fun toggleSmartLearningMemorizerRecall() = smartLearningController.toggleMemorizerActiveRecall()
    fun toggleSmartLearningMemorizerStep(step: Int) = smartLearningController.toggleMemorizerCardReveal(step)
    fun closeSmartLearningMemorizer() {
        ttsManager.stop()
        smartLearningController.closeSmartMemorizer()
    }

    // ==================== 4 Topper-Making Upgrades (Daily Plan, Map Board, Rapid Fire, Audio Revision) ====================

    fun setDailyPlannerSheetOpen(open: Boolean) = topperExtrasController.setPlannerSheetOpen(open)

    fun generateDailyPlan(hours: Int) =
        topperExtrasController.generateNewDailyPlan(hours, uiState.value.masterNotes)

    fun toggleDailyPlanTask(task: DailyPlanTaskEntity) =
        topperExtrasController.toggleDailyTaskCompleted(task)

    fun launchDailyPlanTask(task: DailyPlanTaskEntity) {
        val subj = WbbseSubject.fromId(task.subjectId)
        when (task.actionType) {
            PlanActionType.SMART_LEARNING -> {
                openSmartLearning(subject = subj)
            }

            PlanActionType.MAP_BOARD -> {
                val domain = if (subj == WbbseSubject.HISTORY) {
                    MapBoardDomain.HISTORY
                } else {
                    MapBoardDomain.GEOGRAPHY
                }
                openMapBoard(domain)
            }

            PlanActionType.RAPID_FIRE -> {
                openRapidFire(subj)
            }

            PlanActionType.TOPPER_CAPSULE -> {
                val track = TopperSubjectTrack.entries.firstOrNull { it.linkedSubject == subj }
                    ?: TopperSubjectTrack.PHYSICAL_SCIENCE
                selectTab(AppTab.TOPPER)
                start15MinOfflineCapsule(track)
            }

            else -> {
                selectTab(AppTab.SUBJECTS)
                if (subj != null) openSubjectDetail(subj)
            }
        }
    }

    fun openMapBoard(domain: MapBoardDomain) {
        ttsManager.stop()
        topperExtrasController.openMapBoard(domain)
    }

    fun switchMapBoardDomain(domain: MapBoardDomain) =
        topperExtrasController.switchMapBoardDomain(domain)

    fun closeMapBoard() = topperExtrasController.closeMapBoard()

    fun openRapidFire(subject: WbbseSubject? = null) {
        ttsManager.stop()
        topperExtrasController.openRapidFire(subject)
    }

    fun closeRapidFire() = topperExtrasController.closeRapidFire()

    fun selectRapidFireSubject(subject: WbbseSubject?) =
        topperExtrasController.updateRapidFireSetup(subject = subject)

    fun selectRapidFireQuestionCount(count: Int) =
        topperExtrasController.updateRapidFireSetup(questionCount = count)

    fun selectRapidFireTimerMode(mode: RapidFireTimerMode) =
        topperExtrasController.updateRapidFireSetup(timerMode = mode)

    fun startRapidFireDrill() =
        topperExtrasController.startRapidFire(uiState.value.masterNotes)

    fun answerRapidFireOption(optionIndex: Int) =
        topperExtrasController.answerRapidFireOption(optionIndex)

    fun advanceRapidFireNext() = topperExtrasController.advanceRapidFireNext()

    fun finishRapidFireEarly() = topperExtrasController.finishRapidFireEarly()

    fun openMistakeRecoveryFromRapidFire() {
        topperExtrasController.closeRapidFire()
        selectTab(AppTab.TOPPER)
        startMistakeRecoveryTest(null)
    }

    fun openAudioRevisionForChapter(subject: WbbseSubject, chapterName: String) {
        val chapterNotes = uiState.value.masterNotes.filter {
            it.subjectId == subject.id && it.chapterName == chapterName
        }
        topperExtrasController.openAudioRevisionForChapter(
            subject = subject,
            chapterName = chapterName,
            chapterNotes = chapterNotes
        )
    }

    fun openAudioRevisionForSubject(subject: WbbseSubject) {
        val subjectNotes = uiState.value.masterNotes.filter { it.subjectId == subject.id }
        topperExtrasController.openAudioRevisionForSubject(
            subject = subject,
            subjectNotes = subjectNotes
        )
    }

    fun openAudioRevisionForSmartLearning() {
        val sl = smartLearningState.value
        topperExtrasController.openAudioRevisionForSmartLearning(
            subject = sl.selectedSubject,
            chapterName = sl.selectedChapter,
            messages = sl.messages,
            masterNotes = uiState.value.masterNotes
        )
    }

    fun toggleAudioRevisionPlayPause() = topperExtrasController.toggleAudioPlayPause()
    fun nextAudioRevisionTrack() = topperExtrasController.nextAudioTrack()
    fun previousAudioRevisionTrack() = topperExtrasController.previousAudioTrack()
    fun selectAudioRevisionTrack(index: Int) = topperExtrasController.selectAudioTrack(index)
    fun setAudioRevisionSpeed(speed: Float) = topperExtrasController.setAudioPlaybackSpeed(speed)
    fun toggleAudioRevisionAutoAdvance() = topperExtrasController.toggleAudioAutoAdvance()
    fun closeAudioRevision() = topperExtrasController.closeAudioRevision()

    // ==================== Offline Exam Bank & Score Tracker ====================

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

    // ==================== 19-Feature Master Upgrade Delegates ====================

    // A. Universal Offline PDF Generator & Saved Library
    fun requestPdfExport(suggestedName: String, title: String, subtitle: String, bodyContent: String) =
        masterUpgradeController.requestPdfExport(suggestedName, title, subtitle, bodyContent)
    fun dismissPdfExportDialog() = masterUpgradeController.dismissPdfExportDialog()
    fun confirmGeneratePdf(customFileName: String) = masterUpgradeController.confirmGeneratePdf(customFileName)
    fun openSavedPdfLibrary() = masterUpgradeController.openSavedPdfLibrary()
    fun closeSavedPdfLibrary() = masterUpgradeController.closeSavedPdfLibrary()
    fun refreshSavedPdfs() = masterUpgradeController.refreshSavedPdfs()
    fun renameSavedPdf(item: SavedPdfItem, newName: String) = masterUpgradeController.renameSavedPdf(item, newName)
    fun deleteSavedPdf(item: SavedPdfItem) = masterUpgradeController.deleteSavedPdf(item)
    fun clearPdfStatusMessage() = masterUpgradeController.clearPdfStatusMessage()

    // B. Offline Backup & Restore
    fun exportProgressBackup(uri: Uri) = masterUpgradeController.exportProgressBackup(uri)
    fun importProgressBackup(uri: Uri) = masterUpgradeController.importProgressBackup(uri)
    fun clearBackupStatus() = masterUpgradeController.clearBackupStatus()

    // 4. Brahmastram
    fun openBrahmastram(subject: WbbseSubject = WbbseSubject.PHYSICAL_SCIENCE) =
        masterUpgradeController.openBrahmastram(subject)
    fun closeBrahmastram() = masterUpgradeController.closeBrahmastram()
    fun selectBrahmastramSubject(subject: WbbseSubject) = masterUpgradeController.selectBrahmastramSubject(subject)
    fun regenerateBrahmastramNotes() = masterUpgradeController.generateBrahmastramNotes()
    fun toggleBrahmastramNoteCompleted(noteId: String) = masterUpgradeController.toggleBrahmastramNoteCompleted(noteId)
    fun startBrahmastramExam() = masterUpgradeController.startBrahmastramExam()
    fun selectBrahmastramMcqOption(questionId: String, optionIdx: Int) =
        masterUpgradeController.selectBrahmastramMcqOption(questionId, optionIdx)
    fun updateBrahmastramTypedAnswer(questionId: String, text: String) =
        masterUpgradeController.updateBrahmastramTypedAnswer(questionId, text)
    fun submitBrahmastramExam() = masterUpgradeController.submitBrahmastramExam()
    fun returnToBrahmastramNotes() = masterUpgradeController.returnToBrahmastramNotes()

    // 5. Pocket Book
    fun openPocketBook(category: PocketBookCategory = PocketBookCategory.MATH_FORMULAS) =
        masterUpgradeController.openPocketBook(category)
    fun selectPocketBookCategory(category: PocketBookCategory) = masterUpgradeController.selectPocketBookCategory(category)
    fun updatePocketBookSearch(q: String) = masterUpgradeController.updatePocketBookSearch(q)
    fun closePocketBook() = masterUpgradeController.closePocketBook()

    // 6. AI Viva Oral Tutor
    fun openAiViva(subject: WbbseSubject, chapterName: String = "") =
        masterUpgradeController.openAiViva(subject, chapterName)
    fun speakCurrentVivaQuestion() = masterUpgradeController.speakCurrentVivaQuestion()
    fun submitVivaAnswer(answer: String) = masterUpgradeController.submitVivaAnswer(answer)
    fun closeAiViva() = masterUpgradeController.closeAiViva()

    // 8. Confusion Cleaner
    fun openConfusionCleaner(subject: WbbseSubject) = masterUpgradeController.openConfusionCleaner(subject)
    fun closeConfusionCleaner() = masterUpgradeController.closeConfusionCleaner()

    // 9. Spaced Repetition Capsule
    fun markSpacedRevisionCompleted(item: SpacedRevisionEntity) =
        masterUpgradeController.markSpacedRevisionCompleted(item)

    // 15. Interactive Science Diagram Labelling
    fun openDiagramLab(subject: WbbseSubject) = masterUpgradeController.openDiagramLab(subject)
    fun switchDiagramLabSubject(subject: WbbseSubject) = masterUpgradeController.switchDiagramLabSubject(subject)
    fun selectDiagramIndex(index: Int) = masterUpgradeController.selectDiagramIndex(index)
    fun closeDiagramLab() = masterUpgradeController.closeDiagramLab()

    // 17. Writing Studio
    fun openWritingStudio(language: WritingLanguage) = masterUpgradeController.openWritingStudio(language)
    fun selectWritingFormat(format: WritingFormatType) = masterUpgradeController.selectWritingFormat(format)
    fun updateWritingTopic(topic: String) = masterUpgradeController.updateWritingTopic(topic)
    fun generateWritingOutput() = masterUpgradeController.generateWritingOutput()
    fun closeWritingStudio() = masterUpgradeController.closeWritingStudio()

    // 18. English & Bengali Language Mastery Labs
    fun openLanguageLab(mode: LanguageLabMode) = masterUpgradeController.openLanguageLab(mode)
    fun updateBengaliAnalyzerInput(text: String) = masterUpgradeController.updateBengaliAnalyzerInput(text)
    fun runBengaliGrammarAnalyzer() = masterUpgradeController.runBengaliGrammarAnalyzer()
    fun updateCustomUnseenInput(text: String) = masterUpgradeController.updateCustomUnseenInput(text)
    fun solveCustomUnseenPassage() = masterUpgradeController.solveCustomUnseenPassage()
    fun closeLanguageLab() = masterUpgradeController.closeLanguageLab()

    // 19. Offline Math & Mensuration Solver
    fun openMathSolver(module: MathSolverModule = MathSolverModule.SPHERE) =
        masterUpgradeController.openMathSolver(module)
    fun selectMathSolverModule(module: MathSolverModule) = masterUpgradeController.selectMathSolverModule(module)
    fun closeMathSolver() = masterUpgradeController.closeMathSolver()

    // ==================== Floating "D" Action Hub Tools & Shortcuts ====================

    fun setVoiceInputLanguage(language: VoiceInputLanguage) {
        speechRecognizer.setLanguage(language)
    }

    fun toggleVoiceListening() {
        if (speechRecognizer.state.value.isListening) {
            speechRecognizer.stopListening()
        } else {
            ttsManager.pausePodcast()
            speechRecognizer.startListening()
        }
    }

    fun consumeVoiceInjectedText() {
        _voiceInjectedText.value = null
    }

    /**
     * Resolves the latest AI response across Fast Learning, Brahmastram, Mind Map, Smart Learning,
     * Exam Chat, Main Chat, or verified WBBSE notes.
     */
    private fun resolveLatestContentBundle(): Triple<String, String, String> {
        val flMsg = fastLearningChatState.value.messages.lastOrNull { it.role != "user" && it.content.isNotBlank() }
        if (fastLearningState.value.isOpen && flMsg != null) {
            val subjLabel = WbbseSubject.fromId(flMsg.subjectId)?.bengaliName ?: "ফাস্ট লার্নিং"
            return Triple(flMsg.topicLabel.ifBlank { "$subjLabel মাইক্রো-সামারি" }, subjLabel, flMsg.content)
        }

        val brMsg = brahmastramChatState.value.messages.lastOrNull { it.role != "user" && it.content.isNotBlank() }
        if (brahmastramState.value.isOpen && brMsg != null) {
            val subjLabel = WbbseSubject.fromId(brMsg.subjectId)?.bengaliName ?: "ব্রহ্মাস্ত্রম"
            return Triple(brMsg.topicLabel.ifBlank { "$subjLabel ব্রহ্মাস্ত্রম উত্তর" }, subjLabel, brMsg.content)
        }

        val slMsg = smartLearningState.value.messages.lastOrNull { it.role != "user" && it.content.isNotBlank() }
        if (smartLearningState.value.isOpen && slMsg != null) {
            val subjLabel = slMsg.subject?.bengaliName ?: "স্মার্ট লার্নিং"
            val title = smartLearningState.value.selectedChapter ?: "$subjLabel AI ব্যাখ্যা"
            return Triple(title, subjLabel, slMsg.content)
        }

        val chatMsg = uiState.value.chatMessages.lastOrNull { it.role != "user" && it.content.isNotBlank() }
        if (chatMsg != null) {
            val subj = WbbseSubject.fromId(chatMsg.subjectId)
            val subjLabel = subj?.bengaliName ?: "WBBSE Class 10"
            val title = chatMsg.content.lines().firstOrNull { it.isNotBlank() }
                ?.replace(Regex("[#*📘🔹🎯📖]"), "")
                ?.trim()
                ?.take(48)
                ?: "$subjLabel নোটস"
            return Triple(title, subjLabel, chatMsg.content)
        }

        val examMsg = uiState.value.examChat.messages.lastOrNull { it.role != "user" && it.content.isNotBlank() }
        if (examMsg != null) {
            val subjLabel = examMsg.subject?.bengaliName ?: "WBBSE পরীক্ষা"
            return Triple("$subjLabel AI উত্তর", subjLabel, examMsg.content)
        }

        val fallbackSubject = uiState.value.selectedChatSubject
            ?: uiState.value.selectedDetailSubject
            ?: WbbseSubject.PHYSICAL_SCIENCE
        val sampleNote = uiState.value.masterNotes.firstOrNull { it.subjectId == fallbackSubject.id && it.importanceLevel == "VVI" }
            ?: uiState.value.masterNotes.firstOrNull()
        return if (sampleNote != null) {
            val body = "${sampleNote.chapterName} — ${sampleNote.topicTitle}\n${sampleNote.content}\n📌 মূল সূত্র ও তথ্য: ${sampleNote.keyFormulaOrFact}\n🎯 পরীক্ষার টিপস: ${sampleNote.examTip}"
            Triple(sampleNote.topicTitle, fallbackSubject.bengaliName, body)
        } else {
            Triple("WBBSE Class 10 স্মার্ট স্টাডি", "WBBSE Class 10", "")
        }
    }

    fun openMindMapForLatestAnswer() {
        val (title, subjLabel, body) = resolveLatestContentBundle()
        _mindMapTree.value = MindMapParser.parse(
            rawAnswer = body,
            fallbackTitle = title,
            subjectLabel = subjLabel
        )
    }

    fun openMindMapFromText(title: String, subjectLabel: String, body: String) {
        _mindMapTree.value = MindMapParser.parse(
            rawAnswer = body,
            fallbackTitle = title,
            subjectLabel = subjectLabel
        )
    }

    fun closeMindMap() {
        _mindMapTree.value = null
    }

    fun exportMindMapToPdf() {
        val tree = _mindMapTree.value ?: return
        val formattedBody = buildString {
            append("ভিজ্যুয়াল মাইন্ড-ম্যাপ: ${tree.rootTitle}\n")
            append("বিষয়: ${tree.subjectLabel}\n\n")
            tree.branches.forEachIndexed { idx, branch ->
                append("${idx + 1}. ${branch.title}\n")
                branch.points.forEach { pt -> append("   • $pt\n") }
                append("\n")
            }
        }
        _mindMapTree.value = null
        requestPdfExport(
            suggestedName = "মাইন্ডম্যাপ_${tree.rootTitle.take(24)}",
            title = tree.rootTitle,
            subtitle = "${tree.subjectLabel} • ভিজ্যুয়াল মাইন্ড-ম্যাপ",
            bodyContent = formattedBody
        )
    }

    fun toggleQuickPodcastPlayPause() {
        val pState = ttsManager.podcastState.value
        when {
            pState.isPlaying -> ttsManager.pausePodcast()
            pState.isPaused -> {
                if (!ttsManager.resumePodcast()) {
                    startQuickPodcastFromLatest()
                }
            }
            else -> startQuickPodcastFromLatest()
        }
    }

    private fun startQuickPodcastFromLatest() {
        val (title, subjLabel, body) = resolveLatestContentBundle()
        if (body.isBlank()) {
            localState.update {
                it.copy(errorMessage = "পডকাস্ট শোনার জন্য কোনো নোট বা AI উত্তর পাওয়া যায়নি।")
            }
            return
        }
        val started = ttsManager.startPodcast(
            title = title,
            subtitle = "$subjLabel • অফলাইন পডকাস্ট",
            rawText = body,
            settings = uiState.value.ttsVoiceSettings,
            speed = ttsManager.podcastState.value.speed
        )
        if (!started) {
            localState.update {
                it.copy(errorMessage = "ভয়েস ইঞ্জিন (TTS) প্রস্তুত হচ্ছে, একটু পরে আবার চেষ্টা করো।")
            }
        }
    }

    fun stopQuickPodcast() {
        ttsManager.stopPodcast()
    }

    fun setQuickPodcastSpeed(speed: Float) {
        ttsManager.setPodcastSpeed(speed, uiState.value.ttsVoiceSettings)
    }

    fun openFullPodcastFromDHub() {
        val subj = uiState.value.selectedChatSubject
            ?: uiState.value.selectedDetailSubject
            ?: WbbseSubject.PHYSICAL_SCIENCE
        closeAllOverlaysForShortcut()
        openAudioRevisionForSubject(subj)
    }

    fun exportLatestToPdfFromDHub() {
        val (title, subjLabel, body) = resolveLatestContentBundle()
        if (body.isBlank()) {
            openSavedPdfLibrary()
            return
        }
        requestPdfExport(
            suggestedName = "${subjLabel}_${title.take(22)}",
            title = title,
            subtitle = "$subjLabel • EduAI স্মার্ট নোটস",
            bodyContent = body
        )
    }

    fun closeAllOverlaysForShortcut() {
        closeFastLearning()
        smartLearningController.closeSmartLearning()
        topperExtrasController.closeMapBoard()
        topperExtrasController.closeRapidFire()
        topperExtrasController.closeAudioRevision()
        masterUpgradeController.closeSavedPdfLibrary()
        masterUpgradeController.closeBrahmastram()
        masterUpgradeController.closePocketBook()
        masterUpgradeController.closeAiViva()
        masterUpgradeController.closeConfusionCleaner()
        masterUpgradeController.closeDiagramLab()
        masterUpgradeController.closeWritingStudio()
        masterUpgradeController.closeLanguageLab()
        masterUpgradeController.closeMathSolver()
    }

    // ==================== Fast Learning Hub & Section Chats ====================

    fun openFastLearning() {
        _fastLearningLocalNav.update { it.copy(isOpen = true) }
    }

    fun closeFastLearning() {
        _fastLearningLocalNav.update { it.copy(isOpen = false) }
    }

    fun selectFastLearningTab(tabIndex: Int) {
        _fastLearningLocalNav.update { it.copy(activeTabIndex = tabIndex.coerceIn(0, 1)) }
    }

    fun selectFastLearningSubject(subject: WbbseSubject?) {
        _fastLearningLocalNav.update { it.copy(selectedSubject = subject) }
    }

    fun setFastLearningExamYear(year: Int) {
        viewModelScope.launch {
            settingsDataStore.setFastLearningExamYear(year)
        }
    }

    fun setFastLearningPlan(plan: SyllabusPlanOption) {
        viewModelScope.launch {
            settingsDataStore.setFastLearningPlanMonths(plan.months)
        }
    }

    fun toggleFastLearningChapterCompleted(chapterKey: String) {
        viewModelScope.launch {
            settingsDataStore.toggleFastLearningChapter(chapterKey)
        }
    }

    fun quickReviseChapterInFastLearning(item: FastLearningChapterItem) {
        val year = fastLearningState.value.targetExamYear
        _fastLearningLocalNav.update {
            it.copy(
                activeTabIndex = 1,
                selectedSubject = item.subject
            )
        }
        val prompt = "${item.subject.bengaliName} বিষয়ের '${item.chapterName}' অধ্যায়ের মাধ্যমিক $year পরীক্ষার জন্য সুপার-ফাস্ট মাইক্রো-কনসেপ্ট সামারি, মূল সূত্র ও সম্ভাব্য বোর্ড প্রশ্ন দাও।"
        sectionChatController.sendMessage(
            section = ChatSection.FAST_LEARNING,
            rawText = prompt,
            subject = item.subject,
            sectionContextHint = "Fast Learning VVI Chapter: ${item.chapterName} (${item.subject.bengaliName}, Est. ${item.estimatedBoardMarks} marks)",
            fastLearningMicroMode = true
        )
    }

    fun sendSectionChatMessage(
        section: ChatSection,
        rawText: String,
        subject: WbbseSubject?,
        sectionContextHint: String = ""
    ) {
        sectionChatController.sendMessage(
            section = section,
            rawText = rawText,
            subject = subject,
            sectionContextHint = sectionContextHint,
            fastLearningMicroMode = (section == ChatSection.FAST_LEARNING)
        )
    }

    fun attachSectionChatPhoto(section: ChatSection, uri: Uri) {
        sectionChatController.attachPhotoUri(section, uri)
    }

    fun clearSectionChatPendingPhoto(section: ChatSection) {
        sectionChatController.clearPendingPhoto(section)
    }

    fun clearSectionChat(section: ChatSection) {
        ttsManager.stop()
        sectionChatController.clearSectionChat(section)
    }

    fun dismissSectionChatError(section: ChatSection) {
        sectionChatController.clearError(section)
    }

    fun shortcutOpenFastLearning() {
        closeAllOverlaysForShortcut()
        openFastLearning()
    }

    fun shortcutOpenSmartLearning() {
        closeAllOverlaysForShortcut()
        openSmartLearning(subject = uiState.value.selectedChatSubject)
    }

    fun shortcutOpenBrahmastram() {
        closeAllOverlaysForShortcut()
        openBrahmastram(uiState.value.selectedChatSubject ?: WbbseSubject.PHYSICAL_SCIENCE)
    }

    fun shortcutOpenGrammarLab() {
        closeAllOverlaysForShortcut()
        val mode = if (uiState.value.selectedChatSubject == WbbseSubject.ENGLISH) {
            LanguageLabMode.ENGLISH_LAB
        } else {
            LanguageLabMode.BENGALI_GRAMMAR_LAB
        }
        openLanguageLab(mode)
    }

    fun shortcutOpenSettings() {
        closeAllOverlaysForShortcut()
        selectTab(AppTab.MODEL_HUB)
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
