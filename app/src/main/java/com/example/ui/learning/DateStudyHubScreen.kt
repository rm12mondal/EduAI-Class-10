package com.example.ui.learning

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.EditCalendar
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.HourglassTop
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.ChatMessageEntity
import com.example.model.AutoStudyPlan
import com.example.model.DateStudyDateUtils
import com.example.model.DateStudyHistoryLogItem
import com.example.model.DateStudyHubUiState
import com.example.model.DateStudyMode
import com.example.model.InferenceMode
import com.example.model.SectionChatUiState
import com.example.model.WbbseSubject
import com.example.ui.chat.SectionChatPanel
import com.example.ui.theme.OnlineCobalt
import com.example.ui.theme.SageOffline
import kotlinx.coroutines.delay

/**
 * Full-Screen Auto-Paced Exam Countdown Study Workspace:
 * - Top Live Countdown Banner: "⏳ পরীক্ষার আর X দিন বাকি" + Exam Date + Auto-Selected Plan + "তারিখ বদলান" + Date-Wise History Log
 * - Horizontal Mode Switcher Bar: Smart Learning, Fast Learning, 2/3-Month Syllabus Plan, Brahmastram, Mock Exam
 * - Horizontal Subject Bar: All Subjects + all 7 WBBSE subjects
 * - Contextual 1-Tap Auto-Paced Mode Action tailored to the exact remaining days
 * - Full Multimodal Chat Panel (Text Chat + Send, Voice Mic bn-IN/en-US, TTS Audio Playback, Camera, Gallery, Clear Chat)
 */
