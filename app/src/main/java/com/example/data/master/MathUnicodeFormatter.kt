package com.example.data.master

/**
 * Automatic background math, physics, and chemistry notation formatter.
 * Converts raw LaTeX/ASCII tokens (^2, ^3, _2, _4, \frac, \times, \div, \sqrt, \pi, \theta, \Delta, etc.)
 * into clean, crisp Unicode symbols (², ³, ₂, ₄, ×, ÷, √, π, θ, Δ) for WBBSE Class 10 readability.
 */
object MathUnicodeFormatter {

    private val superscriptMap = mapOf(
        '0' to '⁰', '1' to '¹', '2' to '²', '3' to '³', '4' to '⁴',
        '5' to '⁵', '6' to '⁶', '7' to '⁷', '8' to '⁸', '9' to '⁹',
        '+' to '⁺', '-' to '⁻', '=' to '⁼', '(' to '⁽', ')' to '⁾',
        'n' to 'ⁿ', 'i' to 'ⁱ', 'x' to 'ˣ'
    )

    private val subscriptMap = mapOf(
        '0' to '₀', '1' to '₁', '2' to '₂', '3' to '₃', '4' to '₄',
        '5' to '₅', '6' to '₆', '7' to '₇', '8' to '₈', '9' to '₉',
        '+' to '₊', '-' to '₋', '=' to '₌', '(' to '₍', ')' to '₎',
        'a' to 'ₐ', 'e' to 'ₑ', 'x' to 'ₓ', 'n' to 'ₙ', 'm' to 'ₘ'
    )

    private val fracRegex = Regex("""\\(?:d|t)?frac\{([^{}]+)\}\{([^{}]+)\}""")
    private val sqrtBracedRegex = Regex("""\\sqrt\{([^{}]+)\}""")
    private val sqrtSimpleRegex = Regex("""\\sqrt\s+([A-Za-z0-9]+)""")
    private val bracedSupRegex = Regex("""\^\{([0-9+\-=()nix]+)\}""")
    private val singleSupRegex = Regex("""\^([0-9n])(?![0-9A-Za-z])""")
    private val bracedSubRegex = Regex("""_\{([0-9+\-=()aexnm]+)\}""")
    private val chemicalSubRegex = Regex("""([A-Za-z)\]])_([0-9]+)""")
    private val mathDelimiterRegex = Regex("""\$\$([\s\S]*?)\$\$|\$([^$\n]+)\$|\\\(([^)]+)\\\)|\\\[([^\]]+)\\\]""")

    private val latexReplacements = listOf(
        "\\times" to "×",
        "\\cdot" to "·",
        "\\div" to "÷",
        "\\pm" to "±",
        "\\mp" to "∓",
        "\\neq" to "≠",
        "\\ne" to "≠",
        "\\leq" to "≤",
        "\\le" to "≤",
        "\\geq" to "≥",
        "\\ge" to "≥",
        "\\approx" to "≈",
        "\\equiv" to "≡",
        "\\propto" to "∝",
        "\\infty" to "∞",
        "\\rightarrow" to "→",
        "\\to" to "→",
        "\\leftarrow" to "←",
        "\\leftrightarrow" to "↔",
        "\\Rightarrow" to "⇒",
        "\\Leftrightarrow" to "⇔",
        "\\pi" to "π",
        "\\theta" to "θ",
        "\\Delta" to "Δ",
        "\\delta" to "δ",
        "\\alpha" to "α",
        "\\beta" to "β",
        "\\gamma" to "γ",
        "\\lambda" to "λ",
        "\\Omega" to "Ω",
        "\\omega" to "ω",
        "\\mu" to "μ",
        "\\rho" to "ρ",
        "\\sigma" to "σ",
        "\\phi" to "φ",
        "\\degree" to "°",
        "^\\circ" to "°",
        "\\circ" to "°",
        "\\angle" to "∠",
        "\\triangle" to "△",
        "\\perp" to "⊥",
        "\\parallel" to "∥",
        "\\therefore" to "∴",
        "\\because" to "∵",
        "\\sin" to "sin",
        "\\cos" to "cos",
        "\\tan" to "tan",
        "\\cot" to "cot",
        "\\sec" to "sec",
        "\\csc" to "cosec",
        "\\log" to "log",
        "\\left" to "",
        "\\right" to ""
    )

