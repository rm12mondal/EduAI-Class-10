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
        TopperMistakeEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class EduAiDatabase : RoomDatabase() {
    abstract fun masterDataDao(): MasterDataDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun examAttemptDao(): ExamAttemptDao
    abstract fun topperDao(): TopperDao

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

        fun getInstance(context: Context): EduAiDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    EduAiDatabase::class.java,
                    "eduai_class10_wbbse.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
