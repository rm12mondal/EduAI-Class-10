package com.example.ui.vault

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.media.MediaVaultManager
import com.example.data.media.NotebookImageHelper
import com.example.data.pdf.EduPdfManager
import com.example.model.MediaVaultItem
import com.example.model.SubjectMediaVaultUiState
import com.example.model.VaultMediaType
import com.example.model.WbbseSubject
import com.example.ui.chat.FullScreenDiagramDialog
import com.example.ui.theme.OnlineCobalt
import com.example.ui.theme.SageOffline
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val VaultNavy = Color(0xFF0F172A)
private val VaultTealDeep = Color(0xFF0F4C5C)
private val VaultTealBright = Color(0xFF0E7490)
private val PdfCrimson = Color(0xFFC62828)
private val DiagramPurple = Color(0xFF7C3AED)

/**
 * Unified Subject-Wise Media & PDF Vault Screen launched from the floating "D" menu:
 * "📚 সাবজেক্ট অনুযায়ী ছবি ও PDF"
 *
 * Displays ALL uploaded photos, ALL AI-generated diagrams/mind maps, and ALL saved/generated PDFs
 * across every chat section filtered by subject (All + 7 WBBSE subjects) and media type,
 * with full-screen viewing, storage size display (KB/MB), instant per-item Delete (🗑️),
 * and one-tap "Clear Unused" / "Clear Subject Files" storage cleanup.
 */
