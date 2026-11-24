package com.example.viewmodel

import com.example.data.exam.WbbseExamBank
import com.example.data.gguf.GgufModelManager
import com.example.data.local.DailyPlanTaskEntity
import com.example.data.local.ExamAttemptEntity
import com.example.data.local.MasterDataEntity
import com.example.data.local.StudyRepository
import com.example.data.local.StudyStreakEntity
import com.example.data.local.TopperMistakeEntity
import com.example.data.planner.DailyPlannerService
import com.example.data.speech.EduTtsManager
import com.example.data.topper.TopperQuestionBank
import com.example.model.AudioRevisionTrack
import com.example.model.AudioRevisionUiState
import com.example.model.DailyPlannerUiState
import com.example.model.GgufModelStatus
import com.example.model.InferenceMode
import com.example.model.MapBoardDomain
import com.example.model.MapBoardUiState
import com.example.model.RapidFireAnswerRecord
import com.example.model.RapidFireQuestion
import com.example.model.RapidFireResult
import com.example.model.RapidFireTimerMode
import com.example.model.RapidFireUiState
import com.example.model.SmartLearningMessage
import com.example.model.TopperSectionType
import com.example.model.TopperSubjectTrack
import com.example.model.TtsVoiceSettings
import com.example.model.WbbseSubject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Dedicated controller orchestrating the 4 Topper-Making Flagship Upgrades:
 * 1. AI Daily Study Planner & Streak Tracker (Room + Online Gemini / Offline WBBSE)
 * 2. Interactive Map Pointing Practice Board (History 4-mark & Geography 10-mark)
 * 3. Rapid Fire MCQ Speed Booster (20/30 Qs, 30s/Q or 15-min timer, auto-Mistake Notebook)
 * 4. Hands-Free Voice Tutor & Audio Revision Podcast Player (Continuous Bengali/English TTS)
 */
