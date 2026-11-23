package com.example.ui.topper

import android.net.Uri
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.example.data.topper.TopperQuestionBank
import com.example.model.InferenceMode
import com.example.model.SubjectMasteryStatus
import com.example.model.TimingBucket
import com.example.model.TopperSectionType
import com.example.model.TopperSubjectTrack
import com.example.ui.theme.MonoFontFamily
import com.example.ui.theme.OnlineCobalt
import com.example.ui.theme.SageOffline
import com.example.ui.theme.TerracottaAccent
import com.example.viewmodel.TopperUiState

/**
 * Flagship "টপার (TOPPER — AI মাস্টার সাজেশন ও মাধ্যমিক মক টেস্ট)" Screen.
 * Routes seamlessly between:
 *  - Active Exam Hall Test Runner (TopperTestRunner)
 *  - Evaluation Report Card (TopperReportCard)
 *  - Topper Mastery Dashboard (8 Individual Subject Tracks + Twisted Challenge + 15-Min Capsule + Mistake Recovery)
 */
@Composable
fun TopperScreen(
    topperState: TopperUiState,
    inferenceMode: InferenceMode,
    onSelectTrack: (TopperSubjectTrack) -> Unit,
    onToggleTwistedChallenge: (Boolean) -> Unit,
    onToggleFullBlueprint: (Boolean) -> Unit,
    onStartDailySet: (TopperSubjectTrack) -> Unit,
    onStart15MinCapsule: (TopperSubjectTrack) -> Unit,
    onStartMistakeRecovery: (TopperSubjectTrack?) -> Unit,
    onSelectTimingBucket: (TimingBucket) -> Unit,
    onToggleTimerPause: () -> Unit,
    onExitActivePaper: () -> Unit,
    onSelectMcqOption: (String, Int) -> Unit,
    onUpdateTypedAnswer: (String, TopperSectionType, String) -> Unit,
    onAddPhotoUri: (String, TopperSectionType, Uri) -> Unit,
    onRemovePhoto: (String, Int) -> Unit,
    onSubmitPaper: () -> Unit,
    onShowSnackbar: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 760.dp)
        ) {
            when {
                topperState.activePaper != null -> {
                    TopperTestRunner(
                        topperState = topperState,
                        paper = topperState.activePaper,
                        inferenceMode = inferenceMode,
                        onSelectTimingBucket = onSelectTimingBucket,
                        onToggleTimerPause = onToggleTimerPause,
                        onExitPaper = onExitActivePaper,
                        onSelectMcqOption = onSelectMcqOption,
                        onUpdateTypedAnswer = onUpdateTypedAnswer,
                        onAddPhotoUri = onAddPhotoUri,
                        onRemovePhoto = onRemovePhoto,
                        onSubmitPaper = onSubmitPaper,
                        onShowSnackbar = onShowSnackbar
                    )
                }

                topperState.latestReport != null -> {
                    val report = topperState.latestReport
                    TopperReportCard(
                        report = report,
                        onBackToDashboard = onExitActivePaper,
                        onStartNextDailySet = { onStartDailySet(report.track) },
                        onStartMistakeRecovery = { onStartMistakeRecovery(report.track) }
                    )
                }

                else -> {
                    TopperMasteryDashboard(
                        topperState = topperState,
                        inferenceMode = inferenceMode,
                        onSelectTrack = onSelectTrack,
                        onToggleTwistedChallenge = onToggleTwistedChallenge,
                        onToggleFullBlueprint = onToggleFullBlueprint,
                        onStartDailySet = onStartDailySet,
                        onStart15MinCapsule = onStart15MinCapsule,
                        onStartMistakeRecovery = onStartMistakeRecovery
                    )
                }
            }
        }
    }
}

