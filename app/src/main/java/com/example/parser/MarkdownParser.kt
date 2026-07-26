package com.example.parser

data class InlineRun(
    val text: String,
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val isInlineCode: Boolean = false,
    val isStrikethrough: Boolean = false,
    val isMath: Boolean = false,
    val linkUrl: String? = null
)

data class TaskItem(
    val checked: Boolean,
    val runs: List<InlineRun>,
    val originalLineIndex: Int
)

sealed class MarkdownBlock(val blockIndex: Int) {
    data class Heading(
        val level: Int,
        val text: String,
        val runs: List<InlineRun>,
        val idx: Int
    ) : MarkdownBlock(idx)

    data class Paragraph(
        val runs: List<InlineRun>,
        val idx: Int
    ) : MarkdownBlock(idx)

    data class BlockQuote(
        val text: String,
        val runs: List<InlineRun>,
        val idx: Int
    ) : MarkdownBlock(idx)

    data class CodeBlock(
        val language: String,
        val code: String,
        val idx: Int
    ) : MarkdownBlock(idx)

    data class MathBlock(
        val formula: String,
        val idx: Int
    ) : MarkdownBlock(idx)

    data class MermaidBlock(
        val chartDefinition: String,
        val idx: Int
    ) : MarkdownBlock(idx)

    data class UnorderedList(
        val items: List<List<InlineRun>>,
        val idx: Int
    ) : MarkdownBlock(idx)

    data class OrderedList(
        val items: List<Pair<String, List<InlineRun>>>,
        val idx: Int
    ) : MarkdownBlock(idx)

    data class TaskList(
        val items: List<TaskItem>,
        val idx: Int
    ) : MarkdownBlock(idx)

    data class Table(
        val headers: List<String>,
        val rows: List<List<String>>,
        val idx: Int
    ) : MarkdownBlock(idx)

    data class ImageBlock(
        val altText: String,
        val imageUrl: String,
        val idx: Int
    ) : MarkdownBlock(idx)

    data class Footnote(
        val label: String,
        val content: String,
        val idx: Int
    ) : MarkdownBlock(idx)

    data class HorizontalRule(
        val idx: Int
    ) : MarkdownBlock(idx)
}

data class TocItem(
    val level: Int,
    val title: String,
    val blockIndex: Int
)

data class ReadingStats(
    val wordCount: Int,
    val charCount: Int,
    val readingTimeMinutes: Int,
    val imageCount: Int,
    val codeBlockCount: Int,
    val linkCount: Int,
    val tableCount: Int,
    val headingCount: Int,
    val mathCount: Int,
    val diagramCount: Int
)

data class ParsedMarkdown(
    val rawText: String,
    val blocks: List<MarkdownBlock>,
    val toc: List<TocItem>,
    val wordCount: Int,
    val readingTimeMinutes: Int,
    val stats: ReadingStats
)

object MarkdownParser {

    private val EMOJI_MAP = mapOf(
        ":rocket:" to "🚀",
        ":smile:" to "😄",
        ":heart:" to "❤️",
        ":star:" to "⭐",
        ":fire:" to "🔥",
        ":check:" to "✅",
        ":warning:" to "⚠️",
        ":bulb:" to "💡",
        ":gear:" to "⚙️",
        ":books:" to "📚",
        ":memo:" to "📝",
        ":x:" to "❌",
        ":laptop:" to "💻",
        ":target:" to "🎯",
        ":sparkles:" to "✨",
        ":coffee:" to "☕",
        ":thumbsup:" to "👍",
        ":clap:" to "👏",
        ":tada:" to "🎉",
        ":bug:" to "🐛"
    )