@Composable
fun SubjectMediaVaultScreen(
    state: SubjectMediaVaultUiState,
    onSelectSubject: (WbbseSubject?) -> Unit,
    onSelectTypeFilter: (VaultMediaType?) -> Unit,
    onRefresh: () -> Unit,
    onDeleteItem: (MediaVaultItem) -> Unit,
    onClearUnusedFiles: () -> Unit,
    onClearFilteredSubjectFiles: () -> Unit,
    onDismissStatus: () -> Unit,
    onShowToast: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onClose)

    val context = LocalContext.current
    var fullScreenImagePath by remember { mutableStateOf<String?>(null) }
    var showClearSubjectConfirm by remember { mutableStateOf(false) }

    val filteredItems = remember(state.items, state.selectedSubject, state.selectedTypeFilter) {
        state.items.filter { item ->
            val subjMatch = state.selectedSubject == null || item.subject == state.selectedSubject
            val typeMatch = state.selectedTypeFilter == null || item.mediaType == state.selectedTypeFilter
            subjMatch && typeMatch
        }
    }

    val filteredBytes = remember(filteredItems) { filteredItems.sumOf { it.sizeBytes } }
    val countBySubject = remember(state.items) {
        state.items.groupingBy { it.subject }.eachCount()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("subject_media_vault_screen")
    ) {
        // High-Contrast Gradient Top Header + Subject Filter Tabs
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(VaultNavy, VaultTealDeep, VaultTealBright)))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
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
                            .testTag("vault_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "📚 সাবজেক্ট অনুযায়ী ছবি ও PDF",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "মোট ${state.items.size}টি ফাইল • ${MediaVaultManager.formatFileSize(state.totalStorageBytes)} • Auto 1024px 80% JPEG",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.90f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    IconButton(
                        onClick = onRefresh,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("vault_refresh_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = "Refresh vault",
                            tint = Color.White
                        )
                    }
                }

                // Horizontal Subject Filter Tabs: All + 7 WBBSE Subjects
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("vault_subject_tabs")
                ) {
                    item {
                        val selected = state.selectedSubject == null
                        Surface(
                            onClick = { onSelectSubject(null) },
                            shape = RoundedCornerShape(999.dp),
                            color = if (selected) Color.White else Color.White.copy(alpha = 0.16f),
                            border = BorderStroke(1.dp, if (selected) Color.White else Color.White.copy(alpha = 0.35f)),
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("vault_tab_all")
                        ) {
                            Text(
                                text = "সব (${state.items.size})",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (selected) VaultNavy else Color.White,
                                modifier = Modifier.padding(horizontal = 13.dp, vertical = 7.dp)
                            )
                        }
                    }

                    items(WbbseSubject.entries, key = { it.id }) { subject ->
                        val selected = state.selectedSubject == subject
                        val count = countBySubject[subject] ?: 0
                        Surface(
                            onClick = { onSelectSubject(subject) },
                            shape = RoundedCornerShape(999.dp),
                            color = if (selected) Color.White else Color.White.copy(alpha = 0.16f),
                            border = BorderStroke(1.dp, if (selected) Color.White else Color.White.copy(alpha = 0.35f)),
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("vault_tab_${subject.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = subject.icon,
                                    contentDescription = null,
                                    tint = if (selected) subject.accentColor else Color.White,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = "${subject.bengaliName} ($count)",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selected) VaultNavy else Color.White,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }

        // File-Type Filter Strip + Storage Cleanup Bar
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            tonalElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // File-Type Chips: All / Photo / Diagram / PDF
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        TypeFilterChip(
                            label = "সব ধরন",
                            selected = state.selectedTypeFilter == null,
                            accent = VaultTealBright,
                            tag = "vault_type_all",
                            onClick = { onSelectTypeFilter(null) }
                        )
                    }
                    items(VaultMediaType.entries) { type ->
                        val accent = when (type) {
                            VaultMediaType.PHOTO -> OnlineCobalt
                            VaultMediaType.DIAGRAM -> DiagramPurple
                            VaultMediaType.PDF -> PdfCrimson
                        }
                        TypeFilterChip(
                            label = type.badgeLabel,
                            selected = state.selectedTypeFilter == type,
                            accent = accent,
                            tag = "vault_type_${type.name.lowercase()}",
                            onClick = { onSelectTypeFilter(type) }
                        )
                    }
                }

                // Storage Cleanup Action Buttons: Clear Unused & Clear Subject Files
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledTonalButton(
                        onClick = onClearUnusedFiles,
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 7.dp),
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .testTag("vault_clear_unused_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CleaningServices,
                            contentDescription = "Clear unused files",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (state.unusedFilesCount > 0) {
                                "অব্যবহৃত মুছো (${state.unusedFilesCount})"
                            } else {
                                "অব্যবহৃত পরিষ্কার"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    OutlinedButton(
                        onClick = { showClearSubjectConfirm = true },
                        enabled = filteredItems.isNotEmpty(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (filteredItems.isNotEmpty()) MaterialTheme.colorScheme.error.copy(alpha = 0.55f)
                            else MaterialTheme.colorScheme.outlineVariant
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 7.dp),
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .testTag("vault_clear_subject_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteSweep,
                            contentDescription = "Clear subject files",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = state.selectedSubject?.let { "${it.bengaliName} মুছো" } ?: "সব ফাইল মুছো",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Status Feedback Banner
        AnimatedVisibility(visible = !state.statusMessage.isNullOrBlank()) {
            Surface(
                color = SageOffline.copy(alpha = 0.14f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = state.statusMessage.orEmpty(),
                        style = MaterialTheme.typography.labelMedium,
                        color = SageOffline,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismissStatus, modifier = Modifier.size(30.dp)) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Dismiss",
                            tint = SageOffline,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        // Vault List Content
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = VaultTealBright)
                }
            } else if (filteredItems.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = VaultTealBright.copy(alpha = 0.12f),
                        modifier = Modifier.size(64.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.Storage,
                                contentDescription = null,
                                tint = VaultTealBright,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "এই বিভাগে এখনো কোনো ছবি, ডায়াগ্রাম বা PDF নেই",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "যেকোনো চ্যাট সেকশন থেকে ক্যামেরা/গ্যালারির ছবি আপলোড করলে (অটো 1024px স্মার্ট কমপ্রেশনসহ), AI ডায়াগ্রাম বানালে অথবা নোট PDF সেভ করলে তা এখানে স্বয়ংক্রিয়ভাবে জমা হবে।",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("vault_items_list")
                ) {
                    item {
                        Text(
                            text = "দেখানো হচ্ছে: ${filteredItems.size}টি ফাইল • মোট সাইজ ${MediaVaultManager.formatFileSize(filteredBytes)}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    items(filteredItems, key = { it.id }) { item ->
                        MediaVaultItemCard(
                            item = item,
                            onOpen = {
                                if (item.mediaType == VaultMediaType.PDF) {
                                    try {
                                        context.startActivity(
                                            EduPdfManager.buildOpenPdfIntent(context, item.filePath)
                                        )
                                    } catch (_: ActivityNotFoundException) {
                                        onShowToast("PDF খোলার জন্য কোনো PDF Viewer অ্যাপ পাওয়া যায়নি। শেয়ার বাটন ব্যবহার করতে পারো।")
                                    } catch (e: Exception) {
                                        onShowToast("PDF খোলা যায়নি: ${e.localizedMessage ?: "ত্রুটি"}")
                                    }
                                } else {
                                    fullScreenImagePath = item.filePath
                                }
                            },
                            onShare = {
                                try {
                                    val shareIntent = if (item.mediaType == VaultMediaType.PDF) {
                                        EduPdfManager.buildSharePdfIntent(context, item.filePath, preferWhatsApp = false)
                                    } else {
                                        val uri = EduPdfManager.getFileProviderUri(context, item.filePath)
                                        Intent(Intent.ACTION_SEND).apply {
                                            type = "image/*"
                                            putExtra(Intent.EXTRA_STREAM, uri)
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "ফাইল শেয়ার করুন"))
                                } catch (e: Exception) {
                                    onShowToast("শেয়ার করা যায়নি: ${e.localizedMessage ?: "ত্রুটি"}")
                                }
                            },
                            onDelete = { onDeleteItem(item) }
                        )
                    }
                }
            }
        }
    }

    // Full-Screen Photo / Diagram Viewer
    fullScreenImagePath?.let { path ->
        FullScreenDiagramDialog(
            imagePath = path,
            onDismiss = { fullScreenImagePath = null }
        )
    }

    // Confirmation Dialog for Bulk Subject Cleanup
    if (showClearSubjectConfirm) {
        val label = state.selectedSubject?.bengaliName ?: "নির্বাচিত সব"
        AlertDialog(
            onDismissRequest = { showClearSubjectConfirm = false },
            title = { Text("$label ফাইল মুছবে?") },
            text = {
                Text(
                    "মোট ${filteredItems.size}টি ফাইল (${MediaVaultManager.formatFileSize(filteredBytes)}) স্থায়ীভাবে মোছা হবে। এতে ফোনের স্টোরেজ খালি হবে।"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearSubjectConfirm = false
                        onClearFilteredSubjectFiles()
                    }
                ) {
                    Text("হ্যাঁ, মুছে ফেলো", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearSubjectConfirm = false }) {
                    Text("বাতিল")
                }
            }
        )
    }
}

