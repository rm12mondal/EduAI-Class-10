package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

object TopicImportance {
    const val VVI = "VVI"
    const val MEDIUM = "MEDIUM"
    const val LOW = "LOW"
}

@Entity(tableName = "wbbse_master_data")
data class MasterDataEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: String,
    val chapterName: String,
    val topicTitle: String,
    val keywords: String,
    val content: String,
    val keyFormulaOrFact: String,
    val examTip: String,
    val isBookmarked: Boolean = false,
    val isUserCreated: Boolean = false,
    val importanceLevel: String = TopicImportance.VVI,
    val chapterOrder: Int = 0
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val role: String, // "user" or "assistant"
    val content: String,
    val subjectId: String?,
    val mode: String, // "offline" or "online"
    val sourceLabel: String,
    val latencyMs: Long = 0L,
    val timestamp: Long = System.currentTimeMillis(),
    val imagePath: String? = null,
    val sectionId: String = "main_chat",
    val diagramPath: String? = null,
    val topicLabel: String = ""
)

@Entity(tableName = "exam_attempts")
data class ExamAttemptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: String,
    val examType: String, // "MCQ", "SAQ", "LONG", "TOPPER", "MADHYAMIK_CHAT"
    val questionSummary: String,
    val scoreObtained: Float,
    val maxMarks: Float,
    val accuracyPercent: Int,
    val feedbackSummary: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "topper_set_progress")
data class TopperSetProgressEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val trackId: String,
    val setNumber: Int,
    val usedQuestionIdsCsv: String,
    val scoreObtained: Float,
    val maxMarks: Float,
    val dateStamp: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "topper_mistakes")
data class TopperMistakeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val trackId: String,
    val questionId: String,
    val sectionCode: String,
    val marks: Int,
    val chapter: String,
    val questionText: String,
    val optionsJoined: String, // separated by "|||" for MCQs
    val correctOptionIndex: Int,
    val isTwisted: Boolean,
    val simpleMeaningBn: String,
    val modelAnswer: String,
    val presentationTip: String,
    val keyTermsCsv: String,
    val mistakeNote: String,
    val isResolved: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "daily_plan_tasks")
data class DailyPlanTaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateStamp: String, // YYYY-MM-DD
    val targetHours: Int, // 2, 4, or 6
    val subjectId: String,
    val titleBn: String,
    val detailBn: String,
    val durationMinutes: Int,
    val actionType: String, // SMART_LEARNING, SUBJECT_NOTE, TOPPER_CAPSULE, RAPID_FIRE, MAP_BOARD
    val isCompleted: Boolean = false,
    val orderIndex: Int = 0
)

@Entity(tableName = "study_streak")
data class StudyStreakEntity(
    @PrimaryKey val id: Int = 1,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastCompletedDateStamp: String = "",
    val totalTasksCompleted: Int = 0,
    val selectedDailyHours: Int = 4
)

@Entity(tableName = "brahmastram_progress")
data class BrahmastramProgressEntity(
    @PrimaryKey val subjectId: String,
    val completedNoteIdsCsv: String = "",
    val totalNotesCount: Int = 0,
    val lastExamScore: Float = 0f,
    val lastExamMaxMarks: Float = 0f,
    val examsTakenCount: Int = 0,
    val lastUpdatedMs: Long = System.currentTimeMillis()
)

@Entity(tableName = "spaced_revision_items")
data class SpacedRevisionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: String,
    val chapterName: String,
    val topicTitle: String,
    val keySummary: String,
    val sourceType: String, // "STUDY" or "MISTAKE"
    val intervalDays: Int = 3, // 3 -> 7 -> 21
    val masteryStage: Int = 0, // 0=3d, 1=7d, 2=21d, 3=Mastered
    val lastReviewedAtMs: Long = System.currentTimeMillis(),
    val nextReviewAtMs: Long = System.currentTimeMillis()
)
