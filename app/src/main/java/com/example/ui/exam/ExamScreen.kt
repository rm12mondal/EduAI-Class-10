package com.example.ui.exam

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.HighlightOff
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.R
import com.example.data.exam.WbbseExamBank
import com.example.data.media.NotebookImageHelper
import com.example.model.ExamFormat
import com.example.model.InferenceMode
import com.example.model.McqQuestion
import com.example.model.SubjectiveEvaluationResult
import com.example.model.WbbseSubject
import com.example.ui.theme.MonoFontFamily
import com.example.ui.theme.OnlineCobalt
import com.example.ui.theme.SageOffline
import com.example.ui.theme.TerracottaAccent
import com.example.viewmodel.EduAiUiState
import com.example.viewmodel.PendingPhotoAttachment
import java.util.Locale

@Composable
fun ExamScreen(
    uiState: EduAiUiState,
    onSelectSubject: (WbbseSubject) -> Unit,
    onSelectFormat: (ExamFormat) -> Unit,
    onRecordMcqResult: (WbbseSubject, Int, Int) -> Unit,
    onAttachExamPhotoUri: (Uri) -> Unit,
    onClearExamPhoto: () -> Unit,
    onEvaluateSubjective: (WbbseSubject, ExamFormat, String, Int, String, List<String>, String) -> Unit,
    onClearEvaluation: () -> Unit,
    onClearExamHistory: () -> Unit,
    onToggleExamSmartMode: () -> Unit,
    onToggleExamVisualMode: () -> Unit,
    onToggleExamTranslateMode: () -> Unit,
    onSelectExamTargetMarks: (Int?) -> Unit,
    onAttachExamChatPhotoUri: (Uri) -> Unit,
    onClearExamChatPhoto: () -> Unit,
    onToggleExamSessionPause: () -> Unit,
    onEndExamSession: () -> Unit,
    onClearExamChat: () -> Unit,
    onSendExamChatMessage: (String) -> Unit,
    onToggleSpeakMessage: (Long, String) -> Unit,
    onOpenRapidFire: (WbbseSubject?) -> Unit,
    onShowSnackbar: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // 0 = 3-in-1 Smart Exam Chat, 1 = Offline 252+ Question Bank, 2 = Score & Progress Dashboard
    var activeSubTab by rememberSaveable { mutableIntStateOf(0) }

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
            // Header + 3-Way Segmented Switch (স্মার্ট চ্যাট ↔ অফলাইন ব্যাংক ↔ ফলাফল)
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.exams_title),
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = stringResource(R.string.exams_subtitle),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            onClick = { onOpenRapidFire(uiState.selectedExamSubject) },
                            shape = RoundedCornerShape(999.dp),
                            color = TerracottaAccent.copy(alpha = 0.14f),
                            border = BorderStroke(1.dp, TerracottaAccent.copy(alpha = 0.45f)),
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("exam_header_rapid_fire_btn")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Bolt,
                                    contentDescription = "Rapid Fire MCQ",
                                    tint = TerracottaAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "র্যাপিড ফায়ার",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = TerracottaAccent,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        SegmentedButton(
                            selected = activeSubTab == 0,
                            onClick = { activeSubTab = 0 },
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3),
                            modifier = Modifier.testTag("exam_subtab_chat")
                        ) {
                            Text(
                                text = "স্মার্ট চ্যাট",
                                maxLines = 1
                            )
                        }
                        SegmentedButton(
                            selected = activeSubTab == 1,
                            onClick = { activeSubTab = 1 },
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3),
                            modifier = Modifier.testTag("exam_subtab_practice")
                        ) {
                            Text(
                                text = "ব্যাংক (${WbbseExamBank.totalQuestionCount})",
                                maxLines = 1
                            )
                        }
                        SegmentedButton(
                            selected = activeSubTab == 2,
                            onClick = { activeSubTab = 2 },
                            shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3),
                            modifier = Modifier.testTag("exam_subtab_progress")
                        ) {
                            Text(
                                text = "ফলাফল (${uiState.examAttempts.size})",
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            when (activeSubTab) {
                0 -> ExamChatSection(
                    uiState = uiState,
                    onToggleSmartMode = onToggleExamSmartMode,
                    onToggleVisualMode = onToggleExamVisualMode,
                    onToggleTranslateMode = onToggleExamTranslateMode,
                    onSelectTargetMarks = onSelectExamTargetMarks,
                    onAttachPhotoUri = onAttachExamChatPhotoUri,
                    onClearPendingPhoto = onClearExamChatPhoto,
                    onToggleSessionPause = onToggleExamSessionPause,
                    onEndExamSession = onEndExamSession,
                    onClearExamChat = onClearExamChat,
                    onSendExamMessage = onSendExamChatMessage,
                    onToggleSpeakMessage = onToggleSpeakMessage,
                    onShowSnackbar = onShowSnackbar,
                    modifier = Modifier.weight(1f)
                )

                1 -> ExamPracticeSection(
                    uiState = uiState,
                    onSelectSubject = onSelectSubject,
                    onSelectFormat = onSelectFormat,
                    onRecordMcqResult = onRecordMcqResult,
                    onAttachExamPhotoUri = onAttachExamPhotoUri,
                    onClearExamPhoto = onClearExamPhoto,
                    onEvaluateSubjective = onEvaluateSubjective,
                    onClearEvaluation = onClearEvaluation,
                    onOpenRapidFire = onOpenRapidFire,
                    onShowSnackbar = onShowSnackbar,
                    modifier = Modifier.weight(1f)
                )

                else -> ExamProgressDashboard(
                    stats = uiState.progressStats,
                    attempts = uiState.examAttempts,
                    onPracticeSubject = { subj ->
                        onSelectSubject(subj)
                        activeSubTab = 1
                    },
                    onClearHistory = onClearExamHistory,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ExamPracticeSection(
    uiState: EduAiUiState,
    onSelectSubject: (WbbseSubject) -> Unit,
    onSelectFormat: (ExamFormat) -> Unit,
    onRecordMcqResult: (WbbseSubject, Int, Int) -> Unit,
    onAttachExamPhotoUri: (Uri) -> Unit,
    onClearExamPhoto: () -> Unit,
    onEvaluateSubjective: (WbbseSubject, ExamFormat, String, Int, String, List<String>, String) -> Unit,
    onClearEvaluation: () -> Unit,
    onOpenRapidFire: (WbbseSubject?) -> Unit,
    onShowSnackbar: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val subject = uiState.selectedExamSubject
    val format = uiState.selectedExamFormat

    Column(modifier = modifier.fillMaxSize()) {
        // Rapid Fire Speed Booster Promo Strip
        Surface(
            onClick = { onOpenRapidFire(subject) },
            color = TerracottaAccent.copy(alpha = 0.11f),
            border = BorderStroke(1.dp, TerracottaAccent.copy(alpha = 0.35f)),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .testTag("exam_practice_rapid_fire_banner")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Bolt,
                    contentDescription = null,
                    tint = TerracottaAccent,
                    modifier = Modifier.size(22.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "⚡ র্যাপিড ফায়ার (Speed Booster — ২০/৩০ MCQ)",
                        style = MaterialTheme.typography.labelLarge,
                        color = TerracottaAccent,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "৩০ সেকেন্ড/প্রশ্ন বা ১৫ মিনিট টাইমার • ১০০% অফলাইন • ভুল উত্তর অটো-ভুলের খাতায় সেভ হয়",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        // 7-Subject Selector
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            items(WbbseSubject.entries, key = { it.id }) { item ->
                val isSelected = item == subject
                FilterChip(
                    selected = isSelected,
                    onClick = { onSelectSubject(item) },
                    label = { Text(item.bengaliName) },
                    leadingIcon = {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (isSelected) Color.White else item.accentColor
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = item.accentColor,
                        selectedLabelColor = Color.White,
                        selectedLeadingIconColor = Color.White
                    ),
                    modifier = Modifier.testTag("exam_subject_${item.id}")
                )
            }
        }

        // 3 Exam Format Chips (MCQ, SAQ, Long)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(start = 16.dp, end = 16.dp, bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ExamFormat.entries.forEach { examFormat ->
                val selected = examFormat == format
                FilterChip(
                    selected = selected,
                    onClick = { onSelectFormat(examFormat) },
                    label = { Text(examFormat.shortLabel) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("exam_format_${examFormat.id}")
                )
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        when (format) {
            ExamFormat.MCQ -> McqExamRunner(
                subject = subject,
                onSubmitQuiz = onRecordMcqResult,
                modifier = Modifier.weight(1f)
            )

            ExamFormat.SAQ -> SubjectiveExamRunner(
                subject = subject,
                format = ExamFormat.SAQ,
                inferenceMode = uiState.inferenceMode,
                isEvaluating = uiState.isEvaluatingExam,
                evaluationResult = uiState.latestSubjectiveEvaluation,
                pendingPhoto = null,
                allowPhotoUpload = false,
                onAttachPhotoUri = onAttachExamPhotoUri,
                onClearPhoto = onClearExamPhoto,
                onEvaluate = onEvaluateSubjective,
                onClearEvaluation = onClearEvaluation,
                onShowSnackbar = onShowSnackbar,
                modifier = Modifier.weight(1f)
            )

            ExamFormat.LONG -> SubjectiveExamRunner(
                subject = subject,
                format = ExamFormat.LONG,
                inferenceMode = uiState.inferenceMode,
                isEvaluating = uiState.isEvaluatingExam,
                evaluationResult = uiState.latestSubjectiveEvaluation,
                pendingPhoto = uiState.pendingExamPhoto,
                allowPhotoUpload = true,
                onAttachPhotoUri = onAttachExamPhotoUri,
                onClearPhoto = onClearExamPhoto,
                onEvaluate = onEvaluateSubjective,
                onClearEvaluation = onClearEvaluation,
                onShowSnackbar = onShowSnackbar,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun McqExamRunner(
    subject: WbbseSubject,
    onSubmitQuiz: (WbbseSubject, Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val allQuestions = remember(subject) { WbbseExamBank.getMcqs(subject) }
    val questionSets = remember(allQuestions) { allQuestions.chunked(5) }
    var selectedSetIndex by remember(subject) { mutableIntStateOf(0) }
    val questions = questionSets.getOrElse(selectedSetIndex) { allQuestions }
    val selectedAnswers = remember(subject, selectedSetIndex) { mutableStateMapOf<String, Int>() }
    var submitted by remember(subject, selectedSetIndex) { mutableStateOf(false) }

    val correctCount = questions.count { q -> selectedAnswers[q.id] == q.correctIndex }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("mcq_exam_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (questionSets.size > 1) {
            item(key = "mcq_set_selector") {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    itemsIndexed(questionSets) { setIdx, chunk ->
                        FilterChip(
                            selected = setIdx == selectedSetIndex,
                            onClick = { selectedSetIndex = setIdx },
                            label = { Text("সেট ${setIdx + 1} (${chunk.size})") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = subject.accentColor,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("mcq_set_chip_${setIdx + 1}")
                        )
                    }
                }
            }
        }

        if (submitted) {
            item {
                val pct = ((correctCount.toFloat() / questions.size.coerceAtLeast(1)) * 100).toInt()
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (pct >= 60) SageOffline.copy(alpha = 0.12f) else TerracottaAccent.copy(alpha = 0.12f)
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (pct >= 60) SageOffline else TerracottaAccent
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "ফলাফল সংরক্ষিত হয়েছে!",
                                style = MaterialTheme.typography.titleMedium,
                                color = if (pct >= 60) SageOffline else TerracottaAccent
                            )
                            Text(
                                text = "${subject.bengaliName} MCQ স্কোর: $correctCount / ${questions.size} ($pct%)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        FilledTonalButton(
                            onClick = {
                                selectedAnswers.clear()
                                submitted = false
                            },
                            modifier = Modifier.testTag("mcq_retry_button")
                        ) {
                            Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("আবার দাও")
                        }
                    }
                }
            }
        }

        itemsIndexed(questions, key = { _, q -> q.id }) { idx, q ->
            McqQuestionCard(
                index = idx + 1,
                question = q,
                selectedOption = selectedAnswers[q.id],
                isSubmitted = submitted,
                onSelectOption = { optIdx ->
                    if (!submitted) selectedAnswers[q.id] = optIdx
                }
            )
        }

        if (!submitted) {
            item {
                Button(
                    onClick = {
                        submitted = true
                        onSubmitQuiz(subject, correctCount, questions.size)
                    },
                    enabled = selectedAnswers.size == questions.size,
                    colors = ButtonDefaults.buttonColors(containerColor = subject.accentColor),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .testTag("mcq_submit_button")
                ) {
                    Icon(Icons.Rounded.CheckCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (selectedAnswers.size == questions.size) {
                            "MCQ জমা দাও ও স্কোর দেখো"
                        } else {
                            "সব প্রশ্নের উত্তর বাছো (${selectedAnswers.size}/${questions.size})"
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun McqQuestionCard(
    index: Int,
    question: McqQuestion,
    selectedOption: Int?,
    isSubmitted: Boolean,
    onSelectOption: (Int) -> Unit
) {
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
                text = "প্রশ্ন $index. ${question.question}",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            question.options.forEachIndexed { optIndex, optionText ->
                val isSelected = selectedOption == optIndex
                val isCorrect = optIndex == question.correctIndex
                val borderColor = when {
                    isSubmitted && isCorrect -> SageOffline
                    isSubmitted && isSelected && !isCorrect -> MaterialTheme.colorScheme.error
                    isSelected -> question.subject.accentColor
                    else -> MaterialTheme.colorScheme.outlineVariant
                }
                val bgColor = when {
                    isSubmitted && isCorrect -> SageOffline.copy(alpha = 0.10f)
                    isSubmitted && isSelected && !isCorrect -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                    isSelected -> question.subject.accentColor.copy(alpha = 0.08f)
                    else -> Color.Transparent
                }

                Surface(
                    onClick = { onSelectOption(optIndex) },
                    enabled = !isSubmitted,
                    shape = RoundedCornerShape(12.dp),
                    color = bgColor,
                    border = BorderStroke(1.dp, borderColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = if (isSubmitted) null else ({ onSelectOption(optIndex) })
                        )
                        Text(
                            text = optionText,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        if (isSubmitted && isCorrect) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = "Correct",
                                tint = SageOffline,
                                modifier = Modifier.size(18.dp)
                            )
                        } else if (isSubmitted && isSelected && !isCorrect) {
                            Icon(
                                imageVector = Icons.Rounded.HighlightOff,
                                contentDescription = "Incorrect",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            if (isSubmitted) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "💡 ব্যাখ্যা: ${question.explanation}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SubjectiveExamRunner(
    subject: WbbseSubject,
    format: ExamFormat,
    inferenceMode: InferenceMode,
    isEvaluating: Boolean,
    evaluationResult: SubjectiveEvaluationResult?,
    pendingPhoto: PendingPhotoAttachment?,
    allowPhotoUpload: Boolean,
    onAttachPhotoUri: (Uri) -> Unit,
    onClearPhoto: () -> Unit,
    onEvaluate: (WbbseSubject, ExamFormat, String, Int, String, List<String>, String) -> Unit,
    onClearEvaluation: () -> Unit,
    onShowSnackbar: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val cameraDeniedMsg = stringResource(R.string.camera_permission_denied)

    val items = remember(subject, format) {
        if (format == ExamFormat.SAQ) {
            WbbseExamBank.getSaqs(subject).map {
                SubjectivePromptItem(it.id, it.question, it.maxMarks, it.modelAnswer, it.keyTerms)
            }
        } else {
            WbbseExamBank.getLongQuestions(subject).map {
                SubjectivePromptItem(it.id, it.question, it.maxMarks, it.modelOutline, it.keyTerms)
            }
        }
    }

    var selectedIndex by remember(subject, format) { mutableIntStateOf(0) }
    var typedAnswer by remember(subject, format, selectedIndex) { mutableStateOf("") }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    val activeItem = items.getOrElse(selectedIndex) { items.first() }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val capturedUri = tempCameraUri
        if (success && capturedUri != null) {
            onAttachPhotoUri(capturedUri)
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val uri = NotebookImageHelper.createTempCameraImageUri(context)
            tempCameraUri = uri
            takePictureLauncher.launch(uri)
        } else {
            onShowSnackbar(cameraDeniedMsg)
        }
    }

    val galleryPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onAttachPhotoUri(uri)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("subjective_exam_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Question Selector Pills (Scrollable for all 10 SAQs / 5 Broads)
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                itemsIndexed(items) { idx, _ ->
                    FilterChip(
                        selected = idx == selectedIndex,
                        onClick = {
                            selectedIndex = idx
                            onClearEvaluation()
                            onClearPhoto()
                        },
                        label = { Text("প্রশ্ন ${idx + 1}") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = subject.accentColor,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        // Question Prompt Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, subject.accentColor.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${subject.bengaliName} • ${format.bengaliLabel}",
                            style = MaterialTheme.typography.labelMedium,
                            color = subject.accentColor
                        )
                        Surface(
                            color = subject.accentColor.copy(alpha = 0.14f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "পূর্ণমান: ${activeItem.maxMarks}",
                                style = MaterialTheme.typography.labelSmall,
                                color = subject.accentColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = activeItem.question,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (inferenceMode == InferenceMode.ONLINE) {
                            "অনলাইন AI মূল্যায়ন (Firebase AI Logic • Gemini) সক্রিয়"
                        } else {
                            "অফলাইন রুব্রিক মূল্যায়ন সক্রিয় (খাতার ছবি মূল্যায়নে অনলাইন মোড চালু করো)"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Answer Input Box
        item {
            OutlinedTextField(
                value = typedAnswer,
                onValueChange = { typedAnswer = it },
                label = {
                    Text(
                        if (allowPhotoUpload) {
                            "তোমার উত্তর এখানে টাইপ করো (অথবা খাতার ছবি দাও)"
                        } else {
                            "তোমার সংক্ষিপ্ত উত্তর লেখো (বাংলা বা English)"
                        }
                    )
                },
                minLines = if (allowPhotoUpload) 5 else 3,
                maxLines = 9,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("subjective_answer_input")
            )
        }

        // Camera & Gallery Notebook Photo Upload for Long Questions
        if (allowPhotoUpload) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "অথবা খাতায় লিখে ছবি তুলে জমা দাও",
                            style = MaterialTheme.typography.labelLarge
                        )

                        if (pendingPhoto != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(170.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            ) {
                                Image(
                                    bitmap = pendingPhoto.previewBitmap.asImageBitmap(),
                                    contentDescription = "Attached handwritten answer photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Surface(
                                    onClick = onClearPhoto,
                                    shape = CircleShape,
                                    color = Color.Black.copy(alpha = 0.65f),
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(8.dp)
                                        .size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Rounded.Close,
                                            contentDescription = "Remove photo",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilledTonalButton(
                                onClick = {
                                    val hasCamera = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.CAMERA
                                    ) == PackageManager.PERMISSION_GRANTED
                                    if (hasCamera) {
                                        val uri = NotebookImageHelper.createTempCameraImageUri(context)
                                        tempCameraUri = uri
                                        takePictureLauncher.launch(uri)
                                    } else {
                                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .minimumInteractiveComponentSize()
                                    .testTag("exam_camera_button")
                            ) {
                                Icon(Icons.Rounded.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("ক্যামেরা")
                            }

                            OutlinedButton(
                                onClick = {
                                    galleryPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .minimumInteractiveComponentSize()
                                    .testTag("exam_gallery_button")
                            ) {
                                Icon(Icons.Rounded.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("গ্যালারি")
                            }
                        }
                    }
                }
            }
        }

        // Submit & Evaluate Button
        item {
            Button(
                onClick = {
                    onEvaluate(
                        subject,
                        format,
                        activeItem.question,
                        activeItem.maxMarks,
                        activeItem.modelAnswer,
                        activeItem.keyTerms,
                        typedAnswer
                    )
                },
                enabled = !isEvaluating && (typedAnswer.isNotBlank() || pendingPhoto != null),
                colors = ButtonDefaults.buttonColors(containerColor = subject.accentColor),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .minimumInteractiveComponentSize()
                    .testTag("subjective_submit_button")
            ) {
                if (isEvaluating) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("মূল্যায়ন করা হচ্ছে…")
                } else {
                    Icon(Icons.Rounded.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("উত্তর জমা দাও ও মূল্যায়ন দেখো")
                }
            }
        }

        // Evaluation Result Card
        evaluationResult?.let { result ->
            item {
                SubjectiveEvaluationResultCard(result = result)
            }
        }
    }
}

@Composable
private fun SubjectiveEvaluationResultCard(result: SubjectiveEvaluationResult) {
    val pct = if (result.maxMarks > 0f) ((result.scoreObtained / result.maxMarks) * 100f).toInt() else 0
    val accent = when {
        pct >= 75 -> SageOffline
        pct >= 50 -> OnlineCobalt
        else -> TerracottaAccent
    }
    val scoreStr = if (result.scoreObtained % 1f == 0f) {
        "${result.scoreObtained.toInt()}/${result.maxMarks.toInt()}"
    } else {
        String.format(Locale.US, "%.1f/%d", result.scoreObtained, result.maxMarks.toInt())
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.5.dp, accent),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("subjective_evaluation_card")
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
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Rounded.Verified, contentDescription = null, tint = accent)
                    Text(
                        text = "মূল্যায়ন ফলাফল",
                        style = MaterialTheme.typography.titleMedium,
                        color = accent
                    )
                }
                Surface(
                    color = accent.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "$scoreStr নম্বর ($pct%)",
                        style = MaterialTheme.typography.labelLarge.copy(fontFamily = MonoFontFamily),
                        color = accent,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Text(
                text = result.verdict,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )

            FeedbackBlock(title = "✅ ভালো দিক", body = result.strengths)
            FeedbackBlock(title = "⚠️ ভুলত্রুটি ও ঘাটতি", body = result.mistakesAndGaps)
            FeedbackBlock(title = "🎯 পূর্ণ নম্বর পাওয়ার পরামর্শ", body = result.improvementTips)

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "📘 আদর্শ উত্তর / রূপরেখা:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = result.modelAnswer,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Text(
                text = "মূল্যায়নকারী: ${result.evaluatorSource} • ফলাফল সংরক্ষিত হয়েছে",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FeedbackBlock(title: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private data class SubjectivePromptItem(
    val id: String,
    val question: String,
    val maxMarks: Int,
    val modelAnswer: String,
    val keyTerms: List<String>
)
