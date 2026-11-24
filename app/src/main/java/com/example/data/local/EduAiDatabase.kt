package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        MasterDataEntity::class,
        ChatMessageEntity::class,
        ExamAttemptEntity::class,
        TopperSetProgressEntity::class,
        TopperMistakeEntity::class,
        DailyPlanTaskEntity::class,
        StudyStreakEntity::class,
        BrahmastramProgressEntity::class,
        SpacedRevisionEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class EduAiDatabase : RoomDatabase() {
    abstract fun masterDataDao(): MasterDataDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun examAttemptDao(): ExamAttemptDao
    abstract fun topperDao(): TopperDao
    abstract fun dailyPlannerDao(): DailyPlannerDao
    abstract fun masterUpgradeDao(): MasterUpgradeDao

    companion object {
        @Volatile
        private var INSTANCE: EduAiDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE chat_messages ADD COLUMN imagePath TEXT")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `exam_attempts` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `subjectId` TEXT NOT NULL,
                        `examType` TEXT NOT NULL,
                        `questionSummary` TEXT NOT NULL,
                        `scoreObtained` REAL NOT NULL,
                        `maxMarks` REAL NOT NULL,
                        `accuracyPercent` INTEGER NOT NULL,
                        `feedbackSummary` TEXT NOT NULL,
                        `timestamp` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `topper_set_progress` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `trackId` TEXT NOT NULL,
                        `setNumber` INTEGER NOT NULL,
                        `usedQuestionIdsCsv` TEXT NOT NULL,
                        `scoreObtained` REAL NOT NULL,
                        `maxMarks` REAL NOT NULL,
                        `dateStamp` TEXT NOT NULL,
                        `timestamp` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `topper_mistakes` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `trackId` TEXT NOT NULL,
                        `questionId` TEXT NOT NULL,
                        `sectionCode` TEXT NOT NULL,
                        `marks` INTEGER NOT NULL,
                        `chapter` TEXT NOT NULL,
                        `questionText` TEXT NOT NULL,
                        `optionsJoined` TEXT NOT NULL,
                        `correctOptionIndex` INTEGER NOT NULL,
                        `isTwisted` INTEGER NOT NULL,
                        `simpleMeaningBn` TEXT NOT NULL,
                        `modelAnswer` TEXT NOT NULL,
                        `presentationTip` TEXT NOT NULL,
                        `keyTermsCsv` TEXT NOT NULL,
                        `mistakeNote` TEXT NOT NULL,
                        `isResolved` INTEGER NOT NULL,
                        `timestamp` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE wbbse_master_data ADD COLUMN importanceLevel TEXT NOT NULL DEFAULT 'VVI'"
                )
                db.execSQL(
                    "ALTER TABLE wbbse_master_data ADD COLUMN chapterOrder INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `daily_plan_tasks` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `dateStamp` TEXT NOT NULL,
                        `targetHours` INTEGER NOT NULL,
                        `subjectId` TEXT NOT NULL,
                        `titleBn` TEXT NOT NULL,
                        `detailBn` TEXT NOT NULL,
                        `durationMinutes` INTEGER NOT NULL,
                        `actionType` TEXT NOT NULL,
                        `isCompleted` INTEGER NOT NULL,
                        `orderIndex` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `study_streak` (
                        `id` INTEGER PRIMARY KEY NOT NULL,
                        `currentStreak` INTEGER NOT NULL,
                        `longestStreak` INTEGER NOT NULL,
                        `lastCompletedDateStamp` TEXT NOT NULL,
                        `totalTasksCompleted` INTEGER NOT NULL,
                        `selectedDailyHours` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `brahmastram_progress` (
                        `subjectId` TEXT PRIMARY KEY NOT NULL,
                        `completedNoteIdsCsv` TEXT NOT NULL,
                        `totalNotesCount` INTEGER NOT NULL,
                        `lastExamScore` REAL NOT NULL,
                        `lastExamMaxMarks` REAL NOT NULL,
                        `examsTakenCount` INTEGER NOT NULL,
                        `lastUpdatedMs` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `spaced_revision_items` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `subjectId` TEXT NOT NULL,
                        `chapterName` TEXT NOT NULL,
                        `topicTitle` TEXT NOT NULL,
                        `keySummary` TEXT NOT NULL,
                        `sourceType` TEXT NOT NULL,
                        `intervalDays` INTEGER NOT NULL,
                        `masteryStage` INTEGER NOT NULL,
                        `lastReviewedAtMs` INTEGER NOT NULL,
                        `nextReviewAtMs` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        fun getInstance(context: Context): EduAiDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    EduAiDatabase::class.java,
                    "eduai_class10_wbbse.db"
                )
                    .addMigrations(
                        MIGRATION_1_2,
                        MIGRATION_2_3,
                        MIGRATION_3_4,
                        MIGRATION_4_5,
                        MIGRATION_5_6
                    )
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
