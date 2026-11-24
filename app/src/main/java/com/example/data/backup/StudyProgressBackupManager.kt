package com.example.data.backup

import android.content.Context
import android.net.Uri
import com.example.data.local.BrahmastramProgressEntity
import com.example.data.local.DailyPlanTaskEntity
import com.example.data.local.ExamAttemptEntity
import com.example.data.local.SpacedRevisionEntity
import com.example.data.local.StudyRepository
import com.example.data.local.StudyStreakEntity
import com.example.data.local.TopperMistakeEntity
import com.example.data.local.TopperSetProgressEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

data class BackupSummaryStats(
    val examAttemptsCount: Int,
    val topperSetsCount: Int,
    val mistakesCount: Int,
    val brahmastramCount: Int,
    val spacedRevisionsCount: Int,
    val streakDays: Int
)

/**
 * 100% Offline Study Progress Backup & Restore Manager (`EduAI_Backup.json`).
 * Exports & restores Room Database progress (Mistake Notebook, Brahmastram progress,
 * Streak, Daily Plan, Spaced Repetition, and Score Predictor Exam History) using SAF URIs
 * with zero storage permissions.
 */
object StudyProgressBackupManager {

    private const val SCHEMA_VERSION = 1

    suspend fun exportBackupToUri(
        context: Context,
        uri: Uri,
        repository: StudyRepository
    ): Result<BackupSummaryStats> = withContext(Dispatchers.IO) {
        runCatching {
            val attempts = repository.getBackupExamAttempts()
            val topperSets = repository.getBackupTopperProgress()
            val mistakes = repository.getBackupTopperMistakes()
            val streak = repository.getStudyStreak()
            val dailyTasks = repository.getBackupDailyTasks()
            val brahmastram = repository.getBackupBrahmastram()
            val revisions = repository.getBackupRevisions()

            val root = JSONObject().apply {
                put("app", "EduAI_Class10_WBBSE")
                put("schemaVersion", SCHEMA_VERSION)
                put("exportedAtMs", System.currentTimeMillis())

                put("studyStreak", JSONObject().apply {
                    put("currentStreak", streak.currentStreak)
                    put("longestStreak", streak.longestStreak)
                    put("lastCompletedDateStamp", streak.lastCompletedDateStamp)
                    put("totalTasksCompleted", streak.totalTasksCompleted)
                    put("selectedDailyHours", streak.selectedDailyHours)
                })

                put("examAttempts", JSONArray().apply {
                    attempts.forEach { a ->
                        put(JSONObject().apply {
                            put("subjectId", a.subjectId)
                            put("examType", a.examType)
                            put("questionSummary", a.questionSummary)
                            put("scoreObtained", a.scoreObtained.toDouble())
                            put("maxMarks", a.maxMarks.toDouble())
                            put("accuracyPercent", a.accuracyPercent)
                            put("feedbackSummary", a.feedbackSummary)
                            put("timestamp", a.timestamp)
                        })
                    }
                })

                put("topperSetProgress", JSONArray().apply {
                    topperSets.forEach { s ->
                        put(JSONObject().apply {
                            put("trackId", s.trackId)
                            put("setNumber", s.setNumber)
                            put("usedQuestionIdsCsv", s.usedQuestionIdsCsv)
                            put("scoreObtained", s.scoreObtained.toDouble())
                            put("maxMarks", s.maxMarks.toDouble())
                            put("dateStamp", s.dateStamp)
                            put("timestamp", s.timestamp)
                        })
                    }
                })

                put("topperMistakes", JSONArray().apply {
                    mistakes.forEach { m ->
                        put(JSONObject().apply {
                            put("trackId", m.trackId)
                            put("questionId", m.questionId)
                            put("sectionCode", m.sectionCode)
                            put("marks", m.marks)
                            put("chapter", m.chapter)
                            put("questionText", m.questionText)
                            put("optionsJoined", m.optionsJoined)
                            put("correctOptionIndex", m.correctOptionIndex)
                            put("isTwisted", m.isTwisted)
                            put("simpleMeaningBn", m.simpleMeaningBn)
                            put("modelAnswer", m.modelAnswer)
                            put("presentationTip", m.presentationTip)
                            put("keyTermsCsv", m.keyTermsCsv)
                            put("mistakeNote", m.mistakeNote)
                            put("isResolved", m.isResolved)
                            put("timestamp", m.timestamp)
                        })
                    }
                })

                put("dailyPlanTasks", JSONArray().apply {
                    dailyTasks.forEach { t ->
                        put(JSONObject().apply {
                            put("dateStamp", t.dateStamp)
                            put("targetHours", t.targetHours)
                            put("subjectId", t.subjectId)
                            put("titleBn", t.titleBn)
                            put("detailBn", t.detailBn)
                            put("durationMinutes", t.durationMinutes)
                            put("actionType", t.actionType)
                            put("isCompleted", t.isCompleted)
                            put("orderIndex", t.orderIndex)
                        })
                    }
                })

                put("brahmastramProgress", JSONArray().apply {
                    brahmastram.forEach { b ->
                        put(JSONObject().apply {
                            put("subjectId", b.subjectId)
                            put("completedNoteIdsCsv", b.completedNoteIdsCsv)
                            put("totalNotesCount", b.totalNotesCount)
                            put("lastExamScore", b.lastExamScore.toDouble())
                            put("lastExamMaxMarks", b.lastExamMaxMarks.toDouble())
                            put("examsTakenCount", b.examsTakenCount)
                            put("lastUpdatedMs", b.lastUpdatedMs)
                        })
                    }
                })

                put("spacedRevisions", JSONArray().apply {
                    revisions.forEach { r ->
                        put(JSONObject().apply {
                            put("subjectId", r.subjectId)
                            put("chapterName", r.chapterName)
                            put("topicTitle", r.topicTitle)
                            put("keySummary", r.keySummary)
                            put("sourceType", r.sourceType)
                            put("intervalDays", r.intervalDays)
                            put("masteryStage", r.masteryStage)
                            put("lastReviewedAtMs", r.lastReviewedAtMs)
                            put("nextReviewAtMs", r.nextReviewAtMs)
                        })
                    }
                })
            }

            context.contentResolver.openOutputStream(uri)?.use { out ->
                out.write(root.toString(2).toByteArray(Charsets.UTF_8))
                out.flush()
            } ?: error("ফাইল লেখার অনুমতি পাওয়া যায়নি।")

            BackupSummaryStats(
                examAttemptsCount = attempts.size,
                topperSetsCount = topperSets.size,
                mistakesCount = mistakes.size,
                brahmastramCount = brahmastram.size,
                spacedRevisionsCount = revisions.size,
                streakDays = streak.currentStreak
            )
        }
    }