@Composable
private fun TypeFilterChip(
    label: String,
    selected: Boolean,
    accent: Color,
    tag: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (selected) accent else MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, if (selected) accent else MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .testTag(tag)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun MediaVaultItemCard(
    item: MediaVaultItem,
    onOpen: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    val typeAccent = when (item.mediaType) {
        VaultMediaType.PHOTO -> OnlineCobalt
        VaultMediaType.DIAGRAM -> DiagramPurple
        VaultMediaType.PDF -> PdfCrimson
    }
    val dateLabel = remember(item.lastModifiedMs) {
        SimpleDateFormat("dd MMM, hh:mm a", Locale.US).format(Date(item.lastModifiedMs))
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(11.dp)
        ) {
            // Thumbnail Preview (Photo / Diagram) or Styled PDF Tile
            if (item.mediaType == VaultMediaType.PDF) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = PdfCrimson.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, PdfCrimson.copy(alpha = 0.35f)),
                    modifier = Modifier.size(66.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PictureAsPdf,
                            contentDescription = "PDF file",
                            tint = PdfCrimson,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = "PDF",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = PdfCrimson
                        )
                    }
                }
            } else {
                val thumb = remember(item.filePath) {
                    NotebookImageHelper.decodeFileThumbnail(item.filePath, maxSizePx = 220)
                }
                Box(
                    modifier = Modifier
                        .size(66.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(typeAccent.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (thumb != null) {
                        Image(
                            bitmap = thumb.asImageBitmap(),
                            contentDescription = item.displayTitle,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = if (item.mediaType == VaultMediaType.DIAGRAM) {
                                Icons.Rounded.AutoAwesome
                            } else {
                                Icons.Rounded.Image
                            },
                            contentDescription = item.displayTitle,
                            tint = typeAccent,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            // Metadata Column
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = item.displayTitle,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // File-Type Badge + Subject Badge
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = typeAccent.copy(alpha = 0.14f)
                    ) {
                        Text(
                            text = item.mediaType.badgeLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = typeAccent,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    item.subject?.let { subj ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = subj.accentColor.copy(alpha = 0.14f)
                        ) {
                            Text(
                                text = subj.bengaliName,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = subj.accentColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Source Tag + File Size + Date
                Text(
                    text = "${item.sourceLabel} • ${MediaVaultManager.formatFileSize(item.sizeBytes)} • $dateLabel",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Quick Actions: Open/Share + Instant Delete (🗑️)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("vault_delete_item_btn")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Delete file",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Row {
                    IconButton(
                        onClick = onOpen,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.OpenInNew,
                            contentDescription = "Open full screen",
                            tint = VaultTealBright,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                    IconButton(
                        onClick = onShare,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Share,
                            contentDescription = "Share file",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }
        }
    }
}
