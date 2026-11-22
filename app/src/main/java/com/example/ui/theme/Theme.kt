package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val EduAiLightColorScheme = lightColorScheme(
    primary = AcademicIndigo,
    onPrimary = Color.White,
    primaryContainer = AcademicIndigoContainer,
    onPrimaryContainer = OnAcademicIndigoContainer,
    secondary = TerracottaAccent,
    onSecondary = Color.White,
    secondaryContainer = TerracottaContainer,
    onSecondaryContainer = OnTerracottaContainer,
    tertiary = SageOffline,
    onTertiary = Color.White,
    tertiaryContainer = SageOfflineContainer,
    onTertiaryContainer = OnSageOfflineContainer,
    background = WarmIvoryBackground,
    onBackground = SlateInk,
    surface = WarmParchmentSurface,
    onSurface = SlateInk,
    surfaceVariant = WarmSurfaceVariant,
    onSurfaceVariant = MutedInk,
    outline = WarmOutline,
    outlineVariant = Color(0xFFE7DFD4)
)

private val EduAiDarkColorScheme = darkColorScheme(
    primary = IndigoSoftLight,
    onPrimary = Color(0xFF0A2245),
    primaryContainer = Color(0xFF213C66),
    onPrimaryContainer = Color(0xFFDCE8FF),
    secondary = TerracottaSoftLight,
    onSecondary = Color(0xFF541C09),
    secondaryContainer = Color(0xFF6B2B15),
    onSecondaryContainer = Color(0xFFFFDDD2),
    tertiary = SageSoftLight,
    onTertiary = Color(0xFF063824),
    tertiaryContainer = Color(0xFF15513A),
    onTertiaryContainer = Color(0xFFCFF7E5),
    background = MidnightBackground,
    onBackground = IvoryText,
    surface = MidnightSurface,
    onSurface = IvoryText,
    surfaceVariant = MidnightSurfaceVariant,
    onSurfaceVariant = MutedIvoryText,
    outline = MidnightOutline,
    outlineVariant = Color(0xFF2A374E)
)

val EduAiShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun EduAiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) EduAiDarkColorScheme else EduAiLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = EduAiTypography,
        shapes = EduAiShapes,
        content = content
    )
}
