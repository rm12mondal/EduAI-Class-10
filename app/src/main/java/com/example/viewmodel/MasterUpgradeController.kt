package com.example.viewmodel

import android.content.Context
import android.net.Uri
import com.example.data.backup.StudyProgressBackupManager
import com.example.data.exam.WbbseExamBank
import com.example.data.gguf.GgufModelManager
import com.example.data.local.BrahmastramProgressEntity
import com.example.data.local.ExamAttemptEntity
import com.example.data.local.MasterDataEntity
import com.example.data.local.SpacedRevisionEntity
import com.example.data.local.StudyRepository
import com.example.data.local.TopperMistakeEntity
import com.example.data.master.BrahmastramService
import com.example.data.master.LanguageMasteryBank
import com.example.data.master.StrictWbbsePrecisionEngine
import com.example.data.pdf.EduPdfManager
import com.example.data.pdf.SavedPdfItem
import com.example.data.speech.EduTtsManager
import com.example.model.AiVivaUiState
import com.example.model.BrahmastramStep
import com.example.model.BrahmastramUiState
import com.example.model.ConfusionCleanerUiState
import com.example.model.DiagramLabUiState
import com.example.model.InferenceMode
import com.example.model.LanguageLabMode
import com.example.model.LanguageLabUiState
import com.example.model.MathSolverModule
import com.example.model.MathSolverUiState
import com.example.model.PdfExportRequest
import com.example.model.PocketBookCategory
import com.example.model.PocketBookUiState
import com.example.model.SavedPdfLibraryUiState
import com.example.model.SpacedRevisionUiState
import com.example.model.TopperSectionType
import com.example.model.TopperSubjectTrack
import com.example.model.TtsVoiceSettings
import com.example.model.VivaEvaluationTurn
import com.example.model.VivaQuestionItem
import com.example.model.WbbseSubject
import com.example.model.WritingFormatType
import com.example.model.WritingLanguage
import com.example.model.WritingStudioUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Orchestrates all 19 Master Upgrade features cleanly without bloating EduAiViewModel.
 */
