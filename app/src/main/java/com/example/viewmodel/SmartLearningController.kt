package com.example.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.data.ai.AiRepository
import com.example.data.ai.AiStreamEvent
import com.example.data.ai.SmartLearningService
import com.example.data.local.ChatMessageEntity
import com.example.data.local.ExamAttemptEntity
import com.example.data.local.MasterDataEntity
import com.example.data.local.StudyRepository
import com.example.data.media.NotebookImageHelper
import com.example.model.ChatSection
import com.example.model.FastLearningContextHelper
import com.example.model.InferenceMode
import com.example.model.LlamaInferenceConfig
import com.example.model.SmartLearningMessage
import com.example.model.SmartLearningUiState
import com.example.model.SyllabusPlanOption
import com.example.model.WbbseSubject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private const val TAG = "SmartLearningController"

/**
 * Manages the flagship "স্মার্ট লার্নিং (Smart Learning)" AI Tutor session:
 * - Entry from Main Dashboard, any Subject, or any individual Chapter/Topic note
 * - Interactive Bengali Tutor chat (Online Gemini 3.8 Flash + Offline .gguf & verified notes)
 * - Camera/Gallery textbook photo upload with friendly Offline notice
 * - Auto & on-demand AI Visual Diagram generation (Gemini Nano Banana 2.1)
 * - 2-Minute Quick Quiz (120s live countdown + Room score saving)
 * - Smart Memorizer (Step-by-step Broad Question Active Recall flashcards)
 */
