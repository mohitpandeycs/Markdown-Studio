package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ReadingPalette

@Composable
fun RenderMathBlock(
    formula: String,
    palette: ReadingPalette,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = palette.codeBackground.copy(alpha = 0.7f),
        border = androidx.compose.foundation.BorderStroke(1.dp, palette.primary.copy(alpha = 0.3f)),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "MATH EQUATION",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = palette.primary,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                contentAlignment = Alignment.Center
            ) {
                RenderLaTeXText(
                    rawFormula = formula,
                    palette = palette,
                    isBlockMode = true
                )
            }
        }
    }
}

@Composable
fun RenderLaTeXText(
    rawFormula: String,
    palette: ReadingPalette,
    isBlockMode: Boolean = false
) {
    val cleanFormula = rawFormula.trim()
    val annotated = buildAnnotatedString {
        var i = 0
        while (i < cleanFormula.length) {
            when {
                // Superscript: ^2 or ^{abc}
                cleanFormula[i] == '^' -> {
                    i++
                    val exp = if (i < cleanFormula.length && cleanFormula[i] == '{') {
                        val end = cleanFormula.indexOf('}', i)
                        if (end != -1) {
                            val sub = cleanFormula.substring(i + 1, end)
                            i = end + 1
                            sub
                        } else {
                            val sub = cleanFormula.substring(i)
                            i = cleanFormula.length
                            sub
                        }
                    } else if (i < cleanFormula.length) {
                        val sub = cleanFormula[i].toString()
                        i++
                        sub
                    } else ""

                    withStyle(
                        SpanStyle(
                            baselineShift = BaselineShift.Superscript,
                            fontSize = if (isBlockMode) 13.sp else 10.sp,
                            color = palette.primary,
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append(exp)
                    }
                }

                // Subscript: _2 or _{abc}
                cleanFormula[i] == '_' -> {
                    i++
                    val sub = if (i < cleanFormula.length && cleanFormula[i] == '{') {
                        val end = cleanFormula.indexOf('}', i)
                        if (end != -1) {
                            val content = cleanFormula.substring(i + 1, end)
                            i = end + 1
                            content
                        } else {
                            val content = cleanFormula.substring(i)
                            i = cleanFormula.length
                            content
                        }
                    } else if (i < cleanFormula.length) {
                        val content = cleanFormula[i].toString()
                        i++
                        content
                    } else ""

                    withStyle(
                        SpanStyle(
                            baselineShift = BaselineShift.Subscript,
                            fontSize = if (isBlockMode) 13.sp else 10.sp,
                            color = palette.secondaryText,
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append(sub)
                    }
                }

                // LaTeX Commands: \frac, \sqrt, \alpha, \beta, \sum, \int, \pi, \infty
                cleanFormula[i] == '\\' -> {
                    val rest = cleanFormula.substring(i)
                    when {
                        rest.startsWith("\\frac") -> {
                            // Render fraction as (num)/(den)
                            val fracRegex = Regex("^\\\\frac\\{([^}]+)\\}\\{([^}]+)\\}")
                            val match = fracRegex.find(rest)
                            if (match != null) {
                                val num = match.groupValues[1]
                                val den = match.groupValues[2]
                                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = palette.primary)) {
                                    append("($num / $den)")
                                }
                                i += match.value.length
                            } else {
                                append(" ")
                                i++
                            }
                        }
                        rest.startsWith("\\sqrt") -> {
                            val sqrtRegex = Regex("^\\\\sqrt(\\{([^}]+)\\})?")
                            val match = sqrtRegex.find(rest)
                            if (match != null) {
                                val inner = match.groupValues[2].ifEmpty { "" }
                                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = palette.primary)) {
                                    append("√($inner)")
                                }
                                i += match.value.length
                            } else {
                                append("√")
                                i += 5
                            }
                        }
                        rest.startsWith("\\alpha") -> { append("α"); i += 6 }
                        rest.startsWith("\\beta") -> { append("β"); i += 5 }
                        rest.startsWith("\\gamma") -> { append("γ"); i += 6 }
                        rest.startsWith("\\delta") -> { append("δ"); i += 6 }
                        rest.startsWith("\\theta") -> { append("θ"); i += 6 }
                        rest.startsWith("\\pi") -> { append("π"); i += 3 }
                        rest.startsWith("\\infty") -> { append("∞"); i += 6 }
                        rest.startsWith("\\sum") -> { append("∑"); i += 4 }
                        rest.startsWith("\\int") -> { append("∫"); i += 4 }
                        rest.startsWith("\\times") -> { append("×"); i += 6 }
                        rest.startsWith("\\cdot") -> { append("•"); i += 5 }
                        rest.startsWith("\\pm") -> { append("±"); i += 3 }
                        rest.startsWith("\\neq") -> { append("≠"); i += 4 }
                        rest.startsWith("\\leq") -> { append("≤"); i += 4 }
                        rest.startsWith("\\geq") -> { append("≥"); i += 4 }
                        else -> {
                            append(cleanFormula[i])
                            i++
                        }
                    }
                }

                else -> {
                    append(cleanFormula[i])
                    i++
                }
            }
        }
    }

    Text(
        text = annotated,
        fontSize = if (isBlockMode) 20.sp else 15.sp,
        fontFamily = FontFamily.Serif,
        fontStyle = FontStyle.Italic,
        color = palette.onSurface,
        lineHeight = if (isBlockMode) 28.sp else 20.sp
    )
}
