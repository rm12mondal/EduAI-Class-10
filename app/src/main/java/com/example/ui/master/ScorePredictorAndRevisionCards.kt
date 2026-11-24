package com.example.ui.master

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoGraph
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.HistoryEdu
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.SpacedRevisionEntity
import com.example.data.master.MathUnicodeFormatter
import com.example.data.master.StrictWbbsePrecisionEngine
import com.example.data.master.WbbseSyllabusGuard
import com.example.model.ScorePredictorSummary
import com.example.model.SpacedRevisionUiState
import com.example.model.WbbseSubject

/**
 * Feature #7: "সম্ভাব্য নম্বর মিটার (Score Predictor /700)"
 * Projects total Madhyamik score out of 700 across all 7 WBBSE subjects and highlights the 2 weakest subjects.
 */
@Composable
fun ScorePredictorCard(
    summary: ScorePredictorSummary,
    onFocusSubject: (WbbseSubject) -> Unit,
    compact: Boolean = false,
    modifier: Modifier = Modifier
) {
    var expandedSubjects by remember { mutableStateOf(!compact) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("score_predictor_card"),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.28f)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.34f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AutoGraph,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "সম্ভাব্য মাধ্যমিক নম্বর মিটার",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = summary.gradeLabelBn,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Text(
                        text = "${summary.predictedTotalOutOf700} / 700",
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            LinearProgressIndicator(
                progress = { (summary.predictedTotalOutOf700 / 700f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp),
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            // 2 Weakest Subjects Highlight Strip
            if (summary.twoWeakestSubjects.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.WarningAmber,
                        contentDescription = null,
                        tint = Color(0xFFD84315),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "ফোকাস দরকার:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD84315)
                    )
                    summary.twoWeakestSubjects.forEach { weak ->
                        AssistChip(
                            onClick = { onFocusSubject(weak.subject) },
                            label = {
                                Text(
                                    text = "${weak.subject.bengaliName} (${weak.predictedOutOf100})",
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1
                                )
                            }
                        )
                    }
                }
            }

            if (compact) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expandedSubjects = !expandedSubjects },
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (expandedSubjects) "৭টি বিষয় লুকান" else "৭টি বিষয়ের নম্বর দেখুন",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Icon(
                        imageVector = if (expandedSubjects) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            AnimatedVisibility(visible = expandedSubjects) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    summary.subjectScores.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = item.subject.bengaliName,
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.width(92.dp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            LinearProgressIndicator(
                                progress = { item.predictedOutOf100 / 100f },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(6.dp)
                            )
                            Text(
                                text = "${item.predictedOutOf100}/100",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(48.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Feature #9: "আজকের রিভিশন ক্যাপসুল (Spaced Repetition Auto Card on Home)"
 * Surfaces topics & mistakes due at 3-day, 7-day, and 21-day intervals stored in Room DB.
 */
@Composable
fun TodayRevisionCapsuleCard(
    state: SpacedRevisionUiState,
    onMarkReviewed: (SpacedRevisionEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    if (state.dueNowItems.isEmpty() && state.totalTrackedCount == 0) return

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("today_revision_capsule_card"),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.30f)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.36f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.HistoryEdu,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "আজকের রিভিশন ক্যাপসুল",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "৩-দিন • ৭-দিন • ২১-দিন স্পেসড রিপিটিশন",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.tertiary
                ) {
                    Text(
                        text = if (state.dueNowItems.isNotEmpty()) "${state.dueNowItems.size}টি বাকি" else "সব সম্পন্ন ✓",
                        color = MaterialTheme.colorScheme.onTertiary,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            if (state.dueNowItems.isEmpty()) {
                Text(
                    text = "✅ আজকের সব রিভিশন সম্পন্ন! পরবর্তী শিডিউলে ${state.upcomingCount}টি টপিক আছে।",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                state.dueNowItems.take(3).forEach { item ->
                    val stageBadge = when (item.masteryStage) {
                        0 -> "৩-দিন ধাপ"
                        1 -> "৭-দিন ধাপ"
                        else -> "২১-দিন ধাপ"
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${if (item.sourceType == "MISTAKE") "🔴 ভুল সংশোধন" else "📘 রিভিশন"} • $stageBadge",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (item.sourceType == "MISTAKE") Color(0xFFC62828) else MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = item.topicTitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = item.keySummary,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            TextButton(
                                onClick = { onMarkReviewed(item) },
                                modifier = Modifier.testTag("revision_done_${item.id}")
                            ) {
                                Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("পড়া শেষ", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * WBBSE Class 10 Subject / Chapter Reference Badge rendered automatically on every AI response card.
 */
@Composable
fun WbbseReferenceBadgeChip(
    answerText: String,
    subject: WbbseSubject?,
    chapterHint: String? = null,
    modifier: Modifier = Modifier
) {
    val badge = remember(answerText, subject, chapterHint) {
        WbbseSyllabusGuard.resolveBadge(
            answerText = answerText,
            subject = subject,
            chapterHint = chapterHint
        )
    }
    val bgColor = if (badge.isOutOfSyllabus) {
        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.78f)
    } else {
        (subject?.accentColor ?: MaterialTheme.colorScheme.primary).copy(alpha = 0.12f)
    }
    val fgColor = if (badge.isOutOfSyllabus) {
        MaterialTheme.colorScheme.onErrorContainer
    } else {
        subject?.accentColor ?: MaterialTheme.colorScheme.primary
    }

    Surface(
        shape = RoundedCornerShape(9.dp),
        color = bgColor,
        border = BorderStroke(0.8.dp, fgColor.copy(alpha = 0.30f)),
        modifier = modifier
    ) {
        Text(
            text = badge.labelBn,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = fgColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

/**
 * Feature #10: "টপারের খাতায় যেভাবে লিখবে"
 * Compact inline toggle on Chat & Smart Learning answers rendering structured
 * ভূমিকা + বোল্ড কীওয়ার্ডসহ পয়েন্ট + ডায়াগ্রাম/ছক + উপসংহার format.
 */
@Composable
fun TopperAnswerFormatToggleBlock(
    rawAnswer: String,
    subject: WbbseSubject?,
    marks: Int = 5
) {
    if (rawAnswer.length < 70 || rawAnswer.contains(WbbseSyllabusGuard.OUT_OF_SYLLABUS_REFUSAL_PREFIX)) return
    var showTopperSheet by remember(rawAnswer) { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = if (showTopperSheet) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier
                .padding(top = 6.dp)
                .clickable { showTopperSheet = !showTopperSheet }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.EmojiEvents,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = if (showTopperSheet) "টপারের খাতা লুকান" else "টপারের খাতায় যেভাবে লিখবে (বোর্ড ফরম্যাট)",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        AnimatedVisibility(visible = showTopperSheet) {
            val formatted = remember(rawAnswer, subject, marks) {
                MathUnicodeFormatter.format(
                    StrictWbbsePrecisionEngine.formatTopperAnswerSheet(rawAnswer, subject, marks)
                )
            }
            Surface(
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f)),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
            ) {
                Text(
                    text = formatted,
                    style = MaterialTheme.typography.bodySmall.copy(lineHeight = 19.sp),
                    modifier = Modifier.padding(10.dp)
                )
            }
        }
    }
}
