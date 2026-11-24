package com.example.model

/**
 * Options attached to any Chat or Exam Chat request.
 */
data class ChatPromptOptions(
    val smartToThePoint: Boolean = false,
    val visualDiagramRequested: Boolean = false,
    val translateMode: Boolean = false,
    val targetMarks: Int? = null, // 2, 3, 4, 5, or 8
    val exactTextbookDefinition: Boolean = false
)

/**
 * Intelligent 3-way intent categories inside the Exam & Quiz Chat Box.
 */
enum class ExamChatIntent(
    val badgeBn: String,
    val badgeEn: String
) {
    DIRECT_STUDY("টু-দ্য-পয়েন্ট উত্তর", "Direct Study"),
    NORMAL_TEST("নরমাল টেস্ট (সময়সীমা নেই)", "Normal Test"),
    MADHYAMIK_TEST("মাধ্যমিক টেস্ট (Exam Hall)", "Madhyamik Test"),
    HANDWRITTEN_GRADING("খাতার ছবি মূল্যায়ন", "Sheet Grading"),
    TRANSLATION("বাংলা ↔ English অনুবাদ", "Translation")
}

enum class ExamSessionMode {
    NORMAL_UNTIMED,
    MADHYAMIK_TIMED
}

/**
 * Represents an active Normal Test (untimed) or Madhyamik Test (timed exam hall) session
 * running inside the Exam Chat Box.
 */
data class ActiveExamSession(
    val mode: ExamSessionMode,
    val subject: WbbseSubject?,
    val title: String,
    val chapterHint: String? = null,
    val pageHint: String? = null,
    val fullMarks: Int = 90,
    val totalDurationSeconds: Long = 0L,
    val remainingSeconds: Long = 0L,
    val isPaused: Boolean = false,
    val isTimeUp: Boolean = false
)

/**
 * Single message item inside the 3-in-1 Exam & Quiz Chat Box.
 */
data class ExamChatMessage(
    val id: Long,
    val role: String, // "user" or "assistant"
    val content: String,
    val intent: ExamChatIntent,
    val subject: WbbseSubject?,
    val mode: InferenceMode,
    val sourceLabel: String,
    val userPhotoPath: String? = null,
    val generatedDiagramPath: String? = null,
    val diagramError: String? = null,
    val latencyMs: Long = 0L,
    val timestamp: Long = System.currentTimeMillis()
)
