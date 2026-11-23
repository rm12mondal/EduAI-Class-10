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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttempt(attempt: ExamAttemptEntity): Long

    @Query("DELETE FROM exam_attempts")
    suspend fun clearAllAttempts()
}

@Dao
interface TopperDao {
    @Query("SELECT * FROM topper_set_progress ORDER BY timestamp DESC")
    fun observeAllSetProgress(): Flow<List<TopperSetProgressEntity>>

    @Query("SELECT * FROM topper_set_progress WHERE trackId = :trackId ORDER BY setNumber ASC")
    suspend fun getProgressForTrack(trackId: String): List<TopperSetProgressEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetProgress(progress: TopperSetProgressEntity): Long

    @Query("SELECT * FROM topper_mistakes WHERE isResolved = 0 ORDER BY timestamp DESC")
    fun observeActiveMistakes(): Flow<List<TopperMistakeEntity>>

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
