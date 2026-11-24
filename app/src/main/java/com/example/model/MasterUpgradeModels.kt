package com.example.model

import com.example.data.local.BrahmastramProgressEntity
import com.example.data.local.SpacedRevisionEntity
import com.example.data.pdf.SavedPdfItem

// ==================== A. Universal PDF Export & Library ====================

data class PdfExportRequest(
    val suggestedFileName: String,
    val title: String,
    val subtitle: String,
    val bodyContent: String
)

data class SavedPdfLibraryUiState(
    val isOpen: Boolean = false,
    val items: List<SavedPdfItem> = emptyList(),
    val isLoading: Boolean = false,
    val lastExportedPdf: SavedPdfItem? = null,
    val pendingExportRequest: PdfExportRequest? = null,
    val isGeneratingPdf: Boolean = false,
    val statusMessage: String? = null
)

// ==================== 4. Brahmastram (3-Step Sure-Shot Mastery) ====================

enum class BrahmastramStep(val titleBn: String) {
    NOTES("১. শিওর-শট নোটস"),
    EXAM("২. ব্রহ্মাস্ত্রম প্রশ্নপত্র"),
    RESULT("৩. লাল-কালি ফলাফল")
}

data class BrahmastramStudyNote(
    val id: String,
    val chapter: String,
    val title: String,
    val marksWeight: Int,
    val pyqBadge: String,
    val sureShotNoteBn: String,
    val topperWritingStructureBn: String,
    val keyFormulaOrFact: String,
    val examQuestionPrompt: String,
    val mcqOptions: List<String>,
    val correctOptionIndex: Int,
    val isCompleted: Boolean = false
)

data class BrahmastramExamQuestion(
    val id: String,
    val sourceNoteId: String,
    val chapter: String,
    val questionText: String,
    val marks: Int,
    val options: List<String>, // empty for 2/3-mark written
    val correctOptionIndex: Int,
    val modelAnswer: String,
    val trapKeywords: List<String>
)

data class BrahmastramGradedItem(
    val question: BrahmastramExamQuestion,
    val selectedOption: Int?,
    val typedAnswer: String,
    val obtainedMarks: Float,
    val maxMarks: Float,
    val redPenDeductions: List<String>,
    val unitOrGrammarAudit: List<String>
)

data class BrahmastramExamReport(
    val subject: WbbseSubject,
    val obtainedMarks: Float,
    val maxMarks: Float,
    val accuracyPercent: Int,
    val gradedItems: List<BrahmastramGradedItem>,
    val trapAlertsCount: Int,
    val redPenSummaryBn: String
)

data class BrahmastramUiState(
    val isOpen: Boolean = false,
    val selectedSubject: WbbseSubject = WbbseSubject.PHYSICAL_SCIENCE,
    val currentStep: BrahmastramStep = BrahmastramStep.NOTES,
    val isGenerating: Boolean = false,
    val sourceBadge: String = "",
    val notes: List<BrahmastramStudyNote> = emptyList(),
    val examQuestions: List<BrahmastramExamQuestion> = emptyList(),
    val selectedOptions: Map<String, Int> = emptyMap(),
    val typedAnswers: Map<String, String> = emptyMap(),
    val remainingSeconds: Int = 15 * 60,
    val isTimerRunning: Boolean = false,
    val latestReport: BrahmastramExamReport? = null,
    val progressBySubject: Map<String, BrahmastramProgressEntity> = emptyMap()
)

// ==================== 5. Pocket Book ====================

enum class PocketBookCategory(val titleBn: String) {
    MATH_FORMULAS("গণিত সূত্র ও উপপাদ্য"),
    PHYSICS_REACTIONS("ভৌত বিজ্ঞান ও বিক্রিয়া"),
    LIFE_SCIENCE_KEYS("জীবন বিজ্ঞান মূল তথ্য"),
    HISTORY_DATES("ইতিহাস সাল ও সন্ধি"),
    LITERATURE_AUTHORS("বাংলা ও ইংরেজি লেখক-উৎস")
}

