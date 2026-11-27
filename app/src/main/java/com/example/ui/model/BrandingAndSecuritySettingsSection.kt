package com.example.ui.model

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.branding.AppBrandingManager
import com.example.model.AppBrandingUiState
import com.example.model.AppLockUiState
import com.example.model.LauncherDisguisePreset
import com.example.ui.branding.BrandedAppLogoAvatar
import com.example.ui.branding.LocalAppBranding
import com.example.ui.security.PinDialogMode
import com.example.ui.security.PinSetupModalDialog
import com.example.ui.theme.OnlineCobalt
import com.example.ui.theme.SageOffline
import com.example.ui.theme.SageOfflineContainer

/**
 * Modular Settings cards for:
 * 1) Home Screen Launcher Icon & Name Disguise (5 `<activity-alias>` presets)
 * 2) Complete In-App Logo & Title Replacement (Gallery Photo Picker + Custom App Title + Reset)
 * 3) Strict App Lock (4-Digit PIN via EncryptedSharedPreferences + Biometric Unlock)
 */
@Composable
fun LauncherDisguiseSettingsCard(
    brandingState: AppBrandingUiState,
    onSelectPreset: (LauncherDisguisePreset) -> Unit,
    onDismissStatus: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.5.dp, Color(0xFF4F46E5).copy(alpha = 0.38f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("settings_launcher_disguise_card")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF4F46E5).copy(alpha = 0.14f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Apps,
                            contentDescription = null,
                            tint = Color(0xFF4F46E5),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "🎭 হোম স্ক্রিন আইকন ও নাম ছদ্মবেশ",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "যেকোনো প্রিসেটে ট্যাপ করলেই ফোনের আসল লঞ্চার আইকন ও নাম বদলে যাবে",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            AnimatedVisibility(visible = !brandingState.statusMessage.isNullOrBlank()) {
                Surface(
                    color = SageOfflineContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = brandingState.statusMessage.orEmpty(),
                            style = MaterialTheme.typography.bodySmall,
                            color = SageOffline,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = onDismissStatus,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Close status",
                                tint = SageOffline,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(LauncherDisguisePreset.entries, key = { it.id }) { preset ->
                    val isSelected = brandingState.activeDisguisePreset == preset
                    val presetColor = Color(preset.accentHex)
                    Surface(
                        onClick = { onSelectPreset(preset) },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) {
                            presetColor.copy(alpha = 0.14f)
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerLow
                        },
                        border = BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) presetColor else MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier
                            .width(108.dp)
                            .minimumInteractiveComponentSize()
                            .testTag("disguise_preset_${preset.id}")
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box {
                                if (preset.iconDrawableRes != null) {
                                    Image(
                                        painter = painterResource(id = preset.iconDrawableRes),
                                        contentDescription = preset.launcherLabel,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(13.dp))
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(13.dp))
                                            .background(OnlineCobalt),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.School,
                                            contentDescription = preset.launcherLabel,
                                            tint = Color.White,
                                            modifier = Modifier.size(26.dp)
                                        )
                                    }
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Rounded.CheckCircle,
                                        contentDescription = "Active",
                                        tint = SageOffline,
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .size(18.dp)
                                            .clip(CircleShape)
                                            .background(Color.White)
                                    )
                                }
                            }
                            Text(
                                text = preset.launcherLabel,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = preset.bengaliName,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FullAppRebrandingSettingsCard(
    brandingState: AppBrandingUiState,
    onPrepareSystemPicker: () -> Unit,
    onPickCustomLogoUri: (Uri) -> Unit,
    onResetCustomLogo: () -> Unit,
    onSaveCustomAppTitle: (String) -> Unit,
    onResetCustomAppTitle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val resolvedBranding = LocalAppBranding.current
    var titleDraft by remember { mutableStateOf(brandingState.customAppTitle) }

    LaunchedEffect(brandingState.customAppTitle) {
        if (titleDraft != brandingState.customAppTitle) {
            titleDraft = brandingState.customAppTitle
        }
    }

    // Zero-permission Android Photo Picker for Custom Gallery Logo
    val galleryLogoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onPickCustomLogoUri(uri)
        }
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.5.dp, Color(0xFF0E7490).copy(alpha = 0.40f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("settings_full_rebranding_card")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                BrandedAppLogoAvatar(
                    size = 44.dp,
                    shape = RoundedCornerShape(13.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "🎨 সব লোগো ও ✏️ সব নাম পরিবর্তন করুন",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "বর্তমান নাম: ${resolvedBranding.effectiveTitle} • হেডার, স্প্ল্যাশ, D বাটন, AI অ্যাভাটার ও Recent Apps",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Part A: Custom Gallery Logo Picker + Reset
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Palette,
                            contentDescription = null,
                            tint = Color(0xFF0E7490),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "🎨 সব লোগো পরিবর্তন করুন (Custom Gallery Logo)",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "গ্যালারি থেকে যেকোনো ছবি বাছলে অ্যাপ হেডার, স্প্ল্যাশ স্ক্রিন, ফ্লোটিং \"D\" বাটন ও সব AI অ্যাভাটারে সেই ছবি দেখাবে।",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                onPrepareSystemPicker()
                                galleryLogoPicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0E7490)),
                            modifier = Modifier
                                .weight(1f)
                                .minimumInteractiveComponentSize()
                                .testTag("settings_pick_custom_logo_btn")
                        ) {
                            Icon(Icons.Rounded.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("গ্যালারি থেকে লোগো", maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }

                        OutlinedButton(
                            onClick = onResetCustomLogo,
                            enabled = brandingState.hasCustomLogo,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("settings_reset_custom_logo_btn")
                        ) {
                            Icon(Icons.Rounded.RestartAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reset Logo", maxLines = 1)
                        }
                    }
                }
            }

            // Part B: Custom App Title + Save + Reset
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = null,
                            tint = OnlineCobalt,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "✏️ সব নাম পরিবর্তন করুন (Custom App Title)",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    OutlinedTextField(
                        value = titleDraft,
                        onValueChange = {
                            if (it.length <= AppBrandingManager.MAX_CUSTOM_TITLE_LENGTH) {
                                titleDraft = it
                            }
                        },
                        singleLine = true,
                        label = { Text("নতুন অ্যাপের নাম লিখুন (যেমন: My Notes)") },
                        placeholder = { Text(brandingState.activeDisguisePreset.launcherLabel) },
                        supportingText = {
                            Text("${titleDraft.length}/${AppBrandingManager.MAX_CUSTOM_TITLE_LENGTH} অক্ষর • Home, Splash, Settings ও Recent Apps-এ বদলে যাবে")
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("settings_custom_app_title_input")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onSaveCustomAppTitle(titleDraft) },
                            enabled = titleDraft.trim().isNotEmpty(),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .minimumInteractiveComponentSize()
                                .testTag("settings_save_custom_title_btn")
                        ) {
                            Text("নাম সেভ করুন", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                titleDraft = ""
                                onResetCustomAppTitle()
                            },
                            enabled = brandingState.hasCustomTitle,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("settings_reset_custom_title_btn")
                        ) {
                            Icon(Icons.Rounded.RestartAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reset Name", maxLines = 1)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppLockSecuritySettingsCard(
    lockState: AppLockUiState,
    onEnableOrUpdatePin: (pin: String, enableBiometric: Boolean) -> Unit,
    onEnableExistingPin: () -> Unit,
    onDisableWithPinVerify: (pin: String) -> Boolean,
    onToggleBiometric: (Boolean) -> Unit,
    onTestLockNow: () -> Unit,
    onDismissStatus: () -> Unit,
    modifier: Modifier = Modifier
) {
    var activePinDialog by remember { mutableStateOf<PinDialogMode?>(null) }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.5.dp, Color(0xFFDC2626).copy(alpha = 0.36f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("settings_app_lock_card")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFDC2626).copy(alpha = 0.14f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.Security,
                                contentDescription = null,
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "🔒 অ্যাপ লক (App Lock Security)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (lockState.isLockEnabled) {
                                "সক্রিয় • প্রতিবার অ্যাপ খুললে ৪-ডিজিট PIN${if (lockState.isBiometricEnabled) " / ফিঙ্গারপ্রিন্ট" else ""} লাগবে"
                            } else {
                                "৪-ডিজিট এনক্রিপ্টেড PIN ও ফিঙ্গারপ্রিন্ট / ফেস লক"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Switch(
                    checked = lockState.isLockEnabled,
                    onCheckedChange = { wantEnable ->
                        if (wantEnable) {
                            if (lockState.hasPinConfigured) {
                                onEnableExistingPin()
                            } else {
                                activePinDialog = PinDialogMode.SET_NEW_PIN
                            }
                        } else {
                            activePinDialog = PinDialogMode.VERIFY_TO_DISABLE
                        }
                    },
                    modifier = Modifier.testTag("settings_app_lock_toggle")
                )
            }

            AnimatedVisibility(visible = !lockState.statusMessage.isNullOrBlank()) {
                Surface(
                    color = SageOfflineContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = lockState.statusMessage.orEmpty(),
                            style = MaterialTheme.typography.bodySmall,
                            color = SageOffline,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = onDismissStatus,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Close status",
                                tint = SageOffline,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Biometric Toggle Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Fingerprint,
                        contentDescription = null,
                        tint = if (lockState.isBiometricHardwareAvailable) OnlineCobalt else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                    Column {
                        Text(
                            text = "ফিঙ্গারপ্রিন্ট / ফেস আনলক",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (lockState.isBiometricHardwareAvailable) {
                                "PIN ছাড়া এক ট্যাপে বায়োমেট্রিক আনলক"
                            } else {
                                "এই ডিভাইসে বায়োমেট্রিক হার্ডওয়্যার/সেটআপ পাওয়া যায়নি"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Switch(
                    checked = lockState.isBiometricEnabled,
                    enabled = lockState.isLockEnabled && lockState.isBiometricHardwareAvailable,
                    onCheckedChange = onToggleBiometric,
                    modifier = Modifier.testTag("settings_biometric_toggle")
                )
            }

            // Action Buttons: Set/Change PIN + Test Lock Now
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledTonalButton(
                    onClick = { activePinDialog = PinDialogMode.SET_NEW_PIN },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .testTag("settings_set_change_pin_btn")
                ) {
                    Icon(Icons.Rounded.Lock, contentDescription = null, modifier = Modifier.size(17.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (lockState.hasPinConfigured) "PIN পরিবর্তন করুন" else "৪-ডিজিট PIN সেট করুন",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (lockState.isLockEnabled) {
                    OutlinedButton(
                        onClick = onTestLockNow,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("settings_test_lock_now_btn")
                    ) {
                        Text("এখনই লক করুন", maxLines = 1)
                    }
                }
            }
        }
    }

    activePinDialog?.let { mode ->
        PinSetupModalDialog(
            mode = mode,
            isBiometricAvailable = lockState.isBiometricHardwareAvailable,
            initialBiometricChecked = lockState.isBiometricEnabled || lockState.isBiometricHardwareAvailable,
            onConfirmNewPin = { pin, bio -> onEnableOrUpdatePin(pin, bio) },
            onVerifyPinToDisable = onDisableWithPinVerify,
            onDismiss = { activePinDialog = null }
        )
    }
}
