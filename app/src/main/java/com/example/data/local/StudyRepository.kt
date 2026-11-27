package com.example.data.local

import com.example.model.ChatSection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class StudyRepository(
    private val masterDataDao: MasterDataDao,
    private val chatMessageDao: ChatMessageDao,
    private val examAttemptDao: ExamAttemptDao,
    private val topperDao: TopperDao,
    private val dailyPlannerDao: DailyPlannerDao,
    private val masterUpgradeDao: MasterUpgradeDao
) {
    val allMasterNotes: Flow<List<MasterDataEntity>> = masterDataDao.observeAll()
    val allChatMessages: Flow<List<ChatMessageEntity>> =
        chatMessageDao.observeMessagesBySection(ChatSection.MAIN_CHAT.id)
    val chatMessageCount: Flow<Int> = chatMessageDao.observeCount()
    val allExamAttempts: Flow<List<ExamAttemptEntity>> = examAttemptDao.observeAllAttempts()
    val allTopperSetProgress: Flow<List<TopperSetProgressEntity>> = topperDao.observeAllSetProgress()
    val activeTopperMistakes: Flow<List<TopperMistakeEntity>> = topperDao.observeActiveMistakes()
    val studyStreak: Flow<StudyStreakEntity?> = dailyPlannerDao.observeStreak()
    val allBrahmastramProgress: Flow<List<BrahmastramProgressEntity>> =
        masterUpgradeDao.observeAllBrahmastramProgress()
    val allSpacedRevisions: Flow<List<SpacedRevisionEntity>> =
        masterUpgradeDao.observeAllRevisions()

    suspend fun ensureSeeded() = withContext(Dispatchers.IO) {
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

    fun observeSectionMessages(section: ChatSection): Flow<List<ChatMessageEntity>> =
        chatMessageDao.observeMessagesBySection(section.id)

    fun observeRawSectionMessages(rawSectionId: String): Flow<List<ChatMessageEntity>> =
        chatMessageDao.observeMessagesBySection(rawSectionId)

    suspend fun getSectionMessagesSnapshot(section: ChatSection): List<ChatMessageEntity> =
        chatMessageDao.getMessagesBySectionSnapshot(section.id)

    suspend fun getRawSectionMessagesSnapshot(rawSectionId: String): List<ChatMessageEntity> =
        chatMessageDao.getMessagesBySectionSnapshot(rawSectionId)

    fun observeDateStudyMessages(): Flow<List<ChatMessageEntity>> =
        chatMessageDao.observeDateStudyMessages()

    suspend fun getAllChatMessagesSnapshot(): List<ChatMessageEntity> =
        chatMessageDao.getAllMessagesSnapshot()

    suspend fun saveChatMessage(message: ChatMessageEntity): Long {
        return chatMessageDao.insertMessage(message)
    }

    suspend fun updateChatDiagramPath(messageId: Long, diagramPath: String?) {
        chatMessageDao.updateDiagramPath(messageId, diagramPath)
    }

    suspend fun clearMediaPathReferences(filePath: String) {
        chatMessageDao.clearImagePathReference(filePath)
        chatMessageDao.clearDiagramPathReference(filePath)
    }

    suspend fun clearSectionChat(section: ChatSection) {
        chatMessageDao.clearSection(section.id)
    }

    suspend fun clearRawSectionChat(rawSectionId: String) {
        chatMessageDao.clearSection(rawSectionId)
    }

    suspend fun clearChatHistory() {
        chatMessageDao.clearSection(ChatSection.MAIN_CHAT.id)
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

    fun observeDailyPlanTasks(dateStamp: String): Flow<List<DailyPlanTaskEntity>> =
        dailyPlannerDao.observeTasksForDate(dateStamp)

    suspend fun getDailyPlanTasks(dateStamp: String): List<DailyPlanTaskEntity> =
        dailyPlannerDao.getTasksForDate(dateStamp)

    suspend fun replaceDailyPlanTasks(dateStamp: String, tasks: List<DailyPlanTaskEntity>) {
        dailyPlannerDao.clearTasksForDate(dateStamp)
        if (tasks.isNotEmpty()) {
            dailyPlannerDao.insertTasks(tasks)
        }
    }

    suspend fun setDailyTaskCompleted(taskId: Long, completed: Boolean) {
        dailyPlannerDao.updateTaskCompleted(taskId, completed)
    }

    suspend fun getStudyStreak(): StudyStreakEntity =
        dailyPlannerDao.getStreak() ?: StudyStreakEntity()

    suspend fun saveStudyStreak(streak: StudyStreakEntity) {
        dailyPlannerDao.upsertStreak(streak)
    }

    // ==================== Brahmastram & Spaced Repetition ====================

    suspend fun getBrahmastramProgress(subjectId: String): BrahmastramProgressEntity? =
        masterUpgradeDao.getBrahmastramProgress(subjectId)

    suspend fun upsertBrahmastramProgress(progress: BrahmastramProgressEntity) =
        masterUpgradeDao.upsertBrahmastramProgress(progress)

    suspend fun scheduleOrTouchSpacedRevision(
        subjectId: String,
        chapterName: String,
        topicTitle: String,
        keySummary: String,
        sourceType: String,
        makeImmediatelyDue: Boolean = false
    ) {
        val now = System.currentTimeMillis()
        val existing = masterUpgradeDao.findRevisionByTopic(subjectId, topicTitle)
        if (existing != null) {
            val nextReview = if (makeImmediatelyDue) now else existing.nextReviewAtMs
            masterUpgradeDao.upsertRevision(
                existing.copy(
                    keySummary = keySummary.ifBlank { existing.keySummary },
                    sourceType = if (sourceType == "MISTAKE") "MISTAKE" else existing.sourceType,
                    nextReviewAtMs = nextReview
                )
            )
        } else {
            val firstDue = if (makeImmediatelyDue) now else now + (3L * 24L * 60L * 60L * 1000L)
            masterUpgradeDao.upsertRevision(
                SpacedRevisionEntity(
                    subjectId = subjectId,
                    chapterName = chapterName,
                    topicTitle = topicTitle,
                    keySummary = keySummary,
                    sourceType = sourceType,
                    intervalDays = 3,
                    masteryStage = 0,
                    lastReviewedAtMs = now,
                    nextReviewAtMs = firstDue
                )
            )
        }
    }

    suspend fun completeSpacedRevisionReview(item: SpacedRevisionEntity) {
        val now = System.currentTimeMillis()
        val nextStage = (item.masteryStage + 1).coerceAtMost(3)
        val nextIntervalDays = when (nextStage) {
            1 -> 7
            2 -> 21
            else -> 30
        }
        val nextDue = now + (nextIntervalDays.toLong() * 24L * 60L * 60L * 1000L)
        masterUpgradeDao.upsertRevision(
            item.copy(
                masteryStage = nextStage,
                intervalDays = nextIntervalDays,
                lastReviewedAtMs = now,
                nextReviewAtMs = nextDue
            )
        )
    }

    suspend fun getAllRevisionsSnapshot(): List<SpacedRevisionEntity> =
        masterUpgradeDao.getAllRevisionsSnapshot()

    // ==================== Full Progress Backup & Restore Snapshots ====================

    suspend fun getBackupExamAttempts(): List<ExamAttemptEntity> = examAttemptDao.getAllAttemptsSnapshot()
    suspend fun getBackupTopperProgress(): List<TopperSetProgressEntity> = topperDao.getAllSetProgressSnapshot()
    suspend fun getBackupTopperMistakes(): List<TopperMistakeEntity> = topperDao.getAllMistakesSnapshot()
    suspend fun getBackupDailyTasks(): List<DailyPlanTaskEntity> = dailyPlannerDao.getAllTasksSnapshot()
    suspend fun getBackupBrahmastram(): List<BrahmastramProgressEntity> =
        masterUpgradeDao.getAllBrahmastramProgressSnapshot()
    suspend fun getBackupRevisions(): List<SpacedRevisionEntity> =
        masterUpgradeDao.getAllRevisionsSnapshot()

    suspend fun restoreFullProgressBackup(
        examAttempts: List<ExamAttemptEntity>,
        topperProgress: List<TopperSetProgressEntity>,
        topperMistakes: List<TopperMistakeEntity>,
        streak: StudyStreakEntity?,
        dailyTasks: List<DailyPlanTaskEntity>,
        brahmastramProgress: List<BrahmastramProgressEntity>,
        spacedRevisions: List<SpacedRevisionEntity>
    ) {
        examAttemptDao.clearAllAttempts()
        if (examAttempts.isNotEmpty()) examAttemptDao.insertAttempts(examAttempts)

        topperDao.clearSetProgress()
        if (topperProgress.isNotEmpty()) topperDao.insertSetProgressList(topperProgress)

        topperDao.clearMistakes()
        if (topperMistakes.isNotEmpty()) topperDao.insertMistakes(topperMistakes)

        if (streak != null) dailyPlannerDao.upsertStreak(streak)

        dailyPlannerDao.clearAllTasks()
        if (dailyTasks.isNotEmpty()) dailyPlannerDao.insertTasks(dailyTasks)

        masterUpgradeDao.clearBrahmastramProgress()
        if (brahmastramProgress.isNotEmpty()) masterUpgradeDao.insertBrahmastramProgressList(brahmastramProgress)

        masterUpgradeDao.clearRevisions()
        if (spacedRevisions.isNotEmpty()) masterUpgradeDao.insertRevisions(spacedRevisions)
    }

    suspend fun getBackupUserNotes(): List<MasterDataEntity> =
        masterDataDao.getUserCreatedNotesSnapshot()

    suspend fun getBackupBookmarkedBuiltInKeys(): List<Pair<String, String>> =
        masterDataDao.getBookmarkedBuiltIn().map { it.subjectId to it.topicTitle }

    suspend fun restoreCompleteEncryptedBackup(
        chatMessages: List<ChatMessageEntity>,
        userNotes: List<MasterDataEntity>,
        bookmarkedBuiltInKeys: List<Pair<String, String>>,
        examAttempts: List<ExamAttemptEntity>,
        topperProgress: List<TopperSetProgressEntity>,
        topperMistakes: List<TopperMistakeEntity>,
        streak: StudyStreakEntity?,
        dailyTasks: List<DailyPlanTaskEntity>,
        brahmastramProgress: List<BrahmastramProgressEntity>,
        spacedRevisions: List<SpacedRevisionEntity>
    ) {
        chatMessageDao.clearAll()
        if (chatMessages.isNotEmpty()) {
            chatMessageDao.insertMessages(chatMessages.map { it.copy(id = 0) })
        }

        masterDataDao.deleteAllUserCreatedNotes()
        if (userNotes.isNotEmpty()) {
            masterDataDao.insertAll(userNotes.map { it.copy(id = 0, isUserCreated = true) })
        }

        masterDataDao.clearBuiltInBookmarks()
        bookmarkedBuiltInKeys.forEach { (subjectId, topicTitle) ->
            masterDataDao.bookmarkBySubjectAndTitle(subjectId, topicTitle)
        }

        restoreFullProgressBackup(
            examAttempts = examAttempts,
            topperProgress = topperProgress,
            topperMistakes = topperMistakes,
            streak = streak,
            dailyTasks = dailyTasks,
            brahmastramProgress = brahmastramProgress,
            spacedRevisions = spacedRevisions
        )
    }
}
