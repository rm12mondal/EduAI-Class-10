package com.example.ui.model

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.StopCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.speech.EduTtsManager
import com.example.data.speech.TtsUiState
import com.example.model.TtsVoiceOption
import com.example.model.TtsVoiceSettings
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun VoiceSpeechSettingsCard(
    ttsState: TtsUiState,
    voiceSettings: TtsVoiceSettings,
    onUpdateSettings: (TtsVoiceSettings) -> Unit,
    onTestVoice: (preferBengali: Boolean) -> Unit,
    onRefreshVoices: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isTestingBengali = ttsState.speakingMessageId == EduTtsManager.TEST_PREVIEW_BN_ID
    val isTestingEnglish = ttsState.speakingMessageId == EduTtsManager.TEST_PREVIEW_EN_ID

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier
            .fillMaxWidth()
            .testTag("voice_speech_settings_card")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
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
                        imageVector = Icons.Rounded.RecordVoiceOver,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column {
                        Text(
                            text = stringResource(R.string.voice_settings_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(R.string.voice_settings_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = onRefreshVoices,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("refresh_tts_voices_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = "Refresh installed TTS voices",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // 1. Bengali (bn-IN) Voice Dropdown
            VoicePickerDropdown(
                label = "বাংলা ভয়েস (bn-IN) • ${ttsState.availableBengaliVoices.size}টি উপলব্ধ",
                selectedVoiceName = voiceSettings.bengaliVoiceName,
                options = ttsState.availableBengaliVoices,
                emptyHint = "সিস্টেম ডিফল্ট বাংলা (bn-IN)",
                testTag = "bengali_voice_dropdown",
                onSelectVoice = { voiceName ->
                    onUpdateSettings(voiceSettings.copy(bengaliVoiceName = voiceName))
                }
            )

            // 2. English (en-US) Voice Dropdown
            VoicePickerDropdown(
                label = "English Voice (en-US) • ${ttsState.availableEnglishVoices.size}টি উপলব্ধ",
                selectedVoiceName = voiceSettings.englishVoiceName,
                options = ttsState.availableEnglishVoices,
                emptyHint = "System Default English (en-US)",
                testTag = "english_voice_dropdown",
                onSelectVoice = { voiceName ->
                    onUpdateSettings(voiceSettings.copy(englishVoiceName = voiceName))
                }
            )

            // 3. Test Voice Preview Buttons (Bengali & English)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "${stringResource(R.string.voice_test_button)} (Test Voice)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledTonalButton(
                        onClick = { onTestVoice(true) },
                        enabled = ttsState.isEngineReady,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .testTag("test_bengali_voice_button")
                    ) {
                        Icon(
                            imageVector = if (isTestingBengali) Icons.Rounded.StopCircle else Icons.AutoMirrored.Rounded.VolumeUp,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isTestingBengali) stringResource(R.string.voice_stop_button) else "বাংলা (bn-IN)",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    OutlinedButton(
                        onClick = { onTestVoice(false) },
                        enabled = ttsState.isEngineReady,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .testTag("test_english_voice_button")
                    ) {
                        Icon(
                            imageVector = if (isTestingEnglish) Icons.Rounded.StopCircle else Icons.AutoMirrored.Rounded.VolumeUp,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isTestingEnglish) stringResource(R.string.voice_stop_button) else "English (en-US)",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // 4. Speech Rate Slider (0.7x to 1.5x, default 1.0x)
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "উচ্চারণের গতি (Speech Rate)",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = String.format(Locale.US, "%.2fx", voiceSettings.speechRate),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Slider(
                    value = voiceSettings.speechRate.coerceIn(0.7f, 1.5f),
                    onValueChange = { raw ->
                        val snapped = ((raw * 20f).roundToInt() / 20f).coerceIn(0.7f, 1.5f)
                        onUpdateSettings(voiceSettings.copy(speechRate = snapped))
                    },
                    valueRange = 0.7f..1.5f,
                    steps = 15,
                    modifier = Modifier.testTag("tts_speech_rate_slider")
                )
            }

            // 5. Voice Pitch Slider (0.8 to 1.2, default 1.0)
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "ভয়েস পিচ (Voice Pitch)",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = String.format(Locale.US, "%.2f", voiceSettings.pitch),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Slider(
                    value = voiceSettings.pitch.coerceIn(0.8f, 1.2f),
                    onValueChange = { raw ->
                        val snapped = ((raw * 20f).roundToInt() / 20f).coerceIn(0.8f, 1.2f)
                        onUpdateSettings(voiceSettings.copy(pitch = snapped))
                    },
                    valueRange = 0.8f..1.2f,
                    steps = 7,
                    modifier = Modifier.testTag("tts_voice_pitch_slider")
                )
            }

            // Reset to 1.0x / 1.0 default
            if (voiceSettings.speechRate != 1.0f || voiceSettings.pitch != 1.0f ||
                voiceSettings.bengaliVoiceName != null || voiceSettings.englishVoiceName != null
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = { onUpdateSettings(TtsVoiceSettings()) },
                        modifier = Modifier.testTag("tts_reset_defaults_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.RestartAlt,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ডিফল্ট (1.0x) রিসেট করো")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VoicePickerDropdown(
    label: String,
    selectedVoiceName: String?,
    options: List<TtsVoiceOption>,
    emptyHint: String,
    testTag: String,
    onSelectVoice: (String?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val autoLabel = stringResource(R.string.voice_auto_default)

    val selectedDisplay = remember(selectedVoiceName, options, emptyHint, autoLabel) {
        if (selectedVoiceName.isNullOrBlank()) {
            if (options.isEmpty()) emptyHint else autoLabel
        } else {
            options.firstOrNull { it.voiceName == selectedVoiceName }?.displayLabel ?: selectedVoiceName
        }
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
    ) {
        OutlinedTextField(
            value = selectedDisplay,
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text(autoLabel) },
                onClick = {
                    onSelectVoice(null)
                    expanded = false
                }
            )
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option.displayLabel,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    onClick = {
                        onSelectVoice(option.voiceName)
                        expanded = false
                    }
                )
            }
        }
    }
}
