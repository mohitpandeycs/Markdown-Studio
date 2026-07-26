package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.HorizontalRule
import androidx.compose.material.icons.filled.InsertLink
import androidx.compose.material.icons.filled.PlaylistAddCheck
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ReadingPalette

@Composable
fun MarkdownSourceEditor(
    rawText: String,
    palette: ReadingPalette,
    baseFontSizeSp: Int,
    onContentChange: (newContent: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val linesCount by remember(rawText) {
        derivedStateOf { rawText.lines().size }
    }
    val wordsCount by remember(rawText) {
        derivedStateOf {
            if (rawText.isBlank()) 0
            else rawText.trim().split("\\s+".toRegex()).size
        }
    }
    val charsCount by remember(rawText) {
        derivedStateOf { rawText.length }
    }

    // Helper to insert markdown syntax
    fun insertSyntax(prefix: String, suffix: String = "") {
        val newText = "$rawText$prefix$suffix"
        onContentChange(newText)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(palette.background)
            .padding(16.dp)
    ) {
        // Quick Stats Header
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = palette.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Code,
                        contentDescription = null,
                        tint = palette.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "MARKDOWN SOURCE EDITOR",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.primary
                    )
                }

                Text(
                    text = "$linesCount lines • $wordsCount words • $charsCount chars",
                    fontSize = 11.sp,
                    color = palette.secondaryText
                )
            }
        }

        // Markdown Quick Formatting Toolbar
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = palette.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, palette.dividerColor),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(onClick = { insertSyntax("\n# ") }) {
                    Icon(Icons.Default.Title, contentDescription = "Header", tint = palette.onSurface)
                }
                IconButton(onClick = { insertSyntax("**", "**") }) {
                    Icon(Icons.Default.FormatBold, contentDescription = "Bold", tint = palette.onSurface)
                }
                IconButton(onClick = { insertSyntax("*", "*") }) {
                    Icon(Icons.Default.FormatItalic, contentDescription = "Italic", tint = palette.onSurface)
                }
                IconButton(onClick = { insertSyntax("`", "`") }) {
                    Icon(Icons.Default.Code, contentDescription = "Inline Code", tint = palette.onSurface)
                }
                IconButton(onClick = { insertSyntax("\n> ") }) {
                    Icon(Icons.Default.FormatQuote, contentDescription = "Quote", tint = palette.onSurface)
                }
                IconButton(onClick = { insertSyntax("\n- ") }) {
                    Icon(Icons.Default.FormatListBulleted, contentDescription = "List", tint = palette.onSurface)
                }
                IconButton(onClick = { insertSyntax("\n- [ ] ") }) {
                    Icon(Icons.Default.PlaylistAddCheck, contentDescription = "Task", tint = palette.onSurface)
                }
                IconButton(onClick = { insertSyntax("[", "](https://)") }) {
                    Icon(Icons.Default.InsertLink, contentDescription = "Link", tint = palette.onSurface)
                }
                IconButton(onClick = { insertSyntax("\n---\n") }) {
                    Icon(Icons.Default.HorizontalRule, contentDescription = "Divider", tint = palette.onSurface)
                }
            }
        }

        // Raw Editor Text Area
        OutlinedTextField(
            value = rawText,
            onValueChange = onContentChange,
            modifier = Modifier
                .fillMaxSize()
                .testTag("markdown_source_editor_input"),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = palette.surface,
                unfocusedContainerColor = palette.surface,
                focusedTextColor = palette.onSurface,
                unfocusedTextColor = palette.onSurface,
                focusedBorderColor = palette.primary,
                unfocusedBorderColor = palette.dividerColor
            ),
            textStyle = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = baseFontSizeSp.sp,
                lineHeight = (baseFontSizeSp * 1.4f).sp
            ),
            placeholder = {
                Text(
                    text = "Type or edit raw Markdown here...",
                    fontFamily = FontFamily.Monospace,
                    color = palette.secondaryText
                )
            }
        )
    }
}