@Composable
private fun TopperMasteryDashboard(
    topperState: TopperUiState,
    inferenceMode: InferenceMode,
    onSelectTrack: (TopperSubjectTrack) -> Unit,
    onToggleTwistedChallenge: (Boolean) -> Unit,
    onToggleFullBlueprint: (Boolean) -> Unit,
    onStartDailySet: (TopperSubjectTrack) -> Unit,
    onStart15MinCapsule: (TopperSubjectTrack) -> Unit,
    onStartMistakeRecovery: (TopperSubjectTrack?) -> Unit
) {
    val masteryList = topperState.masteryList.ifEmpty {
        TopperSubjectTrack.entries.map { track ->
            SubjectMasteryStatus(
                track = track,
                completedSetsCount = 0,
                totalSetsCount = track.totalRotationalSets,
                coveredPercent = 0,
                remainingSetsCount = track.totalRotationalSets,
                pendingMistakesCount = 0
            )
        }
    }

    val overallMasteryPct = if (masteryList.isNotEmpty()) {
        masteryList.sumOf { it.coveredPercent } / masteryList.size
    } else 0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("topper_dashboard_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Flagship TOPPER Hero Banner
        item(key = "topper_hero_card") {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                border = BorderStroke(1.2.dp, OnlineCobalt.copy(alpha = 0.4f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF0E2A47),
                                    Color(0xFF164E63),
                                    Color(0xFF1E3A5F)
                                )
                            )
                        )
                        .padding(18.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    color = Color(0xFFFFD54F).copy(alpha = 0.22f),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Rounded.WorkspacePremium,
                                            contentDescription = "TOPPER",
                                            tint = Color(0xFFFFD54F),
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                                Column {
                                    Text(
                                        text = "টপার (TOPPER)",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "AI মাস্টার সাজেশন ও মাধ্যমিক মক টেস্ট",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color.White.copy(alpha = 0.85f)
                                    )
                                }
                            }

                            Surface(
                                color = if (inferenceMode == InferenceMode.ONLINE) {
                                    Color(0xFF38BDF8).copy(alpha = 0.24f)
                                } else {
                                    Color(0xFF34D399).copy(alpha = 0.24f)
                                },
                                shape = RoundedCornerShape(999.dp)
                            ) {
                                Text(
                                    text = if (inferenceMode == InferenceMode.ONLINE) "🌐 অনলাইন AI" else "⚡ অফলাইন",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Text(
                            text = "প্রতিদিন নতুন অ-পুনরাবৃত্ত (Zero-Repeat) বোর্ড সাজেশন সেট, ৩০–৪০% ঘোরানো প্রশ্ন + বাংলা প্রশ্ন ডিকোডার, এবং খাতার ছবি বা টাইপ করে মাধ্যমিক মূল্যায়ন।",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.9f)
                        )

                        // Overall Syllabus Mastery Bar
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "সামগ্রিক মাধ্যমিক সিলেবাস কভারেজ (৮টি ট্র্যাক)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                                Text(
                                    text = "$overallMasteryPct% সম্পন্ন",
                                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = MonoFontFamily),
                                    color = Color(0xFFFFD54F),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            LinearProgressIndicator(
                                progress = { (overallMasteryPct / 100f).coerceIn(0f, 1f) },
                                color = Color(0xFFFFD54F),
                                trackColor = Color.White.copy(alpha = 0.2f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(7.dp)
                                    .clip(RoundedCornerShape(999.dp))
                            )
                        }
                    }
                }
            }
        }

        // 2. Topper Control Switches (Twisted Question Mode + Full 90-Mark Blueprint)
        item(key = "topper_mode_controls") {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Psychology,
                                contentDescription = null,
                                tint = TerracottaAccent,
                                modifier = Modifier.size(22.dp)
                            )
                            Column {
                                Text(
                                    text = "ঘোরানো প্রশ্ন চ্যালেঞ্জ (Twisted Mode)",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "৭৫%+ ঘুরিয়ে আসা বোর্ড প্রশ্ন + বাংলা প্রশ্ন ডিকোডার",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = topperState.twistedChallengeMode,
                            onCheckedChange = onToggleTwistedChallenge,
                            colors = SwitchDefaults.colors(checkedTrackColor = TerracottaAccent),
                            modifier = Modifier.testTag("topper_twisted_switch")
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.EmojiEvents,
                                contentDescription = null,
                                tint = OnlineCobalt,
                                modifier = Modifier.size(22.dp)
                            )
                            Column {
                                Text(
                                    text = "পূর্ণ ৯০ নম্বরের বোর্ড ব্লুপ্রিন্ট (৩ ঘণ্টা ১৫ মি.)",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "বন্ধ থাকলে দ্রুত ৪৫ মিনিটের ডেইলি টপার সেট তৈরি হবে",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = topperState.full90MarkBlueprint,
                            onCheckedChange = onToggleFullBlueprint,
                            colors = SwitchDefaults.colors(checkedTrackColor = OnlineCobalt),
                            modifier = Modifier.testTag("topper_full90_switch")
                        )
                    }
                }
            }
        }

        // 3. Quick Topper Masterstrokes (15-Min Offline Capsule + Mistake Recovery Test)
        item(key = "topper_masterstrokes_row") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 15-Minute Offline Capsule
                Card(
                    onClick = { onStart15MinCapsule(topperState.selectedTrack) },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SageOffline.copy(alpha = 0.12f)),
                    border = BorderStroke(1.dp, SageOffline.copy(alpha = 0.45f)),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("topper_15min_capsule_card")
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Bolt,
                                contentDescription = null,
                                tint = SageOffline,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "১৫-মিনিট ক্যাপসুল",
                                style = MaterialTheme.typography.labelLarge,
                                color = SageOffline,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "অফলাইন দ্রুত রিভিশন (${topperState.selectedTrack.linkedSubject.bengaliName})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Auto Mistake Recovery Test
                Card(
                    onClick = { onStartMistakeRecovery(null) },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = TerracottaAccent.copy(alpha = 0.12f)),
                    border = BorderStroke(1.dp, TerracottaAccent.copy(alpha = 0.45f)),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("topper_mistake_recovery_card")
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
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Refresh,
                                    contentDescription = null,
                                    tint = TerracottaAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "ভুলের খাতা",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = TerracottaAccent,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Surface(
                                color = TerracottaAccent,
                                shape = RoundedCornerShape(999.dp)
                            ) {
                                Text(
                                    text = "${topperState.totalActiveMistakesCount}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = if (topperState.totalActiveMistakesCount > 0) {
                                "${topperState.totalActiveMistakesCount}টি ভুল প্রশ্ন থেকে রিকভারি টেস্ট দাও"
                            } else {
                                "ভুল করলে এখানে স্বয়ংক্রিয়ভাবে জমা হবে"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Generating Paper Progress Banner
        if (topperState.isGeneratingPaper) {
            item(key = "topper_generating_banner") {
                Surface(
                    color = OnlineCobalt.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, OnlineCobalt.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(
                            strokeWidth = 2.5.dp,
                            color = OnlineCobalt,
                            modifier = Modifier.size(22.dp)
                        )
                        Column {
                            Text(
                                text = "আজকের নতুন Zero-Repeat টপার প্রশ্নপত্র তৈরি হচ্ছে…",
                                style = MaterialTheme.typography.labelLarge,
                                color = OnlineCobalt,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${topperState.selectedTrack.bengaliName} • বিগত বছরের প্যাটার্ন + ঘোরানো প্রশ্ন বাছাই চলছে",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // 4. Live Syllabus Mastery Meter Header
        item(key = "mastery_section_header") {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "বিষয়ভিত্তিক লাইভ সিলেবাস মাস্টারি মিটার (৮টি ট্র্যাক)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "যেকোনো বিষয়ের ডেইলি রোটেশনাল সেট শুরু করো — কোনো প্রশ্ন রিপিট হবে না।",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 5. All 8 Individual Subject Mastery Cards
        items(masteryList, key = { it.track.id }) { status ->
            SubjectMasteryTrackCard(
                status = status,
                isSelected = status.track == topperState.selectedTrack,
                isBusy = topperState.isGeneratingPaper,
                onSelect = { onSelectTrack(status.track) },
                onStartDailySet = { onStartDailySet(status.track) },
                onStartCapsule = { onStart15MinCapsule(status.track) }
            )
        }

        item { Spacer(modifier = Modifier.height(12.dp)) }
    }
}

@Composable
private fun SubjectMasteryTrackCard(
    status: SubjectMasteryStatus,
    isSelected: Boolean,
    isBusy: Boolean,
    onSelect: () -> Unit,
    onStartDailySet: () -> Unit,
    onStartCapsule: () -> Unit
) {
    val track = status.track
    val pyqSummary = TopperQuestionBank.getPreviousYearTrendSummary(track)
    val nextSetNum = status.completedSetsCount + 1

    Card(
        onClick = onSelect,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            width = if (isSelected) 1.8.dp else 1.dp,
            color = if (isSelected) track.accentColor else MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("topper_track_${track.id}")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Top Track Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    color = track.accentColor.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = track.icon,
                            contentDescription = track.englishName,
                            tint = track.accentColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = track.bengaliName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = track.blueprintSummaryBn,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Surface(
                    color = track.accentColor.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "${status.coveredPercent}%",
                        style = MaterialTheme.typography.labelLarge.copy(fontFamily = MonoFontFamily),
                        color = track.accentColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                    )
                }
            }

            // Live Syllabus Mastery Progress Bar + Remaining Sets Copy
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                LinearProgressIndicator(
                    progress = { (status.coveredPercent / 100f).coerceIn(0f, 1f) },
                    color = track.accentColor,
                    trackColor = track.accentColor.copy(alpha = 0.16f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(999.dp))
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${status.coveredPercent}% কভার হয়েছে (${status.completedSetsCount}/${status.totalSetsCount} সেট)",
                        style = MaterialTheme.typography.labelSmall,
                        color = track.accentColor,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (status.remainingSetsCount > 0) {
                            "আর মাত্র ${status.remainingSetsCount}টি টপার সেট বাকি!"
                        } else {
                            "🎉 সম্পূর্ণ সিলেবাস মাস্টারি সম্পন্ন!"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // PYQ Board Trend Insight
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = pyqSummary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                )
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onStartDailySet,
                    enabled = !isBusy,
                    colors = ButtonDefaults.buttonColors(containerColor = track.accentColor),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    modifier = Modifier
                        .weight(1.35f)
                        .minimumInteractiveComponentSize()
                        .testTag("topper_start_${track.id}")
                ) {
                    Icon(Icons.Rounded.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "টপার সেট $nextSetNum শুরু করো",
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                FilledTonalButton(
                    onClick = onStartCapsule,
                    enabled = !isBusy,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                    modifier = Modifier
                        .weight(0.85f)
                        .minimumInteractiveComponentSize()
                        .testTag("topper_capsule_${track.id}")
                ) {
                    Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "১৫ মি.",
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
