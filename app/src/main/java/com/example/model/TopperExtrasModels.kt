package com.example.model

import com.example.data.local.DailyPlanTaskEntity
import com.example.data.local.StudyStreakEntity

// ==================== 1. AI Daily Study Planner & Streak ====================

object PlanActionType {
    const val SMART_LEARNING = "SMART_LEARNING"
    const val SUBJECT_NOTE = "SUBJECT_NOTE"
    const val TOPPER_CAPSULE = "TOPPER_CAPSULE"
    const val RAPID_FIRE = "RAPID_FIRE"
    const val MAP_BOARD = "MAP_BOARD"
}

data class DailyPlannerUiState(
    val dateStamp: String = "",
    val selectedHours: Int = 4, // 2, 4, or 6
    val tasks: List<DailyPlanTaskEntity> = emptyList(),
    val streak: StudyStreakEntity = StudyStreakEntity(),
    val isGenerating: Boolean = false,
    val sourceBadge: String = "অফলাইন রুটিন ইঞ্জিন",
    val isSheetOpen: Boolean = false
) {
    val completedCount: Int get() = tasks.count { it.isCompleted }
    val totalCount: Int get() = tasks.size
    val progressFraction: Float
        get() = if (totalCount == 0) 0f else (completedCount.toFloat() / totalCount.toFloat()).coerceIn(0f, 1f)
}

// ==================== 2. Interactive Map Pointing Practice Board ====================

enum class MapBoardDomain(
    val id: String,
    val titleBn: String,
    val marksLabel: String
) {
    GEOGRAPHY("geography", "ভূগোল ম্যাপ পয়েন্টিং", "১০ নম্বর"),
    HISTORY("history", "ইতিহাস ম্যাপ পয়েন্টিং", "৪ নম্বর")
}

enum class MapSymbolType(val symbolChar: String, val labelBn: String) {
    MOUNTAIN("▲", "পর্বত / শৃঙ্গ"),
    RIVER("〰", "নদী"),
    REGION("▓", "অঞ্চল / অরণ্য / মালভূমি"),
    CITY_CENTER("●", "কেন্দ্র / বন্দর / শহর"),
    REVOLT_STAR("★", "বিদ্রোহ / আন্দোলন কেন্দ্র")
}

data class MapPointingLocation(
    val id: String,
    val domain: MapBoardDomain,
    val nameBn: String,
    val nameEn: String,
    val symbolType: MapSymbolType,
    val normX: Float, // 0f..1f on normalized India map canvas
    val normY: Float, // 0f..1f on normalized India map canvas
    val toleranceRadius: Float = 0.085f,
    val regionHintBn: String,
    val boardTipBn: String
)

data class MapBoardUiState(
    val isOpen: Boolean = false,
    val domain: MapBoardDomain = MapBoardDomain.GEOGRAPHY
)

// ==================== 3. Rapid Fire MCQ Speed Booster ====================

enum class RapidFireTimerMode(
    val labelBn: String,
    val subtitleBn: String
) {
    PER_QUESTION_30S("৩০ সেকেন্ড / প্রশ্ন", "প্রতি প্রশ্নে দ্রুত উত্তর"),
    TOTAL_15_MIN("মোট ১৫ মিনিট", "পরীক্ষা হলের স্পিড মোড")
}

data class RapidFireQuestion(
    val id: String,
    val subject: WbbseSubject,
    val chapterLabel: String,
    val questionText: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

data class RapidFireAnswerRecord(
    val question: RapidFireQuestion,
    val selectedIndex: Int?, // null if timed out
    val isCorrect: Boolean,
    val timeTakenSec: Float
)

data class RapidFireResult(
    val subject: WbbseSubject?, // null = All 7 subjects mix
    val totalQuestions: Int,
    val correctCount: Int,
    val wrongCount: Int,
    val timeoutCount: Int,
    val accuracyPercent: Int,
    val avgSecondsPerQuestion: Float,
    val totalSecondsElapsed: Int,
    val mistakesSavedCount: Int,
    val speedRatingBn: String,
    val records: List<RapidFireAnswerRecord>
)

data class RapidFireUiState(
    val isOpen: Boolean = false,
    val selectedSubject: WbbseSubject? = null, // null = All 7 subjects
    val questionCountTarget: Int = 20, // 20 or 30
    val timerMode: RapidFireTimerMode = RapidFireTimerMode.PER_QUESTION_30S,
    val isRunning: Boolean = false,
    val questions: List<RapidFireQuestion> = emptyList(),
    val currentIndex: Int = 0,
    val secondsRemaining: Int = 30,
    val totalElapsedSec: Int = 0,
    val currentQuestionElapsedSec: Int = 0,
    val selectedOptionForCurrent: Int? = null,
    val showFeedbackForCurrent: Boolean = false,
    val currentStreakCombo: Int = 0,
    val answeredRecords: List<RapidFireAnswerRecord> = emptyList(),
    val result: RapidFireResult? = null,
    val ggufTipNote: String? = null
)

// ==================== 4. Hands-Free Voice Tutor & Audio Revision ====================

data class AudioRevisionTrack(
    val id: Long,
    val subjectNameBn: String,
    val chapterName: String,
    val title: String,
    val categoryLabel: String, // "অধ্যায় সারাংশ", "সূত্র ও তথ্য", "VVI প্রশ্নোত্তর"
    val spokenScript: String,
    val displayPreview: String
)

data class AudioRevisionUiState(
    val isOpen: Boolean = false,
    val playlistTitle: String = "",
    val playlistSubtitle: String = "",
    val tracks: List<AudioRevisionTrack> = emptyList(),
    val currentIndex: Int = 0,
    val isPlaying: Boolean = false,
    val autoAdvance: Boolean = true,
    val playbackSpeed: Float = 1.0f // 0.8f, 1.0f, 1.25f
) {
    val currentTrack: AudioRevisionTrack? get() = tracks.getOrNull(currentIndex)
}
