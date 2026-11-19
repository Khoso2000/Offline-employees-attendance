package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import androidx.core.content.FileProvider
import com.example.data.model.CompanySettings
import com.example.domain.DateUtils
import java.io.File
import java.io.FileOutputStream

data class ReportTableData(
    val title: String,
    val subtitle: String,
    val headers: List<String>,
    val rows: List<List<String>>,
    val summaryLines: List<String> = emptyList()
)

object ReportExporter {

    fun copyUriToInternalFile(context: Context, uri: Uri, prefix: String): String? {
        return runCatching {
            val dir = File(context.filesDir, "media").apply { mkdirs() }
            val outFile = File(dir, "${prefix}_${System.currentTimeMillis()}.jpg")
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(outFile).use { output ->
                    input.copyTo(output)
                }
            }
            outFile.absolutePath
        }.getOrNull()
    }

    fun generateCsvString(table: ReportTableData): String {
        val sb = StringBuilder()
        sb.appendLine(escapeCsvRow(listOf(table.title)))
        sb.appendLine(escapeCsvRow(listOf(table.subtitle)))
        sb.appendLine()
        sb.appendLine(escapeCsvRow(table.headers))
        table.rows.forEach { row ->
            sb.appendLine(escapeCsvRow(row))
        }
        if (table.summaryLines.isNotEmpty()) {
            sb.appendLine()
            table.summaryLines.forEach { line ->
                sb.appendLine(escapeCsvRow(listOf(line)))
            }
        }
        return sb.toString()
    }

    private fun escapeCsvRow(cells: List<String>): String =
        cells.joinToString(",") { cell ->
            val escaped = cell.replace("\"", "\"\"")
            "\"$escaped\""
        }

    fun buildPdfBytes(table: ReportTableData, settings: CompanySettings): ByteArray {
        val doc = PdfDocument()
        val pageWidth = 595 // A4 points
        val pageHeight = 842
        val margin = 36f
        val contentWidth = pageWidth - margin * 2

        val headerBgPaint = Paint().apply { color = Color.rgb(15, 39, 74) }
        val zebraPaint = Paint().apply { color = Color.rgb(244, 247, 251) }
        val borderPaint = Paint().apply {
            color = Color.rgb(208, 219, 232)
            strokeWidth = 1f
        }
        val titlePaint = Paint().apply {
            color = Color.WHITE
            textSize = 16f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val subTitlePaint = Paint().apply {
            color = Color.rgb(214, 227, 255)
            textSize = 10f
            isAntiAlias = true
        }
        val metaPaint = Paint().apply {
            color = Color.rgb(71, 85, 105)
            textSize = 9.5f
            isAntiAlias = true
        }
        val thPaint = Paint().apply {
            color = Color.rgb(15, 39, 74)
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val tdPaint = Paint().apply {
            color = Color.rgb(15, 23, 42)
            textSize = 9f
            isAntiAlias = true
        }
        val summaryPaint = Paint().apply {
            color = Color.rgb(15, 39, 74)
            textSize = 10.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val colCount = table.headers.size.coerceAtLeast(1)
        val colWidth = contentWidth / colCount
        val rowHeight = 22f
        var pageNumber = 1
        var rowIndex = 0

        fun drawPageHeader(canvas: Canvas, pageNum: Int) {
            canvas.drawRect(0f, 0f, pageWidth.toFloat(), 74f, headerBgPaint)
            canvas.drawText(settings.companyName, margin, 30f, titlePaint)
            canvas.drawText("${table.title}  •  ${table.subtitle}", margin, 50f, subTitlePaint)
            canvas.drawText(
                "Generated: ${DateUtils.formatTimestamp(System.currentTimeMillis())}   |   Page $pageNum",
                margin,
                92f,
                metaPaint
            )
            // Table header row
            val topY = 106f
            canvas.drawLine(margin, topY, pageWidth - margin, topY, borderPaint)
            table.headers.forEachIndexed { idx, h ->
                val x = margin + idx * colWidth + 4f
                canvas.drawText(truncateText(h, colWidth - 8f, thPaint), x, topY + 15f, thPaint)
            }
            canvas.drawLine(margin, topY + rowHeight, pageWidth - margin, topY + rowHeight, borderPaint)
        }

        while (rowIndex < table.rows.size || pageNumber == 1) {
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            val page = doc.startPage(pageInfo)
            val canvas = page.canvas
            drawPageHeader(canvas, pageNumber)

            var y = 106f + rowHeight
            val maxY = pageHeight - 80f

            while (rowIndex < table.rows.size && y + rowHeight <= maxY) {
                if (rowIndex % 2 == 1) {
                    canvas.drawRect(margin, y, pageWidth - margin, y + rowHeight, zebraPaint)
                }
                val row = table.rows[rowIndex]
                row.forEachIndexed { idx, cell ->
                    if (idx < colCount) {
                        val x = margin + idx * colWidth + 4f
                        canvas.drawText(truncateText(cell, colWidth - 8f, tdPaint), x, y + 15f, tdPaint)
                    }
                }
                y += rowHeight
                canvas.drawLine(margin, y, pageWidth - margin, y, borderPaint)
                rowIndex++
            }

            if (rowIndex >= table.rows.size && table.summaryLines.isNotEmpty()) {
                y += 16f
                table.summaryLines.forEach { line ->
                    if (y < pageHeight - 30f) {
                        canvas.drawText(line, margin, y, summaryPaint)
                        y += 16f
                    }
                }
            }

            doc.finishPage(page)
            pageNumber++
            if (rowIndex >= table.rows.size) break
        }

        val stream = java.io.ByteArrayOutputStream()
        doc.writeTo(stream)
        doc.close()
        return stream.toByteArray()
    }

    private fun truncateText(text: String, maxWidth: Float, paint: Paint): String {
        if (paint.measureText(text) <= maxWidth) return text
        var end = text.length
        while (end > 1 && paint.measureText(text.substring(0, end) + "…") > maxWidth) {
            end--
        }
        return if (end <= 1) text.take(1) else text.substring(0, end) + "…"
    }

    fun shareFile(context: Context, fileName: String, mimeType: String, bytes: ByteArray) {
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(exportDir, fileName)
        FileOutputStream(file).use { it.write(bytes) }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share $fileName"))
    }

    fun printReport(context: Context, table: ReportTableData, settings: CompanySettings) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
        val pdfBytes = buildPdfBytes(table, settings)
        val jobName = "${settings.companyName} - ${table.title}"

        val adapter = object : PrintDocumentAdapter() {
            override fun onLayout(
                oldAttributes: PrintAttributes?,
                newAttributes: PrintAttributes,
                cancellationSignal: CancellationSignal?,
                callback: LayoutResultCallback,
                extras: Bundle?
            ) {
                if (cancellationSignal?.isCanceled == true) {
                    callback.onLayoutCancelled()
                    return
                }
                val info = PrintDocumentInfo.Builder(
                    "${table.title.replace(" ", "_")}.pdf"
                )
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .setPageCount(PrintDocumentInfo.PAGE_COUNT_UNKNOWN)
                    .build()
                callback.onLayoutFinished(info, true)
            }

            override fun onWrite(
                pages: Array<out PageRange>?,
                destination: ParcelFileDescriptor,
                cancellationSignal: CancellationSignal?,
                callback: WriteResultCallback
            ) {
                if (cancellationSignal?.isCanceled == true) {
                    callback.onWriteCancelled()
                    return
                }
                runCatching {
                    FileOutputStream(destination.fileDescriptor).use { out ->
                        out.write(pdfBytes)
                    }
                    callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                }.onFailure {
                    callback.onWriteFailed(it.message)
                }
            }
        }
        printManager.print(jobName, adapter, PrintAttributes.Builder().build())
    }
}
