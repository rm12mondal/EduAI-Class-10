package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import com.example.data.master.MathUnicodeFormatter

private val formulaLineSplitRegex = Regex("""\s+\|\s+|\r?\n""")

/**
 * Returns true when the active MaterialTheme surface is dark.
 */
@Composable
fun isDarkSurfaceTheme(): Boolean = MaterialTheme.colorScheme.surface.luminance() < 0.5f

/**
 * Returns a bright, high-contrast light color when rendered on dark surfaces,
 * while preserving the rich original accent on light surfaces.
 */
@Composable
fun highContrastAccent(accentColor: Color, darkLiftFraction: Float = 0.72f): Color {
    return if (isDarkSurfaceTheme()) {
        lerp(accentColor, Color.White, darkLiftFraction.coerceIn(0f, 1f))
    } else {
        accentColor
    }
}

/**
 * Splits pipe-separated (" | ") or newline-separated formulas into clean individual lines
 * with Unicode math/chemistry symbols formatted automatically.
 */
fun splitFormulaLines(raw: String): List<String> {
    if (raw.isBlank()) return emptyList()
    return MathUnicodeFormatter.format(raw)
        .split(formulaLineSplitRegex)
        .map { it.trim() }
        .filter { it.isNotEmpty() }
}