    suspend fun importBackupFromUri(
        context: Context,
        uri: Uri,
        repository: StudyRepository
    ): Result<BackupSummaryStats> = withContext(Dispatchers.IO) {
        runCatching {
            val jsonText = context.contentResolver.openInputStream(uri)?.use { input ->
                input.bufferedReader(Charsets.UTF_8).readText()
            } ?: error("ব্যাকআপ ফাইলটি পড়া যায়নি।")

            val root = JSONObject(jsonText)
            val appTag = root.optString("app")
            require(appTag == "EduAI_Class10_WBBSE") {
                "সঠিক EduAI_Backup.json ফাইল বেছে নাও।"
            }

            val streakObj = root.optJSONObject("studyStreak")
            val streak = streakObj?.let {
                StudyStreakEntity(
                    id = 1,
                    currentStreak = it.optInt("currentStreak", 0),
                    longestStreak = it.optInt("longestStreak", 0),
                    lastCompletedDateStamp = it.optString("lastCompletedDateStamp", ""),
                    totalTasksCompleted = it.optInt("totalTasksCompleted", 0),
                    selectedDailyHours = it.optInt("selectedDailyHours", 4)
                )
            }

            val attempts = mutableListOf<ExamAttemptEntity>()
            root.optJSONArray("examAttempts")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val o = arr.optJSONObject(i) ?: continue
                    attempts.add(
                        ExamAttemptEntity(
                            subjectId = o.optString("subjectId"),
                            examType = o.optString("examType"),
                            questionSummary = o.optString("questionSummary"),
                            scoreObtained = o.optDouble("scoreObtained", 0.0).toFloat(),
                            maxMarks = o.optDouble("maxMarks", 1.0).toFloat(),
                            accuracyPercent = o.optInt("accuracyPercent", 0),
                            feedbackSummary = o.optString("feedbackSummary"),
                            timestamp = o.optLong("timestamp", System.currentTimeMillis())
                        )
                    )
                }
            }

