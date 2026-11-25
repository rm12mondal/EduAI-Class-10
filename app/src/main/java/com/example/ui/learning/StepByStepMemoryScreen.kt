package com.example.ui.learning

import android.net.Uri
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.InferenceMode
import com.example.model.SectionChatUiState
import com.example.model.StepByStepMemoryUiState
import com.example.model.WbbseSubject
import com.example.ui.chat.SectionChatPanel
import com.example.ui.theme.OnlineCobalt
import com.example.ui.theme.SageOffline

private val StepPurpleDeep = Color(0xFF311B92)
private val StepPurpleMid = Color(0xFF512DA8)
private val StepPurpleAccent = Color(0xFF6D28D9)
private val MemoryAmber = Color(0xFFF59E0B)
private val MemoryAmberDark = Color(0xFFB45309)

/**
 * Dedicated Full-Screen Interactive Interface launched strictly from the floating draggable "D" menu:
 * "🧠 ধাপে ধাপে শিখি ও মনে রাখার টেকনিক (Step-by-Step & Memory Tricks)"
 *
 * Features:
 * - Top subject selector for all 7 WBBSE subjects (Math, Physical Science, Life Science, History, Geography, Bengali, English)
 * - Step-by-Step Interactive Mode: Explains Step 1 first and asks a quick A/B check-question to unlock Step 2, Step 3, etc.
 * - Prominent "💡 সহজে মনে রাখার টেকনিক (Memory Trick)" quick-action button for rhymes, stories, mnemonics & shortcuts
 * - Completely isolated Room-persisted chat history with Clear Chat button in header
 * - Full multimodal toolbar: Text Chat, TTS Audio Playback on every AI message, Voice Input, Camera & Gallery Photo Upload
 */
