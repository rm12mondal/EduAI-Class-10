package com.example.ui.hub

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountTree
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MicOff
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.speech.PodcastUiState
import com.example.data.speech.SpeechRecognitionUiState
import com.example.data.speech.VoiceInputLanguage
import com.example.ui.theme.OnlineCobalt
import com.example.ui.theme.SageOffline
import com.example.ui.theme.TerracottaAccent
import kotlin.math.roundToInt

/**
 * Sleek AssistiveTouch-style Draggable Floating "D" Action Hub.
 * - Can be dragged smoothly anywhere on the screen and auto-snaps cleanly to the nearest screen edge/corner.
 * - Tapping "D" opens a compact, scrollable quick-access menu housing:
 *   1. Voice Input (bn-IN / en-US offline-compatible SpeechRecognizer with RECORD_AUDIO handling)
 *   2. Exact Textbook Definition Toggle (শুধুমাত্র পাঠ্যবইয়ের সংজ্ঞা)
 *   3. Visual Mind-Map Modal launcher for the latest AI response
 *   4. Compact Offline Background Audio Podcast Revision Player (Play/Pause, Stop, 1x / 1.25x, lock-screen support)
 *   5. 1-Tap Core App Feature Shortcuts (Smart Learning & 7 Subjects, Brahmastram & Exam, Grammar Lab, PDF Export, Settings)
 */
