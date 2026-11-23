package com.example.ui.topper

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.R
import com.example.data.ai.ExamIntentRouter
import com.example.data.media.NotebookImageHelper
import com.example.model.InferenceMode
import com.example.model.TimingBucket
import com.example.model.TopperPaper
import com.example.model.TopperQuestion
import com.example.model.TopperSectionType
import com.example.ui.theme.MonoFontFamily
import com.example.ui.theme.OnlineCobalt
import com.example.ui.theme.SageOffline
import com.example.ui.theme.TerracottaAccent
import com.example.viewmodel.PendingPhotoAttachment
import com.example.viewmodel.TopperUiState

/**
 * Full WBBSE Madhyamik TOPPER Exam Hall Runner:
 * - Official Live Countdown Timer with Pause/Resume + automatic Section-Wise TimingBucket tracking
 * - Section Filter Pills (সব বিভাগ / MCQ / VSAQ-SAQ / বড় প্রশ্ন ও ম্যাপ)
 * - Twisted Question badges ("🌀 ঘোরানো প্রশ্ন")
 * - Dual Answer Submission: Keyboard Typing OR Multi-Page Camera/Gallery Handwritten Sheet Upload
 */
@Composable
fun TopperTestRunner(
    topperState: TopperUiState,
    paper: TopperPaper,
    inferenceMode: InferenceMode,
    onSelectTimingBucket: (TimingBucket) -> Unit,
    onToggleTimerPause: () -> Unit,
    onExitPaper: () -> Unit,
    onSelectMcqOption: (String, Int) -> Unit,
    onUpdateTypedAnswer: (String, TopperSectionType, String) -> Unit,
    onAddPhotoUri: (String, TopperSectionType, Uri) -> Unit,
    onRemovePhoto: (String, Int) -> Unit,
    onSubmitPaper: () -> Unit,
    onShowSnackbar: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val cameraDeniedMsg = stringResource(R.string.camera_permission_denied)

    var selectedBucketFilter by remember(paper.setNumber, paper.track) { mutableStateOf<TimingBucket?>(null) }
    var showExitConfirm by remember { mutableStateOf(false) }

    // Track which question initiated a Camera/Gallery capture
    var activePhotoTarget by remember { mutableStateOf<Pair<String, TopperSectionType>?>(null) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    BackHandler(enabled = !topperState.isEvaluatingSubmission) {
        showExitConfirm = true
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val uri = tempCameraUri
        val target = activePhotoTarget
        if (success && uri != null && target != null) {
            onAddPhotoUri(target.first, target.second, uri)
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
        val target = activePhotoTarget
        if (uri != null && target != null) {
            onAddPhotoUri(target.first, target.second, uri)
        }
    }

    val visibleQuestions = remember(paper.questions, selectedBucketFilter) {
        if (selectedBucketFilter == null) paper.questions
        else paper.questions.filter { it.section.timingBucket == selectedBucketFilter }
    }

    val attemptedCount = paper.questions.count { q ->
        if (q.section == TopperSectionType.SECTION_A_MCQ && q.options.isNotEmpty()) {
            topperState.mcqAnswers.containsKey(q.id)
        } else {
            !topperState.typedAnswers[q.id].isNullOrBlank() ||
                topperState.photoPagesByQuestion[q.id].orEmpty().isNotEmpty()
        }
    }

    val isUrgent = topperState.remainingSeconds in 1..300
    val timerAccent = when {
        topperState.isTimeUp -> MaterialTheme.colorScheme.error
        isUrgent -> TerracottaAccent
        else -> paper.track.accentColor
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Sticky Exam Hall Top Bar + Countdown Timer
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
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
                        onClick = { showExitConfirm = true },
                        enabled = !topperState.isEvaluatingSubmission,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("topper_runner_back")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back to Topper dashboard"
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = paper.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "পূর্ণমান: ${paper.fullMarks} • উত্তর দিয়েছ: $attemptedCount/${paper.questions.size} • ${paper.sourceBadge}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Live Countdown Timer Strip
                Surface(
                    color = timerAccent.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, timerAccent.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Timer,
                                contentDescription = null,
                                tint = timerAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = if (topperState.isTimeUp) {
                                    "⏰ সময় শেষ! এখনই খাতা জমা দাও"
                                } else {
                                    val timeStr = ExamIntentRouter.formatCountdownBengali(topperState.remainingSeconds)
                                    if (topperState.isTimerPaused) "⏸ বিরতি: $timeStr"
                                    else "বাকি সময়: $timeStr"
                                },
                                style = MaterialTheme.typography.labelLarge.copy(fontFamily = MonoFontFamily),
                                color = timerAccent,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (!topperState.isTimeUp) {
                            Surface(
                                onClick = onToggleTimerPause,
                                shape = RoundedCornerShape(10.dp),
                                color = timerAccent.copy(alpha = 0.18f),
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("topper_timer_pause")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (topperState.isTimerPaused) Icons.Rounded.PlayArrow else Icons.Rounded.Pause,
                                        contentDescription = if (topperState.isTimerPaused) "Resume" else "Pause",
                                        tint = timerAccent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = if (topperState.isTimerPaused) "চালু" else "বিরতি",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = timerAccent,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // Section Filter Pills (also syncs Section-Wise TimingBucket!)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item(key = "filter_all") {
                        FilterChip(
                            selected = selectedBucketFilter == null,
                            onClick = { selectedBucketFilter = null },
                            label = { Text("সব (${paper.questions.size})") }
                        )
                    }
                    TimingBucket.entries.forEach { bucket ->
                        val count = paper.questions.count { it.section.timingBucket == bucket }
                        if (count > 0) {
                            item(key = "filter_${bucket.name}") {
                                FilterChip(
                                    selected = selectedBucketFilter == bucket,
                                    onClick = {
                                        selectedBucketFilter = bucket
                                        onSelectTimingBucket(bucket)
                                    },
                                    label = { Text("${bucket.bengaliLabel} ($count)") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = paper.track.accentColor,
                                        selectedLabelColor = Color.White
                                    ),
                                    modifier = Modifier.testTag("topper_bucket_${bucket.name.lowercase()}")
                                )
                            }
                        }
                    }
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        // Questions Scrollable Feed
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .testTag("topper_questions_list"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // PYQ Pattern Banner
            item(key = "pyq_banner") {
                Surface(
                    color = paper.track.accentColor.copy(alpha = 0.09f),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, paper.track.accentColor.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "📊 বিগত বছরের বোর্ড প্যাটার্ন ও সাজেশন",
                            style = MaterialTheme.typography.labelMedium,
                            color = paper.track.accentColor,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = paper.previousYearTrendSummary,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (inferenceMode == InferenceMode.OFFLINE) {
                            Text(
                                text = "ℹ️ অফলাইন মোডে MCQ/SAQ স্বয়ংক্রিয়ভাবে মূল্যায়িত হবে; বড় প্রশ্নে ছবি যুক্ত করলে অনলাইন মোডে খাতা চেক করা যাবে।",
                                style = MaterialTheme.typography.labelSmall,
                                color = SageOffline
                            )
                        }
                    }
                }
            }

            itemsIndexed(visibleQuestions, key = { _, q -> q.id }) { idx, q ->
                TopperQuestionCard(
                    index = idx + 1,
                    question = q,
                    trackAccent = paper.track.accentColor,
                    selectedMcqIndex = topperState.mcqAnswers[q.id],
                    typedAnswer = topperState.typedAnswers[q.id].orEmpty(),
                    attachedPhotos = topperState.photoPagesByQuestion[q.id].orEmpty(),
                    onSelectMcqOption = { opt -> onSelectMcqOption(q.id, opt) },
                    onTypedAnswerChange = { txt -> onUpdateTypedAnswer(q.id, q.section, txt) },
                    onCaptureCamera = {
                        activePhotoTarget = q.id to q.section
                        val granted = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED
                        if (granted) {
                            val uri = NotebookImageHelper.createTempCameraImageUri(context)
                            tempCameraUri = uri
                            takePictureLauncher.launch(uri)
                        } else {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    onPickGallery = {
                        activePhotoTarget = q.id to q.section
                        galleryPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onRemovePhoto = { pageIdx -> onRemovePhoto(q.id, pageIdx) }
                )
            }

            // Submit Final Answer Sheet Button
            item(key = "topper_submit_footer") {
                Button(
                    onClick = onSubmitPaper,
                    enabled = !topperState.isEvaluatingSubmission,
                    colors = ButtonDefaults.buttonColors(containerColor = paper.track.accentColor),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .testTag("topper_submit_paper_button")
                ) {
                    if (topperState.isEvaluatingSubmission) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("টপার খাতা মূল্যায়ন ও বিশ্লেষণ চলছে…")
                    } else {
                        Icon(Icons.Rounded.AutoAwesome, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("খাতা জমা দাও ও টপার রিপোর্ট দেখো ($attemptedCount/${paper.questions.size})")
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }

    if (showExitConfirm) {
        AlertDialog(
            onDismissRequest = { showExitConfirm = false },
            title = { Text("টপার মক টেস্ট থেকে বের হবে?") },
            text = { Text("খাতা জমা না দিয়ে বের হলে এই সেটের উত্তর ও টাইমার মুছে যাবে।") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExitConfirm = false
                        onExitPaper()
                    }
                ) {
                    Text("হ্যাঁ, বের হও", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitConfirm = false }) {
                    Text("পরীক্ষা চালিয়ে যাও")
                }
            }
        )
    }
}

@Composable
private fun TopperQuestionCard(
    index: Int,
    question: TopperQuestion,
    trackAccent: Color,
    selectedMcqIndex: Int?,
    typedAnswer: String,
    attachedPhotos: List<PendingPhotoAttachment>,
    onSelectMcqOption: (Int) -> Unit,
    onTypedAnswerChange: (String) -> Unit,
    onCaptureCamera: () -> Unit,
    onPickGallery: () -> Unit,
    onRemovePhoto: (Int) -> Unit
) {
    val isMcq = question.section == TopperSectionType.SECTION_A_MCQ && question.options.isNotEmpty()
    val allowPhotoUpload = !isMcq

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            width = if (question.isTwisted) 1.5.dp else 1.dp,
            color = if (question.isTwisted) TerracottaAccent.copy(alpha = 0.65f)
            else MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Section + Marks + Twisted Badge Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        color = trackAccent.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "বিভাগ ${question.section.code} • ${question.chapter}",
                            style = MaterialTheme.typography.labelSmall,
                            color = trackAccent,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    if (question.isTwisted) {
                        Surface(
                            color = TerracottaAccent.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Psychology,
                                    contentDescription = "Twisted Question",
                                    tint = TerracottaAccent,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "ঘোরানো প্রশ্ন",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TerracottaAccent,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${question.marks} নম্বর",
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = MonoFontFamily),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            // Question Text
            Text(
                text = "প্রশ্ন $index. ${question.questionText}",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Answer Entry: MCQ Radio List OR Dual Submission (Typed + Multi-Page Handwritten Photo Sheets)
            if (isMcq) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    question.options.forEachIndexed { optIdx, optText ->
                        val isSelected = selectedMcqIndex == optIdx
                        Surface(
                            onClick = { onSelectMcqOption(optIdx) },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) trackAccent.copy(alpha = 0.10f) else Color.Transparent,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) trackAccent else MaterialTheme.colorScheme.outlineVariant
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { onSelectMcqOption(optIdx) }
                                )
                                Text(
                                    text = optText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            } else {
                OutlinedTextField(
                    value = typedAnswer,
                    onValueChange = onTypedAnswerChange,
                    label = {
                        Text(
                            if (question.marks >= 3) {
                                "উত্তর টাইপ করো অথবা নিচে খাতার পাতার ছবি যুক্ত করো"
                            } else {
                                "সংক্ষিপ্ত উত্তর লেখো (বাংলা বা English)"
                            }
                        )
                    },
                    minLines = if (question.marks >= 3) 3 else 2,
                    maxLines = 7,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                if (allowPhotoUpload) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (attachedPhotos.isNotEmpty()) {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                itemsIndexed(attachedPhotos) { pageIdx, photo ->
                                    Box(
                                        modifier = Modifier
                                            .size(width = 104.dp, height = 128.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                    ) {
                                        Image(
                                            bitmap = photo.previewBitmap.asImageBitmap(),
                                            contentDescription = "Handwritten answer page ${pageIdx + 1}",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                        Surface(
                                            color = Color.Black.copy(alpha = 0.65f),
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier
                                                .align(Alignment.BottomStart)
                                                .padding(4.dp)
                                        ) {
                                            Text(
                                                text = "পাতা ${pageIdx + 1}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Surface(
                                            onClick = { onRemovePhoto(pageIdx) },
                                            shape = CircleShape,
                                            color = Color.Black.copy(alpha = 0.7f),
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(4.dp)
                                                .size(28.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Rounded.Close,
                                                    contentDescription = "Remove page",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilledTonalButton(
                                onClick = onCaptureCamera,
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .minimumInteractiveComponentSize()
                            ) {
                                Icon(Icons.Rounded.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    if (attachedPhotos.isEmpty()) "খাতার ছবি তোলো" else "+ আরও পাতা তোলো",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }

                            OutlinedButton(
                                onClick = onPickGallery,
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .minimumInteractiveComponentSize()
                            ) {
                                Icon(Icons.Rounded.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("গ্যালারি", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}