class MasterUpgradeController(
    private val appContext: Context,
    private val scope: CoroutineScope,
    private val studyRepository: StudyRepository,
    private val ggufModelManager: GgufModelManager,
    private val ttsManager: EduTtsManager,
    private val getMasterNotes: () -> List<MasterDataEntity>,
    private val getInferenceMode: () -> InferenceMode,
    private val getVoiceSettings: () -> TtsVoiceSettings
) {
    private val brahmastramService = BrahmastramService(ggufModelManager)
    private var brahmastramTimerJob: Job? = null

    private val _pdfState = MutableStateFlow(SavedPdfLibraryUiState())
    val pdfState: StateFlow<SavedPdfLibraryUiState> = _pdfState.asStateFlow()

    private val _brahmastramState = MutableStateFlow(BrahmastramUiState())
    val brahmastramState: StateFlow<BrahmastramUiState> = _brahmastramState.asStateFlow()

    private val _pocketBookState = MutableStateFlow(PocketBookUiState())
    val pocketBookState: StateFlow<PocketBookUiState> = _pocketBookState.asStateFlow()

    private val _vivaState = MutableStateFlow(AiVivaUiState())
    val vivaState: StateFlow<AiVivaUiState> = _vivaState.asStateFlow()

    private val _confusionState = MutableStateFlow(ConfusionCleanerUiState())
    val confusionState: StateFlow<ConfusionCleanerUiState> = _confusionState.asStateFlow()

    private val _spacedRevisionState = MutableStateFlow(SpacedRevisionUiState())
    val spacedRevisionState: StateFlow<SpacedRevisionUiState> = _spacedRevisionState.asStateFlow()

    private val _diagramLabState = MutableStateFlow(DiagramLabUiState())
    val diagramLabState: StateFlow<DiagramLabUiState> = _diagramLabState.asStateFlow()

    private val _writingStudioState = MutableStateFlow(WritingStudioUiState())
    val writingStudioState: StateFlow<WritingStudioUiState> = _writingStudioState.asStateFlow()

    private val _languageLabState = MutableStateFlow(LanguageLabUiState())
    val languageLabState: StateFlow<LanguageLabUiState> = _languageLabState.asStateFlow()

    private val _mathSolverState = MutableStateFlow(MathSolverUiState())
    val mathSolverState: StateFlow<MathSolverUiState> = _mathSolverState.asStateFlow()

    private val _backupStatusMessage = MutableStateFlow<String?>(null)
    val backupStatusMessage: StateFlow<String?> = _backupStatusMessage.asStateFlow()

    init {
        scope.launch {
            studyRepository.allBrahmastramProgress.collect { list ->
                _brahmastramState.update { st ->
                    st.copy(progressBySubject = list.associateBy { it.subjectId })
                }
            }
        }
        scope.launch {
            studyRepository.allSpacedRevisions.collect { items ->
                val now = System.currentTimeMillis()
                val due = items.filter { it.masteryStage < 3 && it.nextReviewAtMs <= now }
                val upcoming = items.count { it.masteryStage < 3 && it.nextReviewAtMs > now }
                _spacedRevisionState.value = SpacedRevisionUiState(
                    dueNowItems = due,
                    upcomingCount = upcoming,
                    totalTrackedCount = items.size
                )
            }
        }
        refreshSavedPdfs()
    }

    fun ensureSpacedRevisionSeeded(masterNotes: List<MasterDataEntity>) {
        scope.launch {
            val existing = studyRepository.getAllRevisionsSnapshot()
            if (existing.isEmpty() && masterNotes.isNotEmpty()) {
                val starters = listOf(
                    WbbseSubject.PHYSICAL_SCIENCE,
                    WbbseSubject.LIFE_SCIENCE,
                    WbbseSubject.HISTORY
                ).mapNotNull { subj ->
                    masterNotes.firstOrNull { it.subjectId == subj.id && it.importanceLevel == "VVI" }
                }
                starters.forEach { note ->
                    studyRepository.scheduleOrTouchSpacedRevision(
                        subjectId = note.subjectId,
                        chapterName = note.chapterName,
                        topicTitle = note.topicTitle,
                        keySummary = note.keyFormulaOrFact.ifBlank { note.content.take(120) },
                        sourceType = "STUDY",
                        makeImmediatelyDue = true
                    )
                }
            }
        }
    }

    fun markSpacedRevisionCompleted(item: SpacedRevisionEntity) {
        scope.launch {
            studyRepository.completeSpacedRevisionReview(item)
        }
    }

    fun recordStudiedTopicForRevision(
        subject: WbbseSubject,
        chapterName: String,
        topicTitle: String,
        keySummary: String,
        isMistake: Boolean = false
    ) {
        scope.launch {
            studyRepository.scheduleOrTouchSpacedRevision(
                subjectId = subject.id,
                chapterName = chapterName,
                topicTitle = topicTitle,
                keySummary = keySummary,
                sourceType = if (isMistake) "MISTAKE" else "STUDY",
                makeImmediatelyDue = isMistake
            )
        }
    }

    // ==================== A. Universal Offline Custom-Named PDF Export & Library ====================

    fun requestPdfExport(
        suggestedName: String,
        title: String,
        subtitle: String,
        bodyContent: String
    ) {
        _pdfState.update {
            it.copy(
                pendingExportRequest = PdfExportRequest(
                    suggestedFileName = EduPdfManager.sanitizeFileName(suggestedName),
                    title = title,
                    subtitle = subtitle,
                    bodyContent = bodyContent
                ),
                lastExportedPdf = null,
                statusMessage = null
            )
        }
    }

    fun dismissPdfExportDialog() {
        _pdfState.update { it.copy(pendingExportRequest = null, lastExportedPdf = null) }
    }

    fun confirmGeneratePdf(customFileName: String) {
        val req = _pdfState.value.pendingExportRequest ?: return
        scope.launch {
            _pdfState.update { it.copy(isGeneratingPdf = true, statusMessage = null) }
            val result = EduPdfManager.generateAndSavePdf(
                context = appContext,
                customName = customFileName,
                title = req.title,
                subtitle = req.subtitle,
                bodyContent = req.bodyContent
            )
            result.onSuccess { gen ->
                val list = EduPdfManager.listSavedPdfs(appContext)
                _pdfState.update {
                    it.copy(
                        isGeneratingPdf = false,
                        items = list,
                        lastExportedPdf = gen.item,
                        statusMessage = "✅ PDF সেভ হয়েছে: ${gen.publicSavedPath}"
                    )
                }
            }.onFailure { err ->
                _pdfState.update {
                    it.copy(
                        isGeneratingPdf = false,
                        statusMessage = "PDF তৈরিতে সমস্যা হয়েছে: ${err.localizedMessage ?: "আবার চেষ্টা করো"}"
                    )
                }
            }
        }
    }

    fun openSavedPdfLibrary() {
        _pdfState.update { it.copy(isOpen = true) }
        refreshSavedPdfs()
    }

    fun closeSavedPdfLibrary() {
        _pdfState.update { it.copy(isOpen = false) }
    }

    fun refreshSavedPdfs() {
        scope.launch {
            _pdfState.update { it.copy(isLoading = true) }
            val list = EduPdfManager.listSavedPdfs(appContext)
            _pdfState.update { it.copy(items = list, isLoading = false) }
        }
    }

    fun renameSavedPdf(item: SavedPdfItem, newName: String) {
        scope.launch {
            EduPdfManager.renameSavedPdf(appContext, item, newName).onSuccess {
                refreshSavedPdfs()
                _pdfState.update { it.copy(statusMessage = "✅ PDF নাম পরিবর্তন হয়েছে।") }
            }
        }
    }

    fun deleteSavedPdf(item: SavedPdfItem) {
        scope.launch {
            EduPdfManager.deleteSavedPdf(item)
            refreshSavedPdfs()
        }
    }

    fun clearPdfStatusMessage() {
        _pdfState.update { it.copy(statusMessage = null) }
    }

    // ==================== B. Offline Study Progress Backup & Restore ====================

    fun exportProgressBackup(uri: Uri) {
        scope.launch {
            StudyProgressBackupManager.exportBackupToUri(appContext, uri, studyRepository)
                .onSuccess { s ->
                    _backupStatusMessage.value =
                        "✅ EduAI_Backup.json সেভ হয়েছে! (${s.examAttemptsCount} পরীক্ষা, ${s.mistakesCount} ভুল, ${s.brahmastramCount} ব্রহ্মাস্ত্রম, ${s.streakDays} দিন স্ট্রিক)"
                }
                .onFailure { e ->
                    _backupStatusMessage.value = "ব্যাকআপ এক্সপোর্ট ব্যর্থ: ${e.localizedMessage ?: "ত্রুটি"}"
                }
        }
    }

    fun importProgressBackup(uri: Uri) {
        scope.launch {
            StudyProgressBackupManager.importBackupFromUri(appContext, uri, studyRepository)
                .onSuccess { s ->
                    _backupStatusMessage.value =
                        "✅ ব্যাকআপ রিস্টোর সম্পন্ন! (${s.examAttemptsCount} পরীক্ষা, ${s.mistakesCount} ভুল, ${s.brahmastramCount} ব্রহ্মাস্ত্রম, ${s.streakDays} দিন স্ট্রিক)"
                }
                .onFailure { e ->
                    _backupStatusMessage.value = "ব্যাকআপ রিস্টোর ব্যর্থ: ${e.localizedMessage ?: "সঠিক JSON ফাইল দাও"}"
                }
        }
    }

    fun clearBackupStatus() {
        _backupStatusMessage.value = null
    }

    // ==================== 4. Brahmastram (3-Step Sure-Shot Mastery) ====================

    fun openBrahmastram(subject: WbbseSubject = WbbseSubject.PHYSICAL_SCIENCE) {
        _brahmastramState.update {
            it.copy(
                isOpen = true,
                selectedSubject = subject,
                currentStep = BrahmastramStep.NOTES,
                latestReport = null
            )
        }
        generateBrahmastramNotes(subject)
    }

    fun closeBrahmastram() {
        stopBrahmastramTimer()
        ttsManager.stop()
        _brahmastramState.update { it.copy(isOpen = false, isTimerRunning = false) }
    }

    fun selectBrahmastramSubject(subject: WbbseSubject) {
        stopBrahmastramTimer()
        _brahmastramState.update {
            it.copy(
                selectedSubject = subject,
                currentStep = BrahmastramStep.NOTES,
                latestReport = null
            )
        }
        generateBrahmastramNotes(subject)
    }

    fun generateBrahmastramNotes(subject: WbbseSubject = _brahmastramState.value.selectedSubject) {
        scope.launch {
            _brahmastramState.update {
                it.copy(isGenerating = true, currentStep = BrahmastramStep.NOTES, latestReport = null)
            }
            val savedProg = studyRepository.getBrahmastramProgress(subject.id)
            val completedIds = savedProg?.completedNoteIdsCsv
                ?.split(",")
                ?.filter { it.isNotBlank() }
                ?.toSet()
                ?: emptySet()

            val (notes, badge) = brahmastramService.generateSureShotNotes(
                subject = subject,
                inferenceMode = getInferenceMode(),
                allMasterNotes = getMasterNotes(),
                completedIds = completedIds
            )
            _brahmastramState.update {
                it.copy(
                    isGenerating = false,
                    notes = notes,
                    sourceBadge = badge
                )
            }
        }
    }

    fun toggleBrahmastramNoteCompleted(noteId: String) {
        val current = _brahmastramState.value
        val updatedNotes = current.notes.map { n ->
            if (n.id == noteId) n.copy(isCompleted = !n.isCompleted) else n
        }
        _brahmastramState.update { it.copy(notes = updatedNotes) }

        val targetNote = updatedNotes.firstOrNull { it.id == noteId }
        scope.launch {
            val completedCsv = updatedNotes.filter { it.isCompleted }.joinToString(",") { it.id }
            val existing = studyRepository.getBrahmastramProgress(current.selectedSubject.id)
            studyRepository.upsertBrahmastramProgress(
                (existing ?: BrahmastramProgressEntity(subjectId = current.selectedSubject.id)).copy(
                    completedNoteIdsCsv = completedCsv,
                    totalNotesCount = updatedNotes.size,
                    lastUpdatedMs = System.currentTimeMillis()
                )
            )
            if (targetNote != null && targetNote.isCompleted) {
                studyRepository.scheduleOrTouchSpacedRevision(
                    subjectId = current.selectedSubject.id,
                    chapterName = targetNote.chapter,
                    topicTitle = targetNote.title,
                    keySummary = targetNote.keyFormulaOrFact.ifBlank { targetNote.sureShotNoteBn.take(120) },
                    sourceType = "STUDY",
                    makeImmediatelyDue = false
                )
            }
        }
    }

    fun startBrahmastramExam() {
        val current = _brahmastramState.value
        if (current.notes.isEmpty()) return
        val questions = brahmastramService.buildExamPaperStrictlyFromNotes(current.notes)
        _brahmastramState.update {
            it.copy(
                currentStep = BrahmastramStep.EXAM,
                examQuestions = questions,
                selectedOptions = emptyMap(),
                typedAnswers = emptyMap(),
                remainingSeconds = 15 * 60,
                isTimerRunning = true,
                latestReport = null
            )
        }
        startBrahmastramTimer()
    }

    fun selectBrahmastramMcqOption(questionId: String, optionIdx: Int) {
        _brahmastramState.update {
            it.copy(selectedOptions = it.selectedOptions + (questionId to optionIdx))
        }
    }

    fun updateBrahmastramTypedAnswer(questionId: String, text: String) {
        _brahmastramState.update {
            it.copy(typedAnswers = it.typedAnswers + (questionId to text))
        }
    }

    fun submitBrahmastramExam() {
        stopBrahmastramTimer()
        val current = _brahmastramState.value
        val report = brahmastramService.gradeBrahmastramExam(
            subject = current.selectedSubject,
            questions = current.examQuestions,
            selectedOptions = current.selectedOptions,
            typedAnswers = current.typedAnswers
        )
        _brahmastramState.update {
            it.copy(
                currentStep = BrahmastramStep.RESULT,
                isTimerRunning = false,
                latestReport = report
            )
        }

        scope.launch {
            studyRepository.saveExamAttempt(
                ExamAttemptEntity(
                    subjectId = report.subject.id,
                    examType = "BRAHMASTRAM",
                    questionSummary = "ব্রহ্মাস্ত্রম প্রশ্নপত্র — ${report.subject.bengaliName}",
                    scoreObtained = report.obtainedMarks,
                    maxMarks = report.maxMarks.coerceAtLeast(1f),
                    accuracyPercent = report.accuracyPercent,
                    feedbackSummary = report.redPenSummaryBn
                )
            )
            val existing = studyRepository.getBrahmastramProgress(report.subject.id)
            studyRepository.upsertBrahmastramProgress(
                (existing ?: BrahmastramProgressEntity(subjectId = report.subject.id)).copy(
                    lastExamScore = report.obtainedMarks,
                    lastExamMaxMarks = report.maxMarks,
                    examsTakenCount = (existing?.examsTakenCount ?: 0) + 1,
                    lastUpdatedMs = System.currentTimeMillis()
                )
            )
            // Send wrong Brahmastram items to Spaced Repetition + Mistake Notebook
            val wrongItems = report.gradedItems.filter { it.obtainedMarks < it.maxMarks * 0.7f }
            val track = TopperSubjectTrack.entries.firstOrNull { it.linkedSubject == report.subject }
                ?: TopperSubjectTrack.PHYSICAL_SCIENCE
            val mistakes = wrongItems.map { item ->
                studyRepository.scheduleOrTouchSpacedRevision(
                    subjectId = report.subject.id,
                    chapterName = item.question.chapter,
                    topicTitle = item.question.questionText.take(65),
                    keySummary = item.question.modelAnswer.take(140),
                    sourceType = "MISTAKE",
                    makeImmediatelyDue = true
                )
                TopperMistakeEntity(
                    trackId = track.id,
                    questionId = item.question.id,
                    sectionCode = if (item.question.options.isNotEmpty()) {
                        TopperSectionType.SECTION_A_MCQ.name
                    } else {
                        TopperSectionType.SECTION_C_SAQ_2M.name
                    },
                    marks = item.question.marks,
                    chapter = item.question.chapter,
                    questionText = item.question.questionText,
                    optionsJoined = item.question.options.joinToString("|||"),
                    correctOptionIndex = item.question.correctOptionIndex,
                    isTwisted = item.question.trapKeywords.isNotEmpty(),
                    simpleMeaningBn = item.redPenDeductions.joinToString(" "),
                    modelAnswer = item.question.modelAnswer,
                    presentationTip = "ব্রহ্মাস্ত্রম পরীক্ষায় ভুল হয়েছিল — লাল-কালির মন্তব্য দেখে নাও।",
                    keyTermsCsv = item.question.chapter,
                    mistakeNote = "ব্রহ্মাস্ত্রম প্রশ্নপত্রে নম্বর কাটা গেছে"
                )
            }
            if (mistakes.isNotEmpty()) {
                studyRepository.saveTopperMistakes(mistakes)
            }
        }
    }

    fun returnToBrahmastramNotes() {
        stopBrahmastramTimer()
        _brahmastramState.update { it.copy(currentStep = BrahmastramStep.NOTES, isTimerRunning = false) }
    }

    private fun startBrahmastramTimer() {
        stopBrahmastramTimer()
        brahmastramTimerJob = scope.launch {
            while (true) {
                delay(1_000L)
                val st = _brahmastramState.value
                if (!st.isOpen || !st.isTimerRunning || st.currentStep != BrahmastramStep.EXAM) break
                val next = (st.remainingSeconds - 1).coerceAtLeast(0)
                _brahmastramState.update { it.copy(remainingSeconds = next) }
                if (next == 0) {
                    submitBrahmastramExam()
                    break
                }
            }
        }
    }

    private fun stopBrahmastramTimer() {
        brahmastramTimerJob?.cancel()
        brahmastramTimerJob = null
    }

    // ==================== 5. Pocket Book ====================

    fun openPocketBook(category: PocketBookCategory = PocketBookCategory.MATH_FORMULAS) {
        _pocketBookState.value = PocketBookUiState(isOpen = true, selectedCategory = category)
    }

    fun selectPocketBookCategory(category: PocketBookCategory) {
        _pocketBookState.update { it.copy(selectedCategory = category) }
    }

    fun updatePocketBookSearch(q: String) {
        _pocketBookState.update { it.copy(searchQuery = q) }
    }

    fun closePocketBook() {
        _pocketBookState.update { it.copy(isOpen = false) }
    }

    // ==================== 6. AI Viva Oral Tutor ("পড়া ধরুন") ====================

    fun openAiViva(subject: WbbseSubject, chapterName: String) {
        val allNotes = getMasterNotes()
        val chapterNotes = allNotes.filter {
            it.subjectId == subject.id && (chapterName.isBlank() || it.chapterName == chapterName)
        }.ifEmpty { allNotes.filter { it.subjectId == subject.id }.take(6) }

        val questions = mutableListOf<VivaQuestionItem>()
        chapterNotes.take(4).forEachIndexed { idx, note ->
            val marks = if (idx % 2 == 0) 1 else 2
            val qText = if (marks == 1) {
                "১ নম্বরের মৌখিক প্রশ্ন (${note.chapterName}): “${note.topicTitle}” সম্পর্কিত মূল সূত্র বা গুরুত্বপূর্ণ তথ্যটি কী?"
            } else {
                "২ নম্বরের মৌখিক প্রশ্ন (${note.chapterName}): “${note.topicTitle}” সংক্ষেপে ব্যাখ্যা করো এবং একটি উদাহরণ দাও।"
            }
            val expected = "${note.keyFormulaOrFact}। ${note.content.take(220)}"
            val keys = "${note.topicTitle} ${note.keyFormulaOrFact} ${note.keywords}"
                .split(Regex("""[\s,।:;()\-]+"""))
                .filter { it.length >= 3 }
                .distinct()
                .take(8)
            questions.add(
                VivaQuestionItem(
                    id = "viva_${note.id}_$idx",
                    chapter = note.chapterName,
                    marks = marks,
                    questionText = qText,
                    expectedAnswer = expected,
                    keyTerms = keys
                )
            )
        }

        // Append 2 crisp 1-mark questions from Exam Bank for lively viva pacing
        WbbseExamBank.getMcqs(subject).take(2).forEachIndexed { idx, mcq ->
            val correctAns = mcq.options.getOrElse(mcq.correctIndex) { "" }
            questions.add(
                VivaQuestionItem(
                    id = "viva_mcq_${mcq.id}_$idx",
                    chapter = chapterName.ifBlank { subject.bengaliName },
                    marks = 1,
                    questionText = "১ নম্বরের দ্রুত ভাইভা: ${mcq.question}",
                    expectedAnswer = "$correctAns (${mcq.explanation})",
                    keyTerms = correctAns.split(Regex("""[\s,।]+""")).filter { it.length >= 2 }
                )
            )
        }

        val finalList = questions.take(6)
        _vivaState.value = AiVivaUiState(
            isOpen = true,
            subject = subject,
            chapterName = chapterName.ifBlank { subject.bengaliName },
            questions = finalList,
            currentIndex = 0,
            history = emptyList(),
            isEvaluating = false,
            isCompleted = false
        )

        finalList.firstOrNull()?.let { speakVivaQuestion(it) }
    }

    fun speakCurrentVivaQuestion() {
        _vivaState.value.currentQuestion?.let { speakVivaQuestion(it) }
    }

    private fun speakVivaQuestion(q: VivaQuestionItem) {
        ttsManager.toggleSpeak(
            messageId = 95_000L + q.id.hashCode(),
            rawText = q.questionText,
            settings = getVoiceSettings()
        )
    }

    fun submitVivaAnswer(studentAnswer: String) {
        val state = _vivaState.value
        val q = state.currentQuestion ?: return
        val clean = studentAnswer.trim()
        if (clean.isBlank() || state.isEvaluating) return

        ttsManager.stop()
        val matched = q.keyTerms.count { term -> clean.contains(term, ignoreCase = true) }
        val ratio = when {
            q.keyTerms.isEmpty() -> if (clean.length > 12) 0.85f else 0.5f
            else -> ((matched.toFloat() / q.keyTerms.size.coerceAtLeast(1)) * 1.6f).coerceIn(0f, 1f)
        }
        val obtained = (q.marks * ratio * 2f).roundToInt() / 2f
        val unitAudit = StrictWbbsePrecisionEngine.analyzeMathPhysicsStepsAndUnits(state.subject, q.questionText, clean)
        val langAudit = StrictWbbsePrecisionEngine.analyzeBengaliEnglishLanguage(state.subject, clean)
        val notes = buildList {
            unitAudit?.let { add(it.summaryBn) }
            langAudit?.let { add(it.summaryBn) }
        }

        val feedback = when {
            obtained >= q.marks * 0.8f -> "🌟 শাবাশ! একদম সঠিক মৌখিক উত্তর ($obtained/${q.marks})। বোর্ড উত্তর: ${q.expectedAnswer}"
            obtained >= q.marks * 0.4f -> "👍 আংশিক সঠিক ($obtained/${q.marks})। পূর্ণ নম্বরের জন্য মূল কথা: ${q.expectedAnswer}"
            else -> "📘 আরেকবার মনে করে নাও ($obtained/${q.marks})। সঠিক উত্তর: ${q.expectedAnswer}"
        }

        val turn = VivaEvaluationTurn(
            question = q,
            studentAnswer = clean,
            marksObtained = obtained,
            maxMarks = q.marks.toFloat(),
            feedbackBn = feedback,
            unitOrGrammarNotes = notes
        )
        val nextIndex = state.currentIndex + 1
        val finished = nextIndex >= state.questions.size

        _vivaState.update {
            it.copy(
                history = it.history + turn,
                currentIndex = if (finished) it.currentIndex else nextIndex,
                isCompleted = finished
            )
        }

        // Speak short spoken verdict + next question smoothly
        val spokenFeedback = if (finished) {
            "ভাইভা শেষ হয়েছে! তোমার উত্তর মূল্যায়ন করা হলো।"
        } else {
            val nextQ = state.questions[nextIndex]
            "${if (obtained >= q.marks * 0.8f) "শাবাশ, দারুণ উত্তর!" else "সঠিক উত্তরটি দেখে নাও।"} পরবর্তী প্রশ্ন: ${nextQ.questionText}"
        }
        ttsManager.toggleSpeak(
            messageId = 96_000L + nextIndex,
            rawText = spokenFeedback,
            settings = getVoiceSettings()
        )

        if (finished) {
            scope.launch {
                val allTurns = _vivaState.value.history
                val obt = allTurns.sumOf { it.marksObtained.toDouble() }.toFloat()
                val max = allTurns.sumOf { it.maxMarks.toDouble() }.toFloat().coerceAtLeast(1f)
                val acc = ((obt / max) * 100f).roundToInt().coerceIn(0, 100)
                studyRepository.saveExamAttempt(
                    ExamAttemptEntity(
                        subjectId = state.subject.id,
                        examType = "AI_VIVA",
                        questionSummary = "পড়া ধরুন AI Viva — ${state.chapterName}",
                        scoreObtained = obt,
                        maxMarks = max,
                        accuracyPercent = acc,
                        feedbackSummary = "মৌখিক ভাইভা স্কোর: $obt/$max ($acc%)"
                    )
                )
            }
        }
    }

    fun closeAiViva() {
        ttsManager.stop()
        _vivaState.update { it.copy(isOpen = false) }
    }

    // ==================== 8. Confusion Cleaner ====================

    fun openConfusionCleaner(subject: WbbseSubject) {
        _confusionState.value = ConfusionCleanerUiState(isOpen = true, subject = subject)
    }

    fun closeConfusionCleaner() {
        _confusionState.update { it.copy(isOpen = false) }
    }

    // ==================== 15. Interactive Science Diagram Labelling ====================

    fun openDiagramLab(subject: WbbseSubject) {
        val validSubject = if (subject == WbbseSubject.PHYSICAL_SCIENCE) {
            WbbseSubject.PHYSICAL_SCIENCE
        } else {
            WbbseSubject.LIFE_SCIENCE
        }
        _diagramLabState.value = DiagramLabUiState(
            isOpen = true,
            subject = validSubject,
            selectedDiagramIndex = 0
        )
    }

    fun switchDiagramLabSubject(subject: WbbseSubject) {
        _diagramLabState.update { it.copy(subject = subject, selectedDiagramIndex = 0) }
    }

    fun selectDiagramIndex(index: Int) {
        _diagramLabState.update { it.copy(selectedDiagramIndex = index) }
    }

    fun closeDiagramLab() {
        _diagramLabState.update { it.copy(isOpen = false) }
    }

    // ==================== 17. WBBSE Writing Studio ====================

    fun openWritingStudio(language: WritingLanguage) {
        val defaultFmt = if (language == WritingLanguage.ENGLISH) {
            WritingFormatType.EN_NOTICE
        } else {
            WritingFormatType.BN_PRABANDHA
        }
        _writingStudioState.value = WritingStudioUiState(
            isOpen = true,
            language = language,
            selectedFormat = defaultFmt,
            topicInput = defaultFmt.sampleTopic,
            generatedOutput = "",
            sourceBadge = ""
        )
    }

    fun selectWritingFormat(format: WritingFormatType) {
        _writingStudioState.update {
            it.copy(
                language = format.language,
                selectedFormat = format,
                topicInput = format.sampleTopic,
                generatedOutput = ""
            )
        }
    }

    fun updateWritingTopic(topic: String) {
        _writingStudioState.update { it.copy(topicInput = topic) }
    }

    fun generateWritingOutput() {
        val st = _writingStudioState.value
        scope.launch {
            _writingStudioState.update { it.copy(isGenerating = true) }
            val (output, badge) = LanguageMasteryBank.generateWriting(
                format = st.selectedFormat,
                topic = st.topicInput,
                inferenceMode = getInferenceMode(),
                ggufModelManager = ggufModelManager
            )
            _writingStudioState.update {
                it.copy(
                    isGenerating = false,
                    generatedOutput = output,
                    sourceBadge = badge
                )
            }
        }
    }

    fun closeWritingStudio() {
        _writingStudioState.update { it.copy(isOpen = false) }
    }

    // ==================== 18. English & Bengali Language Mastery Labs ====================

    fun openLanguageLab(mode: LanguageLabMode) {
        _languageLabState.value = LanguageLabUiState(isOpen = true, mode = mode)
    }

    fun updateBengaliAnalyzerInput(text: String) {
        _languageLabState.update { it.copy(analyzerInput = text) }
    }

    fun runBengaliGrammarAnalyzer() {
        val input = _languageLabState.value.analyzerInput
        scope.launch {
            _languageLabState.update { it.copy(isAnalyzing = true) }
            val result = LanguageMasteryBank.analyzeBengaliGrammar(input, getInferenceMode())
            _languageLabState.update { it.copy(isAnalyzing = false, analyzerOutput = result) }
        }
    }

    fun updateCustomUnseenInput(text: String) {
        _languageLabState.update { it.copy(customUnseenInput = text) }
    }

    fun solveCustomUnseenPassage() {
        val passage = _languageLabState.value.customUnseenInput
        scope.launch {
            _languageLabState.update { it.copy(isSolvingUnseen = true) }
            val result = LanguageMasteryBank.solveCustomUnseenPassage(passage, getInferenceMode())
            _languageLabState.update { it.copy(isSolvingUnseen = false, customUnseenOutput = result) }
        }
    }

    fun closeLanguageLab() {
        _languageLabState.update { it.copy(isOpen = false) }
    }

    // ==================== 19. Offline Math & Mensuration Solver ====================

    fun openMathSolver(module: MathSolverModule = MathSolverModule.SPHERE) {
        _mathSolverState.value = MathSolverUiState(isOpen = true, module = module)
    }

    fun selectMathSolverModule(module: MathSolverModule) {
        _mathSolverState.update { it.copy(module = module) }
    }

    fun closeMathSolver() {
        _mathSolverState.update { it.copy(isOpen = false) }
    }

    fun stopAll() {
        stopBrahmastramTimer()
    }
}