class SmartLearningController(
    private val appContext: Context,
    private val scope: CoroutineScope,
    private val studyRepository: StudyRepository,
    private val aiRepository: AiRepository,
    private val smartLearningService: SmartLearningService,
    private val getTargetExamYear: () -> Int = { FastLearningContextHelper.defaultTargetExamYear() },
    private val getSyllabusPlanLabel: () -> String = { SyllabusPlanOption.MONTHS_3.labelEn }
) {
    private val _uiState = MutableStateFlow(SmartLearningUiState())
    val uiState: StateFlow<SmartLearningUiState> = _uiState.asStateFlow()

    private var tutorJob: Job? = null
    private var quizTimerJob: Job? = null
    private var nextMessageId: Long = 50_000L

    init {
        scope.launch {
            val saved = studyRepository.getSectionMessagesSnapshot(ChatSection.SMART_LEARNING)
            if (saved.isNotEmpty()) {
                val restored = saved.map { entity ->
                    SmartLearningMessage(
                        id = entity.id,
                        role = entity.role,
                        content = entity.content,
                        topicLabel = entity.topicLabel.ifBlank { entity.content.take(48) },
                        subject = WbbseSubject.fromId(entity.subjectId),
                        chapterName = entity.topicLabel.takeIf { it.isNotBlank() },
                        mode = InferenceMode.fromStorage(entity.mode),
                        sourceLabel = entity.sourceLabel,
                        userPhotoPath = entity.imagePath,
                        generatedDiagramPath = entity.diagramPath,
                        timestamp = entity.timestamp
                    )
                }
                val maxId = restored.maxOfOrNull { it.id } ?: 50_000L
                if (maxId >= nextMessageId) nextMessageId = maxId + 1
                _uiState.update { it.copy(messages = restored) }
            }
        }
    }

    fun openSmartLearning(
        subject: WbbseSubject? = null,
        chapterName: String? = null,
        autoExplainNote: MasterDataEntity? = null,
        inferenceMode: InferenceMode = InferenceMode.OFFLINE,
        llamaConfig: LlamaInferenceConfig = LlamaInferenceConfig()
    ) {
        val resolvedSubject = subject ?: autoExplainNote?.let { WbbseSubject.fromId(it.subjectId) }
        val resolvedChapter = chapterName ?: autoExplainNote?.chapterName

        _uiState.update {
            it.copy(
                isOpen = true,
                selectedSubject = resolvedSubject ?: it.selectedSubject,
                selectedChapter = resolvedChapter,
                offlinePhotoNotice = null,
                errorMessage = null
            )
        }

        if (autoExplainNote != null) {
            val prompt = "“${autoExplainNote.topicTitle}” (${autoExplainNote.chapterName}) — এই টপিকটি একদম সহজ বাংলায় বাস্তব উদাহরণ দিয়ে ধাপে ধাপে বুঝিয়ে দাও।"
            sendMessage(
                rawText = prompt,
                inferenceMode = inferenceMode,
                llamaConfig = llamaConfig
            )
        }
    }

    fun closeSmartLearning() {
        quizTimerJob?.cancel()
        _uiState.update {
            it.copy(
                isOpen = false,
                activeQuiz = null,
                activeMemorizer = null,
                offlinePhotoNotice = null
            )
        }
    }

    fun selectSubject(subject: WbbseSubject?) {
        _uiState.update {
            it.copy(
                selectedSubject = subject,
                selectedChapter = null,
                offlinePhotoNotice = null
            )
        }
    }

    fun selectChapter(chapterName: String?) {
        _uiState.update {
            it.copy(
                selectedChapter = chapterName,
                offlinePhotoNotice = null
            )
        }
    }

    fun toggleGenerateDiagram() {
        _uiState.update { it.copy(generateDiagramToggle = !it.generateDiagramToggle) }
    }

    fun dismissOfflinePhotoNotice() {
        _uiState.update { it.copy(offlinePhotoNotice = null) }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun showOfflinePhotoPrompt() {
        _uiState.update {
            it.copy(
                offlinePhotoNotice = "📷 বইয়ের পাতা বা অঙ্কের ছবি দেখে বোঝার জন্য অনুগ্রহ করে ইন্টারনেট চালু করে উপরের সুইচ দিয়ে অনলাইন মোড (Online Mode) অন করো। তবে অফলাইন মোডেও তুমি নিচে লিখে যেকোনো প্রশ্ন করতে পারো!"
            )
        }
    }

    fun attachPhotoUri(uri: Uri) {
        scope.launch {
            val bitmap = NotebookImageHelper.loadScaledBitmap(appContext, uri)
            if (bitmap != null) {
                _uiState.update {
                    it.copy(
                        pendingPhoto = PendingPhotoAttachment(uri = uri, previewBitmap = bitmap),
                        offlinePhotoNotice = null
                    )
                }
            } else {
                _uiState.update {
                    it.copy(errorMessage = "ছবিটি লোড করা যায়নি। পরিষ্কার একটি ছবি বেছে নাও।")
                }
            }
        }
    }

    fun clearPendingPhoto() {
        _uiState.update { it.copy(pendingPhoto = null) }
    }

    fun clearSession() {
        tutorJob?.cancel()
        quizTimerJob?.cancel()
        scope.launch {
            studyRepository.clearSectionChat(ChatSection.SMART_LEARNING)
        }
        _uiState.update {
            it.copy(
                messages = emptyList(),
                isGenerating = false,
                streamingDraft = null,
                pendingPhoto = null,
                offlinePhotoNotice = null,
                activeQuiz = null,
                activeMemorizer = null
            )
        }
    }

    fun sendMessage(
        rawText: String,
        inferenceMode: InferenceMode,
        llamaConfig: LlamaInferenceConfig
    ) {
        val current = _uiState.value
        val pendingPhoto = current.pendingPhoto
        val trimmed = rawText.trim()
        if ((trimmed.isEmpty() && pendingPhoto == null) || current.isGenerating) return

        // Friendly offline guard if user somehow had a photo attached and switched offline
        if (pendingPhoto != null && inferenceMode == InferenceMode.OFFLINE && trimmed.isEmpty()) {
            showOfflinePhotoPrompt()
            return
        }

        val effectiveQuery = trimmed.ifBlank {
            "এই ছবিতে থাকা পাঠ্যবইয়ের বিষয়টি বা অঙ্কটি একদম সহজ বাংলায় বাস্তব উদাহরণসহ ধাপে ধাপে বুঝিয়ে দাও।"
        }
        val subject = current.selectedSubject
        val chapter = current.selectedChapter
        val autoDiagram = inferenceMode == InferenceMode.ONLINE &&
            smartLearningService.shouldAutoGenerateDiagram(effectiveQuery, subject, chapter)
        val shouldGenerateDiagram = inferenceMode == InferenceMode.ONLINE &&
            (current.generateDiagramToggle || autoDiagram)

        tutorJob?.cancel()
        tutorJob = scope.launch {
            val savedPhotoPath = if (pendingPhoto != null && inferenceMode == InferenceMode.ONLINE) {
                NotebookImageHelper.persistThumbnail(appContext, pendingPhoto.previewBitmap)
            } else null

            val userTopicLabel = chapter ?: effectiveQuery.take(48)
            val persistedUserId = studyRepository.saveChatMessage(
                ChatMessageEntity(
                    role = "user",
                    content = effectiveQuery,
                    subjectId = subject?.id,
                    mode = inferenceMode.storageValue,
                    sourceLabel = subject?.bengaliName ?: "স্মার্ট লার্নিং",
                    imagePath = savedPhotoPath,
                    sectionId = ChatSection.SMART_LEARNING.id,
                    topicLabel = userTopicLabel
                )
            )
            if (persistedUserId >= nextMessageId) nextMessageId = persistedUserId + 1

            val userMsg = SmartLearningMessage(
                id = persistedUserId,
                role = "user",
                content = effectiveQuery,
                topicLabel = userTopicLabel,
                subject = subject,
                chapterName = chapter,
                mode = inferenceMode,
                sourceLabel = subject?.bengaliName ?: "স্মার্ট লার্নিং",
                userPhotoPath = savedPhotoPath
            )

            _uiState.update { state ->
                state.copy(
                    messages = state.messages + userMsg,
                    pendingPhoto = null,
                    offlinePhotoNotice = if (pendingPhoto != null && inferenceMode == InferenceMode.OFFLINE) {
                        "📷 ছবি বিশ্লেষণের জন্য অনলাইন মোড চালু করো; এখন অফলাইন নোট ও মডেল থেকে টেক্সট উত্তর দেওয়া হচ্ছে।"
                    } else null,
                    isGenerating = true,
                    errorMessage = null,
                    streamingDraft = StreamingDraft(
                        text = "",
                        sourceLabel = when {
                            inferenceMode == InferenceMode.OFFLINE -> "🎓 অফলাইন স্মার্ট লার্নিং টিউটর…"
                            pendingPhoto != null -> "📷 বইয়ের ছবি পড়ে সহজ বাংলায় ব্যাখ্যা হচ্ছে…"
                            shouldGenerateDiagram -> "🎨 সহজ ব্যাখ্যা ও ২ডি ডায়াগ্রাম তৈরি হচ্ছে…"
                            else -> "🎓 স্মার্ট লার্নিং AI টিউটর লিখছেন…"
                        },
                        mode = inferenceMode,
                        subject = subject,
                        isGeneratingDiagram = shouldGenerateDiagram
                    )
                )
            }

            try {
                var completed: AiStreamEvent.Completed? = null

                smartLearningService.streamTutorExplanation(
                    query = effectiveQuery,
                    selectedSubject = subject,
                    selectedChapter = chapter,
                    mode = inferenceMode,
                    config = llamaConfig,
                    textbookPhoto = if (inferenceMode == InferenceMode.ONLINE) pendingPhoto?.previewBitmap else null,
                    diagramRequested = current.generateDiagramToggle || autoDiagram,
                    targetExamYear = getTargetExamYear(),
                    syllabusPlanLabel = getSyllabusPlanLabel()
                ).collect { event ->
                    when (event) {
                        is AiStreamEvent.Partial -> {
                            _uiState.update { s ->
                                s.copy(
                                    streamingDraft = StreamingDraft(
                                        text = event.accumulatedText,
                                        sourceLabel = event.sourceLabel,
                                        mode = inferenceMode,
                                        subject = subject,
                                        isGeneratingDiagram = shouldGenerateDiagram
                                    )
                                )
                            }
                        }

                        is AiStreamEvent.Completed -> completed = event
                    }
                }

                val finalEvent = completed ?: throw IllegalStateException("টিউটরের উত্তর পাওয়া যায়নি।")
                val resolvedSubject = finalEvent.resolvedSubject ?: subject
                var diagramPath: String? = null
                var diagramErr: String? = null

                if (shouldGenerateDiagram) {
                    _uiState.update { s ->
                        s.copy(
                            streamingDraft = StreamingDraft(
                                text = finalEvent.fullText,
                                sourceLabel = "🎨 লেবেলযুক্ত ২ডি ডায়াগ্রাম আঁকা হচ্ছে (Gemini Nano Banana 2.1)…",
                                mode = inferenceMode,
                                subject = resolvedSubject,
                                isGeneratingDiagram = true
                            )
                        )
                    }
                    try {
                        val bmp = aiRepository.generateVisualDiagram(
                            conceptQuery = "$effectiveQuery ${chapter.orEmpty()}",
                            subject = resolvedSubject,
                            mode = inferenceMode
                        )
                        diagramPath = NotebookImageHelper.persistVisualDiagram(appContext, bmp)
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        Log.e(TAG, "Smart Learning diagram generation failed", e)
                        diagramErr = e.localizedMessage ?: "ভিজ্যুয়াল ডায়াগ্রাম তৈরি করা যায়নি।"
                    }
                }

                val topicTitle = chapter?.takeIf { it.isNotBlank() }
                    ?: effectiveQuery.take(54)

                val persistedAssistantId = studyRepository.saveChatMessage(
                    ChatMessageEntity(
                        role = "assistant",
                        content = finalEvent.fullText,
                        subjectId = resolvedSubject?.id,
                        mode = inferenceMode.storageValue,
                        sourceLabel = finalEvent.sourceLabel,
                        latencyMs = finalEvent.latencyMs,
                        sectionId = ChatSection.SMART_LEARNING.id,
                        diagramPath = diagramPath,
                        topicLabel = topicTitle
                    )
                )
                if (persistedAssistantId >= nextMessageId) nextMessageId = persistedAssistantId + 1

                val assistantMsg = SmartLearningMessage(
                    id = persistedAssistantId,
                    role = "assistant",
                    content = finalEvent.fullText,
                    topicLabel = topicTitle,
                    subject = resolvedSubject,
                    chapterName = chapter,
                    mode = inferenceMode,
                    sourceLabel = finalEvent.sourceLabel,
                    generatedDiagramPath = diagramPath,
                    diagramError = diagramErr
                )

                _uiState.update { s ->
                    s.copy(
                        messages = s.messages + assistantMsg,
                        isGenerating = false,
                        streamingDraft = null
                    )
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Smart Learning timed out", e)
                _uiState.update { s ->
                    s.copy(
                        isGenerating = false,
                        streamingDraft = null,
                        errorMessage = "অনলাইন উত্তর ৬০ সেকেন্ডের মধ্যে আসেনি। আবার চেষ্টা করো বা অফলাইন মোড ব্যবহার করো।"
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Smart Learning tutor failed", e)
                _uiState.update { s ->
                    s.copy(
                        isGenerating = false,
                        streamingDraft = null,
                        errorMessage = e.localizedMessage ?: "স্মার্ট লার্নিং উত্তর তৈরি করতে সমস্যা হয়েছে।"
                    )
                }
            }
        }
    }

    /**
     * Allows generating an on-demand AI Visual Diagram for any already-explained message card.
     */
    fun generateDiagramForMessage(message: SmartLearningMessage, inferenceMode: InferenceMode) {
        if (inferenceMode == InferenceMode.OFFLINE) {
            _uiState.update {
                it.copy(errorMessage = "🎨 AI দিয়ে নতুন ডায়াগ্রাম আঁকতে উপরের সুইচ দিয়ে অনলাইন মোড (Online Mode) চালু করো।")
            }
            return
        }

        scope.launch {
            _uiState.update { s ->
                s.copy(
                    messages = s.messages.map {
                        if (it.id == message.id) it.copy(isGeneratingDiagram = true, diagramError = null) else it
                    }
                )
            }
            try {
                val bmp = aiRepository.generateVisualDiagram(
                    conceptQuery = "${message.topicLabel} — ${message.content.take(260)}",
                    subject = message.subject,
                    mode = inferenceMode
                )
                val path = NotebookImageHelper.persistVisualDiagram(appContext, bmp)
                studyRepository.updateChatDiagramPath(message.id, path)
                _uiState.update { s ->
                    s.copy(
                        messages = s.messages.map {
                            if (it.id == message.id) it.copy(
                                isGeneratingDiagram = false,
                                generatedDiagramPath = path,
                                diagramError = null
                            ) else it
                        }
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "On-demand diagram failed", e)
                _uiState.update { s ->
                    s.copy(
                        messages = s.messages.map {
                            if (it.id == message.id) it.copy(
                                isGeneratingDiagram = false,
                                diagramError = e.localizedMessage ?: "ডায়াগ্রাম তৈরি করা যায়নি।"
                            ) else it
                        }
                    )
                }
            }
        }
    }

    // ==================== 2-Minute Quick Quiz ("পড়া বুঝেছি — ২ মিনিটের কুইজ দিন") ====================

    fun startTwoMinuteQuiz(message: SmartLearningMessage, inferenceMode: InferenceMode) {
        if (_uiState.value.isLoadingQuiz) return
        quizTimerJob?.cancel()
        scope.launch {
            _uiState.update {
                it.copy(
                    isLoadingQuiz = true,
                    activeQuiz = null,
                    quizSelectedAnswers = emptyMap(),
                    quizSubmitted = false,
                    errorMessage = null
                )
            }
            try {
                val quiz = smartLearningService.generateQuickTopicQuiz(
                    topicTitle = message.topicLabel,
                    explanationContext = message.content,
                    subject = message.subject ?: _uiState.value.selectedSubject,
                    chapterName = message.chapterName ?: _uiState.value.selectedChapter,
                    mode = inferenceMode
                )
                _uiState.update {
                    it.copy(
                        isLoadingQuiz = false,
                        activeQuiz = quiz,
                        quizRemainingSeconds = quiz.durationSeconds,
                        quizSelectedAnswers = emptyMap(),
                        quizSubmitted = false
                    )
                }
                startQuizCountdown()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Quick quiz generation failed", e)
                _uiState.update {
                    it.copy(
                        isLoadingQuiz = false,
                        errorMessage = e.localizedMessage ?: "২ মিনিটের কুইজ তৈরি করতে সমস্যা হয়েছে।"
                    )
                }
            }
        }
    }

    fun selectQuizOption(questionId: String, optionIndex: Int) {
        if (_uiState.value.quizSubmitted) return
        _uiState.update {
            it.copy(quizSelectedAnswers = it.quizSelectedAnswers + (questionId to optionIndex))
        }
    }

    fun submitQuickQuiz() {
        val state = _uiState.value
        val quiz = state.activeQuiz ?: return
        if (state.quizSubmitted) return

        quizTimerJob?.cancel()
        val correct = quiz.questions.count { q -> state.quizSelectedAnswers[q.id] == q.correctIndex }
        val total = quiz.questions.size.coerceAtLeast(1)
        val pct = ((correct.toFloat() / total.toFloat()) * 100f).roundToInt()

        _uiState.update { it.copy(quizSubmitted = true) }

        scope.launch {
            studyRepository.saveExamAttempt(
                ExamAttemptEntity(
                    subjectId = (quiz.subject ?: WbbseSubject.PHYSICAL_SCIENCE).id,
                    examType = "SMART_QUIZ",
                    questionSummary = "স্মার্ট লার্নিং ২-মিনিট কুইজ: ${quiz.topicTitle.take(42)}",
                    scoreObtained = correct.toFloat(),
                    maxMarks = total.toFloat(),
                    accuracyPercent = pct,
                    feedbackSummary = "২-মিনিট কুইজ স্কোর: $correct/$total ($pct%)"
                )
            )
        }
    }

    fun closeQuickQuiz() {
        quizTimerJob?.cancel()
        _uiState.update {
            it.copy(
                activeQuiz = null,
                quizSelectedAnswers = emptyMap(),
                quizSubmitted = false
            )
        }
    }

    private fun startQuizCountdown() {
        quizTimerJob?.cancel()
        quizTimerJob = scope.launch {
            while (true) {
                delay(1_000L)
                val s = _uiState.value
                if (s.activeQuiz == null || s.quizSubmitted) break
                val next = (s.quizRemainingSeconds - 1).coerceAtLeast(0)
                _uiState.update { it.copy(quizRemainingSeconds = next) }
                if (next == 0) {
                    submitQuickQuiz()
                    break
                }
            }
        }
    }

    // ==================== Smart Memorizer ("এই টপিকের বড় প্রশ্ন মুখস্থ করুন") ====================

    fun startSmartMemorizer(message: SmartLearningMessage, inferenceMode: InferenceMode) {
        if (_uiState.value.isLoadingMemorizer) return
        scope.launch {
            _uiState.update {
                it.copy(
                    isLoadingMemorizer = true,
                    activeMemorizer = null,
                    activeRecallHiddenMode = false,
                    revealedCardSteps = emptySet(),
                    errorMessage = null
                )
            }
            try {
                val deck = smartLearningService.generateSmartMemorizerDeck(
                    topicTitle = message.topicLabel,
                    explanationContext = message.content,
                    subject = message.subject ?: _uiState.value.selectedSubject,
                    chapterName = message.chapterName ?: _uiState.value.selectedChapter,
                    mode = inferenceMode
                )
                _uiState.update {
                    it.copy(
                        isLoadingMemorizer = false,
                        activeMemorizer = deck,
                        activeRecallHiddenMode = false,
                        revealedCardSteps = emptySet()
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Smart Memorizer generation failed", e)
                _uiState.update {
                    it.copy(
                        isLoadingMemorizer = false,
                        errorMessage = e.localizedMessage ?: "স্মার্ট মেমোরাইজার তৈরি করতে সমস্যা হয়েছে।"
                    )
                }
            }
        }
    }

    fun toggleMemorizerActiveRecall() {
        _uiState.update {
            val nextHidden = !it.activeRecallHiddenMode
            it.copy(
                activeRecallHiddenMode = nextHidden,
                revealedCardSteps = emptySet()
            )
        }
    }

    fun toggleMemorizerCardReveal(stepNumber: Int) {
        _uiState.update { s ->
            val updated = if (stepNumber in s.revealedCardSteps) {
                s.revealedCardSteps - stepNumber
            } else {
                s.revealedCardSteps + stepNumber
            }
            s.copy(revealedCardSteps = updated)
        }
    }

    fun closeSmartMemorizer() {
        _uiState.update {
            it.copy(
                activeMemorizer = null,
                activeRecallHiddenMode = false,
                revealedCardSteps = emptySet()
            )
        }
    }

    fun stopAll() {
        tutorJob?.cancel()
        quizTimerJob?.cancel()
    }
}