data class PocketBookEntry(
    val id: String,
    val category: PocketBookCategory,
    val subject: WbbseSubject,
    val title: String,
    val formulaOrFact: String,
    val examTip: String
)

data class PocketBookUiState(
    val isOpen: Boolean = false,
    val selectedCategory: PocketBookCategory = PocketBookCategory.MATH_FORMULAS,
    val searchQuery: String = ""
)

// ==================== 6. AI Viva Oral Tutor ("পড়া ধরুন") ====================

data class VivaQuestionItem(
    val id: String,
    val chapter: String,
    val marks: Int, // 1 or 2
    val questionText: String,
    val expectedAnswer: String,
    val keyTerms: List<String>
)

data class VivaEvaluationTurn(
    val question: VivaQuestionItem,
    val studentAnswer: String,
    val marksObtained: Float,
    val maxMarks: Float,
    val feedbackBn: String,
    val unitOrGrammarNotes: List<String>
)

data class AiVivaUiState(
    val isOpen: Boolean = false,
    val subject: WbbseSubject = WbbseSubject.PHYSICAL_SCIENCE,
    val chapterName: String = "",
    val questions: List<VivaQuestionItem> = emptyList(),
    val currentIndex: Int = 0,
    val history: List<VivaEvaluationTurn> = emptyList(),
    val isEvaluating: Boolean = false,
    val isCompleted: Boolean = false
) {
    val currentQuestion: VivaQuestionItem? get() = questions.getOrNull(currentIndex)
    val totalObtained: Float get() = history.sumOf { it.marksObtained.toDouble() }.toFloat()
    val totalPossible: Float get() = history.sumOf { it.maxMarks.toDouble() }.toFloat()
}

// ==================== 7. Score Predictor (/700) ====================

data class SubjectPredictedScore(
    val subject: WbbseSubject,
    val predictedOutOf100: Int,
    val attemptsCount: Int,
    val statusBn: String
)

data class ScorePredictorSummary(
    val predictedTotalOutOf700: Int,
    val percentage: Int,
    val gradeLabelBn: String,
    val subjectScores: List<SubjectPredictedScore>,
    val twoWeakestSubjects: List<SubjectPredictedScore>,
    val hasRealExamData: Boolean
)

// ==================== 8. Confusion Cleaner ====================

data class ConfusionComparisonRow(
    val basis: String,
    val leftValue: String,
    val rightValue: String
)

data class ConfusionPairItem(
    val id: String,
    val subject: WbbseSubject,
    val leftTitle: String,
    val rightTitle: String,
    val rows: List<ConfusionComparisonRow>,
    val memoryTrickBn: String
)

data class ConfusionCleanerUiState(
    val isOpen: Boolean = false,
    val subject: WbbseSubject = WbbseSubject.PHYSICAL_SCIENCE
)

// ==================== 9. Spaced Repetition Capsule ====================

data class SpacedRevisionUiState(
    val dueNowItems: List<SpacedRevisionEntity> = emptyList(),
    val upcomingCount: Int = 0,
    val totalTrackedCount: Int = 0
)

// ==================== 15. Interactive Science Diagram Labelling ====================

enum class ScienceDiagramType {
    NEURON,
    REFLEX_ARC,
    CHROMOSOME,
    HUMAN_EYE,
    PRISM_REFRACTION,
    CONVEX_LENS
}

data class DiagramCalloutPoint(
    val id: String,
    val numberLabel: Int,
    val labelBn: String,
    val labelEn: String,
    val anchorX: Float, // 0f..1f on canvas
    val anchorY: Float, // 0f..1f on canvas
    val boxX: Float,    // 0f..1f callout badge position
    val boxY: Float,
    val hintBn: String
)

data class ScienceDiagramSpec(
    val id: String,
    val type: ScienceDiagramType,
    val subject: WbbseSubject,
    val titleBn: String,
    val titleEn: String,
    val boardMarksNote: String,
    val callouts: List<DiagramCalloutPoint>
)

data class DiagramLabUiState(
    val isOpen: Boolean = false,
    val subject: WbbseSubject = WbbseSubject.LIFE_SCIENCE,
    val selectedDiagramIndex: Int = 0
)

