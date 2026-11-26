package com.example.ui.backup

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.backup.SecretRecoveryBackupManager
import com.example.data.media.MediaVaultManager
import com.example.model.EncryptedBackupStats
import com.example.model.RecoveryKeyBackupUiState
import com.example.ui.theme.MonoFontFamily
import com.example.ui.theme.OnlineCobalt
import com.example.ui.theme.SageOffline

private val RecoveryEmeraldDark = Color(0xFF064E3B)
private val RecoveryTealMid = Color(0xFF0F766E)
private val RecoveryBlueDeep = Color(0xFF1E3A8A)
private val DriveBlue = Color(0xFF1A73E8)
private val KeyGold = Color(0xFFD97706)

/**
 * Full-Screen "🔑 রিকভারি কী ও ব্যাকআপ (Secret Recovery Key & Dual Backup)" Hub
 * accessible from both the floating draggable "D" menu and Settings.
 *
 * Tab 0: Secret Recovery Key (`EDU10-XXXX-XXXX`) + Copy Key + Dual AES-256 Encrypted `.edukey` Export
 *        (1. Save to Local Storage via SAF CreateDocument, 2. Save to Google Drive via Share Sheet)
 * Tab 1: "🔓 চাবি দিয়ে ডেটা ফিরিয়ে আনুন (Restore Data)" — Enter Secret Recovery Key + OpenDocument
 *        file picker browsing both Local Storage and Google Drive to decrypt & restore all data.
 */
