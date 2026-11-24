package com.example.ui.master

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.WritingFormatType
import com.example.model.WritingLanguage
import com.example.model.WritingStudioUiState

/**
 * Feature #17: "রাইটিং ও রচনা মাস্টার (WBBSE Writing Studio)"
 * Generates board-format English (Notice with Box, Report, Letters, Process, Biography, Story)
 * and Bengali (প্রবন্ধ, সংলাপ, প্রতিবেদন, বঙ্গানুবাদ) with 1-tap Offline PDF export.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WritingStudioScreen(
    state: WritingStudioUiState,
    onSelectFormat: (WritingFormatType) -> Unit,
    onUpdateTopic: (String) -> Unit,
    onGenerate: () -> Unit,
    onExportPdf: (suggestedName: String, title: String, subtitle: String, body: String) -> Unit,
    onClose: () -> Unit
) {
    BackHandler { onClose() }
    val clipboard = LocalClipboardManager.current
    var copiedBanner by remember { mutableStateOf(false) }

    val formatsForLanguage = remember(state.language) {
        WritingFormatType.entries.filter { it.language == state.language }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "রাইটিং ও রচনা মাস্টার",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "WBBSE বোর্ড ফরম্যাট • অনলাইন ও অফলাইন + PDF",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.testTag("writing_studio_back")
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "ফিরে যান")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Language switcher
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    WritingLanguage.entries.forEach { lang ->
                        FilterChip(
                            selected = state.language == lang,
                            onClick = {
                                val firstFmt = WritingFormatType.entries.first { it.language == lang }
                                onSelectFormat(firstFmt)
                                copiedBanner = false
                            },
                            label = { Text(lang.labelBn, fontWeight = FontWeight.SemiBold) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Format selector chips
            item {
                Text(
                    text = "রাইটিং ফরম্যাট বেছে নাও:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    formatsForLanguage.forEach { fmt ->
                        FilterChip(
                            selected = state.selectedFormat == fmt,
                            onClick = {
                                onSelectFormat(fmt)
                                copiedBanner = false
                            },
                            label = { Text("${fmt.titleBn} (${fmt.marksNote})") }
                        )
                    }
                }
            }

            // Topic input + Generate button
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = state.topicInput,
                            onValueChange = onUpdateTopic,
                            label = {
                                Text(
                                    if (state.selectedFormat == WritingFormatType.BN_BONGANUBAD)
                                        "ইংরেজি অনুচ্ছেদ লিখুন/পেস্ট করুন"
                                    else
                                        "বিষয় / টপিক লিখুন (Topic)"
                                )
                            },
                            minLines = if (state.selectedFormat == WritingFormatType.BN_BONGANUBAD) 3 else 2,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("writing_topic_input")
                        )
                        Button(
                            onClick = {
                                copiedBanner = false
                                onGenerate()
                            },
                            enabled = !state.isGenerating,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("writing_generate_btn")
                        ) {
                            if (state.isGenerating) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("বোর্ড ফরম্যাট তৈরি হচ্ছে...")
                            } else {
                                Icon(Icons.Rounded.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("পূর্ণ নম্বরের উত্তর তৈরি করো")
                            }
                        }
                    }
                }
            }

            // Output Card with Copy & PDF Export
            if (state.generatedOutput.isNotBlank()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = state.sourceBadge.ifBlank { "WBBSE বোর্ড ফরম্যাট" },
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                                Text(
                                    text = state.selectedFormat.marksNote,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Text(
                                text = state.generatedOutput,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = if (state.selectedFormat == WritingFormatType.EN_NOTICE) FontFamily.Monospace else FontFamily.Default,
                                    lineHeight = 22.sp
                                )
                            )

                            if (copiedBanner) {
                                Text(
                                    text = "✅ ক্লিপবোর্ডে কপি করা হয়েছে!",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        clipboard.setText(AnnotatedString(state.generatedOutput))
                                        copiedBanner = true
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("writing_copy_btn")
                                ) {
                                    Icon(Icons.Rounded.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("কপি করুন")
                                }
                                Button(
                                    onClick = {
                                        onExportPdf(
                                            "${state.selectedFormat.titleBn}_${state.topicInput.take(24)}",
                                            "${state.selectedFormat.titleBn}: ${state.topicInput}",
                                            "WBBSE Class 10 • ${state.selectedFormat.marksNote}",
                                            state.generatedOutput
                                        )
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("writing_pdf_btn")
                                ) {
                                    Icon(Icons.Rounded.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("PDF তৈরি করুন")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