@Composable
fun FloatingDActionHub(
    speechState: SpeechRecognitionUiState,
    exactTextbookMode: Boolean,
    podcastState: PodcastUiState,
    hasLatestAnswerForMindMap: Boolean,
    onSelectVoiceLanguage: (VoiceInputLanguage) -> Unit,
    onToggleVoiceListening: () -> Unit,
    onToggleExactTextbookMode: () -> Unit,
    onOpenMindMap: () -> Unit,
    onTogglePodcastPlayPause: () -> Unit,
    onStopPodcast: () -> Unit,
    onSetPodcastSpeed: (Float) -> Unit,
    onOpenFullPodcastPlayer: () -> Unit,
    onShortcutSmartLearning: () -> Unit,
    onShortcutBrahmastram: () -> Unit,
    onShortcutGrammarLab: () -> Unit,
    onShortcutExportPdf: () -> Unit,
    onShortcutSettings: () -> Unit,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val density = LocalDensity.current

    var isMenuOpen by remember { mutableStateOf(false) }
    var isDragging by remember { mutableStateOf(false) }

    // Runtime RECORD_AUDIO permission launcher for Voice Input
    val recordAudioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            onToggleVoiceListening()
        } else {
            onShowMessage("ভয়েস ইনপুটের জন্য মাইক্রোফোন (RECORD_AUDIO) অনুমতি প্রয়োজন।")
        }
    }

    // Optional Android 13+ notification permission for lock-screen podcast media notification
    val notifPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        onTogglePodcastPlayPause()
    }

    fun triggerVoiceWithPermission() {
        val hasMic = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        if (hasMic) {
            onToggleVoiceListening()
        } else {
            recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    fun triggerPodcastWithNotificationCheck() {
        if (!podcastState.isPlaying && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasNotif = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!hasNotif) {
                notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                return
            }
        }
        onTogglePodcastPlayPause()
    }

    // Subtle breathing pulse when Mic is listening or Podcast is playing
    val isLiveActivity = speechState.isListening || podcastState.isPlaying
    val pulseTransition = rememberInfiniteTransition(label = "d_hub_pulse")
    val pulseScale by pulseTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isLiveActivity) 1.12f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    BoxWithConstraints(
        modifier = modifier.fillMaxSize()
    ) {
        val fabSizeDp = 56.dp
        val edgePaddingDp = 12.dp
        val topSafeDp = 76.dp
        val bottomSafeDp = 96.dp

        val maxWpx = with(density) { maxWidth.toPx() }
        val maxHpx = with(density) { maxHeight.toPx() }
        val fabPx = with(density) { fabSizeDp.toPx() }
        val padPx = with(density) { edgePaddingDp.toPx() }
        val topMinY = with(density) { topSafeDp.toPx() }
        val bottomMaxY = (maxHpx - fabPx - with(density) { bottomSafeDp.toPx() }).coerceAtLeast(topMinY)
        val rightMaxX = (maxWpx - fabPx - padPx).coerceAtLeast(padPx)

        // Default resting anchor: right edge, comfortably above the bottom composer / nav bar
        var targetOffset by remember { mutableStateOf<Offset?>(null) }
        val resolvedTarget = targetOffset ?: Offset(
            x = rightMaxX,
            y = (bottomMaxY * 0.72f).coerceIn(topMinY, bottomMaxY)
        )

        // Clamp when screen rotates / resizes
        LaunchedEffect(rightMaxX, bottomMaxY) {
            targetOffset?.let { current ->
                targetOffset = Offset(
                    x = current.x.coerceIn(padPx, rightMaxX),
                    y = current.y.coerceIn(topMinY, bottomMaxY)
                )
            }
        }

        val animatedOffset by animateOffsetAsState(
            targetValue = resolvedTarget,
            animationSpec = if (isDragging) {
                spring(stiffness = Spring.StiffnessHigh)
            } else {
                spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            },
            label = "d_fab_snap_offset"
        )

        // 1. Scrim + Compact Popup Menu when open
        AnimatedVisibility(
            visible = isMenuOpen,
            enter = fadeIn(tween(180)),
            exit = fadeOut(tween(150))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.42f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { isMenuOpen = false }
                    )
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                AnimatedVisibility(
                    visible = isMenuOpen,
                    enter = scaleIn(initialScale = 0.90f) + fadeIn(),
                    exit = scaleOut(targetScale = 0.92f) + fadeOut()
                ) {
                    DHubPopupMenuCard(
                        speechState = speechState,
                        exactTextbookMode = exactTextbookMode,
                        podcastState = podcastState,
                        hasLatestAnswerForMindMap = hasLatestAnswerForMindMap,
                        onClose = { isMenuOpen = false },
                        onSelectVoiceLanguage = onSelectVoiceLanguage,
                        onTriggerVoice = { triggerVoiceWithPermission() },
                        onToggleExactTextbookMode = onToggleExactTextbookMode,
                        onOpenMindMap = {
                            isMenuOpen = false
                            onOpenMindMap()
                        },
                        onTogglePodcastPlayPause = { triggerPodcastWithNotificationCheck() },
                        onStopPodcast = onStopPodcast,
                        onSetPodcastSpeed = onSetPodcastSpeed,
                        onOpenFullPodcastPlayer = {
                            isMenuOpen = false
                            onOpenFullPodcastPlayer()
                        },
                        onShortcutSmartLearning = {
                            isMenuOpen = false
                            onShortcutSmartLearning()
                        },
                        onShortcutBrahmastram = {
                            isMenuOpen = false
                            onShortcutBrahmastram()
                        },
                        onShortcutGrammarLab = {
                            isMenuOpen = false
                            onShortcutGrammarLab()
                        },
                        onShortcutExportPdf = {
                            isMenuOpen = false
                            onShortcutExportPdf()
                        },
                        onShortcutSettings = {
                            isMenuOpen = false
                            onShortcutSettings()
                        }
                    )
                }
            }
        }

        // 2. Draggable AssistiveTouch-Style Circular "D" Floating Button
        val activeRingColor = when {
            speechState.isListening -> Color(0xFFDC2626)
            podcastState.isPlaying -> SageOffline
            exactTextbookMode -> Color(0xFFD97706)
            else -> OnlineCobalt
        }

        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        x = animatedOffset.x.roundToInt(),
                        y = animatedOffset.y.roundToInt()
                    )
                }
                .size(fabSizeDp)
                .scale(if (isDragging) 1.08f else pulseScale)
                .alpha(if (isDragging || isMenuOpen || isLiveActivity) 1f else 0.90f)
                .pointerInput(rightMaxX, topMinY, bottomMaxY) {
                    detectDragGestures(
                        onDragStart = {
                            isDragging = true
                        },
                        onDragEnd = {
                            isDragging = false
                            val cur = targetOffset ?: resolvedTarget
                            // Snap cleanly to the nearest screen edge (left, right, top, or bottom)
                            val distLeft = cur.x - padPx
                            val distRight = rightMaxX - cur.x
                            val distTop = cur.y - topMinY
                            val distBottom = bottomMaxY - cur.y
                            val minEdge = minOf(distLeft, distRight, distTop, distBottom)

                            val snapped = when {
                                minEdge == distTop && distTop < 72f -> Offset(
                                    x = cur.x.coerceIn(padPx, rightMaxX),
                                    y = topMinY
                                )
                                minEdge == distBottom && distBottom < 72f -> Offset(
                                    x = cur.x.coerceIn(padPx, rightMaxX),
                                    y = bottomMaxY
                                )
                                distLeft <= distRight -> Offset(
                                    x = padPx,
                                    y = cur.y.coerceIn(topMinY, bottomMaxY)
                                )
                                else -> Offset(
                                    x = rightMaxX,
                                    y = cur.y.coerceIn(topMinY, bottomMaxY)
                                )
                            }
                            targetOffset = snapped
                        },
                        onDragCancel = {
                            isDragging = false
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val current = targetOffset ?: resolvedTarget
                            targetOffset = Offset(
                                x = (current.x + dragAmount.x).coerceIn(padPx, rightMaxX),
                                y = (current.y + dragAmount.y).coerceIn(topMinY, bottomMaxY)
                            )
                        }
                    )
                }
                .shadow(elevation = 10.dp, shape = CircleShape, clip = false)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF0F172A),
                            activeRingColor
                        )
                    )
                )
                .clickable { isMenuOpen = !isMenuOpen }
                .testTag("floating_d_hub_button"),
            contentAlignment = Alignment.Center
        ) {
            // Sleek inner glass ring (AssistiveTouch aesthetic)
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.28f),
                                    activeRingColor.copy(alpha = 0.85f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "D",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 21.sp,
                            letterSpacing = 0.5.sp
                        ),
                        color = Color.White
                    )
                }
            }

            // Tiny live dot badge when Voice or Podcast is active
            if (isLiveActivity || exactTextbookMode) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(11.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                speechState.isListening -> Color(0xFFEF4444)
                                podcastState.isPlaying -> Color(0xFF22C55E)
                                else -> Color(0xFFF59E0B)
                            }
                        )
                )
            }
        }
    }
}

