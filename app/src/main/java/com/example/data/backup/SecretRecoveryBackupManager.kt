package com.example.data.backup

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.local.BrahmastramProgressEntity
import com.example.data.local.ChatMessageEntity
import com.example.data.local.DailyPlanTaskEntity
import com.example.data.local.ExamAttemptEntity
import com.example.data.local.MasterDataEntity
import com.example.data.local.SettingsDataStore
import com.example.data.local.SpacedRevisionEntity
import com.example.data.local.StudyRepository
import com.example.data.local.StudyStreakEntity
import com.example.data.local.TopicImportance
import com.example.data.local.TopperMistakeEntity
import com.example.data.local.TopperSetProgressEntity
import com.example.model.EncryptedBackupStats
import com.example.model.FastLearningContextHelper
import com.example.model.SyllabusPlanOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import javax.crypto.AEADBadTagException
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Secret Recovery Key & Dual Encrypted Backup Engine (`.edukey` format).
 *
 * Security design:
 * - Derives a 256-bit AES key from the student's Secret Recovery Key using PBKDF2WithHmacSHA256
 *   (100,000 iterations + fresh 16-byte SecureRandom salt per backup).
 * - Compresses the JSON bundle with GZIP (~10 KB - 2 MB) and encrypts using AES/GCM/NoPadding
 *   with a fresh 12-byte SecureRandom IV and 128-bit authentication tag.
 * - Binary layout: [8B Magic "EDUKEY01"][16B Salt][12B IV][AES-GCM Ciphertext + 16B Tag]
 * - Supports zero-server dual export:
 *   1) Local Storage via Android Storage Access Framework (`CreateDocument`)
 *   2) Google Drive via Android Share Sheet (`ACTION_SEND` targeting Google Drive directly)
 * - Supports instant restore via Android `OpenDocument` (browsing Local Storage & Google Drive).
 */
object SecretRecoveryBackupManager {

    private val MAGIC_HEADER = "EDUKEY01".toByteArray(Charsets.US_ASCII)
    private const val SALT_BYTES = 16
    private const val IV_BYTES = 12
    private const val GCM_TAG_BITS = 128
    private const val PBKDF2_ITERATIONS = 100_000
    private const val AES_KEY_BITS = 256
    private const val MAX_IMPORT_BYTES = 25 * 1024 * 1024 // 25 MB safety cap
    private const val SCHEMA_VERSION = 2
    private const val GOOGLE_DRIVE_PACKAGE = "com.google.android.apps.docs"

