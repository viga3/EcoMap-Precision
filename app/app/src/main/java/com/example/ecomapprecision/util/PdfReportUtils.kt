package com.example.ecomapprecision.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.ecomapprecision.data.model.Segnalazione
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfReportUtils {

    fun generateAndSharePdfReport(context: Context, segnalazioni: List<Segnalazione>) {
        if (segnalazioni.isEmpty()) {
            Toast.makeText(context, "Nessuna segnalazione da esportare", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 portrait (595x842 pt)
            val page = pdfDocument.startPage(pageInfo)
            val canvas: Canvas = page.canvas

            val paint = Paint().apply {
                isAntiAlias = true
            }

            // Header Background Accent
            paint.color = Color.parseColor("#0F766E") // Dark Teal Header
            canvas.drawRect(0f, 0f, 595f, 90f, paint)

            // Header Title
            paint.color = Color.WHITE
            paint.textSize = 22f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("EcoMap - Report Segnalazioni", 30f, 42f, paint)

            paint.textSize = 12f
            paint.typeface = Typeface.DEFAULT
            val nowStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ITALIAN).format(Date())
            canvas.drawText("Generato il: $nowStr  |  Operatore: Villani Michele", 30f, 68f, paint)

            // Document Info Subtitle
            paint.color = Color.parseColor("#1F2937")
            paint.textSize = 14f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("Riepilogo Segnalazioni Rifiuti (${segnalazioni.size} Totali)", 30f, 120f, paint)

            var yPos = 150f

            // Table Header Row
            paint.color = Color.parseColor("#E5E7EB")
            canvas.drawRect(30f, yPos - 15f, 565f, yPos + 10f, paint)

            paint.color = Color.parseColor("#374151")
            paint.textSize = 10f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

            canvas.drawText("DATA", 40f, yPos, paint)
            canvas.drawText("TIPO", 120f, yPos, paint)
            canvas.drawText("N.", 235f, yPos, paint)
            canvas.drawText("INDIRIZZO", 270f, yPos, paint)
            canvas.drawText("STATO", 485f, yPos, paint)

            yPos += 24f

            // Rows Loop
            paint.typeface = Typeface.DEFAULT
            val dateFormat = SimpleDateFormat("dd/MM/yy HH:mm", Locale.ITALIAN)

            for ((index, seg) in segnalazioni.withIndex()) {
                if (yPos > 800f) break // Avoid page overflow on single page preview

                // Alternate row background
                if (index % 2 == 1) {
                    paint.color = Color.parseColor("#F9FAFB")
                    canvas.drawRect(30f, yPos - 14f, 565f, yPos + 10f, paint)
                }

                // Date (ID rimosso come richiesto)
                paint.color = Color.parseColor("#111827")
                paint.typeface = Typeface.DEFAULT
                val dateStr = dateFormat.format(Date(seg.dataCreazione))
                canvas.drawText(dateStr, 40f, yPos, paint)

                // Tipo
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText(seg.tipo.title, 120f, yPos, paint)

                // N. (Quantità / Totale)
                val totCount = if (seg.tipo == com.example.ecomapprecision.data.model.TipoSegnalazione.BOMBOLE) {
                    val sum = seg.bomboleRossa + seg.bomboleBlu + seg.bomboleGrigia + seg.bomboleVerde
                    if (sum > 0) sum.toString() else "1"
                } else {
                    "1"
                }
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.color = Color.parseColor("#0F766E")
                canvas.drawText(totCount, 238f, yPos, paint)

                // Indirizzo
                paint.color = Color.parseColor("#111827")
                paint.typeface = Typeface.DEFAULT
                val shortAddr = if (seg.indirizzo.length > 32) seg.indirizzo.substring(0, 29) + "..." else seg.indirizzo
                canvas.drawText(shortAddr, 270f, yPos, paint)

                // Stato Badge Text
                val (statusText, statusHex) = when (seg.stato.name) {
                    "NUOVO" -> "NUOVO" to "#DC2626"
                    "IN_CARICO" -> "IN CARICO" to "#D97706"
                    else -> "RISOLTO" to "#059669"
                }
                paint.color = Color.parseColor(statusHex)
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText(statusText, 485f, yPos, paint)

                yPos += 22f
            }

            // Footer Line
            paint.color = Color.parseColor("#9CA3AF")
            canvas.drawLine(30f, 810f, 565f, 810f, paint)
            paint.textSize = 9f
            paint.color = Color.parseColor("#6B7280")
            paint.typeface = Typeface.DEFAULT
            canvas.drawText("EcoMap Precision v2.1.0b by Michele Villani - Documento Ufficiale Gestione Rifiuti", 30f, 825f, paint)

            pdfDocument.finishPage(page)

            // Save PDF to cache directory
            val cacheDir = File(context.cacheDir, "reports")
            if (!cacheDir.exists()) cacheDir.mkdirs()

            val pdfFile = File(cacheDir, "Report_Segnalazioni_EcoMap.pdf")
            val outputStream = FileOutputStream(pdfFile)
            pdfDocument.writeTo(outputStream)
            outputStream.close()
            pdfDocument.close()

            // Launch Share Intent using FileProvider
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Report Segnalazioni EcoMap")
                putExtra(Intent.EXTRA_TEXT, "In allegato il report PDF delle segnalazioni rifiuti EcoMap.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(shareIntent, "Condividi Report PDF"))

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Errore generazione PDF: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }
}