@Composable
private fun DHubPopupMenuCard(
    speechState: SpeechRecognitionUiState,
    exactTextbookMode: Boolean,
    podcastState: PodcastUiState,
    hasLatestAnswerForMindMap: Boolean,
    onClose: () -> Unit,
    onSelectVoiceLanguage: (VoiceInputLanguage) -> Unit,
    onTriggerVoice: () -> Unit,
    onToggleExactTextbookMode: () -> Unit,
    onOpenMindMap: () -> Unit,
    onTogglePodcastPlayPause: () -> Unit,
    onStopPodcast: () -> Unit,
    onSetPodcastSpeed: (Float) -> Unit,
    onOpenFullPodcastPlayer: () -> Unit,
    onShortcutSmartLearning: () -> Unit,
    onShortcutBrahmastram: () -> Unit,
    onShortcutGrammarLab: () -> Unit,
    onShortcutExportPdf: () -> Unit,
    onShortcutSettings: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        shadowElevation = 14.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 350.dp)
            .heightIn(max = 560.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            )
            .testTag("floating_d_hub_menu")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Popup Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                OnlineCobalt.copy(alpha = 0.12f),
                                SageOffline.copy(alpha = 0.10f)
                            )
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(OnlineCobalt),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "D",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                    Column {
                        Text(
                            text = "EduAI কুইক অ্যাকশন হাব",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "স্মার্ট টুলস ও ১-ট্যাপ শর্টকাট",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .size(38.dp)
                        .testTag("d_hub_close_btn")
                ) {
                    Icon(Icons.Rounded.Close, contentDescription = "Close D Hub")
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Scrollable Tools & Shortcuts
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // SECTION 1: NEW SMART STUDY TOOLS
                Text(
                    text = "স্মার্ট স্টাডি টুলস",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Tool 1: Voice Input (bn-IN & en-US SpeechRecognizer)
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (speechState.isListening) {
                        Color(0xFFDC2626).copy(alpha = 0.10f)
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerLow
                    },
                    border = BorderStroke(
                        1.dp,
                        if (speechState.isListening) Color(0xFFDC2626) else MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
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
                                    imageVector = if (speechState.isListening) Icons.Rounded.GraphicEq else Icons.Rounded.Mic,
                                    contentDescription = null,
                                    tint = if (speechState.isListening) Color(0xFFDC2626) else OnlineCobalt,
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(
                                        text = "ভয়েস ইনপুট (Mic)",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "সরাসরি চ্যাট বক্সে প্রশ্ন টাইপ হবে",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            FilledTonalButton(
                                onClick = onTriggerVoice,
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("d_hub_voice_mic_btn")
                            ) {
                                Icon(
                                    imageVector = if (speechState.isListening) Icons.Rounded.MicOff else Icons.Rounded.Mic,
                                    contentDescription = "Voice Input",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (speechState.isListening) "থামাও" else "বলো",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }

                        // Language Switcher: bn-IN / en-US
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            VoiceInputLanguage.entries.forEach { lang ->
                                val selected = speechState.language == lang
                                Surface(
                                    onClick = { onSelectVoiceLanguage(lang) },
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (selected) OnlineCobalt else MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(
                                        1.dp,
                                        if (selected) OnlineCobalt else MaterialTheme.colorScheme.outlineVariant
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("d_hub_voice_lang_${lang.localeTag}")
                                ) {
                                    Text(
                                        text = lang.labelBn,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        if (speechState.partialText.isNotBlank() || !speechState.statusMessage.isNullOrBlank()) {
                            Text(
                                text = speechState.partialText.ifBlank { speechState.statusMessage.orEmpty() },
                                style = MaterialTheme.typography.bodySmall,
                                color = if (speechState.isListening) Color(0xFFDC2626) else SageOffline,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Tool 2: Exact Textbook Definition Toggle
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (exactTextbookMode) {
                        Color(0xFFD97706).copy(alpha = 0.12f)
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerLow
                    },
                    border = BorderStroke(
                        1.dp,
                        if (exactTextbookMode) Color(0xFFD97706) else MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onToggleExactTextbookMode() }
                        .testTag("d_hub_exact_textbook_toggle")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.MenuBook,
                            contentDescription = null,
                            tint = if (exactTextbookMode) Color(0xFFD97706) else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(21.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "শুধুমাত্র পাঠ্যবইয়ের সংজ্ঞা",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (exactTextbookMode) {
                                    "চালু: হুবহু WBBSE সংজ্ঞা + SI/CGS মানক একক"
                                } else {
                                    "বন্ধ: সহজ ধাপে ধাপে ব্যাখ্যা মোড"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = exactTextbookMode,
                            onCheckedChange = { onToggleExactTextbookMode() }
                        )
                    }
                }

                // Tool 3: Visual Mind-Map Button
                Surface(
                    onClick = onOpenMindMap,
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .testTag("d_hub_mindmap_btn")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AccountTree,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(21.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "ভিজ্যুয়াল মাইন্ড-ম্যাপ (Flowchart)",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (hasLatestAnswerForMindMap) {
                                    "সর্বশেষ AI উত্তরের ট্রি/ফ্লোচার্ট দেখো"
                                } else {
                                    "সিলেবাস নোট থেকে তাৎক্ষণিক ফ্লোচার্ট দেখো"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.secondary
                        ) {
                            Text(
                                text = "খুলুন",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondary,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Tool 4: Offline Background Audio Podcast Revision Player
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (podcastState.isPlaying) {
                        SageOffline.copy(alpha = 0.12f)
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerLow
                    },
                    border = BorderStroke(
                        1.dp,
                        if (podcastState.isPlaying) SageOffline else MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
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
                                Icon(
                                    imageVector = Icons.Rounded.Headphones,
                                    contentDescription = null,
                                    tint = SageOffline,
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(
                                        text = "পডকাস্ট শুনুন (ব্যাকগ্রাউন্ড)",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = podcastState.currentTitle.ifBlank {
                                            "অফলাইন TTS • স্ক্রিন লক থাকলেও চলবে"
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            TextButton(
                                onClick = onOpenFullPodcastPlayer,
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.testTag("d_hub_full_podcast_btn")
                            ) {
                                Text("প্লেলিস্ট", style = MaterialTheme.typography.labelSmall)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Play / Pause Button
                            FilledTonalButton(
                                onClick = onTogglePodcastPlayPause,
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("d_hub_podcast_play_pause_btn")
                            ) {
                                Icon(
                                    imageVector = if (podcastState.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                    contentDescription = if (podcastState.isPlaying) "Pause Podcast" else "Play Podcast",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (podcastState.isPlaying) "বিরতি" else "শোনো",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }

                            if (podcastState.isPlaying || podcastState.isPaused) {
                                IconButton(
                                    onClick = onStopPodcast,
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .size(38.dp)
                                        .testTag("d_hub_podcast_stop_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Stop,
                                        contentDescription = "Stop Podcast",
                                        tint = TerracottaAccent
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            // Speed Chips: 1x and 1.25x
                            listOf(1.0f to "1x", 1.25f to "1.25x").forEach { (speedVal, label) ->
                                val selected = kotlin.math.abs(podcastState.speed - speedVal) < 0.05f
                                Surface(
                                    onClick = { onSetPodcastSpeed(speedVal) },
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (selected) SageOffline else MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(
                                        1.dp,
                                        if (selected) SageOffline else MaterialTheme.colorScheme.outlineVariant
                                    ),
                                    modifier = Modifier.testTag("d_hub_podcast_speed_$label")
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.padding(vertical = 2.dp)
                )

                // SECTION 2: CORE APP FEATURE SHORTCUTS (1-TAP ACCESS)
                Text(
                    text = "১-ট্যাপ দ্রুত শর্টকাট",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                DHubShortcutRow(
                    icon = Icons.Rounded.AutoAwesome,
                    tint = OnlineCobalt,
                    title = "স্মার্ট লার্নিং ও ৭টি বিষয়",
                    subtitle = "AI টিউটর ও ১০৩ অধ্যায়ের সম্পূর্ণ সিলেবাস",
                    tag = "d_hub_shortcut_smart_learning",
                    onClick = onShortcutSmartLearning
                )

                DHubShortcutRow(
                    icon = Icons.Rounded.Bolt,
                    tint = Color(0xFFD97706),
                    title = "ব্রহ্মাস্ত্রম ও পরীক্ষা",
                    subtitle = "নিশ্চিত সাজেশন নোটস ও টাইমড বোর্ড মক টেস্ট",
                    tag = "d_hub_shortcut_brahmastram",
                    onClick = onShortcutBrahmastram
                )

                DHubShortcutRow(
                    icon = Icons.Rounded.Translate,
                    tint = Color(0xFF7C3AED),
                    title = "ব্যাকরণ ও গ্রামার ল্যাব",
                    subtitle = "বাংলা কারক-সমাস-বাচ্য ও English Grammar",
                    tag = "d_hub_shortcut_grammar_lab",
                    onClick = onShortcutGrammarLab
                )

                DHubShortcutRow(
                    icon = Icons.Rounded.PictureAsPdf,
                    tint = Color(0xFFC62828),
                    title = "PDF সেভ করুন",
                    subtitle = "চ্যাট ও নোটস অফলাইনে কাস্টম নামে PDF বানাও",
                    tag = "d_hub_shortcut_pdf_export",
                    onClick = onShortcutExportPdf
                )

                DHubShortcutRow(
                    icon = Icons.Rounded.Settings,
                    tint = SageOffline,
                    title = "অফলাইন মডেল ও সেটিংস",
                    subtitle = "GGUF Llama মডেল, ভয়েস ও প্রোগ্রেস ব্যাকআপ",
                    tag = "d_hub_shortcut_settings",
                    onClick = onShortcutSettings
                )
            }
        }
    }
}

@Composable
private fun DHubShortcutRow(
    icon: ImageVector,
    tint: Color,
    title: String,
    subtitle: String,
    tag: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)),
        modifier = Modifier
            .fillMaxWidth()
            .minimumInteractiveComponentSize()
            .testTag(tag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = tint.copy(alpha = 0.14f),
                modifier = Modifier.size(34.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
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
}
