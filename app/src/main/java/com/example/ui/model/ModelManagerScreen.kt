package com.example.ui.model

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeveloperBoard
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.FileUpload
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material.icons.rounded.Backup
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.gguf.GgufFileInspector
import com.example.model.AppBrandingUiState
import com.example.model.AppLockUiState
import com.example.model.DiscoveredGgufFile
import com.example.model.GgufFileMetadata
import com.example.model.GgufModelStatus
import com.example.model.InferenceMode
import com.example.model.LauncherDisguisePreset
import com.example.model.LlamaInferenceConfig
import com.example.model.TtsVoiceSettings
import com.example.ui.branding.BrandedAppLogoAvatar
import com.example.ui.branding.LocalAppBranding
import com.example.ui.theme.MonoFontFamily
import com.example.ui.theme.OnlineCobalt
import com.example.ui.theme.SageOffline
import com.example.ui.theme.SageOfflineContainer
import com.example.viewmodel.EduAiUiState
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun ModelManagerScreen(
    uiState: EduAiUiState,
    onImportGgufUri: (android.net.Uri) -> Unit,
    onScanModels: () -> Unit,
    onCreateDiagnosticGguf: () -> Unit,
    onLoadModel: (String) -> Unit,
    onUnloadModel: () -> Unit,
    onDeleteModel: (String) -> Unit,
    onToggleInferenceMode: (Boolean) -> Unit,
    onUpdateLlamaConfig: (LlamaInferenceConfig) -> Unit,
    onUpdateTtsVoiceSettings: (TtsVoiceSettings) -> Unit,
    onTestTtsVoice: (Boolean) -> Unit,
    onRefreshTtsVoices: () -> Unit,
    onClearChatHistory: () -> Unit,
    onDismissStatusBanner: () -> Unit,
    backupStatusMessage: String? = null,
    onExportBackupUri: (android.net.Uri) -> Unit = {},
    onImportBackupUri: (android.net.Uri) -> Unit = {},
    onDismissBackupStatus: () -> Unit = {},
    secretRecoveryKey: String = "",
    onOpenRecoveryKeyBackup: () -> Unit = {},
    brandingState: AppBrandingUiState = AppBrandingUiState(),
    appLockState: AppLockUiState = AppLockUiState(),
    onSelectLauncherDisguise: (LauncherDisguisePreset) -> Unit = {},
    onPrepareSystemPicker: () -> Unit = {},
    onPickCustomLogoUri: (android.net.Uri) -> Unit = {},
    onResetCustomLogo: () -> Unit = {},
    onSaveCustomAppTitle: (String) -> Unit = {},
    onResetCustomAppTitle: () -> Unit = {},
    onDismissBrandingStatus: () -> Unit = {},
    onEnableOrUpdateAppLockPin: (String, Boolean) -> Unit = { _, _ -> },
    onEnableExistingAppLock: () -> Unit = {},
    onDisableAppLockWithPinVerify: (String) -> Boolean = { false },
    onToggleBiometricUnlock: (Boolean) -> Unit = {},
    onTestLockNow: () -> Unit = {},
    onDismissAppLockStatus: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Zero-permission Android Storage Access Framework (SAF) file picker
    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            onImportGgufUri(uri)
        }
    }

    val createBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            onExportBackupUri(uri)
        }
    }

    val restoreBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            onImportBackupUri(uri)
        }
    }

    val ggufState = uiState.ggufState
    val isOnlineMode = uiState.inferenceMode == InferenceMode.ONLINE

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp)
                .testTag("model_manager_list"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                val branding = LocalAppBranding.current
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    BrandedAppLogoAvatar(
                        size = 46.dp,
                        shape = RoundedCornerShape(14.dp)
                    )
                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (branding.isRebrandedOrDisguised) {
                                "${branding.effectiveTitle} • সেটিংস"
                            } else {
                                stringResource(R.string.model_title)
                            },
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (branding.isRebrandedOrDisguised) {
                                "অ্যাপ প্রাইভেসি, লোগো, ডিসগাইজ, লক ও মডেল সেটিংস"
                            } else {
                                stringResource(R.string.model_subtitle)
                            },
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = MonoFontFamily),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // A. Home Screen Launcher Icon & Name Disguise (5 Activity-Alias Presets)
            item(key = "settings_launcher_disguise_item") {
                LauncherDisguiseSettingsCard(
                    brandingState = brandingState,
                    onSelectPreset = onSelectLauncherDisguise,
                    onDismissStatus = onDismissBrandingStatus
                )
            }

            // B. Complete In-App Logo & Name Replacement (Gallery Logo + Custom App Title)
            item(key = "settings_full_rebranding_item") {
                FullAppRebrandingSettingsCard(
                    brandingState = brandingState,
                    onPrepareSystemPicker = onPrepareSystemPicker,
                    onPickCustomLogoUri = onPickCustomLogoUri,
                    onResetCustomLogo = onResetCustomLogo,
                    onSaveCustomAppTitle = onSaveCustomAppTitle,
                    onResetCustomAppTitle = onResetCustomAppTitle
                )
            }

            // C. Strict App Lock Security (4-Digit PIN + Fingerprint / Face Biometric)
            item(key = "settings_app_lock_item") {
                AppLockSecuritySettingsCard(
                    lockState = appLockState,
                    onEnableOrUpdatePin = onEnableOrUpdateAppLockPin,
                    onEnableExistingPin = onEnableExistingAppLock,
                    onDisableWithPinVerify = onDisableAppLockWithPinVerify,
                    onToggleBiometric = onToggleBiometricUnlock,
                    onTestLockNow = onTestLockNow,
                    onDismissStatus = onDismissAppLockStatus
                )
            }

            // Status Notification Banner
            item {
                AnimatedVisibility(visible = !ggufState.importStatusMessage.isNullOrBlank()) {
                    Surface(
                        color = SageOfflineContainer,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, SageOffline.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = ggufState.importStatusMessage.orEmpty(),
                                style = MaterialTheme.typography.bodySmall,
                                color = SageOffline,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = onDismissStatusBanner,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Dismiss status",
                                    tint = SageOffline,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 1. Target GGUF Model Status & File Actions Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Memory,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(26.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = GgufFileInspector.TARGET_MODEL_FILENAME,
                                    style = MaterialTheme.typography.titleSmall.copy(fontFamily = MonoFontFamily),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Quantization: Q4_K_M • আনুমানিক সাইজ ~808 MB",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            ModelStatusChip(status = ggufState.status)
                        }

                        if (ggufState.isImporting) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                LinearProgressIndicator(
                                    progress = { ggufState.importProgress.coerceIn(0f, 1f) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Text(
                                    text = "ইম্পোর্ট চলছে: ${(ggufState.importProgress * 100).roundToInt()}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Button(
                            onClick = {
                                onPrepareSystemPicker()
                                openDocumentLauncher.launch(arrayOf("*/*"))
                            },
                            enabled = !ggufState.isImporting,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .minimumInteractiveComponentSize()
                                .testTag("import_gguf_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.FileUpload,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.model_import_button))
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilledTonalButton(
                                onClick = onScanModels,
                                enabled = !ggufState.isImporting,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .minimumInteractiveComponentSize()
                                    .testTag("scan_gguf_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = stringResource(R.string.model_scan_button),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            OutlinedButton(
                                onClick = onCreateDiagnosticGguf,
                                enabled = !ggufState.isImporting,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .minimumInteractiveComponentSize()
                                    .testTag("create_diagnostic_gguf_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Science,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = stringResource(R.string.model_test_gguf_button),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.FolderOpen,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "লোকাল মডেল ফোল্ডার (Zero-Permission App Storage)",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Text(
                                    text = "Internal: ${ggufState.internalModelsDirPath}\nExternal: ${ggufState.externalModelsDirPath}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = if (ggufState.isNativeLlamaEngineBundled) {
                                        "Native JNI Runtime (libllama_jni.so): Active"
                                    } else {
                                        "Runtime: GGUF Binary Inspector + WBBSE RAG Engine (JNI bridge ready for libllama_jni.so)"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            // 2. Active GGUF Binary Header Inspector Card
            ggufState.activeMetadata?.let { meta ->
                item {
                    GgufBinaryInspectorCard(
                        metadata = meta,
                        onUnload = onUnloadModel
                    )
                }
            }

            // 3. Discovered Local .gguf Files
            if (ggufState.discoveredFiles.isNotEmpty()) {
                item {
                    Text(
                        text = "পাওয়া গেছে (${ggufState.discoveredFiles.size}টি .gguf ফাইল)",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                items(ggufState.discoveredFiles, key = { it.absolutePath }) { file ->
                    DiscoveredModelRow(
                        file = file,
                        onLoad = { onLoadModel(file.absolutePath) },
                        onDelete = { onDeleteModel(file.absolutePath) }
                    )
                }
            }

            // 4. Voice & Speech Settings Card (Bengali bn-IN & English en-US TTS)
            item {
                VoiceSpeechSettingsCard(
                    ttsState = uiState.ttsState,
                    voiceSettings = uiState.ttsVoiceSettings,
                    onUpdateSettings = onUpdateTtsVoiceSettings,
                    onTestVoice = onTestTtsVoice,
                    onRefreshVoices = onRefreshTtsVoices
                )
            }

            // 5. Hybrid Mode & Llama Inference Parameters
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Tune,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = stringResource(R.string.model_settings_title),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "হাইব্রিড মোড (অফলাইন ↔ অনলাইন)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (isOnlineMode) {
                                        "লাইভ Firebase AI Logic (Gemini 3.8 Flash) সক্রিয়"
                                    } else {
                                        "সম্পূর্ণ অফলাইন WBBSE + Llama-3.2 GGUF মোড সক্রিয়"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = isOnlineMode,
                                onCheckedChange = onToggleInferenceMode,
                                modifier = Modifier.testTag("settings_hybrid_switch")
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                        val cfg = uiState.llamaConfig
                        SliderSettingRow(
                            label = "Context Window (n_ctx)",
                            valueText = "${cfg.contextLength} tokens",
                            value = cfg.contextLength.toFloat(),
                            valueRange = 512f..4096f,
                            steps = 6,
                            onValueChange = {
                                val snapped = ((it / 512f).roundToInt() * 512).coerceIn(512, 4096)
                                onUpdateLlamaConfig(cfg.copy(contextLength = snapped))
                            }
                        )

                        SliderSettingRow(
                            label = "Temperature",
                            valueText = String.format(Locale.US, "%.2f", cfg.temperature),
                            value = cfg.temperature,
                            valueRange = 0.1f..1.0f,
                            steps = 8,
                            onValueChange = {
                                onUpdateLlamaConfig(cfg.copy(temperature = it))
                            }
                        )

                        SliderSettingRow(
                            label = "CPU Threads (n_threads)",
                            valueText = "${cfg.cpuThreads} cores",
                            value = cfg.cpuThreads.toFloat(),
                            valueRange = 1f..8f,
                            steps = 6,
                            onValueChange = {
                                onUpdateLlamaConfig(cfg.copy(cpuThreads = it.roundToInt().coerceIn(1, 8)))
                            }
                        )
                    }
                }
            }

            // 5. Local Room Storage Summary & Maintenance
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Storage,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "অফলাইন ডেটাবেস স্থিতি (Room SQLite)",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        Text(
                            text = "• WBBSE ক্লাস ১০ মাস্টার ডেটা: ৭টি বিষয়, ${uiState.masterNotes.size}টি নোট\n• সংরক্ষিত চ্যাট বার্তা: ${uiState.chatMessages.size}টি",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        OutlinedButton(
                            onClick = onClearChatHistory,
                            enabled = uiState.chatMessages.isNotEmpty(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .minimumInteractiveComponentSize()
                                .testTag("settings_clear_chat_button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.model_clear_chat))
                        }
                    }
                }
            }

            // 6. Secret Recovery Key & Dual Encrypted Backup (.edukey — Local Storage + Google Drive)
            item(key = "secret_recovery_key_backup_card") {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.5.dp, Color(0xFF0F766E).copy(alpha = 0.45f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF0F766E).copy(alpha = 0.14f),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Rounded.Key,
                                        contentDescription = null,
                                        tint = Color(0xFF0F766E),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "🔑 রিকভারি কী ও ব্যাকআপ (.edukey)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "সব চ্যাট, রিপোর্ট কার্ড, স্কোর ও নোট AES-256 এনক্রিপ্টেড ফোনে ও Google Drive-এ সেভ করুন",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (secretRecoveryKey.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF064E3B).copy(alpha = 0.10f),
                                border = BorderStroke(1.dp, Color(0xFF0F766E).copy(alpha = 0.30f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "তোমার চাবি:",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = secretRecoveryKey,
                                        style = MaterialTheme.typography.titleSmall.copy(fontFamily = MonoFontFamily),
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF0F766E)
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = onOpenRecoveryKeyBackup,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF0F766E),
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .minimumInteractiveComponentSize()
                                .testTag("settings_open_recovery_key_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Key,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "🔑 রিকভারি কী, Google Drive ব্যাকআপ ও রিস্টোর খুলুন",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // 7. Offline Study Progress Backup & Restore (EduAI_Backup.json via SAF)
            item(key = "progress_backup_restore_card") {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Backup,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Column {
                                Text(
                                    text = "প্রোগ্রেস ব্যাকআপ ও রিস্টোর (১০০% অফলাইন)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "EduAI_Backup.json • পরীক্ষার স্কোর, টপার সেট, ভুলের খাতা, ব্রহ্মাস্ত্রম, রিভিশন ও স্ট্রিক",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (!backupStatusMessage.isNullOrBlank()) {
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
                                        text = backupStatusMessage,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SageOffline,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = onDismissBackupStatus,
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Rounded.Close, contentDescription = "Close", tint = SageOffline, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    onPrepareSystemPicker()
                                    createBackupLauncher.launch("EduAI_Backup.json")
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .minimumInteractiveComponentSize()
                                    .testTag("backup_export_btn")
                            ) {
                                Icon(Icons.Rounded.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("ব্যাকআপ সেভ", maxLines = 1)
                            }
                            OutlinedButton(
                                onClick = {
                                    onPrepareSystemPicker()
                                    restoreBackupLauncher.launch(arrayOf("application/json", "*/*"))
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .minimumInteractiveComponentSize()
                                    .testTag("backup_restore_btn")
                            ) {
                                Icon(Icons.Rounded.FileUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("রিস্টোর করুন", maxLines = 1)
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }
        }
    }
}

@Composable
private fun ModelStatusChip(status: GgufModelStatus) {
    val (label, bg, fg) = when (status) {
        GgufModelStatus.READY_LOADED -> Triple("READY", SageOfflineContainer, SageOffline)
        GgufModelStatus.VALIDATING -> Triple("CHECKING", MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.primary)
        GgufModelStatus.CORRUPT_OR_INVALID -> Triple("INVALID", MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.error)
        GgufModelStatus.NOT_FOUND -> Triple("NOT LOADED", MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
    }
    Surface(
        color = bg,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = fg,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun GgufBinaryInspectorCard(
    metadata: GgufFileMetadata,
    onUnload: () -> Unit
) {
    val accent = if (metadata.isValidMagic) SageOffline else MaterialTheme.colorScheme.error
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.45f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("gguf_inspector_card")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (metadata.isValidMagic) Icons.Rounded.CheckCircle else Icons.Rounded.ErrorOutline,
                        contentDescription = null,
                        tint = accent
                    )
                    Text(
                        text = "GGUF বাইনারি হেডার ইন্সপেক্টর",
                        style = MaterialTheme.typography.titleMedium,
                        color = accent
                    )
                }

                TextButton(onClick = onUnload) {
                    Text(stringResource(R.string.model_unload_button))
                }
            }

            Text(
                text = metadata.validationMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    MetadataLine("File", metadata.fileName)
                    MetadataLine("Size", formatByteSize(metadata.fileSizeBytes))
                    MetadataLine("Magic / Ver", if (metadata.isValidMagic) "GGUF v${metadata.ggufVersion}" else "Invalid")
                    MetadataLine("Architecture", metadata.architecture)
                    MetadataLine("Model Name", metadata.modelName)
                    MetadataLine("Quantization", metadata.quantizationHint)
                    MetadataLine("Tensors / KV", "${metadata.tensorCount} tensors • ${metadata.metadataKvCount} KV pairs")
                    MetadataLine("Context Len", "${metadata.contextLength} tokens")
                }
            }
        }
    }
}

@Composable
private fun MetadataLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun DiscoveredModelRow(
    file: DiscoveredGgufFile,
    onLoad: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            width = 1.dp,
            color = if (file.isLoaded) SageOffline else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.DeveloperBoard,
                contentDescription = null,
                tint = if (file.isLoaded) SageOffline else OnlineCobalt
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.fileName,
                    style = MaterialTheme.typography.bodyMedium.copy(fontFamily = MonoFontFamily),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = formatByteSize(file.sizeBytes) + if (file.isLoaded) " • সক্রিয়" else "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (!file.isLoaded) {
                FilledTonalButton(
                    onClick = onLoad,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(stringResource(R.string.model_load_button), style = MaterialTheme.typography.labelMedium)
                }
            }
            IconButton(
                onClick = onDelete,
                modifier = Modifier.minimumInteractiveComponentSize()
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = stringResource(R.string.model_delete_button),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun SliderSettingRow(
    label: String,
    valueText: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, style = MaterialTheme.typography.bodySmall)
            Text(
                text = valueText,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps
        )
    }
}

private fun formatByteSize(bytes: Long): String {
    return when {
        bytes >= 1_073_741_824L -> String.format(Locale.US, "%.2f GB", bytes / 1_073_741_824.0)
        bytes >= 1_048_576L -> String.format(Locale.US, "%.1f MB", bytes / 1_048_576.0)
        bytes >= 1024L -> String.format(Locale.US, "%.1f KB", bytes / 1024.0)
        else -> "$bytes B"
    }
}