            val topperSets = mutableListOf<TopperSetProgressEntity>()
            root.optJSONArray("topperSetProgress")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val o = arr.optJSONObject(i) ?: continue
                    topperSets.add(
                        TopperSetProgressEntity(
                            trackId = o.optString("trackId"),
                            setNumber = o.optInt("setNumber", 1),
                            usedQuestionIdsCsv = o.optString("usedQuestionIdsCsv"),
                            scoreObtained = o.optDouble("scoreObtained", 0.0).toFloat(),
                            maxMarks = o.optDouble("maxMarks", 1.0).toFloat(),
                            dateStamp = o.optString("dateStamp"),
                            timestamp = o.optLong("timestamp", System.currentTimeMillis())
                        )
                    )
                }
            }

            val mistakes = mutableListOf<TopperMistakeEntity>()
            root.optJSONArray("topperMistakes")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val o = arr.optJSONObject(i) ?: continue
                    mistakes.add(
                        TopperMistakeEntity(
                            trackId = o.optString("trackId"),
                            questionId = o.optString("questionId"),
                            sectionCode = o.optString("sectionCode"),
                            marks = o.optInt("marks", 1),
                            chapter = o.optString("chapter"),
                            questionText = o.optString("questionText"),
                            optionsJoined = o.optString("optionsJoined"),
                            correctOptionIndex = o.optInt("correctOptionIndex", 0),
                            isTwisted = o.optBoolean("isTwisted", false),
                            simpleMeaningBn = o.optString("simpleMeaningBn"),
                            modelAnswer = o.optString("modelAnswer"),
                            presentationTip = o.optString("presentationTip"),
                            keyTermsCsv = o.optString("keyTermsCsv"),
                            mistakeNote = o.optString("mistakeNote"),
                            isResolved = o.optBoolean("isResolved", false),
                            timestamp = o.optLong("timestamp", System.currentTimeMillis())
                        )
                    )
                }
            }

            val dailyTasks = mutableListOf<DailyPlanTaskEntity>()
            root.optJSONArray("dailyPlanTasks")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val o = arr.optJSONObject(i) ?: continue
                    dailyTasks.add(
                        DailyPlanTaskEntity(
                            dateStamp = o.optString("dateStamp"),
                            targetHours = o.optInt("targetHours", 4),
                            subjectId = o.optString("subjectId"),
                            titleBn = o.optString("titleBn"),
                            detailBn = o.optString("detailBn"),
                            durationMinutes = o.optInt("durationMinutes", 30),
                            actionType = o.optString("actionType", "SMART_LEARNING"),
                            isCompleted = o.optBoolean("isCompleted", false),
                            orderIndex = o.optInt("orderIndex", i)
                        )
                    )
                }
            }

            val brahmastram = mutableListOf<BrahmastramProgressEntity>()
            root.optJSONArray("brahmastramProgress")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val o = arr.optJSONObject(i) ?: continue
                    brahmastram.add(
                        BrahmastramProgressEntity(
                            subjectId = o.optString("subjectId"),
                            completedNoteIdsCsv = o.optString("completedNoteIdsCsv"),
                            totalNotesCount = o.optInt("totalNotesCount", 0),
                            lastExamScore = o.optDouble("lastExamScore", 0.0).toFloat(),
                            lastExamMaxMarks = o.optDouble("lastExamMaxMarks", 0.0).toFloat(),
                            examsTakenCount = o.optInt("examsTakenCount", 0),
                            lastUpdatedMs = o.optLong("lastUpdatedMs", System.currentTimeMillis())
                        )
                    )
                }
            }

            val revisions = mutableListOf<SpacedRevisionEntity>()
            root.optJSONArray("spacedRevisions")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val o = arr.optJSONObject(i) ?: continue
                    revisions.add(
                        SpacedRevisionEntity(
                            subjectId = o.optString("subjectId"),
                            chapterName = o.optString("chapterName"),
                            topicTitle = o.optString("topicTitle"),
                            keySummary = o.optString("keySummary"),
                            sourceType = o.optString("sourceType", "STUDY"),
                            intervalDays = o.optInt("intervalDays", 3),
                            masteryStage = o.optInt("masteryStage", 0),
                            lastReviewedAtMs = o.optLong("lastReviewedAtMs", System.currentTimeMillis()),
                            nextReviewAtMs = o.optLong("nextReviewAtMs", System.currentTimeMillis())
                        )
                    )
                }
            }

            repository.restoreFullProgressBackup(
                examAttempts = attempts,
                topperProgress = topperSets,
                topperMistakes = mistakes,
                streak = streak,
                dailyTasks = dailyTasks,
                brahmastramProgress = brahmastram,
                spacedRevisions = revisions
            )

            BackupSummaryStats(
                examAttemptsCount = attempts.size,
                topperSetsCount = topperSets.size,
                mistakesCount = mistakes.size,
                brahmastramCount = brahmastram.size,
                spacedRevisionsCount = revisions.size,
                streakDays = streak?.currentStreak ?: 0
            )
        }
    }
}
