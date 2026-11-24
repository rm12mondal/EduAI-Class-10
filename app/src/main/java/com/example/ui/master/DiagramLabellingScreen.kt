package com.example.ui.master

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.master.ScienceDiagramLabBank
import com.example.model.DiagramCalloutPoint
import com.example.model.DiagramLabUiState
import com.example.model.ScienceDiagramSpec
import com.example.model.ScienceDiagramType
import com.example.model.WbbseSubject
import kotlin.math.roundToInt

/**
 * Feature #15: "ছবি চিহ্নিতকরণ প্র্যাকটিস (Interactive Science Diagram Labelling)"
 * 100% Offline Interactive Canvas Vector Diagrams for Life Science & Physical Science.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DiagramLabellingScreen(
    state: DiagramLabUiState,
    onSwitchSubject: (WbbseSubject) -> Unit,
    onSelectDiagram: (Int) -> Unit,
    onExportPdf: (suggestedName: String, title: String, subtitle: String, body: String) -> Unit,
    onClose: () -> Unit
) {
    BackHandler { onClose() }

    val diagrams = remember(state.subject) {
        ScienceDiagramLabBank.getDiagramsForSubject(state.subject)
    }
    val activeDiagram = diagrams.getOrElse(state.selectedDiagramIndex) { diagrams.first() }

    // Per-diagram interactive state
    val matchedCallouts = remember(activeDiagram.id) { mutableStateMapOf<String, Boolean>() }
    var selectedCalloutId by remember(activeDiagram.id) {
        mutableStateOf(activeDiagram.callouts.firstOrNull()?.id)
    }
    var feedbackBanner by remember(activeDiagram.id) {
        mutableStateOf("ডায়াগ্রামের যেকোনো নম্বর বক্সে ট্যাপ করে নিচের তালিকা থেকে সঠিক বাংলা/ইংরেজি লেবেল বেছে নাও।")
    }
    var isLastWrong by remember(activeDiagram.id) { mutableStateOf(false) }

    val shuffledLabels = remember(activeDiagram.id) {
        activeDiagram.callouts.shuffled()
    }
    val solvedCount = matchedCallouts.values.count { it }
    val totalCount = activeDiagram.callouts.size

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "ছবি চিহ্নিতকরণ প্র্যাকটিস",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "১০০% অফলাইন • বোর্ড ৫/৩ নম্বর ডায়াগ্রাম ল্যাব",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.testTag("diagram_lab_back")
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "ফিরে যান")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val body = buildString {
                                appendLine("চিত্র: ${activeDiagram.titleBn} (${activeDiagram.titleEn})")
                                appendLine(activeDiagram.boardMarksNote)
                                appendLine()
                                activeDiagram.callouts.forEach { pt ->
                                    appendLine("${pt.numberLabel}. ${pt.labelBn} (${pt.labelEn})")
                                    appendLine("   • কাজ/পরিচয়: ${pt.hintBn}")
                                }
                            }
                            onExportPdf(
                                "চিত্র_${activeDiagram.titleBn}",
                                activeDiagram.titleBn,
                                activeDiagram.boardMarksNote,
                                body
                            )
                        },
                        modifier = Modifier.testTag("diagram_lab_pdf")
                    ) {
                        Icon(Icons.Rounded.PictureAsPdf, contentDescription = "PDF তৈরি করুন")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Subject Toggle Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(WbbseSubject.LIFE_SCIENCE, WbbseSubject.PHYSICAL_SCIENCE).forEach { subj ->
                        FilterChip(
                            selected = state.subject == subj,
                            onClick = { onSwitchSubject(subj) },
                            label = {
                                Text(
                                    text = if (subj == WbbseSubject.LIFE_SCIENCE) "জীবন বিজ্ঞান (৪টি)" else "ভৌত বিজ্ঞান (২টি)",
                                    fontWeight = FontWeight.SemiBold
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Diagram selector chips
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    diagrams.forEachIndexed { idx, spec ->
                        FilterChip(
                            selected = idx == state.selectedDiagramIndex,
                            onClick = { onSelectDiagram(idx) },
                            label = { Text(spec.titleBn) }
                        )
                    }
                }
            }

            // Header Info + Score Progress
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = activeDiagram.titleBn,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = activeDiagram.boardMarksNote,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (solvedCount == totalCount) Color(0xFF1B5E20) else MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                text = "$solvedCount / $totalCount চিহ্নিত",
                                color = Color.White,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }

            // Interactive Vector Canvas Board with Callout Boxes
            item {
                InteractiveDiagramCanvasCard(
                    spec = activeDiagram,
                    selectedCalloutId = selectedCalloutId,
                    matchedCallouts = matchedCallouts,
                    onSelectCallout = { pt ->
                        selectedCalloutId = pt.id
                        isLastWrong = false
                        feedbackBanner = if (matchedCallouts[pt.id] == true) {
                            "✅ বক্স #${pt.numberLabel}: ${pt.labelBn} (${pt.labelEn}) — ${pt.hintBn}"
                        } else {
                            "🎯 বক্স #${pt.numberLabel} সিলেক্ট হয়েছে। হিন্ট: ${pt.hintBn}"
                        }
                    }
                )
            }

            // Live Feedback Banner
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = when {
                        isLastWrong -> MaterialTheme.colorScheme.errorContainer
                        solvedCount == totalCount -> Color(0xFFE8F5E9)
                        else -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = feedbackBanner,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = if (isLastWrong) MaterialTheme.colorScheme.onErrorContainer else Color(0xFF1B2B20),
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Label Bank Chips
            item {
                Text(
                    text = "সঠিক লেবেল বেছে নাও (Tap to Match Selected Box):",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    shuffledLabels.forEach { option ->
                        val alreadyMatched = matchedCallouts[option.id] == true
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (alreadyMatched) Color(0xFF2E7D32).copy(alpha = 0.14f)
                            else MaterialTheme.colorScheme.surfaceContainerHigh,
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (alreadyMatched) Color(0xFF2E7D32) else MaterialTheme.colorScheme.outlineVariant
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable(enabled = !alreadyMatched) {
                                    val targetId = selectedCalloutId
                                        ?: activeDiagram.callouts.firstOrNull { matchedCallouts[it.id] != true }?.id
                                    if (targetId == option.id) {
                                        matchedCallouts[option.id] = true
                                        isLastWrong = false
                                        val nextOpen = activeDiagram.callouts.firstOrNull { matchedCallouts[it.id] != true }
                                        selectedCalloutId = nextOpen?.id
                                        feedbackBanner = if (nextOpen == null) {
                                            "🏆 দুর্দান্ত! সবকটি (${totalCount}/${totalCount}) অংশ নিখুঁতভাবে চিহ্নিত করেছ!"
                                        } else {
                                            "✅ একদম সঠিক! #${option.numberLabel} = ${option.labelBn} (${option.labelEn})। এবার #${nextOpen.numberLabel} চিহ্নিত করো।"
                                        }
                                    } else {
                                        isLastWrong = true
                                        val target = activeDiagram.callouts.firstOrNull { it.id == targetId }
                                        feedbackBanner = "❌ বক্স #${target?.numberLabel ?: "?"}-এর জন্য ‘${option.labelBn}’ সঠিক নয়। হিন্ট: ${target?.hintBn.orEmpty()}"
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (alreadyMatched) {
                                    Icon(
                                        Icons.Rounded.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF2E7D32),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                                Column {
                                    Text(
                                        text = option.labelBn,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = option.labelEn,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Action Buttons (Reset & Reveal All)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            matchedCallouts.clear()
                            selectedCalloutId = activeDiagram.callouts.firstOrNull()?.id
                            isLastWrong = false
                            feedbackBanner = "🔄 আবার শুরু করো! বক্স #1 সিলেক্ট করা আছে।"
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("diagram_reset_btn")
                    ) {
                        Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("আবার প্র্যাকটিস")
                    }
                    Button(
                        onClick = {
                            activeDiagram.callouts.forEach { matchedCallouts[it.id] = true }
                            isLastWrong = false
                            feedbackBanner = "👁️ সব চিহ্নিত অংশ দেখানো হয়েছে। খাতায় এঁকে অনুশীলন করো!"
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("diagram_reveal_btn")
                    ) {
                        Icon(Icons.Rounded.Visibility, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("সব উত্তর দেখো")
                    }
                }
            }
        }
    }
}

@Composable
private fun InteractiveDiagramCanvasCard(
    spec: ScienceDiagramSpec,
    selectedCalloutId: String?,
    matchedCallouts: Map<String, Boolean>,
    onSelectCallout: (DiagramCalloutPoint) -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFCFDF9)),
        modifier = Modifier.fillMaxWidth()
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(290.dp)
        ) {
            val widthPx = constraints.maxWidth.toFloat()
            val heightPx = constraints.maxHeight.toFloat()

            Canvas(modifier = Modifier.fillMaxSize()) {
                // Soft notebook blueprint grid
                val gridColor = Color(0xFFE8EFEA)
                val step = 28.dp.toPx()
                var gx = step
                while (gx < size.width) {
                    drawLine(gridColor, Offset(gx, 0f), Offset(gx, size.height), strokeWidth = 1f)
                    gx += step
                }
                var gy = step
                while (gy < size.height) {
                    drawLine(gridColor, Offset(0f, gy), Offset(size.width, gy), strokeWidth = 1f)
                    gy += step
                }

                // Draw the authentic science vector diagram
                when (spec.type) {
                    ScienceDiagramType.NEURON -> drawNeuronDiagram()
                    ScienceDiagramType.REFLEX_ARC -> drawReflexArcDiagram()
                    ScienceDiagramType.CHROMOSOME -> drawChromosomeDiagram()
                    ScienceDiagramType.HUMAN_EYE -> drawHumanEyeDiagram()
                    ScienceDiagramType.PRISM_REFRACTION -> drawPrismRefractionDiagram()
                    ScienceDiagramType.CONVEX_LENS -> drawConvexLensDiagram()
                }

                // Draw leader lines & anchor dots for each callout
                val dash = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                spec.callouts.forEach { pt ->
                    val anchor = Offset(pt.anchorX * size.width, pt.anchorY * size.height)
                    val boxPos = Offset(pt.boxX * size.width, pt.boxY * size.height)
                    val isMatched = matchedCallouts[pt.id] == true
                    val isSelected = selectedCalloutId == pt.id
                    val lineColor = when {
                        isMatched -> Color(0xFF2E7D32)
                        isSelected -> Color(0xFFD84315)
                        else -> Color(0xFF455A64)
                    }
                    drawLine(
                        color = lineColor,
                        start = anchor,
                        end = boxPos,
                        strokeWidth = if (isSelected) 3.5f else 2f,
                        pathEffect = if (isMatched) null else dash
                    )
                    drawCircle(color = Color.White, radius = 6.dp.toPx(), center = anchor)
                    drawCircle(color = lineColor, radius = 4.dp.toPx(), center = anchor)
                }
            }

            // Interactive Callout Boxes positioned at (boxX, boxY)
            spec.callouts.forEach { pt ->
                val isMatched = matchedCallouts[pt.id] == true
                val isSelected = selectedCalloutId == pt.id
                val boxCenterX = (pt.boxX * widthPx).roundToInt()
                val boxCenterY = (pt.boxY * heightPx).roundToInt()

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = when {
                        isMatched -> Color(0xFF1B5E20)
                        isSelected -> Color(0xFFD84315)
                        else -> Color(0xFF263238)
                    },
                    border = BorderStroke(if (isSelected) 2.dp else 1.dp, Color.White),
                    shadowElevation = 3.dp,
                    modifier = Modifier
                        .offset {
                            val halfW = if (isMatched) 115 else 68
                            val halfH = 32
                            IntOffset(
                                x = (boxCenterX - halfW).coerceIn(8, (widthPx - halfW * 2 - 8).roundToInt().coerceAtLeast(8)),
                                y = (boxCenterY - halfH).coerceIn(8, (heightPx - halfH * 2 - 8).roundToInt().coerceAtLeast(8))
                            )
                        }
                        .clickable { onSelectCallout(pt) }
                        .testTag("diagram_box_${pt.numberLabel}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .background(Color.White.copy(alpha = 0.22f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${pt.numberLabel}",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (isMatched) pt.labelBn else "ট্যাপ #${pt.numberLabel}",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawNeuronDiagram() {
    val w = size.width
    val h = size.height
    val cy = h * 0.50f
    // Dendrites
    val dendriteColor = Color(0xFF5E35B1)
    listOf(
        Offset(w * 0.14f, h * 0.24f),
        Offset(w * 0.10f, h * 0.38f),
        Offset(w * 0.10f, h * 0.60f),
        Offset(w * 0.16f, h * 0.72f),
        Offset(w * 0.26f, h * 0.26f)
    ).forEach { tip ->
        drawLine(dendriteColor, Offset(w * 0.24f, cy), tip, strokeWidth = 5f, cap = StrokeCap.Round)
        drawLine(dendriteColor, tip, Offset(tip.x - 14f, tip.y - 10f), strokeWidth = 3f, cap = StrokeCap.Round)
    }
    // Cell Body (Soma) & Nucleus
    drawCircle(color = Color(0xFFD1C4E9), radius = h * 0.14f, center = Offset(w * 0.24f, cy))
    drawCircle(color = dendriteColor, radius = h * 0.14f, center = Offset(w * 0.24f, cy), style = Stroke(4f))
    drawCircle(color = Color(0xFF4527A0), radius = h * 0.055f, center = Offset(w * 0.24f, cy))

    // Axon shaft
    drawLine(Color(0xFF3949AB), Offset(w * 0.31f, cy), Offset(w * 0.80f, cy), strokeWidth = 8f)

    // Myelin sheaths (with Nodes of Ranvier gaps)
    val sheathCenters = listOf(0.42f, 0.52f, 0.63f, 0.73f)
    sheathCenters.forEach { cxFrac ->
        val rx = w * cxFrac
        drawRoundRect(
            color = Color(0xFFFFCA28),
            topLeft = Offset(rx - w * 0.038f, cy - 16.dp.toPx()),
            size = Size(w * 0.076f, 32.dp.toPx()),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f)
        )
        drawRoundRect(
            color = Color(0xFFEF6C00),
            topLeft = Offset(rx - w * 0.038f, cy - 16.dp.toPx()),
            size = Size(w * 0.076f, 32.dp.toPx()),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f),
            style = Stroke(2.5f)
        )
    }

    // Axon terminal end brush
    val endStart = Offset(w * 0.80f, cy)
    listOf(0.36f, 0.44f, 0.50f, 0.58f, 0.65f).forEach { yFrac ->
        val tip = Offset(w * 0.89f, h * yFrac)
        drawLine(Color(0xFF3949AB), endStart, tip, strokeWidth = 4f, cap = StrokeCap.Round)
        drawCircle(Color(0xFFE53935), radius = 4.5f, center = tip)
    }
}

private fun DrawScope.drawReflexArcDiagram() {
    val w = size.width
    val h = size.height
    // Spinal cord cross-section on right
    drawOval(
        color = Color(0xFFE3F2FD),
        topLeft = Offset(w * 0.62f, h * 0.25f),
        size = Size(w * 0.26f, h * 0.50f)
    )
    drawOval(
        color = Color(0xFF1565C0),
        topLeft = Offset(w * 0.62f, h * 0.25f),
        size = Size(w * 0.26f, h * 0.50f),
        style = Stroke(4f)
    )
    // Butterfly gray matter
    val grayPath = Path().apply {
        moveTo(w * 0.70f, h * 0.36f)
        lineTo(w * 0.75f, h * 0.46f)
        lineTo(w * 0.80f, h * 0.36f)
        lineTo(w * 0.78f, h * 0.50f)
        lineTo(w * 0.80f, h * 0.64f)
        lineTo(w * 0.75f, h * 0.54f)
        lineTo(w * 0.70f, h * 0.64f)
        lineTo(w * 0.72f, h * 0.50f)
        close()
    }
    drawPath(grayPath, color = Color(0xFF90A4AE))

    // Skin receptor (top-left)
    drawRoundRect(
        color = Color(0xFFFFCCBC),
        topLeft = Offset(w * 0.08f, h * 0.28f),
        size = Size(w * 0.12f, h * 0.14f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(14f, 14f)
    )
    // Muscle effector (bottom-left)
    drawOval(
        color = Color(0xFFEF9A9A),
        topLeft = Offset(w * 0.08f, h * 0.58f),
        size = Size(w * 0.16f, h * 0.15f)
    )

    // Sensory neuron path (top curve)
    val sensory = Path().apply {
        moveTo(w * 0.20f, h * 0.35f)
        quadraticBezierTo(w * 0.44f, h * 0.22f, w * 0.71f, h * 0.42f)
    }
    drawPath(sensory, color = Color(0xFF00897B), style = Stroke(6f, cap = StrokeCap.Round))

    // Interneuron inside spinal cord
    drawLine(Color(0xFF6A1B9A), Offset(w * 0.71f, h * 0.42f), Offset(w * 0.71f, h * 0.58f), strokeWidth = 6f)

    // Motor neuron path (bottom curve)
    val motor = Path().apply {
        moveTo(w * 0.71f, h * 0.58f)
        quadraticBezierTo(w * 0.46f, h * 0.76f, w * 0.23f, h * 0.65f)
    }
    drawPath(motor, color = Color(0xFFD84315), style = Stroke(6f, cap = StrokeCap.Round))
}

private fun DrawScope.drawChromosomeDiagram() {
    val w = size.width
    val h = size.height
    val cx = w * 0.50f
    // Satellite bulb at top
    drawCircle(Color(0xFF80CBC4), radius = 14.dp.toPx(), center = Offset(cx, h * 0.16f))
    drawCircle(Color(0xFF00695C), radius = 14.dp.toPx(), center = Offset(cx, h * 0.16f), style = Stroke(3f))
    // Secondary constriction neck
    drawLine(Color(0xFF00695C), Offset(cx, h * 0.21f), Offset(cx, h * 0.28f), strokeWidth = 6f)

    // Left & Right chromatid arms (upper & lower)
    val armColor = Color(0xFF4DB6AC)
    val strokeColor = Color(0xFF00695C)
    listOf(-20.dp.toPx(), 20.dp.toPx()).forEach { dx ->
        drawRoundRect(
            color = armColor,
            topLeft = Offset(cx + dx - 14.dp.toPx(), h * 0.28f),
            size = Size(28.dp.toPx(), h * 0.20f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(24f, 24f)
        )
        drawRoundRect(
            color = strokeColor,
            topLeft = Offset(cx + dx - 14.dp.toPx(), h * 0.28f),
            size = Size(28.dp.toPx(), h * 0.20f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(24f, 24f),
            style = Stroke(3f)
        )
        drawRoundRect(
            color = armColor,
            topLeft = Offset(cx + dx - 14.dp.toPx(), h * 0.52f),
            size = Size(28.dp.toPx(), h * 0.34f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(24f, 24f)
        )
        drawRoundRect(
            color = strokeColor,
            topLeft = Offset(cx + dx - 14.dp.toPx(), h * 0.52f),
            size = Size(28.dp.toPx(), h * 0.34f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(24f, 24f),
            style = Stroke(3f)
        )
    }
    // Centromere
    drawCircle(Color(0xFFFFB300), radius = 13.dp.toPx(), center = Offset(cx, h * 0.50f))
    drawCircle(Color(0xFFE65100), radius = 13.dp.toPx(), center = Offset(cx, h * 0.50f), style = Stroke(3f))
}

private fun DrawScope.drawHumanEyeDiagram() {
    val w = size.width
    val h = size.height
    val center = Offset(w * 0.52f, h * 0.50f)
    val r = h * 0.32f
    // Sclera & Retina outer rings
    drawCircle(Color(0xFFE1F5FE), radius = r, center = center)
    drawCircle(Color(0xFF0277BD), radius = r, center = center, style = Stroke(6f))
    drawCircle(Color(0xFFFFAB91), radius = r - 7f, center = center, style = Stroke(4f))
    // Cornea bulge on left
    drawArc(
        color = Color(0xFF00ACC1),
        startAngle = 125f,
        sweepAngle = 110f,
        useCenter = false,
        topLeft = Offset(center.x - r - 18.dp.toPx(), center.y - r * 0.65f),
        size = Size(r * 0.85f, r * 1.3f),
        style = Stroke(6f)
    )
    // Biconvex Lens
    drawOval(
        color = Color(0xFFB3E5FC),
        topLeft = Offset(w * 0.31f, center.y - 26.dp.toPx()),
        size = Size(20.dp.toPx(), 52.dp.toPx())
    )
    drawOval(
        color = Color(0xFF01579B),
        topLeft = Offset(w * 0.31f, center.y - 26.dp.toPx()),
        size = Size(20.dp.toPx(), 52.dp.toPx()),
        style = Stroke(3f)
    )
    // Optic Nerve exiting right
    drawLine(Color(0xFFFFB300), Offset(center.x + r - 4f, center.y + 16f), Offset(w * 0.90f, center.y + 28f), strokeWidth = 14f)
}

private fun DrawScope.drawPrismRefractionDiagram() {
    val w = size.width
    val h = size.height
    val top = Offset(w * 0.50f, h * 0.22f)
    val left = Offset(w * 0.28f, h * 0.72f)
    val right = Offset(w * 0.72f, h * 0.72f)
    val prismPath = Path().apply {
        moveTo(top.x, top.y)
        lineTo(right.x, right.y)
        lineTo(left.x, left.y)
        close()
    }
    drawPath(prismPath, color = Color(0xFFE0F7FA))
    drawPath(prismPath, color = Color(0xFF00838F), style = Stroke(4.5f))

    // Incident ray -> Refracted ray -> Emergent ray
    val pInStart = Offset(w * 0.12f, h * 0.64f)
    val pFace1 = Offset(w * 0.37f, h * 0.50f)
    val pFace2 = Offset(w * 0.63f, h * 0.50f)
    val pOutEnd = Offset(w * 0.88f, h * 0.68f)
    drawLine(Color(0xFFD32F2F), pInStart, pFace1, strokeWidth = 5f, cap = StrokeCap.Round)
    drawLine(Color(0xFF1976D2), pFace1, pFace2, strokeWidth = 5f, cap = StrokeCap.Round)
    drawLine(Color(0xFF388E3C), pFace2, pOutEnd, strokeWidth = 5f, cap = StrokeCap.Round)

    // Dashed extensions for deviation angle (δ)
    val dash = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
    drawLine(Color(0xFFD32F2F), pFace1, Offset(w * 0.68f, h * 0.33f), strokeWidth = 2.5f, pathEffect = dash)
    drawLine(Color(0xFF388E3C), pFace2, Offset(w * 0.45f, h * 0.37f), strokeWidth = 2.5f, pathEffect = dash)
}

private fun DrawScope.drawConvexLensDiagram() {
    val w = size.width
    val h = size.height
    val cy = h * 0.50f
    // Principal Axis
    drawLine(Color(0xFF455A64), Offset(w * 0.06f, cy), Offset(w * 0.94f, cy), strokeWidth = 2.5f)
    // Convex Lens in center
    drawOval(
        color = Color(0xFFB3E5FC).copy(alpha = 0.7f),
        topLeft = Offset(w * 0.50f - 14.dp.toPx(), h * 0.22f),
        size = Size(28.dp.toPx(), h * 0.56f)
    )
    drawOval(
        color = Color(0xFF0277BD),
        topLeft = Offset(w * 0.50f - 14.dp.toPx(), h * 0.22f),
        size = Size(28.dp.toPx(), h * 0.56f),
        style = Stroke(3.5f)
    )
    // Focus points F1, F2
    drawCircle(Color(0xFFE65100), radius = 5f, center = Offset(w * 0.34f, cy))
    drawCircle(Color(0xFFE65100), radius = 5f, center = Offset(w * 0.66f, cy))
    drawCircle(Color(0xFF01579B), radius = 5f, center = Offset(w * 0.50f, cy))

    // Object arrow (left)
    val objX = w * 0.16f
    val objTop = h * 0.32f
    drawLine(Color(0xFF2E7D32), Offset(objX, cy), Offset(objX, objTop), strokeWidth = 6f, cap = StrokeCap.Round)
    // Inverted Real Image arrow (right)
    val imgX = w * 0.78f
    val imgBottom = h * 0.66f
    drawLine(Color(0xFFC62828), Offset(imgX, cy), Offset(imgX, imgBottom), strokeWidth = 6f, cap = StrokeCap.Round)

    // Ray 1: Parallel -> Lens -> Through F2 -> Image tip
    drawLine(Color(0xFF1565C0), Offset(objX, objTop), Offset(w * 0.50f, objTop), strokeWidth = 3f)
    drawLine(Color(0xFF1565C0), Offset(w * 0.50f, objTop), Offset(imgX, imgBottom), strokeWidth = 3f)
    // Ray 2: Through Optical Centre O -> Image tip
    drawLine(Color(0xFF6A1B9A), Offset(objX, objTop), Offset(imgX, imgBottom), strokeWidth = 3f)
}
