package com.example.model

import androidx.annotation.DrawableRes
import com.example.R

/**
 * 5 selectable Android Home Screen Launcher Disguise presets backed by
 * `<activity-alias>` declarations in `AndroidManifest.xml`.
 */
enum class LauncherDisguisePreset(
    val id: String,
    val aliasClassName: String,
    val launcherLabel: String,
    val bengaliName: String,
    val accentHex: Long,
    @DrawableRes val iconDrawableRes: Int?
) {
    DEFAULT_EDUAI(
        id = "default_eduai",
        aliasClassName = "com.example.LauncherDefault",
        launcherLabel = "EduAI Class 10",
        bengaliName = "ডিফল্ট EduAI",
        accentHex = 0xFF1D4ED8L,
        iconDrawableRes = null
    ),
    CALCULATOR(
        id = "calculator",
        aliasClassName = "com.example.LauncherCalculator",
        launcherLabel = "Calculator",
        bengaliName = "ক্যালকুলেটর",
        accentHex = 0xFF1E293BL,
        iconDrawableRes = R.drawable.ic_disguise_calculator
    ),
    MY_NOTES(
        id = "my_notes",
        aliasClassName = "com.example.LauncherNotes",
        launcherLabel = "My Notes",
        bengaliName = "মাই নোটস",
        accentHex = 0xFFD97706L,
        iconDrawableRes = R.drawable.ic_disguise_notes
    ),
    CALENDAR(
        id = "calendar",
        aliasClassName = "com.example.LauncherCalendar",
        launcherLabel = "Calendar",
        bengaliName = "ক্যালেন্ডার",
        accentHex = 0xFF1D4ED8L,
        iconDrawableRes = R.drawable.ic_disguise_calendar
    ),
    PERSONAL_VAULT(
        id = "personal_vault",
        aliasClassName = "com.example.LauncherVault",
        launcherLabel = "Personal Vault",
        bengaliName = "পার্সোনাল ভল্ট",
        accentHex = 0xFF065F46L,
        iconDrawableRes = R.drawable.ic_disguise_vault
    );

    companion object {
        fun fromId(id: String?): LauncherDisguisePreset =
            entries.firstOrNull { it.id == id } ?: DEFAULT_EDUAI
    }
}

data class AppBrandingUiState(
    val activeDisguisePreset: LauncherDisguisePreset = LauncherDisguisePreset.DEFAULT_EDUAI,
    val customAppTitle: String = "",
    val customLogoPath: String? = null,
    val logoVersion: Long = 0L,
    val statusMessage: String? = null
) {
    /**
     * Effective visible app title across Home header, Splash, Settings, Lock screen,
     * and Android Recent Apps (`TaskDescription`).
     * - If the student entered a custom name, that name always wins everywhere.
     * - Otherwise, if a Disguise Preset is active, its disguised launcher label is used
     *   so zero default EduAI branding leaks.
     */
    val effectiveAppTitle: String
        get() = customAppTitle.trim().ifBlank { activeDisguisePreset.launcherLabel }

    val hasCustomTitle: Boolean
        get() = customAppTitle.isNotBlank()

    val hasCustomLogo: Boolean
        get() = !customLogoPath.isNullOrBlank()
}

data class AppLockUiState(
    val isLockEnabled: Boolean = false,
    val hasPinConfigured: Boolean = false,
    val isBiometricEnabled: Boolean = false,
    val isBiometricHardwareAvailable: Boolean = false,
    val isLocked: Boolean = false,
    val failedAttempts: Int = 0,
    val lockoutUntilEpochMs: Long = 0L,
    val errorMessage: String? = null,
    val statusMessage: String? = null
) {
    val isTemporarilyLockedOut: Boolean
        get() = lockoutUntilEpochMs > System.currentTimeMillis()
}
