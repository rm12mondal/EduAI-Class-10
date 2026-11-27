package com.example.ui.chat

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
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.rounded.AccountTree
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import com.example.data.local.ChatMessageEntity
import com.example.data.master.MathUnicodeFormatter
import com.example.data.media.NotebookImageHelper
import com.example.model.InferenceMode
import com.example.model.SectionChatUiState
import com.example.model.WbbseSubject
import com.example.ui.branding.BrandedAiAssistantAvatar
import com.example.ui.branding.LocalAppBranding
import com.example.ui.common.rememberAsyncCachedImageBitmap
import com.example.ui.master.WbbseReferenceBadgeChip
import com.example.ui.theme.OnlineCobalt
import com.example.ui.theme.SageOffline
import com.example.ui.theme.TerracottaAccent
import com.example.viewmodel.StreamingDraft

/**
 * Reusable, full-featured, Room-persisted Section Chat panel used inside:
 * - Brahmastram (ChatSection.BRAHMASTRAM)
 * - Mind Map Modal (ChatSection.MIND_MAP)
 * - Fast Learning Hub (ChatSection.FAST_LEARNING)
 *
 * Includes:
 * - Compact Header with small "Clear Chat" button & quick Audio Playback of latest reply
 * - Direct 1-tap Camera Photo Capture (with runtime CAMERA permission)
 * - Direct 1-tap Gallery Photo Upload (Android Photo Picker, zero storage permissions)
 * - Direct 1-tap Voice Input (Microphone SpeechRecognizer)
 * - Per-message TTS Audio Playback (Listen / Stop) + Copy + optional extra AI action (e.g. "মাইন্ড-ম্যাপ বানাও")
 */
