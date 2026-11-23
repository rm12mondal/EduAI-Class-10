package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

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
    val isUserCreated: Boolean = false
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
    val imagePath: String? = null
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
