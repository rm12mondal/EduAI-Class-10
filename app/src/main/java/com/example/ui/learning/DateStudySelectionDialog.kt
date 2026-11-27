package com.example.ui.learning

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
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
import com.example.model.AutoStudyPlan
import com.example.model.DateStudyDateUtils
import com.example.ui.theme.OnlineCobalt

/**
 * Single-step Madhyamik Exam Date Selection Dialog:
 * - Student only picks their "মাধ্যমিক পরীক্ষার তারিখ (Select Exam Date)" via Calendar or +30/+60/+90 day presets.
 * - Automatically computes `daysRemaining` from Today's device date and shows a live auto-paced plan preview badge:
 *   "⏳ পরীক্ষার আর X দিন বাকি • অটো প্ল্যান: [Selected Mode]"
 *   (>90d Smart Learning, 45–90d 2/3-Month Crash Course, 15–44d Fast Learning VVI, <15d Brahmastram & Mock Exam).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateStudySelectionDialog(
    initialExamDateEpochMs: Long,
    isChangingDate: Boolean,
    onDismiss: () -> Unit,
    onConfirmStart: (examDateEpochMs: Long) -> Unit
) {
    var pickedExamDateMs by remember(initialExamDateEpochMs) { mutableLongStateOf(initialExamDateEpochMs) }
    var showCalendarPicker by remember { mutableStateOf(false) }

    val pickedDateIso = remember(pickedExamDateMs) { DateStudyDateUtils.utcMillisToIso(pickedExamDateMs) }
    val readableExamDate = remember(pickedDateIso) { DateStudyDateUtils.formatExamDateReadable(pickedDateIso) }
    val daysRemaining = remember(pickedExamDateMs) { DateStudyDateUtils.computeDaysRemaining(pickedExamDateMs) }
    val autoPlan = remember(daysRemaining) { AutoStudyPlan.fromDaysRemaining(daysRemaining) }

    val plus30Ms = remember { DateStudyDateUtils.offsetDaysUtcMidnightMs(30) }
    val plus60Ms = remember { DateStudyDateUtils.offsetDaysUtcMidnightMs(60) }
    val plus90Ms = remember { DateStudyDateUtils.offsetDaysUtcMidnightMs(90) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            shadowElevation = 16.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp)
                .widthIn(max = 420.dp)
                .testTag("date_study_selection_dialog")
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // High-Contrast Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color(0xFF0F172A),
                                    autoPlan.accentColor
                                )
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CalendarMonth,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = if (isChangingDate) {
                                    "📅 পরীক্ষার তারিখ পরিবর্তন করুন"
                                } else {
                                    "▶️ তারিখ দিয়ে পড়া শুরু করুন"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "শুধু পরীক্ষার তারিখ দাও — বাকি দিন গুনে অটো প্ল্যান তৈরি হবে",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.90f),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .size(38.dp)
                            .testTag("date_study_dialog_close_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Single Clean Card: 📅 মাধ্যমিক পরীক্ষার তারিখ বেছে নাও (Select Exam Date)
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = OnlineCobalt.copy(alpha = 0.08f),
                        border = BorderStroke(1.2.dp, OnlineCobalt.copy(alpha = 0.38f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("exam_date_selection_card")
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "📅 মাধ্যমিক পরীক্ষার তারিখ বেছে নাও (Select Exam Date)",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "নির্বাচিত পরীক্ষার তারিখ",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = readableExamDate,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                FilledTonalButton(
                                    onClick = { showCalendarPicker = true },
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .testTag("date_study_open_calendar_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.DateRange,
                                        contentDescription = "Open Calendar",
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "ক্যালেন্ডার",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Quick Preset Buttons: +30 দিন, +60 দিন, +90 দিন
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val presets = listOf(
                                    Triple("+30 দিন", plus30Ms, "exam_preset_30"),
                                    Triple("+60 দিন", plus60Ms, "exam_preset_60"),
                                    Triple("+90 দিন", plus90Ms, "exam_preset_90")
                                )
                                presets.forEach { (label, targetMs, tag) ->
                                    val isSelected = pickedExamDateMs == targetMs
                                    Surface(
                                        onClick = { pickedExamDateMs = targetMs },
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) OnlineCobalt else MaterialTheme.colorScheme.surface,
                                        border = BorderStroke(
                                            1.dp,
                                            if (isSelected) OnlineCobalt else MaterialTheme.colorScheme.outlineVariant
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .minimumInteractiveComponentSize()
                                            .testTag(tag)
                                    ) {
                                        Box(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 9.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = label,
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Live Auto-Countdown & Auto-Selected Plan Preview Badge
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = autoPlan.accentColor.copy(alpha = 0.13f),
                        border = BorderStroke(1.4.dp, autoPlan.accentColor.copy(alpha = 0.55f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("exam_countdown_auto_plan_badge")
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.AutoAwesome,
                                    contentDescription = null,
                                    tint = autoPlan.accentColor,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "⏳ পরীক্ষার আর $daysRemaining দিন বাকি • অটো প্ল্যান: ${autoPlan.labelBn}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = autoPlan.descriptionBn,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    // Primary Start Now CTA Button
                    Button(
                        onClick = { onConfirmStart(pickedExamDateMs) },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = autoPlan.accentColor,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(vertical = 14.dp, horizontal = 16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                            .testTag("date_study_start_now_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.RocketLaunch,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isChangingDate) {
                                "🚀 নতুন তারিখ সেট করুন (Update Exam Date)"
                            } else {
                                "🚀 পড়া শুরু করুন (Start Now)"
                            },
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }

    // Native Material 3 Calendar DatePickerDialog
    if (showCalendarPicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = pickedExamDateMs
        )
        DatePickerDialog(
            onDismissRequest = { showCalendarPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { chosen ->
                            pickedExamDateMs = chosen
                        }
                        showCalendarPicker = false
                    },
                    modifier = Modifier.testTag("date_study_calendar_confirm_btn")
                ) {
                    Text("নিশ্চিত করুন", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCalendarPicker = false }) {
                    Text("বাতিল")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
