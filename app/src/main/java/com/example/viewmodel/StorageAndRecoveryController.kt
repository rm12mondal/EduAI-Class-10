package com.example.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.data.backup.SecretRecoveryBackupManager
import com.example.data.local.SettingsDataStore
import com.example.data.local.StudyRepository
import com.example.data.media.MediaVaultManager
import com.example.model.MediaVaultItem
import com.example.model.RecoveryKeyBackupUiState
import com.example.model.SubjectMediaVaultUiState
import com.example.model.VaultMediaType
import com.example.model.WbbseSubject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Coordinates:
 * 1) Subject-Wise Media & PDF Vault (Photos, AI Diagrams, Saved PDFs across all chat sections)
 * 2) Secret Recovery Key & Dual Encrypted `.edukey` Backup / Restore (Local Storage + Google Drive)
 */
class StorageAndRecoveryController(
    private val appContext: Context,
    private val scope: CoroutineScope,
    private val studyRepository: StudyRepository,
    private val settingsDataStore: SettingsDataStore,
    private val onAfterPdfChanged: () -> Unit,
    private val onAfterEncryptedRestore: suspend () -> Unit
) {
    private val _mediaVaultState = MutableStateFlow(SubjectMediaVaultUiState())
    val mediaVaultState: StateFlow<SubjectMediaVaultUiState> = _mediaVaultState.asStateFlow()

    private val _recoveryState = MutableStateFlow(RecoveryKeyBackupUiState())
    val recoveryState: StateFlow<RecoveryKeyBackupUiState> = _recoveryState.asStateFlow()

    init {
        scope.launch {
            val key = settingsDataStore.getOrCreateSecretRecoveryKey()
            _recoveryState.update { it.copy(secretRecoveryKey = key) }
        }
        scope.launch {
            settingsDataStore.settingsFlow.collect { stored ->
                if (stored.secretRecoveryKey.isNotBlank()) {
                    _recoveryState.update { it.copy(secretRecoveryKey = stored.secretRecoveryKey) }
                }
            }
        }
    }

    // ==================== 1. Subject-Wise Media & PDF Vault ====================

    fun openMediaVault(initialSubject: WbbseSubject? = null) {
        _mediaVaultState.update {
            it.copy(
                isOpen = true,
                selectedSubject = initialSubject,
                statusMessage = null
            )
        }
        refreshMediaVault()
    }

    fun closeMediaVault() {
        _mediaVaultState.update { it.copy(isOpen = false, statusMessage = null) }
    }

    fun selectVaultSubject(subject: WbbseSubject?) {
        _mediaVaultState.update { it.copy(selectedSubject = subject) }
    }

    fun selectVaultTypeFilter(type: VaultMediaType?) {
        _mediaVaultState.update { it.copy(selectedTypeFilter = type) }
    }

    fun clearVaultStatus() {
        _mediaVaultState.update { it.copy(statusMessage = null) }
    }

    fun refreshMediaVault() {
        scope.launch {
            _mediaVaultState.update { it.copy(isLoading = true) }
            val snapshot = MediaVaultManager.scanAllVaultItems(appContext, studyRepository)
            _mediaVaultState.update {
                it.copy(
                    isLoading = false,
                    items = snapshot.items,
                    totalStorageBytes = snapshot.totalStorageBytes,
                    unusedFilesCount = snapshot.unusedFilesCount,
                    unusedStorageBytes = snapshot.unusedStorageBytes
                )
            }
        }
    }

    fun deleteVaultItem(item: MediaVaultItem) {
        scope.launch {
            val ok = MediaVaultManager.deleteVaultItem(appContext, item, studyRepository)
            if (ok) {
                if (item.mediaType == VaultMediaType.PDF) {
                    onAfterPdfChanged()
                }
                val freedStr = MediaVaultManager.formatFileSize(item.sizeBytes)
                refreshMediaVault()
                _mediaVaultState.update {
                    it.copy(statusMessage = "🗑️ মুছে ফেলা হয়েছে: ${item.displayTitle} ($freedStr খালি হলো)")
                }
            } else {
                _mediaVaultState.update {
                    it.copy(statusMessage = "ফাইলটি মোছা যায়নি। আবার চেষ্টা করো।")
                }
            }
        }
    }

    fun clearUnusedVaultFiles() {
        scope.launch {
            val currentItems = _mediaVaultState.value.items
            val res = MediaVaultManager.clearUnusedFiles(appContext, currentItems, studyRepository)
            refreshMediaVault()
            val msg = if (res.deletedCount > 0) {
                "🧹 ${res.deletedCount}টি অব্যবহৃত/ক্যাশ ফাইল মোছা হয়েছে (${MediaVaultManager.formatFileSize(res.freedBytes)} খালি হলো)!"
            } else {
                "✨ কোনো অব্যবহৃত ফাইল নেই—তোমার স্টোরেজ ইতিমধ্যেই পরিষ্কার!"
            }
            _mediaVaultState.update { it.copy(statusMessage = msg) }
        }
    }

    fun clearFilteredSubjectFiles() {
        scope.launch {
            val st = _mediaVaultState.value
            val targets = st.items.filter { item ->
                val subjectMatches = st.selectedSubject == null || item.subject == st.selectedSubject
                val typeMatches = st.selectedTypeFilter == null || item.mediaType == st.selectedTypeFilter
                subjectMatches && typeMatches
            }
            if (targets.isEmpty()) {
                _mediaVaultState.update { it.copy(statusMessage = "মোছার মতো কোনো ফাইল পাওয়া যায়নি।") }
                return@launch
            }
            val hadPdf = targets.any { it.mediaType == VaultMediaType.PDF }
            val res = MediaVaultManager.clearItemsBatch(appContext, targets, studyRepository)
            if (hadPdf) onAfterPdfChanged()
            refreshMediaVault()
            val scopeLabel = st.selectedSubject?.bengaliName ?: "নির্বাচিত"
            _mediaVaultState.update {
                it.copy(
                    statusMessage = "🗑️ $scopeLabel অংশের ${res.deletedCount}টি ফাইল মোছা হয়েছে (${MediaVaultManager.formatFileSize(res.freedBytes)} খালি হলো)!"
                )
            }
        }
    }

    // ==================== 2. Secret Recovery Key & Dual Encrypted Backup ====================

    fun openRecoveryBackup(initialTabIndex: Int = 0) {
        _recoveryState.update {
            it.copy(
                isOpen = true,
                activeTabIndex = initialTabIndex.coerceIn(0, 1),
                statusMessage = null,
                isErrorStatus = false
            )
        }
        scope.launch {
            val key = settingsDataStore.getOrCreateSecretRecoveryKey()
            _recoveryState.update { it.copy(secretRecoveryKey = key) }
        }
    }

    fun closeRecoveryBackup() {
        _recoveryState.update { it.copy(isOpen = false, statusMessage = null) }
    }

    fun selectRecoveryTab(tabIndex: Int) {
        _recoveryState.update {
            it.copy(
                activeTabIndex = tabIndex.coerceIn(0, 1),
                statusMessage = null,
                isErrorStatus = false
            )
        }
    }

    fun updateRestoreKeyInput(raw: String) {
        _recoveryState.update { it.copy(restoreKeyInput = raw.uppercase()) }
    }

    fun clearRecoveryStatus() {
        _recoveryState.update { it.copy(statusMessage = null, isErrorStatus = false) }
    }

    fun exportEncryptedBackupToLocalUri(uri: Uri) {
        scope.launch {
            val key = _recoveryState.value.secretRecoveryKey.ifBlank {
                settingsDataStore.getOrCreateSecretRecoveryKey()
            }
            _recoveryState.update { it.copy(isWorking = true, statusMessage = null, isErrorStatus = false) }
            val result = SecretRecoveryBackupManager.exportEncryptedBackupToUri(
                context = appContext,
                uri = uri,
                secretKey = key,
                repository = studyRepository,
                settingsDataStore = settingsDataStore
            )
            result.onSuccess { stats ->
                val sizeStr = MediaVaultManager.formatFileSize(stats.fileSizeBytes)
                _recoveryState.update {
                    it.copy(
                        isWorking = false,
                        secretRecoveryKey = key,
                        lastBackupStats = stats,
                        isErrorStatus = false,
                        statusMessage = "✅ এনক্রিপ্টেড .edukey ব্যাকআপ ফোনের স্টোরেজে সেভ হয়েছে! ($sizeStr • ${stats.totalChatMessages}টি চ্যাট, ${stats.examAttemptsCount}টি পরীক্ষা, ${stats.studyNotesAndBookmarksCount}টি নোট)"
                    )
                }
            }.onFailure { err ->
                _recoveryState.update {
                    it.copy(
                        isWorking = false,
                        isErrorStatus = true,
                        statusMessage = "ব্যাকআপ সেভ করতে সমস্যা হয়েছে: ${err.localizedMessage ?: "আবার চেষ্টা করো"}"
                    )
                }
            }
        }
    }

    fun exportEncryptedBackupForGoogleDrive(onLaunchShareIntent: (Intent) -> Unit) {
        scope.launch {
            val key = _recoveryState.value.secretRecoveryKey.ifBlank {
                settingsDataStore.getOrCreateSecretRecoveryKey()
            }
            _recoveryState.update { it.copy(isWorking = true, statusMessage = null, isErrorStatus = false) }
            val result = SecretRecoveryBackupManager.createEncryptedBackupFileForDrive(
                context = appContext,
                secretKey = key,
                repository = studyRepository,
                settingsDataStore = settingsDataStore
            )
            result.onSuccess { (file, stats) ->
                val sizeStr = MediaVaultManager.formatFileSize(stats.fileSizeBytes)
                val shareIntent = SecretRecoveryBackupManager.buildGoogleDriveShareIntent(appContext, file)
                _recoveryState.update {
                    it.copy(
                        isWorking = false,
                        secretRecoveryKey = key,
                        lastBackupStats = stats,
                        isErrorStatus = false,
                        statusMessage = "☁️ Google Drive-এ সেভ করার জন্য ${file.name} ($sizeStr) প্রস্তুত! ওপেন হওয়া শিট থেকে 'Save' চাপো।"
                    )
                }
                onLaunchShareIntent(shareIntent)
            }.onFailure { err ->
                _recoveryState.update {
                    it.copy(
                        isWorking = false,
                        isErrorStatus = true,
                        statusMessage = "Google Drive ব্যাকআপ তৈরি ব্যর্থ: ${err.localizedMessage ?: "আবার চেষ্টা করো"}"
                    )
                }
            }
        }
    }

    fun restoreEncryptedBackupFromUri(uri: Uri) {
        val enteredKey = _recoveryState.value.restoreKeyInput.trim()
        if (enteredKey.length < 8) {
            _recoveryState.update {
                it.copy(
                    isErrorStatus = true,
                    statusMessage = "⚠️ আগে তোমার Secret Recovery Key (যেমন: EDU10-XXXX-XXXX) উপরের বক্সে লেখো!"
                )
            }
            return
        }

        scope.launch {
            _recoveryState.update { it.copy(isWorking = true, statusMessage = null, isErrorStatus = false) }
            val result = SecretRecoveryBackupManager.restoreEncryptedBackupFromUri(
                context = appContext,
                uri = uri,
                enteredKey = enteredKey,
                repository = studyRepository,
                settingsDataStore = settingsDataStore
            )
            result.onSuccess { stats ->
                onAfterEncryptedRestore()
                _recoveryState.update {
                    it.copy(
                        isWorking = false,
                        lastRestoreStats = stats,
                        isErrorStatus = false,
                        statusMessage = "🎉 ডেটা সম্পূর্ণভাবে ফিরে এসেছে! (${stats.totalChatMessages}টি চ্যাট, ${stats.examAttemptsCount}টি পরীক্ষা, ${stats.completedChaptersCount}টি অধ্যায়, ${stats.studyNotesAndBookmarksCount}টি নোট ও বুকমার্ক)"
                    )
                }
            }.onFailure { err ->
                _recoveryState.update {
                    it.copy(
                        isWorking = false,
                        isErrorStatus = true,
                        statusMessage = "❌ রিস্টোর ব্যর্থ: ${err.localizedMessage ?: "ভুল রিকভারি কী অথবা অকার্যকর .edukey ফাইল।"}"
                    )
                }
            }
        }
    }
}
