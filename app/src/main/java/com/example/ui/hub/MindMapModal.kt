package com.example.ui.hub

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountTree
import androidx.compose.material.icons.rounded.Chat
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.ZoomIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.master.MathUnicodeFormatter
import com.example.model.InferenceMode
import com.example.model.SectionChatUiState
import com.example.ui.chat.SectionChatPanel

data class MindMapBranch(
    val id: Int,
    val title: String,
    val points: List<String>,
    val accentColor: Color
)

data class MindMapTree(
    val rootTitle: String,
    val subjectLabel: String,
    val rawSourceText: String,
    val branches: List<MindMapBranch>
)

private val branchPalette = listOf(
    Color(0xFF1E5AA8), // Cobalt Blue
    Color(0xFF0F766E), // Deep Teal
    Color(0xFFB45309), // Amber Ochre
    Color(0xFF7C3AED), // Royal Violet
    Color(0xFFBE185D), // Crimson Rose
    Color(0xFF15803D)  // Emerald Forest
)

/**
 * Deterministic 100% offline parser that transforms any AI response (Bengali or English)
 * into a structured hierarchical Mind-Map / Flowchart tree with Unicode math symbols.
 */
object MindMapParser {

    fun parse(
        rawAnswer: String,
        fallbackTitle: String = "WBBSE স্মার্ট কনসেপ্ট",
        subjectLabel: String = "WBBSE Class 10"
    ): MindMapTree {
        val formatted = MathUnicodeFormatter.format(rawAnswer).trim()
        if (formatted.isBlank()) {
            return MindMapTree(
                rootTitle = fallbackTitle,
                subjectLabel = subjectLabel,
                rawSourceText = "",
                branches = listOf(
                    MindMapBranch(
                        id = 0,
                        title = "শুরু করো",
                        points = listOf("চ্যাট বা স্মার্ট লার্নিংয়ে যেকোনো প্রশ্ন জিজ্ঞেস করলে এখানে তার ভিজ্যুয়াল মাইন্ড-ম্যাপ তৈরি হবে।"),
                        accentColor = branchPalette[0]
                    )
                )
            )
        }

        val lines = formatted
            .lines()
            .map { it.trim() }
            .filter { it.isNotBlank() }

        // Determine root title from the first meaningful line or fallback
        val firstCleaned = cleanLinePrefix(lines.firstOrNull().orEmpty())
        val rootTitle = when {
            firstCleaned.length in 4..64 -> firstCleaned
            fallbackTitle.isNotBlank() -> fallbackTitle.take(60)
            else -> firstCleaned.take(56).ifBlank { "মূল ধারণা (Core Concept)" }
        }

        val parsedBranches = mutableListOf<Pair<String, MutableList<String>>>()
        var currentHeading: String? = null
        var currentPoints = mutableListOf<String>()

        fun flushBranch() {
            val h = currentHeading
            if (!h.isNullOrBlank() || currentPoints.isNotEmpty()) {
                val safeHeading = (h ?: "মূল পয়েন্ট ${parsedBranches.size + 1}").take(52)
                parsedBranches.add(safeHeading to currentPoints.toMutableList())
            }
            currentHeading = null
            currentPoints = mutableListOf()
        }

        val bodyLines = if (lines.size > 1 && cleanLinePrefix(lines.first()) == rootTitle) {
            lines.drop(1)
        } else {
            lines
        }

        for (line in bodyLines) {
            val isHeading = line.startsWith("#") ||
                line.startsWith("**") && line.endsWith("**") ||
                line.startsWith("🔹") ||
                line.startsWith("📌") ||
                line.startsWith("🎯") ||
                line.startsWith("📖") ||
                line.startsWith("🏆") ||
                line.startsWith("💡") ||
                (line.contains(":") && line.substringBefore(":").length in 3..42 && !line.startsWith("•") && !line.startsWith("-"))

            if (isHeading) {
                val cleaned = cleanLinePrefix(line)
                if (cleaned.contains(":") && cleaned.substringAfter(":").trim().length >= 8) {
                    flushBranch()
                    currentHeading = cleaned.substringBefore(":").trim()
                    currentPoints.add(cleaned.substringAfter(":").trim())
                } else {
                    flushBranch()
                    currentHeading = cleaned
                }
            } else if (line.startsWith("•") || line.startsWith("-") || line.startsWith("*") || Regex("""^[0-9০-৯]+[.)]\s+""").containsMatchIn(line)) {
                val cleaned = cleanLinePrefix(line)
                if (cleaned.isNotBlank()) {
                    if (currentHeading == null && cleaned.contains(":") && cleaned.substringBefore(":").length <= 36) {
                        currentHeading = cleaned.substringBefore(":").trim()
                        val rest = cleaned.substringAfter(":").trim()
                        if (rest.isNotBlank()) currentPoints.add(rest)
                    } else {
                        if (currentHeading == null) {
                            currentHeading = "ধাপ ${parsedBranches.size + 1}"
                        }
                        currentPoints.add(cleaned)
                        if (currentPoints.size >= 4) {
                            flushBranch()
                        }
                    }
                }
            } else {
                // Split paragraph into crisp Bengali/English sentences
                val sentences = cleanLinePrefix(line)
                    .split(Regex("[।.!?]+\\s*"))
                    .map { it.trim() }
                    .filter { it.length >= 6 }

                if (sentences.isNotEmpty()) {
                    if (currentHeading == null) {
                        currentHeading = when (parsedBranches.size) {
                            0 -> "মূল সংজ্ঞা ও ধারণা"
                            1 -> "গুরুত্বপূর্ণ সূত্র ও তথ্য"
                            2 -> "বোর্ড পরীক্ষার পয়েন্ট"
                            else -> "মূল অংশ ${parsedBranches.size + 1}"
                        }
                    }
                    sentences.forEach { sentence ->
                        currentPoints.add(sentence)
                        if (currentPoints.size >= 3) {
                            flushBranch()
                        }
                    }
                }
            }
        }
        flushBranch()

        // Ensure at least 2 visual branches if input was a single block
        val finalBranches = if (parsedBranches.isEmpty()) {
            listOf(
                MindMapBranch(
                    id = 0,
                    title = "সারাংশ",
                    points = listOf(formatted.take(240)),
                    accentColor = branchPalette[0]
                )
            )
        } else {
            parsedBranches.take(6).mapIndexed { idx, (heading, pts) ->
                val effectivePts = if (pts.isEmpty()) listOf(heading) else pts.take(4)
                MindMapBranch(
                    id = idx,
                    title = heading,
                    points = effectivePts,
                    accentColor = branchPalette[idx % branchPalette.size]
                )
            }
        }

        return MindMapTree(
            rootTitle = rootTitle,
            subjectLabel = subjectLabel,
            rawSourceText = formatted,
            branches = finalBranches
        )
    }