// ==================== 17. Writing Studio ====================

enum class WritingLanguage(val labelBn: String) {
    ENGLISH("ইংরেজি (English Writing)"),
    BENGALI("বাংলা (নির্মিতি ও রচনা)")
}

enum class WritingFormatType(
    val language: WritingLanguage,
    val titleBn: String,
    val marksNote: String,
    val sampleTopic: String
) {
    EN_NOTICE(WritingLanguage.ENGLISH, "Notice Writing (Box Format)", "10 Marks", "Inter-School Science Exhibition"),
    EN_REPORT(WritingLanguage.ENGLISH, "Newspaper / Event Report", "10 Marks", "Tree Plantation Programme at School"),
    EN_FORMAL_LETTER(WritingLanguage.ENGLISH, "Formal / Editorial Letter", "10 Marks", "Reckless Driving & Road Safety"),
    EN_INFORMAL_LETTER(WritingLanguage.ENGLISH, "Personal / Informal Letter", "10 Marks", "Advising Younger Brother on Time Management"),
    EN_PROCESS(WritingLanguage.ENGLISH, "Process Writing", "10 Marks", "Preparation of Orange Juice / ORS"),
    EN_BIOGRAPHY(WritingLanguage.ENGLISH, "Biography Writing", "10 Marks", "Acharya Prafulla Chandra Ray"),
    EN_STORY(WritingLanguage.ENGLISH, "Story Writing (with Moral)", "10 Marks", "The Honest Woodcutter"),
    BN_PRABANDHA(WritingLanguage.BENGALI, "প্রবন্ধ রচনা", "১০ নম্বর", "দৈনন্দিন জীবনে বিজ্ঞান"),
    BN_SANGLAP(WritingLanguage.BENGALI, "সংলাপ রচনা", "৫ নম্বর", "মোবাইল ফোনের সুফল ও কুফল নিয়ে দুই বন্ধুর সংলাপ"),
    BN_PROTIBEDON(WritingLanguage.BENGALI, "প্রতিবেদন রচনা", "৫ নম্বর", "রক্তদান শিবির ও বৃক্ষরোপণ উৎসব"),
    BN_BONGANUBAD(WritingLanguage.BENGALI, "বঙ্গানুবাদ (English → বাংলা)", "৪ নম্বর", "Work is worship. Honesty is the best policy. A student should always respect teachers and parents.")
}

data class WritingStudioUiState(
    val isOpen: Boolean = false,
    val language: WritingLanguage = WritingLanguage.ENGLISH,
    val selectedFormat: WritingFormatType = WritingFormatType.EN_NOTICE,
    val topicInput: String = WritingFormatType.EN_NOTICE.sampleTopic,
    val generatedOutput: String = "",
    val sourceBadge: String = "",
    val isGenerating: Boolean = false
)

// ==================== 18. Language Mastery Labs (English & Bengali) ====================

enum class LanguageLabMode {
    ENGLISH_LAB,
    BENGALI_GRAMMAR_LAB
}

data class LanguageLabUiState(
    val isOpen: Boolean = false,
    val mode: LanguageLabMode = LanguageLabMode.ENGLISH_LAB,
    val analyzerInput: String = "",
    val analyzerOutput: String = "",
    val isAnalyzing: Boolean = false,
    val customUnseenInput: String = "",
    val customUnseenOutput: String = "",
    val isSolvingUnseen: Boolean = false
)

// ==================== 19. Offline Math & Mensuration Solver ====================

enum class MathSolverModule(val titleBn: String) {
    ARITHMETIC_INTEREST("সরল ও চক্রবৃদ্ধি সুদ"),
    QUADRATIC_SRIDHAR("দ্বিঘাত (শ্রীধর আচার্য)"),
    SPHERE("গোলক (Sphere)"),
    CYLINDER("লম্ব বৃত্তাকার চোঙ"),
    CONE("লম্ব বৃত্তাকার শঙ্কু")
}

data class MathSolverUiState(
    val isOpen: Boolean = false,
    val module: MathSolverModule = MathSolverModule.SPHERE
)
