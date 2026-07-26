package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.WrapText
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ReadingPalette

@Composable
fun RenderEnhancedCodeBlock(
    language: String,
    code: String,
    palette: ReadingPalette,
    searchQuery: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var isWordWrapEnabled by remember { mutableStateOf(false) }

    val codeLines = remember(code) { code.lines() }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = palette.codeBackground,
        border = androidx.compose.foundation.BorderStroke(1.dp, palette.dividerColor),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .testTag("enhanced_code_block")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(palette.surface.copy(alpha = 0.6f))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(palette.primary.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = language.ifBlank { "code" }.uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = palette.primary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${codeLines.size} lines",
                        fontSize = 11.sp,
                        color = palette.secondaryText
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Line Wrap Toggle
                    IconButton(
                        onClick = { isWordWrapEnabled = !isWordWrapEnabled },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.WrapText,
                            contentDescription = "Toggle line wrap",
                            tint = if (isWordWrapEnabled) palette.primary else palette.secondaryText,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Copy Button
                    IconButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(code))
                            Toast.makeText(context, "Code copied to clipboard", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("copy_code_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy code",
                            tint = palette.secondaryText,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Code Content Box
            val scrollModifier = if (isWordWrapEnabled) Modifier else Modifier.horizontalScroll(rememberScrollState())

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(scrollModifier)
                    .padding(12.dp)
            ) {
                Row {
                    // Line Numbers Column
                    Column(
                        modifier = Modifier.padding(end = 12.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        codeLines.indices.forEach { lineIdx ->
                            Text(
                                text = "${lineIdx + 1}",
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                color = palette.secondaryText.copy(alpha = 0.5f),
                                lineHeight = 18.sp
                            )
                        }
                    }

                    // Divider line
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .background(palette.dividerColor.copy(alpha = 0.5f))
                            .padding(end = 12.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    // Highlighted Code Text
                    Column {
                        codeLines.forEach { lineText ->
                            Text(
                                text = buildSyntaxHighlightedString(lineText, language, searchQuery, palette),
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

fun buildSyntaxHighlightedString(
    line: String,
    language: String,
    searchQuery: String,
    palette: ReadingPalette
): AnnotatedString {
    val keywords = setOf(
        "val", "var", "fun", "class", "object", "interface", "import", "package",
        "return", "if", "else", "when", "for", "while", "try", "catch", "throw",
        "public", "private", "protected", "override", "data", "sealed", "enum",
        "const", "type", "let", "const", "function", "def", "import", "from", "as"
    )

    return buildAnnotatedString {
        val words = line.split(Regex("(?<=\\b)|(?=\\b)"))
        words.forEach { word ->
            when {
                searchQuery.isNotBlank() && word.contains(searchQuery, ignoreCase = true) -> {
                    withStyle(SpanStyle(background = palette.searchHighlight, color = Color.Black)) {
                        append(word)
                    }
                }
                keywords.contains(word.trim()) -> {
                    withStyle(SpanStyle(color = palette.primary, fontWeight = FontWeight.Bold)) {
                        append(word)
                    }
                }
                word.startsWith("\"") || word.endsWith("\"") || word.startsWith("'") -> {
                    withStyle(SpanStyle(color = Color(0xFF10B981))) { // String color
                        append(word)
                    }
                }
                word.trim().matches(Regex("^\\d+$")) -> {
                    withStyle(SpanStyle(color = Color(0xFFF59E0B))) { // Number color
                        append(word)
                    }
                }
                word.startsWith("//") || word.startsWith("#") -> {
                    withStyle(SpanStyle(color = palette.secondaryText)) {
                        append(word)
                    }
                }
                else -> {
                    withStyle(SpanStyle(color = palette.codeText)) {
                        append(word)
                    }
                }
            }
        }
    }
}
