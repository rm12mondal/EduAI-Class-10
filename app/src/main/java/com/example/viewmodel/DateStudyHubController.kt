package com.example.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.data.ai.AiRepository
import com.example.data.ai.AiStreamEvent
import com.example.data.local.ChatMessageEntity
import com.example.data.local.StudyRepository
import com.example.data.media.NotebookImageHelper
import com.example.model.AutoStudyPlan
import com.example.model.ChatPromptOptions
import com.example.model.ChatSection
import com.example.model.DateStudyDateUtils
import com.example.model.DateStudyHistoryLogItem
import com.example.model.DateStudyHubUiState
import com.example.model.DateStudyMode
import com.example.model.InferenceMode
import com.example.model.LlamaInferenceConfig
import com.example.model.SectionChatUiState
import com.example.model.WbbseSubject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val DATE_STUDY_TAG = "DateStudyHubController"

private data class DateStudyTransientState(
    val isGenerating: Boolean = false,
    val streamingDraft: StreamingDraft? = null,
    val pendingPhoto: PendingPhotoAttachment? = null,
    val errorMessage: String? = null
)

/**
 * Manages the Auto-Paced Exam-Countdown Start Hub ("▶️ Start (তারিখ দিয়ে পড়া শুরু করুন)"):
 * - Enforces Madhyamik Exam Date selection before opening the workspace.
 * - Automatically computes `daysRemaining` from Today's Date on the device clock
 *   and auto-selects the optimal study tier (>90d Deep Study, 45–90d Crash Course,
 *   15–44d High-Weightage VVI, <15d Final Revision).
 * - Persists daily study sessions cleanly in Room under `date_study_YYYY-MM-DD`
 *   with reactive Date-Wise History logging and full multimodal AI controls.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DateStudyHubController(
    private val appContext: Context,
    private val scope: CoroutineScope,
    private val studyRepository: StudyRepository,
    private val aiRepository: AiRepository,
    private val getInferenceMode: () -> InferenceMode,
    private val getLlamaConfig: () -> LlamaInferenceConfig,
    private val getExactTextbookMode: () -> Boolean,
    private val getTargetExamYear: () -> Int
) {
    private val _hubNavState = MutableStateFlow(DateStudyHubUiState())
    private val _transientState = MutableStateFlow(DateStudyTransientState())
    private var activeGenerationJob: Job? = null

    private val historyLogsFlow = studyRepository.observeDateStudyMessages().map { allMsgs ->
        allMsgs
            .filter { it.sectionId.startsWith(ChatSection.DATE_STUDY_PREFIX) }
            .groupBy { it.sectionId.removePrefix(ChatSection.DATE_STUDY_PREFIX) }
            .map { (dateIso, msgs) ->
                val newest = msgs.maxByOrNull { it.timestamp } ?: msgs.first()
                val lastUserMsg = msgs.firstOrNull { it.role == "user" && it.content.isNotBlank() }
                DateStudyHistoryLogItem(
                    dateIso = dateIso,
                    displayDateBn = DateStudyDateUtils.formatReadableDate(dateIso),
                    messageCount = msgs.size,
                    lastTopic = (lastUserMsg?.content ?: newest.topicLabel.ifBlank { newest.content })
                        .replace("\n", " ")
                        .trim()
                        .take(72),
                    lastModeLabel = newest.sourceLabel.ifBlank { "স্টাডি সেশন" },
                    lastTimestampMs = newest.timestamp
                )
            }
            .sortedByDescending { it.dateIso }
    }

    val hubState: StateFlow<DateStudyHubUiState> = combine(
        _hubNavState,
        historyLogsFlow
    ) { nav, logs ->
        nav.copy(historyLogs = logs)
    }.stateIn(
        scope = scope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = DateStudyHubUiState()
    )

    private val currentDateMessagesFlow = _hubNavState
        .map { it.selectedDateIso }
        .distinctUntilChanged()
        .flatMapLatest { dateIso ->
            studyRepository.observeRawSectionMessages(ChatSection.dateStudySectionId(dateIso))
        }

    val chatState: StateFlow<SectionChatUiState> = combine(
        currentDateMessagesFlow,
        _transientState
    ) { savedMessages, transient ->
        SectionChatUiState(
            section = ChatSection.DATE_STUDY,
            messages = savedMessages,
            isGenerating = transient.isGenerating,
            streamingDraft = transient.streamingDraft,
            pendingPhoto = transient.pendingPhoto,
            errorMessage = transient.errorMessage
        )
    }.stateIn(
        scope = scope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = SectionChatUiState(section = ChatSection.DATE_STUDY)
    )

    /**
     * Mandatory Step 1 when tapping "▶️ Start (তারিখ দিয়ে পড়া শুরু করুন)" in the "D" menu:
     * opens the Exam Date Selection dialog and refreshes live countdown from device clock.
     */
    fun openMandatoryDateSelection() {
        _hubNavState.update { current ->
            val refreshedDays = DateStudyDateUtils.computeDaysRemaining(current.examDateEpochMs)
            val refreshedPlan = AutoStudyPlan.fromDaysRemaining(refreshedDays)
            current.copy(
                isDatePickerDialogOpen = true,
                daysRemaining = refreshedDays,
                autoPlan = refreshedPlan
            )
        }
    }

    fun openChangeDateDialog() {
        _hubNavState.update { current ->
            val refreshedDays = DateStudyDateUtils.computeDaysRemaining(current.examDateEpochMs)
            current.copy(
                isDatePickerDialogOpen = true,
                showHistorySheet = false,
                daysRemaining = refreshedDays,
                autoPlan = AutoStudyPlan.fromDaysRemaining(refreshedDays)
            )
        }
    }

    fun dismissDatePickerDialog() {
        _hubNavState.update { it.copy(isDatePickerDialogOpen = false) }
    }

    /**
     * Locks the chosen Madhyamik Exam Date, computes exact `daysRemaining` from Today's Date,
     * auto-selects the study plan tier & default mode, and launches the workspace.
     */
    fun confirmExamDateAndStart(examDateEpochMs: Long) {
        val examIso = DateStudyDateUtils.utcMillisToIso(examDateEpochMs)
        val daysLeft = DateStudyDateUtils.computeDaysRemaining(examDateEpochMs)
        val autoPlan = AutoStudyPlan.fromDaysRemaining(daysLeft)
        val todayIso = DateStudyDateUtils.todayIso()

        _hubNavState.update { current ->
            current.copy(
                isDatePickerDialogOpen = false,
                isWorkspaceOpen = true,
                showHistorySheet = false,
                examDateEpochMs = examDateEpochMs,
                examDateIso = examIso,
                daysRemaining = daysLeft,
                autoPlan = autoPlan,
                selectedDateIso = if (current.isWorkspaceOpen) current.selectedDateIso else todayIso,
                selectedMode = autoPlan.defaultMode
            )
        }
    }

    fun setHistorySheetVisible(visible: Boolean) {
        _hubNavState.update { it.copy(showHistorySheet = visible) }
    }

    fun jumpToHistoryDate(dateIso: String) {
        if (dateIso != _hubNavState.value.selectedDateIso) {
            activeGenerationJob?.cancel()
            _transientState.update { DateStudyTransientState() }
        }
        _hubNavState.update {
            it.copy(
                selectedDateIso = dateIso,
                showHistorySheet = false,
                isWorkspaceOpen = true
            )
        }
    }

    fun selectMode(mode: DateStudyMode) {
        _hubNavState.update { it.copy(selectedMode = mode) }
    }

    fun selectSubject(subject: WbbseSubject?) {
        _hubNavState.update { it.copy(selectedSubject = subject) }
    }

    fun closeWorkspace() {
        _hubNavState.update {
            it.copy(
                isWorkspaceOpen = false,
                isDatePickerDialogOpen = false,
                showHistorySheet = false
            )
        }
    }

    fun attachPhotoUri(uri: Uri) {
        scope.launch {
            val bitmap = NotebookImageHelper.loadScaledBitmap(appContext, uri)
            if (bitmap != null) {
                _transientState.update {
                    it.copy(
                        pendingPhoto = PendingPhotoAttachment(uri = uri, previewBitmap = bitmap),
                        errorMessage = null
                    )
                }
            } else {
                _transientState.update {
                    it.copy(errorMessage = "ছবিটি লোড করা যায়নি। পরিষ্কার একটি ছবি বেছে নাও।")
                }
            }
        }
    }

    fun clearPendingPhoto() {
        _transientState.update { it.copy(pendingPhoto = null) }
    }

    fun clearError() {
        _transientState.update { it.copy(errorMessage = null) }
    }

    fun clearCurrentDateChat() {
        val dateIso = _hubNavState.value.selectedDateIso
        activeGenerationJob?.cancel()
        scope.launch {
            studyRepository.clearRawSectionChat(ChatSection.dateStudySectionId(dateIso))
            _transientState.update { DateStudyTransientState() }
        }
    }

    fun triggerOneTapModeAction() {
        val nav = _hubNavState.value
        val freshDays = DateStudyDateUtils.computeDaysRemaining(nav.examDateEpochMs)
        val freshPlan = AutoStudyPlan.fromDaysRemaining(freshDays)
        val prompt = DateStudyDateUtils.buildOneTapPrompt(
            mode = nav.selectedMode,
            subject = nav.selectedSubject,
            autoPlan = freshPlan,
            daysRemaining = freshDays,
            examDateIso = nav.examDateIso
        )
        sendMessage(prompt)
    }

    fun sendMessage(rawText: String) {
        val currentTransient = _transientState.value
        val pendingPhoto = currentTransient.pendingPhoto
        val trimmed = rawText.trim()
        if ((trimmed.isEmpty() && pendingPhoto == null) || currentTransient.isGenerating) return

        val nav = _hubNavState.value
        val daysLeft = DateStudyDateUtils.computeDaysRemaining(nav.examDateEpochMs)
        val autoPlan = AutoStudyPlan.fromDaysRemaining(daysLeft)
        val activeMode = nav.selectedMode
        val activeSubject = nav.selectedSubject
        val sessionDateIso = nav.selectedDateIso
        val rawSectionId = ChatSection.dateStudySectionId(sessionDateIso)
        val inferenceMode = getInferenceMode()
        val llamaConfig = getLlamaConfig()
        val examYear = nav.examDateIso.take(4).toIntOrNull() ?: getTargetExamYear()

        // Keep live countdown synced in state
        if (daysLeft != nav.daysRemaining || autoPlan != nav.autoPlan) {
            _hubNavState.update { it.copy(daysRemaining = daysLeft, autoPlan = autoPlan) }
        }

        val effectiveQuery = trimmed.ifBlank {
            when (activeMode) {
                DateStudyMode.SMART_LEARNING ->
                    "পরীক্ষার $daysLeft দিন বাকি—এই ছবির টপিকটি সহজ বাংলায় বাস্তব উদাহরণসহ ধাপে ধাপে বুঝিয়ে দাও।"
                DateStudyMode.FAST_LEARNING ->
                    "পরীক্ষার $daysLeft দিন বাকি—এই ছবির মূল ধারণাটি মাধ্যমিক $examYear-এর জন্য ফাস্ট মাইক্রো-সামারি আকারে বুঝিয়ে দাও।"
                DateStudyMode.SYLLABUS_PLAN ->
                    "পরীক্ষার $daysLeft দিন বাকি (${autoPlan.labelBn})—এই ছবির অধ্যায়টি আজকের ডেইলি টার্গেটে কীভাবে দ্রুত শেষ করব তা বুঝিয়ে দাও।"
                DateStudyMode.BRAHMASTRAM ->
                    "পরীক্ষার $daysLeft দিন বাকি—এই ছবির প্রশ্ন/নোটটি ব্রহ্মাস্ত্র শিওর-শট বোর্ড মান অনুযায়ী সমাধান ও সাজেশন পয়েন্ট দাও।"
                DateStudyMode.MOCK_EXAM ->
                    "এই হাতের লেখা খাতার উত্তরটি WBBSE মাধ্যমিক মান অনুযায়ী মূল্যায়ন করো, ভুলত্রুটি দেখাও এবং সঠিক নম্বর দাও।"
            }
        }

        activeGenerationJob?.cancel()
        activeGenerationJob = scope.launch {
            _transientState.update {
                it.copy(
                    pendingPhoto = null,
                    isGenerating = true,
                    errorMessage = null,
                    streamingDraft = StreamingDraft(
                        text = "",
                        sourceLabel = when (activeMode) {
                            DateStudyMode.SMART_LEARNING -> "✨ স্মার্ট লার্নিং (${daysLeft}d বাকি) লিখছে…"
                            DateStudyMode.FAST_LEARNING -> "🚀 ফাস্ট লার্নিং VVI (${daysLeft}d বাকি) তৈরি হচ্ছে…"
                            DateStudyMode.SYLLABUS_PLAN -> "⏳ ${autoPlan.shortBadgeBn} (${daysLeft}d বাকি) রুটিন তৈরি হচ্ছে…"
                            DateStudyMode.BRAHMASTRAM -> "⚡ ব্রহ্মাস্ত্র (${daysLeft}d বাকি) সাজেশন তৈরি হচ্ছে…"
                            DateStudyMode.MOCK_EXAM -> "📝 মক এক্সাম (${daysLeft}d বাকি) প্রশ্ন/মূল্যায়ন তৈরি হচ্ছে…"
                        },
                        mode = inferenceMode,
                        subject = activeSubject
                    )
                )
            }

            try {
                val recentHistoryContext = studyRepository.getRawSectionMessagesSnapshot(rawSectionId)
                    .takeLast(6)
                    .joinToString("\n") { msg ->
                        val speaker = if (msg.role == "user") "Student" else "Tutor"
                        "$speaker: ${msg.content.take(420)}"
                    }

                val modeContextHint = buildAutoPacedModeContextHint(
                    examDateIso = nav.examDateIso,
                    daysRemaining = daysLeft,
                    autoPlan = autoPlan,
                    mode = activeMode,
                    subject = activeSubject,
                    targetExamYear = examYear
                )

                val options = ChatPromptOptions(
                    smartToThePoint = activeMode == DateStudyMode.BRAHMASTRAM ||
                        activeMode == DateStudyMode.MOCK_EXAM ||
                        autoPlan == AutoStudyPlan.FINAL_REVISION,
                    visualDiagramRequested = false,
                    translateMode = false,
                    targetMarks = null,
                    exactTextbookDefinition = getExactTextbookMode(),
                    targetExamYear = examYear,
                    syllabusPlanLabel = "${autoPlan.shortBadgeBn} ($daysLeft days left until ${nav.examDateIso})",
                    fastLearningMicroMode = activeMode == DateStudyMode.FAST_LEARNING ||
                        activeMode == DateStudyMode.SYLLABUS_PLAN ||
                        autoPlan == AutoStudyPlan.HIGH_WEIGHTAGE_VVI,
                    sectionContextHint = modeContextHint,
                    stepByStepInteractiveMode = false,
                    memoryTrickMode = false,
                    recentConversationContext = recentHistoryContext
                )

                val savedImagePath = pendingPhoto?.let {
                    NotebookImageHelper.persistThumbnail(appContext, it.previewBitmap)
                }

                studyRepository.saveChatMessage(
                    ChatMessageEntity(
                        role = "user",
                        content = effectiveQuery,
                        subjectId = activeSubject?.id,
                        mode = inferenceMode.storageValue,
                        sourceLabel = "${activeMode.labelBn} • ${daysLeft}d বাকি",
                        imagePath = savedImagePath,
                        sectionId = rawSectionId,
                        topicLabel = effectiveQuery.take(54)
                    )
                )

                var completedEvent: AiStreamEvent.Completed? = null

                aiRepository.generateAnswerStream(
                    query = effectiveQuery,
                    selectedSubject = activeSubject,
                    mode = inferenceMode,
                    config = llamaConfig,
                    imageBitmap = pendingPhoto?.previewBitmap,
                    options = options
                ).collect { event ->
                    when (event) {
                        is AiStreamEvent.Partial -> {
                            _transientState.update { s ->
                                s.copy(
                                    streamingDraft = StreamingDraft(
                                        text = event.accumulatedText,
                                        sourceLabel = "${activeMode.shortHeaderBn} • ${daysLeft} দিন বাকি",
                                        mode = inferenceMode,
                                        subject = activeSubject
                                    )
                                )
                            }
                        }

                        is AiStreamEvent.Completed -> completedEvent = event
                    }
                }

                val finalEvent = completedEvent ?: throw IllegalStateException("উত্তর পাওয়া যায়নি।")

                studyRepository.saveChatMessage(
                    ChatMessageEntity(
                        role = "assistant",
                        content = finalEvent.fullText,
                        subjectId = (finalEvent.resolvedSubject ?: activeSubject)?.id,
                        mode = inferenceMode.storageValue,
                        sourceLabel = "${activeMode.shortHeaderBn} • ${daysLeft} দিন বাকি • ${finalEvent.sourceLabel}",
                        latencyMs = finalEvent.latencyMs,
                        sectionId = rawSectionId,
                        topicLabel = effectiveQuery.take(54)
                    )
                )

                _transientState.update {
                    it.copy(
                        isGenerating = false,
                        streamingDraft = null
                    )
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(DATE_STUDY_TAG, "Date study timed out", e)
                _transientState.update {
                    it.copy(
                        isGenerating = false,
                        streamingDraft = null,
                        errorMessage = "অনলাইন উত্তর ৬০ সেকেন্ডের মধ্যে আসেনি। আবার চেষ্টা করো বা অফলাইন মোড ব্যবহার করো।"
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(DATE_STUDY_TAG, "Date study generation failed", e)
                _transientState.update {
                    it.copy(
                        isGenerating = false,
                        streamingDraft = null,
                        errorMessage = e.localizedMessage ?: "উত্তর তৈরি করতে সমস্যা হয়েছে।"
                    )
                }
            }
        }
    }

    private fun buildAutoPacedModeContextHint(
        examDateIso: String,
        daysRemaining: Int,
        autoPlan: AutoStudyPlan,
        mode: DateStudyMode,
        subject: WbbseSubject?,
        targetExamYear: Int
    ): String {
        val subjLabel = subject?.let { "${it.bengaliName} (${it.englishName})" } ?: "All WBBSE Class 10 Subjects"
        val examDisplay = DateStudyDateUtils.formatExamDateReadable(examDateIso)
        val todayDisplay = DateStudyDateUtils.formatReadableDate(DateStudyDateUtils.todayIso())
        val modeInstruction = when (mode) {
            DateStudyMode.SMART_LEARNING ->
                "SMART LEARNING MODE: Explain concepts in warm, crystal-clear simple Bengali with relatable analogies, step-by-step clarity, separate-line clean Unicode formulas, and pacing tuned to exactly $daysRemaining days left."
            DateStudyMode.FAST_LEARNING ->
                "FAST LEARNING VVI MODE: Deliver ultra-concise, high-weightage micro-concept summaries focused on Madhyamik $targetExamYear VVI patterns, separate-line formulas, and 10-second memory tricks for the remaining $daysRemaining days."
            DateStudyMode.SYLLABUS_PLAN ->
                "CRASH COURSE / SYLLABUS PACE MODE: Build an exact daily study routine, priority chapter list, time blocks, and end-of-day checklist calibrated precisely to the $daysRemaining days remaining before $examDisplay."
            DateStudyMode.BRAHMASTRAM ->
                "BRAHMASTRAM FINAL REVISION MODE: Prioritize 100% high-probability WBBSE Class 10 board questions, topper writing keywords, separate-line formulas, and last-minute scoring strategies for $daysRemaining days remaining."
            DateStudyMode.MOCK_EXAM ->
                "MOCK EXAM MODE: Act as a WBBSE Class 10 Board Examiner. When asked for a test, generate a structured question paper WITHOUT revealing answers immediately. When the student submits text or notebook photo answers, grade strictly with marks and model board corrections."
        }
        return "AUTO-PACED EXAM COUNTDOWN HUB | Today: $todayDisplay | Madhyamik Exam Date: $examDisplay ($examDateIso) | EXACT DAYS REMAINING: $daysRemaining days | Auto-Selected Plan: ${autoPlan.labelBn} (${autoPlan.aiStrategyEn}) | Active Mode: ${mode.labelBn} | Subject: $subjLabel | $modeInstruction"
    }

    fun stopAll() {
        activeGenerationJob?.cancel()
        activeGenerationJob = null
    }
}