    fun suggestBackupFileName(): String {
        val stamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())
        return "EduAI_Class10_Backup_$stamp.edukey"
    }

    fun normalizeRecoveryKey(rawKey: String): String =
        rawKey.trim().uppercase(Locale.US).replace(Regex("\\s+"), "")

    suspend fun exportEncryptedBackupToUri(
        context: Context,
        uri: Uri,
        secretKey: String,
        repository: StudyRepository,
        settingsDataStore: SettingsDataStore
    ): Result<EncryptedBackupStats> = withContext(Dispatchers.IO) {
        runCatching {
            val (encryptedBytes, stats) = buildEncryptedPayload(
                secretKey = secretKey,
                repository = repository,
                settingsDataStore = settingsDataStore
            )
            context.contentResolver.openOutputStream(uri)?.use { out ->
                out.write(encryptedBytes)
                out.flush()
            } ?: error("ফোনের স্টোরেজে ফাইল সেভ করা যায়নি।")

            stats.copy(fileSizeBytes = encryptedBytes.size.toLong())
        }
    }

    suspend fun createEncryptedBackupFileForDrive(
        context: Context,
        secretKey: String,
        repository: StudyRepository,
        settingsDataStore: SettingsDataStore
    ): Result<Pair<File, EncryptedBackupStats>> = withContext(Dispatchers.IO) {
        runCatching {
            val (encryptedBytes, stats) = buildEncryptedPayload(
                secretKey = secretKey,
                repository = repository,
                settingsDataStore = settingsDataStore
            )
            val backupsDir = File(context.cacheDir, "edu_backups").apply {
                if (!exists()) mkdirs()
            }
            // Keep cache tidy: remove older temporary backup bundles
            backupsDir.listFiles()?.forEach { old ->
                if (old.isFile) old.delete()
            }
            val fileName = suggestBackupFileName()
            val outFile = File(backupsDir, fileName)
            FileOutputStream(outFile).use { fos ->
                fos.write(encryptedBytes)
                fos.flush()
            }
            outFile to stats.copy(
                fileSizeBytes = encryptedBytes.size.toLong(),
                exportedFileName = fileName
            )
        }
    }

    /**
     * Builds a zero-permission Share Intent targeting Google Drive directly when installed,
     * falling back cleanly to the Android Share Sheet so students can upload to Drive anywhere.
     */
    fun buildGoogleDriveShareIntent(context: Context, backupFile: File): Intent {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            backupFile
        )
        val baseIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/octet-stream"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, backupFile.name)
            putExtra(
                Intent.EXTRA_TEXT,
                "EduAI Class 10 Encrypted Backup (${backupFile.name}) — রিকভারি কী দিয়ে যেকোনো সময় রিস্টোর করা যাবে।"
            )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val hasGoogleDrive = try {
            context.packageManager.getPackageInfo(GOOGLE_DRIVE_PACKAGE, 0)
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }

        return if (hasGoogleDrive) {
            baseIntent.apply { setPackage(GOOGLE_DRIVE_PACKAGE) }
        } else {
            Intent.createChooser(baseIntent, "☁️ Google Drive-এ ব্যাকআপ সেভ করুন")
        }
    }

    suspend fun restoreEncryptedBackupFromUri(
        context: Context,
        uri: Uri,
        enteredKey: String,
        repository: StudyRepository,
        settingsDataStore: SettingsDataStore
    ): Result<EncryptedBackupStats> = withContext(Dispatchers.IO) {
        runCatching {
            val normalizedKey = normalizeRecoveryKey(enteredKey)
            require(normalizedKey.length >= 8) {
                "সঠিক Secret Recovery Key লিখো (যেমন: EDU10-XXXX-XXXX)।"
            }

            val encryptedBytes = context.contentResolver.openInputStream(uri)?.use { input ->
                val baos = ByteArrayOutputStream()
                val buffer = ByteArray(8192)
                var total = 0
                while (true) {
                    val read = input.read(buffer)
                    if (read <= 0) break
                    total += read
                    if (total > MAX_IMPORT_BYTES) {
                        error("ফাইলটি অত্যন্ত বড় (২৫ MB-এর বেশি)। সঠিক .edukey ফাইল বেছে নাও।")
                    }
                    baos.write(buffer, 0, read)
                }
                baos.toByteArray()
            } ?: error("নির্বাচিত .edukey ফাইলটি পড়া যায়নি।")

            val jsonText = decryptPayload(encryptedBytes, normalizedKey)
            val root = JSONObject(jsonText)
            require(root.optString("app") == "EduAI_Class10_WBBSE_EncryptedBackup") {
                "এটি সঠিক EduAI Class 10 ব্যাকআপ ফাইল নয়।"
            }

            // 1. Restore Chat Histories across ALL sections
            val chatMessages = mutableListOf<ChatMessageEntity>()
            root.optJSONArray("chatMessages")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val o = arr.optJSONObject(i) ?: continue
                    chatMessages.add(
                        ChatMessageEntity(
                            role = o.optString("role", "user"),
                            content = o.optString("content", ""),
                            subjectId = o.optString("subjectId").takeIf { it.isNotBlank() },
                            mode = o.optString("mode", "offline"),
                            sourceLabel = o.optString("sourceLabel", ""),
                            latencyMs = o.optLong("latencyMs", 0L),
                            timestamp = o.optLong("timestamp", System.currentTimeMillis()),
                            imagePath = null, // File paths are device-local; preserve clean text history
                            sectionId = o.optString("sectionId", "main_chat"),
                            diagramPath = null,
                            topicLabel = o.optString("topicLabel", "")
                        )
                    )
                }
            }

            // 2. Restore Self-Report Card progress
            val selfReportObj = root.optJSONObject("selfReportProgress")
            val restoredExamYear = selfReportObj?.optInt(
                "targetExamYear",
                FastLearningContextHelper.defaultTargetExamYear()
            ) ?: FastLearningContextHelper.defaultTargetExamYear()
            val restoredPlanMonths = selfReportObj?.optDouble(
                "syllabusPlanMonths",
                SyllabusPlanOption.MONTHS_3.months.toDouble()
            )?.toFloat() ?: SyllabusPlanOption.MONTHS_3.months
            val completedChapters = mutableSetOf<String>()
            selfReportObj?.optJSONArray("completedChapters")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val key = arr.optString(i)
                    if (key.isNotBlank()) completedChapters.add(key)
                }
            }

            // 3. Restore User Study Notes & Bookmarks
            val userNotes = mutableListOf<MasterDataEntity>()
            root.optJSONArray("userNotes")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val o = arr.optJSONObject(i) ?: continue
                    userNotes.add(
                        MasterDataEntity(
                            subjectId = o.optString("subjectId", "math"),
                            chapterName = o.optString("chapterName", "আমার নোট"),
                            topicTitle = o.optString("topicTitle", "নোট"),
                            keywords = o.optString("keywords", ""),
                            content = o.optString("content", ""),
                            keyFormulaOrFact = o.optString("keyFormulaOrFact", ""),
                            examTip = o.optString("examTip", ""),
                            isBookmarked = o.optBoolean("isBookmarked", false),
                            isUserCreated = true,
                            importanceLevel = o.optString("importanceLevel", TopicImportance.VVI),
                            chapterOrder = o.optInt("chapterOrder", 99)
                        )
                    )
                }
            }

            val bookmarkedKeys = mutableListOf<Pair<String, String>>()
            root.optJSONArray("bookmarkedBuiltInKeys")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val o = arr.optJSONObject(i) ?: continue
                    val subjId = o.optString("subjectId")
                    val topic = o.optString("topicTitle")
                    if (subjId.isNotBlank() && topic.isNotBlank()) {
                        bookmarkedKeys.add(subjId to topic)
                    }
                }
            }

            // 4. Restore Exam Scores, Topper Progress, Mistakes, Streak, Daily Tasks, Brahmastram, Spaced Revisions
            val streak = root.optJSONObject("studyStreak")?.let {
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

            repository.restoreCompleteEncryptedBackup(
                chatMessages = chatMessages,
                userNotes = userNotes,
                bookmarkedBuiltInKeys = bookmarkedKeys,
                examAttempts = attempts,
                topperProgress = topperSets,
                topperMistakes = mistakes,
                streak = streak,
                dailyTasks = dailyTasks,
                brahmastramProgress = brahmastram,
                spacedRevisions = revisions
            )

            settingsDataStore.restoreFastLearningProgress(
                examYear = restoredExamYear,
                planMonths = restoredPlanMonths,
                completedChapters = completedChapters
            )
            // Keep the verified restored key active on this device
            settingsDataStore.saveSecretRecoveryKey(normalizedKey)

            EncryptedBackupStats(
                fileSizeBytes = encryptedBytes.size.toLong(),
                totalChatMessages = chatMessages.size,
                chatSectionsCovered = chatMessages.map { it.sectionId }.distinct().size,
                examAttemptsCount = attempts.size,
                completedChaptersCount = completedChapters.size,
                studyNotesAndBookmarksCount = userNotes.size + bookmarkedKeys.size,
                streakDays = streak?.currentStreak ?: 0
            )
        }
    }

    private suspend fun buildEncryptedPayload(
        secretKey: String,
        repository: StudyRepository,
        settingsDataStore: SettingsDataStore
    ): Pair<ByteArray, EncryptedBackupStats> {
        val normalizedKey = normalizeRecoveryKey(secretKey)
        require(normalizedKey.length >= 8) { "বৈধ Secret Recovery Key প্রয়োজন।" }

        val storedSettings = settingsDataStore.settingsFlow.first()
        val chatMessages = repository.getAllChatMessagesSnapshot()
        val userNotes = repository.getBackupUserNotes()
        val bookmarkedKeys = repository.getBackupBookmarkedBuiltInKeys()
        val attempts = repository.getBackupExamAttempts()
        val topperSets = repository.getBackupTopperProgress()
        val mistakes = repository.getBackupTopperMistakes()
        val streak = repository.getStudyStreak()
        val dailyTasks = repository.getBackupDailyTasks()
        val brahmastram = repository.getBackupBrahmastram()
        val revisions = repository.getBackupRevisions()

        val root = JSONObject().apply {
            put("app", "EduAI_Class10_WBBSE_EncryptedBackup")
            put("schemaVersion", SCHEMA_VERSION)
            put("exportedAtMs", System.currentTimeMillis())

            put("selfReportProgress", JSONObject().apply {
                put("targetExamYear", storedSettings.fastLearningExamYear)
                put("syllabusPlanMonths", storedSettings.fastLearningPlanMonths.toDouble())
                put("completedChapters", JSONArray().apply {
                    storedSettings.fastLearningCompletedChapters.forEach { put(it) }
                })
            })

            put("chatMessages", JSONArray().apply {
                chatMessages.forEach { m ->
                    put(JSONObject().apply {
                        put("role", m.role)
                        put("content", m.content)
                        put("subjectId", m.subjectId ?: "")
                        put("mode", m.mode)
                        put("sourceLabel", m.sourceLabel)
                        put("latencyMs", m.latencyMs)
                        put("timestamp", m.timestamp)
                        put("sectionId", m.sectionId)
                        put("topicLabel", m.topicLabel)
                    })
                }
            })

            put("userNotes", JSONArray().apply {
                userNotes.forEach { n ->
                    put(JSONObject().apply {
                        put("subjectId", n.subjectId)
                        put("chapterName", n.chapterName)
                        put("topicTitle", n.topicTitle)
                        put("keywords", n.keywords)
                        put("content", n.content)
                        put("keyFormulaOrFact", n.keyFormulaOrFact)
                        put("examTip", n.examTip)
                        put("isBookmarked", n.isBookmarked)
                        put("importanceLevel", n.importanceLevel)
                        put("chapterOrder", n.chapterOrder)
                    })
                }
            })

            put("bookmarkedBuiltInKeys", JSONArray().apply {
                bookmarkedKeys.forEach { (subjId, topic) ->
                    put(JSONObject().apply {
                        put("subjectId", subjId)
                        put("topicTitle", topic)
                    })
                }
            })

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

        val encryptedBytes = encryptPayload(root.toString(), normalizedKey)
        val stats = EncryptedBackupStats(
            fileSizeBytes = encryptedBytes.size.toLong(),
            totalChatMessages = chatMessages.size,
            chatSectionsCovered = chatMessages.map { it.sectionId }.distinct().size,
            examAttemptsCount = attempts.size,
            completedChaptersCount = storedSettings.fastLearningCompletedChapters.size,
            studyNotesAndBookmarksCount = userNotes.size + bookmarkedKeys.size,
            streakDays = streak.currentStreak
        )
        return encryptedBytes to stats
    }

    private fun encryptPayload(plainJson: String, normalizedKey: String): ByteArray {
        val compressed = gzipCompress(plainJson.toByteArray(Charsets.UTF_8))
        val rng = SecureRandom()
        val salt = ByteArray(SALT_BYTES).also { rng.nextBytes(it) }
        val iv = ByteArray(IV_BYTES).also { rng.nextBytes(it) }
        val aesKey = deriveAesKey(normalizedKey, salt)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, aesKey, GCMParameterSpec(GCM_TAG_BITS, iv))
        val cipherText = cipher.doFinal(compressed)

        return ByteArrayOutputStream(MAGIC_HEADER.size + SALT_BYTES + IV_BYTES + cipherText.size).apply {
            write(MAGIC_HEADER)
            write(salt)
            write(iv)
            write(cipherText)
        }.toByteArray()
    }

    private fun decryptPayload(payload: ByteArray, normalizedKey: String): String {
        val headerSize = MAGIC_HEADER.size + SALT_BYTES + IV_BYTES
        require(payload.size > headerSize + 16) {
            "ফাইলটি সঠিক .edukey ব্যাকআপ নয় অথবা অসম্পূর্ণ।"
        }
        val magic = payload.copyOfRange(0, MAGIC_HEADER.size)
        require(magic.contentEquals(MAGIC_HEADER)) {
            "সঠিক EduAI .edukey এনক্রিপ্টেড ব্যাকআপ ফাইল বেছে নাও।"
        }

        val salt = payload.copyOfRange(MAGIC_HEADER.size, MAGIC_HEADER.size + SALT_BYTES)
        val iv = payload.copyOfRange(MAGIC_HEADER.size + SALT_BYTES, headerSize)
        val cipherText = payload.copyOfRange(headerSize, payload.size)

        val aesKey = deriveAesKey(normalizedKey, salt)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, aesKey, GCMParameterSpec(GCM_TAG_BITS, iv))

        val compressed = try {
            cipher.doFinal(cipherText)
        } catch (_: AEADBadTagException) {
            error("ভুল Secret Recovery Key! দয়া করে সঠিক চাবি (EDU10-XXXX-XXXX) দিয়ে আবার চেষ্টা করো।")
        }

        return gzipDecompress(compressed).toString(Charsets.UTF_8)
    }

    private fun deriveAesKey(normalizedKey: String, salt: ByteArray): SecretKeySpec {
        val spec = PBEKeySpec(normalizedKey.toCharArray(), salt, PBKDF2_ITERATIONS, AES_KEY_BITS)
        return try {
            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            SecretKeySpec(factory.generateSecret(spec).encoded, "AES")
        } finally {
            spec.clearPassword()
        }
    }

    private fun gzipCompress(data: ByteArray): ByteArray {
        val baos = ByteArrayOutputStream()
        GZIPOutputStream(baos).use { gz -> gz.write(data) }
        return baos.toByteArray()
    }

    private fun gzipDecompress(data: ByteArray): ByteArray {
        GZIPInputStream(ByteArrayInputStream(data)).use { gz ->
            val baos = ByteArrayOutputStream()
            val buf = ByteArray(8192)
            var total = 0
            while (true) {
                val n = gz.read(buf)
                if (n <= 0) break
                total += n
                if (total > MAX_IMPORT_BYTES) {
                    error("ব্যাকআপ ডেটা সীমার বাইরে।")
                }
                baos.write(buf, 0, n)
            }
            return baos.toByteArray()
        }
    }
}
