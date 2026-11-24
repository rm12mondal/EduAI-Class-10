package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MasterDataDao {
    @Query("SELECT * FROM wbbse_master_data ORDER BY subjectId ASC, chapterOrder ASC, id ASC")
    fun observeAll(): Flow<List<MasterDataEntity>>

    @Query("SELECT * FROM wbbse_master_data WHERE subjectId = :subjectId ORDER BY chapterOrder ASC, id ASC")
    fun observeBySubject(subjectId: String): Flow<List<MasterDataEntity>>

    @Query("SELECT * FROM wbbse_master_data ORDER BY subjectId ASC, chapterOrder ASC, id ASC")
    suspend fun getAllSnapshot(): List<MasterDataEntity>

    @Query("SELECT * FROM wbbse_master_data WHERE subjectId = :subjectId ORDER BY chapterOrder ASC, id ASC")
    suspend fun getBySubjectSnapshot(subjectId: String): List<MasterDataEntity>

    @Query("SELECT COUNT(*) FROM wbbse_master_data")
    suspend fun count(): Int

    @Query("SELECT COUNT(*) FROM wbbse_master_data WHERE isUserCreated = 0")
    suspend fun countBuiltIn(): Int

    @Query("SELECT * FROM wbbse_master_data WHERE isUserCreated = 0 AND isBookmarked = 1")
    suspend fun getBookmarkedBuiltIn(): List<MasterDataEntity>

    @Query("DELETE FROM wbbse_master_data WHERE isUserCreated = 0")
    suspend fun deleteAllBuiltIn()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<MasterDataEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: MasterDataEntity): Long

    @Query("UPDATE wbbse_master_data SET isBookmarked = :bookmarked WHERE id = :id")
    suspend fun updateBookmark(id: Long, bookmarked: Boolean)

    @Query("DELETE FROM wbbse_master_data WHERE id = :id AND isUserCreated = 1")
    suspend fun deleteUserNote(id: Long)
}

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC, id ASC")
    fun observeAllMessages(): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity): Long

    @Query("DELETE FROM chat_messages")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM chat_messages")
    fun observeCount(): Flow<Int>
}

@Dao
interface ExamAttemptDao {
    @Query("SELECT * FROM exam_attempts ORDER BY timestamp DESC, id DESC")
    fun observeAllAttempts(): Flow<List<ExamAttemptEntity>>

    @Query("SELECT * FROM exam_attempts ORDER BY timestamp DESC, id DESC")
    suspend fun getAllAttemptsSnapshot(): List<ExamAttemptEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttempt(attempt: ExamAttemptEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttempts(attempts: List<ExamAttemptEntity>)

    @Query("DELETE FROM exam_attempts")
    suspend fun clearAllAttempts()
}

@Dao
interface TopperDao {
    @Query("SELECT * FROM topper_set_progress ORDER BY timestamp DESC")
    fun observeAllSetProgress(): Flow<List<TopperSetProgressEntity>>

    @Query("SELECT * FROM topper_set_progress ORDER BY timestamp DESC")
    suspend fun getAllSetProgressSnapshot(): List<TopperSetProgressEntity>

