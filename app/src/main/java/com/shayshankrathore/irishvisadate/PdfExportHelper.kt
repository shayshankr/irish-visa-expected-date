package com.shayshankrathore.irishvisadate

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.shayshankrathore.irishvisadate.ui.VisaType
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

object PdfExportHelper {

    private val fmt = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH)
    private fun LocalDate.fmt() = format(fmt)

    fun export(
        context: Context,
        visaType: VisaType,
        embassy: Embassy,
        vac: VacOption,
        submissionDate: LocalDate,
        earliest: LocalDate,
        latest: LocalDate,
        passportReturn: LocalDate,
    ) {
        val pdf      = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page     = pdf.startPage(pageInfo)
        drawPage(page.canvas, visaType, embassy, vac, submissionDate, earliest, latest, passportReturn)
        pdf.finishPage(page)

        val file = File(context.cacheDir, "visa_summary.pdf")
        try {
            file.outputStream().use { pdf.writeTo(it) }
        } finally {
            pdf.close()
        }

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        context.startActivity(
            Intent.createChooser(
                Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "Irish Visa Application Summary")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                },
                "Share PDF",
            )
        )
    }

    private fun drawPage(
        canvas: Canvas,
        visaType: VisaType,
        embassy: Embassy,
        vac: VacOption,
        submissionDate: LocalDate,
        earliest: LocalDate,
        latest: LocalDate,
        passportReturn: LocalDate,
    ) {
        val margin = 48f

        val header = Paint().apply {
            color     = Color.parseColor("#063320")
            isAntiAlias = true
        }
        canvas.drawRect(0f, 0f, 595f, 90f, header)

        val titlePaint = Paint().apply {
            color     = Color.WHITE
            textSize  = 22f
            isFakeBoldText = true
            isAntiAlias = true
        }
        canvas.drawText("Irish Visa Application Summary", margin, 42f, titlePaint)
        val subPaint = Paint().apply {
            color    = Color.parseColor("#A5D6A7")
            textSize = 12f
            isAntiAlias = true
        }
        canvas.drawText("Generated ${LocalDate.now().fmt()}", margin, 65f, subPaint)

        val accentBar = Paint().apply { color = Color.parseColor("#4CAF50") }
        canvas.drawRect(0f, 90f, 200f, 95f, accentBar)
        val accentBar2 = Paint().apply { color = Color.WHITE }
        canvas.drawRect(200f, 90f, 400f, 95f, accentBar2)
        val accentBar3 = Paint().apply { color = Color.parseColor("#FF8F00") }
        canvas.drawRect(400f, 90f, 595f, 95f, accentBar3)

        var y = 130f

        val sectionPaint = Paint().apply {
            color    = Color.parseColor("#063320")
            textSize = 10f
            isFakeBoldText = true
            letterSpacing = 0.1f
            isAntiAlias = true
        }
        val labelPaint = Paint().apply {
            color    = Color.parseColor("#666666")
            textSize = 11f
            isAntiAlias = true
        }
        val valuePaint = Paint().apply {
            color    = Color.parseColor("#1B2B1B")
            textSize = 14f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val smallPaint = Paint().apply {
            color    = Color.parseColor("#888888")
            textSize = 10f
            isAntiAlias = true
        }
        val divPaint = Paint().apply {
            color = Color.parseColor("#DDEEDC")
            strokeWidth = 1f
        }

        fun section(title: String) {
            canvas.drawText(title, margin, y, sectionPaint)
            y += 6f
            canvas.drawLine(margin, y, 595f - margin, y, divPaint)
            y += 14f
        }
        fun row(label: String, value: String, note: String = "") {
            canvas.drawText(label, margin, y, labelPaint)
            canvas.drawText(value, margin + 160f, y, valuePaint)
            y += 4f
            if (note.isNotBlank()) {
                canvas.drawText(note, margin + 160f, y + 14f, smallPaint)
                y += 14f
            }
            y += 20f
        }

        section("APPLICATION DETAILS")
        row("Embassy",       "${embassy.flag} ${embassy.label}")
        row("VAC Location",  "${vac.label} (${vac.cities})")
        row("Visa Type",     visaType.label)
        row("Application Fee", "€${visaType.feeEur} (non-refundable)")
        row("Submitted",     submissionDate.fmt())
        y += 8f

        section("EXPECTED DECISION WINDOW")
        row("Earliest Decision", earliest.fmt())
        row("Latest Decision",   latest.fmt(), "Incl. ${vac.transitDays} working day(s) VAC transit")
        row("Passport Return",   "~${passportReturn.fmt()}", "Incl. ${embassy.courierDays} working day(s) courier return")
        y += 8f

        section("CONTACT")
        row("Email",   embassy.contact.email)
        row("Phone",   embassy.contact.phone)
        row("Address", embassy.contact.address)
        y += 8f

        val footerPaint = Paint().apply {
            color    = Color.parseColor("#AAAAAA")
            textSize = 9f
            isAntiAlias = true
        }
        canvas.drawText("Generated by Irish Visa Tracker — verify all details at ireland.ie", margin, 810f, footerPaint)
    }
}