class TopperExtrasController(
    private val scope: CoroutineScope,
    private val studyRepository: StudyRepository,
    private val ggufModelManager: GgufModelManager,
    private val ttsManager: EduTtsManager,
    private val getCurrentVoiceSettings: () -> TtsVoiceSettings,
    private val getCurrentInferenceMode: () -> InferenceMode
) {
    private val plannerService = DailyPlannerService(ggufModelManager)
    private val todayStamp: String get() = formatDate(Date())

    private val _plannerState = MutableStateFlow(DailyPlannerUiState(dateStamp = todayStamp))
    val plannerState: StateFlow<DailyPlannerUiState> = _plannerState.asStateFlow()

    private val _mapBoardState = MutableStateFlow(MapBoardUiState())
    val mapBoardState: StateFlow<MapBoardUiState> = _mapBoardState.asStateFlow()

    private val _rapidFireState = MutableStateFlow(RapidFireUiState())
    val rapidFireState: StateFlow<RapidFireUiState> = _rapidFireState.asStateFlow()

    private val _audioRevisionState = MutableStateFlow(AudioRevisionUiState())
    val audioRevisionState: StateFlow<AudioRevisionUiState> = _audioRevisionState.asStateFlow()

    private var rapidFireTimerJob: Job? = null
    private var autoAdvanceDelayJob: Job? = null

    init {
        scope.launch {
            val today = todayStamp
            combine(
                studyRepository.observeDailyPlanTasks(today),
                studyRepository.studyStreak
            ) { tasks, streakEntity ->
                tasks to (streakEntity ?: StudyStreakEntity())
            }.collect { (tasks, streak) ->
                _plannerState.update { current ->
                    current.copy(
                        dateStamp = today,
                        tasks = tasks,
                        streak = streak,
                        selectedHours = tasks.firstOrNull()?.targetHours ?: streak.selectedDailyHours
                    )
                }
            }
        }
    }

    fun ensureTodayPlanInitialized(masterNotes: List<MasterDataEntity>) {
        scope.launch {
            val today = todayStamp
            val existing = studyRepository.getDailyPlanTasks(today)
            if (existing.isEmpty()) {
                val streak = studyRepository.getStudyStreak()
                val hours = streak.selectedDailyHours.takeIf { it in listOf(2, 4, 6) } ?: 4
                val (tasks, badge) = plannerService.generateDailyPlan(
                    dateStamp = today,
                    targetHours = hours,
                    inferenceMode = InferenceMode.OFFLINE, // fast instant startup; user can regenerate with Online AI anytime
                    allMasterNotes = masterNotes
                )
                studyRepository.replaceDailyPlanTasks(today, tasks)
                _plannerState.update { it.copy(sourceBadge = badge) }
            }
        }
    }

    // ==================== 1. Daily Study Planner & Streak ====================

    fun setPlannerSheetOpen(open: Boolean) {
        _plannerState.update { it.copy(isSheetOpen = open) }
    }

    fun generateNewDailyPlan(hours: Int, masterNotes: List<MasterDataEntity>) {
        val safeHours = hours.takeIf { it in listOf(2, 4, 6) } ?: 4
        scope.launch {
            _plannerState.update { it.copy(selectedHours = safeHours, isGenerating = true) }
            val currentStreak = studyRepository.getStudyStreak()
            studyRepository.saveStudyStreak(currentStreak.copy(selectedDailyHours = safeHours))

            val today = todayStamp
            val (tasks, badge) = plannerService.generateDailyPlan(
                dateStamp = today,
                targetHours = safeHours,
                inferenceMode = getCurrentInferenceMode(),
                allMasterNotes = masterNotes
            )
            studyRepository.replaceDailyPlanTasks(today, tasks)
            _plannerState.update {
                it.copy(
                    isGenerating = false,
                    selectedHours = safeHours,
                    sourceBadge = badge
                )
            }
        }
    }

    fun toggleDailyTaskCompleted(task: DailyPlanTaskEntity) {
        scope.launch {
            val newCompleted = !task.isCompleted
            studyRepository.setDailyTaskCompleted(task.id, newCompleted)

            val streak = studyRepository.getStudyStreak()
            val today = todayStamp
            val updatedTotal = (streak.totalTasksCompleted + if (newCompleted) 1 else -1).coerceAtLeast(0)

            val updatedStreak = if (newCompleted && streak.lastCompletedDateStamp != today) {
                val yesterday = getYesterdayDateStamp()
                val nextStreak = if (streak.lastCompletedDateStamp == yesterday) {
                    streak.currentStreak + 1
                } else {
                    1
                }
                streak.copy(
                    currentStreak = nextStreak,
                    longestStreak = maxOf(streak.longestStreak, nextStreak),
                    lastCompletedDateStamp = today,
                    totalTasksCompleted = updatedTotal
                )
            } else {
                streak.copy(totalTasksCompleted = updatedTotal)
            }
            studyRepository.saveStudyStreak(updatedStreak)
        }
    }

    // ==================== 2. Interactive Map Pointing Board ====================

    fun openMapBoard(domain: MapBoardDomain) {
        _mapBoardState.value = MapBoardUiState(isOpen = true, domain = domain)
    }

    fun switchMapBoardDomain(domain: MapBoardDomain) {
        _mapBoardState.update { it.copy(domain = domain) }
    }

    fun closeMapBoard() {
        _mapBoardState.update { it.copy(isOpen = false) }
    }

    // ==================== 3. Rapid Fire MCQ Speed Booster ====================

    fun openRapidFire(subject: WbbseSubject? = null) {
        stopRapidFireTimer()
        _rapidFireState.value = RapidFireUiState(
            isOpen = true,
            selectedSubject = subject,
            questionCountTarget = 20,
            timerMode = RapidFireTimerMode.PER_QUESTION_30S
        )
    }

    fun closeRapidFire() {
        stopRapidFireTimer()
        _rapidFireState.update { it.copy(isOpen = false, isRunning = false) }
    }

    fun updateRapidFireSetup(
        subject: WbbseSubject? = _rapidFireState.value.selectedSubject,
        questionCount: Int = _rapidFireState.value.questionCountTarget,
        timerMode: RapidFireTimerMode = _rapidFireState.value.timerMode
    ) {
        _rapidFireState.update {
            it.copy(
                selectedSubject = subject,
                questionCountTarget = questionCount.coerceIn(10, 30),
                timerMode = timerMode,
                result = null
            )
        }
    }

    fun startRapidFire(masterNotes: List<MasterDataEntity>) {
        val cfg = _rapidFireState.value
        val questions = buildOfflineRapidFirePool(
            subject = cfg.selectedSubject,
            count = cfg.questionCountTarget,
            masterNotes = masterNotes
        )
        if (questions.isEmpty()) return

        val initialSec = when (cfg.timerMode) {
            RapidFireTimerMode.PER_QUESTION_30S -> 30
            RapidFireTimerMode.TOTAL_15_MIN -> 15 * 60
        }
        val ggufHint = if (ggufModelManager.state.value.status == GgufModelStatus.READY_LOADED) {
            "⚡ লোকাল Llama .gguf + যাচাইকৃত WBBSE প্রশ্নব্যাংক সক্রিয় (১০০% অফলাইন)"
        } else {
            "⚡ ১০০% অফলাইন WBBSE প্রশ্নব্যাংক সক্রিয়"
        }

        _rapidFireState.update {
            it.copy(
                isRunning = true,
                questions = questions,
                currentIndex = 0,
                secondsRemaining = initialSec,
                totalElapsedSec = 0,
                currentQuestionElapsedSec = 0,
                selectedOptionForCurrent = null,
                showFeedbackForCurrent = false,
                currentStreakCombo = 0,
                answeredRecords = emptyList(),
                result = null,
                ggufTipNote = ggufHint
            )
        }
        startRapidFireTimer()
    }

    fun answerRapidFireOption(optionIndex: Int) {
        val state = _rapidFireState.value
        if (!state.isRunning || state.showFeedbackForCurrent) return
        val q = state.questions.getOrNull(state.currentIndex) ?: return

        val isCorrect = optionIndex == q.correctIndex
        val record = RapidFireAnswerRecord(
            question = q,
            selectedIndex = optionIndex,
            isCorrect = isCorrect,
            timeTakenSec = state.currentQuestionElapsedSec.coerceAtLeast(1).toFloat()
        )
        _rapidFireState.update {
            it.copy(
                selectedOptionForCurrent = optionIndex,
                showFeedbackForCurrent = true,
                currentStreakCombo = if (isCorrect) it.currentStreakCombo + 1 else 0,
                answeredRecords = it.answeredRecords + record
            )
        }
    }

    fun advanceRapidFireNext() {
        val state = _rapidFireState.value
        if (!state.isRunning) return

        val nextIdx = state.currentIndex + 1
        if (nextIdx >= state.questions.size) {
            finishRapidFire()
            return
        }

        _rapidFireState.update {
            it.copy(
                currentIndex = nextIdx,
                currentQuestionElapsedSec = 0,
                selectedOptionForCurrent = null,
                showFeedbackForCurrent = false,
                secondsRemaining = if (it.timerMode == RapidFireTimerMode.PER_QUESTION_30S) 30 else it.secondsRemaining
            )
        }
    }

    fun finishRapidFireEarly() {
        finishRapidFire()
    }

    private fun startRapidFireTimer() {
        stopRapidFireTimer()
        rapidFireTimerJob = scope.launch {
            while (true) {
                delay(1_000L)
                val state = _rapidFireState.value
                if (!state.isRunning) break
                if (state.showFeedbackForCurrent) continue

                val nextRemaining = (state.secondsRemaining - 1).coerceAtLeast(0)
                val nextTotalElapsed = state.totalElapsedSec + 1
                val nextQElapsed = state.currentQuestionElapsedSec + 1

                if (nextRemaining == 0) {
                    if (state.timerMode == RapidFireTimerMode.PER_QUESTION_30S) {
                        val q = state.questions.getOrNull(state.currentIndex)
                        if (q != null) {
                            val timeoutRecord = RapidFireAnswerRecord(
                                question = q,
                                selectedIndex = null,
                                isCorrect = false,
                                timeTakenSec = 30f
                            )
                            _rapidFireState.update {
                                it.copy(
                                    secondsRemaining = 0,
                                    totalElapsedSec = nextTotalElapsed,
                                    currentQuestionElapsedSec = nextQElapsed,
                                    showFeedbackForCurrent = true,
                                    selectedOptionForCurrent = null,
                                    currentStreakCombo = 0,
                                    answeredRecords = it.answeredRecords + timeoutRecord
                                )
                            }
                        }
                    } else {
                        // 15-minute total timer expired -> finish immediately
                        _rapidFireState.update {
                            it.copy(secondsRemaining = 0, totalElapsedSec = nextTotalElapsed)
                        }
                        finishRapidFire()
                        break
                    }
                } else {
                    _rapidFireState.update {
                        it.copy(
                            secondsRemaining = nextRemaining,
                            totalElapsedSec = nextTotalElapsed,
                            currentQuestionElapsedSec = nextQElapsed
                        )
                    }
                }
            }
        }
    }

    private fun stopRapidFireTimer() {
        rapidFireTimerJob?.cancel()
        rapidFireTimerJob = null
    }

    private fun finishRapidFire() {
        stopRapidFireTimer()
        val state = _rapidFireState.value
        val records = state.answeredRecords
        val total = records.size.coerceAtLeast(1)
        val correct = records.count { it.isCorrect }
        val timeouts = records.count { it.selectedIndex == null }
        val wrong = records.count { !it.isCorrect }
        val accuracy = ((correct * 100f) / total).roundToInt()
        val avgSec = if (records.isEmpty()) 0f else {
            (records.sumOf { it.timeTakenSec.toDouble() } / records.size).toFloat()
        }

        val rating = when {
            accuracy >= 85 && avgSec <= 15f -> "⚡ সুপার ফাস্ট টপার!"
            accuracy >= 70 && avgSec <= 22f -> "🔥 দারুণ বোর্ড স্পিড"
            accuracy >= 50 -> "👍 ভালো চেষ্টা — আরও দ্রুত করো"
            else -> "📚 ভুলের খাতা থেকে এখনই রিভিশন দাও"
        }

        val wrongRecords = records.filter { !it.isCorrect }
        val result = RapidFireResult(
            subject = state.selectedSubject,
            totalQuestions = records.size,
            correctCount = correct,
            wrongCount = wrong,
            timeoutCount = timeouts,
            accuracyPercent = accuracy,
            avgSecondsPerQuestion = avgSec,
            totalSecondsElapsed = state.totalElapsedSec,
            mistakesSavedCount = wrongRecords.size,
            speedRatingBn = rating,
            records = records
        )

        _rapidFireState.update {
            it.copy(isRunning = false, result = result)
        }

        // Persist exam score + automatically send all wrong/timed-out answers to Mistake Notebook
        scope.launch {
            val subjectForAttempt = state.selectedSubject ?: WbbseSubject.PHYSICAL_SCIENCE
            studyRepository.saveExamAttempt(
                ExamAttemptEntity(
                    subjectId = subjectForAttempt.id,
                    examType = "RAPID_FIRE",
                    questionSummary = "র্যাপিড ফায়ার (${state.selectedSubject?.bengaliName ?: "সকল বিষয়"} • ${records.size} প্রশ্ন)",
                    scoreObtained = correct.toFloat(),
                    maxMarks = records.size.coerceAtLeast(1).toFloat(),
                    accuracyPercent = accuracy,
                    feedbackSummary = "$rating | গড় সময়: ${"%.1f".format(avgSec)} সেকেন্ড/প্রশ্ন"
                )
            )

            if (wrongRecords.isNotEmpty()) {
                val mistakeEntities = wrongRecords.map { rec ->
                    val track = mapSubjectToTopperTrack(rec.question.subject)
                    TopperMistakeEntity(
                        trackId = track.id,
                        questionId = "rf_${rec.question.id}",
                        sectionCode = TopperSectionType.SECTION_A_MCQ.name,
                        marks = 1,
                        chapter = rec.question.chapterLabel,
                        questionText = rec.question.questionText,
                        optionsJoined = rec.question.options.joinToString("|||"),
                        correctOptionIndex = rec.question.correctIndex,
                        isTwisted = false,
                        simpleMeaningBn = rec.question.explanation,
                        modelAnswer = rec.question.options.getOrElse(rec.question.correctIndex) { "" },
                        presentationTip = "র্যাপিড ফায়ারে ভুল হয়েছিল (${if (rec.selectedIndex == null) "সময় শেষ" else "ভুল অপশন"}) — সঠিক তথ্য মনে রাখো।",
                        keyTermsCsv = rec.question.chapterLabel,
                        mistakeNote = if (rec.selectedIndex == null) "সময় শেষ হয়ে গিয়েছিল" else "র্যাপিড ফায়ারে ভুল উত্তর"
                    )
                }
                studyRepository.saveTopperMistakes(mistakeEntities)
            }
        }
    }

    private fun buildOfflineRapidFirePool(
        subject: WbbseSubject?,
        count: Int,
        masterNotes: List<MasterDataEntity>
    ): List<RapidFireQuestion> {
        val subjects = if (subject != null) listOf(subject) else WbbseSubject.entries
        val pool = mutableListOf<RapidFireQuestion>()
        val seenTexts = mutableSetOf<String>()

        // 1. Primary WbbseExamBank MCQs
        subjects.forEach { subj ->
            WbbseExamBank.getMcqs(subj).forEach { mcq ->
                val norm = mcq.question.trim()
                if (seenTexts.add(norm)) {
                    pool.add(
                        RapidFireQuestion(
                            id = mcq.id,
                            subject = subj,
                            chapterLabel = subj.bengaliName,
                            questionText = mcq.question,
                            options = mcq.options,
                            correctIndex = mcq.correctIndex,
                            explanation = mcq.explanation
                        )
                    )
                }
            }
        }

        // 2. Topper Twisted MCQs
        val tracks = if (subject != null) {
            TopperSubjectTrack.entries.filter { it.linkedSubject == subject }
        } else {
            TopperSubjectTrack.entries
        }
        tracks.forEach { track ->
            TopperQuestionBank.getTwistedQuestions(track)
                .filter { it.options.size >= 4 && it.correctOptionIndex in it.options.indices }
                .forEach { tq ->
                    val norm = tq.questionText.trim()
                    if (seenTexts.add(norm)) {
                        pool.add(
                            RapidFireQuestion(
                                id = tq.id,
                                subject = track.linkedSubject,
                                chapterLabel = tq.chapter,
                                questionText = tq.questionText,
                                options = tq.options,
                                correctIndex = tq.correctOptionIndex,
                                explanation = tq.simpleMeaningBn.ifBlank { tq.modelAnswer }
                            )
                        )
                    }
                }
        }

        // 3. If single subject still needs extra unique MCQs to hit 20/30, derive verified fact-recognition MCQs from MasterDataEntity
        if (pool.size < count && subject != null) {
            val subjectNotes = masterNotes.filter { it.subjectId == subject.id && it.keyFormulaOrFact.isNotBlank() }
            val allChapters = subjectNotes.map { it.chapterName }.distinct()
            if (allChapters.size >= 4) {
                subjectNotes.shuffled().forEachIndexed { idx, note ->
                    if (pool.size >= count + 6) return@forEachIndexed
                    val distractors = allChapters.filter { it != note.chapterName }.shuffled().take(3)
                    if (distractors.size == 3) {
                        val opts = (distractors + note.chapterName).shuffled()
                        val correctIdx = opts.indexOf(note.chapterName)
                        val qText = "নিম্নলিখিত গুরুত্বপূর্ণ তথ্য/সূত্রটি কোন অধ্যায়ের অন্তর্গত?\n“${note.topicTitle}”"
                        if (seenTexts.add(qText)) {
                            pool.add(
                                RapidFireQuestion(
                                    id = "derived_${subject.id}_${note.id}_$idx",
                                    subject = subject,
                                    chapterLabel = note.chapterName,
                                    questionText = qText,
                                    options = opts,
                                    correctIndex = correctIdx,
                                    explanation = "${note.chapterName}: ${note.keyFormulaOrFact}"
                                )
                            )
                        }
                    }
                }
            }
        }

        return pool.shuffled().take(count)
    }

    // ==================== 4. Hands-Free Voice Tutor & Audio Revision ====================

    fun openAudioRevisionForChapter(
        subject: WbbseSubject,
        chapterName: String,
        chapterNotes: List<MasterDataEntity>
    ) {
        val tracks = buildAudioTracksForNotes(
            subject = subject,
            notes = chapterNotes,
            includeMcqQa = true
        )
        startAudioRevisionPlaylist(
            title = chapterName,
            subtitle = "${subject.bengaliName} • হ্যান্ডস-ফ্রি অডিও পডকাস্ট",
            tracks = tracks
        )
    }

    fun openAudioRevisionForSubject(
        subject: WbbseSubject,
        subjectNotes: List<MasterDataEntity>
    ) {
        // Prioritize VVI topics first for high-impact hands-free revision
        val sorted = subjectNotes.sortedBy { if (it.importanceLevel == "VVI") 0 else 1 }
        val tracks = buildAudioTracksForNotes(
            subject = subject,
            notes = sorted.take(18),
            includeMcqQa = true
        )
        startAudioRevisionPlaylist(
            title = "${subject.bengaliName} — সম্পূর্ণ অডিও রিভিশন",
            subtitle = "VVI সারাংশ, সূত্র ও প্রশ্নোত্তর পডকাস্ট",
            tracks = tracks
        )
    }

    fun openAudioRevisionForSmartLearning(
        subject: WbbseSubject?,
        chapterName: String?,
        messages: List<SmartLearningMessage>,
        masterNotes: List<MasterDataEntity>
    ) {
        val assistantTracks = messages
            .filter { it.role != "user" && it.content.isNotBlank() }
            .mapIndexed { idx, msg ->
                AudioRevisionTrack(
                    id = 80_000L + idx,
                    subjectNameBn = subject?.bengaliName ?: "স্মার্ট লার্নিং",
                    chapterName = chapterName ?: "AI টিউটর সেশন",
                    title = "টিউটর ব্যাখ্যা ${idx + 1}",
                    categoryLabel = "স্মার্ট লার্নিং ব্যাখ্যা",
                    spokenScript = msg.content,
                    displayPreview = msg.content.take(260)
                )
            }

        val fallbackNotes = if (subject != null) {
            masterNotes.filter {
                it.subjectId == subject.id &&
                    (chapterName.isNullOrBlank() || it.chapterName == chapterName)
            }.take(10)
        } else {
            masterNotes.filter { it.importanceLevel == "VVI" }.take(10)
        }

        val combined = assistantTracks + buildAudioTracksForNotes(
            subject = subject ?: WbbseSubject.PHYSICAL_SCIENCE,
            notes = fallbackNotes,
            includeMcqQa = assistantTracks.isEmpty()
        )

        startAudioRevisionPlaylist(
            title = chapterName ?: "${subject?.bengaliName ?: "WBBSE"} স্মার্ট অডিও টিউটর",
            subtitle = "চোখ বন্ধ করে শুনে রিভিশন দাও",
            tracks = combined
        )
    }

    private fun buildAudioTracksForNotes(
        subject: WbbseSubject,
        notes: List<MasterDataEntity>,
        includeMcqQa: Boolean
    ): List<AudioRevisionTrack> {
        val tracks = mutableListOf<AudioRevisionTrack>()
        var nextId = 70_000L

        notes.forEach { note ->
            val spoken = buildString {
                append("${note.chapterName}। টপিক: ${note.topicTitle}। ")
                append(note.content)
                if (note.keyFormulaOrFact.isNotBlank()) {
                    append(" গুরুত্বপূর্ণ সূত্র ও তথ্য: ${note.keyFormulaOrFact}। ")
                }
                if (note.examTip.isNotBlank()) {
                    append(" মাধ্যমিক পরীক্ষার টিপস: ${note.examTip}")
                }
            }
            tracks.add(
                AudioRevisionTrack(
                    id = nextId++,
                    subjectNameBn = subject.bengaliName,
                    chapterName = note.chapterName,
                    title = note.topicTitle,
                    categoryLabel = if (note.importanceLevel == "VVI") "VVI অধ্যায় সারাংশ ও সূত্র" else "অধ্যায় সারাংশ",
                    spokenScript = spoken,
                    displayPreview = "${note.content}\n\n📌 ${note.keyFormulaOrFact}"
                )
            )
        }

        if (includeMcqQa) {
            WbbseExamBank.getMcqs(subject).take(4).forEachIndexed { idx, mcq ->
                val ans = mcq.options.getOrElse(mcq.correctIndex) { "" }
                val spoken = "VVI প্রশ্ন ${idx + 1}: ${mcq.question}। সঠিক উত্তর হলো: $ans। ব্যাখ্যা: ${mcq.explanation}"
                tracks.add(
                    AudioRevisionTrack(
                        id = nextId++,
                        subjectNameBn = subject.bengaliName,
                        chapterName = "${subject.bengaliName} VVI প্রশ্নোত্তর",
                        title = "VVI প্রশ্নোত্তর #${idx + 1}",
                        categoryLabel = "VVI প্রশ্নোত্তর",
                        spokenScript = spoken,
                        displayPreview = "প্রশ্ন: ${mcq.question}\n\n✅ উত্তর: $ans\n💡 ${mcq.explanation}"
                    )
                )
            }
        }
        return tracks
    }

    private fun startAudioRevisionPlaylist(
        title: String,
        subtitle: String,
        tracks: List<AudioRevisionTrack>
    ) {
        if (tracks.isEmpty()) return
        autoAdvanceDelayJob?.cancel()
        ttsManager.stop()
        _audioRevisionState.value = AudioRevisionUiState(
            isOpen = true,
            playlistTitle = title,
            playlistSubtitle = subtitle,
            tracks = tracks,
            currentIndex = 0,
            isPlaying = false,
            autoAdvance = true,
            playbackSpeed = 1.0f
        )
        playCurrentAudioTrack()
    }

    fun toggleAudioPlayPause() {
        val state = _audioRevisionState.value
        if (state.isPlaying) {
            autoAdvanceDelayJob?.cancel()
            ttsManager.stop()
            _audioRevisionState.update { it.copy(isPlaying = false) }
        } else {
            playCurrentAudioTrack()
        }
    }

    fun nextAudioTrack() {
        val state = _audioRevisionState.value
        if (state.tracks.isEmpty()) return
        val next = (state.currentIndex + 1).coerceAtMost(state.tracks.lastIndex)
        _audioRevisionState.update { it.copy(currentIndex = next) }
        playCurrentAudioTrack()
    }

    fun previousAudioTrack() {
        val state = _audioRevisionState.value
        if (state.tracks.isEmpty()) return
        val prev = (state.currentIndex - 1).coerceAtLeast(0)
        _audioRevisionState.update { it.copy(currentIndex = prev) }
        playCurrentAudioTrack()
    }

    fun selectAudioTrack(index: Int) {
        val state = _audioRevisionState.value
        if (index !in state.tracks.indices) return
        _audioRevisionState.update { it.copy(currentIndex = index) }
        playCurrentAudioTrack()
    }

    fun setAudioPlaybackSpeed(speed: Float) {
        val safeSpeed = speed.coerceIn(0.75f, 1.5f)
        val wasPlaying = _audioRevisionState.value.isPlaying
        _audioRevisionState.update { it.copy(playbackSpeed = safeSpeed) }
        if (wasPlaying) {
            playCurrentAudioTrack()
        }
    }

    fun toggleAudioAutoAdvance() {
        _audioRevisionState.update { it.copy(autoAdvance = !it.autoAdvance) }
    }

    fun closeAudioRevision() {
        autoAdvanceDelayJob?.cancel()
        ttsManager.stop()
        _audioRevisionState.update { it.copy(isOpen = false, isPlaying = false) }
    }

    private fun playCurrentAudioTrack() {
        autoAdvanceDelayJob?.cancel()
        val state = _audioRevisionState.value
        val track = state.currentTrack ?: return

        val started = ttsManager.speakRevisionTrack(
            trackMessageId = track.id,
            rawText = track.spokenScript,
            settings = getCurrentVoiceSettings(),
            speedOverride = state.playbackSpeed,
            onComplete = {
                scope.launch {
                    val latest = _audioRevisionState.value
                    if (!latest.isOpen || !latest.isPlaying) return@launch
                    if (latest.autoAdvance && latest.currentIndex < latest.tracks.lastIndex) {
                        autoAdvanceDelayJob?.cancel()
                        autoAdvanceDelayJob = scope.launch {
                            delay(800L) // gentle breath pause between podcast tracks
                            val afterPause = _audioRevisionState.value
                            if (afterPause.isOpen && afterPause.isPlaying && afterPause.currentIndex < afterPause.tracks.lastIndex) {
                                _audioRevisionState.update { it.copy(currentIndex = it.currentIndex + 1) }
                                playCurrentAudioTrack()
                            }
                        }
                    } else {
                        _audioRevisionState.update { it.copy(isPlaying = false) }
                    }
                }
            }
        )
        _audioRevisionState.update { it.copy(isPlaying = started) }
    }

    fun stopAll() {
        stopRapidFireTimer()
        autoAdvanceDelayJob?.cancel()
    }

    private fun mapSubjectToTopperTrack(subject: WbbseSubject): TopperSubjectTrack = when (subject) {
        WbbseSubject.BENGALI -> TopperSubjectTrack.BENGALI
        WbbseSubject.ENGLISH -> TopperSubjectTrack.ENGLISH
        WbbseSubject.MATHEMATICS -> TopperSubjectTrack.MATHEMATICS
        WbbseSubject.PHYSICAL_SCIENCE -> TopperSubjectTrack.PHYSICAL_SCIENCE
        WbbseSubject.LIFE_SCIENCE -> TopperSubjectTrack.LIFE_SCIENCE
        WbbseSubject.HISTORY -> TopperSubjectTrack.HISTORY
        WbbseSubject.GEOGRAPHY -> TopperSubjectTrack.GEOGRAPHY
    }

    private fun formatDate(date: Date): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(date)

    private fun getYesterdayDateStamp(): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        return formatDate(cal.time)
    }
}