@Composable
fun StepByStepMemoryScreen(
    state: StepByStepMemoryUiState,
    chatState: SectionChatUiState,
    inferenceMode: InferenceMode,
    speakingMessageId: Long?,
    voiceInjectedText: String?,
    onToggleInferenceMode: (Boolean) -> Unit,
    onSelectSubject: (WbbseSubject) -> Unit,
    onToggleMemoryTrickMode: () -> Unit,
    onTriggerQuickMemoryTrick: () -> Unit,
    onSendChatMessage: (String) -> Unit,
    onSendMemoryTrickMessage: (String) -> Unit,
    onAttachChatPhoto: (Uri) -> Unit,
    onClearPendingPhoto: () -> Unit,
    onClearChat: () -> Unit,
    onToggleSpeak: (Long, String) -> Unit,
    onDismissChatError: () -> Unit,
    onConsumeVoiceInjectedText: () -> Unit,
    onShowSnackbar: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onClose)

    val starterPrompts = remember(state.selectedSubject, state.memoryTrickMode) {
        buildStarterPromptsForSubject(
            subject = state.selectedSubject,
            memoryTrickMode = state.memoryTrickMode
        )
    }

    val showQuickUnlockBar = remember(chatState.messages, chatState.isGenerating) {
        !chatState.isGenerating && chatState.messages.lastOrNull()?.role != "user" && chatState.messages.isNotEmpty()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("step_by_step_memory_screen")
    ) {
        // High-Contrast Gradient Top Header + 7 WBBSE Subject Selector
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(StepPurpleDeep, StepPurpleMid, Color(0xFF6A1B9A))
                    )
                )
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
                        onClick = onClose,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("sbs_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "🧠 ধাপে ধাপে শিখি ও মেমোরি ট্রিক",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${state.selectedSubject.bengaliName} (${state.selectedSubject.englishName}) • ${
                                if (state.memoryTrickMode) "💡 মেমোরি ট্রিক মোড" else "🧠 ধাপে ধাপে A/B মোড"
                            }",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.92f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Compact Online / Offline Toggle
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (inferenceMode == InferenceMode.ONLINE) "অনলাইন" else "অফলাইন",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Switch(
                            checked = inferenceMode == InferenceMode.ONLINE,
                            onCheckedChange = onToggleInferenceMode,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = OnlineCobalt,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = SageOffline.copy(alpha = 0.85f)
                            ),
                            modifier = Modifier.testTag("sbs_mode_switch")
                        )
                    }
                }

                // Top Subject Selector for All 7 WBBSE Subjects
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sbs_subject_selector_row")
                ) {
                    items(WbbseSubject.entries, key = { it.id }) { subject ->
                        val selected = state.selectedSubject == subject
                        Surface(
                            onClick = { onSelectSubject(subject) },
                            shape = RoundedCornerShape(999.dp),
                            color = if (selected) Color.White else Color.White.copy(alpha = 0.16f),
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (selected) Color.White else Color.White.copy(alpha = 0.35f)
                            ),
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("sbs_subject_${subject.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = subject.icon,
                                    contentDescription = null,
                                    tint = if (selected) subject.accentColor else Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = subject.bengaliName,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selected) StepPurpleDeep else Color.White,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }

        // Prominent "💡 সহজে মনে রাখার টেকনিক (Memory Trick)" Quick-Action Bar & Mode Toggle
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            tonalElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Prominent 1-Tap Quick-Action Button for Memory Tricks (Rhymes, Stories, Mnemonics)
                    Button(
                        onClick = onTriggerQuickMemoryTrick,
                        enabled = !chatState.isGenerating,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MemoryAmberDark,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 9.dp),
                        modifier = Modifier
                            .weight(1.35f)
                            .minimumInteractiveComponentSize()
                            .testTag("sbs_memory_trick_quick_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Lightbulb,
                            contentDescription = "Memory Trick",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "💡 সহজে মনে রাখার টেকনিক",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Mode Toggle Chip: Step-by-Step vs Memory Trick Input Mode
                    Surface(
                        onClick = onToggleMemoryTrickMode,
                        shape = RoundedCornerShape(14.dp),
                        color = if (state.memoryTrickMode) {
                            MemoryAmber.copy(alpha = 0.20f)
                        } else {
                            StepPurpleAccent.copy(alpha = 0.14f)
                        },
                        border = BorderStroke(
                            1.dp,
                            if (state.memoryTrickMode) MemoryAmberDark else StepPurpleAccent
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .testTag("sbs_mode_toggle_chip")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (state.memoryTrickMode) Icons.Rounded.AutoAwesome else Icons.Rounded.Psychology,
                                contentDescription = null,
                                tint = if (state.memoryTrickMode) MemoryAmberDark else StepPurpleAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (state.memoryTrickMode) "ট্রিক মোড চালু" else "ধাপে ধাপে চালু",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (state.memoryTrickMode) MemoryAmberDark else StepPurpleAccent,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Helpful 1-line mode explanation banner
                Text(
                    text = if (state.memoryTrickMode) {
                        "💡 মেমোরি ট্রিক মোড: যেকোনো সূত্র, সাল, সংজ্ঞা বা ব্যাকরণ লিখলে/ছবি দিলে ছড়া, গল্প ও শর্টকাট ট্রিক পাবে।"
                    } else {
                        "🧠 ধাপে ধাপে মোড: অঙ্ক বা প্রশ্ন দিলে পুরো উত্তর একসাথে নয়—প্রথমে ধাপ ১ বুঝিয়ে A/B প্রশ্নে পরের ধাপ আনলক হবে!"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Quick Interactive Step Unlock Chips (A / B / Next Step / Memory Trick)
                if (showQuickUnlockBar) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sbs_quick_unlock_row")
                    ) {
                        item {
                            QuickUnlockChip(
                                label = "✅ উত্তর: A",
                                accent = Color(0xFF15803D),
                                tag = "sbs_quick_reply_a",
                                onClick = { onSendChatMessage("উত্তর: A") }
                            )
                        }
                        item {
                            QuickUnlockChip(
                                label = "🔷 উত্তর: B",
                                accent = OnlineCobalt,
                                tag = "sbs_quick_reply_b",
                                onClick = { onSendChatMessage("উত্তর: B") }
                            )
                        }
                        item {
                            QuickUnlockChip(
                                label = "🔓 পরবর্তী ধাপ",
                                accent = StepPurpleAccent,
                                tag = "sbs_quick_reply_next",
                                onClick = { onSendChatMessage("পরবর্তী ধাপ সহজভাবে দেখাও") }
                            )
                        }
                        item {
                            QuickUnlockChip(
                                label = "💡 এটার মেমোরি ট্রিক",
                                accent = MemoryAmberDark,
                                tag = "sbs_quick_reply_trick",
                                onClick = {
                                    onSendMemoryTrickMessage(
                                        "এই টপিক ও সূত্রটি সহজে মনে রাখার জন্য ছড়া, মজার গল্প ও শর্টকাট মেমোরি ট্রিক দাও।"
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        // Isolated Persistent Multimodal Chat Panel (Clear Chat + TTS Audio + Voice + Camera + Gallery)
        SectionChatPanel(
            state = chatState,
            inferenceMode = inferenceMode,
            speakingMessageId = speakingMessageId,
            accentColor = if (state.memoryTrickMode) MemoryAmberDark else StepPurpleAccent,
            starterPrompts = starterPrompts,
            onSendMessage = onSendChatMessage,
            onAttachPhotoUri = onAttachChatPhoto,
            onClearPendingPhoto = onClearPendingPhoto,
            onClearChat = onClearChat,
            onToggleSpeak = onToggleSpeak,
            onShowToast = onShowSnackbar,
            onDismissError = onDismissChatError,
            voiceInjectedText = voiceInjectedText,
            onConsumeVoiceInjectedText = onConsumeVoiceInjectedText,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun QuickUnlockChip(
    label: String,
    accent: Color,
    tag: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(999.dp),
        color = accent.copy(alpha = 0.13f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.45f)),
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .testTag(tag)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = accent,
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp)
        )
    }
}

private fun buildStarterPromptsForSubject(
    subject: WbbseSubject,
    memoryTrickMode: Boolean
): List<String> {
    if (memoryTrickMode) {
        return when (subject) {
            WbbseSubject.MATHEMATICS -> listOf(
                "ত্রিকোণমিতির sin, cos, tan মান ও পরিমিতির গোলক-শঙ্কু-চোঙ সূত্র মনে রাখার মজার ছড়া দাও",
                "শ্রীধর আচার্য ও চক্রবৃদ্ধি সুদের সূত্র মনে রাখার শর্টকাট ট্রিক বলো"
            )
            WbbseSubject.PHYSICAL_SCIENCE -> listOf(
                "বয়েল, চার্লস, অ্যাভোগাড্রো ও ওহমের সূত্র মনে রাখার ছড়া ও গল্প বলো",
                "তড়িৎ-রাসায়নিক শ্রেণি ও পর্যায় সারণির গুরুত্বপূর্ণ গ্রুপ মনে রাখার মেমোরি ট্রিক দাও"
            )
            WbbseSubject.LIFE_SCIENCE -> listOf(
                "মাইটোসিসের ৪টি দশা (PMAT) ও উদ্ভিদ হরমোনের কাজ মনে রাখার ছড়া দাও",
                "মেন্ডেলের একসংকর ও দ্বিসংকর জনন অনুপাত এবং নাইট্রোজেন চক্রের শর্টকাট ট্রিক দাও"
            )
            WbbseSubject.HISTORY -> listOf(
                "১৮৫৫ থেকে ১৯৪২ পর্যন্ত মাধ্যমিকের সব VVI ঐতিহাসিক সাল মনে রাখার গল্প ও ছড়া দাও",
                "সাঁওতাল, নীল ও ১৮৫৭ মহাবিদ্রোহের নেতাদের নাম মনে রাখার অ্যাক্রোনিম ট্রিক দাও"
            )
            WbbseSubject.GEOGRAPHY -> listOf(
                "বায়ুমণ্ডলের স্তর ও হিমবাহ-নদীর ক্ষয়জাত ভূমিরূপ মনে রাখার ছড়া ও অ্যাক্রোনিম দাও",
                "ভারতের মৃত্তিকা, নদী ও ফসল উৎপাদক রাজ্য মনে রাখার সহজ মেমোরি ট্রিক বলো"
            )
            WbbseSubject.BENGALI -> listOf(
                "বাংলা কারক, সমাস ও বাচ্য ১ সেকেন্ডে চেনার মজার ছড়া ও শর্টকাট ট্রিক দাও",
                "মাধ্যমিকের গল্প-কবিতার লেখক ও চরিত্রের নাম মনে রাখার গল্প-ট্রিক দাও"
            )
            WbbseSubject.ENGLISH -> listOf(
                "Voice Change, Narration ও Joining-এর নিয়ম মনে রাখার সহজ ছড়া ও শর্টকাট ট্রিক দাও",
                "WBBSE Class 10 Phrasal Verbs সহজে মনে রাখার মজার মেমোরি ট্রিক বলো"
            )
        }
    }

    return when (subject) {
        WbbseSubject.MATHEMATICS -> listOf(
            "x² - 5x + 6 = 0 দ্বিঘাত সমীকরণটি ধাপে ধাপে শেখাও (আগে শুধু ধাপ ১ বলো)",
            "বার্ষিক 5% সরল সুদে 2000 টাকার 3 বছরের সুদ-আসল ধাপে ধাপে বের করা শেখাও",
            "একটি গোলকের ব্যাসার্ধ 7 সেমি হলে সমগ্রতলের ক্ষেত্রফল ধাপে ধাপে শেখাও"
        )
        WbbseSubject.PHYSICAL_SCIENCE -> listOf(
            "27°C উষ্ণতায় ও 76 cm Hg চাপে কোনো গ্যাসের আয়তন 300 mL হলে STP-তে আয়তন ধাপে ধাপে শেখাও",
            "10Ω রোধের মধ্য দিয়ে 2A তড়িৎ 5 মিনিট প্রবাহিত হলে উৎপন্ন তাপ ধাপে ধাপে বের করা শেখাও",
            "ওহমের সূত্র ও রোধের শ্রেণি-সমান্তরাল সমবায়ের অঙ্ক ধাপে ধাপে বোঝাও"
        )
        WbbseSubject.LIFE_SCIENCE -> listOf(
            "মাইটোসিস কোষ বিভাজনের দশাগুলো ধাপে ধাপে প্রশ্ন করে শেখাও",
            "মেন্ডেলের একসংকর জনন চেকার বোর্ড ধাপে ধাপে বুঝিয়ে দাও"
        )
        WbbseSubject.HISTORY -> listOf(
            "১৮৫৭ সালের মহাবিদ্রোহের কারণ ও ফলাফল ধাপে ধাপে প্রশ্ন করে শেখাও",
            "নীল বিদ্রোহের গুরুত্ব ও বাংলায় নবজাগরণ ধাপে ধাপে বোঝাও"
        )
        WbbseSubject.GEOGRAPHY -> listOf(
            "নদীর ক্ষয় ও সঞ্চয়কার্যের ফলে গঠিত ভূমিরূপগুলো ধাপে ধাপে শেখাও",
            "বায়ুচাপ বলয় ও নিয়ত বায়ুপ্রবাহের সম্পর্ক ধাপে ধাপে বোঝাও"
        )
        WbbseSubject.BENGALI -> listOf(
            "কারক ও অকারক পদ কীভাবে চিনব তা উদাহরণসহ ধাপে ধাপে শেখাও",
            "কর্তৃবাচ্য থেকে কর্মবাচ্যে রূপান্তর ধাপে ধাপে অনুশীলন করাও"
        )
        WbbseSubject.ENGLISH -> listOf(
            "Teach Voice Change step by step with A/B practice questions",
            "Teach Indirect Speech (Narration Change) for WBBSE Class 10 step by step"
        )
    }
}
