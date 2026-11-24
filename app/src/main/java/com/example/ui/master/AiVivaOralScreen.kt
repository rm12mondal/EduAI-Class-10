package com.example.ui.master

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.AiVivaUiState
import com.example.ui.chat.rememberVoiceInputLauncher
import com.example.ui.theme.MonoFontFamily
import com.example.ui.theme.OnlineCobalt
import com.example.ui.theme.SageOffline
import com.example.ui.theme.TerracottaAccent

/**
 * Feature #6: "পড়া ধরুন — AI Viva (Interactive Oral Tutor)"
 * Uses Bengali (bn-IN) & English (en-US) TTS to ask 1-mark & 2-mark questions aloud one by one,
 * accepts Voice Mic OR Text answers, and grades immediately with Step/Unit & Grammar checks!
 */
@Composable
fun AiVivaOralScreen(
    state: AiVivaUiState,
    onRepeatQuestionAloud: () -> Unit,
    onSubmitAnswer: (String) -> Unit,
    onClose: () -> Unit,
    onShowToast: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onClose() }
    var answerInput by rememberSaveable { mutableStateOf("") }

    val launchVoiceInput = rememberVoiceInputLauncher(
        onResultText = { spoken ->
            answerInput = if (answerInput.isBlank()) spoken else "$answerInput $spoken"
        },
        onUnavailable = onShowToast
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 700.dp)
        ) {
            Surface(color = MaterialTheme.colorScheme.surface, tonalElevation = 2.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("ai_viva_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "পড়া ধরুন — AI Viva (মৌখিক পরীক্ষা)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${state.subject.bengaliName} • ${state.chapterName}",
                            style = MaterialTheme.typography.labelSmall,
                            color = state.subject.accentColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Surface(
                        color = OnlineCobalt.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(999.dp)
                    ) {
                        Text(
                            text = "${state.history.size}/${state.questions.size}",
                            style = MaterialTheme.typography.labelMedium.copy(fontFamily = MonoFontFamily),
                            color = OnlineCobalt,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Active Question Card
                if (!state.isCompleted && state.currentQuestion != null) {
                    val q = state.currentQuestion!!
                    item(key = "active_viva_q") {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = OnlineCobalt.copy(alpha = 0.1f)),
                            border = BorderStroke(1.3.dp, OnlineCobalt.copy(alpha = 0.45f)),
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
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(Icons.Rounded.RecordVoiceOver, contentDescription = null, tint = OnlineCobalt)
                                        Text(
                                            text = "প্রশ্ন ${state.currentIndex + 1} (${q.marks} নম্বর)",
                                            style = MaterialTheme.typography.titleSmall,
                                            color = OnlineCobalt,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    FilledTonalIconButton(
                                        onClick = onRepeatQuestionAloud,
                                        modifier = Modifier.minimumInteractiveComponentSize()
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                                            contentDescription = "Repeat Question Aloud"
                                        )
                                    }
                                }

                                Text(
                                    text = q.questionText,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Text(
                                    text = "🎙️ নিচের মাইক বাটনে ট্যাপ করে মুখে উত্তর দাও, অথবা টাইপ করে পাঠাও!",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Completed Summary Card
                if (state.isCompleted) {
                    item(key = "viva_completed_summary") {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = SageOffline.copy(alpha = 0.14f)),
                            border = BorderStroke(1.2.dp, SageOffline.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = SageOffline)
                                    Text(
                                        text = "AI Viva সম্পন্ন! মোট স্কোর: ${state.totalObtained} / ${state.totalPossible}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = SageOffline,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = "তোমার মৌখিক পরীক্ষার নম্বর স্বয়ংক্রিয়ভাবে প্রোগ্রেস ট্র্যাকার ও সম্ভাব্য নম্বর মিটারে যুক্ত হয়েছে।",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Button(
                                    onClick = onClose,
                                    colors = ButtonDefaults.buttonColors(containerColor = SageOffline),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("অধ্যায়ে ফিরে যাও", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Previous Graded Turns
                itemsIndexed(state.history.reversed(), key = { i, t -> "${t.question.id}_$i" }) { _, turn ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = turn.question.questionText,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "${turn.marksObtained}/${turn.maxMarks}",
                                    style = MaterialTheme.typography.labelLarge.copy(fontFamily = MonoFontFamily),
                                    color = if (turn.marksObtained >= turn.maxMarks * 0.75f) SageOffline else TerracottaAccent,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "তোমার উত্তর: ${turn.studentAnswer}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = turn.feedbackBn,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            turn.unitOrGrammarNotes.forEach { n ->
                                Text(
                                    text = n,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = OnlineCobalt
                                )
                            }
                        }
                    }
                }
            }

            // Voice Mic + Text Answer Composer
            if (!state.isCompleted) {
                Surface(color = MaterialTheme.colorScheme.surface, tonalElevation = 3.dp) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilledTonalIconButton(
                            onClick = launchVoiceInput,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("viva_mic_button")
                        ) {
                            Icon(Icons.Rounded.Mic, contentDescription = "Speak Answer", tint = TerracottaAccent)
                        }

                        OutlinedTextField(
                            value = answerInput,
                            onValueChange = { answerInput = it },
                            placeholder = { Text("মুখে বলো বা উত্তর টাইপ করো…") },
                            maxLines = 3,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.weight(1f)
                        )

                        Button(
                            onClick = {
                                if (answerInput.isNotBlank()) {
                                    val ans = answerInput
                                    answerInput = ""
                                    onSubmitAnswer(ans)
                                }
                            },
                            enabled = answerInput.isNotBlank(),
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("viva_submit_button")
                        ) {
                            Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = "Submit Viva Answer")
                        }
                    }
                }
            }
        }
    }
}
