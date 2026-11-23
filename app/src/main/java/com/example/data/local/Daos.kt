package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MasterDataDao {
    @Query("SELECT * FROM wbbse_master_data ORDER BY subjectId ASC, id ASC")
    fun observeAll(): Flow<List<MasterDataEntity>>

    @Query("SELECT * FROM wbbse_master_data WHERE subjectId = :subjectId ORDER BY id ASC")
    fun observeBySubject(subjectId: String): Flow<List<MasterDataEntity>>

    @Query("SELECT * FROM wbbse_master_data")
    suspend fun getAllSnapshot(): List<MasterDataEntity>

    @Query("SELECT * FROM wbbse_master_data WHERE subjectId = :subjectId")
    suspend fun getBySubjectSnapshot(subjectId: String): List<MasterDataEntity>

    @Query("SELECT COUNT(*) FROM wbbse_master_data")
    suspend fun count(): Int

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
