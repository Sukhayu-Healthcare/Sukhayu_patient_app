package com.sukhayu.patient.utils

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.widget.Toast
import com.sukhayu.patient.data.model.ConsultationWithItems
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale

object PdfDownloadHelper {

    private fun safeFormatDate(dateString: String?): String {
        return try {
            if (dateString.isNullOrEmpty()) {
                "N/A"
            } else {
                val inputFormats = arrayOf(
                    "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                    "yyyy-MM-dd'T'HH:mm:ss",
                    "yyyy-MM-dd HH:mm:ss",
                    "yyyy-MM-dd"
                )
                var parsed: java.util.Date? = null
                for (f in inputFormats) {
                    try {
                        parsed = SimpleDateFormat(f, Locale.getDefault()).parse(dateString)
                        if (parsed != null) break
                    } catch (_: Exception) { /* try next */ }
                }
                parsed?.let {
                    SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(it)
                } ?: dateString
            }
        } catch (e: Exception) {
            "N/A"
        }
    }

    fun generateAndDownloadPdf(context: Context, consultation: ConsultationWithItems) {
        try {
            // Basic page config (A4-like)
            val pageWidth = 595
            val pageHeight = 842
            val margin = 40
            val lineHeight = 18

            val pdf = PdfDocument()

            var pageNumber = 1
            var y = margin

            fun startNewPage(): PdfDocument.Page {
                val info = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                val page = pdf.startPage(info)
                y = margin
                pageNumber++
                return page
            }

            var page = startNewPage()
            val canvas = page.canvas
            val titlePaint = Paint().apply {
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textSize = 18f
                isFakeBoldText = true
            }
            val normalPaint = Paint().apply {
                textSize = 12f
            }
            val smallPaint = Paint().apply {
                textSize = 10f
            }

            // Title (centered)
            val title = "Consultation Report"
            val titleWidth = titlePaint.measureText(title)
            canvas.drawText(title, (pageWidth - titleWidth) / 2f, y.toFloat(), titlePaint)
            y += (lineHeight * 2)

            // Consultation details
            canvas.drawText(
                "Doctor: ${consultation.doctor_name ?: "Dr. ID ${consultation.doctor_id}"}",
                margin.toFloat(),
                y.toFloat(),
                normalPaint
            )
            y += lineHeight
            canvas.drawText(
                "Phone: ${consultation.doctor_phone ?: "N/A"}",
                margin.toFloat(),
                y.toFloat(),
                normalPaint
            )
            y += lineHeight
            canvas.drawText(
                "Date: ${safeFormatDate(consultation.consultation_date)}",
                margin.toFloat(),
                y.toFloat(),
                normalPaint
            )
            y += lineHeight
            canvas.drawText(
                "Diagnosis: ${consultation.diagnosis ?: "Not specified"}",
                margin.toFloat(),
                y.toFloat(),
                normalPaint
            )
            y += lineHeight
            canvas.drawText(
                "Notes: ${consultation.notes ?: "No Notes"}",
                margin.toFloat(),
                y.toFloat(),
                normalPaint
            )
            y += (lineHeight * 2)

            // Prescription header
            canvas.drawText("Prescription:", margin.toFloat(), y.toFloat(), titlePaint)
            y += (lineHeight * 1.5).toInt()

            // Table column positions
            val col1 = margin
            val col2 = margin + 160
            val col3 = margin + 300
            val col4 = margin + 420
            val col5 = margin + 480

            // Header row (bold)
            canvas.drawText("Medicine", col1.toFloat(), y.toFloat(), titlePaint)
            canvas.drawText("Dosage", col2.toFloat(), y.toFloat(), titlePaint)
            canvas.drawText("Freq", col3.toFloat(), y.toFloat(), titlePaint)
            canvas.drawText("Duration", col4.toFloat(), y.toFloat(), titlePaint)
            canvas.drawText("Instr", col5.toFloat(), y.toFloat(), titlePaint)
            y += lineHeight

            if (consultation.items.isEmpty()) {
                // No items
                canvas.drawText("No prescription items", margin.toFloat(), y.toFloat(), smallPaint)
                y += lineHeight
            } else {
                consultation.items.forEach { item ->
                    // Check page overflow
                    if (y > pageHeight - margin) {
                        pdf.finishPage(page)
                        page = startNewPage()
                    }
                    // Medicine (wrap if needed)
                    canvas.drawText(item.medicine_name, col1.toFloat(), y.toFloat(), smallPaint)
                    canvas.drawText(item.dosage ?: "-", col2.toFloat(), y.toFloat(), smallPaint)
                    canvas.drawText(item.frequency ?: "-", col3.toFloat(), y.toFloat(), smallPaint)
                    canvas.drawText(item.duration ?: "-", col4.toFloat(), y.toFloat(), smallPaint)
                    // instructions may be long — draw as multi-line if needed
                    val instr = item.instructions ?: "-"
                    val instrMaxWidth = pageWidth - col5 - margin
                    val instrPaint = smallPaint
                    val words = instr.split(" ")
                    var instrLine = ""
                    var instrY = y
                    var firstInstrLine = true
                    for (w in words) {
                        val candidate = if (instrLine.isEmpty()) w else "$instrLine $w"
                        if (instrPaint.measureText(candidate) > instrMaxWidth) {
                            // draw current instrLine
                            if (firstInstrLine) {
                                canvas.drawText(instrLine, col5.toFloat(), instrY.toFloat(), instrPaint)
                                firstInstrLine = false
                            } else {
                                // subsequent lines start at col1 to keep readable
                                instrY += lineHeight
                                if (instrY > pageHeight - margin) {
                                    pdf.finishPage(page)
                                    page = startNewPage()
                                    instrY = y
                                }
                                canvas.drawText(instrLine, col1.toFloat(), instrY.toFloat(), instrPaint)
                            }
                            instrLine = w
                        } else {
                            instrLine = candidate
                        }
                    }
                    // draw remaining instrLine
                    if (instrLine.isNotEmpty()) {
                        if (firstInstrLine) {
                            canvas.drawText(instrLine, col5.toFloat(), y.toFloat(), instrPaint)
                        } else {
                            instrY += lineHeight
                            if (instrY > pageHeight - margin) {
                                pdf.finishPage(page)
                                page = startNewPage()
                                instrY = y
                            }
                            canvas.drawText(instrLine, col1.toFloat(), instrY.toFloat(), instrPaint)
                        }
                    }

                    y += lineHeight
                }
            }

            // Finish current page
            pdf.finishPage(page)

            // Write file to Downloads
            val downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloads.exists()) downloads.mkdirs()
            val fileName = "Consultation_${consultation.consultation_id}_${System.currentTimeMillis()}.pdf"
            val file = File(downloads, fileName)
            val fos = FileOutputStream(file)
            pdf.writeTo(fos)
            fos.flush()
            fos.close()
            pdf.close()

            Toast.makeText(context, "PDF saved to Downloads: $fileName", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error generating PDF: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
