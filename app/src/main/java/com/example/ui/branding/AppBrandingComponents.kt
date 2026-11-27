package com.example.ui.branding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.model.LauncherDisguisePreset
import com.example.ui.theme.OnlineCobalt
import com.example.ui.theme.SageOffline

/**
 * Resolved in-app branding provided at the root via [LocalAppBranding].
 * Immediately updates every App Header, Splash/Welcome logo, Floating "D" button,
 * Settings header, Lock Screen, and AI Assistant avatar whenever the user changes
 * Custom Gallery Logo, Custom App Title, or Launcher Disguise Preset.
 */
data class ResolvedAppBranding(
    val effectiveTitle: String = "EduAI Class 10",
    val hasCustomTitle: Boolean = false,
    val customLogoBitmap: ImageBitmap? = null,
    val activePreset: LauncherDisguisePreset = LauncherDisguisePreset.DEFAULT_EDUAI
) {
    val isRebrandedOrDisguised: Boolean
        get() = hasCustomTitle || customLogoBitmap != null || activePreset != LauncherDisguisePreset.DEFAULT_EDUAI
}

val LocalAppBranding = staticCompositionLocalOf { ResolvedAppBranding() }

/**
 * Universal branded logo avatar used in Home Header, Splash/Welcome, Settings Header,
 * Floating "D" Popup Header, and App Lock Screen.
 * Priority: Custom Gallery Logo -> Active Disguise Icon -> Default Gradient Icon.
 */
@Composable
fun BrandedAppLogoAvatar(
    size: Dp = 40.dp,
    shape: Shape = RoundedCornerShape(12.dp),
    fallbackIcon: ImageVector = Icons.Rounded.School,
    contentDescription: String? = null,
    modifier: Modifier = Modifier
) {
    val branding = LocalAppBranding.current
    val customBmp = branding.customLogoBitmap
    val presetIconRes = branding.activePreset.iconDrawableRes

    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.secondary
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        when {
            customBmp != null -> {
                Image(
                    bitmap = customBmp,
                    contentDescription = contentDescription ?: branding.effectiveTitle,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            presetIconRes != null -> {
                Image(
                    painter = painterResource(id = presetIconRes),
                    contentDescription = contentDescription ?: branding.effectiveTitle,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            else -> {
                Icon(
                    imageVector = fallbackIcon,
                    contentDescription = contentDescription,
                    tint = Color.White,
                    modifier = Modifier.size(size * 0.56f)
                )
            }
        }
    }
}

/**
 * Compact circular AI assistant avatar placed beside every AI response header across
 * Main Chat, Smart Learning, Exam Chat, Brahmastram, Fast Learning, Mind-Map, and Step-by-Step Tutor.
 * When a Custom Gallery Logo is set, replaces the default AI icon everywhere instantly.
 */
@Composable
fun BrandedAiAssistantAvatar(
    size: Dp = 22.dp,
    defaultIcon: ImageVector = Icons.Rounded.AutoAwesome,
    defaultTint: Color = OnlineCobalt,
    modifier: Modifier = Modifier
) {
    val branding = LocalAppBranding.current
    val customBmp = branding.customLogoBitmap
    val presetIconRes = branding.activePreset.iconDrawableRes

    when {
        customBmp != null -> {
            Image(
                bitmap = customBmp,
                contentDescription = branding.effectiveTitle,
                contentScale = ContentScale.Crop,
                modifier = modifier
                    .size(size)
                    .clip(CircleShape)
            )
        }
        presetIconRes != null -> {
            Image(
                painter = painterResource(id = presetIconRes),
                contentDescription = branding.effectiveTitle,
                contentScale = ContentScale.Crop,
                modifier = modifier
                    .size(size)
                    .clip(CircleShape)
            )
        }
        else -> {
            Box(
                modifier = modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(defaultTint.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = defaultIcon,
                    contentDescription = null,
                    tint = defaultTint,
                    modifier = Modifier.size(size * 0.68f)
                )
            }
        }
    }
}

/**
 * Crisp Branded Launch Splash Overlay displayed briefly on cold start showing
 * the student's Custom Gallery Logo (or active Disguise icon) and Custom App Title.
 */
@Composable
fun BrandedSplashOverlay(
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    val branding = LocalAppBranding.current

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(150)),
        exit = fadeOut(tween(320)),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF0B132B),
                            Color(0xFF1B2A4A),
                            Color(0xFF0F2C3D)
                        )
                    )
                )
                .testTag("branded_splash_overlay"),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(32.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = Color.White.copy(alpha = 0.08f),
                    border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.24f)),
                    shadowElevation = 16.dp
                ) {
                    Box(modifier = Modifier.padding(10.dp)) {
                        BrandedAppLogoAvatar(
                            size = 86.dp,
                            shape = RoundedCornerShape(22.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = branding.effectiveTitle,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (branding.isRebrandedOrDisguised) {
                        "প্রাইভেট ওয়ার্কস্পেস লোড হচ্ছে…"
                    } else {
                        "WBBSE মাধ্যমিক এআই সহায়ক"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.78f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = SageOffline.copy(alpha = 0.22f),
                    border = BorderStroke(1.dp, SageOffline.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "100% OFFLINE READY",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF86EFAC),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }
            }
        }
    }
}
