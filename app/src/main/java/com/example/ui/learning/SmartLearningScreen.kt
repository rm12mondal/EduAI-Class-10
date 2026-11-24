package com.example.ui.learning

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.AutoGraph
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.WifiOff
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
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
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
import com.example.data.local.MasterDataEntity
import com.example.data.media.NotebookImageHelper
import com.example.model.InferenceMode
import com.example.model.SmartLearningMessage
import com.example.model.SmartLearningUiState
import com.example.model.WbbseSubject
import com.example.ui.chat.DiagramGeneratingBanner
import com.example.ui.chat.FullScreenDiagramDialog
import com.example.ui.chat.GeneratedDiagramCard
import com.example.ui.chat.rememberVoiceInputLauncher
import com.example.ui.master.TopperAnswerFormatToggleBlock
import com.example.ui.theme.OnlineCobalt
import com.example.ui.theme.SageOffline
import com.example.ui.theme.TerracottaAccent
import com.example.viewmodel.PendingPhotoAttachment
import com.example.viewmodel.StreamingDraft

/**
 * Flagship "স্মার্ট লার্নিং (Smart Learning)" Interactive AI Tutor Screen:
 * - Accessible from Main Dashboard/Chat, Subjects Overview, and EVERY WBBSE Subject & Chapter
 * - Ultra-simple, friendly Bengali Tutor explanations with real-life examples & step-by-step math/science
 * - Prominent Camera (ক্যামেরায় ছবি তুলুন) & Gallery (গ্যালারি থেকে ছবি দিন) buttons right in composer
 *   with friendly Bengali Offline notice when internet/Online mode is off
 * - "ছবি তৈরি করে বুঝিয়ে দাও" AI Visual Diagram Generator (auto + on-demand)
 * - Two Quick Study Action Buttons on every explanation:
 *     1. "পড়া বুঝেছি — ২ মিনিটের কুইজ দিন"
 *     2. "এই টপিকের বড় প্রশ্ন মুখস্থ করুন" (links directly to Smart Memorizer)
 */
