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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.AutoGraph
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.R
import com.example.data.ai.ExamIntentRouter
import com.example.data.media.NotebookImageHelper
import com.example.model.ActiveExamSession
import com.example.model.ExamChatIntent
import com.example.model.ExamChatMessage
import com.example.model.ExamSessionMode
import com.example.model.InferenceMode
import com.example.ui.chat.DiagramGeneratingBanner
import com.example.ui.chat.FullScreenDiagramDialog
import com.example.ui.chat.GeneratedDiagramCard
import com.example.ui.chat.SmartAnswerAndToolsStrip
import com.example.ui.chat.rememberVoiceInputLauncher
import com.example.ui.theme.MonoFontFamily
import com.example.ui.theme.OnlineCobalt
import com.example.ui.theme.SageOffline
import com.example.ui.theme.TerracottaAccent
import com.example.viewmodel.EduAiUiState
import com.example.viewmodel.PendingPhotoAttachment
import com.example.viewmodel.StreamingDraft

/**
 * 3-in-1 Intelligent WBBSE Madhyamik Exam & Study Chat Box:
 * - Intent A: Direct Study / Condense Answer (Smart To-The-Point Board Format)
 * - Intent B: Normal Test (Untimed, Chapter / Page targeted)
 * - Intent C: Official WBBSE Madhyamik Test with Live Bengali Countdown Timer
 * - Camera / Gallery Handwritten Answer-Sheet Grading + AI Visual 2D Diagrams + Mic + TTS
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamChatSection(
    uiState: EduAiUiState,
    onToggleSmartMode: () -> Unit,
    onToggleVisualMode: () -> Unit,
    onToggleTranslateMode: () -> Unit,
    onSelectTargetMarks: (Int?) -> Unit,
    onAttachPhotoUri: (Uri) -> Unit,
    onClearPendingPhoto: () -> Unit,
    onToggleSessionPause: () -> Unit,
    onEndExamSession: () -> Unit,
    onClearExamChat: () -> Unit,
    onSendExamMessage: (String) -> Unit,
    onToggleSpeakMessage: (Long, String) -> Unit,
    onShowSnackbar: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val examChat = uiState.examChat
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val listState = rememberLazyListState()

    var inputText by rememberSaveable { mutableStateOf("") }
    var showPhotoSheet by remember { mutableStateOf(false) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    var zoomedDiagramPath by remember { mutableStateOf<String?>(null) }

    val cameraDeniedMsg = stringResource(R.string.camera_permission_denied)
    val copiedMsg = stringResource(R.string.chat_copied_toast)

    val launchVoiceInput = rememberVoiceInputLauncher(
        onResultText = { spoken ->
            inputText = if (inputText.isBlank()) spoken else "$inputText $spoken"
        },
        onUnavailable = onShowSnackbar
    )

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val uri = tempCameraUri
        if (success && uri != null) {
            onAttachPhotoUri(uri)
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

    LaunchedEffect(examChat.messages.size, examChat.streamingDraft?.text?.length) {
        val totalItems = examChat.messages.size + if (examChat.streamingDraft != null) 1 else 0
        if (totalItems > 0) {
            listState.animateScrollToItem(totalItems - 1)
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Active Normal Test or Madhyamik Exam Hall Countdown Banner
        examChat.activeSession?.let { session ->
            ActiveExamSessionBanner(
                session = session,
                onTogglePause = onToggleSessionPause,
                onEndSession = onEndExamSession
            )
        }

        // Messages Area or 3-in-1 Quick-Launch Empty State
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (examChat.messages.isEmpty() && examChat.streamingDraft == null) {
                ExamChatEmptyState(
                    inferenceMode = uiState.inferenceMode,
                    onQuickPrompt = { prompt ->
                        onSendExamMessage(prompt)
                    },
                    onOpenPhotoPicker = { showPhotoSheet = true }
                )
            } else {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("exam_chat_messages_list")
                ) {
                    item(key = "exam_chat_header_actions") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (uiState.inferenceMode == InferenceMode.ONLINE) {
                                    "অনলাইন ৩-ইন-১ পরীক্ষক (Gemini)"
                                } else {
                                    "অফলাইন পরীক্ষক (WBBSE ব্যাংক + Llama)"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = if (uiState.inferenceMode == InferenceMode.ONLINE) OnlineCobalt else SageOffline
                            )
                            Surface(
                                onClick = onClearExamChat,
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.testTag("exam_chat_clear_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.DeleteOutline,
                                        contentDescription = "Clear exam chat",
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        text = "নতুন সেশন",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    }

                    items(examChat.messages, key = { it.id }) { msg ->
                        ExamChatMessageBubble(
                            message = msg,
                            isSpeaking = uiState.ttsState.speakingMessageId == msg.id,
                            onSpeak = { onToggleSpeakMessage(msg.id, msg.content) },
                            onCopy = {
                                clipboard.setText(AnnotatedString(msg.content))
                                onShowSnackbar(copiedMsg)
                            },
                            onZoomDiagram = { path -> zoomedDiagramPath = path }
                        )
                    }

                    examChat.streamingDraft?.let { draft ->
                        item(key = "exam_chat_streaming") {
                            ExamStreamingDraftBubble(draft = draft)
                        }
                    }
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        // Exam Chat Composer Bar (Smart To-The-Point + Marks + Translate + Photo + Mic + AI Visual)
        ExamChatComposer(
            inputText = inputText,
            onInputChange = { inputText = it },
            smartMode = examChat.smartToThePointMode,
            visualMode = examChat.visualDiagramMode,
            translateMode = examChat.translateMode,
            targetMarks = examChat.targetMarks,
            pendingPhoto = examChat.pendingPhoto,
            isGenerating = examChat.isGenerating,
            inferenceMode = uiState.inferenceMode,
            onToggleSmartMode = onToggleSmartMode,
            onToggleVisualMode = onToggleVisualMode,
            onToggleTranslateMode = onToggleTranslateMode,
            onSelectTargetMarks = onSelectTargetMarks,
            onOpenPhotoPicker = { showPhotoSheet = true },
            onClearPendingPhoto = onClearPendingPhoto,
            onLaunchMic = launchVoiceInput,
            onSend = {
                val msg = inputText
                inputText = ""
                onSendExamMessage(msg)
            }
        )
    }

    if (showPhotoSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPhotoSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "হাতের লেখা খাতার ছবি জমা দাও (৩/৪/৫/৮ নম্বর মূল্যায়ন)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "খাতার পরিষ্কার ছবি তুলে বা গ্যালারি থেকে দিলে AI WBBSE বোর্ডের নিয়মে নম্বর ও উন্নতির টিপস দেবে।",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        onClick = {
                            showPhotoSheet = false
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
                        shape = RoundedCornerShape(16.dp),
                        color = OnlineCobalt.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, OnlineCobalt.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .testTag("exam_chat_sheet_camera")
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Rounded.CameraAlt, contentDescription = "Camera", tint = OnlineCobalt)
                            Text("ক্যামেরা", style = MaterialTheme.typography.labelLarge, color = OnlineCobalt)
                        }
                    }

                    Surface(
                        onClick = {
                            showPhotoSheet = false
                            galleryPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        shape = RoundedCornerShape(16.dp),
                        color = TerracottaAccent.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, TerracottaAccent.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .testTag("exam_chat_sheet_gallery")
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Rounded.PhotoLibrary, contentDescription = "Gallery", tint = TerracottaAccent)
                            Text("গ্যালারি", style = MaterialTheme.typography.labelLarge, color = TerracottaAccent)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    zoomedDiagramPath?.let { path ->
        FullScreenDiagramDialog(
            imagePath = path,
            onDismiss = { zoomedDiagramPath = null }
        )
    }
}

@Composable
private fun ActiveExamSessionBanner(
    session: ActiveExamSession,
    onTogglePause: () -> Unit,
    onEndSession: () -> Unit
) {
    val isMadhyamik = session.mode == ExamSessionMode.MADHYAMIK_TIMED
    val isUrgent = isMadhyamik && session.remainingSeconds in 1..300
    val accent = when {
        session.isTimeUp -> MaterialTheme.colorScheme.error
        isUrgent -> TerracottaAccent
        isMadhyamik -> OnlineCobalt
        else -> SageOffline
    }

    Surface(
        color = accent.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.45f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("exam_active_session_banner")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = if (isMadhyamik) Icons.Rounded.Timer else Icons.Rounded.Description,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = session.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = accent,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (isMadhyamik && !session.isTimeUp) {
                        IconButton(
                            onClick = onTogglePause,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("exam_timer_pause_button")
                        ) {
                            Icon(
                                imageVector = if (session.isPaused) Icons.Rounded.PlayArrow else Icons.Rounded.Pause,
                                contentDescription = if (session.isPaused) "Resume timer" else "Pause timer",
                                tint = accent
                            )
                        }
                    }
                    IconButton(
                        onClick = onEndSession,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("exam_session_end_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "End exam session",
                            tint = accent
                        )
                    }
                }
            }

            if (isMadhyamik) {
                val timeLabel = if (session.isTimeUp) {
                    "⏰ সময় শেষ! এখন তোমার উত্তরগুলো জমা দাও (টাইপ বা খাতার ছবি)!"
                } else {
                    val formatted = ExamIntentRouter.formatCountdownBengali(session.remainingSeconds)
                    if (session.isPaused) "⏸ বিরতি — তোমার কাছে বাকি সময়: $formatted"
                    else "⏳ তোমার কাছে বাকি সময়: $formatted"
                }
                Text(
                    text = timeLabel,
                    style = MaterialTheme.typography.labelLarge.copy(fontFamily = MonoFontFamily),
                    color = accent,
                    fontWeight = FontWeight.Bold
                )
            } else {
                val details = buildList {
                    add("সময়সীমা নেই (Untimed)")
                    session.chapterHint?.let { add("অধ্যায়: $it") }
                    session.pageHint?.let { add("পাতা: $it") }
                }.joinToString(" • ")
                Text(
                    text = details,
                    style = MaterialTheme.typography.labelMedium,
                    color = accent
                )
            }
        }
    }
}

@Composable
private fun ExamChatEmptyState(
    inferenceMode: InferenceMode,
    onQuickPrompt: (String) -> Unit,
    onOpenPhotoPicker: () -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxSize()
            .testTag("exam_chat_empty_state")
    ) {
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    OnlineCobalt.copy(alpha = 0.16f),
                                    TerracottaAccent.copy(alpha = 0.12f)
                                )
                            )
                        )
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "৩-ইন-১ মাধ্যমিক স্মার্ট স্টাডি ও পরীক্ষা চ্যাট",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "যেকোনো প্রশ্ন, টু-দ্য-পয়েন্ট উত্তর, নির্দিষ্ট অধ্যায়ের নরমাল টেস্ট, টাইমার-সহ মাধ্যমিক পরীক্ষা, অথবা হাতের লেখা খাতার ছবি সরাসরি এখানে দাও।",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (inferenceMode == InferenceMode.ONLINE) {
                                "🟢 অনলাইন মোড সক্রিয় (AI Visual ২ডি ডায়াগ্রাম + খাতার ছবি মূল্যায়ন)"
                            } else {
                                "🟠 অফলাইন মোড সক্রিয় (২৫২+ বোর্ড প্রশ্ন ব্যাংক + Llama ইঞ্জিন)"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = if (inferenceMode == InferenceMode.ONLINE) OnlineCobalt else SageOffline,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        item {
            QuickExamActionCard(
                badge = "Intent A • টু-দ্য-পয়েন্ট উত্তর",
                title = "আমার কাছে উত্তর নেই — ৫ নম্বরের বোর্ড উত্তর দাও",
                promptPreview = "“অ্যানাফেজ দশার বৈশিষ্ট্য ৩ নম্বরের মতো টু-দ্য-পয়েন্ট উত্তর দাও, যাতে মাধ্যমিকে ফুল মার্কস পাই।”",
                accent = TerracottaAccent,
                icon = Icons.Rounded.Bolt,
                tag = "exam_quick_intent_a",
                onClick = {
                    onQuickPrompt("অ্যানাফেজ দশার বৈশিষ্ট্য ৩ নম্বরের মতো টু-দ্য-পয়েন্ট উত্তর দাও, যাতে মাধ্যমিকে ফুল মার্কস পাই।")
                }
            )
        }

        item {
            QuickExamActionCard(
                badge = "Intent B • নরমাল টেস্ট (সময়সীমা নেই)",
                title = "নির্দিষ্ট অধ্যায় ও পাতা থেকে নরমাল টেস্ট নাও",
                promptPreview = "“জীবন বিজ্ঞান ২য় অধ্যায়ের ৪৫ নম্বর পাতা থেকে আমার একটা নরমাল টেস্ট নাও।”",
                accent = SageOffline,
                icon = Icons.Rounded.Description,
                tag = "exam_quick_intent_b",
                onClick = {
                    onQuickPrompt("জীবন বিজ্ঞান ২য় অধ্যায়ের ৪৫ নম্বর পাতা থেকে আমার একটা নরমাল টেস্ট নাও।")
                }
            )
        }

        item {
            QuickExamActionCard(
                badge = "Intent C • মাধ্যমিক পরীক্ষা হল (লাইভ টাইমার)",
                title = "বিগত বছরের বোর্ড প্যাটার্নে টাইমার-সহ মাধ্যমিক টেস্ট",
                promptPreview = "“ভৌত বিজ্ঞানের বিগত বছরের মাধ্যমিক বোর্ড প্যাটার্নে টাইমার-সহ মাধ্যমিক টেস্ট নাও।”",
                accent = OnlineCobalt,
                icon = Icons.Rounded.WorkspacePremium,
                tag = "exam_quick_intent_c",
                onClick = {
                    onQuickPrompt("ভৌত বিজ্ঞানের বিগত বছরের মাধ্যমিক বোর্ড প্যাটার্নে টাইমার-সহ মাধ্যমিক টেস্ট নাও।")
                }
            )
        }

        item {
            QuickExamActionCard(
                badge = "খাতার ছবি মূল্যায়ন (৩/৪/৫/৮ নম্বর)",
                title = "খাতায় লিখে ক্যামেরা/গ্যালারি থেকে ছবি জমা দাও",
                promptPreview = "হাতের লেখা উত্তর যাচাই, ভুলত্রুটি নির্দেশ এবং WBBSE বোর্ড অনুযায়ী নম্বর প্রাপ্তি।",
                accent = MaterialTheme.colorScheme.primary,
                icon = Icons.Rounded.CameraAlt,
                tag = "exam_quick_camera",
                onClick = onOpenPhotoPicker
            )
        }
    }
}

@Composable
private fun QuickExamActionCard(
    badge: String,
    title: String,
    promptPreview: String,
    accent: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tag: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.35f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag(tag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                color = accent.copy(alpha = 0.14f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = badge,
                    style = MaterialTheme.typography.labelSmall,
                    color = accent,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = promptPreview,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ExamChatMessageBubble(
    message: ExamChatMessage,
    isSpeaking: Boolean,
    onSpeak: () -> Unit,
    onCopy: () -> Unit,
    onZoomDiagram: (String) -> Unit
) {
    val isUser = message.role == "user"
    val bubbleColor = if (isUser) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }
    val borderColor = if (isUser) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
    } else {
        MaterialTheme.colorScheme.outlineVariant
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Surface(
            color = bubbleColor,
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (isUser) 18.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 18.dp
            ),
            border = BorderStroke(1.dp, borderColor),
            tonalElevation = if (isUser) 0.dp else 1.dp,
            modifier = Modifier.fillMaxWidth(0.94f)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Header Badge Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = (if (isUser) MaterialTheme.colorScheme.primary else OnlineCobalt).copy(alpha = 0.14f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (isUser) message.intent.badgeBn else message.sourceLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isUser) MaterialTheme.colorScheme.primary else OnlineCobalt,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    message.subject?.let { subj ->
                        Text(
                            text = subj.bengaliName,
                            style = MaterialTheme.typography.labelSmall,
                            color = subj.accentColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Attached User Handwritten Photo Thumbnail
                message.userPhotoPath?.let { path ->
                    val bmp = remember(path) { NotebookImageHelper.decodeFileThumbnail(path, maxSizePx = 720) }
                    if (bmp != null) {
                        Image(
                            bitmap = bmp.asImageBitmap(),
                            contentDescription = "Submitted handwritten sheet",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 130.dp, max = 210.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onZoomDiagram(path) }
                        )
                    }
                }

                // Message Text
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isUser) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                )

                // Generated AI Visual 2D Diagram
                message.generatedDiagramPath?.let { diagPath ->
                    GeneratedDiagramCard(
                        imagePath = diagPath,
                        onZoomClick = { onZoomDiagram(diagPath) }
                    )
                }

                // Transparent diagram generation error if any (never hidden or mocked)
                message.diagramError?.let { err ->
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "⚠️ ভিজ্যুয়াল ডায়াগ্রাম তৈরি হয়নি: $err",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                // Assistant Action Row (Listen TTS + Copy)
                if (!isUser) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            onClick = onSpeak,
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSpeaking) TerracottaAccent.copy(alpha = 0.16f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("exam_chat_tts_${message.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (isSpeaking) Icons.Rounded.Stop else Icons.AutoMirrored.Rounded.VolumeUp,
                                    contentDescription = if (isSpeaking) "Stop listening" else "Listen aloud",
                                    tint = if (isSpeaking) TerracottaAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (isSpeaking) "থামাও" else "শোনো",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSpeaking) TerracottaAccent else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            onClick = onCopy,
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.ContentCopy,
                                    contentDescription = "Copy answer",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = "কপি",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
private fun ExamStreamingDraftBubble(draft: StreamingDraft) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, OnlineCobalt.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth(0.94f)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CircularProgressIndicator(
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(14.dp),
                    color = OnlineCobalt
                )
                Text(
                    text = draft.sourceLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = OnlineCobalt,
                    fontWeight = FontWeight.SemiBold
                )
            }
            if (draft.text.isNotBlank()) {
                Text(
                    text = draft.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            if (draft.isGeneratingDiagram) {
                DiagramGeneratingBanner()
            }
        }
    }
}

@Composable
private fun ExamChatComposer(
    inputText: String,
    onInputChange: (String) -> Unit,
    smartMode: Boolean,
    visualMode: Boolean,
    translateMode: Boolean,
    targetMarks: Int?,
    pendingPhoto: PendingPhotoAttachment?,
    isGenerating: Boolean,
    inferenceMode: InferenceMode,
    onToggleSmartMode: () -> Unit,
    onToggleVisualMode: () -> Unit,
    onToggleTranslateMode: () -> Unit,
    onSelectTargetMarks: (Int?) -> Unit,
    onOpenPhotoPicker: () -> Unit,
    onClearPendingPhoto: () -> Unit,
    onLaunchMic: () -> Unit,
    onSend: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Pending Handwritten Answer Sheet Thumbnail
            if (pendingPhoto != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                    border = BorderStroke(1.dp, OnlineCobalt.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Image(
                            bitmap = pendingPhoto.previewBitmap.asImageBitmap(),
                            contentDescription = "Attached answer sheet preview",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(54.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "খাতার ছবি যুক্ত হয়েছে",
                                style = MaterialTheme.typography.labelLarge,
                                color = OnlineCobalt
                            )
                            Text(
                                text = if (inferenceMode == InferenceMode.ONLINE) {
                                    "Send চাপলেই WBBSE মূল্যায়ন ও নম্বর পাবে"
                                } else {
                                    "খাতার ছবি মূল্যায়নের জন্য অনলাইন মোড চালু করো"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(
                            onClick = onClearPendingPhoto,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Rounded.Close, contentDescription = "Remove attached photo")
                        }
                    }
                }
            }

            // Smart To-The-Point Answer + Marks + Translate Strip
            SmartAnswerAndToolsStrip(
                smartToThePoint = smartMode,
                translateMode = translateMode,
                targetMarks = targetMarks,
                onToggleSmartAnswer = onToggleSmartMode,
                onToggleTranslate = onToggleTranslateMode,
                onSelectMarks = onSelectTargetMarks,
                testTagPrefix = "exam_chat"
            )

            // Action Tool Bar: Camera, Mic, and "AI Visual (ছবি দিয়ে বুঝিয়ে দাও)" right next to them
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    onClick = onOpenPhotoPicker,
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("exam_chat_camera_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CameraAlt,
                            contentDescription = "Attach notebook photo",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text("খাতা", style = MaterialTheme.typography.labelSmall)
                    }
                }

                Surface(
                    onClick = onLaunchMic,
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("exam_chat_mic_button")
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Mic,
                            contentDescription = "Voice input",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                FilterChip(
                    selected = visualMode,
                    onClick = onToggleVisualMode,
                    label = {
                        Text(
                            text = stringResource(R.string.ai_visual_toggle),
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.AutoGraph,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = OnlineCobalt,
                        selectedLabelColor = Color.White,
                        selectedLeadingIconColor = Color.White
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("exam_chat_ai_visual_toggle")
                )
            }

            // Text Input + Send Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = onInputChange,
                    placeholder = {
                        Text(
                            text = stringResource(R.string.exam_chat_placeholder),
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    shape = RoundedCornerShape(18.dp),
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (!isGenerating && (inputText.isNotBlank() || pendingPhoto != null)) {
                                onSend()
                            }
                        }
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("exam_chat_input")
                )

                val canSend = !isGenerating && (inputText.isNotBlank() || pendingPhoto != null)
                Surface(
                    onClick = onSend,
                    enabled = canSend,
                    shape = CircleShape,
                    color = if (canSend) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("exam_chat_send_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (isGenerating) {
                            CircularProgressIndicator(
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.Send,
                                contentDescription = "Send exam message",
                                tint = if (canSend) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