    private fun cleanLinePrefix(line: String): String {
        return line
            .replace(Regex("""^[#*\-•🔹📘📌🎯📖🏆💡✅⚡]+\s*"""), "")
            .replace(Regex("""^[0-9০-৯]+[.)]\s*"""), "")
            .replace("**", "")
            .replace("__", "")
            .replace("`", "")
            .trim()
    }
}

/**
 * Interactive Tree / Flowchart Visual Mind-Map Modal for the latest AI response.
 * Supports pinch-to-zoom, pan gestures, tap-to-focus branch cards, and 1-tap PDF export.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VisualMindMapModal(
    tree: MindMapTree,
    chatState: SectionChatUiState,
    inferenceMode: InferenceMode,
    speakingMessageId: Long?,
    voiceInjectedText: String?,
    onExportPdf: () -> Unit,
    onSendChatMessage: (String) -> Unit,
    onAttachChatPhoto: (Uri) -> Unit,
    onClearChatPendingPhoto: () -> Unit,
    onClearChat: () -> Unit,
    onToggleSpeak: (Long, String) -> Unit,
    onDismissChatError: () -> Unit,
    onConsumeVoiceInjectedText: () -> Unit,
    onRebuildMindMapFromReply: (String, String, String) -> Unit,
    onShowToast: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }
    var selectedBranchId by remember(tree) { mutableStateOf<Int?>(null) }
    var showChatTab by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            color = MaterialTheme.colorScheme.background,
            shape = RoundedCornerShape(0.dp),
            modifier = Modifier
                .fillMaxSize()
                .testTag("visual_mindmap_modal")
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Header Bar
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Rounded.AccountTree,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Column {
                                    Text(
                                        text = "ভিজ্যুয়াল মাইন্ড-ম্যাপ (Flowchart)",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${tree.subjectLabel} • ${tree.branches.size}টি মূল শাখা • জুম/ট্যাপ করো",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (!showChatTab) {
                                    IconButton(
                                        onClick = {
                                            scale = 1f
                                            panOffset = Offset.Zero
                                            selectedBranchId = null
                                        },
                                        modifier = Modifier
                                            .minimumInteractiveComponentSize()
                                            .testTag("mindmap_reset_zoom_btn")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.RestartAlt,
                                            contentDescription = "Reset zoom"
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = onDismiss,
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .testTag("mindmap_close_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Close,
                                        contentDescription = "Close Mind-Map"
                                    )
                                }
                            }
                        }

                        // 2-Tab Switcher: Flowchart View ↔ Dedicated Mind-Map Chat
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 12.dp, end = 12.dp, bottom = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                onClick = { showChatTab = false },
                                shape = RoundedCornerShape(12.dp),
                                color = if (!showChatTab) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .weight(1f)
                                    .minimumInteractiveComponentSize()
                                    .testTag("mindmap_tab_canvas")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.AccountTree,
                                        contentDescription = null,
                                        tint = if (!showChatTab) Color.White else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "🌳 মাইন্ড-ম্যাপ",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (!showChatTab) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            Surface(
                                onClick = { showChatTab = true },
                                shape = RoundedCornerShape(12.dp),
                                color = if (showChatTab) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .weight(1f)
                                    .minimumInteractiveComponentSize()
                                    .testTag("mindmap_tab_chat")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Chat,
                                        contentDescription = null,
                                        tint = if (showChatTab) Color.White else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "💬 মাইন্ড-ম্যাপ চ্যাট (${chatState.messages.size})",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (showChatTab) Color.White else MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                if (showChatTab) {
                    val starterPrompts = remember(tree.rootTitle) {
                        listOf(
                            "'${tree.rootTitle}' টপিকটির প্রতিটি শাখা সহজভাবে পয়েন্ট করে বুঝিয়ে দাও।",
                            "'${tree.rootTitle}' থেকে মাধ্যমিকে আসার মতো ৩টি গুরুত্বপূর্ণ প্রশ্ন ও উত্তর বলো।",
                            "নতুন টপিক: আলোর প্রতিসরণ ও লেন্স — মাইন্ড-ম্যাপ আকারে সাজিয়ে দাও।"
                        )
                    }
                    SectionChatPanel(
                        state = chatState,
                        inferenceMode = inferenceMode,
                        speakingMessageId = speakingMessageId,
                        accentColor = MaterialTheme.colorScheme.secondary,
                        starterPrompts = starterPrompts,
                        onSendMessage = onSendChatMessage,
                        onAttachPhotoUri = onAttachChatPhoto,
                        onClearPendingPhoto = onClearChatPendingPhoto,
                        onClearChat = onClearChat,
                        onToggleSpeak = onToggleSpeak,
                        onShowToast = onShowToast,
                        onDismissError = onDismissChatError,
                        voiceInjectedText = voiceInjectedText,
                        onConsumeVoiceInjectedText = onConsumeVoiceInjectedText,
                        extraAssistantActionLabel = "মাইন্ড-ম্যাপে দেখো",
                        onExtraAssistantAction = { msg ->
                            onRebuildMindMapFromReply(
                                msg.topicLabel.ifBlank { tree.rootTitle },
                                tree.subjectLabel,
                                msg.content
                            )
                            showChatTab = false
                        },
                        modifier = Modifier.weight(1f)
                    )
                } else {
                // Interactive Zoomable Flowchart Canvas & Node Tree
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.surfaceContainerLowest,
                                    MaterialTheme.colorScheme.surfaceContainerLow
                                )
                            )
                        )
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                val nextScale = (scale * zoom).coerceIn(0.8f, 2.4f)
                                scale = nextScale
                                panOffset = if (nextScale > 1.02f) {
                                    Offset(
                                        x = (panOffset.x + pan.x).coerceIn(-420f, 420f),
                                        y = (panOffset.y + pan.y).coerceIn(-420f, 420f)
                                    )
                                } else {
                                    Offset.Zero
                                }
                            }
                        }
                ) {
                    val scrollState = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .graphicsLayer(
                                scaleX = scale,
                                scaleY = scale,
                                translationX = panOffset.x,
                                translationY = panOffset.y
                            )
                            .padding(horizontal = 16.dp, vertical = 18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Root Concept Node
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.primary,
                            shadowElevation = 6.dp,
                            modifier = Modifier.fillMaxWidth(0.92f)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.16f)
                                ) {
                                    Text(
                                        text = "মূল ধারণা • ${tree.subjectLabel}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                                Text(
                                    text = tree.rootTitle,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        // Curved Flowchart Spine Connector
                        val spineColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(34.dp)
                        ) {
                            val centerX = size.width / 2f
                            drawLine(
                                color = spineColor,
                                start = Offset(centerX, 0f),
                                end = Offset(centerX, size.height * 0.55f),
                                strokeWidth = 5f,
                                cap = StrokeCap.Round
                            )
                            val leftPath = Path().apply {
                                moveTo(centerX, size.height * 0.55f)
                                cubicTo(
                                    centerX, size.height * 0.85f,
                                    size.width * 0.22f, size.height * 0.65f,
                                    size.width * 0.18f, size.height
                                )
                            }
                            val rightPath = Path().apply {
                                moveTo(centerX, size.height * 0.55f)
                                cubicTo(
                                    centerX, size.height * 0.85f,
                                    size.width * 0.78f, size.height * 0.65f,
                                    size.width * 0.82f, size.height
                                )
                            }
                            drawPath(leftPath, color = spineColor, style = Stroke(width = 4f, cap = StrokeCap.Round))
                            drawPath(rightPath, color = spineColor, style = Stroke(width = 4f, cap = StrokeCap.Round))
                            drawCircle(color = spineColor, radius = 6f, center = Offset(centerX, size.height * 0.55f))
                        }

                        // Branch Nodes with Tree Connector Stems
                        tree.branches.forEachIndexed { index, branch ->
                            val isSelected = selectedBranchId == null || selectedBranchId == branch.id
                            val alpha = if (isSelected) 1f else 0.48f

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                // Left Flowchart Tree Connector
                                Canvas(
                                    modifier = Modifier
                                        .width(28.dp)
                                        .height(64.dp)
                                ) {
                                    val x = size.width * 0.42f
                                    drawLine(
                                        color = branch.accentColor.copy(alpha = 0.65f),
                                        start = Offset(x, 0f),
                                        end = Offset(x, size.height),
                                        strokeWidth = 4f,
                                        cap = StrokeCap.Round
                                    )
                                    val curve = Path().apply {
                                        moveTo(x, size.height * 0.35f)
                                        quadraticTo(x, size.height * 0.5f, size.width, size.height * 0.5f)
                                    }
                                    drawPath(
                                        curve,
                                        color = branch.accentColor,
                                        style = Stroke(width = 4f, cap = StrokeCap.Round)
                                    )
                                    drawCircle(
                                        color = branch.accentColor,
                                        radius = 6f,
                                        center = Offset(x, size.height * 0.35f)
                                    )
                                }

                                // Branch Card + Child Concept Pills
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = alpha),
                                    border = BorderStroke(
                                        width = if (selectedBranchId == branch.id) 2.dp else 1.2.dp,
                                        color = branch.accentColor.copy(alpha = if (isSelected) 0.85f else 0.35f)
                                    ),
                                    shadowElevation = if (selectedBranchId == branch.id) 4.dp else 1.dp,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            selectedBranchId = if (selectedBranchId == branch.id) null else branch.id
                                        }
                                        .testTag("mindmap_branch_${branch.id}")
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Surface(
                                                shape = CircleShape,
                                                color = branch.accentColor,
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(
                                                        text = "${index + 1}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White
                                                    )
                                                }
                                            }
                                            Text(
                                                text = branch.title,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = branch.accentColor,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }

                                        FlowRow(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            branch.points.forEach { leaf ->
                                                Surface(
                                                    shape = RoundedCornerShape(10.dp),
                                                    color = branch.accentColor.copy(alpha = 0.10f),
                                                    border = BorderStroke(0.8.dp, branch.accentColor.copy(alpha = 0.28f))
                                                ) {
                                                    Text(
                                                        text = "▸ $leaf",
                                                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                                                        color = MaterialTheme.colorScheme.onSurface,
                                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Zoom indicator badge
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ZoomIn,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "${(scale * 100).toInt()}% • দুই আঙুলে জুম করো",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Bottom Action Bar
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 3.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = onExportPdf,
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFC62828),
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .minimumInteractiveComponentSize()
                                .testTag("mindmap_export_pdf_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.PictureAsPdf,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("মাইন্ড-ম্যাপ PDF সেভ করুন", maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }

                        Button(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("mindmap_done_btn")
                        ) {
                            Text("বন্ধ করো")
                        }
                    }
                }
                }
            }
        }
    }
}