    fun parse(rawText: String): ParsedMarkdown {
        val lines = rawText.lines()
        val blocks = mutableListOf<MarkdownBlock>()
        var blockCounter = 0

        var i = 0
        while (i < lines.size) {
            val line = lines[i]
            val trimmed = line.trim()

            if (trimmed.isEmpty()) {
                i++
                continue
            }

            // Math Block ($$ ... $$)
            if (trimmed.startsWith("$$")) {
                val formulaLines = mutableListOf<String>()
                val singleLineContent = trimmed.removePrefix("$$").removeSuffix("$$").trim()
                if (trimmed.endsWith("$$") && trimmed.length > 4) {
                    blocks.add(MarkdownBlock.MathBlock(singleLineContent, blockCounter++))
                    i++
                    continue
                } else {
                    i++
                    while (i < lines.size && !lines[i].trim().startsWith("$$")) {
                        formulaLines.add(lines[i])
                        i++
                    }
                    if (i < lines.size && lines[i].trim().startsWith("$$")) {
                        i++
                    }
                    blocks.add(MarkdownBlock.MathBlock(formulaLines.joinToString("\n"), blockCounter++))
                    continue
                }
            }

            // Code / Mermaid / Math Block (```)
            if (trimmed.startsWith("```")) {
                val language = trimmed.removePrefix("```").trim().lowercase()
                val codeLines = mutableListOf<String>()
                i++
                while (i < lines.size && !lines[i].trim().startsWith("```")) {
                    codeLines.add(lines[i])
                    i++
                }
                if (i < lines.size && lines[i].trim().startsWith("```")) {
                    i++
                }
                val rawCode = codeLines.joinToString("\n")
                if (language == "mermaid") {
                    blocks.add(MarkdownBlock.MermaidBlock(rawCode, blockCounter++))
                } else if (language == "math" || language == "latex") {
                    blocks.add(MarkdownBlock.MathBlock(rawCode, blockCounter++))
                } else {
                    blocks.add(MarkdownBlock.CodeBlock(language, rawCode, blockCounter++))
                }
                continue
            }

            // Horizontal Rule
            if (trimmed == "---" || trimmed == "***" || trimmed == "___" || trimmed == "--- ") {
                blocks.add(MarkdownBlock.HorizontalRule(blockCounter++))
                i++
                continue
            }

            // Footnotes definition ([^1]: content)
            val footnoteMatch = Regex("^\\[\\^(\\d+|\\w+)\\]:\\s*(.*)$").find(trimmed)
            if (footnoteMatch != null) {
                val label = footnoteMatch.groupValues[1]
                val content = footnoteMatch.groupValues[2]
                blocks.add(MarkdownBlock.Footnote(label, content, blockCounter++))
                i++
                continue
            }

            // Heading (#)
            if (trimmed.startsWith("#")) {
                var level = 0
                while (level < trimmed.length && trimmed[level] == '#') {
                    level++
                }
                if (level in 1..6 && (level >= trimmed.length || trimmed[level] == ' ')) {
                    val headingText = replaceEmojis(trimmed.substring(level).trim())
                    blocks.add(
                        MarkdownBlock.Heading(
                            level = level,
                            text = headingText,
                            runs = parseInlineFormatting(headingText),
                            idx = blockCounter++
                        )
                    )
                    i++
                    continue
                }
            }

            // Image (![alt](url))
            val imageMatch = Regex("^!\\[(.* nomination)?(.*)\\]\\((.*)\\)$").find(trimmed)
            if (imageMatch != null) {
                val alt = imageMatch.groupValues[2]
                val url = imageMatch.groupValues[3]
                blocks.add(MarkdownBlock.ImageBlock(altText = alt, imageUrl = url, idx = blockCounter++))
                i++
                continue
            }

            // Blockquote (>)
            if (trimmed.startsWith(">")) {
                val quoteLines = mutableListOf<String>()
                while (i < lines.size && lines[i].trim().startsWith(">")) {
                    quoteLines.add(lines[i].trim().removePrefix(">").trim())
                    i++
                }
                val fullQuote = replaceEmojis(quoteLines.joinToString("\n"))
                blocks.add(
                    MarkdownBlock.BlockQuote(
                        text = fullQuote,
                        runs = parseInlineFormatting(fullQuote),
                        idx = blockCounter++
                    )
                )
                continue
            }

            // Table (| header | header |)
            if (trimmed.startsWith("|") && trimmed.endsWith("|")) {
                val tableLines = mutableListOf<String>()
                while (i < lines.size && lines[i].trim().startsWith("|")) {
                    tableLines.add(lines[i].trim())
                    i++
                }
                if (tableLines.size >= 2) {
                    val parseRow = { r: String ->
                        r.split("|")
                            .drop(1)
                            .dropLast(1)
                            .map { replaceEmojis(it.trim()) }
                    }
                    val headers = parseRow(tableLines[0])
                    val rows = tableLines.drop(2).map { parseRow(it) }
                    blocks.add(MarkdownBlock.Table(headers, rows, blockCounter++))
                    continue
                }
            }

            // Task List (- [ ] or - [x])
            if (isTaskListItem(trimmed)) {
                val taskItems = mutableListOf<TaskItem>()
                while (i < lines.size && isTaskListItem(lines[i].trim())) {
                    val tLine = lines[i].trim()
                    val isChecked = tLine.contains("[x]") || tLine.contains("[X]")
                    val textContent = replaceEmojis(tLine.replace(Regex("^[*-]\\s*\\[[ xX]\\]"), "").trim())
                    taskItems.add(
                        TaskItem(
                            checked = isChecked,
                            runs = parseInlineFormatting(textContent),
                            originalLineIndex = i
                        )
                    )
                    i++
                }
                blocks.add(MarkdownBlock.TaskList(taskItems, blockCounter++))
                continue
            }

            // Unordered List (- item, * item, + item)
            if (isUnorderedListItem(trimmed)) {
                val items = mutableListOf<List<InlineRun>>()
                while (i < lines.size && isUnorderedListItem(lines[i].trim())) {
                    val tLine = lines[i].trim()
                    val textContent = replaceEmojis(tLine.replace(Regex("^[*-+]\\s+"), "").trim())
                    items.add(parseInlineFormatting(textContent))
                    i++
                }
                blocks.add(MarkdownBlock.UnorderedList(items, blockCounter++))
                continue
            }

            // Ordered List (1. item, 2. item)
            if (isOrderedListItem(trimmed)) {
                val items = mutableListOf<Pair<String, List<InlineRun>>>()
                while (i < lines.size && isOrderedListItem(lines[i].trim())) {
                    val tLine = lines[i].trim()
                    val numberMatch = Regex("^(\\d+\\.)\\s+(.*)$").find(tLine)
                    val num = numberMatch?.groupValues?.get(1) ?: "1."
                    val textContent = replaceEmojis(numberMatch?.groupValues?.get(2) ?: tLine)
                    items.add(Pair(num, parseInlineFormatting(textContent)))
                    i++
                }
                blocks.add(MarkdownBlock.OrderedList(items, blockCounter++))
                continue
            }

            // Paragraph (Fallback)
            val paragraphLines = mutableListOf<String>()
            while (i < lines.size) {
                val pTrim = lines[i].trim()
                if (pTrim.isEmpty() || pTrim.startsWith("#") || pTrim.startsWith("```") ||
                    pTrim.startsWith(">") || pTrim == "---" || pTrim.startsWith("$$") ||
                    isTaskListItem(pTrim) || isUnorderedListItem(pTrim) || isOrderedListItem(pTrim) ||
                    (pTrim.startsWith("|") && pTrim.endsWith("|"))
                ) {
                    break
                }
                paragraphLines.add(pTrim)
                i++
            }

            if (paragraphLines.isNotEmpty()) {
                val fullPara = replaceEmojis(paragraphLines.joinToString(" "))
                blocks.add(
                    MarkdownBlock.Paragraph(
                        runs = parseInlineFormatting(fullPara),
                        idx = blockCounter++
                    )
                )
            }
        }

        val toc = blocks.filterIsInstance<MarkdownBlock.Heading>().map {
            TocItem(level = it.level, title = it.text, blockIndex = it.idx)
        }

        val cleanTextForStats = rawText.replace(Regex("<[^>]*>"), "")
        val words = cleanTextForStats.split(Regex("\\s+")).filter { it.isNotBlank() }
        val wordCount = words.size
        val charCount = cleanTextForStats.length
        val readingTime = (wordCount / 200).coerceAtLeast(1)

        var linkCount = 0
        blocks.forEach { block ->
            when (block) {
                is MarkdownBlock.Paragraph -> linkCount += block.runs.count { it.linkUrl != null }
                is MarkdownBlock.Heading -> linkCount += block.runs.count { it.linkUrl != null }
                is MarkdownBlock.BlockQuote -> linkCount += block.runs.count { it.linkUrl != null }
                else -> {}
            }
        }

        val stats = ReadingStats(
            wordCount = wordCount,
            charCount = charCount,
            readingTimeMinutes = readingTime,
            imageCount = blocks.count { it is MarkdownBlock.ImageBlock },
            codeBlockCount = blocks.count { it is MarkdownBlock.CodeBlock },
            linkCount = linkCount,
            tableCount = blocks.count { it is MarkdownBlock.Table },
            headingCount = blocks.count { it is MarkdownBlock.Heading },
            mathCount = blocks.count { it is MarkdownBlock.MathBlock },
            diagramCount = blocks.count { it is MarkdownBlock.MermaidBlock }
        )

        return ParsedMarkdown(
            rawText = rawText,
            blocks = blocks,
            toc = toc,
            wordCount = wordCount,
            readingTimeMinutes = readingTime,
            stats = stats
        )
    }

