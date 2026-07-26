package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.parser.InlineRun
import com.example.parser.MarkdownBlock
import com.example.parser.TaskItem
import com.example.ui.theme.ReadingPalette
import com.example.ui.theme.ReadingThemes

import com.example.data.HighlightNote

@Composable
fun RenderMarkdownBlock(
    block: MarkdownBlock,
    palette: ReadingPalette,
    baseFontSizeSp: Int,
    fontFamilyName: String,
    lineSpacingMultiplier: Float,
    searchQuery: String,
    isHighlightedBlock: Boolean,
    highlights: List<HighlightNote> = emptyList(),
    onTaskCheckedToggle: (taskIndex: Int) -> Unit,
    onQuickHighlightText: ((selectedText: String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val fontFamily = ReadingThemes.getFontFamily(fontFamilyName)
    val baseSp = baseFontSizeSp.sp
    val lineHeight = (baseFontSizeSp * lineSpacingMultiplier).sp

    val containerModifier = if (isHighlightedBlock) {
        modifier
            .fillMaxWidth()
            .background(
                color = palette.searchHighlight.copy(alpha = 0.35f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(vertical = 4.dp, horizontal = 6.dp)
    } else {
        modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    }

    Box(modifier = containerModifier) {
        when (block) {
            is MarkdownBlock.Heading -> {
                val scale = when (block.level) {
                    1 -> 1.8f
                    2 -> 1.5f
                    3 -> 1.3f
                    4 -> 1.15f
                    5 -> 1.05f
                    else -> 1.0f
                }
                val headingSize = (baseFontSizeSp * scale).sp
                val headingWeight = if (block.level <= 2) FontWeight.Bold else FontWeight.SemiBold

                Column(
                    modifier = Modifier
                        .padding(top = (12 / block.level).dp, bottom = 4.dp)
                        .clickable(enabled = onQuickHighlightText != null) {
                            onQuickHighlightText?.invoke(block.text)
                        }
                ) {
                    Text(
                        text = buildFormattedAnnotatedString(
                            runs = block.runs,
                            palette = palette,
                            searchQuery = searchQuery,
                            highlights = highlights
                        ),
                        fontSize = headingSize,
                        fontWeight = headingWeight,
                        fontFamily = fontFamily,
                        color = palette.onSurface,
                        lineHeight = (baseFontSizeSp * scale * 1.25f).sp
                    )
                    if (block.level <= 2) {
                        Spacer(modifier = Modifier.height(4.dp))
                        HorizontalDivider(
                            color = palette.dividerColor,
                            thickness = if (block.level == 1) 2.dp else 1.dp
                        )
                    }
                }
            }

            is MarkdownBlock.Paragraph -> {
                val fullText = remember(block.runs) { block.runs.joinToString("") { it.text } }
                Text(
                    text = buildFormattedAnnotatedString(
                        runs = block.runs,
                        palette = palette,
                        searchQuery = searchQuery,
                        highlights = highlights
                    ),
                    fontSize = baseSp,
                    fontFamily = fontFamily,
                    color = palette.onSurface,
                    lineHeight = lineHeight,
                    modifier = Modifier.clickable(enabled = onQuickHighlightText != null) {
                        if (fullText.isNotBlank()) {
                            onQuickHighlightText?.invoke(fullText)
                        }
                    }
                )
            }

            is MarkdownBlock.BlockQuote -> {
                Surface(
                    shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp),
                    color = palette.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp, horizontal = 12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(32.dp)
                                .background(palette.quoteBorder, shape = RoundedCornerShape(2.dp))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = buildFormattedAnnotatedString(
                                runs = block.runs,
                                palette = palette,
                                searchQuery = searchQuery
                            ),
                            fontSize = baseSp,
                            fontStyle = FontStyle.Italic,
                            fontFamily = fontFamily,
                            color = palette.onSurface.copy(alpha = 0.9f),
                            lineHeight = lineHeight
                        )
                    }
                }
            }

            is MarkdownBlock.CodeBlock -> {
                RenderEnhancedCodeBlock(
                    language = block.language,
                    code = block.code,
                    palette = palette,
                    searchQuery = searchQuery
                )
            }

            is MarkdownBlock.MathBlock -> {
                RenderMathBlock(
                    formula = block.formula,
                    palette = palette
                )
            }

            is MarkdownBlock.MermaidBlock -> {
                RenderMermaidBlock(
                    chartDefinition = block.chartDefinition,
                    palette = palette
                )
            }

            is MarkdownBlock.Footnote -> {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = palette.background,
                    border = androidx.compose.foundation.BorderStroke(1.dp, palette.dividerColor),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "[^${block.label}]",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = palette.primary,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = block.content,
                            fontSize = 12.sp,
                            color = palette.secondaryText
                        )
                    }
                }
            }

            is MarkdownBlock.UnorderedList -> {
                Column(modifier = Modifier.fillMaxWidth()) {
                    block.items.forEach { itemRuns ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "•",
                                fontSize = baseSp,
                                fontWeight = FontWeight.Bold,
                                color = palette.primary,
                                modifier = Modifier.padding(end = 10.dp)
                            )
                            Text(
                                text = buildFormattedAnnotatedString(itemRuns, palette, searchQuery),
                                fontSize = baseSp,
                                fontFamily = fontFamily,
                                color = palette.onSurface,
                                lineHeight = lineHeight
                            )
                        }
                    }
                }
            }

            is MarkdownBlock.OrderedList -> {
                Column(modifier = Modifier.fillMaxWidth()) {
                    block.items.forEach { (number, itemRuns) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = number,
                                fontSize = baseSp,
                                fontWeight = FontWeight.Bold,
                                color = palette.primary,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = buildFormattedAnnotatedString(itemRuns, palette, searchQuery),
                                fontSize = baseSp,
                                fontFamily = fontFamily,
                                color = palette.onSurface,
                                lineHeight = lineHeight
                            )
                        }
                    }
                }
            }

            is MarkdownBlock.TaskList -> {
                Column(modifier = Modifier.fillMaxWidth()) {
                    block.items.forEachIndexed { itemIdx, taskItem ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onTaskCheckedToggle(itemIdx) }
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = taskItem.checked,
                                onCheckedChange = { onTaskCheckedToggle(itemIdx) },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = palette.primary,
                                    uncheckedColor = palette.secondaryText
                                ),
                                modifier = Modifier.testTag("task_checkbox_${block.idx}_$itemIdx")
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = buildFormattedAnnotatedString(
                                    runs = taskItem.runs,
                                    palette = palette,
                                    searchQuery = searchQuery,
                                    forceStrikethrough = taskItem.checked
                                ),
                                fontSize = baseSp,
                                fontFamily = fontFamily,
                                color = if (taskItem.checked) palette.secondaryText else palette.onSurface,
                                lineHeight = lineHeight
                            )
                        }
                    }
                }
            }

            is MarkdownBlock.Table -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 6.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .border(1.dp, palette.tableBorder, RoundedCornerShape(8.dp))
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        // Table Headers
                        Row(
                            modifier = Modifier
                                .background(palette.tableHeaderBg)
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            block.headers.forEach { header ->
                                Text(
                                    text = header,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = baseSp,
                                    fontFamily = fontFamily,
                                    color = palette.onSurface,
                                    modifier = Modifier
                                        .padding(horizontal = 12.dp, vertical = 4.dp)
                                        .width(120.dp)
                                )
                            }
                        }
                        HorizontalDivider(color = palette.tableBorder, thickness = 1.dp)

                        // Table Rows
                        block.rows.forEachIndexed { rowIdx, row ->
                            Row(
                                modifier = Modifier
                                    .background(
                                        if (rowIdx % 2 == 0) palette.surface else palette.background
                                    )
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                row.forEach { cell ->
                                    Text(
                                        text = cell,
                                        fontSize = baseSp,
                                        fontFamily = fontFamily,
                                        color = palette.onSurface,
                                        modifier = Modifier
                                            .padding(horizontal = 12.dp, vertical = 4.dp)
                                            .width(120.dp)
                                    )
                                }
                            }
                            if (rowIdx < block.rows.size - 1) {
                                HorizontalDivider(color = palette.tableBorder.copy(alpha = 0.5f), thickness = 0.5.dp)
                            }
                        }
                    }
                }
            }

            is MarkdownBlock.ImageBlock -> {
                val imageModel = if (block.imageUrl.startsWith("img_")) {
                    val resId = LocalContext.current.resources.getIdentifier(
                        block.imageUrl, "drawable", LocalContext.current.packageName
                    )
                    if (resId != 0) resId else block.imageUrl
                } else block.imageUrl

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = palette.surface,
                        tonalElevation = 2.dp
                    ) {
                        AsyncImage(
                            model = imageModel,
                            contentDescription = block.altText,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                    if (block.altText.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = block.altText,
                            fontSize = 12.sp,
                            fontStyle = FontStyle.Italic,
                            color = palette.secondaryText
                        )
                    }
                }
            }

            is MarkdownBlock.HorizontalRule -> {
                HorizontalDivider(
                    color = palette.dividerColor,
                    thickness = 1.5.dp,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
        }
    }
}

