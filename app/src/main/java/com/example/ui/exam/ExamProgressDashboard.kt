package com.example.ui.exam

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.rounded.Assessment
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.ExamAttemptEntity
import com.example.model.ExamFormat
import com.example.model.OverallProgressStats
import com.example.model.SubjectProgressSummary
import com.example.model.WbbseSubject
import com.example.ui.theme.MonoFontFamily
import com.example.ui.theme.OnlineCobalt
import com.example.ui.theme.SageOffline
import com.example.ui.theme.TerracottaAccent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ExamProgressDashboard(
    stats: OverallProgressStats,
    attempts: List<ExamAttemptEntity>,
    onPracticeSubject: (WbbseSubject) -> Unit,
    onClearHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("exam_progress_dashboard"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Top KPI Metric Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricStatCard(
                    title = "মোট পরীক্ষা",
                    value = "${stats.totalTestsTaken}",
                    subtitle = "Room-এ সংরক্ষিত",
                    accent = MaterialTheme.colorScheme.primary,
                    icon = Icons.Rounded.Assessment,
                    modifier = Modifier.weight(1f)
                )
                MetricStatCard(
                    title = "গড় নির্ভুলতা",
                    value = "${stats.overallAccuracyPercent}%",
                    subtitle = "${formatMarks(stats.totalMarksObtained)}/${formatMarks(stats.totalMarksPossible)} নম্বর",
                    accent = if (stats.overallAccuracyPercent >= 65) SageOffline else TerracottaAccent,
                    icon = Icons.Rounded.TrendingUp,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 2. Weak Areas Needing Practice
        if (stats.weakSubjects.isNotEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = TerracottaAccent.copy(alpha = 0.09f)
                    ),
                    border = BorderStroke(1.dp, TerracottaAccent.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("weak_areas_card")
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.WarningAmber,
                                contentDescription = null,
                                tint = TerracottaAccent
                            )
                            Text(
                                text = if (stats.totalTestsTaken == 0) {
                                    "অনুশীলন শুরু করো (প্রস্তাবিত বিষয়)"
                                } else {
                                    "দুর্বল বিষয় — আরও অনুশীলন প্রয়োজন"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                color = TerracottaAccent
                            )
                        }

                        stats.weakSubjects.forEach { summary ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${summary.subject.bengaliName} (${summary.subject.englishName})",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = if (summary.attemptsCount == 0) {
                                            "এখনো কোনো পরীক্ষা দেওয়া হয়নি"
                                        } else {
                                            "নির্ভুলতা: ${summary.accuracyPercent}% • ${summary.attemptsCount}টি পরীক্ষা"
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                FilledTonalButton(
                                    onClick = { onPracticeSubject(summary.subject) },
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.minimumInteractiveComponentSize()
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.PlayArrow,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("অনুশীলন", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Subject-Wise Accuracy & Marks Breakdown (All 7 WBBSE Subjects)
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.EmojiEvents,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "৭টি বিষয়ের পারফরম্যান্স বিশ্লেষণ",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    stats.subjectSummaries.forEach { summary ->
                        SubjectAccuracyBarRow(summary = summary)
                    }
                }
            }
        }

        // 4. Test History List
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "সাম্প্রতিক পরীক্ষার ইতিহাস (${attempts.size})",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                if (attempts.isNotEmpty()) {
                    OutlinedButton(
                        onClick = onClearHistory,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("clear_exam_history_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteSweep,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("মুছুন", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }

        if (attempts.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Assessment,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(34.dp)
                        )
                        Text(
                            text = "এখনো কোনো পরীক্ষার রেকর্ড নেই",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = "‘পরীক্ষা দাও’ ট্যাবে গিয়ে MCQ, SAQ বা বড় প্রশ্নের উত্তর জমা দাও—তোমার স্কোর ও বিশ্লেষণ এখানে সংরক্ষিত হবে।",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(attempts, key = { it.id }) { attempt ->
                ExamHistoryItemCard(attempt = attempt)
            }
        }
    }
}

@Composable
private fun MetricStatCard(
    title: String,
    value: String,
    subtitle: String,
    accent: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.28f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = accent,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SubjectAccuracyBarRow(summary: SubjectProgressSummary) {
    val progress = if (summary.attemptsCount > 0) summary.accuracyPercent / 100f else 0f
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = summary.subject.icon,
                    contentDescription = null,
                    tint = summary.subject.accentColor,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = summary.subject.bengaliName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(
                text = if (summary.attemptsCount == 0) {
                    "বাকি আছে"
                } else {
                    "${formatMarks(summary.totalObtained)}/${formatMarks(summary.totalPossible)} (${summary.accuracyPercent}%)"
                },
                style = MaterialTheme.typography.labelSmall.copy(fontFamily = MonoFontFamily),
                color = if (summary.attemptsCount == 0) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    summary.subject.accentColor
                }
            )
        }

        LinearProgressIndicator(
            progress = { progress },
            color = summary.subject.accentColor,
            trackColor = summary.subject.accentColor.copy(alpha = 0.14f),
            strokeCap = StrokeCap.Round,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
        )
    }
}

@Composable
private fun ExamHistoryItemCard(attempt: ExamAttemptEntity) {
    val subject = WbbseSubject.fromId(attempt.subjectId) ?: WbbseSubject.BENGALI
    val format = ExamFormat.fromId(attempt.examType)
    val dateLabel = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(attempt.timestamp))
    val scoreColor = when {
        attempt.accuracyPercent >= 75 -> SageOffline
        attempt.accuracyPercent >= 50 -> OnlineCobalt
        else -> TerracottaAccent
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        color = subject.accentColor.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = subject.bengaliName,
                            style = MaterialTheme.typography.labelSmall,
                            color = subject.accentColor,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = format.shortLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                }

                Surface(
                    color = scoreColor.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "${formatMarks(attempt.scoreObtained)}/${formatMarks(attempt.maxMarks)} • ${attempt.accuracyPercent}%",
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = MonoFontFamily),
                        color = scoreColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Text(
                text = attempt.questionSummary,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = attempt.feedbackSummary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = dateLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun formatMarks(marks: Float): String {
    return if (marks % 1f == 0f) marks.toInt().toString() else String.format(Locale.US, "%.1f", marks)
}