@Composable
fun SectionChatPanel(
    state: SectionChatUiState,
    inferenceMode: InferenceMode,
    speakingMessageId: Long?,
    accentColor: Color,
    starterPrompts: List<String>,
    onSendMessage: (String) -> Unit,
    onAttachPhotoUri: (Uri) -> Unit,
    onClearPendingPhoto: () -> Unit,
    onClearChat: () -> Unit,
    onToggleSpeak: (Long, String) -> Unit,
    onShowToast: (String) -> Unit,
    onDismissError: () -> Unit = {},
    voiceInjectedText: String? = null,
    onConsumeVoiceInjectedText: () -> Unit = {},
    extraAssistantActionLabel: String? = null,
    onExtraAssistantAction: ((ChatMessageEntity) -> Unit)? = null,
    customHeaderTitle: String? = null,
    customHeaderSubtitle: String? = null,
    onPrepareSystemPicker: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val listState = rememberLazyListState()

    var inputText by rememberSaveable(state.section.id) { mutableStateOf("") }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    var zoomedPhotoPath by remember { mutableStateOf<String?>(null) }

    val cameraDeniedMsg = stringResource(R.string.camera_permission_denied)
    val copiedMsg = stringResource(R.string.chat_copied_toast)

    LaunchedEffect(voiceInjectedText) {
        if (!voiceInjectedText.isNullOrBlank()) {
            inputText = if (inputText.isBlank()) voiceInjectedText else "$inputText $voiceInjectedText"
            onConsumeVoiceInjectedText()
        }
    }

    LaunchedEffect(state.errorMessage) {
        val err = state.errorMessage
        if (!err.isNullOrBlank()) {
            onShowToast(err)
            onDismissError()
        }
    }

    val totalItems = state.messages.size + if (state.streamingDraft != null) 1 else 0
    LaunchedEffect(totalItems, state.streamingDraft?.text?.length) {
        if (totalItems > 0) {
            listState.animateScrollToItem(totalItems - 1)
        }
    }

    val launchVoiceInput = rememberVoiceInputLauncher(
        onResultText = { spoken ->
            inputText = if (inputText.isBlank()) spoken else "$inputText $spoken"
        },
        onUnavailable = onShowToast
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
            onShowToast(cameraDeniedMsg)
        }
    }

    val galleryPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onAttachPhotoUri(uri)
        }
    }

    fun triggerCamera() {
        onPrepareSystemPicker()
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
    }

    fun triggerGallery() {
        onPrepareSystemPicker()
        galleryPickerLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    val latestAssistantMsg = remember(state.messages) {
        state.messages.lastOrNull { it.role != "user" && it.content.isNotBlank() }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
    ) {
        // Compact Persistent Chat Header with small Clear Chat button & Quick Listen Audio
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = customHeaderTitle ?: state.section.titleBn,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = (if (inferenceMode == InferenceMode.ONLINE) OnlineCobalt else SageOffline).copy(alpha = 0.14f)
                        ) {
                            Text(
                                text = if (inferenceMode == InferenceMode.ONLINE) "অনলাইন" else "অফলাইন",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (inferenceMode == InferenceMode.ONLINE) OnlineCobalt else SageOffline,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "${customHeaderSubtitle ?: state.section.subtitleBn} • সেভড: ${state.messages.size}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (latestAssistantMsg != null) {
                        val isLatestSpeaking = speakingMessageId == latestAssistantMsg.id
                        Surface(
                            onClick = { onToggleSpeak(latestAssistantMsg.id, latestAssistantMsg.content) },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isLatestSpeaking) TerracottaAccent.copy(alpha = 0.16f)
                            else accentColor.copy(alpha = 0.14f),
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("${state.section.id}_header_audio_btn")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = if (isLatestSpeaking) Icons.Rounded.Stop else Icons.AutoMirrored.Rounded.VolumeUp,
                                    contentDescription = if (isLatestSpeaking) "Stop audio" else "Listen latest answer",
                                    tint = if (isLatestSpeaking) TerracottaAccent else accentColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (isLatestSpeaking) "থামাও" else "শোনো",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isLatestSpeaking) TerracottaAccent else accentColor
                                )
                            }
                        }
                    }

                    // Small "Clear Chat" Button in header
                    Surface(
                        onClick = onClearChat,
                        enabled = state.messages.isNotEmpty() || state.isGenerating,
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(
                            alpha = if (state.messages.isNotEmpty()) 0.85f else 0.45f
                        ),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("${state.section.id}_clear_chat_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.DeleteSweep,
                                contentDescription = "Clear Chat",
                                tint = if (state.messages.isNotEmpty()) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "চ্যাট মুছো",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (state.messages.isNotEmpty()) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                            )
                        }
                    }
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        // Messages Feed or Starter Prompts
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (state.messages.isEmpty() && state.streamingDraft == null) {
                LazyColumn(
                    contentPadding = PaddingValues(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = accentColor.copy(alpha = 0.10f),
                            border = BorderStroke(1.dp, accentColor.copy(alpha = 0.30f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "💬 ${state.section.titleBn} (আলাদা সেভ করা চ্যাট)",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = accentColor
                                )
                                Text(
                                    text = "এই সেকশনের কথোপকথন আলাদাভাবে সেভ থাকবে—যেকোনো সময় ফিরে এসে আবার শুরু করতে পারবে। ভয়েস, অডিও, গ্যালারি ও ক্যামেরা ছবি সব সাপোর্ট চালু আছে।",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    items(starterPrompts, key = { it }) { prompt ->
                        Surface(
                            onClick = { onSendMessage(prompt) },
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier
                                .fillMaxWidth()
                                .minimumInteractiveComponentSize()
                        ) {
                            Text(
                                text = "⚡ $prompt",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 11.dp)
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("${state.section.id}_messages_list")
                ) {
                    items(state.messages, key = { it.id }) { msg ->
                        SectionChatBubble(
                            message = msg,
                            accentColor = accentColor,
                            isSpeaking = speakingMessageId == msg.id,
                            onToggleSpeak = { onToggleSpeak(msg.id, msg.content) },
                            onCopy = {
                                clipboard.setText(AnnotatedString(MathUnicodeFormatter.format(msg.content)))
                                onShowToast(copiedMsg)
                            },
                            onZoomImage = { path -> zoomedPhotoPath = path },
                            extraActionLabel = extraAssistantActionLabel,
                            onExtraAction = onExtraAssistantAction?.let { cb -> { cb(msg) } }
                        )
                    }

                    state.streamingDraft?.let { draft ->
                        item(key = "${state.section.id}_streaming") {
                            SectionStreamingBubble(draft = draft, accentColor = accentColor)
                        }
                    }
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        // Full-Tool Input Bar: Camera + Gallery + Mic + Text + Send
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                state.pendingPhoto?.let { photo ->
                    Surface(
                        color = accentColor.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Image(
                                bitmap = photo.previewBitmap.asImageBitmap(),
                                contentDescription = "Attached photo preview",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                            Text(
                                text = "ছবি যুক্ত হয়েছে • পাঠাও বাটনে ট্যাপ করো",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = onClearPendingPhoto,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Rounded.Close, contentDescription = "Remove photo")
                            }
                        }
                    }
                }

                // Direct 1-Tap Media & Voice Tools Row: Camera | Gallery | Voice Mic
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        onClick = ::triggerCamera,
                        enabled = !state.isGenerating,
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .testTag("${state.section.id}_camera_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CameraAlt,
                                contentDescription = "Capture photo with camera",
                                tint = accentColor,
                                modifier = Modifier.size(17.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "ক্যামেরা",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1
                            )
                        }
                    }

                    Surface(
                        onClick = ::triggerGallery,
                        enabled = !state.isGenerating,
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .testTag("${state.section.id}_gallery_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.PhotoLibrary,
                                contentDescription = "Upload photo from gallery",
                                tint = accentColor,
                                modifier = Modifier.size(17.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "গ্যালারি",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1
                            )
                        }
                    }

                    Surface(
                        onClick = launchVoiceInput,
                        enabled = !state.isGenerating,
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .testTag("${state.section.id}_mic_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Mic,
                                contentDescription = "Voice input",
                                tint = accentColor,
                                modifier = Modifier.size(17.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "ভয়েস",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1
                            )
                        }
                    }
                }

                // Text Input + Send Button
                val canSend = !state.isGenerating && (inputText.isNotBlank() || state.pendingPhoto != null)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = {
                            Text(
                                text = state.section.placeholderBn,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        shape = RoundedCornerShape(22.dp),
                        maxLines = 4,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (canSend) {
                                    val msg = inputText
                                    inputText = ""
                                    onSendMessage(msg)
                                }
                            }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.background,
                            unfocusedContainerColor = MaterialTheme.colorScheme.background
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("${state.section.id}_input_field")
                    )

                    Surface(
                        onClick = {
                            val msg = inputText
                            inputText = ""
                            onSendMessage(msg)
                        },
                        enabled = canSend,
                        shape = CircleShape,
                        color = if (canSend) accentColor else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("${state.section.id}_send_btn")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (state.isGenerating) {
                                CircularProgressIndicator(
                                    strokeWidth = 2.dp,
                                    color = accentColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.Send,
                                    contentDescription = "Send message",
                                    tint = if (canSend) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    zoomedPhotoPath?.let { path ->
        FullScreenDiagramDialog(
            imagePath = path,
            onDismiss = { zoomedPhotoPath = null }
        )
    }
}

@Composable
private fun SectionChatBubble(
    message: ChatMessageEntity,
    accentColor: Color,
    isSpeaking: Boolean,
    onToggleSpeak: () -> Unit,
    onCopy: () -> Unit,
    onZoomImage: (String) -> Unit,
    extraActionLabel: String?,
    onExtraAction: (() -> Unit)?
) {
    val isUser = message.role == "user"
    val subject = remember(message.subjectId) { WbbseSubject.fromId(message.subjectId) }

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
            modifier = Modifier.fillMaxWidth(0.94f)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!isUser) {
                    val branding = LocalAppBranding.current
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            BrandedAiAssistantAvatar(
                                size = 22.dp,
                                defaultTint = accentColor
                            )
                            Surface(
                                color = accentColor.copy(alpha = 0.14f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = message.sourceLabel.ifBlank { branding.effectiveTitle },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = accentColor,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                        subject?.let { subj ->
                            Text(
                                text = subj.bengaliName,
                                style = MaterialTheme.typography.labelSmall,
                                color = subj.accentColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    WbbseReferenceBadgeChip(
                        answerText = message.content,
                        subject = subject
                    )
                }

                message.imagePath?.let { path ->
                    val bmp = rememberAsyncCachedImageBitmap(path, maxSizePx = 480)
                    if (bmp != null) {
                        Image(
                            bitmap = bmp,
                            contentDescription = "Attached photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 120.dp, max = 200.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onZoomImage(path) }
                        )
                    }
                }

                val formattedText = remember(message.content, isUser) {
                    if (isUser) message.content else MathUnicodeFormatter.format(message.content)
                }
                Text(
                    text = formattedText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isUser) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                )

                if (!isUser) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            onClick = onToggleSpeak,
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSpeaking) TerracottaAccent.copy(alpha = 0.16f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f),
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (isSpeaking) Icons.Rounded.Stop else Icons.AutoMirrored.Rounded.VolumeUp,
                                    contentDescription = if (isSpeaking) "Stop audio" else "Listen aloud",
                                    tint = if (isSpeaking) TerracottaAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(15.dp)
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
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f),
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.ContentCopy,
                                    contentDescription = "Copy text",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "কপি",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (extraActionLabel != null && onExtraAction != null) {
                            Surface(
                                onClick = onExtraAction,
                                shape = RoundedCornerShape(10.dp),
                                color = accentColor.copy(alpha = 0.14f),
                                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.35f)),
                                modifier = Modifier.minimumInteractiveComponentSize()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.AccountTree,
                                        contentDescription = extraActionLabel,
                                        tint = accentColor,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = extraActionLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = accentColor,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionStreamingBubble(
    draft: StreamingDraft,
    accentColor: Color
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.45f)),
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
                    color = accentColor,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = draft.sourceLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = accentColor,
                    fontWeight = FontWeight.SemiBold
                )
            }
            if (draft.text.isNotBlank()) {
                Text(
                    text = MathUnicodeFormatter.format(draft.text),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
