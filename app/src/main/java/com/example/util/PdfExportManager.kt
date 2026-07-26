package com.example.util

import android.content.Context
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import com.example.parser.MarkdownBlock
import com.example.parser.ParsedMarkdown
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object PdfExportManager {

    suspend fun exportToPdf(
        context: Context,
        documentTitle: String,
        parsed: ParsedMarkdown,
        targetUri: Uri? = null
    ): File? = withContext(Dispatchers.IO) {
        try {
            val pdfDocument = PdfDocument()
            val pageWidth = 595 // A4 width in points (1/72 inch)
            val pageHeight = 842 // A4 height in points
            val margin = 40f
            val contentWidth = pageWidth - (margin * 2)

            var pageNumber = 1
            var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            var page = pdfDocument.startPage(pageInfo)
            var canvas = page.canvas

            val titlePaint = Paint().apply {
                color = AndroidColor.BLACK
                textSize = 20f
                typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
                isAntiAlias = true
            }

            val heading1Paint = Paint().apply {
                color = AndroidColor.parseColor("#1E293B")
                textSize = 18f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            val heading2Paint = Paint().apply {
                color = AndroidColor.parseColor("#334155")
                textSize = 15f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            val bodyPaint = Paint().apply {
                color = AndroidColor.parseColor("#1E293B")
                textSize = 11f
                typeface = Typeface.DEFAULT
                isAntiAlias = true
            }

            val codePaint = Paint().apply {
                color = AndroidColor.parseColor("#0F172A")
                textSize = 10f
                typeface = Typeface.MONOSPACE
                isAntiAlias = true
            }

            val codeBgPaint = Paint().apply {
                color = AndroidColor.parseColor("#F1F5F9")
                style = Paint.Style.FILL
            }

            val dividerPaint = Paint().apply {
                color = AndroidColor.parseColor("#E2E8F0")
                strokeWidth = 1f
            }

            var currentY = margin + 20f

            // Draw Header Title
            canvas.drawText(documentTitle.removeSuffix(".md"), margin, currentY, titlePaint)
            currentY += 24f
            canvas.drawLine(margin, currentY, pageWidth - margin, currentY, dividerPaint)
            currentY += 20f

            fun checkNewPage(neededHeight: Float) {
                if (currentY + neededHeight > pageHeight - margin - 30f) {
                    // Draw Footer Page Number
                    val footerPaint = Paint().apply {
                        color = AndroidColor.GRAY
                        textSize = 9f
                        isAntiAlias = true
                    }
                    canvas.drawText("Page $pageNumber", pageWidth / 2f - 15f, pageHeight - margin, footerPaint)
                    pdfDocument.finishPage(page)

                    pageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas
                    currentY = margin + 20f
                }
            }

            for (block in parsed.blocks) {
                when (block) {
                    is MarkdownBlock.Heading -> {
                        val paint = if (block.level <= 2) heading1Paint else heading2Paint
                        checkNewPage(24f)
                        canvas.drawText(block.text, margin, currentY, paint)
                        currentY += 22f
                    }

                    is MarkdownBlock.Paragraph -> {
                        val fullText = block.runs.joinToString("") { it.text }
                        val words = fullText.split(" ")
                        var currentLine = ""
                        for (word in words) {
                            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
                            if (bodyPaint.measureText(testLine) > contentWidth) {
                                checkNewPage(14f)
                                canvas.drawText(currentLine, margin, currentY, bodyPaint)
                                currentY += 14f
                                currentLine = word
                            } else {
                                currentLine = testLine
                            }
                        }
                        if (currentLine.isNotEmpty()) {
                            checkNewPage(14f)
                            canvas.drawText(currentLine, margin, currentY, bodyPaint)
                            currentY += 18f
                        }
                    }

                    is MarkdownBlock.CodeBlock -> {
                        val codeLines = block.code.lines()
                        val blockHeight = codeLines.size * 13f + 12f
                        checkNewPage(blockHeight)

                        canvas.drawRect(
                            margin,
                            currentY - 10f,
                            pageWidth - margin,
                            currentY + blockHeight - 10f,
                            codeBgPaint
                        )

                        for (cLine in codeLines) {
                            val lineText = if (cLine.length > 80) cLine.take(80) + "..." else cLine
                            canvas.drawText(lineText, margin + 8f, currentY, codePaint)
                            currentY += 13f
                        }
                        currentY += 12f
                    }

                    is MarkdownBlock.BlockQuote -> {
                        checkNewPage(20f)
                        val quotePaint = Paint(bodyPaint).apply {
                            color = AndroidColor.parseColor("#475569")
                            typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                        }
                        val borderPaint = Paint().apply {
                            color = AndroidColor.parseColor("#2563EB")
                            strokeWidth = 3f
                        }
                        canvas.drawLine(margin, currentY - 10f, margin, currentY + 10f, borderPaint)
                        canvas.drawText(block.text.take(90), margin + 12f, currentY, quotePaint)
                        currentY += 20f
                    }

                    is MarkdownBlock.UnorderedList -> {
                        for (item in block.items) {
                            checkNewPage(14f)
                            val itemText = item.joinToString("") { it.text }
                            canvas.drawText("• $itemText", margin + 10f, currentY, bodyPaint)
                            currentY += 14f
                        }
                        currentY += 6f
                    }

                    is MarkdownBlock.OrderedList -> {
                        for (item in block.items) {
                            checkNewPage(14f)
                            val itemText = item.second.joinToString("") { it.text }
                            canvas.drawText("${item.first} $itemText", margin + 10f, currentY, bodyPaint)
                            currentY += 14f
                        }
                        currentY += 6f
                    }

                    is MarkdownBlock.TaskList -> {
                        for (item in block.items) {
                            checkNewPage(14f)
                            val box = if (item.checked) "[x]" else "[ ]"
                            val itemText = item.runs.joinToString("") { it.text }
                            canvas.drawText("$box $itemText", margin + 10f, currentY, bodyPaint)
                            currentY += 14f
                        }
                        currentY += 6f
                    }

                    is MarkdownBlock.Table -> {
                        checkNewPage(30f)
                        val tablePaint = Paint(bodyPaint).apply { textSize = 10f }
                        val headerText = block.headers.joinToString("  |  ")
                        canvas.drawText(headerText, margin, currentY, titlePaint)
                        currentY += 14f
                        for (row in block.rows) {
                            checkNewPage(14f)
                            canvas.drawText(row.joinToString("  |  "), margin, currentY, tablePaint)
                            currentY += 14f
                        }
                        currentY += 10f
                    }

                    is MarkdownBlock.HorizontalRule -> {
                        checkNewPage(16f)
                        canvas.drawLine(margin, currentY, pageWidth - margin, currentY, dividerPaint)
                        currentY += 16f
                    }

                    else -> {}
                }
            }

            // Draw Final Page Footer
            val footerPaint = Paint().apply {
                color = AndroidColor.GRAY
                textSize = 9f
                isAntiAlias = true
            }
            canvas.drawText("Page $pageNumber", pageWidth / 2f - 15f, pageHeight - margin, footerPaint)
            pdfDocument.finishPage(page)

            val pdfFile: File
            if (targetUri != null) {
                context.contentResolver.openOutputStream(targetUri)?.use { outputStream ->
                    pdfDocument.writeTo(outputStream)
                }
                pdfFile = File(context.cacheDir, "exported_doc.pdf")
            } else {
                val outputDir = context.getExternalFilesDir(null) ?: context.cacheDir
                pdfFile = File(outputDir, "${documentTitle.replace(" ", "_")}.pdf")
                FileOutputStream(pdfFile).use { out ->
                    pdfDocument.writeTo(out)
                }
            }

            pdfDocument.close()
            pdfFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
