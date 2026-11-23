package com.example.model

enum class ExamFormat(
    val id: String,
    val bengaliLabel: String,
    val shortLabel: String,
    val subtitleBn: String
) {
    MCQ(
        id = "MCQ",
        bengaliLabel = "MCQ (বহুবিকল্প)",
        shortLabel = "MCQ",
        subtitleBn = "অটোমেটিক স্কোর ও ব্যাখ্যা"
    ),
    SAQ(
        id = "SAQ",
        bengaliLabel = "SAQ (সংক্ষিপ্ত)",
        shortLabel = "SAQ",
        subtitleBn = "টাইপ করে উত্তর ও AI মূল্যায়ন (২ নম্বর)"
    ),
    LONG(
        id = "LONG",
        bengaliLabel = "বড় প্রশ্ন (রচনাধর্মী)",
        shortLabel = "LONG",
        subtitleBn = "টাইপ বা খাতার ছবি তুলে মূল্যায়ন (৫ নম্বর)"
    );

    companion object {
        fun fromId(id: String): ExamFormat = entries.firstOrNull { it.id == id } ?: MCQ
    }
}

data class McqQuestion(
    val id: String,
    val subject: WbbseSubject,
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

data class SaqQuestion(
    val id: String,
    val subject: WbbseSubject,
    val question: String,
    val maxMarks: Int = 2,
    val modelAnswer: String,
    val keyTerms: List<String>
)

data class LongQuestion(
    val id: String,
    val subject: WbbseSubject,
    val question: String,
    val maxMarks: Int = 5,
    val modelOutline: String,
    val keyTerms: List<String>
)

data class SubjectiveEvaluationResult(
    val scoreObtained: Float,
    val maxMarks: Float,
    val verdict: String,
    val strengths: String,
    val mistakesAndGaps: String,
    val improvementTips: String,
    val modelAnswer: String,
    val evaluatorSource: String
)

data class SubjectProgressSummary(
    val subject: WbbseSubject,
    val attemptsCount: Int,
    val totalObtained: Float,
    val totalPossible: Float,
    val accuracyPercent: Int
)

data class OverallProgressStats(
    val totalTestsTaken: Int,
    val overallAccuracyPercent: Int,
    val totalMarksObtained: Float,
    val totalMarksPossible: Float,
    val subjectSummaries: List<SubjectProgressSummary>,
    val weakSubjects: List<SubjectProgressSummary>
)
