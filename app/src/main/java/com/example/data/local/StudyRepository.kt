package com.example.data.local

import kotlinx.coroutines.flow.Flow

class StudyRepository(
    private val masterDataDao: MasterDataDao,
    private val chatMessageDao: ChatMessageDao,
    private val examAttemptDao: ExamAttemptDao,
    private val topperDao: TopperDao
) {
    val allMasterNotes: Flow<List<MasterDataEntity>> = masterDataDao.observeAll()
    val allChatMessages: Flow<List<ChatMessageEntity>> = chatMessageDao.observeAllMessages()
    val chatMessageCount: Flow<Int> = chatMessageDao.observeCount()
    val allExamAttempts: Flow<List<ExamAttemptEntity>> = examAttemptDao.observeAllAttempts()
    val allTopperSetProgress: Flow<List<TopperSetProgressEntity>> = topperDao.observeAllSetProgress()
    val activeTopperMistakes: Flow<List<TopperMistakeEntity>> = topperDao.observeActiveMistakes()

    suspend fun ensureSeeded() {
        val targetRecords = WbbseMasterDataSeed.createInitialRecords()
        val existingBuiltInCount = masterDataDao.countBuiltIn()
        if (existingBuiltInCount != targetRecords.size) {
            val bookmarkedKeys = masterDataDao.getBookmarkedBuiltIn()
                .map { "${it.subjectId}|${it.topicTitle}" }
                .toSet()
            if (existingBuiltInCount > 0) {
                masterDataDao.deleteAllBuiltIn()
            }
            val merged = if (bookmarkedKeys.isEmpty()) {
                targetRecords
            } else {
                targetRecords.map { item ->
                    val key = "${item.subjectId}|${item.topicTitle}"
                    if (key in bookmarkedKeys) item.copy(isBookmarked = true) else item
                }
            }
            masterDataDao.insertAll(merged)
        }
    }

    suspend fun getMasterDataSnapshot(subjectId: String?): List<MasterDataEntity> {
        return if (subjectId == null) {
            masterDataDao.getAllSnapshot()
        } else {
            masterDataDao.getBySubjectSnapshot(subjectId)
        }
    }

    suspend fun toggleBookmark(id: Long, bookmarked: Boolean) {
        masterDataDao.updateBookmark(id, bookmarked)
    }

    suspend fun addCustomStudyNote(note: MasterDataEntity) {
        masterDataDao.insert(note.copy(isUserCreated = true))
    }

    suspend fun deleteCustomStudyNote(id: Long) {
        masterDataDao.deleteUserNote(id)
    }

    suspend fun saveChatMessage(message: ChatMessageEntity): Long {
        return chatMessageDao.insertMessage(message)
    }

    suspend fun clearChatHistory() {
        chatMessageDao.clearAll()
    }

    suspend fun saveExamAttempt(attempt: ExamAttemptEntity): Long {
        return examAttemptDao.insertAttempt(attempt)
    }

    suspend fun clearExamHistory() {
        examAttemptDao.clearAllAttempts()
    }

    suspend fun getTopperTrackProgress(trackId: String): List<TopperSetProgressEntity> {
        return topperDao.getProgressForTrack(trackId)
    }

    suspend fun saveTopperSetProgress(progress: TopperSetProgressEntity): Long {
        return topperDao.insertSetProgress(progress)
    }

    suspend fun getActiveMistakesForTrack(trackId: String?): List<TopperMistakeEntity> {
        return if (trackId == null) {
            topperDao.getAllActiveMistakes()
        } else {
            topperDao.getActiveMistakesForTrack(trackId)
        }
    }

    suspend fun saveTopperMistakes(mistakes: List<TopperMistakeEntity>) {
        if (mistakes.isNotEmpty()) {
            topperDao.insertMistakes(mistakes)
        }
    }

    suspend fun resolveTopperMistakes(questionIds: List<String>) {
        if (questionIds.isNotEmpty()) {
            topperDao.markMistakesResolved(questionIds)
        }
    }

    suspend fun clearTopperProgress() {
        topperDao.clearSetProgress()
        topperDao.clearMistakes()
    }
}
