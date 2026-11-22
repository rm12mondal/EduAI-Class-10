package com.example.data.local

import kotlinx.coroutines.flow.Flow

class StudyRepository(
    private val masterDataDao: MasterDataDao,
    private val chatMessageDao: ChatMessageDao
) {
    val allMasterNotes: Flow<List<MasterDataEntity>> = masterDataDao.observeAll()
    val allChatMessages: Flow<List<ChatMessageEntity>> = chatMessageDao.observeAllMessages()
    val chatMessageCount: Flow<Int> = chatMessageDao.observeCount()

    suspend fun ensureSeeded() {
        if (masterDataDao.count() == 0) {
            masterDataDao.insertAll(WbbseMasterDataSeed.createInitialRecords())
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
}
