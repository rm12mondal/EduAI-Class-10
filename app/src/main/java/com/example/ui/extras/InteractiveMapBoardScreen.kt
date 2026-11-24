package com.example.ui.extras

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CenterFocusStrong
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.HighlightOff
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.map.MapPointingBank
import com.example.model.MapBoardDomain
import com.example.model.MapPointingLocation
import com.example.model.MapSymbolType
import com.example.ui.theme.MonoFontFamily
import com.example.ui.theme.OnlineCobalt
import com.example.ui.theme.SageOffline
import com.example.ui.theme.TerracottaAccent
import kotlin.math.hypot

/**
 * Interactive Zoomable Vector/Canvas Map Pointing Practice Board for WBBSE Class 10:
 * - Geography Map Pointing (10 Marks — 26 authentic locations)
 * - History Map Pointing (4 Marks — 18 authentic locations)
 * - Tap-to-Pin Quiz Mode with instant accuracy check + board-standard symbol reveal
 * - Study Atlas Mode to explore all 44 board locations offline
 */
@Composable
fun InteractiveMapBoardScreen(
    domain: MapBoardDomain,
    onSwitchDomain: (MapBoardDomain) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    // 0 = Practice Tap Quiz, 1 = Study Atlas (All Pins)
    var boardMode by remember { mutableIntStateOf(0) }
    val locations = remember(domain) { MapPointingBank.getByDomain(domain) }
    var shuffledQuiz by remember(domain) { mutableStateOf(locations.shuffled()) }
    var quizIndex by remember(domain) { mutableIntStateOf(0) }
    var userTapNorm by remember(domain, quizIndex) { mutableStateOf<Offset?>(null) }
    var showHint by remember(domain, quizIndex) { mutableStateOf(false) }
    var scoreCorrect by remember(domain) { mutableIntStateOf(0) }
    var scoreAttempted by remember(domain) { mutableIntStateOf(0) }
    var selectedAtlasLoc by remember(domain) { mutableStateOf<MapPointingLocation?>(locations.firstOrNull()) }

    // Zoom & Pan state
    var zoomScale by remember { mutableFloatStateOf(1f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }

    val currentTarget = shuffledQuiz.getOrNull(quizIndex)
    val tapEvaluation = remember(userTapNorm, currentTarget) {
        val tap = userTapNorm
        val target = currentTarget
        if (tap == null || target == null) null
        else {
            val dist = hypot(tap.x - target.normX, tap.y - target.normY)
            dist <= target.toleranceRadius
        }
    }

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
                            .testTag("map_board_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "ম্যাপ পয়েন্টিং প্র্যাকটিস বোর্ড",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "ইতিহাস (৪) + ভূগোল (১০) = ১৪ নম্বর নিশ্চিত • ৪৪টি বোর্ড লোকেশন",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Surface(
                        color = SageOffline.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(999.dp)
                    ) {
                        Text(
                            text = "স্কোর $scoreCorrect/$scoreAttempted",
                            style = MaterialTheme.typography.labelMedium.copy(fontFamily = MonoFontFamily),
                            fontWeight = FontWeight.Bold,
                            color = SageOffline,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Domain Switcher (Geography 10 Marks vs History 4 Marks)
                item {
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        MapBoardDomain.entries.forEachIndexed { idx, dom ->
                            SegmentedButton(
                                selected = domain == dom,
                                onClick = {
                                    onSwitchDomain(dom)
                                    zoomScale = 1f
                                    panOffset = Offset.Zero
                                },
                                shape = SegmentedButtonDefaults.itemShape(idx, MapBoardDomain.entries.size),
                                modifier = Modifier.testTag("map_domain_${dom.id}")
                            ) {
                                Text(
                                    text = "${dom.titleBn.substringBefore(" ")} (${dom.marksLabel})",
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                // Mode Switcher (Practice Pin Test vs Study Atlas)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = boardMode == 0,
                            onClick = { boardMode = 0 },
                            label = { Text("🎯 ট্যাপ প্র্যাকটিস টেস্ট") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = OnlineCobalt.copy(alpha = 0.16f),
                                selectedLabelColor = OnlineCobalt
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .minimumInteractiveComponentSize()
                                .testTag("map_mode_quiz")
                        )
                        FilterChip(
                            selected = boardMode == 1,
                            onClick = { boardMode = 1 },
                            label = { Text("🗺️ সব পিন অ্যাটলাস (${locations.size})") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SageOffline.copy(alpha = 0.16f),
                                selectedLabelColor = SageOffline
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .minimumInteractiveComponentSize()
                                .testTag("map_mode_atlas")
                        )
                    }
                }

                // Active Question Prompt Card (Quiz Mode)
                if (boardMode == 0 && currentTarget != null) {
                    item {
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.2.dp, OnlineCobalt.copy(alpha = 0.45f))
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "প্রশ্ন ${quizIndex + 1} / ${shuffledQuiz.size} • মানচিত্রে ট্যাপ করো:",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = OnlineCobalt,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Surface(
                                        color = TerracottaAccent.copy(alpha = 0.12f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = "${currentTarget.symbolType.symbolChar} ${currentTarget.symbolType.labelBn}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TerracottaAccent,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = currentTarget.nameBn,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                if (showHint) {
                                    Text(
                                        text = "💡 ইঙ্গিত: ${currentTarget.regionHintBn}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                } else if (userTapNorm == null) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "ভারতের মানচিত্রে সঠিক স্থানে আঙুল দিয়ে ট্যাপ করো।",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        FilledTonalButton(
                                            onClick = { showHint = true },
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                                        ) {
                                            Icon(Icons.Rounded.Lightbulb, contentDescription = null, modifier = Modifier.size(15.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("ইঙ্গিত", style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Zoomable Interactive India Vector Outline Map Canvas
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFEAF4FB)),
                        border = BorderStroke(1.2.dp, OnlineCobalt.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(0.92f)
                                .clip(RoundedCornerShape(20.dp))
                                .pointerInput(Unit) {
                                    detectTransformGestures { _, pan, zoom, _ ->
                                        val nextScale = (zoomScale * zoom).coerceIn(1f, 2.5f)
                                        val maxPan = (nextScale - 1f) * size.width * 0.45f
                                        zoomScale = nextScale
                                        panOffset = Offset(
                                            x = (panOffset.x + pan.x).coerceIn(-maxPan, maxPan),
                                            y = (panOffset.y + pan.y).coerceIn(-maxPan, maxPan)
                                        )
                                    }
                                }
                                .pointerInput(boardMode, currentTarget, locations, zoomScale, panOffset) {
                                    detectTapGestures { tapPos ->
                                        val cx = size.width / 2f
                                        val cy = size.height / 2f
                                        val unscaledX = ((tapPos.x - cx - panOffset.x) / zoomScale) + cx
                                        val unscaledY = ((tapPos.y - cy - panOffset.y) / zoomScale) + cy
                                        val normX = (unscaledX / size.width.toFloat()).coerceIn(0f, 1f)
                                        val normY = (unscaledY / size.height.toFloat()).coerceIn(0f, 1f)

                                        if (boardMode == 0 && currentTarget != null && userTapNorm == null) {
                                            userTapNorm = Offset(normX, normY)
                                            scoreAttempted++
                                            val dist = hypot(normX - currentTarget.normX, normY - currentTarget.normY)
                                            if (dist <= currentTarget.toleranceRadius) {
                                                scoreCorrect++
                                            }
                                        } else if (boardMode == 1) {
                                            val nearest = locations.minByOrNull { loc ->
                                                hypot(normX - loc.normX, normY - loc.normY)
                                            }
                                            if (nearest != null) {
                                                selectedAtlasLoc = nearest
                                            }
                                        }
                                    }
                                }
                                .testTag("interactive_india_map_canvas")
                        ) {
                            Canvas(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer(
                                        scaleX = zoomScale,
                                        scaleY = zoomScale,
                                        translationX = panOffset.x,
                                        translationY = panOffset.y
                                    )
                            ) {
                                drawIndiaBaseMap()

                                if (boardMode == 0 && currentTarget != null) {
                                    // Draw user's tap pin & reveal exact board symbol/location
                                    userTapNorm?.let { tap ->
                                        val tapPx = Offset(tap.x * size.width, tap.y * size.height)
                                        val targetPx = Offset(
                                            currentTarget.normX * size.width,
                                            currentTarget.normY * size.height
                                        )
                                        val isCorrect = tapEvaluation == true

                                        // Tolerance target zone ring
                                        drawCircle(
                                            color = Color(0xFF15803D).copy(alpha = 0.16f),
                                            radius = currentTarget.toleranceRadius * size.width,
                                            center = targetPx
                                        )
                                        drawCircle(
                                            color = Color(0xFF15803D),
                                            radius = currentTarget.toleranceRadius * size.width,
                                            center = targetPx,
                                            style = Stroke(
                                                width = 2.5f,
                                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f))
                                            )
                                        )

                                        // Dashed correction line if wrong
                                        if (!isCorrect) {
                                            drawLine(
                                                color = Color(0xFFC62828),
                                                start = tapPx,
                                                end = targetPx,
                                                strokeWidth = 3f,
                                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f))
                                            )
                                        }

                                        // User pin
                                        drawCircle(
                                            color = if (isCorrect) Color(0xFF16A34A) else Color(0xFFDC2626),
                                            radius = 12f,
                                            center = tapPx
                                        )
                                        drawCircle(
                                            color = Color.White,
                                            radius = 5f,
                                            center = tapPx
                                        )

                                        // Exact Board Pin
                                        drawBoardSymbolMarker(
                                            center = targetPx,
                                            symbolType = currentTarget.symbolType,
                                            highlight = true
                                        )
                                    }
                                } else if (boardMode == 1) {
                                    // Draw all domain pins in Study Atlas Mode
                                    locations.forEach { loc ->
                                        val isSelected = loc.id == selectedAtlasLoc?.id
                                        drawBoardSymbolMarker(
                                            center = Offset(loc.normX * size.width, loc.normY * size.height),
                                            symbolType = loc.symbolType,
                                            highlight = isSelected
                                        )
                                    }
                                }
                            }

                            // Zoom Controls Overlay
                            Column(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                                    tonalElevation = 2.dp
                                ) {
                                    Column {
                                        IconButton(
                                            onClick = { zoomScale = (zoomScale + 0.35f).coerceAtMost(2.5f) },
                                            modifier = Modifier.size(38.dp)
                                        ) {
                                            Icon(Icons.Rounded.Add, contentDescription = "Zoom in", modifier = Modifier.size(18.dp))
                                        }
                                        IconButton(
                                            onClick = {
                                                zoomScale = (zoomScale - 0.35f).coerceAtLeast(1f)
                                                if (zoomScale <= 1.05f) panOffset = Offset.Zero
                                            },
                                            modifier = Modifier.size(38.dp)
                                        ) {
                                            Icon(Icons.Rounded.Remove, contentDescription = "Zoom out", modifier = Modifier.size(18.dp))
                                        }
                                        IconButton(
                                            onClick = {
                                                zoomScale = 1f
                                                panOffset = Offset.Zero
                                            },
                                            modifier = Modifier.size(38.dp)
                                        ) {
                                            Icon(Icons.Rounded.CenterFocusStrong, contentDescription = "Reset zoom", modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }

                            // Ocean Labels & Legend badge
                            Surface(
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = "▲ পর্বত  〰 নদী  ▓ অঞ্চল  ● কেন্দ্র  ★ বিদ্রোহ",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                // Instant Feedback Card (Quiz Mode after tap)
                if (boardMode == 0 && currentTarget != null && tapEvaluation != null) {
                    item {
                        val isCorrect = tapEvaluation
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isCorrect) Color(0xFF16A34A).copy(alpha = 0.12f)
                                else Color(0xFFDC2626).copy(alpha = 0.10f)
                            ),
                            border = BorderStroke(
                                1.2.dp,
                                if (isCorrect) Color(0xFF16A34A) else Color(0xFFDC2626)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isCorrect) Icons.Rounded.CheckCircle else Icons.Rounded.HighlightOff,
                                        contentDescription = null,
                                        tint = if (isCorrect) Color(0xFF15803D) else Color(0xFFC62828)
                                    )
                                    Text(
                                        text = if (isCorrect) {
                                            "সঠিক পয়েন্ট! (+১ নম্বর নিশ্চিত)"
                                        } else {
                                            "ভুল পয়েন্ট — সবুজ বৃত্তের সঠিক অবস্থান দেখে নাও"
                                        },
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCorrect) Color(0xFF15803D) else Color(0xFFC62828)
                                    )
                                }
                                Text(
                                    text = "📍 ${currentTarget.nameBn} (${currentTarget.nameEn}) — ${currentTarget.regionHintBn}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "📌 বোর্ড চিহ্ন (${currentTarget.symbolType.symbolChar}): ${currentTarget.boardTipBn}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            if (quizIndex < shuffledQuiz.lastIndex) {
                                                quizIndex++
                                            } else {
                                                shuffledQuiz = locations.shuffled()
                                                quizIndex = 0
                                            }
                                            userTapNorm = null
                                            showHint = false
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .minimumInteractiveComponentSize()
                                            .testTag("map_next_location_btn")
                                    ) {
                                        Text(if (quizIndex < shuffledQuiz.lastIndex) "পরবর্তী স্থান" else "নতুন সেট শুরু করো")
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                // Study Atlas Selected Pin Info Card
                if (boardMode == 1 && selectedAtlasLoc != null) {
                    val loc = selectedAtlasLoc!!
                    item {
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.2.dp, SageOffline.copy(alpha = 0.45f))
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Rounded.Map, contentDescription = null, tint = SageOffline)
                                    Text(
                                        text = "${loc.symbolType.symbolChar} ${loc.nameBn}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = "অবস্থান: ${loc.regionHintBn}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "বোর্ড টিপস: ${loc.boardTipBn}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "মানচিত্রের যেকোনো পিনে ট্যাপ করে তার নাম ও বোর্ড চিহ্ন দেখে নাও।",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = OnlineCobalt
                                )
                            }
                        }
                    }
                }

                // Shuffle / Reset Action
                item {
                    FilledTonalButton(
                        onClick = {
                            shuffledQuiz = locations.shuffled()
                            quizIndex = 0
                            userTapNorm = null
                            showHint = false
                            scoreCorrect = 0
                            scoreAttempted = 0
                            zoomScale = 1f
                            panOffset = Offset.Zero
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                    ) {
                        Icon(Icons.Rounded.Shuffle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ম্যাপ প্র্যাকটিস রিসেট ও শাফেল করো")
                    }
                }
            }
        }
    }
}

/**
 * Draws a crisp, authentic vector outline of India with key river arcs, Himalayan & Western Ghats
 * physical contours, and subtle state/regional divider lines calibrated to MapPointingBank coords.
 */
private fun DrawScope.drawIndiaBaseMap() {
    val w = size.width
    val h = size.height

    // Subtle lat/lon graticule grid
    val gridColor = Color(0xFF90CAF9).copy(alpha = 0.25f)
    for (i in 1..4) {
        val gx = w * (i / 5f)
        val gy = h * (i / 5f)
        drawLine(gridColor, Offset(gx, 0f), Offset(gx, h), strokeWidth = 1f)
        drawLine(gridColor, Offset(0f, gy), Offset(w, gy), strokeWidth = 1f)
    }

    // Main India Outline Polygon (calibrated: J&K top -> Gujarat -> Kanyakumari -> WB -> Northeast)
    val indiaPoints = listOf(
        0.31f to 0.06f, 0.38f to 0.08f, 0.43f to 0.14f, 0.40f to 0.19f,
        0.46f to 0.24f, 0.49f to 0.28f, 0.56f to 0.34f, 0.67f to 0.36f,
        0.72f to 0.34f, 0.77f to 0.36f, 0.85f to 0.31f, 0.93f to 0.33f,
        0.91f to 0.41f, 0.89f to 0.48f, 0.85f to 0.51f, 0.83f to 0.46f,
        0.78f to 0.44f, 0.76f to 0.49f, 0.74f to 0.54f, 0.66f to 0.58f,
        0.62f to 0.64f, 0.53f to 0.70f, 0.49f to 0.77f, 0.47f to 0.86f,
        0.40f to 0.94f, 0.36f to 0.90f, 0.32f to 0.81f, 0.28f to 0.70f,
        0.24f to 0.58f, 0.21f to 0.53f, 0.18f to 0.57f, 0.12f to 0.54f,
        0.10f to 0.48f, 0.15f to 0.46f, 0.14f to 0.41f, 0.22f to 0.34f,
        0.28f to 0.27f, 0.27f to 0.18f, 0.25f to 0.11f
    )

    val outlinePath = Path().apply {
        indiaPoints.forEachIndexed { idx, (nx, ny) ->
            if (idx == 0) moveTo(nx * w, ny * h) else lineTo(nx * w, ny * h)
        }
        close()
    }

    // Landmass Fill & Border
    drawPath(path = outlinePath, color = Color(0xFFFFFBF0))
    drawPath(
        path = outlinePath,
        color = Color(0xFF1E3A5F),
        style = Stroke(width = 3.2f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )

    // Subtle regional boundary guides (North/Central/Deccan/WB)
    val guideStroke = Stroke(width = 1.2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f)))
    drawLine(Color(0xFFB0BEC5), Offset(0.24f * w, 0.53f * h), Offset(0.68f * w, 0.53f * h), strokeWidth = 1.2f, pathEffect = guideStroke.pathEffect)
    drawLine(Color(0xFFB0BEC5), Offset(0.68f * w, 0.38f * h), Offset(0.68f * w, 0.55f * h), strokeWidth = 1.2f, pathEffect = guideStroke.pathEffect)

    // Key Mountain Range Arcs (Himalaya + Western Ghats)
    val mountainColor = Color(0xFF8D6E63).copy(alpha = 0.55f)
    val himalayaPath = Path().apply {
        moveTo(0.32f * w, 0.14f * h)
        quadraticTo(0.47f * w, 0.27f * h, 0.72f * w, 0.35f * h)
    }
    drawPath(himalayaPath, mountainColor, style = Stroke(width = 4f, cap = StrokeCap.Round))

    val westernGhats = Path().apply {
        moveTo(0.28f * w, 0.60f * h)
        quadraticTo(0.33f * w, 0.75f * h, 0.38f * w, 0.89f * h)
    }
    drawPath(westernGhats, mountainColor, style = Stroke(width = 3.2f, cap = StrokeCap.Round))

    // Major River Reference Paths (Ganga, Brahmaputra, Narmada, Godavari, Kaveri)
    val riverColor = Color(0xFF0288D1).copy(alpha = 0.65f)
    val riverStroke = Stroke(width = 2.6f, cap = StrokeCap.Round)

    // Ganga
    drawPath(
        Path().apply {
            moveTo(0.41f * w, 0.26f * h)
            quadraticTo(0.55f * w, 0.40f * h, 0.72f * w, 0.52f * h)
        },
        riverColor,
        style = riverStroke
    )
    // Brahmaputra
    drawPath(
        Path().apply {
            moveTo(0.90f * w, 0.33f * h)
            quadraticTo(0.82f * w, 0.38f * h, 0.75f * w, 0.48f * h)
        },
        riverColor,
        style = riverStroke
    )
    // Narmada (west-flowing)
    drawPath(
        Path().apply {
            moveTo(0.50f * w, 0.52f * h)
            lineTo(0.24f * w, 0.54f * h)
        },
        riverColor,
        style = riverStroke
    )
    // Godavari
    drawPath(
        Path().apply {
            moveTo(0.30f * w, 0.62f * h)
            quadraticTo(0.44f * w, 0.65f * h, 0.57f * w, 0.67f * h)
        },
        riverColor,
        style = riverStroke
    )
    // Kaveri
    drawPath(
        Path().apply {
            moveTo(0.35f * w, 0.81f * h)
            lineTo(0.48f * w, 0.83f * h)
        },
        riverColor,
        style = riverStroke
    )

    // Sri Lanka subtle silhouette for geographical reference
    val sriLanka = Path().apply {
        moveTo(0.47f * w, 0.92f * h)
        lineTo(0.50f * w, 0.94f * h)
        lineTo(0.49f * w, 0.98f * h)
        lineTo(0.46f * w, 0.96f * h)
        close()
    }
    drawPath(sriLanka, Color(0xFFE0E0E0))
    drawPath(sriLanka, Color(0xFF90A4AE), style = Stroke(width = 1.4f))

    // Andaman & Nicobar reference dots
    listOf(0.84f to 0.76f, 0.84f to 0.80f, 0.85f to 0.85f).forEach { (ax, ay) ->
        drawCircle(Color(0xFF1E3A5F).copy(alpha = 0.55f), radius = 3f, center = Offset(ax * w, ay * h))
    }
}

private fun DrawScope.drawBoardSymbolMarker(
    center: Offset,
    symbolType: MapSymbolType,
    highlight: Boolean
) {
    val baseRadius = if (highlight) 12f else 7.5f
    val color = when (symbolType) {
        MapSymbolType.MOUNTAIN -> Color(0xFF6D4C41)
        MapSymbolType.RIVER -> Color(0xFF0277BD)
        MapSymbolType.REGION -> Color(0xFF2E7D32)
        MapSymbolType.CITY_CENTER -> Color(0xFF1565C0)
        MapSymbolType.REVOLT_STAR -> Color(0xFFC62828)
    }

    if (highlight) {
        drawCircle(color = color.copy(alpha = 0.25f), radius = baseRadius * 1.9f, center = center)
    }

    when (symbolType) {
        MapSymbolType.MOUNTAIN -> {
            val tri = Path().apply {
                moveTo(center.x, center.y - baseRadius)
                lineTo(center.x - baseRadius, center.y + baseRadius * 0.8f)
                lineTo(center.x + baseRadius, center.y + baseRadius * 0.8f)
                close()
            }
            drawPath(tri, color)
            drawPath(tri, Color.White, style = Stroke(width = 2f))
        }
        MapSymbolType.REGION -> {
            drawCircle(color = color.copy(alpha = 0.45f), radius = baseRadius * 1.25f, center = center)
            drawCircle(color = color, radius = baseRadius * 1.25f, center = center, style = Stroke(width = 2.4f))
        }
        else -> {
            drawCircle(color = color, radius = baseRadius, center = center)
            drawCircle(color = Color.White, radius = baseRadius * 0.42f, center = center)
        }
    }
}
