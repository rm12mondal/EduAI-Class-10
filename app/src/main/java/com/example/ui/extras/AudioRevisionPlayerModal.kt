package com.example.ui.extras

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.AudioRevisionUiState
import com.example.ui.theme.MonoFontFamily

/**
 * Eye-Rest Friendly "হ্যান্ডস-ফ্রি অডিও রিভিশন (Voice Tutor Podcast)" full-screen player.
 * Continuously reads chapter summaries, VVI Q&A, and formulas aloud in Bengali (`bn-IN`)
 * and English (`en-US`) offline with Play/Pause, Next/Previous, Auto-Next, and 0.8x/1.0x/1.25x speed.
 */
@Composable
fun AudioRevisionPlayerModal(
    state: AudioRevisionUiState,
    onTogglePlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSelectTrack: (Int) -> Unit,
    onSelectSpeed: (Float) -> Unit,
    onToggleAutoAdvance: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onClose() }

    val track = state.currentTrack
    val progress = if (state.tracks.isEmpty()) 0f else {
        (state.currentIndex + 1).toFloat() / state.tracks.size.toFloat()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0B132B),
                        Color(0xFF1C2541),
                        Color(0xFF0F172A)
                    )
                )
            ),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Top Header
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
                        color = Color(0xFF38BDF8).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.Headphones,
                                contentDescription = "Audio Revision",
                                tint = Color(0xFF7DD3FC)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "হ্যান্ডস-ফ্রি অডিও রিভিশন",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = state.playlistSubtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.72f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("audio_revision_close_btn")
                ) {
                    Icon(Icons.Rounded.Close, contentDescription = "Close Audio Revision", tint = Color.White)
                }
            }

            // Now Playing Eye-Rest Card
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
                border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = Color(0xFF38BDF8).copy(alpha = 0.22f),
                            shape = RoundedCornerShape(999.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.GraphicEq,
                                    contentDescription = null,
                                    tint = Color(0xFF7DD3FC),
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = track?.categoryLabel ?: "অডিও পডকাস্ট",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFBAE6FD)
                                )
                            }
                        }

                        Text(
                            text = "টপিক ${state.currentIndex + 1} / ${state.tracks.size}",
                            style = MaterialTheme.typography.labelMedium.copy(fontFamily = MonoFontFamily),
                            color = Color(0xFFFFD54F),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = track?.title ?: state.playlistTitle,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Text(
                        text = track?.displayPreview.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f),
                        maxLines = 5,
                        overflow = TextOverflow.Ellipsis
                    )

                    LinearProgressIndicator(
                        progress = { progress },
                        color = Color(0xFF38BDF8),
                        trackColor = Color.White.copy(alpha = 0.16f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(999.dp))
                    )

                    // Primary Transport Controls (Prev / Play-Pause / Next)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onPrevious,
                            enabled = state.currentIndex > 0,
                            modifier = Modifier
                                .size(52.dp)
                                .testTag("audio_prev_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.SkipPrevious,
                                contentDescription = "Previous topic",
                                tint = if (state.currentIndex > 0) Color.White else Color.White.copy(alpha = 0.35f),
                                modifier = Modifier.size(34.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(20.dp))

                        Surface(
                            onClick = onTogglePlayPause,
                            shape = CircleShape,
                            color = Color(0xFF38BDF8),
                            modifier = Modifier
                                .size(64.dp)
                                .testTag("audio_play_pause_btn")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (state.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                    contentDescription = if (state.isPlaying) "Pause" else "Play",
                                    tint = Color(0xFF0B132B),
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(20.dp))

                        IconButton(
                            onClick = onNext,
                            enabled = state.currentIndex < state.tracks.lastIndex,
                            modifier = Modifier
                                .size(52.dp)
                                .testTag("audio_next_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.SkipNext,
                                contentDescription = "Next topic",
                                tint = if (state.currentIndex < state.tracks.lastIndex) Color.White else Color.White.copy(alpha = 0.35f),
                                modifier = Modifier.size(34.dp)
                            )
                        }
                    }

                    // Speed Selector (0.8x, 1.0x, 1.25x) + Auto-Next Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(0.8f to "0.8x", 1.0f to "1.0x", 1.25f to "1.25x").forEach { (speedVal, label) ->
                                val selected = kotlin.math.abs(state.playbackSpeed - speedVal) < 0.05f
                                FilterChip(
                                    selected = selected,
                                    onClick = { onSelectSpeed(speedVal) },
                                    label = {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelMedium.copy(fontFamily = MonoFontFamily),
                                            fontWeight = FontWeight.Bold
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        containerColor = Color.White.copy(alpha = 0.08f),
                                        labelColor = Color.White.copy(alpha = 0.85f),
                                        selectedContainerColor = Color(0xFF38BDF8),
                                        selectedLabelColor = Color(0xFF0B132B)
                                    ),
                                    modifier = Modifier.testTag("audio_speed_$label")
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "অটো-নেক্সট",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Switch(
                                checked = state.autoAdvance,
                                onCheckedChange = { onToggleAutoAdvance() },
                                colors = SwitchDefaults.colors(
                                    checkedTrackColor = Color(0xFF38BDF8),
                                    checkedThumbColor = Color(0xFF0B132B)
                                )
                            )
                        }
                    }
                }
            }

            // Playlist Queue
            Text(
                text = "অডিও প্লেলিস্ট (${state.tracks.size}টি টপিক) — যেকোনো টপিকে ট্যাপ করো",
                style = MaterialTheme.typography.labelLarge,
                color = Color.White.copy(alpha = 0.85f),
                fontWeight = FontWeight.SemiBold
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                itemsIndexed(state.tracks, key = { _, item -> item.id }) { idx, item ->
                    val isCurrent = idx == state.currentIndex
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isCurrent) Color(0xFF38BDF8).copy(alpha = 0.22f)
                        else Color.White.copy(alpha = 0.06f),
                        border = BorderStroke(
                            1.dp,
                            if (isCurrent) Color(0xFF38BDF8) else Color.White.copy(alpha = 0.12f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectTrack(idx) }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "${idx + 1}",
                                style = MaterialTheme.typography.labelLarge.copy(fontFamily = MonoFontFamily),
                                fontWeight = FontWeight.Bold,
                                color = if (isCurrent) Color(0xFF7DD3FC) else Color.White.copy(alpha = 0.6f)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = item.categoryLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.65f)
                                )
                            }
                            if (isCurrent && state.isPlaying) {
                                Icon(
                                    imageVector = Icons.Rounded.GraphicEq,
                                    contentDescription = "Playing",
                                    tint = Color(0xFF7DD3FC),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
