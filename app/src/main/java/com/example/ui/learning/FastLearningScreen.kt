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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Chat
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.BrahmastramProgressEntity
import com.example.data.local.ExamAttemptEntity
import com.example.data.local.MasterDataEntity
import com.example.data.master.MathUnicodeFormatter
import com.example.model.FastLearningChapterItem
import com.example.model.FastLearningContextHelper
import com.example.model.FastLearningSelfReport
import com.example.model.FastLearningUiState
import com.example.model.InferenceMode
import com.example.model.SectionChatUiState
import com.example.model.SyllabusPlanOption
import com.example.model.WbbseSubject
import com.example.ui.chat.SectionChatPanel
import com.example.ui.theme.MonoFontFamily
import com.example.ui.theme.OnlineCobalt
import com.example.ui.theme.SageOffline
import com.example.ui.theme.isDarkSurfaceTheme
import com.example.ui.theme.splitFormulaLines

/**
 * Dedicated "🚀 ফাস্ট লার্নিং (Fast Learning)" Hub accessed from the Draggable "D" Button.
 * - All 7 WBBSE subjects & chapters sorted by high-weightage VVI priority
 * - Exam Year selector (e.g., 2027, 2028, 2029) & Syllabus Completion Plan (2.5M / 3M / 4M / 5M)
 * - Motivating Self-Report Card (days remaining, syllabus %, mock test scores)
 * - Separate persistent Fast Learning Chat with full Mic, Audio Playback, Gallery & Camera support
 *   passing real-time device date/time & upcoming Exam Year pattern into AI context.
 */
