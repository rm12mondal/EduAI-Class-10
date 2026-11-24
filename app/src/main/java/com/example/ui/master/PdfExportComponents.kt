package com.example.ui.master

import android.content.Intent
import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.pdf.EduPdfManager
import com.example.data.pdf.SavedPdfItem
import com.example.model.SavedPdfLibraryUiState
import com.example.ui.theme.OnlineCobalt
import com.example.ui.theme.SageOffline
import com.example.ui.theme.TerracottaAccent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Universal Offline Custom-Named PDF Generator Dialog (Feature #1).
 * Prompts the student for a custom file name (e.g. "ভৌত_বিজ্ঞান_আলো_নোটস"), generates
 * the Bengali/English PDF 100% offline via Android's PdfDocument API, saves to Downloads/EduAI_Notes/,
 * and immediately shows an "PDF খুলুন" button + WhatsApp Share!
 */
@Composable
fun PdfExportDialog(
    state: SavedPdfLibraryUiState,
    onConfirmGenerate: (String) -> Unit,
    onDismiss: () -> Unit,
    onOpenLibrary: () -> Unit,
    onShowToast: (String) -> Unit
) {
    val req = state.pendingExportRequest ?: return
    val context = LocalContext.current
    var fileNameInput by remember(req.suggestedFileName) { mutableStateOf(req.suggestedFileName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.PictureAsPdf,
                    contentDescription = null,
                    tint = Color(0xFFC62828)
                )
                Text(
                    text = "PDF তৈরি করুন (১০০% অফলাইন)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (state.lastExportedPdf != null) {
                    val saved = state.lastExportedPdf
                    Surface(
                        color = SageOffline.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, SageOffline.copy(alpha = 0.4f))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = null,
                                    tint = SageOffline,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "PDF সফলভাবে তৈরি হয়েছে!",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = SageOffline,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "ফাইল: ${saved.fileName}\nলোকেশন: ${saved.publicDownloadsNote}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Button(
                        onClick = {
                            try {
                                val intent = EduPdfManager.buildOpenPdfIntent(context, saved.absolutePath)
                                context.startActivity(Intent.createChooser(intent, "PDF খুলুন"))
                            } catch (_: Exception) {
                                onShowToast("কোনো PDF ভিউয়ার অ্যাপ পাওয়া যায়নি।")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = OnlineCobalt),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                            .testTag("open_generated_pdf_button")
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.OpenInNew, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("PDF খুলুন", fontWeight = FontWeight.Bold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilledTonalButton(
                            onClick = {
                                sharePdfSafely(context, saved.absolutePath, preferWhatsApp = true, onShowToast)
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Rounded.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("WhatsApp", maxLines = 1)
                        }
                        OutlinedButton(
                            onClick = {
                                onDismiss()
                                onOpenLibrary()
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("আমার PDF", maxLines = 1)
                        }
                    }
                } else {
                    Text(
                        text = "তোমার পছন্দমতো PDF ফাইলের নাম দাও (Downloads/EduAI_Notes/ ফোল্ডারে সেভ হবে):",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = fileNameInput,
                        onValueChange = { fileNameInput = it },
                        label = { Text("ফাইলের নাম (যেমন: ভৌত_বিজ্ঞান_আলো_নোটস)") },
                        suffix = { Text(".pdf") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("pdf_custom_filename_input")
                    )
                    if (state.isGeneratingPdf) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Text("অফলাইন বাংলা/ইংরেজি PDF তৈরি হচ্ছে…", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    state.statusMessage?.let { msg ->
                        Text(
                            text = msg,
                            style = MaterialTheme.typography.bodySmall,
                            color = TerracottaAccent
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (state.lastExportedPdf == null) {
                Button(
                    onClick = { onConfirmGenerate(fileNameInput) },
                    enabled = !state.isGeneratingPdf && fileNameInput.isNotBlank(),
                    modifier = Modifier.testTag("confirm_generate_pdf_button")
                ) {
                    Text("PDF সেভ করুন")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (state.lastExportedPdf != null) "বন্ধ করুন" else "বাতিল")
            }
        }
    )
}

/**
 * Feature #2: "আমার সেভ করা PDF (My Saved PDFs Library)" Screen.
 * Lists all custom-named PDFs inside `Downloads/EduAI_Notes/`, allowing students to
 * Open, Rename, Share via WhatsApp, or Delete directly offline inside the app.
 */
@Composable
fun SavedPdfLibraryScreen(
    state: SavedPdfLibraryUiState,
    onRename: (SavedPdfItem, String) -> Unit,
    onDelete: (SavedPdfItem) -> Unit,
    onBack: () -> Unit,
    onShowToast: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    var renamingItem by remember { mutableStateOf<SavedPdfItem?>(null) }
    var renameText by remember { mutableStateOf("") }
    val dateFmt = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 720.dp)
        ) {
            Surface(color = MaterialTheme.colorScheme.surface, tonalElevation = 2.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("pdf_library_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "আমার সেভ করা PDF (${state.items.size})",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "ফোল্ডার: Downloads/EduAI_Notes/ • ১০০% অফলাইন লাইব্রেরি",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (state.items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(22.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.PictureAsPdf,
                                contentDescription = null,
                                tint = Color(0xFFC62828),
                                modifier = Modifier.size(44.dp)
                            )
                            Text(
                                text = "এখনো কোনো PDF সেভ করা হয়নি",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "চ্যাট উত্তর, স্মার্ট লার্নিং, পকেট বুক, রাইটিং স্টুডিও অথবা ব্রহ্মাস্ত্রম নোটস থেকে ‘PDF তৈরি করুন’ বাটনে ট্যাপ করলেই এখানে সেভ হয়ে যাবে!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("saved_pdf_list")
                ) {
                    items(state.items, key = { it.absolutePath }) { item ->
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Surface(
                                        color = Color(0xFFC62828).copy(alpha = 0.14f),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.size(44.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Rounded.PictureAsPdf,
                                                contentDescription = null,
                                                tint = Color(0xFFC62828)
                                            )
                                        }
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.displayName,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        val kb = (item.sizeBytes / 1024L).coerceAtLeast(1L)
                                        Text(
                                            text = "${kb} KB • ${dateFmt.format(Date(item.lastModifiedMs))}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            renamingItem = item
                                            renameText = item.displayName
                                        },
                                        modifier = Modifier.minimumInteractiveComponentSize()
                                    ) {
                                        Icon(Icons.Rounded.Edit, contentDescription = "Rename PDF")
                                    }
                                    IconButton(
                                        onClick = { onDelete(item) },
                                        modifier = Modifier.minimumInteractiveComponentSize()
                                    ) {
                                        Icon(
                                            Icons.Outlined.Delete,
                                            contentDescription = "Delete PDF",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            try {
                                                val intent = EduPdfManager.buildOpenPdfIntent(context, item.absolutePath)
                                                context.startActivity(Intent.createChooser(intent, "PDF খুলুন"))
                                            } catch (_: Exception) {
                                                onShowToast("PDF খোলার অ্যাপ পাওয়া যায়নি।")
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = OnlineCobalt),
                                        shape = RoundedCornerShape(12.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.AutoMirrored.Rounded.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("খুলুন", maxLines = 1)
                                    }

                                    FilledTonalButton(
                                        onClick = {
                                            sharePdfSafely(context, item.absolutePath, preferWhatsApp = true, onShowToast)
                                        },
                                        colors = ButtonDefaults.filledTonalButtonColors(
                                            containerColor = SageOffline.copy(alpha = 0.16f),
                                            contentColor = SageOffline
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Rounded.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("WhatsApp", maxLines = 1, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    renamingItem?.let { item ->
        AlertDialog(
            onDismissRequest = { renamingItem = null },
            title = { Text("PDF-এর নাম পরিবর্তন করুন") },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    label = { Text("নতুন নাম") },
                    suffix = { Text(".pdf") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRename(item, renameText)
                        renamingItem = null
                    },
                    enabled = renameText.isNotBlank()
                ) {
                    Text("সেভ করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { renamingItem = null }) {
                    Text("বাতিল")
                }
            }
        )
    }
}

private fun sharePdfSafely(
    context: android.content.Context,
    filePath: String,
    preferWhatsApp: Boolean,
    onShowToast: (String) -> Unit
) {
    try {
        val intent = EduPdfManager.buildSharePdfIntent(context, filePath, preferWhatsApp = preferWhatsApp)
        context.startActivity(intent)
    } catch (_: Exception) {
        try {
            val fallback = EduPdfManager.buildSharePdfIntent(context, filePath, preferWhatsApp = false)
            context.startActivity(Intent.createChooser(fallback, "PDF শেয়ার করুন"))
        } catch (_: Exception) {
            onShowToast("PDF শেয়ার করার অ্যাপ পাওয়া যায়নি।")
        }
    }
}
