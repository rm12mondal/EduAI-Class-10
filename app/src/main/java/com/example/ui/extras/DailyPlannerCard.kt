package com.example.ui.extras

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.DailyPlanTaskEntity
import com.example.model.DailyPlannerUiState
import com.example.model.InferenceMode
import com.example.model.PlanActionType
import com.example.model.WbbseSubject
import com.example.ui.theme.MonoFontFamily
import com.example.ui.theme.OnlineCobalt
import com.example.ui.theme.SageOffline
import com.example.ui.theme.TerracottaAccent
import kotlin.math.roundToInt

/**
 * Interactive "দৈনিক রুটিন (Daily Plan) ও স্ট্রিক ট্র্যাকার" card shown on the Home Dashboard
 * and accessible anytime via the header Daily Plan sheet dialog.
 */
@Composable
fun DailyStudyPlannerCard(
    state: DailyPlannerUiState,
    inferenceMode: InferenceMode,
    onSelectHoursAndGenerate: (Int) -> Unit,
    onToggleTask: (DailyPlanTaskEntity) -> Unit,
    onLaunchTaskAction: (DailyPlanTaskEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val pct = (state.progressFraction * 100f).roundToInt()

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.2.dp, OnlineCobalt.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("daily_study_planner_card")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Gradient Header with Streak Flame Badge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF143154),
                                Color(0xFF1F4E79),
                                Color(0xFF0E6251)
                            )
                        )
                    )
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                color = Color.White.copy(alpha = 0.16f),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Rounded.CalendarToday,
                                        contentDescription = "Daily Study Plan",
                                        tint = Color(0xFFFFD54F),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "দৈনিক রুটিন (AI Daily Plan)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = state.sourceBadge,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.82f)
                                )
                            }
                        }

                        // Daily Study Streak Badge
                        Surface(
                            color = Color(0xFFFF6D00).copy(alpha = 0.25f),
                            shape = RoundedCornerShape(999.dp),
                            border = BorderStroke(1.dp, Color(0xFFFFB74D)),
                            modifier = Modifier.testTag("daily_streak_badge")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.LocalFireDepartment,
                                    contentDescription = "Study Streak",
                                    tint = Color(0xFFFFCA28),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "${state.streak.currentStreak} দিন স্ট্রিক",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    // Live Daily Progress Bar
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "আজকের টার্গেট: ${state.completedCount}/${state.totalCount} সম্পন্ন" +
                                    if (state.streak.longestStreak > 0) " • সেরা: ${state.streak.longestStreak} দিন" else "",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                            Text(
                                text = "$pct%",
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = MonoFontFamily),
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFD54F)
                            )
                        }
                        LinearProgressIndicator(
                            progress = { state.progressFraction },
                            color = Color(0xFF4ADE80),
                            trackColor = Color.White.copy(alpha = 0.22f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(7.dp)
                                .clip(RoundedCornerShape(999.dp))
                        )
                    }
                }
            }

            // Hour Picker + Regenerate Row
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "আজ কত ঘণ্টা পড়বে?",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    TextButton(
                        onClick = { onSelectHoursAndGenerate(state.selectedHours) },
                        enabled = !state.isGenerating,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.testTag("regenerate_daily_plan_btn")
                    ) {
                        if (state.isGenerating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = if (inferenceMode == InferenceMode.ONLINE) Icons.Rounded.AutoAwesome else Icons.Rounded.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (inferenceMode == InferenceMode.ONLINE) "AI নতুন রুটিন" else "নতুন রুটিন",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(2, 4, 6).forEach { hrs ->
                        val selected = state.selectedHours == hrs
                        FilterChip(
                            selected = selected,
                            onClick = { onSelectHoursAndGenerate(hrs) },
                            enabled = !state.isGenerating,
                            label = {
                                Text(
                                    text = "${hrs} ঘণ্টা",
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = OnlineCobalt.copy(alpha = 0.16f),
                                selectedLabelColor = OnlineCobalt
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .minimumInteractiveComponentSize()
                                .testTag("daily_hours_chip_$hrs")
                        )
                    }
                }

                // Task Checklist
                if (state.tasks.isEmpty()) {
                    Text(
                        text = "তোমার দৈনিক রুটিন তৈরি হচ্ছে…",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        state.tasks.forEach { task ->
                            DailyTaskChecklistItem(
                                task = task,
                                onToggle = { onToggleTask(task) },
                                onLaunch = { onLaunchTaskAction(task) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyTaskChecklistItem(
    task: DailyPlanTaskEntity,
    onToggle: () -> Unit,
    onLaunch: () -> Unit
) {
    val subject = WbbseSubject.fromId(task.subjectId) ?: WbbseSubject.PHYSICAL_SCIENCE
    val actionLabel = when (task.actionType) {
        PlanActionType.SMART_LEARNING -> "স্মার্ট লার্নিং"
        PlanActionType.MAP_BOARD -> "ম্যাপ বোর্ড"
        PlanActionType.RAPID_FIRE -> "র্যাপিড ফায়ার"
        PlanActionType.TOPPER_CAPSULE -> "টপার ক্যাপসুল"
        else -> "অধ্যায় নোট"
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (task.isCompleted) {
            SageOffline.copy(alpha = 0.10f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        },
        border = BorderStroke(
            width = 1.dp,
            color = if (task.isCompleted) SageOffline.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("daily_plan_task_${task.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(start = 4.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(checkedColor = SageOffline),
                modifier = Modifier.minimumInteractiveComponentSize()
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        color = subject.accentColor.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = subject.bengaliName,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = subject.accentColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                        )
                    }
                    Text(
                        text = "${task.durationMinutes} মি.",
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = MonoFontFamily),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = task.titleBn,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = task.detailBn,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Surface(
                onClick = onLaunch,
                shape = RoundedCornerShape(10.dp),
                color = TerracottaAccent.copy(alpha = 0.12f),
                modifier = Modifier
                    .padding(start = 6.dp)
                    .minimumInteractiveComponentSize()
                    .testTag("launch_daily_task_${task.id}")
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = actionLabel,
                        tint = TerracottaAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = actionLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = TerracottaAccent,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

/**
 * Full-screen/Dialog wrapper for accessing Daily Plan anytime from the Chat Header pill
 * even when chat messages are visible.
 */
@Composable
fun DailyPlannerSheetDialog(
    state: DailyPlannerUiState,
    inferenceMode: InferenceMode,
    onDismiss: () -> Unit,
    onSelectHoursAndGenerate: (Int) -> Unit,
    onToggleTask: (DailyPlanTaskEntity) -> Unit,
    onLaunchTaskAction: (DailyPlanTaskEntity) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .heightIn(max = 680.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "দৈনিক স্মার্ট স্টাডি রুটিন",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close Daily Plan")
                    }
                }
                LazyColumn(
                    contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 16.dp)
                ) {
                    item {
                        DailyStudyPlannerCard(
                            state = state,
                            inferenceMode = inferenceMode,
                            onSelectHoursAndGenerate = onSelectHoursAndGenerate,
                            onToggleTask = onToggleTask,
                            onLaunchTaskAction = { task ->
                                onDismiss()
                                onLaunchTaskAction(task)
                            }
                        )
                    }
                }
            }
        }
    }
}
