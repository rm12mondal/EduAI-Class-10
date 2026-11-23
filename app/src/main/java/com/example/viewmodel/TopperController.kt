package com.example.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.data.ai.TopperAiService
import com.example.data.gguf.GgufModelManager
import com.example.data.local.ExamAttemptEntity
import com.example.data.local.StudyRepository
import com.example.data.local.TopperMistakeEntity
import com.example.data.local.TopperSetProgressEntity
import com.example.data.media.NotebookImageHelper
import com.example.data.topper.TopperQuestionBank
import com.example.model.InferenceMode
import com.example.model.LlamaInferenceConfig
import com.example.model.SectionTimeBreakdown
import com.example.model.SubjectMasteryStatus
import com.example.model.TimingBucket
import com.example.model.TopperPaper
import com.example.model.TopperSectionType
import com.example.model.TopperSubjectTrack
import com.example.model.TopperSubmissionReport
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

data class TopperUiState(
    val selectedTrack: TopperSubjectTrack = TopperSubjectTrack.PHYSICAL_SCIENCE,
    val twistedChallengeMode: Boolean = false,
    val full90MarkBlueprint: Boolean = false,
    val masteryList: List<SubjectMasteryStatus> = emptyList(),
    val totalActiveMistakesCount: Int = 0,
    val isGeneratingPaper: Boolean = false,
    val activePaper: TopperPaper? = null,
    val activeTimingBucket: TimingBucket = TimingBucket.MCQ,
    val remainingSeconds: Long = 0L,
    val isTimerPaused: Boolean = false,
    val isTimeUp: Boolean = false,
    val sectionTimeBreakdown: SectionTimeBreakdown = SectionTimeBreakdown(),
    val mcqAnswers: Map<String, Int> = emptyMap(),
    val typedAnswers: Map<String, String> = emptyMap(),
    val photoPagesByQuestion: Map<String, List<PendingPhotoAttachment>> = emptyMap(),
    val isEvaluatingSubmission: Boolean = false,
    val latestReport: TopperSubmissionReport? = null,
    val errorMessage: String? = null
)

private data class LocalTopperState(
    val selectedTrack: TopperSubjectTrack = TopperSubjectTrack.PHYSICAL_SCIENCE,
    val twistedChallengeMode: Boolean = false,
    val full90MarkBlueprint: Boolean = false,
    val isGeneratingPaper: Boolean = false,
    val activePaper: TopperPaper? = null,
    val activeTimingBucket: TimingBucket = TimingBucket.MCQ,
    val remainingSeconds: Long = 0L,
    val isTimerPaused: Boolean = false,
    val isTimeUp: Boolean = false,
    val sectionTimeBreakdown: SectionTimeBreakdown = SectionTimeBreakdown(),
    val mcqAnswers: Map<String, Int> = emptyMap(),
    val typedAnswers: Map<String, String> = emptyMap(),
    val photoPagesByQuestion: Map<String, List<PendingPhotoAttachment>> = emptyMap(),
    val isEvaluatingSubmission: Boolean = false,
    val latestReport: TopperSubmissionReport? = null,
    val errorMessage: String? = null
)

