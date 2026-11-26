package com.example.model

enum class VaultMediaType(
    val labelBn: String,
    val badgeLabel: String
) {
    PHOTO("ছবি (Photo)", "📷 Photo"),
    DIAGRAM("ডায়াগ্রাম (Diagram)", "🎨 Diagram"),
    PDF("PDF নোট", "📄 PDF")
}

data class MediaVaultItem(
    val id: String,
    val filePath: String,
    val fileName: String,
    val displayTitle: String,
    val mediaType: VaultMediaType,
    val subject: WbbseSubject?,
    val sourceLabel: String,
    val sizeBytes: Long,
    val lastModifiedMs: Long,
    val isUnusedOrOrphan: Boolean = false
)

data class SubjectMediaVaultUiState(
    val isOpen: Boolean = false,
    val isLoading: Boolean = false,
    val selectedSubject: WbbseSubject? = null, // null = All subjects
    val selectedTypeFilter: VaultMediaType? = null, // null = All types
    val items: List<MediaVaultItem> = emptyList(),
    val totalStorageBytes: Long = 0L,
    val unusedFilesCount: Int = 0,
    val unusedStorageBytes: Long = 0L,
    val statusMessage: String? = null
)

data class EncryptedBackupStats(
    val fileSizeBytes: Long,
    val totalChatMessages: Int,
    val chatSectionsCovered: Int,
    val examAttemptsCount: Int,
    val completedChaptersCount: Int,
    val studyNotesAndBookmarksCount: Int,
    val streakDays: Int,
    val exportedFileName: String = ""
)

data class RecoveryKeyBackupUiState(
    val isOpen: Boolean = false,
    val activeTabIndex: Int = 0, // 0 = Key & Dual Backup, 1 = Restore Data with Key
    val secretRecoveryKey: String = "",
    val restoreKeyInput: String = "",
    val isWorking: Boolean = false,
    val statusMessage: String? = null,
    val isErrorStatus: Boolean = false,
    val lastBackupStats: EncryptedBackupStats? = null,
    val lastRestoreStats: EncryptedBackupStats? = null
)
