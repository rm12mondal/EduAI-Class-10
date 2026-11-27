package com.example.ui.chat

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoGraph
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material.icons.rounded.ZoomIn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.ui.common.rememberAsyncCachedImageBitmap
import com.example.ui.theme.OnlineCobalt
import com.example.ui.theme.SageOffline
import com.example.ui.theme.TerracottaAccent

private val marksOptions = listOf(2, 3, 4, 5, 8)

/**
 * Reusable pill bar placed right above both the Main Chat and Exam Chat composer boxes:
 * - "Smart To-The-Point Answer (টু-দ্য-পয়েন্ট উত্তর)" toggle
 * - Exact mark-weightage selector (2, 3, 4, 5, 8 marks) when Smart Answer Mode is active
 * - "বাংলা ↔ English" quick translation toggle
 */
@Composable
fun SmartAnswerAndToolsStrip(
    smartToThePoint: Boolean,
    translateMode: Boolean,
    targetMarks: Int?,
    onToggleSmartAnswer: () -> Unit,
    onToggleTranslate: () -> Unit,
    onSelectMarks: (Int?) -> Unit,
    testTagPrefix: String,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        item(key = "smart_to_the_point") {
            FilterChip(
                selected = smartToThePoint,
                onClick = onToggleSmartAnswer,
                label = {
                    Text(
                        text = stringResource(R.string.smart_answer_toggle),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (smartToThePoint) FontWeight.Bold else FontWeight.Medium
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Bolt,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = TerracottaAccent,
                    selectedLabelColor = Color.White,
                    selectedLeadingIconColor = Color.White
                ),
                modifier = Modifier.testTag("${testTagPrefix}_smart_answer_toggle")
            )
        }

        if (smartToThePoint) {
            items(marksOptions, key = { "marks_$it" }) { m ->
                val selected = targetMarks == m
                FilterChip(
                    selected = selected,
                    onClick = { onSelectMarks(if (selected) null else m) },
                    label = {
                        Text(
                            text = "${m} নম্বর",
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.testTag("${testTagPrefix}_marks_$m")
                )
            }
        }

        item(key = "translate_toggle") {
            FilterChip(
                selected = translateMode,
                onClick = onToggleTranslate,
                label = {
                    Text(
                        text = stringResource(R.string.translate_toggle),
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Translate,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = OnlineCobalt,
                    selectedLabelColor = Color.White,
                    selectedLeadingIconColor = Color.White
                ),
                modifier = Modifier.testTag("${testTagPrefix}_translate_toggle")
            )
        }
    }
}

/**
 * Displays an AI-generated labeled 2D educational diagram inside an assistant bubble,
 * with a crisp textbook frame and tap-to-zoom full-screen viewer.
 */
@Composable
fun GeneratedDiagramCard(
    imagePath: String,
    onZoomClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bitmap = rememberAsyncCachedImageBitmap(imagePath, maxSizePx = 720)
    if (bitmap == null) return

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        border = BorderStroke(1.2.dp, OnlineCobalt.copy(alpha = 0.35f)),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onZoomClick)
            .testTag("ai_generated_diagram_card")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(OnlineCobalt.copy(alpha = 0.08f))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AutoGraph,
                        contentDescription = null,
                        tint = OnlineCobalt,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "AI ভিজ্যুয়াল ডায়াগ্রাম (2D Labeled)",
                        style = MaterialTheme.typography.labelMedium,
                        color = OnlineCobalt,
                        fontWeight = FontWeight.Bold
                    )
                }
                Icon(
                    imageVector = Icons.Rounded.ZoomIn,
                    contentDescription = stringResource(R.string.diagram_tap_zoom),
                    tint = OnlineCobalt,
                    modifier = Modifier.size(18.dp)
                )
            }

            Image(
                bitmap = bitmap,
                contentDescription = "AI Generated 2D Educational Diagram",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 170.dp, max = 280.dp)
                    .padding(6.dp)
                    .clip(RoundedCornerShape(10.dp))
            )

            Text(
                text = stringResource(R.string.diagram_tap_zoom),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 10.dp, end = 10.dp, bottom = 6.dp)
            )
        }
    }
}

@Composable
fun DiagramGeneratingBanner(modifier: Modifier = Modifier) {
    Surface(
        color = OnlineCobalt.copy(alpha = 0.10f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, OnlineCobalt.copy(alpha = 0.3f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CircularProgressIndicator(
                color = OnlineCobalt,
                strokeWidth = 2.5.dp,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = stringResource(R.string.diagram_generating),
                style = MaterialTheme.typography.labelMedium,
                color = OnlineCobalt
            )
        }
    }
}

@Composable
fun FullScreenDiagramDialog(
    imagePath: String,
    onDismiss: () -> Unit
) {
    val fullBitmap = rememberAsyncCachedImageBitmap(imagePath, maxSizePx = 1280)
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.92f))
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        val nextScale = (scale * zoom).coerceIn(1f, 4f)
                        scale = nextScale
                        offset = if (nextScale > 1f) offset + pan else Offset.Zero
                    }
                }
        ) {
            if (fullBitmap != null) {
                Image(
                    bitmap = fullBitmap,
                    contentDescription = "Zoomed AI Educational Diagram",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offset.x,
                            translationY = offset.y
                        )
                )
            } else {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    color = Color.Black.copy(alpha = 0.65f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "🎨 AI ভিজ্যুয়াল ডায়াগ্রাম • জুম করতে দুই আঙুলে টানো",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                Surface(
                    onClick = onDismiss,
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.2f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Close diagram viewer",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

/**
 * Launches Android's built-in Speech Recognizer (Bengali + English) without requiring RECORD_AUDIO permission.
 */
@Composable
fun rememberVoiceInputLauncher(
    onResultText: (String) -> Unit,
    onUnavailable: (String) -> Unit
): () -> Unit {
    val context = LocalContext.current
    val promptText = stringResource(R.string.voice_input_prompt)
    val unavailableMsg = stringResource(R.string.voice_input_unavailable)

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val recognized = matches?.firstOrNull()?.trim()
            if (!recognized.isNullOrBlank()) {
                onResultText(recognized)
            }
        }
    }

    return remember(context, promptText, unavailableMsg) {
        {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "bn-IN")
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "bn-IN")
                putExtra(RecognizerIntent.EXTRA_PROMPT, promptText)
            }
            val resolved = intent.resolveActivity(context.packageManager)
            if (resolved != null) {
                runCatching { launcher.launch(intent) }
                    .onFailure { onUnavailable(unavailableMsg) }
            } else {
                onUnavailable(unavailableMsg)
            }
        }
    }
}