@Composable
fun SmartLearningScreen(
    state: SmartLearningUiState,
    masterNotes: List<MasterDataEntity>,
    inferenceMode: InferenceMode,
    speakingMessageId: Long?,
    onBack: () -> Unit,
    onToggleInferenceMode: (Boolean) -> Unit,
    onSelectSubject: (WbbseSubject?) -> Unit,
    onSelectChapter: (String?) -> Unit,
    onToggleDiagramMode: () -> Unit,
    onShowOfflinePhotoNotice: () -> Unit,
    onDismissOfflinePhotoNotice: () -> Unit,
    onAttachPhotoUri: (Uri) -> Unit,
    onClearPendingPhoto: () -> Unit,
    onClearSession: () -> Unit,
    onSendMessage: (String) -> Unit,
    onGenerateDiagramForMessage: (SmartLearningMessage) -> Unit,
    onStartTwoMinuteQuiz: (SmartLearningMessage) -> Unit,
    onSelectQuizOption: (String, Int) -> Unit,
    onSubmitQuickQuiz: () -> Unit,
    onCloseQuickQuiz: () -> Unit,
    onStartSmartMemorizer: (SmartLearningMessage) -> Unit,
    onToggleMemorizerRecall: () -> Unit,
    onToggleMemorizerRevealStep: (Int) -> Unit,
    onCloseSmartMemorizer: () -> Unit,
    onToggleSpeak: (Long, String) -> Unit,
    onOpenAudioRevision: () -> Unit,
    onExportPdf: (String, String, String, String) -> Unit = { _, _, _, _ -> },
    onShowSnackbar: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val listState = rememberLazyListState()

    var inputText by rememberSaveable { mutableStateOf("") }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    var zoomedImagePath by remember { mutableStateOf<String?>(null) }

    val cameraDeniedMsg = stringResource(R.string.camera_permission_denied)
    val copiedMsg = stringResource(R.string.chat_copied_toast)

    val chaptersForSubject = remember(masterNotes, state.selectedSubject) {
        val subj = state.selectedSubject
        if (subj == null) emptyList()
        else masterNotes
            .filter { it.subjectId == subj.id }
            .map { it.chapterName }
            .distinct()
    }

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

    LaunchedEffect(state.messages.size, state.streamingDraft?.text?.length) {
        val count = state.messages.size + if (state.streamingDraft != null) 1 else 0
        if (count > 0) {
            listState.animateScrollToItem(count - 1)
        }
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
                .widthIn(max = 760.dp)
        ) {
            // 1. Flagship Top Bar with Back + Hybrid Online/Offline Switch
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("smart_learning_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Back from Smart Learning"
                            )
                        }

                        Surface(
                            color = OnlineCobalt.copy(alpha = 0.14f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.School,
                                    contentDescription = null,
                                    tint = OnlineCobalt,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.smart_learning_title),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (inferenceMode == InferenceMode.ONLINE) {
                                    "অনলাইন AI টিউটর • ছবি ও ডায়াগ্রাম চালু"
                                } else {
                                    "অফলাইন টিউটর • অধ্যায় নোট + Llama"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = if (inferenceMode == InferenceMode.ONLINE) OnlineCobalt else SageOffline,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Surface(
                            onClick = onOpenAudioRevision,
                            shape = RoundedCornerShape(999.dp),
                            color = Color(0xFF0E7490).copy(alpha = 0.14f),
                            border = BorderStroke(1.dp, Color(0xFF0E7490).copy(alpha = 0.4f)),
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("smart_learning_audio_revision_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Headphones,
                                    contentDescription = "Hands-free Audio Revision",
                                    tint = Color(0xFF0E7490),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "অডিও",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF0E7490),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = if (inferenceMode == InferenceMode.ONLINE) "অনলাইন" else "অফলাইন",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (inferenceMode == InferenceMode.ONLINE) OnlineCobalt else SageOffline,
                                fontWeight = FontWeight.Bold
                            )
                            Switch(
                                checked = inferenceMode == InferenceMode.ONLINE,
                                onCheckedChange = onToggleInferenceMode,
                                colors = SwitchDefaults.colors(
                                    checkedTrackColor = OnlineCobalt,
                                    uncheckedTrackColor = SageOffline.copy(alpha = 0.35f)
                                ),
                                modifier = Modifier.testTag("smart_learning_mode_switch")
                            )
                        }
                    }

                    // 2. Subject Selector Row (সব বিষয় + 7 WBBSE Subjects)
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item(key = "sl_subj_all") {
                            FilterChip(
                                selected = state.selectedSubject == null,
                                onClick = { onSelectSubject(null) },
                                label = { Text("সব বিষয়") },
                                modifier = Modifier.testTag("sl_subject_all")
                            )
                        }
                        items(WbbseSubject.entries, key = { it.id }) { subj ->
                            val selected = state.selectedSubject == subj
                            FilterChip(
                                selected = selected,
                                onClick = { onSelectSubject(subj) },
                                label = { Text(subj.bengaliName) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = subj.icon,
                                        contentDescription = null,
                                        modifier = Modifier.size(15.dp),
                                        tint = if (selected) Color.White else subj.accentColor
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = subj.accentColor,
                                    selectedLabelColor = Color.White,
                                    selectedLeadingIconColor = Color.White
                                ),
                                modifier = Modifier.testTag("sl_subject_${subj.id}")
                            )
                        }
                    }

                    // 3. Chapter Selector Row (when a subject is chosen)
                    if (chaptersForSubject.isNotEmpty()) {
                        LazyRow(
                            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            item(key = "sl_ch_all") {
                                FilterChip(
                                    selected = state.selectedChapter == null,
                                    onClick = { onSelectChapter(null) },
                                    label = { Text("সব অধ্যায়", style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                            items(chaptersForSubject, key = { it }) { ch ->
                                val selected = state.selectedChapter == ch
                                val accent = state.selectedSubject?.accentColor ?: OnlineCobalt
                                FilterChip(
                                    selected = selected,
                                    onClick = { onSelectChapter(if (selected) null else ch) },
                                    label = {
                                        Text(
                                            text = ch,
                                            style = MaterialTheme.typography.labelSmall,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = accent.copy(alpha = 0.16f),
                                        selectedLabelColor = accent
                                    )
                                )
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Friendly Bengali Offline Photo Notice Banner
            state.offlinePhotoNotice?.let { notice ->
                Surface(
                    color = TerracottaAccent.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, TerracottaAccent.copy(alpha = 0.45f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("smart_learning_offline_photo_notice")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.WifiOff,
                            contentDescription = null,
                            tint = TerracottaAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = notice,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = onDismissOfflinePhotoNotice,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Rounded.Close, contentDescription = "Dismiss notice", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // Loading strip when generating 2-Minute Quiz or Smart Memorizer
            if (state.isLoadingQuiz || state.isLoadingMemorizer) {
                Surface(
                    color = OnlineCobalt.copy(alpha = 0.12f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            color = OnlineCobalt,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (state.isLoadingQuiz) {
                                "⚡ ২ মিনিটের দ্রুত কুইজ তৈরি হচ্ছে…"
                            } else {
                                "🧠 স্মার্ট মেমোরাইজার মুখস্থ কার্ড তৈরি হচ্ছে…"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = OnlineCobalt,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // 4. Tutor Chat Feed or Welcome Empty State
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (state.messages.isEmpty() && state.streamingDraft == null) {
                    SmartLearningWelcomeView(
                        selectedSubject = state.selectedSubject,
                        selectedChapter = state.selectedChapter,
                        inferenceMode = inferenceMode,
                        onQuickAsk = { prompt -> onSendMessage(prompt) },
                        onCameraClick = {
                            if (inferenceMode == InferenceMode.OFFLINE) {
                                onShowOfflinePhotoNotice()
                            } else {
                                val hasCam = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.CAMERA
                                ) == PackageManager.PERMISSION_GRANTED
                                if (hasCam) {
                                    val uri = NotebookImageHelper.createTempCameraImageUri(context)
                                    tempCameraUri = uri
                                    takePictureLauncher.launch(uri)
                                } else {
                                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            }
                        }
                    )
                } else {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("smart_learning_messages_list")
                    ) {
                        item(key = "sl_clear_bar") {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = state.selectedChapter?.let { "অধ্যায়: $it" }
                                        ?: (state.selectedSubject?.bengaliName ?: "WBBSE ক্লাস ১০ স্মার্ট লার্নিং"),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Surface(
                                    onClick = onClearSession,
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            Icons.Rounded.DeleteOutline,
                                            contentDescription = "Clear session",
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Text("নতুন টপিক", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }

                        items(state.messages, key = { it.id }) { msg ->
                            SmartLearningMessageCard(
                                message = msg,
                                isSpeaking = speakingMessageId == msg.id,
                                isBusyOverlay = state.isLoadingQuiz || state.isLoadingMemorizer,
                                onSpeak = { onToggleSpeak(msg.id, msg.content) },
                                onCopy = {
                                    clipboard.setText(AnnotatedString(msg.content))
                                    onShowSnackbar(copiedMsg)
                                },
                                onExportPdf = {
                                    val subjName = msg.subject?.bengaliName ?: "স্মার্ট_লার্নিং"
                                    val ch = msg.chapterName ?: "অধ্যায়_নোট"
                                    onExportPdf(
                                        "${subjName}_${ch}",
                                        "$subjName — স্মার্ট লার্নিং নোটস",
                                        msg.chapterName ?: msg.sourceLabel,
                                        msg.content
                                    )
                                },
                                onZoomImage = { path -> zoomedImagePath = path },
                                onGenerateDiagram = { onGenerateDiagramForMessage(msg) },
                                onStartTwoMinQuiz = { onStartTwoMinuteQuiz(msg) },
                                onStartMemorizer = { onStartSmartMemorizer(msg) }
                            )
                        }

                        state.streamingDraft?.let { draft ->
                            item(key = "sl_streaming") {
                                SmartLearningStreamingCard(draft = draft)
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // 5. Smart Learning Composer Bar with prominent Camera, Gallery, Visual Diagram & Mic buttons
            SmartLearningComposerBar(
                inputText = inputText,
                onInputChange = { inputText = it },
                diagramToggle = state.generateDiagramToggle,
                pendingPhoto = state.pendingPhoto,
                isGenerating = state.isGenerating,
                inferenceMode = inferenceMode,
                onToggleDiagram = onToggleDiagramMode,
                onCameraClick = {
                    if (inferenceMode == InferenceMode.OFFLINE) {
                        onShowOfflinePhotoNotice()
                    } else {
                        val hasCam = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED
                        if (hasCam) {
                            val uri = NotebookImageHelper.createTempCameraImageUri(context)
                            tempCameraUri = uri
                            takePictureLauncher.launch(uri)
                        } else {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    }
                },
                onGalleryClick = {
                    if (inferenceMode == InferenceMode.OFFLINE) {
                        onShowOfflinePhotoNotice()
                    } else {
                        galleryPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                },
                onClearPendingPhoto = onClearPendingPhoto,
                onMicClick = launchVoiceInput,
                onSend = {
                    val q = inputText
                    inputText = ""
                    onSendMessage(q)
                }
            )
        }
    }

    // 2-Minute Quick Quiz Dialog
    state.activeQuiz?.let { quiz ->
        TwoMinuteQuizDialog(
            quiz = quiz,
            remainingSeconds = state.quizRemainingSeconds,
            selectedAnswers = state.quizSelectedAnswers,
            submitted = state.quizSubmitted,
            onSelectOption = onSelectQuizOption,
            onSubmit = onSubmitQuickQuiz,
            onDismiss = onCloseQuickQuiz
        )
    }

    // Smart Memorizer Dialog
    state.activeMemorizer?.let { deck ->
        SmartMemorizerDialog(
            deck = deck,
            activeRecallHidden = state.activeRecallHiddenMode,
            revealedSteps = state.revealedCardSteps,
            isSpeakingMemorizer = speakingMessageId == 999_991L,
            onToggleActiveRecall = onToggleMemorizerRecall,
            onToggleRevealStep = onToggleMemorizerRevealStep,
            onSpeakMemorizer = { txt -> onToggleSpeak(999_991L, txt) },
            onDismiss = onCloseSmartMemorizer
        )
    }

    // Full-screen pinch-to-zoom diagram/photo viewer
    zoomedImagePath?.let { path ->
        FullScreenDiagramDialog(
            imagePath = path,
            onDismiss = { zoomedImagePath = null }
        )
    }
}

@Composable
private fun SmartLearningWelcomeView(
    selectedSubject: WbbseSubject?,
    selectedChapter: String?,
    inferenceMode: InferenceMode,
    onQuickAsk: (String) -> Unit,
    onCameraClick: () -> Unit
) {
    val contextLabel = buildString {
        append(selectedSubject?.bengaliName ?: "WBBSE ক্লাস ১০")
        if (!selectedChapter.isNullOrBlank()) append(" • $selectedChapter")
    }

    val starterPrompts = remember(selectedSubject, selectedChapter) {
        if (!selectedChapter.isNullOrBlank()) {
            listOf(
                "“$selectedChapter” অধ্যায়টি একদম সহজ বাংলায় বাস্তব উদাহরণ দিয়ে বুঝিয়ে দাও।",
                "“$selectedChapter” থেকে সবচেয়ে কঠিন ধারণাটি ছবি তৈরি করে ধাপে ধাপে বোঝাও।",
                "“$selectedChapter” অধ্যায়ের মূল সূত্র ও মাধ্যমিকে আসার মতো গুরুত্বপূর্ণ অংশগুলো সহজ করে বলো।"
            )
        } else when (selectedSubject) {
            WbbseSubject.MATHEMATICS -> listOf(
                "পিথাগোরাসের উপপাদ্যটি একদম সহজ ভাষায় ছবি ও ধাপে ধাপে প্রমাণ করে বুঝিয়ে দাও।",
                "চক্রবৃদ্ধি সুদের অঙ্ক করার সহজ কৌশল বাস্তব উদাহরণ দিয়ে বোঝাও।",
                "দ্বিঘাত সমীকরণের শ্রীধর আচার্যের সূত্র কীভাবে কাজ করে সহজ বাংলায় বোঝাও।"
            )
            WbbseSubject.PHYSICAL_SCIENCE -> listOf(
                "বয়েলের সূত্র ও চার্লসের সূত্র বাস্তব জীবনের বেলুন ও সাইকেলের পাম্পের উদাহরণ দিয়ে বুঝিয়ে দাও।",
                "প্রিজমের মধ্য দিয়ে আলোর প্রতিসরণ ও বিচ্ছুরণ ছবি তৈরি করে সহজ করে বোঝাও।",
                "ওহমের সূত্র (V=IR) এবং রোধের শ্রেণি ও সমান্তরাল সমবায় একদম সহজ বাংলায় বোঝাও।"
            )
            WbbseSubject.LIFE_SCIENCE -> listOf(
                "মাইটোসিস কোষ বিভাজনের অ্যানাফেজ দশা গল্পের মতো করে ছবি দিয়ে বুঝিয়ে দাও।",
                "প্রতিবর্ত চাপ (Reflex Arc) কীভাবে কাজ করে গরম কড়াইয়ে হাত লাগার উদাহরণ দিয়ে বোঝাও।",
                "মেন্ডেলের একসংকর জনন ও চেকার বোর্ড একদম সহজ ভাষায় ধাপে ধাপে বোঝাও।"
            )
            WbbseSubject.HISTORY -> listOf(
                "১৮৫৭ সালের মহাবিদ্রোহ (সিপাহী বিদ্রোহ) কেন হয়েছিল—গল্পের মতো করে মানচিত্রসহ বুঝিয়ে দাও।",
                "নীল বিদ্রোহের কারণ ও গুরুত্ব সহজ পয়েন্টে বাস্তব উদাহরণ দিয়ে বোঝাও।"
            )
            WbbseSubject.GEOGRAPHY -> listOf(
                "নদীর উচ্চগতি, মধ্যগতি ও নিম্নগতির ভূমিরূপ ছবি তৈরি করে সহজ বাংলায় বুঝিয়ে দাও।",
                "বৃষ্টিচ্ছায় অঞ্চল ও মৌসুমি বায়ুর প্রভাব সহজ উদাহরণসহ চিত্র দিয়ে বোঝাও।"
            )
            WbbseSubject.ENGLISH -> listOf(
                "Voice Change ও Narration Change করার সবচেয়ে সহজ নিয়ম বাংলা উদাহরণসহ বুঝিয়ে দাও।",
                "Father's Help গল্পটির মূল ভাব ও গুরুত্বপূর্ণ গ্রামার সহজ বাংলায় বুঝিয়ে দাও।"
            )
            else -> listOf(
                "মাইটোসিসের অ্যানাফেজ দশা একদম সহজ বাংলায় ছবি তৈরি করে বুঝিয়ে দাও।",
                "বয়েলের সূত্র ও PV=nRT বাস্তব জীবনের উদাহরণ দিয়ে ধাপে ধাপে বোঝাও।",
                "পিথাগোরাসের উপপাদ্য বা কারক-বিভক্তি একদম সহজ গল্পের মতো করে বোঝাও।"
            )
        }
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxSize()
            .testTag("smart_learning_welcome")
    ) {
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                border = BorderStroke(1.dp, OnlineCobalt.copy(alpha = 0.35f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    OnlineCobalt.copy(alpha = 0.16f),
                                    SageOffline.copy(alpha = 0.13f)
                                )
                            )
                        )
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "🎓 স্মার্ট লার্নিং AI টিউটর ($contextLabel)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "যেকোনো কঠিন পড়া, বইয়ের অনুচ্ছেদ, ব্যাকরণ বা অঙ্ক এখানে জিজ্ঞেস করো অথবা বইয়ের ছবি তুলে দাও — আমি একদম সহজ বাংলায় বাস্তব উদাহরণ ও ছবি দিয়ে বুঝিয়ে দেব!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (inferenceMode == InferenceMode.ONLINE) {
                                "🟢 অনলাইন মোড: ছবি পড়ে সমাধান + অটো ২ডি ডায়াগ্রাম + ২-মিনিট কুইজ ও স্মার্ট মেমোরাইজার"
                            } else {
                                "🟠 অফলাইন মোড: যাচাইকৃত অধ্যায় নোট + লোকাল Llama মডেল + অফলাইন কুইজ ও মেমোরাইজার"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = if (inferenceMode == InferenceMode.ONLINE) OnlineCobalt else SageOffline,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Camera Textbook Photo Highlight Card
        item {
            Card(
                onClick = onCameraClick,
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = TerracottaAccent.copy(alpha = 0.10f)),
                border = BorderStroke(1.dp, TerracottaAccent.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("smart_learning_welcome_camera")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        color = TerracottaAccent.copy(alpha = 0.18f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Rounded.CameraAlt, contentDescription = null, tint = TerracottaAccent)
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "📷 বইয়ের পাতা বা অঙ্কের ছবি তুলে সহজে বোঝো",
                            style = MaterialTheme.typography.titleSmall,
                            color = TerracottaAccent,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "কঠিন ডায়াগ্রাম, বইয়ের প্যারাগ্রাফ বা অঙ্কের ছবি তুললেই ধাপে ধাপে বাংলায় সমাধান পাবে।",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "এক ট্যাপে পড়া শুরু করো:",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        items(starterPrompts) { prompt ->
            Card(
                onClick = { onQuickAsk(prompt) },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "💬 $prompt",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}

@Composable
private fun SmartLearningMessageCard(
    message: SmartLearningMessage,
    isSpeaking: Boolean,
    isBusyOverlay: Boolean,
    onSpeak: () -> Unit,
    onCopy: () -> Unit,
    onExportPdf: () -> Unit,
    onZoomImage: (String) -> Unit,
    onGenerateDiagram: () -> Unit,
    onStartTwoMinQuiz: () -> Unit,
    onStartMemorizer: () -> Unit
) {
    val isUser = message.role == "user"
    val accent = message.subject?.accentColor ?: OnlineCobalt

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Surface(
            color = if (isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (isUser) 18.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 18.dp
            ),
            border = BorderStroke(
                1.dp,
                if (isUser) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                else MaterialTheme.colorScheme.outlineVariant
            ),
            tonalElevation = if (isUser) 0.dp else 1.dp,
            modifier = Modifier.fillMaxWidth(0.95f)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Header Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = accent.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = message.sourceLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = accent,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                    message.chapterName?.let { ch ->
                        Text(
                            text = ch,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // User's uploaded textbook/math photo
                message.userPhotoPath?.let { photoPath ->
                    val bmp = remember(photoPath) { NotebookImageHelper.decodeFileThumbnail(photoPath, 720) }
                    if (bmp != null) {
                        Image(
                            bitmap = bmp.asImageBitmap(),
                            contentDescription = "Uploaded textbook or math problem photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 130.dp, max = 210.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onZoomImage(photoPath) }
                        )
                    }
                }

                // Explanation Text (with Topper Answer Format Toggle for assistant responses)
                if (!isUser) {
                    TopperAnswerFormatToggleBlock(
                        rawAnswer = message.content,
                        subject = message.subject
                    )
                } else {
                    Text(
                        text = message.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                // On-demand diagram progress
                if (message.isGeneratingDiagram) {
                    DiagramGeneratingBanner()
                }

                // AI Generated Educational Diagram
                message.generatedDiagramPath?.let { diagPath ->
                    GeneratedDiagramCard(
                        imagePath = diagPath,
                        onZoomClick = { onZoomImage(diagPath) }
                    )
                }

                // Transparent diagram error if any
                message.diagramError?.let { err ->
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "⚠️ ডায়াগ্রাম তৈরি হয়নি: $err",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                // Assistant Utilities (Listen + Copy + On-Demand Generate Diagram)
                if (!isUser) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            onClick = onSpeak,
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSpeaking) TerracottaAccent.copy(alpha = 0.16f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (isSpeaking) Icons.Rounded.Stop else Icons.AutoMirrored.Rounded.VolumeUp,
                                    contentDescription = if (isSpeaking) "Stop" else "Listen",
                                    tint = if (isSpeaking) TerracottaAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (isSpeaking) "থামাও" else "শোনো",
                                    style = MaterialTheme.typography.labelSmall
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
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.ContentCopy,
                                    contentDescription = "Copy",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text("কপি", style = MaterialTheme.typography.labelSmall)
                            }
                        }

                        Surface(
                            onClick = onExportPdf,
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFC62828).copy(alpha = 0.11f),
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("sl_pdf_export_${message.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.PictureAsPdf,
                                    contentDescription = "Save as PDF",
                                    tint = Color(0xFFC62828),
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = "PDF",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFC62828),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (message.generatedDiagramPath == null && !message.isGeneratingDiagram) {
                            Surface(
                                onClick = onGenerateDiagram,
                                shape = RoundedCornerShape(10.dp),
                                color = OnlineCobalt.copy(alpha = 0.12f),
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("sl_draw_diagram_${message.id}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.AutoGraph,
                                        contentDescription = "Generate visual diagram",
                                        tint = OnlineCobalt,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        text = "ছবি আঁকো",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = OnlineCobalt,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))

                    // REQUIRED TWO QUICK STUDY ACTION BUTTONS AT THE END OF EVERY TOPIC EXPLANATION
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = onStartTwoMinQuiz,
                            enabled = !isBusyOverlay,
                            colors = ButtonDefaults.buttonColors(containerColor = SageOffline),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .minimumInteractiveComponentSize()
                                .testTag("sl_quick_quiz_btn_${message.id}")
                        ) {
                            Icon(Icons.Rounded.Quiz, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.smart_learning_action_quiz),
                                style = MaterialTheme.typography.labelLarge
                            )
                        }

                        FilledTonalButton(
                            onClick = onStartMemorizer,
                            enabled = !isBusyOverlay,
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = TerracottaAccent.copy(alpha = 0.15f),
                                contentColor = TerracottaAccent
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .minimumInteractiveComponentSize()
                                .testTag("sl_memorizer_btn_${message.id}")
                        ) {
                            Icon(Icons.Rounded.AutoStories, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.smart_learning_action_memorize),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SmartLearningStreamingCard(draft: StreamingDraft) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, OnlineCobalt.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth(0.95f)
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
                    color = OnlineCobalt,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = draft.sourceLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = OnlineCobalt,
                    fontWeight = FontWeight.Bold
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
private fun SmartLearningComposerBar(
    inputText: String,
    onInputChange: (String) -> Unit,
    diagramToggle: Boolean,
    pendingPhoto: PendingPhotoAttachment?,
    isGenerating: Boolean,
    inferenceMode: InferenceMode,
    onToggleDiagram: () -> Unit,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onClearPendingPhoto: () -> Unit,
    onMicClick: () -> Unit,
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Pending Textbook / Math Problem Photo Preview
            if (pendingPhoto != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = OnlineCobalt.copy(alpha = 0.10f),
                    border = BorderStroke(1.dp, OnlineCobalt.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Image(
                            bitmap = pendingPhoto.previewBitmap.asImageBitmap(),
                            contentDescription = "Selected textbook photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(54.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "বইয়ের/অঙ্কের ছবি যুক্ত হয়েছে",
                                style = MaterialTheme.typography.labelLarge,
                                color = OnlineCobalt,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Send চাপলেই সহজ বাংলায় ধাপে ধাপে ব্যাখ্যা পাবে",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(
                            onClick = onClearPendingPhoto,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Rounded.Close, contentDescription = "Remove photo")
                        }
                    }
                }
            }

            // Prominent Camera (ক্যামেরায় ছবি তুলুন) & Gallery (গ্যালারি থেকে ছবি দিন) buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledTonalButton(
                    onClick = onCameraClick,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (inferenceMode == InferenceMode.ONLINE) {
                            OnlineCobalt.copy(alpha = 0.14f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        contentColor = if (inferenceMode == InferenceMode.ONLINE) OnlineCobalt else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .testTag("smart_learning_camera_btn")
                ) {
                    Icon(Icons.Rounded.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = stringResource(R.string.smart_learning_camera_btn),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                OutlinedButton(
                    onClick = onGalleryClick,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .testTag("smart_learning_gallery_btn")
                ) {
                    Icon(Icons.Rounded.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = stringResource(R.string.smart_learning_gallery_btn),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Visual Diagram Generator Toggle + Voice Mic Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = diagramToggle,
                    onClick = onToggleDiagram,
                    label = {
                        Text(
                            text = stringResource(R.string.smart_learning_visual_btn),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    leadingIcon = {
                        Icon(Icons.Rounded.AutoGraph, contentDescription = null, modifier = Modifier.size(16.dp))
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = OnlineCobalt,
                        selectedLabelColor = Color.White,
                        selectedLeadingIconColor = Color.White
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("smart_learning_diagram_toggle")
                )

                Surface(
                    onClick = onMicClick,
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("smart_learning_mic_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Mic,
                            contentDescription = "Voice input",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text("বলো", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            // Input Field + Send Button
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
                            text = stringResource(R.string.smart_learning_placeholder),
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    shape = RoundedCornerShape(18.dp),
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OnlineCobalt,
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
                        .testTag("smart_learning_input")
                )

                val canSend = !isGenerating && (inputText.isNotBlank() || pendingPhoto != null)
                Surface(
                    onClick = onSend,
                    enabled = canSend,
                    shape = CircleShape,
                    color = if (canSend) OnlineCobalt else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("smart_learning_send_btn")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (isGenerating) {
                            CircularProgressIndicator(
                                strokeWidth = 2.dp,
                                color = OnlineCobalt,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.Send,
                                contentDescription = "Send to Smart Learning Tutor",
                                tint = if (canSend) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
