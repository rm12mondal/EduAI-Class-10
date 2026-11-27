package com.example.ui.security

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.security.BiometricUnlockHelper
import com.example.model.AppLockUiState
import com.example.ui.branding.BrandedAppLogoAvatar
import com.example.ui.branding.LocalAppBranding
import kotlinx.coroutines.delay

/**
 * Full-screen high-contrast App Lock Screen enforcing 4-digit PIN and optional
 * Fingerprint / Face Biometric unlock on every app open & resume (`ON_START`).
 * Respects custom logo & custom title so zero default branding leaks on the lock screen.
 */
@Composable
fun AppLockScreen(
    state: AppLockUiState,
    onSubmitPin: (String) -> Boolean,
    onBiometricSuccess: () -> Unit,
    onBiometricError: (String) -> Unit,
    onClearError: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Strictly block back navigation while locked
    BackHandler(enabled = true) {}

    val context = LocalContext.current
    val branding = LocalAppBranding.current
    var enteredPin by remember { mutableStateOf("") }
    var remainingLockoutSec by remember { mutableLongStateOf(0L) }

    // Live countdown ticker during brute-force cooldown
    LaunchedEffect(state.lockoutUntilEpochMs) {
        while (true) {
            val diff = state.lockoutUntilEpochMs - System.currentTimeMillis()
            if (diff <= 0L) {
                remainingLockoutSec = 0L
                break
            }
            remainingLockoutSec = ((diff + 999L) / 1000L).coerceAtLeast(1L)
            delay(500L)
        }
    }

    fun triggerBiometric() {
        val activity = BiometricUnlockHelper.findFragmentActivity(context)
        if (activity != null) {
            BiometricUnlockHelper.authenticate(
                activity = activity,
                appTitle = branding.effectiveTitle,
                onSuccess = onBiometricSuccess,
                onError = onBiometricError
            )
        } else {
            onBiometricError("বায়োমেট্রিক চালু করা যায়নি। ৪-ডিজিট PIN ব্যবহার করুন।")
        }
    }

    // Automatically launch Fingerprint/Face prompt when Lock Screen appears if enabled
    LaunchedEffect(state.isLocked, state.isBiometricEnabled, state.isBiometricHardwareAvailable) {
        if (state.isLocked && state.isBiometricEnabled && state.isBiometricHardwareAvailable) {
            delay(220L)
            triggerBiometric()
        }
    }

    fun onDigitPressed(digit: String) {
        if (remainingLockoutSec > 0L || enteredPin.length >= 4) return
        if (state.errorMessage != null) onClearError()
        val next = enteredPin + digit
        enteredPin = next
        if (next.length == 4) {
            val unlocked = onSubmitPin(next)
            if (!unlocked) {
                enteredPin = ""
            }
        }
    }

    fun onBackspacePressed() {
        if (enteredPin.isNotEmpty()) {
            enteredPin = enteredPin.dropLast(1)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF090F1F),
                        Color(0xFF13223F),
                        Color(0xFF0B172A)
                    )
                )
            )
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .testTag("app_lock_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 380.dp)
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Branded Custom/Disguised Logo + Lock Badge
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White.copy(alpha = 0.08f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.22f))
            ) {
                Box(modifier = Modifier.padding(8.dp)) {
                    BrandedAppLogoAvatar(
                        size = 64.dp,
                        shape = RoundedCornerShape(18.dp)
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Lock,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = branding.effectiveTitle,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
                Text(
                    text = if (remainingLockoutSec > 0L) {
                        "অস্থায়ীভাবে লক করা আছে • $remainingLockoutSec সেকেন্ড অপেক্ষা করো"
                    } else {
                        "অ্যাপ আনলক করতে ৪-ডিজিট PIN দাও"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (remainingLockoutSec > 0L) Color(0xFFFCA5A5) else Color(0xFFCBD5E1),
                    textAlign = TextAlign.Center
                )
            }

            // 4-Digit Animated PIN Dots
            PinDotsIndicator(
                enteredLength = enteredPin.length,
                isError = !state.errorMessage.isNullOrBlank()
            )

            // Error Feedback Banner
            if (!state.errorMessage.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFEF4444).copy(alpha = 0.18f),
                    border = BorderStroke(1.dp, Color(0xFFF87171).copy(alpha = 0.5f))
                ) {
                    Text(
                        text = state.errorMessage,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFFFECACA),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(6.dp))
            }

            // Circular Tactile PIN Keypad
            NumericPinKeypad(
                showBiometricKey = state.isBiometricEnabled && state.isBiometricHardwareAvailable,
                enabled = remainingLockoutSec == 0L,
                onDigit = ::onDigitPressed,
                onBackspace = ::onBackspacePressed,
                onBiometric = ::triggerBiometric
            )

            if (state.isBiometricEnabled && state.isBiometricHardwareAvailable) {
                TextButton(
                    onClick = ::triggerBiometric,
                    modifier = Modifier.testTag("app_lock_biometric_text_btn")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Fingerprint,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ফিঙ্গারপ্রিন্ট / ফেস দিয়ে আনলক করুন",
                        color = Color(0xFF7DD3FC),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun PinDotsIndicator(
    enteredLength: Int,
    isError: Boolean
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(18.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        repeat(4) { idx ->
            val filled = idx < enteredLength
            val dotSize by animateDpAsState(
                targetValue = if (filled) 18.dp else 14.dp,
                label = "pin_dot_size_$idx"
            )
            val dotColor by animateColorAsState(
                targetValue = when {
                    isError -> Color(0xFFF87171)
                    filled -> Color(0xFF38BDF8)
                    else -> Color.White.copy(alpha = 0.28f)
                },
                label = "pin_dot_color_$idx"
            )
            Box(
                modifier = Modifier
                    .size(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(dotSize)
                        .clip(CircleShape)
                        .background(dotColor)
                )
            }
        }
    }
}

@Composable
private fun NumericPinKeypad(
    showBiometricKey: Boolean,
    enabled: Boolean,
    onDigit: (String) -> Unit,
    onBackspace: () -> Unit,
    onBiometric: () -> Unit
) {
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9")
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        rows.forEach { rowDigits ->
            Row(horizontalArrangement = Arrangement.spacedBy(22.dp)) {
                rowDigits.forEach { digit ->
                    KeypadCircleButton(
                        enabled = enabled,
                        tag = "pin_key_$digit",
                        onClick = { onDigit(digit) }
                    ) {
                        Text(
                            text = digit,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Bottom Row: [Biometric / Empty] [0] [Backspace]
        Row(horizontalArrangement = Arrangement.spacedBy(22.dp)) {
            if (showBiometricKey) {
                KeypadCircleButton(
                    enabled = true,
                    tag = "pin_key_biometric",
                    containerColor = Color(0xFF0EA5E9).copy(alpha = 0.22f),
                    onClick = onBiometric
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Fingerprint,
                        contentDescription = "Biometric Unlock",
                        tint = Color(0xFF7DD3FC),
                        modifier = Modifier.size(30.dp)
                    )
                }
            } else {
                Spacer(modifier = Modifier.size(72.dp))
            }

            KeypadCircleButton(
                enabled = enabled,
                tag = "pin_key_0",
                onClick = { onDigit("0") }
            ) {
                Text(
                    text = "0",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            KeypadCircleButton(
                enabled = true,
                tag = "pin_key_backspace",
                containerColor = Color.White.copy(alpha = 0.08f),
                onClick = onBackspace
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Backspace,
                    contentDescription = "Delete digit",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun KeypadCircleButton(
    enabled: Boolean,
    tag: String,
    containerColor: Color = Color.White.copy(alpha = 0.13f),
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Surface(
        shape = CircleShape,
        color = if (enabled) containerColor else Color.White.copy(alpha = 0.05f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = if (enabled) 0.20f else 0.08f)),
        modifier = Modifier
            .size(72.dp)
            .minimumInteractiveComponentSize()
            .clip(CircleShape)
            .clickable(enabled = enabled, onClick = onClick)
            .testTag(tag)
    ) {
        Box(contentAlignment = Alignment.Center) {
            content()
        }
    }
}

enum class PinDialogMode {
    SET_NEW_PIN,
    VERIFY_TO_DISABLE
}

/**
 * Clean 2-step 4-digit PIN Setup / Change / Verify-to-Disable dialog launched from Settings.
 */
@Composable
fun PinSetupModalDialog(
    mode: PinDialogMode,
    isBiometricAvailable: Boolean,
    initialBiometricChecked: Boolean,
    onConfirmNewPin: (pin: String, enableBiometric: Boolean) -> Unit,
    onVerifyPinToDisable: (pin: String) -> Boolean,
    onDismiss: () -> Unit
) {
    var step by remember { mutableStateOf(0) } // 0 = first entry, 1 = confirm entry
    var firstPin by remember { mutableStateOf("") }
    var activeInput by remember { mutableStateOf("") }
    var enableBiometric by remember { mutableStateOf(initialBiometricChecked && isBiometricAvailable) }
    var localError by remember { mutableStateOf<String?>(null) }

    val titleText = when {
        mode == PinDialogMode.VERIFY_TO_DISABLE -> "অ্যাপ লক বন্ধ করতে বর্তমান PIN দাও"
        step == 0 -> "নতুন ৪-ডিজিট PIN সেট করুন"
        else -> "নিশ্চিত করতে PIN আবার দাও"
    }

    fun handleDigit(d: String) {
        if (activeInput.length >= 4) return
        localError = null
        val next = activeInput + d
        activeInput = next
        if (next.length == 4) {
            if (mode == PinDialogMode.VERIFY_TO_DISABLE) {
                val ok = onVerifyPinToDisable(next)
                if (ok) {
                    onDismiss()
                } else {
                    localError = "ভুল PIN! আবার চেষ্টা করো।"
                    activeInput = ""
                }
            } else {
                if (step == 0) {
                    firstPin = next
                    activeInput = ""
                    step = 1
                } else {
                    if (next == firstPin) {
                        onConfirmNewPin(next, enableBiometric)
                        onDismiss()
                    } else {
                        localError = "দুটি PIN মেলেনি! আবার নতুন PIN দাও।"
                        firstPin = ""
                        activeInput = ""
                        step = 0
                    }
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = titleText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Visual 4-digit dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(4) { i ->
                        val filled = i < activeInput.length
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        localError != null -> MaterialTheme.colorScheme.error
                                        filled -> MaterialTheme.colorScheme.primary
                                        else -> MaterialTheme.colorScheme.outlineVariant
                                    }
                                )
                        )
                    }
                }

                if (!localError.isNullOrBlank()) {
                    Text(
                        text = localError.orEmpty(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }

                // Compact modal numeric grid
                val grid = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("C", "0", "⌫")
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    grid.forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            row.forEach { key ->
                                Surface(
                                    onClick = {
                                        when (key) {
                                            "C" -> {
                                                activeInput = ""
                                                localError = null
                                            }
                                            "⌫" -> if (activeInput.isNotEmpty()) {
                                                activeInput = activeInput.dropLast(1)
                                            }
                                            else -> handleDigit(key)
                                        }
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    modifier = Modifier
                                        .size(width = 68.dp, height = 48.dp)
                                        .testTag("pin_setup_key_$key")
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = key,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (mode == PinDialogMode.SET_NEW_PIN && isBiometricAvailable) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { enableBiometric = !enableBiometric }
                    ) {
                        Checkbox(
                            checked = enableBiometric,
                            onCheckedChange = { enableBiometric = it }
                        )
                        Text(
                            text = "ফিঙ্গারপ্রিন্ট / ফেস আনলক চালু রাখুন",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বাতিল")
            }
        }
    )
}