@Composable
fun DateStudyHubScreen(
    state: DateStudyHubUiState,
    chatState: SectionChatUiState,
    inferenceMode: InferenceMode,
    speakingMessageId: Long?,
    voiceInjectedText: String?,
    onToggleInferenceMode: (Boolean) -> Unit,
    onOpenChangeDate: () -> Unit,
    onSetHistoryVisible: (Boolean) -> Unit,
    onJumpToHistoryDate: (String) -> Unit,
    onSelectMode: (DateStudyMode) -> Unit,
    onSelectSubject: (WbbseSubject?) -> Unit,
    onTriggerOneTapModeAction: () -> Unit,
    onSendMessage: (String) -> Unit,
    onAttachPhoto: (Uri) -> Unit,
    onClearPendingPhoto: () -> Unit,
    onClearChat: () -> Unit,
    onToggleSpeak: (Long, String) -> Unit,
    onDismissError: () -> Unit,
    onConsumeVoiceInjectedText: () -> Unit,
    onOpenMindMapFromReply: (title: String, subjectLabel: String, body: String) -> Unit,
    onPrepareSystemPicker: () -> Unit,
    onShowSnackbar: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = {
        if (state.showHistorySheet) {
            onSetHistoryVisible(false)
        } else {
            onClose()
        }
    })

    val compactExamDate = remember(state.examDateIso) {
        DateStudyDateUtils.formatCompactDate(state.examDateIso)
    }
    val starterPrompts = remember(
        state.selectedMode,
        state.selectedSubject,
        state.autoPlan,
        state.daysRemaining,
        state.examDateIso
    ) {
        DateStudyDateUtils.buildStarterPrompts(
            mode = state.selectedMode,
            subject = state.selectedSubject,
            autoPlan = state.autoPlan,
            daysRemaining = state.daysRemaining,
            examDateIso = state.examDateIso
        )
    }

    val activeAccent = state.selectedMode.accentColor

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("date_study_hub_screen")
    ) {
        // 1. High-Contrast Live Exam Countdown Top Banner
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color(0xFF0F172A),
                            activeAccent.copy(alpha = 0.88f)
                        )
                    )
                )
                .padding(horizontal = 10.dp, vertical = 10.dp),
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
                        .size(40.dp)
                        .testTag("date_study_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                LiveExamCountdownHeaderBadge(
                    examDateEpochMs = state.examDateEpochMs,
                    compactExamDate = compactExamDate,
                    initialDaysRemaining = state.daysRemaining,
                    initialAutoPlan = state.autoPlan,
                    modifier = Modifier.weight(1f)
                )

                // Online/Offline quick toggle pill
                val isOnline = inferenceMode == InferenceMode.ONLINE
                Surface(
                    onClick = { onToggleInferenceMode(!isOnline) },
                    shape = RoundedCornerShape(999.dp),
                    color = if (isOnline) OnlineCobalt else SageOffline,
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("date_study_mode_toggle")
                ) {
                    Text(
                        text = if (isOnline) "🌐 অনলাইন" else "⚡ অফলাইন",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            // Secondary Banner Row: Change Exam Date + Date-Wise Study History Log buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    onClick = onOpenChangeDate,
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.16f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .testTag("date_study_change_date_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.EditCalendar,
                            contentDescription = "Change Exam Date",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "পরীক্ষার তারিখ বদলান",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Surface(
                    onClick = { onSetHistoryVisible(true) },
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.16f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("date_study_history_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.History,
                            contentDescription = "Date History",
                            tint = Color(0xFFFDE68A),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "ইতিহাস (${state.historyLogs.size})",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // 2. Compact Horizontal Selector Bars (Mode Switcher Bar + Subject Bar)
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Bar A: All 5 Major Study Modes
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("date_study_mode_bar")
                ) {
                    items(DateStudyMode.entries, key = { it.id }) { mode ->
                        val selected = state.selectedMode == mode
                        Surface(
                            onClick = { onSelectMode(mode) },
                            shape = RoundedCornerShape(12.dp),
                            color = if (selected) mode.accentColor else MaterialTheme.colorScheme.surfaceContainerHigh,
                            border = BorderStroke(
                                width = if (selected) 1.5.dp else 1.dp,
                                color = if (selected) mode.accentColor else MaterialTheme.colorScheme.outlineVariant
                            ),
                            modifier = Modifier.testTag("date_study_mode_${mode.id}")
                        ) {
                            Text(
                                text = mode.labelBn,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                // Bar B: All Subjects + 7 WBBSE Subjects
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("date_study_subject_bar")
                ) {
                    item(key = "all_subjects") {
                        val isAll = state.selectedSubject == null
                        Surface(
                            onClick = { onSelectSubject(null) },
                            shape = RoundedCornerShape(999.dp),
                            color = if (isAll) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerLow,
                            border = BorderStroke(
                                1.dp,
                                if (isAll) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                            ),
                            modifier = Modifier.testTag("date_study_subject_all")
                        ) {
                            Text(
                                text = "সব বিষয়",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isAll) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp)
                            )
                        }
                    }

                    items(WbbseSubject.entries, key = { it.id }) { subject ->
                        val selected = state.selectedSubject == subject
                        Surface(
                            onClick = { onSelectSubject(subject) },
                            shape = RoundedCornerShape(999.dp),
                            color = if (selected) activeAccent.copy(alpha = 0.18f)
                            else MaterialTheme.colorScheme.surfaceContainerLow,
                            border = BorderStroke(
                                1.dp,
                                if (selected) activeAccent else MaterialTheme.colorScheme.outlineVariant
                            ),
                            modifier = Modifier.testTag("date_study_subject_${subject.id}")
                        ) {
                            Text(
                                text = subject.bengaliName,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium,
                                color = if (selected) activeAccent else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Compact 1-Tap Auto-Paced Mode Action Trigger Strip
                Surface(
                    onClick = onTriggerOneTapModeAction,
                    enabled = !chatState.isGenerating,
                    shape = RoundedCornerShape(12.dp),
                    color = activeAccent.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, activeAccent.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .testTag("date_study_one_tap_action_btn")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = activeAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = state.selectedMode.oneTapActionLabelBn,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = activeAccent,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = activeAccent
                        ) {
                            Text(
                                text = "${state.daysRemaining}d প্ল্যান",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        // 3. Full Multimodal Chat Interface Active Across Every Mode
        SectionChatPanel(
            state = chatState,
            inferenceMode = inferenceMode,
            speakingMessageId = speakingMessageId,
            accentColor = activeAccent,
            starterPrompts = starterPrompts,
            onSendMessage = onSendMessage,
            onAttachPhotoUri = onAttachPhoto,
            onClearPendingPhoto = onClearPendingPhoto,
            onClearChat = onClearChat,
            onToggleSpeak = onToggleSpeak,
            onShowToast = onShowSnackbar,
            onDismissError = onDismissError,
            voiceInjectedText = voiceInjectedText,
            onConsumeVoiceInjectedText = onConsumeVoiceInjectedText,
            extraAssistantActionLabel = "মাইন্ড-ম্যাপ",
            onExtraAssistantAction = { msg: ChatMessageEntity ->
                val subjName = WbbseSubject.fromId(msg.subjectId)?.bengaliName
                    ?: state.selectedSubject?.bengaliName
                    ?: "WBBSE Class 10"
                onOpenMindMapFromReply(
                    msg.topicLabel.ifBlank { "${state.selectedMode.shortHeaderBn} (${state.daysRemaining} দিন বাকি)" },
                    subjName,
                    msg.content
                )
            },
            customHeaderTitle = "${state.selectedMode.shortHeaderBn} • ${state.daysRemaining} দিন বাকি",
            customHeaderSubtitle = "${state.autoPlan.shortBadgeBn} • ${state.selectedMode.subtitleBn}",
            onPrepareSystemPicker = onPrepareSystemPicker,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )
    }

    // Date-Wise Study History Modal Dialog
    if (state.showHistorySheet) {
        DateWiseHistoryDialog(
            logs = state.historyLogs,
            activeDateIso = state.selectedDateIso,
            onSelectDate = onJumpToHistoryDate,
            onOpenNewExamDatePicker = {
                onSetHistoryVisible(false)
                onOpenChangeDate()
            },
            onDismiss = { onSetHistoryVisible(false) }
        )
    }
}

/**
 * Isolated lightweight composable scope for the live exam countdown banner.
 * Checks for midnight/day rollover locally every 60 seconds without recomposing
 * the surrounding workspace or SectionChatPanel.
 */
@Composable
private fun LiveExamCountdownHeaderBadge(
    examDateEpochMs: Long,
    compactExamDate: String,
    initialDaysRemaining: Int,
    initialAutoPlan: AutoStudyPlan,
    modifier: Modifier = Modifier
) {
    var liveDays by remember(examDateEpochMs, initialDaysRemaining) {
        mutableIntStateOf(initialDaysRemaining)
    }
    LaunchedEffect(examDateEpochMs) {
        while (true) {
            delay(60_000L)
            val recomputed = DateStudyDateUtils.computeDaysRemaining(examDateEpochMs)
            if (recomputed != liveDays) {
                liveDays = recomputed
            }
        }
    }
    val resolvedPlan = remember(liveDays, initialAutoPlan) {
        if (liveDays == initialDaysRemaining) initialAutoPlan else AutoStudyPlan.fromDaysRemaining(liveDays)
    }

    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.HourglassTop,
                contentDescription = null,
                tint = Color(0xFFFDE68A),
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "⏳ পরীক্ষার আর $liveDays দিন বাকি",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.testTag("exam_countdown_banner_text")
            )
        }
        Text(
            text = "পরীক্ষা: $compactExamDate • ${resolvedPlan.labelBn}",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.92f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun DateWiseHistoryDialog(
    logs: List<DateStudyHistoryLogItem>,
    activeDateIso: String,
    onSelectDate: (String) -> Unit,
    onOpenNewExamDatePicker: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            shadowElevation = 14.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp)
                .widthIn(max = 420.dp)
                .heightIn(max = 520.dp)
                .testTag("date_study_history_dialog")
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0F172A))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.History,
                            contentDescription = null,
                            tint = Color(0xFFFDE68A),
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = "📅 তারিখভিত্তিক স্টাডি ইতিহাস",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "মোট ${logs.size}টি দিনে পড়াশোনা সেভ আছে",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close History", tint = Color.White)
                    }
                }

                if (logs.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "এখনো কোনো দিনের স্টাডি লগ জমা হয়নি",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "যেকোনো প্রশ্ন করলেই আজকের তারিখের ইতিহাস স্বয়ংক্রিয়ভাবে সেভ হয়ে যাবে।",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                    ) {
                        items(logs, key = { it.dateIso }) { item ->
                            val isActive = item.dateIso == activeDateIso
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isActive) OnlineCobalt.copy(alpha = 0.12f)
                                else MaterialTheme.colorScheme.surfaceContainerLow,
                                border = BorderStroke(
                                    1.dp,
                                    if (isActive) OnlineCobalt else MaterialTheme.colorScheme.outlineVariant
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectDate(item.dateIso) }
                                    .minimumInteractiveComponentSize()
                                    .testTag("date_study_history_item_${item.dateIso}")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isActive) OnlineCobalt else MaterialTheme.colorScheme.primaryContainer
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = item.dateIso.takeLast(2),
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (isActive) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = item.displayDateBn,
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f, fill = false)
                                            )
                                            Surface(
                                                shape = RoundedCornerShape(999.dp),
                                                color = SageOffline.copy(alpha = 0.16f)
                                            ) {
                                                Text(
                                                    text = "${item.messageCount} মেসেজ",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = SageOffline,
                                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = item.lastTopic.ifBlank { item.lastModeLabel },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Surface(
                        onClick = onOpenNewExamDatePicker,
                        shape = RoundedCornerShape(12.dp),
                        color = OnlineCobalt,
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Text(
                            text = "📅 পরীক্ষার তারিখ বদলান",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        )
                    }
                }
            }
        }
    }
}
