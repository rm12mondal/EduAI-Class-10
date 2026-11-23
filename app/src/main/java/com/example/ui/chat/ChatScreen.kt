package com.example.ui.chat

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.rounded.AddAPhoto
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.OfflineBolt
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.StopCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.R
import com.example.data.local.ChatMessageEntity
import com.example.data.media.NotebookImageHelper
import com.example.model.GgufModelStatus
import com.example.model.InferenceMode
import com.example.model.WbbseSubject
import com.example.ui.theme.OnlineCobalt
import com.example.ui.theme.OnlineCobaltContainer
import com.example.ui.theme.SageOffline
import com.example.ui.theme.SageOfflineContainer
import com.example.viewmodel.EduAiUiState
import com.example.viewmodel.PendingPhotoAttachment
import com.example.viewmodel.StreamingDraft

@Composable
fun ChatScreen(
    uiState: EduAiUiState,
    onToggleMode: (Boolean) -> Unit,
    onSelectSubject: (WbbseSubject?) -> Unit,
    onSendQuestion: (String) -> Unit,
    onAttachPhotoUri: (Uri) -> Unit,
    onClearPendingPhoto: () -> Unit,
    onToggleSpeakMessage: (Long, String) -> Unit,
    onClearChat: () -> Unit,
    onOpenModelHub: () -> Unit,
    onShowToast: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var inputText by rememberSaveable { mutableStateOf("") }
    var showPhotoSourceDialog by remember { mutableStateOf(false) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    val listState = rememberLazyListState()
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    val copiedToastMsg = stringResource(R.string.chat_copied_toast)
    val cameraDeniedMsg = stringResource(R.string.camera_permission_denied)

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

    val totalItems = uiState.chatMessages.size + if (uiState.streamingDraft != null) 1 else 0
    LaunchedEffect(totalItems, uiState.streamingDraft?.text?.length) {
        if (totalItems > 0) {
            listState.animateScrollToItem(totalItems - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
    ) {
        // 1. Header with EduAI branding + Offline/Online Hybrid Toggle Switch
        HybridChatHeader(
            uiState = uiState,
            onToggleMode = onToggleMode,
            onClearChat = onClearChat,
            onOpenModelHub = onOpenModelHub
        )

        // 2. 7-Subject WBBSE Filter Strip
        SubjectFilterRow(
            selectedSubject = uiState.selectedChatSubject,
            onSelectSubject = onSelectSubject
        )

        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant,
            thickness = 1.dp
        )

        // 3. Chat Conversation Area or Welcome Empty State
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.TopCenter
        ) {
            if (uiState.chatMessages.isEmpty() && uiState.streamingDraft == null) {
                ChatWelcomeState(
                    selectedSubject = uiState.selectedChatSubject,
                    inferenceMode = uiState.inferenceMode,
                    onQuickPromptClick = { prompt ->
                        onSendQuestion(prompt)
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 680.dp)
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 680.dp)
                        .testTag("chat_messages_list"),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = uiState.chatMessages,
                        key = { it.id }
                    ) { message ->
                        ChatBubbleItem(
                            message = message,
                            isSpeaking = uiState.ttsState.speakingMessageId == message.id,
                            onToggleSpeak = {
                                onToggleSpeakMessage(message.id, message.content)
                            },
                            onCopyClick = {
                                clipboard.setText(AnnotatedString(message.content))
                                onShowToast(copiedToastMsg)
                            }
                        )
                    }

                    uiState.streamingDraft?.let { draft ->
                        item(key = "streaming_draft") {
                            StreamingBubbleItem(draft = draft)
                        }
                    }
                }
            }
        }

        // 4. Message Composer Bar with Notebook Photo Attachment
        ChatComposerBar(
            value = inputText,
            onValueChange = { inputText = it },
            selectedSubject = uiState.selectedChatSubject,
            pendingPhoto = uiState.pendingChatPhoto,
            isGenerating = uiState.isGenerating,
            onAttachPhotoClick = { showPhotoSourceDialog = true },
            onClearPhoto = onClearPendingPhoto,
            onSend = {
                if (inputText.isNotBlank() || uiState.pendingChatPhoto != null) {
                    val toSend = inputText
                    inputText = ""
                    onSendQuestion(toSend)
                }
            }
        )
    }

    if (showPhotoSourceDialog) {
        NotebookPhotoSourceDialog(
            isOnlineMode = uiState.inferenceMode == InferenceMode.ONLINE,
            onDismiss = { showPhotoSourceDialog = false },
            onTakePhoto = {
                showPhotoSourceDialog = false
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
            onPickGallery = {
                showPhotoSourceDialog = false
                galleryPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }
        )
    }
}

