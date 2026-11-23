package com.example.ui.topper

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.TopperQuestionFeedback
import com.example.model.TopperSubmissionReport
import com.example.ui.theme.MonoFontFamily
import com.example.ui.theme.OnlineCobalt
import com.example.ui.theme.SageOffline
import com.example.ui.theme.TerracottaAccent
import java.util.Locale

/**
 * Flagship TOPPER Evaluation Report Card:
 * - Total Score ("প্রাপ্ত মোট নম্বর") + Madhyamik Readiness %
 * - "তুমি কতটা ভালো করেছ" strengths, missing keywords, and actionable advice
 * - Section-Wise Speed & Time Analytics (MCQ / SAQ / Broad + 3h 15m pacing guidance)
 * - Per-Question Review with Bengali "🔍 প্রশ্ন ডিকোডার (Question Decoder)",
 *   "🏆 টপার টিপস (খাতা সাজানোর কৌশল)", and Verified Board Model Answer
 */
@Composable
fun TopperReportCard(
    report: TopperSubmissionReport,
    onBackToDashboard: () -> Unit,
    onStartNextDailySet: () -> Unit,
    onStartMistakeRecovery: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onBackToDashboard()
    }

    val scoreAccent = when {
        report.readinessPercent >= 80 -> SageOffline
        report.readinessPercent >= 55 -> OnlineCobalt
        else -> TerracottaAccent
    }
    val formattedScore = if (report.obtainedMarks % 1f == 0f) {
        "${report.obtainedMarks.toInt()} / ${report.totalMarks.toInt()}"
    } else {
        String.format(Locale.US, "%.1f / %d", report.obtainedMarks, report.totalMarks.toInt())
    }
    val mistakesInThisPaper = report.questionFeedbacks.count { !it.isCorrectOrStrong }

    Column(modifier = modifier.fillMaxSize()) {
        // Top Header Bar
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
                    onClick = onBackToDashboard,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("topper_report_back")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back to Topper dashboard"
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "টপার মূল্যায়ন রিপোর্ট",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = report.paperTitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .testTag("topper_report_list"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Score Hero Banner ("প্রাপ্ত মোট নম্বর" + "তুমি কতটা ভালো করেছ")
            item(key = "score_hero") {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    border = BorderStroke(1.5.dp, scoreAccent.copy(alpha = 0.55f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        scoreAccent.copy(alpha = 0.18f),
                                        MaterialTheme.colorScheme.surface
                                    )
                                )
                            )
                            .padding(16.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.EmojiEvents,
                                        contentDescription = null,
                                        tint = scoreAccent,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "প্রাপ্ত মোট নম্বর",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = formattedScore,
                                            style = MaterialTheme.typography.headlineSmall.copy(fontFamily = MonoFontFamily),
                                            fontWeight = FontWeight.Bold,
                                            color = scoreAccent
                                        )
                                    }
                                }

                                Surface(
                                    color = scoreAccent.copy(alpha = 0.16f),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "${report.readinessPercent}%",
                                            style = MaterialTheme.typography.titleLarge.copy(fontFamily = MonoFontFamily),
                                            color = scoreAccent,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "বোর্ড প্রস্তুতি",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = scoreAccent
                                        )
                                    }
                                }
                            }

                            LinearProgressIndicator(
                                progress = { (report.readinessPercent / 100f).coerceIn(0f, 1f) },
                                color = scoreAccent,
                                trackColor = scoreAccent.copy(alpha = 0.18f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(999.dp))
                            )

                            Text(
                                text = "তুমি কতটা ভালো করেছ: ${report.verdictTitle}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Text(
                                text = "মূল্যায়নকারী: ${report.evaluatorSource}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Offline Handwritten Sheet Notice (when user attached photos while offline)
            report.offlinePhotoWarning?.let { warning ->
                item(key = "offline_photo_warning") {
                    Surface(
                        color = TerracottaAccent.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, TerracottaAccent.copy(alpha = 0.45f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.WarningAmber,
                                contentDescription = null,
                                tint = TerracottaAccent,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = warning,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // 2. Detailed Performance Summary Card
            item(key = "performance_breakdown") {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ReportInsightRow(title = "✅ তোমার শক্তির জায়গা", body = report.strengths)
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        ReportInsightRow(title = "⚠️ ভুলত্রুটি ও বাদ পড়া মূল শব্দ (Keywords)", body = report.mistakesAndMissingKeywords)
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        ReportInsightRow(title = "🎯 ৯০%+ পাওয়ার পরবর্তী পদক্ষেপ", body = report.improvementAdvice)
                    }
                }
            }

            // 3. Section-Wise Speed & Time Analytics Card
            item(key = "time_analytics") {
                val tb = report.timeBreakdown
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, OnlineCobalt.copy(alpha = 0.35f))
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
                                imageVector = Icons.Rounded.Speed,
                                contentDescription = null,
                                tint = OnlineCobalt,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "বিভাগভিত্তিক সময় ও গতির বিশ্লেষণ (৩ ঘণ্টা ১৫ মিনিট স্ট্র্যাটেজি)",
                                style = MaterialTheme.typography.titleSmall,
                                color = OnlineCobalt,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TimeMetricChip(label = "MCQ", seconds = tb.mcqSeconds, modifier = Modifier.weight(1f))
                            TimeMetricChip(label = "VSAQ/SAQ", seconds = tb.saqSeconds, modifier = Modifier.weight(1f))
                            TimeMetricChip(label = "বড় প্রশ্ন", seconds = tb.broadSeconds, modifier = Modifier.weight(1f))
                        }

                        Text(
                            text = report.pacingAdviceBn,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // 4. Question-by-Question Review Header
            item(key = "review_header") {
                Text(
                    text = "প্রশ্নভিত্তিক মূল্যায়ন, প্রশ্ন ডিকোডার ও টপার টিপস (${report.questionFeedbacks.size}টি)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // 5. Per-Question Feedback Cards
            itemsIndexed(report.questionFeedbacks, key = { idx, fb -> "${fb.question.id}_$idx" }) { idx, fb ->
                TopperQuestionFeedbackCard(index = idx + 1, feedback = fb)
            }

            // 6. Footer Action Buttons (Mistake Recovery + Next Rotational Set)
            item(key = "report_footer_actions") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (mistakesInThisPaper > 0) {
                        Button(
                            onClick = onStartMistakeRecovery,
                            colors = ButtonDefaults.buttonColors(containerColor = TerracottaAccent),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .minimumInteractiveComponentSize()
                                .testTag("topper_report_recovery_btn")
                        ) {
                            Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ভুলের খাতা থেকে রিকভারি টেস্ট দাও (${mistakesInThisPaper}টি ভুল সেভ হয়েছে)")
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onBackToDashboard,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .minimumInteractiveComponentSize()
                        ) {
                            Text("ড্যাশবোর্ডে ফিরো")
                        }
                        Button(
                            onClick = onStartNextDailySet,
                            colors = ButtonDefaults.buttonColors(containerColor = report.track.accentColor),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .minimumInteractiveComponentSize()
                                .testTag("topper_report_next_set_btn")
                        ) {
                            Text("পরবর্তী সেট শুরু করো")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun ReportInsightRow(title: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun TimeMetricChip(
    label: String,
    seconds: Long,
    modifier: Modifier = Modifier
) {
    val mins = seconds / 60L
    val remSec = seconds % 60L
    val formatted = if (mins > 0) "${mins}মি ${remSec}সে" else "${remSec} সেকেন্ড"
    Surface(
        color = OnlineCobalt.copy(alpha = 0.09f),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = formatted,
                style = MaterialTheme.typography.labelLarge.copy(fontFamily = MonoFontFamily),
                color = OnlineCobalt,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun TopperQuestionFeedbackCard(
    index: Int,
    feedback: TopperQuestionFeedback
) {
    val q = feedback.question
    val statusColor = if (feedback.isCorrectOrStrong) SageOffline else TerracottaAccent
    val scoreText = if (feedback.marksAwarded % 1f == 0f) {
        "${feedback.marksAwarded.toInt()}/${feedback.maxMarks.toInt()}"
    } else {
        String.format(Locale.US, "%.1f/%d", feedback.marksAwarded, feedback.maxMarks.toInt())
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.45f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = if (feedback.isCorrectOrStrong) Icons.Rounded.CheckCircle else Icons.Rounded.ErrorOutline,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "প্রশ্ন $index • বিভাগ ${q.section.code}",
                        style = MaterialTheme.typography.labelMedium,
                        color = statusColor,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    color = statusColor.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "$scoreText নম্বর",
                        style = MaterialTheme.typography.labelMedium.copy(fontFamily = MonoFontFamily),
                        color = statusColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            // Question Text
            Text(
                text = q.questionText,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Student's submitted response + examiner remark
            Text(
                text = "তোমার উত্তর: ${feedback.studentSummary}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = feedback.feedbackNote,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Bengali Question Decoder Box (for Twisted / Indirect Questions)
            feedback.decoderNoteBn?.takeIf { it.isNotBlank() }?.let { decoder ->
                Surface(
                    color = TerracottaAccent.copy(alpha = 0.10f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, TerracottaAccent.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Psychology,
                                contentDescription = null,
                                tint = TerracottaAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "🔍 প্রশ্ন ডিকোডার (Question Decoder)",
                                style = MaterialTheme.typography.labelMedium,
                                color = TerracottaAccent,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = decoder,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Topper's Presentation Tip Box ("খাতা সাজানোর কৌশল")
            if (feedback.presentationTip.isNotBlank()) {
                Surface(
                    color = OnlineCobalt.copy(alpha = 0.09f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, OnlineCobalt.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Lightbulb,
                                contentDescription = null,
                                tint = OnlineCobalt,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "🏆 টপার টিপস (খাতা সাজানোর কৌশল)",
                                style = MaterialTheme.typography.labelMedium,
                                color = OnlineCobalt,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = feedback.presentationTip,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Verified Board Model Answer
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Verified,
                            contentDescription = null,
                            tint = SageOffline,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "📘 বোর্ড আদর্শ উত্তর:",
                            style = MaterialTheme.typography.labelMedium,
                            color = SageOffline,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = feedback.modelAnswer,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