    @Query("SELECT * FROM topper_set_progress WHERE trackId = :trackId ORDER BY setNumber ASC")
    suspend fun getProgressForTrack(trackId: String): List<TopperSetProgressEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetProgress(progress: TopperSetProgressEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetProgressList(list: List<TopperSetProgressEntity>)

    @Query("SELECT * FROM topper_mistakes WHERE isResolved = 0 ORDER BY timestamp DESC")
    fun observeActiveMistakes(): Flow<List<TopperMistakeEntity>>

    @Query("SELECT * FROM topper_mistakes ORDER BY timestamp DESC")
    suspend fun getAllMistakesSnapshot(): List<TopperMistakeEntity>

    @Query("SELECT * FROM topper_mistakes WHERE trackId = :trackId AND isResolved = 0 ORDER BY timestamp DESC")
    suspend fun getActiveMistakesForTrack(trackId: String): List<TopperMistakeEntity>

    @Query("SELECT * FROM topper_mistakes WHERE isResolved = 0 ORDER BY timestamp DESC")
    suspend fun getAllActiveMistakes(): List<TopperMistakeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMistakes(mistakes: List<TopperMistakeEntity>)

    @Query("UPDATE topper_mistakes SET isResolved = 1 WHERE questionId IN (:questionIds)")
    suspend fun markMistakesResolved(questionIds: List<String>)

    @Query("DELETE FROM topper_set_progress")
    suspend fun clearSetProgress()

    @Query("DELETE FROM topper_mistakes")
    suspend fun clearMistakes()
}

@Dao
interface DailyPlannerDao {
    @Query("SELECT * FROM daily_plan_tasks WHERE dateStamp = :dateStamp ORDER BY orderIndex ASC, id ASC")
    fun observeTasksForDate(dateStamp: String): Flow<List<DailyPlanTaskEntity>>

    @Query("SELECT * FROM daily_plan_tasks WHERE dateStamp = :dateStamp ORDER BY orderIndex ASC, id ASC")
    suspend fun getTasksForDate(dateStamp: String): List<DailyPlanTaskEntity>

    @Query("SELECT * FROM daily_plan_tasks ORDER BY dateStamp DESC, orderIndex ASC")
    suspend fun getAllTasksSnapshot(): List<DailyPlanTaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<DailyPlanTaskEntity>)

    @Query("UPDATE daily_plan_tasks SET isCompleted = :completed WHERE id = :id")
    suspend fun updateTaskCompleted(id: Long, completed: Boolean)

    @Query("DELETE FROM daily_plan_tasks WHERE dateStamp = :dateStamp")
    suspend fun clearTasksForDate(dateStamp: String)

    @Query("DELETE FROM daily_plan_tasks")
    suspend fun clearAllTasks()

    @Query("SELECT * FROM study_streak WHERE id = 1")
    fun observeStreak(): Flow<StudyStreakEntity?>

    @Query("SELECT * FROM study_streak WHERE id = 1")
    suspend fun getStreak(): StudyStreakEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStreak(streak: StudyStreakEntity)
}

@Dao
interface MasterUpgradeDao {
    @Query("SELECT * FROM brahmastram_progress")
    fun observeAllBrahmastramProgress(): Flow<List<BrahmastramProgressEntity>>

    @Query("SELECT * FROM brahmastram_progress")
    suspend fun getAllBrahmastramProgressSnapshot(): List<BrahmastramProgressEntity>

    @Query("SELECT * FROM brahmastram_progress WHERE subjectId = :subjectId")
    suspend fun getBrahmastramProgress(subjectId: String): BrahmastramProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBrahmastramProgress(progress: BrahmastramProgressEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBrahmastramProgressList(items: List<BrahmastramProgressEntity>)

    @Query("DELETE FROM brahmastram_progress")
    suspend fun clearBrahmastramProgress()

    @Query("SELECT * FROM spaced_revision_items ORDER BY nextReviewAtMs ASC, id ASC")
    fun observeAllRevisions(): Flow<List<SpacedRevisionEntity>>

    @Query("SELECT * FROM spaced_revision_items ORDER BY nextReviewAtMs ASC, id ASC")
    suspend fun getAllRevisionsSnapshot(): List<SpacedRevisionEntity>

    @Query("SELECT * FROM spaced_revision_items WHERE subjectId = :subjectId AND topicTitle = :topicTitle LIMIT 1")
    suspend fun findRevisionByTopic(subjectId: String, topicTitle: String): SpacedRevisionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRevision(item: SpacedRevisionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRevisions(items: List<SpacedRevisionEntity>)

    @Query("DELETE FROM spaced_revision_items")
    suspend fun clearRevisions()
}
