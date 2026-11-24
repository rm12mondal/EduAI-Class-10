package com.example.ui.extras

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
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.HighlightOff
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.RapidFireResult
import com.example.model.RapidFireTimerMode
import com.example.model.RapidFireUiState
import com.example.model.WbbseSubject
import com.example.ui.theme.MonoFontFamily
import com.example.ui.theme.OnlineCobalt
import com.example.ui.theme.SageOffline
import com.example.ui.theme.TerracottaAccent
import java.util.Locale

/**
 * Exam Hall Speed Booster — "র্যাপিড ফায়ার (Rapid Fire MCQ)" Screen.
 * Works 100% Offline with 20 or 30 WBBSE 1-mark MCQs, strict countdown timer
 * (30s/question or 15 min total), instant speed analytics, and automatic Mistake Notebook sync.
 */
@Composable
fun RapidFireScreen(
    state: RapidFireUiState,
    onSelectSubject: (WbbseSubject?) -> Unit,
    onSelectQuestionCount: (Int) -> Unit,
    onSelectTimerMode: (RapidFireTimerMode) -> Unit,
    onStartDrill: () -> Unit,
    onAnswerOption: (Int) -> Unit,
    onNextQuestion: () -> Unit,
    onFinishEarly: () -> Unit,
    onOpenMistakeRecovery: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

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
            // Top Bar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("rapid_fire_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "র্যাপিড ফায়ার (Speed Booster)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "১০০% অফলাইন • পরীক্ষা হলের স্পিড ও নির্ভুলতা অনুশীলন",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (state.isRunning) {
                        TextButton(onClick = onFinishEarly) {
                            Text("শেষ করো", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            when {
                state.isRunning -> RapidFireActiveRunner(
                    state = state,
                    onAnswerOption = onAnswerOption,
                    onNextQuestion = onNextQuestion,
                    modifier = Modifier.weight(1f)
                )

                state.result != null -> RapidFireAnalyticsReport(
                    result = state.result,
                    onPlayAgain = onStartDrill,
                    onOpenMistakeRecovery = onOpenMistakeRecovery,
                    onBackToSetup = { onSelectSubject(state.selectedSubject) },
                    modifier = Modifier.weight(1f)
                )

                else -> RapidFireSetupView(
                    state = state,
                    onSelectSubject = onSelectSubject,
                    onSelectQuestionCount = onSelectQuestionCount,
                    onSelectTimerMode = onSelectTimerMode,
                    onStartDrill = onStartDrill,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun RapidFireSetupView(
    state: RapidFireUiState,
    onSelectSubject: (WbbseSubject?) -> Unit,
    onSelectQuestionCount: (Int) -> Unit,
    onSelectTimerMode: (RapidFireTimerMode) -> Unit,
    onStartDrill: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                border = BorderStroke(1.2.dp, TerracottaAccent.copy(alpha = 0.45f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF7C2D12), Color(0xFFB45309), Color(0xFF9A3412))
                            )
                        )
                        .padding(18.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Rounded.Bolt, contentDescription = null, tint = Color(0xFFFFD54F), modifier = Modifier.size(28.dp))
                            Text(
                                text = "মাধ্যমিক স্পিড বুস্টার চ্যালেঞ্জ",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Text(
                            text = "টাইমারের বিপরীতে ২০ বা ৩০টি ১-নম্বরের WBBSE MCQ সমাধান করো! প্রতিটি ভুল উত্তর স্বয়ংক্রিয়ভাবে ‘ভুলের খাতা’য় সংরক্ষিত হবে।",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.92f)
                        )
                    }
                }
            }
        }

        // Subject Picker
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "১. বিষয় নির্বাচন করো:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = state.selectedSubject == null,
                            onClick = { onSelectSubject(null) },
                            label = { Text("সকল ৭ বিষয় মিক্স") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = TerracottaAccent.copy(alpha = 0.18f),
                                selectedLabelColor = TerracottaAccent
                            ),
                            modifier = Modifier.testTag("rf_subject_all")
                        )
                    }
                    items(WbbseSubject.entries, key = { it.id }) { subj ->
                        FilterChip(
                            selected = state.selectedSubject == subj,
                            onClick = { onSelectSubject(subj) },
                            label = { Text(subj.bengaliName) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = subj.accentColor.copy(alpha = 0.18f),
                                selectedLabelColor = subj.accentColor
                            ),
                            modifier = Modifier.testTag("rf_subject_${subj.id}")
                        )
                    }
                }
            }
        }

        // Question Count Picker (20 vs 30)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "২. প্রশ্নের সংখ্যা:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf(20, 30).forEach { count ->
                        val selected = state.questionCountTarget == count
                        FilterChip(
                            selected = selected,
                            onClick = { onSelectQuestionCount(count) },
                            label = {
                                Text(
                                    text = "${count}টি ১-নম্বরের MCQ",
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .minimumInteractiveComponentSize()
                                .testTag("rf_count_$count")
                        )
                    }
                }
            }
        }

        // Timer Mode Picker
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "৩. কাউন্টডাউন টাইমার মোড:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    RapidFireTimerMode.entries.forEach { mode ->
                        val selected = state.timerMode == mode
                        Card(
                            onClick = { onSelectTimerMode(mode) },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (selected) OnlineCobalt.copy(alpha = 0.14f)
                                else MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(
                                if (selected) 1.5.dp else 1.dp,
                                if (selected) OnlineCobalt else MaterialTheme.colorScheme.outlineVariant
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("rf_timer_${mode.name}")
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Timer,
                                    contentDescription = null,
                                    tint = if (selected) OnlineCobalt else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = mode.labelBn,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selected) OnlineCobalt else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = mode.subtitleBn,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Button(
                onClick = onStartDrill,
                colors = ButtonDefaults.buttonColors(containerColor = TerracottaAccent),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("start_rapid_fire_btn")
            ) {
                Icon(Icons.Rounded.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "⚡ র্যাপিড ফায়ার শুরু করো (${state.questionCountTarget} প্রশ্ন)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun RapidFireActiveRunner(
    state: RapidFireUiState,
    onAnswerOption: (Int) -> Unit,
    onNextQuestion: () -> Unit,
    modifier: Modifier = Modifier
) {
    val question = state.questions.getOrNull(state.currentIndex) ?: return
    val maxTimer = if (state.timerMode == RapidFireTimerMode.PER_QUESTION_30S) 30f else 900f
    val timerFraction = (state.secondsRemaining / maxTimer).coerceIn(0f, 1f)
    val timerColor = when {
        state.secondsRemaining <= 8 -> Color(0xFFDC2626)
        state.secondsRemaining <= 15 -> Color(0xFFEA580C)
        else -> SageOffline
    }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Timer + Progress + Combo Streak
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "প্রশ্ন ${state.currentIndex + 1} / ${state.questions.size}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )

                        if (state.currentStreakCombo >= 2) {
                            Surface(
                                color = Color(0xFFFF6D00).copy(alpha = 0.15f),
                                shape = RoundedCornerShape(999.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Icon(Icons.Rounded.LocalFireDepartment, contentDescription = null, tint = Color(0xFFE65100), modifier = Modifier.size(15.dp))
                                    Text(
                                        text = "${state.currentStreakCombo}x কম্বো",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFE65100)
                                    )
                                }
                            }
                        }

                        Surface(
                            color = timerColor.copy(alpha = 0.14f),
                            shape = RoundedCornerShape(999.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Rounded.Timer, contentDescription = null, tint = timerColor, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                val mins = state.secondsRemaining / 60
                                val secs = state.secondsRemaining % 60
                                Text(
                                    text = if (state.timerMode == RapidFireTimerMode.TOTAL_15_MIN) {
                                        String.format(Locale.US, "%02d:%02d", mins, secs)
                                    } else {
                                        "${state.secondsRemaining}s"
                                    },
                                    style = MaterialTheme.typography.labelLarge.copy(fontFamily = MonoFontFamily),
                                    fontWeight = FontWeight.Bold,
                                    color = timerColor
                                )
                            }
                        }
                    }

                    LinearProgressIndicator(
                        progress = { timerFraction },
                        color = timerColor,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(999.dp))
                    )
                }
            }
        }

        // Question Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.2.dp, question.subject.accentColor.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = question.subject.accentColor.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "${question.subject.bengaliName} • ${question.chapterLabel}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = question.subject.accentColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Text(
                        text = question.questionText,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // 4 Options
        itemsIndexed(question.options) { idx, optionText ->
            val labels = listOf("A", "B", "C", "D")
            val isAnswered = state.showFeedbackForCurrent
            val isCorrectOption = idx == question.correctIndex
            val isUserChoice = idx == state.selectedOptionForCurrent

            val bgColor = when {
                !isAnswered -> MaterialTheme.colorScheme.surface
                isCorrectOption -> Color(0xFF16A34A).copy(alpha = 0.15f)
                isUserChoice && !isCorrectOption -> Color(0xFFDC2626).copy(alpha = 0.15f)
                else -> MaterialTheme.colorScheme.surface
            }
            val borderColor = when {
                !isAnswered -> MaterialTheme.colorScheme.outlineVariant
                isCorrectOption -> Color(0xFF16A34A)
                isUserChoice && !isCorrectOption -> Color(0xFFDC2626)
                else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            }

            Card(
                onClick = { if (!isAnswered) onAnswerOption(idx) },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = bgColor),
                border = BorderStroke(if (isAnswered && (isCorrectOption || isUserChoice)) 1.5.dp else 1.dp, borderColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("rf_option_$idx")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        color = borderColor.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.size(30.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = labels.getOrElse(idx) { "${idx + 1}" },
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        text = optionText,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    if (isAnswered && isCorrectOption) {
                        Icon(Icons.Rounded.CheckCircle, contentDescription = "Correct", tint = Color(0xFF16A34A))
                    } else if (isAnswered && isUserChoice) {
                        Icon(Icons.Rounded.HighlightOff, contentDescription = "Wrong", tint = Color(0xFFDC2626))
                    }
                }
            }
        }

        // Instant Explanation + Next Button
        if (state.showFeedbackForCurrent) {
            item {
                val timedOut = state.selectedOptionForCurrent == null
                val wasCorrect = state.selectedOptionForCurrent == question.correctIndex
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (wasCorrect) SageOffline.copy(alpha = 0.12f)
                        else TerracottaAccent.copy(alpha = 0.12f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = when {
                                wasCorrect -> "✅ দারুণ স্পিড! একদম সঠিক উত্তর।"
                                timedOut -> "⏱️ সময় শেষ! ভুলের খাতায় যুক্ত হবে।"
                                else -> "❌ ভুল উত্তর — ভুলের খাতায় যুক্ত হবে।"
                            },
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "💡 ব্যাখ্যা: ${question.explanation}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Button(
                            onClick = onNextQuestion,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .minimumInteractiveComponentSize()
                                .testTag("rf_next_btn")
                        ) {
                            Text(if (state.currentIndex < state.questions.lastIndex) "পরবর্তী প্রশ্ন" else "ফলাফল ও স্পিড রিপোর্ট দেখো")
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RapidFireAnalyticsReport(
    result: RapidFireResult,
    onPlayAgain: () -> Unit,
    onOpenMistakeRecovery: () -> Unit,
    onBackToSetup: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.4.dp, OnlineCobalt.copy(alpha = 0.45f))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Rounded.Speed, contentDescription = null, tint = OnlineCobalt, modifier = Modifier.size(38.dp))
                    Text(
                        text = result.speedRatingBn,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = OnlineCobalt
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatTile("সঠিক উত্তর", "${result.correctCount}/${result.totalQuestions}", SageOffline)
                        StatTile("নির্ভুলতার হার", "${result.accuracyPercent}%", OnlineCobalt)
                        StatTile(
                            "গড় সময়/প্রশ্ন",
                            String.format(Locale.US, "%.1fs", result.avgSecondsPerQuestion),
                            TerracottaAccent
                        )
                    }

                    if (result.mistakesSavedCount > 0) {
                        Surface(
                            color = TerracottaAccent.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "📒 ${result.mistakesSavedCount}টি ভুল/টাইমআউট প্রশ্ন স্বয়ংক্রিয়ভাবে ‘ভুলের খাতা’য় (Mistake Notebook) সেভ করা হয়েছে!",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = TerracottaAccent,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onPlayAgain,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .minimumInteractiveComponentSize()
                        ) {
                            Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("আবার খেলো")
                        }

                        if (result.mistakesSavedCount > 0) {
                            FilledTonalButton(
                                onClick = onOpenMistakeRecovery,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .minimumInteractiveComponentSize()
                                    .testTag("rf_open_mistakes_btn")
                            ) {
                                Text("ভুলের খাতা খোলো", maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        } else {
                            OutlinedButton(
                                onClick = onBackToSetup,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .minimumInteractiveComponentSize()
                            ) {
                                Text("সেটিংস বদলাও")
                            }
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "প্রশ্নভিত্তিক স্পিড বিশ্লেষণ (${result.records.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }

        itemsIndexed(result.records) { idx, rec ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(
                    1.dp,
                    if (rec.isCorrect) SageOffline.copy(alpha = 0.45f) else Color(0xFFDC2626).copy(alpha = 0.45f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (rec.isCorrect) "✅ প্রশ্ন ${idx + 1} (সঠিক)" else "❌ প্রশ্ন ${idx + 1} (ভুল)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (rec.isCorrect) SageOffline else Color(0xFFC62828)
                        )
                        Text(
                            text = "${rec.timeTakenSec.toInt()} সেকেন্ড",
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = MonoFontFamily),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(text = rec.question.questionText, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                    Text(
                        text = "সঠিক উত্তর: ${rec.question.options.getOrElse(rec.question.correctIndex) { "" }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = SageOffline
                    )
                }
            }
        }
    }
}

@Composable
private fun StatTile(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(fontFamily = MonoFontFamily),
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