    fun replaceEmojis(text: String): String {
        var result = text
        EMOJI_MAP.forEach { (key, emoji) ->
            result = result.replace(key, emoji)
        }
        return result
    }

    private fun isTaskListItem(line: String): Boolean {
        return line.matches(Regex("^[*-]\\s*\\[[ xX]\\].*$"))
    }

    private fun isUnorderedListItem(line: String): Boolean {
        return line.matches(Regex("^[*-+]\\s+.*$")) && !isTaskListItem(line)
    }

    private fun isOrderedListItem(line: String): Boolean {
        return line.matches(Regex("^\\d+\\.\\s+.*$"))
    }

    fun parseInlineFormatting(text: String): List<InlineRun> {
        val runs = mutableListOf<InlineRun>()
        if (text.isEmpty()) return runs

        // Inline Math: $ formula $
        // Inline Code: `code`
        // BoldItalic: ***text***
        // Bold: **text**
        // Italic: *text* or _text_
        // Strikethrough: ~~text~~
        // Links: [label](url) or autolink https://...
        val tokenRegex = Regex("(\\\$[^\\\$]+\\\$|\\[.*?\\]\\(.*?\\)|`.*?`|\\*\\*\\*.*?\\*\\*\\*|\\*\\*.*?\\*\\*|\\*.*?\\*|_.*?_|~~.*?~~|https?://[^\\s]+)")

        var currentIndex = 0
        tokenRegex.findAll(text).forEach { match ->
            if (match.range.first > currentIndex) {
                val plainText = text.substring(currentIndex, match.range.first)
                runs.add(InlineRun(text = plainText))
            }

            val token = match.value
            when {
                token.startsWith("$") && token.endsWith("$") && token.length > 2 -> {
                    runs.add(InlineRun(text = token.removeSurrounding("$"), isMath = true))
                }
                token.startsWith("[") && token.contains("](") && token.endsWith(")") -> {
                    val linkMatch = Regex("\\[(.*?)\\]\\((.*?)\\)").find(token)
                    val label = linkMatch?.groupValues?.get(1) ?: token
                    val url = linkMatch?.groupValues?.get(2) ?: ""
                    runs.add(InlineRun(text = label, linkUrl = url))
                }
                token.startsWith("http://") || token.startsWith("https://") -> {
                    runs.add(InlineRun(text = token, linkUrl = token))
                }
                token.startsWith("`") && token.endsWith("`") -> {
                    runs.add(InlineRun(text = token.removeSurrounding("`"), isInlineCode = true))
                }
                token.startsWith("***") && token.endsWith("***") -> {
                    runs.add(InlineRun(text = token.removeSurrounding("***"), isBold = true, isItalic = true))
                }
                token.startsWith("**") && token.endsWith("**") -> {
                    runs.add(InlineRun(text = token.removeSurrounding("**"), isBold = true))
                }
                token.startsWith("*") && token.endsWith("*") -> {
                    runs.add(InlineRun(text = token.removeSurrounding("*"), isItalic = true))
                }
                token.startsWith("_") && token.endsWith("_") -> {
                    runs.add(InlineRun(text = token.removeSurrounding("_"), isItalic = true))
                }
                token.startsWith("~~") && token.endsWith("~~") -> {
                    runs.add(InlineRun(text = token.removeSurrounding("~~"), isStrikethrough = true))
                }
                else -> {
                    runs.add(InlineRun(text = token))
                }
            }

            currentIndex = match.range.last + 1
        }

        if (currentIndex < text.length) {
            runs.add(InlineRun(text = text.substring(currentIndex)))
        }

        return runs
    }
}