class TopperController(
    private val appContext: Context,
    private val scope: CoroutineScope,
    private val studyRepository: StudyRepository,
    private val ggufModelManager: GgufModelManager,
    private val topperAiService: TopperAiService = TopperAiService()
) {
    private val local = MutableStateFlow(LocalTopperState())
    private var timerJob: Job? = null

    val uiState: StateFlow<TopperUiState> = combine(
        local,
        studyRepository.allTopperSetProgress,
        studyRepository.activeTopperMistakes
    ) { state, progressRows, mistakeRows ->
        val mastery = TopperSubjectTrack.entries.map { track ->
            val trackRows = progressRows.filter { it.trackId == track.id }
            val completedSets = trackRows.map { it.setNumber }.distinct().size
            val totalSets = track.totalRotationalSets
            val coveredPct = ((completedSets.toFloat() / totalSets.toFloat()) * 100f)
                .roundToInt()
                .coerceIn(0, 100)
            val remaining = (totalSets - completedSets).coerceAtLeast(0)
            val mistakesCount = mistakeRows.count { it.trackId == track.id }
            SubjectMasteryStatus(
                track = track,
                completedSetsCount = completedSets,
                totalSetsCount = totalSets,
                coveredPercent = coveredPct,
                remainingSetsCount = remaining,
                pendingMistakesCount = mistakesCount
            )
        }

        TopperUiState(
            selectedTrack = state.selectedTrack,
            twistedChallengeMode = state.twistedChallengeMode,
            full90MarkBlueprint = state.full90MarkBlueprint,
            masteryList = mastery,
            totalActiveMistakesCount = mistakeRows.size,
            isGeneratingPaper = state.isGeneratingPaper,
            activePaper = state.activePaper,
            activeTimingBucket = state.activeTimingBucket,
            remainingSeconds = state.remainingSeconds,
            isTimerPaused = state.isTimerPaused,
            isTimeUp = state.isTimeUp,
            sectionTimeBreakdown = state.sectionTimeBreakdown,
            mcqAnswers = state.mcqAnswers,
            typedAnswers = state.typedAnswers,
            photoPagesByQuestion = state.photoPagesByQuestion,
            isEvaluatingSubmission = state.isEvaluatingSubmission,
            latestReport = state.latestReport,
            errorMessage = state.errorMessage
        )
    }.stateIn(
        scope = scope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = TopperUiState()
    )

    fun selectTrack(track: TopperSubjectTrack) {
        local.update { it.copy(selectedTrack = track) }
    }

    fun toggleTwistedChallenge(enabled: Boolean) {
        local.update { it.copy(twistedChallengeMode = enabled) }
    }

    fun toggleFullBlueprint(enabled: Boolean) {
        local.update { it.copy(full90MarkBlueprint = enabled) }
    }

    fun dismissError() {
        local.update { it.copy(errorMessage = null) }
    }

    fun setActiveTimingBucket(bucket: TimingBucket) {
        local.update { it.copy(activeTimingBucket = bucket) }
    }

    fun toggleTimerPause() {
        local.update { it.copy(isTimerPaused = !it.isTimerPaused) }
    }

    fun exitActivePaper() {
        timerJob?.cancel()
        local.update {
            it.copy(
                activePaper = null,
                latestReport = null,
                mcqAnswers = emptyMap(),
                typedAnswers = emptyMap(),
                photoPagesByQuestion = emptyMap(),
                sectionTimeBreakdown = SectionTimeBreakdown(),
                remainingSeconds = 0L,
                isTimerPaused = false,
                isTimeUp = false
            )
        }
    }

    fun startDailyTopperSet(
        track: TopperSubjectTrack,
        inferenceMode: InferenceMode,
        llamaConfig: LlamaInferenceConfig
    ) {
        if (local.value.isGeneratingPaper) return
        val twisted = local.value.twistedChallengeMode
        val fullBlueprint = local.value.full90MarkBlueprint

        scope.launch {
            local.update {
                it.copy(
                    selectedTrack = track,
                    isGeneratingPaper = true,
                    latestReport = null,
                    errorMessage = null
                )
            }
            try {
                val pastProgress = studyRepository.getTopperTrackProgress(track.id)
                val nextSetNumber = (pastProgress.maxOfOrNull { it.setNumber } ?: 0) + 1
                val usedIds = pastProgress
                    .flatMap { it.usedQuestionIdsCsv.split(",") }
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .toSet()

                val paper = if (inferenceMode == InferenceMode.ONLINE) {
                    topperAiService.generateOnlineTopperPaper(
                        track = track,
                        nextSetNumber = nextSetNumber,
                        previouslyCoveredChapters = usedIds.toList(),
                        twistedChallengeMode = twisted,
                        fullBoardMode = fullBlueprint
                    )
                } else {
                    TopperQuestionBank.buildOfflineRotationalSet(
                        track = track,
                        nextSetNumber = nextSetNumber,
                        previouslyUsedIds = usedIds,
                        twistedChallengeMode = twisted,
                        ggufModelManager = ggufModelManager,
                        llamaConfig = llamaConfig
                    )
                }

                activatePaper(paper)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("TopperController", "Failed to start daily Topper set", e)
                local.update {
                    it.copy(
                        isGeneratingPaper = false,
                        errorMessage = e.localizedMessage ?: "টপার প্রশ্নপত্র তৈরি করতে সমস্যা হয়েছে।"
                    )
                }
            }
        }
    }

    fun start15MinOfflineCapsule(track: TopperSubjectTrack) {
        val paper = TopperQuestionBank.build15MinCapsule(
            track = track,
            ggufModelManager = ggufModelManager
        )
        local.update { it.copy(selectedTrack = track, latestReport = null, errorMessage = null) }
        activatePaper(paper)
    }

    fun startMistakeRecoveryTest(trackFilter: TopperSubjectTrack? = null) {
        scope.launch {
            val mistakes = studyRepository.getActiveMistakesForTrack(trackFilter?.id)
            val fallbackTrack = trackFilter ?: local.value.selectedTrack
            val paper = TopperQuestionBank.buildMistakeRecoveryPaper(mistakes, fallbackTrack)
            if (paper == null) {
                local.update {
                    it.copy(errorMessage = "তোমার ভুলের খাতায় এখন কোনো অমীমাংসিত ভুল নেই! আগে একটি টপার সেট দাও।")
                }
            } else {
                local.update { it.copy(latestReport = null, errorMessage = null) }
                activatePaper(paper)
            }
        }
    }

    fun selectMcqAnswer(questionId: String, optionIndex: Int) {
        local.update {
            it.copy(
                activeTimingBucket = TimingBucket.MCQ,
                mcqAnswers = it.mcqAnswers + (questionId to optionIndex)
            )
        }
    }

    fun updateTypedAnswer(questionId: String, section: TopperSectionType, text: String) {
        local.update {
            it.copy(
                activeTimingBucket = section.timingBucket,
                typedAnswers = it.typedAnswers + (questionId to text)
            )
        }
    }

    fun addQuestionPhotoUri(questionId: String, section: TopperSectionType, uri: Uri) {
        scope.launch {
            val bitmap = NotebookImageHelper.loadScaledBitmap(appContext, uri, maxDimensionPx = 1280)
            if (bitmap != null) {
                local.update { state ->
                    val existing = state.photoPagesByQuestion[questionId].orEmpty()
                    val updated = existing + PendingPhotoAttachment(uri = uri, previewBitmap = bitmap)
                    state.copy(
                        activeTimingBucket = section.timingBucket,
                        photoPagesByQuestion = state.photoPagesByQuestion + (questionId to updated)
                    )
                }
            } else {
                local.update {
                    it.copy(errorMessage = "খাতার ছবি লোড করা যায়নি। পরিষ্কার ছবি বেছে নাও।")
                }
            }
        }
    }

    fun removeQuestionPhoto(questionId: String, pageIndex: Int) {
        local.update { state ->
            val current = state.photoPagesByQuestion[questionId].orEmpty().toMutableList()
            if (pageIndex in current.indices) {
                current.removeAt(pageIndex)
            }
            state.copy(
                photoPagesByQuestion = state.photoPagesByQuestion + (questionId to current)
            )
        }
    }

    fun submitTopperPaper(inferenceMode: InferenceMode) {
        val snapshot = local.value
        val paper = snapshot.activePaper ?: return
        if (snapshot.isEvaluatingSubmission) return

        timerJob?.cancel()
        scope.launch {
            local.update { it.copy(isEvaluatingSubmission = true, errorMessage = null) }
            try {
                val bitmapMap = snapshot.photoPagesByQuestion.mapValues { (_, list) ->
                    list.map { it.previewBitmap }
                }

                val report = if (inferenceMode == InferenceMode.ONLINE) {
                    topperAiService.evaluateTopperSubmissionOnline(
                        paper = paper,
                        mcqAnswers = snapshot.mcqAnswers,
                        typedAnswers = snapshot.typedAnswers,
                        photoPagesByQuestion = bitmapMap,
                        timeBreakdown = snapshot.sectionTimeBreakdown
                    )
                } else {
                    topperAiService.evaluateTopperSubmissionOffline(
                        paper = paper,
                        mcqAnswers = snapshot.mcqAnswers,
                        typedAnswers = snapshot.typedAnswers,
                        photoPagesByQuestion = bitmapMap,
                        timeBreakdown = snapshot.sectionTimeBreakdown
                    )
                }

                // 1. Record set completion for rotational daily tracking (when standard daily Topper Set)
                if (!paper.isCapsule15Min && !paper.isMistakeRecovery && paper.setNumber > 0) {
                    val todayStamp = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                    studyRepository.saveTopperSetProgress(
                        TopperSetProgressEntity(
                            trackId = paper.track.id,
                            setNumber = paper.setNumber,
                            usedQuestionIdsCsv = paper.questions.joinToString(",") { it.id },
                            scoreObtained = report.obtainedMarks,
                            maxMarks = report.totalMarks,
                            dateStamp = todayStamp
                        )
                    )
                }

                // 2. Update Auto Mistake Notebook
                if (paper.isMistakeRecovery) {
                    val masteredIds = report.questionFeedbacks
                        .filter { it.isCorrectOrStrong }
                        .map { it.question.id }
                    studyRepository.resolveTopperMistakes(masteredIds)
                } else {
                    val newMistakes = report.questionFeedbacks
                        .filterNot { it.isCorrectOrStrong }
                        .map { fb ->
                            val q = fb.question
                            TopperMistakeEntity(
                                trackId = paper.track.id,
                                questionId = q.id,
                                sectionCode = q.section.code,
                                marks = q.marks,
                                chapter = q.chapter,
                                questionText = q.questionText,
                                optionsJoined = q.options.joinToString("|||"),
                                correctOptionIndex = q.correctOptionIndex,
                                isTwisted = q.isTwisted,
                                simpleMeaningBn = q.simpleMeaningBn,
                                modelAnswer = q.modelAnswer,
                                presentationTip = q.presentationTip,
                                keyTermsCsv = q.keyTerms.joinToString(","),
                                mistakeNote = fb.feedbackNote
                            )
                        }
                    studyRepository.saveTopperMistakes(newMistakes)
                }

                // 3. Also log into ExamAttemptEntity so Progress Dashboard reflects Topper performance!
                studyRepository.saveExamAttempt(
                    ExamAttemptEntity(
                        subjectId = paper.track.linkedSubject.id,
                        examType = when {
                            paper.isCapsule15Min -> "TOPPER_15M"
                            paper.isMistakeRecovery -> "RECOVERY"
                            else -> "TOPPER"
                        },
                        questionSummary = paper.title.take(72),
                        scoreObtained = report.obtainedMarks,
                        maxMarks = report.totalMarks,
                        accuracyPercent = report.readinessPercent,
                        feedbackSummary = "${report.verdictTitle} (${report.evaluatorSource})"
                    )
                )

                local.update {
                    it.copy(
                        isEvaluatingSubmission = false,
                        latestReport = report,
                        activePaper = null
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("TopperController", "Failed to evaluate Topper submission", e)
                local.update {
                    it.copy(
                        isEvaluatingSubmission = false,
                        errorMessage = e.localizedMessage ?: "টপার খাতা মূল্যায়ন করতে সমস্যা হয়েছে।"
                    )
                }
            }
        }
    }

    fun stopTimer() {
        timerJob?.cancel()
    }

    private fun activatePaper(paper: TopperPaper) {
        timerJob?.cancel()
        local.update {
            it.copy(
                isGeneratingPaper = false,
                activePaper = paper,
                activeTimingBucket = TimingBucket.MCQ,
                remainingSeconds = paper.durationSeconds,
                isTimerPaused = false,
                isTimeUp = false,
                sectionTimeBreakdown = SectionTimeBreakdown(),
                mcqAnswers = emptyMap(),
                typedAnswers = emptyMap(),
                photoPagesByQuestion = emptyMap(),
                latestReport = null
            )
        }
        timerJob = scope.launch {
            while (true) {
                delay(1_000L)
                val current = local.value
                if (current.activePaper == null) break
                if (current.isTimerPaused || current.isTimeUp) continue

                val nextRemaining = (current.remainingSeconds - 1L).coerceAtLeast(0L)
                val nextBreakdown = when (current.activeTimingBucket) {
                    TimingBucket.MCQ -> current.sectionTimeBreakdown.copy(
                        mcqSeconds = current.sectionTimeBreakdown.mcqSeconds + 1L
                    )
                    TimingBucket.SAQ -> current.sectionTimeBreakdown.copy(
                        saqSeconds = current.sectionTimeBreakdown.saqSeconds + 1L
                    )
                    TimingBucket.BROAD -> current.sectionTimeBreakdown.copy(
                        broadSeconds = current.sectionTimeBreakdown.broadSeconds + 1L
                    )
                }
                local.update {
                    it.copy(
                        remainingSeconds = nextRemaining,
                        isTimeUp = nextRemaining == 0L,
                        sectionTimeBreakdown = nextBreakdown
                    )
                }
                if (nextRemaining == 0L) break
            }
        }
    }
}
