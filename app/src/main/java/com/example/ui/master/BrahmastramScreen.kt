package com.example.ui.master

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.StopCircle
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.BrahmastramStep
import com.example.model.BrahmastramUiState
import com.example.model.WbbseSubject
import com.example.ui.theme.MonoFontFamily
import com.example.ui.theme.OnlineCobalt
import com.example.ui.theme.SageOffline
import com.example.ui.theme.TerracottaAccent
import java.util.Locale

/**
 * Feature #4: "ব্রহ্মাস্ত্রম (Brahmastram — 3-Step Sure-Shot Mastery Workflow)"
 * Usable anytime (6+ months before exam):
 *   Step 1: Generate Sure-Shot WBBSE Suggestions & Complete Study Notes (with Topper Answer Sheet tab + PDF Export)
 *   Step 2: Read / Listen via TTS & Mark Completed (Room persisted)
 *   Step 3: Take a Timed "ব্রহ্মাস্ত্রম প্রশ্নপত্র" Exam strictly from those exact notes + Red-Pen & Trap Alert Report!
 */
@Composable
fun BrahmastramScreen(
    state: BrahmastramUiState,
    speakingMessageId: Long?,
    onSelectSubject: (WbbseSubject) -> Unit,
    onRegenerateNotes: () -> Unit,
    onToggleNoteCompleted: (String) -> Unit,
    onToggleSpeak: (Long, String) -> Unit,
    onExportNotesPdf: (String, String, String) -> Unit,
    onStartTimedExam: () -> Unit,
    onSelectMcqOption: (String, Int) -> Unit,
    onUpdateTypedAnswer: (String, String) -> Unit,
    onSubmitExam: () -> Unit,
    onBackToNotes: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        if (state.currentStep != BrahmastramStep.NOTES) onBackToNotes() else onClose()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 740.dp)
        ) {
            // Gradient Top Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF4A148C), Color(0xFF880E4F), Color(0xFFB71C1C))
                        )
                    )
                    .padding(horizontal = 8.dp, vertical = 10.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                if (state.currentStep != BrahmastramStep.NOTES) onBackToNotes() else onClose()
                            },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("brahmastram_back_button")
                        ) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "ব্রহ্মাস্ত্রম (Sure-Shot সাজেশন ও পরীক্ষা)",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = state.sourceBadge.ifBlank { "যেকোনো সময় পড়ো → শোনো → ঠিক সেই নোটস থেকে পরীক্ষা দাও" },
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.85f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (state.currentStep == BrahmastramStep.EXAM) {
                            val min = state.remainingSeconds / 60
                            val sec = state.remainingSeconds % 60
                            Surface(
                                color = Color(0xFFFFD54F).copy(alpha = 0.25f),
                                shape = RoundedCornerShape(999.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Rounded.Timer, contentDescription = null, tint = Color(0xFFFFD54F), modifier = Modifier.size(16.dp))
                                    Text(
                                        text = String.format(Locale.US, "%02d:%02d", min, sec),
                                        style = MaterialTheme.typography.labelLarge.copy(fontFamily = MonoFontFamily),
                                        color = Color(0xFFFFD54F),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // 3-Step Visual Stepper Indicator
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        BrahmastramStep.entries.forEach { step ->
                            val active = state.currentStep == step
                            Surface(
                                color = if (active) Color(0xFFFFD54F) else Color.White.copy(alpha = 0.16f),
                                shape = RoundedCornerShape(999.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = step.titleBn,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (active) Color(0xFF3E0A49) else Color.White,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }
            }

            when (state.currentStep) {
                BrahmastramStep.NOTES -> BrahmastramNotesStep(
                    state = state,
                    speakingMessageId = speakingMessageId,
                    onSelectSubject = onSelectSubject,
                    onRegenerateNotes = onRegenerateNotes,
                    onToggleNoteCompleted = onToggleNoteCompleted,
                    onToggleSpeak = onToggleSpeak,
                    onExportNotesPdf = onExportNotesPdf,
                    onStartTimedExam = onStartTimedExam,
                    modifier = Modifier.weight(1f)
                )

                BrahmastramStep.EXAM -> BrahmastramExamStep(
                    state = state,
                    onSelectMcqOption = onSelectMcqOption,
                    onUpdateTypedAnswer = onUpdateTypedAnswer,
                    onSubmitExam = onSubmitExam,
                    modifier = Modifier.weight(1f)
                )

                BrahmastramStep.RESULT -> BrahmastramResultStep(
                    state = state,
                    onBackToNotes = onBackToNotes,
                    onRetakeExam = onStartTimedExam,
                    onExportReportPdf = onExportNotesPdf,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun BrahmastramNotesStep(
    state: BrahmastramUiState,
    speakingMessageId: Long?,
    onSelectSubject: (WbbseSubject) -> Unit,
    onRegenerateNotes: () -> Unit,
    onToggleNoteCompleted: (String) -> Unit,
    onToggleSpeak: (Long, String) -> Unit,
    onExportNotesPdf: (String, String, String) -> Unit,
    onStartTimedExam: () -> Unit,
    modifier: Modifier = Modifier
) {
    val completedCount = state.notes.count { it.isCompleted }
    val progress = if (state.notes.isEmpty()) 0f else completedCount.toFloat() / state.notes.size.toFloat()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(WbbseSubject.entries, key = { it.id }) { subj ->
                    val selected = subj == state.selectedSubject
                    FilterChip(
                        selected = selected,
                        onClick = { onSelectSubject(subj) },
                        label = { Text(subj.bengaliName) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = subj.accentColor,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("brahmastram_subject_${subj.id}")
                    )
                }
            }
        }

        // Progress + Export All PDF + Start Timed Exam Action Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "পড়া সম্পন্ন: $completedCount / ${state.notes.size}টি শিওর-শট নোট",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = onRegenerateNotes) {
                            Icon(Icons.Rounded.Refresh, contentDescription = "Refresh Brahmastram")
                        }
                    }
                    LinearProgressIndicator(
                        progress = { progress },
                        color = Color(0xFF880E4F),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(999.dp))
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onStartTimedExam,
                            enabled = state.notes.isNotEmpty() && !state.isGenerating,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF880E4F)),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1.2f)
                                .minimumInteractiveComponentSize()
                                .testTag("start_brahmastram_exam_button")
                        ) {
                            Icon(Icons.Rounded.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("ব্রহ্মাস্ত্রম পরীক্ষা দাও", fontWeight = FontWeight.Bold, maxLines = 1)
                        }
                        FilledTonalButton(
                            onClick = {
                                val fullText = state.notes.joinToString("\n\n════════════════════\n\n") { n ->
                                    "■ ${n.title} (${n.chapter} • ${n.marksWeight} নম্বর • ${n.pyqBadge})\n${n.sureShotNoteBn}\n📌 মূল তথ্য: ${n.keyFormulaOrFact}\n\n${n.topperWritingStructureBn}"
                                }
                                onExportNotesPdf(
                                    "ব্রহ্মাস্ত্রম_${state.selectedSubject.bengaliName}_সাজেশন",
                                    "ব্রহ্মাস্ত্রম শিওর-শট সাজেশন — ${state.selectedSubject.bengaliName}",
                                    fullText
                                )
                            },
                            enabled = state.notes.isNotEmpty(),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(0.8f)
                                .minimumInteractiveComponentSize()
                                .testTag("export_brahmastram_pdf_button")
                        ) {
                            Icon(Icons.Rounded.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("PDF তৈরি", maxLines = 1)
                        }
                    }
                }
            }
        }

        if (state.isGenerating) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("ব্রহ্মাস্ত্রম শিওর-শট নোটস তৈরি হচ্ছে…")
                }
            }
        } else {
            itemsIndexed(state.notes, key = { _, n -> n.id }) { idx, note ->
                var showTopperTab by remember(note.id) { mutableStateOf(false) }
                val ttsId = 91_000L + idx
                val isSpeaking = speakingMessageId == ttsId

                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (note.isCompleted) {
                            SageOffline.copy(alpha = 0.08f)
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (note.isCompleted) SageOffline.copy(alpha = 0.45f) else MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Checkbox(
                                    checked = note.isCompleted,
                                    onCheckedChange = { onToggleNoteCompleted(note.id) }
                                )
                                Column {
                                    Text(
                                        text = "${idx + 1}. ${note.title}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${note.chapter} • ${note.marksWeight} নম্বর • ${note.pyqBadge}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFFC62828),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                            IconButton(
                                onClick = {
                                    onToggleSpeak(ttsId, "${note.title}। ${note.sureShotNoteBn}। মূল তথ্য: ${note.keyFormulaOrFact}")
                                },
                                modifier = Modifier.minimumInteractiveComponentSize()
                            ) {
                                Icon(
                                    imageVector = if (isSpeaking) Icons.Rounded.StopCircle else Icons.AutoMirrored.Rounded.VolumeUp,
                                    contentDescription = "Listen Note",
                                    tint = if (isSpeaking) TerracottaAccent else OnlineCobalt
                                )
                            }
                        }

                        // Inline Toggle: Normal Note vs "টপারের খাতায় যেভাবে লিখবে" (Feature #10)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = !showTopperTab,
                                onClick = { showTopperTab = false },
                                label = { Text("শিওর-শট নোটস") }
                            )
                            FilterChip(
                                selected = showTopperTab,
                                onClick = { showTopperTab = true },
                                label = { Text("🏆 টপারের খাতায় যেভাবে লিখবে") }
                            )
                        }

                        Text(
                            text = if (showTopperTab) note.topperWritingStructureBn else note.sureShotNoteBn,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (note.keyFormulaOrFact.isNotBlank()) {
                            Surface(
                                color = OnlineCobalt.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "📌 মূল সূত্র/তথ্য: ${note.keyFormulaOrFact}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = OnlineCobalt,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BrahmastramExamStep(
    state: BrahmastramUiState,
    onSelectMcqOption: (String, Int) -> Unit,
    onUpdateTypedAnswer: (String, String) -> Unit,
    onSubmitExam: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Surface(
                color = Color(0xFF880E4F).copy(alpha = 0.1f),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "📝 এই প্রশ্নপত্রটি সম্পূর্ণভাবে তোমার পড়া ব্রহ্মাস্ত্রম নোটস থেকেই তৈরি হয়েছে। সাবধানে উত্তর দাও — নেতিবাচক (‘নয়/ব্যতীত’) শব্দগুলো খেয়াল রাখো!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        itemsIndexed(state.examQuestions, key = { _, q -> q.id }) { idx, q ->
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "প্রশ্ন ${idx + 1} (${q.marks} নম্বর • ${q.chapter})",
                        style = MaterialTheme.typography.labelMedium,
                        color = OnlineCobalt,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = q.questionText,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )

                    if (q.options.isNotEmpty()) {
                        val chosen = state.selectedOptions[q.id]
                        q.options.forEachIndexed { optIdx, optText ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = chosen == optIdx,
                                    onClick = { onSelectMcqOption(q.id, optIdx) }
                                )
                                Text(
                                    text = optText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    } else {
                        OutlinedTextField(
                            value = state.typedAnswers[q.id].orEmpty(),
                            onValueChange = { onUpdateTypedAnswer(q.id, it) },
                            label = { Text("তোমার উত্তর এখানে লেখো (সূত্র, একক ও মূল পয়েন্টসহ)…") },
                            minLines = 3,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        item {
            Button(
                onClick = onSubmitExam,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF880E4F)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .minimumInteractiveComponentSize()
                    .testTag("submit_brahmastram_exam_button")
            ) {
                Icon(Icons.Rounded.CheckCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("উত্তরপত্র জমা দাও ও লাল-কালি মূল্যায়ন দেখো", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun BrahmastramResultStep(
    state: BrahmastramUiState,
    onBackToNotes: () -> Unit,
    onRetakeExam: () -> Unit,
    onExportReportPdf: (String, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val report = state.latestReport ?: return
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF880E4F).copy(alpha = 0.12f)),
                border = BorderStroke(1.2.dp, Color(0xFF880E4F).copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "ব্রহ্মাস্ত্রম ফলাফল: ${report.obtainedMarks} / ${report.maxMarks} (${report.accuracyPercent}%)",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF880E4F)
                    )
                    Text(
                        text = report.redPenSummaryBn,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (report.trapAlertsCount > 0) {
                        Surface(
                            color = Color(0xFFC62828).copy(alpha = 0.14f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Rounded.Warning, contentDescription = null, tint = Color(0xFFC62828))
                                Text(
                                    text = "🚨 লাল-কালি ও ট্র্যাপ অ্যালার্ট: এই পত্রে ${report.trapAlertsCount}টি নেতিবাচক ট্র্যাপ প্রশ্ন ছিল!",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color(0xFFC62828),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(onClick = onBackToNotes, modifier = Modifier.weight(1f)) {
                            Text("নোটসে ফিরুন")
                        }
                        Button(onClick = onRetakeExam, modifier = Modifier.weight(1f)) {
                            Text("আবার পরীক্ষা")
                        }
                    }
                    FilledTonalButton(
                        onClick = {
                            val body = buildString {
                                appendLine("প্রাপ্ত নম্বর: ${report.obtainedMarks}/${report.maxMarks} (${report.accuracyPercent}%)")
                                appendLine(report.redPenSummaryBn)
                                appendLine()
                                report.gradedItems.forEachIndexed { i, it ->
                                    appendLine("${i + 1}. ${it.question.questionText} [${it.obtainedMarks}/${it.maxMarks}]")
                                    it.redPenDeductions.forEach { d -> appendLine("   $d") }
                                    it.unitOrGrammarAudit.forEach { u -> appendLine("   $u") }
                                    appendLine()
                                }
                            }
                            onExportReportPdf(
                                "ব্রহ্মাস্ত্রম_ফলাফল_${report.subject.bengaliName}",
                                "ব্রহ্মাস্ত্রম লাল-কালি মূল্যায়ন রিপোর্ট — ${report.subject.bengaliName}",
                                body
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Rounded.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("রিপোর্ট PDF তৈরি করুন")
                    }
                }
            }
        }

        itemsIndexed(report.gradedItems, key = { _, it -> it.question.id }) { idx, item ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(
                    1.dp,
                    if (item.obtainedMarks >= item.maxMarks * 0.8f) SageOffline.copy(alpha = 0.45f)
                    else Color(0xFFC62828).copy(alpha = 0.45f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "প্রশ্ন ${idx + 1} (${item.question.chapter})",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${item.obtainedMarks}/${item.maxMarks}",
                            style = MaterialTheme.typography.labelLarge.copy(fontFamily = MonoFontFamily),
                            color = if (item.obtainedMarks >= item.maxMarks * 0.8f) SageOffline else Color(0xFFC62828),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(text = item.question.questionText, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)

                    if (item.question.trapKeywords.isNotEmpty()) {
                        Surface(
                            color = Color(0xFFFFEBEE),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "🚨 প্রশ্ন ট্র্যাপ শব্দ: ${item.question.trapKeywords.joinToString(", ")}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFC62828),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Red-Pen Deduction Log
                    Surface(
                        color = Color(0xFFC62828).copy(alpha = 0.07f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "✒️ নম্বর কাটার কারণ (Red-Pen Log):",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFC62828),
                                fontWeight = FontWeight.Bold
                            )
                            item.redPenDeductions.forEach { d ->
                                Text(text = d, style = MaterialTheme.typography.bodySmall)
                            }
                            item.unitOrGrammarAudit.forEach { u ->
                                Text(
                                    text = u,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = OnlineCobalt
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