@Composable
fun FastLearningScreen(
    state: FastLearningUiState,
    chatState: SectionChatUiState,
    masterNotes: List<MasterDataEntity>,
    examAttempts: List<ExamAttemptEntity>,
    brahmastramProgress: List<BrahmastramProgressEntity>,
    inferenceMode: InferenceMode,
    speakingMessageId: Long?,
    voiceInjectedText: String?,
    onToggleInferenceMode: (Boolean) -> Unit,
    onSelectTab: (Int) -> Unit,
    onSelectSubject: (WbbseSubject?) -> Unit,
    onSelectExamYear: (Int) -> Unit,
    onSelectPlan: (SyllabusPlanOption) -> Unit,
    onToggleChapterCompleted: (String) -> Unit,
    onQuickReviseChapter: (FastLearningChapterItem) -> Unit,
    onSendChatMessage: (String) -> Unit,
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
    BackHandler {
        if (state.activeTabIndex == 1) onSelectTab(0) else onClose()
    }

    val selfReport = remember(
        state.targetExamYear,
        state.syllabusPlan,
        masterNotes,
        state.completedChapterKeys,
        examAttempts,
        brahmastramProgress
    ) {
        FastLearningContextHelper.computeSelfReport(
            targetExamYear = state.targetExamYear,
            planOption = state.syllabusPlan,
            allMasterNotes = masterNotes,
            completedChapterKeys = state.completedChapterKeys,
            examAttempts = examAttempts,
            brahmastramProgress = brahmastramProgress
        )
    }

    val prioritizedChapters = remember(
        masterNotes,
        state.completedChapterKeys,
        state.selectedSubject
    ) {
        FastLearningContextHelper.buildPrioritizedChapters(
            masterNotes = masterNotes,
            completedChapterKeys = state.completedChapterKeys,
            selectedSubject = state.selectedSubject
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("fast_learning_screen"),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 760.dp)
        ) {
            // High-Contrast Deep Indigo-Cyan-Amber Top Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF0F172A),
                                Color(0xFF1D4ED8),
                                Color(0xFF0E7490)
                            )
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
                                if (state.activeTabIndex == 1) onSelectTab(0) else onClose()
                            },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("fast_learning_back_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "🚀 ফাস্ট লার্নিং (Fast Learning)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "মাধ্যমিক ${state.targetExamYear} লক্ষ্য • ${selfReport.deviceDateTimeBn}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.90f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

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
                                    checkedTrackColor = Color(0xFF38BDF8),
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = Color.White.copy(alpha = 0.30f)
                                ),
                                modifier = Modifier.testTag("fast_learning_mode_switch")
                            )
                        }
                    }

                    // 2-Tab Switcher: VVI Syllabus & Self-Report ↔ Fast Learning Chat
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            onClick = { onSelectTab(0) },
                            shape = RoundedCornerShape(12.dp),
                            color = if (state.activeTabIndex == 0) Color(0xFFFDE047) else Color.White.copy(alpha = 0.16f),
                            modifier = Modifier
                                .weight(1f)
                                .minimumInteractiveComponentSize()
                                .testTag("fast_learning_tab_syllabus")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.RocketLaunch,
                                    contentDescription = null,
                                    tint = if (state.activeTabIndex == 0) Color(0xFF0F172A) else Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "VVI সিলেবাস ও রিপোর্ট",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (state.activeTabIndex == 0) Color(0xFF0F172A) else Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Surface(
                            onClick = { onSelectTab(1) },
                            shape = RoundedCornerShape(12.dp),
                            color = if (state.activeTabIndex == 1) Color(0xFFFDE047) else Color.White.copy(alpha = 0.16f),
                            modifier = Modifier
                                .weight(1f)
                                .minimumInteractiveComponentSize()
                                .testTag("fast_learning_tab_chat")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Chat,
                                    contentDescription = null,
                                    tint = if (state.activeTabIndex == 1) Color(0xFF0F172A) else Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "ফাস্ট চ্যাট (${chatState.messages.size})",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (state.activeTabIndex == 1) Color(0xFF0F172A) else Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            if (state.activeTabIndex == 0) {
                FastLearningSyllabusAndReportView(
                    state = state,
                    selfReport = selfReport,
                    chapters = prioritizedChapters,
                    onSelectSubject = onSelectSubject,
                    onSelectExamYear = onSelectExamYear,
                    onSelectPlan = onSelectPlan,
                    onToggleChapterCompleted = onToggleChapterCompleted,
                    onQuickReviseChapter = onQuickReviseChapter,
                    modifier = Modifier.weight(1f)
                )
            } else {
                val starterPrompts = remember(state.targetExamYear, state.selectedSubject) {
                    val yr = state.targetExamYear
                    val subjName = state.selectedSubject?.bengaliName
                    if (subjName != null) {
                        listOf(
                            "মাধ্যমিক $yr-এর জন্য $subjName বিষয়ের সবচেয়ে গুরুত্বপূর্ণ VVI অধ্যায়ের মাইক্রো-সামারি দাও।",
                            "$subjName-এর আসন্ন $yr বোর্ড পরীক্ষার সম্ভাব্য ৫ নম্বরের প্রশ্ন ও ১০-সেকেন্ড ট্রিক বলো।",
                            "$subjName থেকে কোন সূত্র ও সংজ্ঞাগুলো $yr-এ আসার সম্ভাবনা সবচেয়ে বেশি?"
                        )
                    } else {
                        listOf(
                            "মাধ্যমিক $yr-এর জন্য ভৌত বিজ্ঞানের 'চলতড়িৎ' অধ্যায়ের সুপার-ফাস্ট মাইক্রো-সামারি দাও।",
                            "গণিতের 'একচলবিশিষ্ট দ্বিঘাত সমীকরণ' ও 'উপপাদ্য' $yr বোর্ড প্যাটার্নে শর্টকাটে বোঝাও।",
                            "জীবন বিজ্ঞানের 'কোষ বিভাজন' অধ্যায়ের ৩-লাইন সামারি ও সম্ভাব্য $yr প্রশ্ন বলো।"
                        )
                    }
                }
                SectionChatPanel(
                    state = chatState,
                    inferenceMode = inferenceMode,
                    speakingMessageId = speakingMessageId,
                    accentColor = Color(0xFF0E7490),
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
    }
}

@Composable
private fun FastLearningSyllabusAndReportView(
    state: FastLearningUiState,
    selfReport: FastLearningSelfReport,
    chapters: List<FastLearningChapterItem>,
    onSelectSubject: (WbbseSubject?) -> Unit,
    onSelectExamYear: (Int) -> Unit,
    onSelectPlan: (SyllabusPlanOption) -> Unit,
    onToggleChapterCompleted: (String) -> Unit,
    onQuickReviseChapter: (FastLearningChapterItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isDarkSurfaceTheme()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Exam Year & Syllabus Completion Plan Selector Card
        item(key = "fl_target_selector_card") {
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
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CalendarMonth,
                            contentDescription = null,
                            tint = OnlineCobalt,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "তোমার মাধ্যমিক পরীক্ষার বছর (Target Exam Year)",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        state.availableExamYears.forEach { year ->
                            val selected = state.targetExamYear == year
                            Surface(
                                onClick = { onSelectExamYear(year) },
                                shape = RoundedCornerShape(12.dp),
                                color = if (selected) OnlineCobalt else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .weight(1f)
                                    .minimumInteractiveComponentSize()
                                    .testTag("fl_exam_year_$year")
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(vertical = 9.dp)
                                ) {
                                    Text(
                                        text = "মাধ্যমিক $year",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Schedule,
                            contentDescription = null,
                            tint = Color(0xFF0E7490),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "সিলেবাস শেষ করার লক্ষ্য (Syllabus Plan)",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SyllabusPlanOption.entries.forEach { option ->
                            val selected = state.syllabusPlan == option
                            Surface(
                                onClick = { onSelectPlan(option) },
                                shape = RoundedCornerShape(12.dp),
                                color = if (selected) Color(0xFF0E7490) else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .weight(1f)
                                    .minimumInteractiveComponentSize()
                                    .testTag("fl_plan_${option.totalDays}")
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                                ) {
                                    Text(
                                        text = option.labelBn,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "${option.totalDays} দিন",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (selected) Color.White.copy(alpha = 0.90f)
                                        else MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Motivating Self-Report Card (Days Remaining, Syllabus %, Mock Test Scores)
        item(key = "fl_self_report_card") {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.Transparent,
                border = BorderStroke(1.2.dp, Color(0xFF38BDF8).copy(alpha = 0.45f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("fast_learning_self_report_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF1E1B4B),
                                    Color(0xFF1E3A8A),
                                    Color(0xFF115E59)
                                )
                            )
                        )
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "📊 আমার সেলফ-রিপোর্ট কার্ড",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Text(
                                text = selfReport.motivationTitleBn,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFDE047)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = Color.White.copy(alpha = 0.16f)
                        ) {
                            Text(
                                text = "দৈনিক ${selfReport.recommendedChaptersPerDay} অধ্যায়",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }

                    // 3 Key Stat Tiles
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SelfReportStatTile(
                            label = "বাকি দিন",
                            value = "${selfReport.daysRemainingToExam}",
                            sub = "ফেব্রু ${selfReport.targetExamYear}",
                            accent = Color(0xFFFDE047),
                            modifier = Modifier.weight(1f)
                        )
                        SelfReportStatTile(
                            label = "সিলেবাস সম্পন্ন",
                            value = "${selfReport.syllabusCompletionPercent}%",
                            sub = "${selfReport.completedChaptersCount}/${selfReport.totalChaptersCount} অধ্যায়",
                            accent = Color(0xFF4ADE80),
                            modifier = Modifier.weight(1f)
                        )
                        SelfReportStatTile(
                            label = "মক টেস্ট গড়",
                            value = if (selfReport.mockTestsTaken > 0) "${selfReport.mockAveragePercent}%" else "--",
                            sub = "${selfReport.mockTestsTaken}টি পরীক্ষা",
                            accent = Color(0xFF38BDF8),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    LinearProgressIndicator(
                        progress = { (selfReport.syllabusCompletionPercent / 100f).coerceIn(0f, 1f) },
                        color = Color(0xFF4ADE80),
                        trackColor = Color.White.copy(alpha = 0.20f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(999.dp))
                    )

                    Text(
                        text = selfReport.motivationMessageBn,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.94f)
                    )
                }
            }
        }

        // 3. 7-Subject Filter Strip
        item(key = "fl_subject_filter") {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "🔥 হাই-ওয়েটেজ VVI অগ্রাধিকার অনুযায়ী ৭টি বিষয়ের অধ্যায় (${chapters.size}টি)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item(key = "fl_subj_all") {
                        FilterChip(
                            selected = state.selectedSubject == null,
                            onClick = { onSelectSubject(null) },
                            label = { Text("সব ৭টি বিষয়", fontWeight = FontWeight.SemiBold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = OnlineCobalt,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("fl_subject_all")
                        )
                    }
                    items(WbbseSubject.entries, key = { it.id }) { subj ->
                        val selected = state.selectedSubject == subj
                        FilterChip(
                            selected = selected,
                            onClick = { onSelectSubject(if (selected) null else subj) },
                            label = { Text(subj.bengaliName, fontWeight = FontWeight.SemiBold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = subj.accentColor,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("fl_subject_${subj.id}")
                        )
                    }
                }
            }
        }

        // 4. VVI-Prioritized Chapter Cards
        items(chapters, key = { it.chapterKey }) { chapter ->
            FastLearningChapterCard(
                item = chapter,
                targetExamYear = state.targetExamYear,
                isDark = isDark,
                onToggleCompleted = { onToggleChapterCompleted(chapter.chapterKey) },
                onQuickRevise = { onQuickReviseChapter(chapter) }
            )
        }

        item { Spacer(modifier = Modifier.height(56.dp)) }
    }
}

@Composable
private fun SelfReportStatTile(
    label: String,
    value: String,
    sub: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color.White.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.35f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.85f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontFamily = MonoFontFamily),
                fontWeight = FontWeight.ExtraBold,
                color = accent,
                maxLines = 1
            )
            Text(
                text = sub,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.80f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun FastLearningChapterCard(
    item: FastLearningChapterItem,
    targetExamYear: Int,
    isDark: Boolean,
    onToggleCompleted: () -> Unit,
    onQuickRevise: () -> Unit
) {
    val badgeColor = when {
        item.priorityBadgeBn.contains("সুপার") -> if (isDark) Color(0xFFFF8A80) else Color(0xFFC62828)
        item.priorityBadgeBn.contains("হাই") -> if (isDark) Color(0xFFFFD54F) else Color(0xFFB45309)
        else -> if (isDark) Color(0xFF90CAF9) else OnlineCobalt
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isCompleted) {
                SageOffline.copy(alpha = if (isDark) 0.18f else 0.08f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = BorderStroke(
            1.dp,
            if (item.isCompleted) SageOffline.copy(alpha = 0.55f) else MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(13.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Checkbox(
                    checked = item.isCompleted,
                    onCheckedChange = { onToggleCompleted() },
                    modifier = Modifier.testTag("fl_chapter_check_${item.chapterKey}")
                )
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = item.subject.accentColor.copy(alpha = 0.18f)
                        ) {
                            Text(
                                text = item.subject.bengaliName,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White else item.subject.accentColor,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = item.priorityBadgeBn,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = badgeColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.chapterName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "সম্ভাব্য ওয়েটেজ: ~${item.estimatedBoardMarks} নম্বর • ${item.topicsCount}টি টপিক (${item.vviCount} VVI)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (item.keyTopicsPreview.isNotBlank()) {
                Text(
                    text = "মূল টপিক: ${item.keyTopicsPreview}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            val formulaLines = remember(item.firstFormulaHint) {
                splitFormulaLines(MathUnicodeFormatter.format(item.firstFormulaHint)).take(2)
            }
            if (formulaLines.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = OnlineCobalt.copy(alpha = if (isDark) 0.22f else 0.08f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        formulaLines.forEach { line ->
                            Text(
                                text = "• $line",
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = MonoFontFamily),
                                fontWeight = FontWeight.SemiBold,
                                color = if (isDark) Color.White else OnlineCobalt,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Button(
                onClick = onQuickRevise,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0E7490),
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .minimumInteractiveComponentSize()
                    .testTag("fl_quick_revise_${item.chapterKey}")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Bolt,
                    contentDescription = null,
                    tint = Color(0xFFFDE047),
                    modifier = Modifier.size(17.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "⚡ $targetExamYear ফাস্ট মাইক্রো-সামারি পড়ো",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
