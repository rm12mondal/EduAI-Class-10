package com.example.ui.learning

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.HighlightOff
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.MemorizerPointCard
import com.example.model.QuickTopicQuiz
import com.example.model.SmartMemorizerDeck
import com.example.ui.theme.MonoFontFamily
import com.example.ui.theme.OnlineCobalt
import com.example.ui.theme.SageOffline
import com.example.ui.theme.TerracottaAccent
import java.util.Locale

/**
 * Interactive 2-Minute Quick Quiz Dialog launched directly from "পড়া বুঝেছি — ২ মিনিটের কুইজ দিন".
 */
@Composable
fun TwoMinuteQuizDialog(
    quiz: QuickTopicQuiz,
    remainingSeconds: Int,
    selectedAnswers: Map<String, Int>,
    submitted: Boolean,
    onSelectOption: (String, Int) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit
) {
    val accent = quiz.subject?.accentColor ?: OnlineCobalt
    val mins = remainingSeconds / 60
    val secs = remainingSeconds % 60
    val timeText = String.format(Locale.US, "%02d:%02d", mins, secs)
    val correctCount = quiz.questions.count { q -> selectedAnswers[q.id] == q.correctIndex }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.background,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.88f)
                .testTag("smart_learning_quiz_dialog")
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header + Countdown Timer
                Surface(
                    color = accent.copy(alpha = 0.12f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "⚡ ২ মিনিটের দ্রুত কুইজ",
                                style = MaterialTheme.typography.titleMedium,
                                color = accent,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${quiz.topicTitle} • ${quiz.sourceLabel}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                color = if (remainingSeconds <= 20 && !submitted) TerracottaAccent else accent,
                                shape = RoundedCornerShape(999.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Timer,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        text = timeText,
                                        style = MaterialTheme.typography.labelMedium.copy(fontFamily = MonoFontFamily),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(Icons.Rounded.Close, contentDescription = "Close quiz")
                            }
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (submitted) {
                        item(key = "quiz_result_banner") {
                            val pct = ((correctCount.toFloat() / quiz.questions.size.coerceAtLeast(1)) * 100f).toInt()
                            val resultColor = if (pct >= 75) SageOffline else TerracottaAccent
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = resultColor.copy(alpha = 0.14f)),
                                border = BorderStroke(1.2.dp, resultColor)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = if (pct >= 75) "🎉 দারুণ! টপিকটি তোমার খুব ভালো বোঝা হয়ে গেছে!" else "💪 ভালো চেষ্টা! নিচের ব্যাখ্যাগুলো দেখে নাও।",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = resultColor,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "স্কোর: $correctCount / ${quiz.questions.size} ($pct%) • প্রগ্রেস ড্যাশবোর্ডে সংরক্ষিত হয়েছে",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }

                    itemsIndexed(quiz.questions, key = { _, q -> q.id }) { idx, q ->
                        val selectedIdx = selectedAnswers[q.id]
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "প্রশ্ন ${idx + 1}. ${q.question}",
                                    style = MaterialTheme.typography.titleSmall
                                )
                                q.options.forEachIndexed { optIdx, optText ->
                                    val isChosen = selectedIdx == optIdx
                                    val isRight = optIdx == q.correctIndex
                                    val borderCol = when {
                                        submitted && isRight -> SageOffline
                                        submitted && isChosen && !isRight -> MaterialTheme.colorScheme.error
                                        isChosen -> accent
                                        else -> MaterialTheme.colorScheme.outlineVariant
                                    }
                                    val bgCol = when {
                                        submitted && isRight -> SageOffline.copy(alpha = 0.11f)
                                        submitted && isChosen && !isRight -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.45f)
                                        isChosen -> accent.copy(alpha = 0.08f)
                                        else -> Color.Transparent
                                    }

                                    Surface(
                                        onClick = { onSelectOption(q.id, optIdx) },
                                        enabled = !submitted,
                                        shape = RoundedCornerShape(10.dp),
                                        color = bgCol,
                                        border = BorderStroke(1.dp, borderCol),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RadioButton(
                                                selected = isChosen,
                                                onClick = if (submitted) null else ({ onSelectOption(q.id, optIdx) })
                                            )
                                            Text(
                                                text = optText,
                                                style = MaterialTheme.typography.bodyMedium,
                                                modifier = Modifier.weight(1f)
                                            )
                                            if (submitted && isRight) {
                                                Icon(
                                                    Icons.Rounded.CheckCircle,
                                                    contentDescription = "Correct",
                                                    tint = SageOffline,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            } else if (submitted && isChosen && !isRight) {
                                                Icon(
                                                    Icons.Rounded.HighlightOff,
                                                    contentDescription = "Incorrect",
                                                    tint = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                                if (submitted) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "💡 ব্যাখ্যা: ${q.explanationBn}",
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.padding(8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    if (!submitted) {
                        Button(
                            onClick = onSubmit,
                            enabled = selectedAnswers.isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(containerColor = accent),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .minimumInteractiveComponentSize()
                                .testTag("smart_quiz_submit_button")
                        ) {
                            Text("উত্তর জমা দাও (${selectedAnswers.size}/${quiz.questions.size})")
                        }
                    } else {
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(containerColor = SageOffline),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .minimumInteractiveComponentSize()
                        ) {
                            Text("চ্যাটে ফিরে যাও")
                        }
                    }
                }
            }
        }
    }
}

/**
 * Interactive Smart Memorizer Dialog launched directly from "এই টপিকের বড় প্রশ্ন মুখস্থ করুন":
 * - High-probability WBBSE 3/4/5/8-mark Broad Question
 * - Bengali Memory Hook / Mnemonic Card
 * - Must-Write Board Keywords Chips
 * - Active Recall Mode (Hide answers & tap each step to self-test recall!)
 * - TTS Listen button to hear and memorize aloud
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SmartMemorizerDialog(
    deck: SmartMemorizerDeck,
    activeRecallHidden: Boolean,
    revealedSteps: Set<Int>,
    isSpeakingMemorizer: Boolean,
    onToggleActiveRecall: () -> Unit,
    onToggleRevealStep: (Int) -> Unit,
    onSpeakMemorizer: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val accent = deck.subject?.accentColor ?: TerracottaAccent
    val fullSpeakText = buildString {
        append("${deck.broadQuestionBn}. ")
        append("মনে রাখার কৌশল: ${deck.memoryHookBn}. ")
        deck.points.forEach { p ->
            append("পয়েন্ট ${p.stepNumber}, ${p.headingBn}: ${p.bodyBn}. ")
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.background,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.90f)
                .testTag("smart_memorizer_dialog")
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header
                Surface(
                    color = accent.copy(alpha = 0.13f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AutoStories,
                                contentDescription = null,
                                tint = accent,
                                modifier = Modifier.size(24.dp)
                            )
                            Column {
                                Text(
                                    text = "🧠 স্মার্ট মেমোরাইজার (${deck.targetMarks} নম্বর)",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = accent,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = deck.sourceLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(Icons.Rounded.Close, contentDescription = "Close memorizer")
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Broad Question Prompt Card
                    item(key = "broad_q_card") {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.2.dp, accent.copy(alpha = 0.45f))
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "📌 মাধ্যমিক সম্ভাব্য বড় প্রশ্ন (${deck.targetMarks} নম্বর)",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = accent,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = deck.broadQuestionBn,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                // Listen Aloud + Active Recall Mode Buttons
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    FilledTonalButton(
                                        onClick = onToggleActiveRecall,
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .minimumInteractiveComponentSize()
                                            .testTag("memorizer_recall_toggle")
                                    ) {
                                        Icon(
                                            imageVector = if (activeRecallHidden) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (activeRecallHidden) "সব উত্তর দেখাও" else "লুকিয়ে মুখস্থ ঝালাই",
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    }

                                    FilledTonalButton(
                                        onClick = { onSpeakMemorizer(fullSpeakText) },
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                        modifier = Modifier
                                            .minimumInteractiveComponentSize()
                                            .testTag("memorizer_tts_button")
                                    ) {
                                        Icon(
                                            imageVector = if (isSpeakingMemorizer) Icons.Rounded.Stop else Icons.AutoMirrored.Rounded.VolumeUp,
                                            contentDescription = "Listen to memorizer",
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (isSpeakingMemorizer) "থামাও" else "শোনো",
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Bengali Memory Hook / Mnemonic Banner
                    item(key = "memory_hook") {
                        Surface(
                            color = OnlineCobalt.copy(alpha = 0.10f),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, OnlineCobalt.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Psychology,
                                        contentDescription = null,
                                        tint = OnlineCobalt,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "🎯 মনে রাখার জাদু-কৌশল (Memory Hook)",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = OnlineCobalt,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = deck.memoryHookBn,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // Must-Write Board Keywords
                    if (deck.keywords.isNotEmpty()) {
                        item(key = "keywords_block") {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "🔑 খাতায় অবশ্যই লিখতে হবে (Board Keywords):",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold
                                )
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    deck.keywords.forEach { kw ->
                                        Surface(
                                            color = SageOffline.copy(alpha = 0.14f),
                                            shape = RoundedCornerShape(999.dp),
                                            border = BorderStroke(1.dp, SageOffline.copy(alpha = 0.4f))
                                        ) {
                                            Text(
                                                text = kw,
                                                style = MaterialTheme.typography.labelMedium,
                                                color = SageOffline,
                                                fontWeight = FontWeight.SemiBold,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Step-by-Step Memorization Point Cards (with Active Recall Tap-to-Reveal)
                    itemsIndexed(deck.points, key = { _, p -> p.stepNumber }) { _, point ->
                        val isVisible = !activeRecallHidden || (point.stepNumber in revealedSteps)
                        MemorizerStepCard(
                            point = point,
                            accent = accent,
                            isBodyVisible = isVisible,
                            onToggleReveal = { onToggleRevealStep(point.stepNumber) }
                        )
                    }

                    // Board Exam Presentation Tip
                    item(key = "board_tip") {
                        Surface(
                            color = TerracottaAccent.copy(alpha = 0.10f),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, TerracottaAccent.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Lightbulb,
                                    contentDescription = null,
                                    tint = TerracottaAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "🏆 ফুল মার্কস টিপস: ${deck.examTipBn}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = accent),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                    ) {
                        Text("মুখস্থ সম্পন্ন — চ্যাটে ফিরে যাও")
                    }
                }
            }
        }
    }
}

@Composable
private fun MemorizerStepCard(
    point: MemorizerPointCard,
    accent: Color,
    isBodyVisible: Boolean,
    onToggleReveal: () -> Unit
) {
    Card(
        onClick = onToggleReveal,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, if (isBodyVisible) accent.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ধাপ ${point.stepNumber} • ${point.headingBn}",
                    style = MaterialTheme.typography.titleSmall,
                    color = accent,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    color = accent.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (isBodyVisible) "খোলা" else "ট্যাপ করে দেখো",
                        style = MaterialTheme.typography.labelSmall,
                        color = accent,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            if (isBodyVisible) {
                Text(
                    text = point.bodyBn,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .padding(10.dp)
                ) {
                    Text(
                        text = "🕵️ মনে করার ইঙ্গিত (Hint): ${point.recallCueBn}\n(মনে মনে বলে উত্তর মেলাতে এখানে ট্যাপ করো)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