    fun format(raw: String): String {
        if (raw.isBlank()) return raw
        // Fast path if none of the typical math/latex markers exist
        if (!raw.contains('\\') && !raw.contains('^') && !raw.contains('_') && !raw.contains('$')) {
            return raw
        }

        var text = raw

        // 1. Unwrap $...$, $$...$$, \(...\), \[...\] while preserving inner expression
        text = mathDelimiterRegex.replace(text) { m ->
            m.groupValues.drop(1).firstOrNull { it.isNotEmpty() }.orEmpty().trim()
        }

        // 2. Replace nested/simple \frac{a}{b} up to 3 passes
        repeat(3) {
            if (text.contains("frac{")) {
                text = fracRegex.replace(text) { m ->
                    val num = m.groupValues[1].trim()
                    val den = m.groupValues[2].trim()
                    when ("$num/$den") {
                        "1/2" -> "½"
                        "1/3" -> "⅓"
                        "2/3" -> "⅔"
                        "1/4" -> "¼"
                        "3/4" -> "¾"
                        else -> {
                            val left = if (num.length > 1 && num.any { it == '+' || it == '-' || it == ' ' }) "($num)" else num
                            val right = if (den.length > 1 && den.any { it == '+' || it == '-' || it == ' ' }) "($den)" else den
                            "$left/$right"
                        }
                    }
                }
            }
        }

        // 3. Replace \sqrt{...} and \sqrt x
        repeat(2) {
            if (text.contains("\\sqrt")) {
                text = sqrtBracedRegex.replace(text) { m ->
                    val inner = m.groupValues[1].trim()
                    if (inner.length <= 2) "√$inner" else "√($inner)"
                }
                text = sqrtSimpleRegex.replace(text) { m ->
                    "√${m.groupValues[1]}"
                }
            }
        }
        text = text.replace("\\sqrt", "√")

        // 4. Replace standard LaTeX math & Greek commands
        for ((token, replacement) in latexReplacements) {
            if (text.contains(token)) {
                text = text.replace(token, replacement)
            }
        }

        // 5. Convert braced superscripts ^{2}, ^{-1}, ^{2+} -> ², ⁻¹, ²⁺
        text = bracedSupRegex.replace(text) { m ->
            toSuperscript(m.groupValues[1])
        }

        // 6. Convert single superscripts ^2, ^3, ^n -> ², ³, ⁿ
        text = singleSupRegex.replace(text) { m ->
            toSuperscript(m.groupValues[1])
        }

        // 7. Convert braced subscripts _{2}, _{4} -> ₂, ₄
        text = bracedSubRegex.replace(text) { m ->
            toSubscript(m.groupValues[1])
        }

        // 8. Convert chemical/math subscripts H_2O, SO_4, x_1 -> H₂O, SO₄, x₁
        // Carefully avoids snake_case identifiers with letters after digits
        text = chemicalSubRegex.replace(text) { m ->
            "${m.groupValues[1]}${toSubscript(m.groupValues[2])}"
        }

        // 9. Clean leftover \text{...} or \mathrm{...}
        text = text.replace(Regex("""\\(?:text|mathrm|mathbf|mathit)\{([^{}]+)\}"""), "$1")

        return text
    }

    private fun toSuperscript(input: String): String =
        input.map { superscriptMap[it] ?: it }.joinToString("")

    private fun toSubscript(input: String): String =
        input.map { subscriptMap[it] ?: it }.joinToString("")
}
