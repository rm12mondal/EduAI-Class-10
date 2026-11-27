package com.example.viewmodel

import android.content.Context
import android.net.Uri
import com.example.data.branding.AppBrandingManager
import com.example.data.local.SettingsDataStore
import com.example.data.security.AppLockSecureStorage
import com.example.data.security.BiometricUnlockHelper
import com.example.data.security.PinVerifyOutcome
import com.example.model.AppBrandingUiState
import com.example.model.AppLockUiState
import com.example.model.LauncherDisguisePreset
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Coordinates:
 * 1) Home Screen Launcher Icon & Name Disguise (`<activity-alias>` switcher)
 * 2) Complete In-App Logo & Title Rebranding (Custom Gallery Logo + Custom App Name)
 * 3) Strict App Lock (4-Digit PIN via EncryptedSharedPreferences + Biometric Unlock + ON_START enforcement)
 */
class BrandingAndLockController(
    private val appContext: Context,
    private val scope: CoroutineScope,
    private val settingsDataStore: SettingsDataStore
) {
    private val lockStorage = AppLockSecureStorage(appContext)

    private val _brandingState = MutableStateFlow(AppBrandingUiState())
    val brandingState: StateFlow<AppBrandingUiState> = _brandingState.asStateFlow()

    private val _appLockState = MutableStateFlow(AppLockUiState())
    val appLockState: StateFlow<AppLockUiState> = _appLockState.asStateFlow()

    // Prevents locking on the very first ON_START after Activity creation
    // or right when returning from an in-app system photo/file picker.
    private var hasHandledInitialStart = false
    private var suppressNextStartLockOnce = false

    @Volatile
    private var hasCompletedInitialLockWarmup = false

    init {
        // Warm EncryptedSharedPreferences, Biometric hardware status, and Launcher alias strictly on Dispatchers.IO
        scope.launch(Dispatchers.IO) {
            val snap = lockStorage.readSnapshotAsync()
            val bioAvailable = BiometricUnlockHelper.isBiometricAvailable(appContext)
            val detectedPreset = AppBrandingManager.detectActiveLauncherPreset(appContext)
            hasCompletedInitialLockWarmup = true
            _appLockState.update { current ->
                current.copy(
                    isLockEnabled = snap.isLockEnabled,
                    hasPinConfigured = snap.hasPinConfigured,
                    isBiometricEnabled = snap.isBiometricEnabled,
                    isBiometricHardwareAvailable = bioAvailable,
                    isLocked = snap.isLockEnabled,
                    failedAttempts = snap.failedAttempts,
                    lockoutUntilEpochMs = snap.lockoutUntilEpochMs
                )
            }
            _brandingState.update { current ->
                if (current.activeDisguisePreset == LauncherDisguisePreset.DEFAULT_EDUAI && detectedPreset != LauncherDisguisePreset.DEFAULT_EDUAI) {
                    current.copy(activeDisguisePreset = detectedPreset)
                } else {
                    current
                }
            }
        }

        scope.launch {
            settingsDataStore.settingsFlow.collect { stored ->
                val resolvedPreset = withContext(Dispatchers.IO) {
                    // Warm custom logo cache on IO before exposing path to Compose
                    if (!stored.customLogoPath.isNullOrBlank()) {
                        AppBrandingManager.decodeCustomLogoBitmap(stored.customLogoPath)
                    }
                    if (stored.launcherDisguiseId.isNotBlank()) {
                        LauncherDisguisePreset.fromId(stored.launcherDisguiseId)
                    } else {
                        AppBrandingManager.detectActiveLauncherPreset(appContext)
                    }
                }
                _brandingState.update { current ->
                    current.copy(
                        activeDisguisePreset = resolvedPreset,
                        customAppTitle = stored.customAppTitle,
                        customLogoPath = stored.customLogoPath
                    )
                }
            }
        }
    }

    // ==================== 1. Home Screen Launcher Disguise ====================

    fun selectLauncherDisguise(preset: LauncherDisguisePreset) {
        scope.launch {
            val ok = withContext(Dispatchers.IO) {
                AppBrandingManager.switchLauncherAlias(appContext, preset)
            }
            if (ok) {
                settingsDataStore.setLauncherDisguiseId(preset.id)
                _brandingState.update {
                    it.copy(
                        activeDisguisePreset = preset,
                        statusMessage = "✅ হোম স্ক্রিন আইকন ও নাম '${preset.launcherLabel}'-এ পরিবর্তন হয়েছে! (লঞ্চার আপডেট হতে ২-৫ সেকেন্ড লাগতে পারে)"
                    )
                }
            } else {
                _brandingState.update {
                    it.copy(statusMessage = "লঞ্চার আইকন পরিবর্তন করা যায়নি। আবার চেষ্টা করো।")
                }
            }
        }
    }

    // ==================== 2. Complete In-App Logo & Title Rebranding ====================

    fun setCustomGalleryLogo(uri: Uri) {
        scope.launch {
            val savedPath = AppBrandingManager.saveCustomGalleryLogo(appContext, uri)
            if (savedPath != null) {
                settingsDataStore.setCustomLogoPath(savedPath)
                _brandingState.update {
                    it.copy(
                        customLogoPath = savedPath,
                        logoVersion = System.currentTimeMillis(),
                        statusMessage = "🎨 নতুন কাস্টম লোগো পুরো অ্যাপে (হেডার, স্প্ল্যাশ, D বাটন ও AI অ্যাভাটারে) চালু হয়েছে!"
                    )
                }
            } else {
                _brandingState.update {
                    it.copy(statusMessage = "ছবিটি লোগো হিসেবে সেভ করা যায়নি। অন্য একটি ছবি চেষ্টা করো।")
                }
            }
        }
    }

    fun resetCustomGalleryLogo() {
        scope.launch {
            AppBrandingManager.deleteCustomGalleryLogo(appContext)
            settingsDataStore.setCustomLogoPath(null)
            _brandingState.update {
                it.copy(
                    customLogoPath = null,
                    logoVersion = System.currentTimeMillis(),
                    statusMessage = "🔄 কাস্টম লোগো রিসেট করে ডিফল্ট লোগো ফিরিয়ে আনা হয়েছে।"
                )
            }
        }
    }

    fun setCustomAppTitle(rawTitle: String) {
        val cleaned = AppBrandingManager.sanitizeCustomTitle(rawTitle)
        if (cleaned.isEmpty()) {
            resetCustomAppTitle()
            return
        }
        scope.launch {
            settingsDataStore.setCustomAppTitle(cleaned)
            _brandingState.update {
                it.copy(
                    customAppTitle = cleaned,
                    statusMessage = "✏️ অ্যাপের নাম '$cleaned' হিসেবে সব স্ক্রিন ও Recent Apps-এ সেট হয়েছে!"
                )
            }
        }
    }

    fun resetCustomAppTitle() {
        scope.launch {
            settingsDataStore.setCustomAppTitle("")
            _brandingState.update {
                it.copy(
                    customAppTitle = "",
                    statusMessage = "🔄 অ্যাপের কাস্টম নাম রিসেট করা হয়েছে।"
                )
            }
        }
    }

    fun clearBrandingStatus() {
        _brandingState.update { it.copy(statusMessage = null) }
    }

    // ==================== 3. Strict App Lock (4-Digit PIN + Biometric) ====================

    fun refreshBiometricAvailability() {
        scope.launch(Dispatchers.IO) {
            val available = BiometricUnlockHelper.isBiometricAvailable(appContext)
            _appLockState.update { it.copy(isBiometricHardwareAvailable = available) }
        }
    }

    fun enableOrUpdatePin(newPin: String, enableBiometric: Boolean): Boolean {
        if (!AppLockSecureStorage.isValidFourDigitPin(newPin)) {
            _appLockState.update {
                it.copy(errorMessage = "ঠিক ৪টি সংখ্যার (0–9) PIN দাও।")
            }
            return false
        }
        scope.launch(Dispatchers.IO) {
            val bioAvailable = BiometricUnlockHelper.isBiometricAvailable(appContext)
            val canUseBio = enableBiometric && bioAvailable
            val saved = lockStorage.savePinAndEnable(newPin, canUseBio)
            if (saved) {
                val snap = lockStorage.readSnapshot()
                _appLockState.update {
                    it.copy(
                        isLockEnabled = snap.isLockEnabled,
                        hasPinConfigured = snap.hasPinConfigured,
                        isBiometricEnabled = snap.isBiometricEnabled,
                        isBiometricHardwareAvailable = bioAvailable,
                        isLocked = false,
                        failedAttempts = 0,
                        lockoutUntilEpochMs = 0L,
                        errorMessage = null,
                        statusMessage = "🔒 ৪-ডিজিট PIN অ্যাপ লক সফলভাবে চালু ও এনক্রিপ্টেড স্টোরেজে সেভ হয়েছে!"
                    )
                }
            }
        }
        return true
    }

    fun enableExistingPinLock(): Boolean {
        scope.launch(Dispatchers.IO) {
            val ok = lockStorage.enableLockIfPinExists()
            if (ok) {
                val snap = lockStorage.readSnapshot()
                _appLockState.update {
                    it.copy(
                        isLockEnabled = snap.isLockEnabled,
                        hasPinConfigured = snap.hasPinConfigured,
                        isBiometricEnabled = snap.isBiometricEnabled,
                        statusMessage = "🔒 অ্যাপ লক পুনরায় চালু হয়েছে!"
                    )
                }
            }
        }
        return true
    }

    fun disableAppLockWithPinVerification(currentPin: String): Boolean {
        return when (val outcome = lockStorage.verifyPin(currentPin)) {
            is PinVerifyOutcome.Success -> {
                lockStorage.disableLock()
                val snap = lockStorage.readSnapshot()
                _appLockState.update {
                    it.copy(
                        isLockEnabled = false,
                        hasPinConfigured = snap.hasPinConfigured,
                        isBiometricEnabled = false,
                        isLocked = false,
                        failedAttempts = 0,
                        lockoutUntilEpochMs = 0L,
                        errorMessage = null,
                        statusMessage = "🔓 অ্যাপ লক বন্ধ করা হয়েছে।"
                    )
                }
                true
            }
            is PinVerifyOutcome.Invalid -> {
                _appLockState.update {
                    it.copy(
                        failedAttempts = outcome.failedAttempts,
                        errorMessage = "ভুল PIN! বন্ধ করতে সঠিক ৪-ডিজিট PIN দাও।"
                    )
                }
                false
            }
            is PinVerifyOutcome.LockedOut -> {
                _appLockState.update {
                    it.copy(
                        lockoutUntilEpochMs = outcome.lockoutUntilEpochMs,
                        errorMessage = "অনেকবার ভুল PIN দেওয়া হয়েছে। ${outcome.remainingSeconds} সেকেন্ড পর চেষ্টা করো।"
                    )
                }
                false
            }
        }
    }

    fun setBiometricUnlockEnabled(enabled: Boolean) {
        scope.launch(Dispatchers.IO) {
            val available = BiometricUnlockHelper.isBiometricAvailable(appContext)
            if (enabled && !available) {
                _appLockState.update {
                    it.copy(
                        isBiometricHardwareAvailable = false,
                        statusMessage = "এই ফোনে ফিঙ্গারপ্রিন্ট বা ফেস লক সেটআপ করা নেই। ফোনের Settings থেকে আগে চালু করুন।"
                    )
                }
                return@launch
            }
            if (!_appLockState.value.isLockEnabled) {
                _appLockState.update {
                    it.copy(statusMessage = "আগে ৪-ডিজিট PIN দিয়ে অ্যাপ লক চালু করুন।")
                }
                return@launch
            }
            lockStorage.setBiometricEnabled(enabled)
            _appLockState.update {
                it.copy(
                    isBiometricEnabled = enabled,
                    isBiometricHardwareAvailable = available,
                    statusMessage = if (enabled) {
                        "👆 ফিঙ্গারপ্রিন্ট / বায়োমেট্রিক আনলক চালু হয়েছে!"
                    } else {
                        "বায়োমেট্রিক আনলক বন্ধ করা হয়েছে (এখন শুধু ৪-ডিজিট PIN লাগবে)।"
                    }
                )
            }
        }
    }

    fun submitUnlockPin(candidatePin: String): Boolean {
        return when (val outcome = lockStorage.verifyPin(candidatePin)) {
            is PinVerifyOutcome.Success -> {
                _appLockState.update {
                    it.copy(
                        isLocked = false,
                        failedAttempts = 0,
                        lockoutUntilEpochMs = 0L,
                        errorMessage = null
                    )
                }
                true
            }
            is PinVerifyOutcome.Invalid -> {
                val left = (AppLockSecureStorage.MAX_FAILED_ATTEMPTS - outcome.failedAttempts).coerceAtLeast(1)
                _appLockState.update {
                    it.copy(
                        failedAttempts = outcome.failedAttempts,
                        errorMessage = "ভুল PIN! আরও $left বার চেষ্টা বাকি আছে।"
                    )
                }
                false
            }
            is PinVerifyOutcome.LockedOut -> {
                _appLockState.update {
                    it.copy(
                        lockoutUntilEpochMs = outcome.lockoutUntilEpochMs,
                        errorMessage = "৫ বার ভুল PIN! নিরাপত্তার জন্য ${outcome.remainingSeconds} সেকেন্ড অপেক্ষা করো।"
                    )
                }
                false
            }
        }
    }

    fun onBiometricUnlockSuccess() {
        _appLockState.update {
            it.copy(
                isLocked = false,
                failedAttempts = 0,
                lockoutUntilEpochMs = 0L,
                errorMessage = null
            )
        }
    }

    fun onBiometricError(message: String) {
        _appLockState.update { it.copy(errorMessage = message) }
    }

    fun clearLockError() {
        _appLockState.update { it.copy(errorMessage = null) }
    }

    fun clearLockStatus() {
        _appLockState.update { it.copy(statusMessage = null) }
    }

    fun suppressNextStartLockForSystemPicker() {
        suppressNextStartLockOnce = true
    }

    /**
     * Enforces App Lock every time the Activity transitions to `ON_START`
     * (app opened from launcher, returned from background, Recent Apps, or screen unlock).
     */
    fun onActivityStarted() {
        refreshBiometricAvailability()
        if (!hasHandledInitialStart) {
            hasHandledInitialStart = true
            // Initial cold-start lock state was already set synchronously in constructor
            return
        }
        if (suppressNextStartLockOnce) {
            suppressNextStartLockOnce = false
            return
        }
        if (_appLockState.value.isLockEnabled) {
            _appLockState.update {
                it.copy(
                    isLocked = true,
                    errorMessage = null
                )
            }
        }
    }

    fun lockNowForTesting() {
        if (_appLockState.value.isLockEnabled) {
            _appLockState.update {
                it.copy(
                    isLocked = true,
                    errorMessage = null
                )
            }
        }
    }
}