@Composable
private fun NotebookPhotoSourceDialog(
    isOnlineMode: Boolean,
    onDismiss: () -> Unit,
    onTakePhoto: () -> Unit,
    onPickGallery: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.photo_picker_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = if (isOnlineMode) {
                        "খাতার হাতের লেখার পরিষ্কার ছবি তুলে বা গ্যালারি থেকে বেছে পাঠাও—Gemini AI বানান, তথ্য ও মাধ্যমিক মান অনুযায়ী মূল্যায়ন করবে।"
                    } else {
                        "দ্রষ্টব্য: খাতার ছবি মূল্যায়ন করতে উপরের হাইব্রিড সুইচ দিয়ে অনলাইন মোড (Online) চালু রেখো।"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                FilledTonalButton(
                    onClick = onTakePhoto,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .testTag("chat_camera_option_button")
                ) {
                    Icon(Icons.Rounded.CameraAlt, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.photo_take_camera))
                }

                OutlinedButton(
                    onClick = onPickGallery,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .testTag("chat_gallery_option_button")
                ) {
                    Icon(Icons.Rounded.PhotoLibrary, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.photo_choose_gallery))
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বাতিল")
            }
        }
    )
}

@Composable
private fun HybridChatHeader(
    uiState: EduAiUiState,
    onToggleMode: (Boolean) -> Unit,
    onClearChat: () -> Unit,
    onOpenModelHub: () -> Unit
) {
    val isOnlineMode = uiState.inferenceMode == InferenceMode.ONLINE
    val pillColor by animateColorAsState(
        targetValue = if (isOnlineMode) OnlineCobaltContainer else SageOfflineContainer,
        animationSpec = spring(),
        label = "hybrid_pill_bg"
    )
    val accentColor by animateColorAsState(
        targetValue = if (isOnlineMode) OnlineCobalt else SageOffline,
        animationSpec = spring(),
        label = "hybrid_pill_fg"
    )

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
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
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.secondary
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.School,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = stringResource(R.string.app_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (uiState.chatMessages.isNotEmpty()) {
                    IconButton(
                        onClick = onClearChat,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("clear_chat_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteSweep,
                            contentDescription = stringResource(R.string.chat_clear_cd),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Hybrid Offline / Online Toggle Bar
            Surface(
                color = pillColor,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1f)
                            .clickable(onClick = onOpenModelHub)
                            .padding(vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = if (isOnlineMode) Icons.Rounded.Cloud else Icons.Rounded.OfflineBolt,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isOnlineMode) {
                                        "অনলাইন মোড (Online)"
                                    } else {
                                        "অফলাইন মোড (Offline)"
                                    },
                                    style = MaterialTheme.typography.labelLarge,
                                    color = accentColor
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(accentColor)
                                )
                            }
                            val statusSubtext = if (isOnlineMode) {
                                if (uiState.isDeviceOnline) {
                                    "Firebase AI Logic • Gemini 3.8 Flash (Live)"
                                } else {
                                    "নেটওয়ার্ক বিচ্ছিন্ন • অফলাইন মোডে সুইচ করো"
                                }
                            } else {
                                val gguf = uiState.ggufState
                                if (gguf.status == GgufModelStatus.READY_LOADED && gguf.activeMetadata != null) {
                                    "GGUF লোড: ${gguf.activeMetadata.fileName}"
                                } else {
                                    "WBBSE মাস্টার ডেটা • GGUF সেটআপ করতে ট্যাপ করো"
                                }
                            }
                            Text(
                                text = statusSubtext,
                                style = MaterialTheme.typography.bodySmall,
                                color = accentColor.copy(alpha = 0.85f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = if (isOnlineMode) stringResource(R.string.mode_online) else stringResource(R.string.mode_offline),
                            style = MaterialTheme.typography.labelMedium,
                            color = accentColor,
                            fontWeight = FontWeight.Bold
                        )
                        Switch(
                            checked = isOnlineMode,
                            onCheckedChange = onToggleMode,
                            modifier = Modifier.testTag("hybrid_mode_switch"),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = OnlineCobalt,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = SageOffline,
                                uncheckedBorderColor = Color.Transparent
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SubjectFilterRow(
    selectedSubject: WbbseSubject?,
    onSelectSubject: (WbbseSubject?) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .testTag("subject_filter_row")
    ) {
        item(key = "all") {
            FilterChip(
                selected = selectedSubject == null,
                onClick = { onSelectSubject(null) },
                label = { Text(stringResource(R.string.subject_all)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                },
                modifier = Modifier.testTag("subject_chip_all")
            )
        }

        items(WbbseSubject.entries, key = { it.id }) { subject ->
            val isSelected = selectedSubject == subject
            FilterChip(
                selected = isSelected,
                onClick = { onSelectSubject(if (isSelected) null else subject) },
                label = { Text(subject.bengaliName) },
                leadingIcon = {
                    Icon(
                        imageVector = subject.icon,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (isSelected) Color.White else subject.accentColor
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = subject.accentColor,
                    selectedLabelColor = Color.White,
                    selectedLeadingIconColor = Color.White
                ),
                modifier = Modifier.testTag("subject_chip_${subject.id}")
            )
        }
    }
}

@Composable
private fun ChatWelcomeState(
    selectedSubject: WbbseSubject?,
    inferenceMode: InferenceMode,
    onQuickPromptClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val starterPrompts = if (selectedSubject != null) {
        listOf(
            selectedSubject.sampleQuestion,
            "${selectedSubject.bengaliName} বিষয়ের গুরুত্বপূর্ণ সূত্র ও পরীক্ষার টিপস বলো",
            "${selectedSubject.englishName} key chapters summary for WBBSE Class 10"
        )
    } else {
        listOf(
            WbbseSubject.PHYSICAL_SCIENCE.sampleQuestion,
            WbbseSubject.MATHEMATICS.sampleQuestion,
            WbbseSubject.BENGALI.sampleQuestion,
            WbbseSubject.LIFE_SCIENCE.sampleQuestion,
            WbbseSubject.HISTORY.sampleQuestion,
            WbbseSubject.GEOGRAPHY.sampleQuestion,
            WbbseSubject.ENGLISH.sampleQuestion
        )
    }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(155.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_study_banner_1795374668926),
                            contentDescription = "EduAI Class 10 Study Desk",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color(0xCC1B365D)
                                        )
                                    )
                                )
                        )
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(14.dp)
                        ) {
                            Surface(
                                color = if (inferenceMode == InferenceMode.OFFLINE) SageOffline else OnlineCobalt,
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text(
                                    text = if (inferenceMode == InferenceMode.OFFLINE) {
                                        "OFFLINE • 7 SUBJECTS READY"
                                    } else {
                                        "ONLINE LIVE • GEMINI 3.8 FLASH"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = stringResource(R.string.chat_welcome_title),
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White
                            )
                        }
                    }
                    Text(
                        text = stringResource(R.string.chat_welcome_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        }

        item {
            Text(
                text = if (selectedSubject != null) {
                    "${selectedSubject.bengaliName} — প্রস্তাবিত প্রশ্ন"
                } else {
                    stringResource(R.string.chat_quick_prompts)
                },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 2.dp)
            )
        }

        items(starterPrompts) { prompt ->
            Surface(
                onClick = { onQuickPromptClick(prompt) },
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .minimumInteractiveComponentSize()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        tint = selectedSubject?.accentColor ?: MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = prompt,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatBubbleItem(
    message: ChatMessageEntity,
    isSpeaking: Boolean,
    onToggleSpeak: () -> Unit,
    onCopyClick: () -> Unit
) {
    val isUser = message.role == "user"
    val subject = WbbseSubject.fromId(message.subjectId)
    val isOnlineMsg = message.mode == InferenceMode.ONLINE.storageValue
    val attachedBitmap = remember(message.imagePath) {
        message.imagePath?.let { NotebookImageHelper.decodeFileThumbnail(it) }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 20.dp,
                topEnd = 20.dp,
                bottomStart = if (isUser) 20.dp else 6.dp,
                bottomEnd = if (isUser) 6.dp else 20.dp
            ),
            color = if (isUser) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.surface
            },
            border = if (isUser) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            tonalElevation = if (isUser) 0.dp else 1.dp,
            modifier = Modifier.widthIn(max = 335.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (!isUser) {
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
                                imageVector = if (isOnlineMsg) Icons.Rounded.Cloud else Icons.Rounded.Memory,
                                contentDescription = null,
                                tint = if (isOnlineMsg) OnlineCobalt else SageOffline,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = message.sourceLabel,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isOnlineMsg) OnlineCobalt else SageOffline,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Offline Native Text-to-Speech (Play / Stop) Button
                            IconButton(
                                onClick = onToggleSpeak,
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .size(38.dp)
                                    .testTag("tts_button_${message.id}")
                            ) {
                                Icon(
                                    imageVector = if (isSpeaking) Icons.Rounded.StopCircle else Icons.AutoMirrored.Rounded.VolumeUp,
                                    contentDescription = if (isSpeaking) {
                                        stringResource(R.string.tts_stop_cd)
                                    } else {
                                        stringResource(R.string.tts_play_cd)
                                    },
                                    tint = if (isSpeaking) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            IconButton(
                                onClick = onCopyClick,
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.ContentCopy,
                                    contentDescription = stringResource(R.string.chat_copy_cd),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                } else if (subject != null) {
                    Text(
                        text = subject.bengaliName,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                    )
                }

                if (attachedBitmap != null) {
                    Image(
                        bitmap = attachedBitmap.asImageBitmap(),
                        contentDescription = "Uploaded handwritten notebook photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                }

                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isUser) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )

                if (!isUser) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (isSpeaking) {
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "🔊 পড়ে শোনানো হচ্ছে…",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        if (subject != null) {
                            Surface(
                                color = subject.accentColor.copy(alpha = 0.14f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "${subject.bengaliName} • ${subject.shortCode}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = subject.accentColor,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        if (message.latencyMs > 0L) {
                            Text(
                                text = "${message.latencyMs}ms",
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

@Composable
private fun StreamingBubbleItem(draft: StreamingDraft) {
    val isOnline = draft.mode == InferenceMode.ONLINE
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomEnd = 20.dp, bottomStart = 6.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, (if (isOnline) OnlineCobalt else SageOffline).copy(alpha = 0.4f)),
            modifier = Modifier.widthIn(max = 335.dp)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = if (isOnline) Icons.Rounded.Cloud else Icons.Rounded.Memory,
                        contentDescription = null,
                        tint = if (isOnline) OnlineCobalt else SageOffline,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = draft.sourceLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isOnline) OnlineCobalt else SageOffline
                    )
                }
                Text(
                    text = draft.text.ifBlank { stringResource(R.string.chat_thinking) },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun ChatComposerBar(
    value: String,
    onValueChange: (String) -> Unit,
    selectedSubject: WbbseSubject?,
    pendingPhoto: PendingPhotoAttachment?,
    isGenerating: Boolean,
    onAttachPhotoClick: () -> Unit,
    onClearPhoto: () -> Unit,
    onSend: () -> Unit
) {
    val canSend = (value.isNotBlank() || pendingPhoto != null) && !isGenerating

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        shadowElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Pending Handwritten Notebook Photo Preview Chip
            if (pendingPhoto != null) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Image(
                            bitmap = pendingPhoto.previewBitmap.asImageBitmap(),
                            contentDescription = "Selected notebook photo preview",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(10.dp))
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "খাতার ছবি যুক্ত হয়েছে",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "পাঠাও বাটনে চাপ দিলে হাতের লেখা মূল্যায়ন শুরু হবে",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        IconButton(
                            onClick = onClearPhoto,
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Remove attached photo",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Camera / Gallery Attachment Button
                Surface(
                    onClick = onAttachPhotoClick,
                    enabled = !isGenerating,
                    shape = CircleShape,
                    color = if (pendingPhoto != null) {
                        MaterialTheme.colorScheme.secondaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("chat_attach_photo_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.AddAPhoto,
                            contentDescription = stringResource(R.string.chat_attach_photo_cd),
                            tint = if (pendingPhoto != null) {
                                MaterialTheme.colorScheme.secondary
                            } else {
                                MaterialTheme.colorScheme.primary
                            },
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    placeholder = {
                        Text(
                            text = when {
                                pendingPhoto != null -> "খাতার ছবি নিয়ে কিছু বলো (ঐচ্ছিক)…"
                                selectedSubject != null -> "${selectedSubject.bengaliName} নিয়ে প্রশ্ন লেখো…"
                                else -> stringResource(R.string.chat_input_placeholder)
                            },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 4,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { if (canSend) onSend() }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.background,
                        unfocusedContainerColor = MaterialTheme.colorScheme.background
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_field")
                )

                Surface(
                    onClick = onSend,
                    enabled = canSend,
                    shape = CircleShape,
                    color = if (canSend) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("chat_send_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Send,
                            contentDescription = stringResource(R.string.chat_send_cd),
                            tint = if (canSend) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}