@Composable
fun buildFormattedAnnotatedString(
    runs: List<InlineRun>,
    palette: ReadingPalette,
    searchQuery: String,
    highlights: List<HighlightNote> = emptyList(),
    forceStrikethrough: Boolean = false
): AnnotatedString {
    return buildAnnotatedString {
        runs.forEach { run ->
            val isMatch = searchQuery.isNotBlank() && run.text.contains(searchQuery, ignoreCase = true)
            val matchedHighlight = highlights.find { run.text.contains(it.selectedText, ignoreCase = true) }

            val highlightBgColor = remember(matchedHighlight?.colorHex) {
                matchedHighlight?.let {
                    try {
                        Color(android.graphics.Color.parseColor(it.colorHex)).copy(alpha = 0.45f)
                    } catch (e: Exception) {
                        null
                    }
                }
            }

            val spanStyle = SpanStyle(
                color = when {
                    run.isMath -> palette.primary
                    run.linkUrl != null -> palette.primary
                    else -> palette.onSurface
                },
                fontWeight = if (run.isBold || run.isMath) FontWeight.Bold else FontWeight.Normal,
                fontStyle = if (run.isItalic || run.isMath) FontStyle.Italic else FontStyle.Normal,
                fontFamily = if (run.isInlineCode || run.isMath) FontFamily.Monospace else null,
                background = when {
                    isMatch -> palette.searchHighlight
                    run.isInlineCode -> palette.codeBackground
                    run.isMath -> palette.codeBackground.copy(alpha = 0.5f)
                    else -> highlightBgColor ?: Color.Unspecified
                },
                textDecoration = when {
                    forceStrikethrough || run.isStrikethrough -> TextDecoration.LineThrough
                    run.linkUrl != null -> TextDecoration.Underline
                    else -> TextDecoration.None
                }
            )

            if (run.linkUrl != null) {
                pushStringAnnotation(tag = "URL", annotation = run.linkUrl)
            }
            withStyle(spanStyle) {
                if (run.isMath) {
                    append("\$ ${run.text} \$")
                } else {
                    append(run.text)
                }
            }
            if (run.linkUrl != null) {
                pop()
            }
        }
    }
}