@Composable
fun RecoveryKeyBackupScreen(
    state: RecoveryKeyBackupUiState,
    onSelectTab: (Int) -> Unit,
    onUpdateRestoreKeyInput: (String) -> Unit,
    onExportToLocalUri: (Uri) -> Unit,
    onExportToGoogleDrive: ((Intent) -> Unit) -> Unit,
    onRestoreFromUri: (Uri) -> Unit,
    onDismissStatus: () -> Unit,
    onShowToast: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onClose)

    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    // SAF CreateDocument for Local Storage (.edukey encrypted bundle)
    val saveLocalLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        if (uri != null) {
            onExportToLocalUri(uri)
        }
    }

    // SAF OpenDocument for Restore (browses both Local Storage AND Google Drive natively!)
    val openRestoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            onRestoreFromUri(uri)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("recovery_key_backup_screen")
    ) {
        // High-Contrast Gradient Header + 2-Tab Segmented Switcher
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(RecoveryEmeraldDark, RecoveryTealMid, RecoveryBlueDeep)
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("recovery_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "🔑 রিকভারি কী ও ব্যাকআপ",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "AES-256 এনক্রিপ্টেড .edukey ফাইল • ফোন ও Google Drive ব্যাকআপ",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.92f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // 2 Tabs: Backup vs Restore
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RecoveryHeaderTab(
                        label = "🔑 কী ও ব্যাকআপ",
                        selected = state.activeTabIndex == 0,
                        tag = "recovery_tab_backup",
                        onClick = { onSelectTab(0) },
                        modifier = Modifier.weight(1f)
                    )
                    RecoveryHeaderTab(
                        label = "🔓 ডেটা ফিরিয়ে আনুন",
                        selected = state.activeTabIndex == 1,
                        tag = "recovery_tab_restore",
                        onClick = { onSelectTab(1) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Status Feedback Banner
        AnimatedVisibility(visible = !state.statusMessage.isNullOrBlank()) {
            val bannerBg = if (state.isErrorStatus) {
                MaterialTheme.colorScheme.errorContainer
            } else {
                SageOffline.copy(alpha = 0.14f)
            }
            val bannerFg = if (state.isErrorStatus) {
                MaterialTheme.colorScheme.onErrorContainer
            } else {
                SageOffline
            }
            Surface(color = bannerBg, modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (state.isErrorStatus) Icons.Rounded.ErrorOutline else Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = bannerFg,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = state.statusMessage.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = bannerFg,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismissStatus, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Dismiss",
                            tint = bannerFg,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Body Content
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (state.activeTabIndex == 0) {
                // TAB 0: SECRET RECOVERY KEY & DUAL BACKUP
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.5.dp, KeyGold.copy(alpha = 0.55f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = KeyGold.copy(alpha = 0.16f),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Rounded.Key,
                                            contentDescription = null,
                                            tint = KeyGold,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "তোমার ব্যক্তিগত Secret Recovery Key",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "এই চাবি দিয়েই তোমার .edukey ফাইল AES এনক্রিপ্ট ও আনলক হবে",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Crisp Readable Recovery Key Display Box
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFF0F172A),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("recovery_secret_key_box")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = state.secretRecoveryKey.ifBlank { "EDU10-••••-••••" },
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontFamily = MonoFontFamily,
                                            fontSize = 21.sp,
                                            letterSpacing = 1.6.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        ),
                                        color = Color(0xFFFDE68A),
                                        maxLines = 1
                                    )

                                    FilledTonalButton(
                                        onClick = {
                                            if (state.secretRecoveryKey.isNotBlank()) {
                                                clipboard.setText(AnnotatedString(state.secretRecoveryKey))
                                                onShowToast("✅ রিকভারি কী কপি হয়েছে: ${state.secretRecoveryKey}")
                                            }
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        modifier = Modifier
                                            .minimumInteractiveComponentSize()
                                            .testTag("recovery_copy_key_btn")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.ContentCopy,
                                            contentDescription = "Copy Key",
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = "Copy Key",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = KeyGold.copy(alpha = 0.10f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "📌 টিপস: এই চাবিটি তোমার খাতায় লিখে রাখো অথবা কপি করে নিরাপদ জায়গায় সেভ করো। ফোন হারালে বা অ্যাপ রিইনস্টল করলে এই চাবি + .edukey ফাইল দিয়েই সব ডেটা ফিরে পাবে!",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }
                }

                // Dual Export Options Card
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
                                    imageVector = Icons.Rounded.Security,
                                    contentDescription = null,
                                    tint = RecoveryTealMid,
                                    modifier = Modifier.size(22.dp)
                                )
                                Column {
                                    Text(
                                        text = "ডুয়াল এনক্রিপ্টেড ব্যাকআপ (.edukey)",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "সব ৭টি চ্যাট হিস্ট্রি, সেলফ-রিপোর্ট কার্ড, পরীক্ষার স্কোর ও নোটস (~100 KB - 2 MB)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Export Option 1: Local Storage via SAF CreateDocument
                            Button(
                                onClick = {
                                    saveLocalLauncher.launch(SecretRecoveryBackupManager.suggestBackupFileName())
                                },
                                enabled = !state.isWorking,
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = RecoveryTealMid,
                                    contentColor = Color.White
                                ),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 13.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .minimumInteractiveComponentSize()
                                    .testTag("recovery_save_local_btn")
                            ) {
                                if (state.isWorking) {
                                    CircularProgressIndicator(
                                        strokeWidth = 2.dp,
                                        color = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Rounded.PhoneAndroid,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "📱 ফোনের স্টোরেজে সেভ করুন (Save to Local Storage)",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Export Option 2: Google Drive via Direct Android Share Sheet
                            Button(
                                onClick = {
                                    onExportToGoogleDrive { shareIntent ->
                                        try {
                                            context.startActivity(shareIntent)
                                        } catch (_: Exception) {
                                            onShowToast("Google Drive শেয়ার শিট খোলা যায়নি। ফোনের স্টোরেজে সেভ করে ড্রাইভে আপলোড করতে পারো।")
                                        }
                                    }
                                },
                                enabled = !state.isWorking,
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = DriveBlue,
                                    contentColor = Color.White
                                ),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 13.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .minimumInteractiveComponentSize()
                                    .testTag("recovery_save_drive_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.CloudUpload,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "☁️ Google Drive-এ সেভ করুন (Save to Google Drive)",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                // Last Backup Bundle Summary
                state.lastBackupStats?.let { stats ->
                    item {
                        BackupStatsSummaryCard(
                            title = "সর্বশেষ .edukey ব্যাকআপ রিপোর্ট",
                            stats = stats,
                            accent = SageOffline
                        )
                    }
                }
            } else {
                // TAB 1: RESTORE DATA WITH SECRET RECOVERY KEY
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.5.dp, OnlineCobalt.copy(alpha = 0.45f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = OnlineCobalt.copy(alpha = 0.14f),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Rounded.LockOpen,
                                            contentDescription = null,
                                            tint = OnlineCobalt,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Column {
                                    Text(
                                        text = "🔓 চাবি দিয়ে ডেটা ফিরিয়ে আনুন (Restore Data)",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "লোকাল স্টোরেজ অথবা Google Drive থেকে .edukey ফাইল বেছে নাও",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            OutlinedTextField(
                                value = state.restoreKeyInput,
                                onValueChange = onUpdateRestoreKeyInput,
                                label = { Text("Secret Recovery Key (যেমন: EDU10-XXXX-XXXX)") },
                                placeholder = { Text("EDU10-XXXX-XXXX") },
                                singleLine = true,
                                textStyle = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = MonoFontFamily,
                                    letterSpacing = 1.2.sp
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("recovery_restore_key_input")
                            )

                            if (state.secretRecoveryKey.isNotBlank() &&
                                state.restoreKeyInput.trim() != state.secretRecoveryKey
                            ) {
                                OutlinedButton(
                                    onClick = { onUpdateRestoreKeyInput(state.secretRecoveryKey) },
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("recovery_use_current_key_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Key,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "আমার বর্তমান চাবি বসাও (${state.secretRecoveryKey})",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    if (state.restoreKeyInput.trim().length < 8) {
                                        onShowToast("⚠️ আগে সঠিক Secret Recovery Key (EDU10-XXXX-XXXX) লেখো।")
                                    } else {
                                        openRestoreLauncher.launch(arrayOf("*/*"))
                                    }
                                },
                                enabled = !state.isWorking,
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = OnlineCobalt,
                                    contentColor = Color.White
                                ),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 13.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .minimumInteractiveComponentSize()
                                    .testTag("recovery_pick_edukey_restore_btn")
                            ) {
                                if (state.isWorking) {
                                    CircularProgressIndicator(
                                        strokeWidth = 2.dp,
                                        color = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Rounded.FolderOpen,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "📂 .edukey ফাইল বেছে রিস্টোর করুন (Local / Drive)",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "💡 ফাইল পিকার খুললে উপরের বাম কোণের মেনু (☰) থেকে তোমার ফোনের Downloads ফোল্ডার অথবা সরাসরি Google Drive থেকে .edukey ফাইল বেছে নিতে পারবে।",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }
                }

                // Last Restore Summary
                state.lastRestoreStats?.let { stats ->
                    item {
                        BackupStatsSummaryCard(
                            title = "✅ রিস্টোর হওয়া ডেটার বিবরণ",
                            stats = stats,
                            accent = OnlineCobalt
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun RecoveryHeaderTab(
    label: String,
    selected: Boolean,
    tag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (selected) Color.White else Color.White.copy(alpha = 0.16f),
        border = BorderStroke(1.dp, if (selected) Color.White else Color.White.copy(alpha = 0.35f)),
        modifier = modifier
            .minimumInteractiveComponentSize()
            .testTag(tag)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.ExtraBold,
                color = if (selected) RecoveryEmeraldDark else Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun BackupStatsSummaryCard(
    title: String,
    stats: EncryptedBackupStats,
    accent: Color
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = accent.copy(alpha = 0.10f)),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = accent
            )
            Text(
                text = "• ফাইল সাইজ: ${MediaVaultManager.formatFileSize(stats.fileSizeBytes)} (AES-256 এনক্রিপ্টেড)\n" +
                    "• মোট চ্যাট বার্তা: ${stats.totalChatMessages}টি (${stats.chatSectionsCovered}টি সেকশন)\n" +
                    "• পরীক্ষার স্কোর ও মক টেস্ট: ${stats.examAttemptsCount}টি\n" +
                    "• সেলফ-রিপোর্ট সম্পন্ন অধ্যায়: ${stats.completedChaptersCount}টি • স্ট্রিক: ${stats.streakDays} দিন\n" +
                    "• কাস্টম নোটস ও বুকমার্ক: ${stats.studyNotesAndBookmarksCount}টি",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
