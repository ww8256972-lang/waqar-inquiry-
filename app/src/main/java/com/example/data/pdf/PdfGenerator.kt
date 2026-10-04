package com.example.data.pdf

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.local.entity.AdmissionEntity
import com.example.data.local.entity.InvoiceEntity
import com.example.data.local.entity.PaymentEntity
import com.example.data.local.entity.QuotationEntity
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfGenerator {

    private val rupeeFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale.Builder().setLanguage("en").setRegion("IN").build())
    private val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    private val dateOnlyFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())

    fun formatRupee(amount: Double): String {
        return "₹ " + String.format(Locale.US, "%,.2f", amount)
    }

    fun generatePaymentReceipt(context: Context, payment: PaymentEntity): File {
        val pdfDir = File(context.cacheDir, "pdf").apply { mkdirs() }
        val file = File(pdfDir, "Receipt_${payment.id.take(8)}.pdf")

        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Background & Header bar
        paint.color = Color.parseColor("#0D1B2A")
        canvas.drawRect(0f, 0f, 595f, 110f, paint)

        // Title
        paint.color = Color.WHITE
        paint.textSize = 20f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("WAQAR WEBSITE INQUIRY", 40f, 48f, paint)

        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Official Payment Receipt & Admission Acknowledgment", 40f, 72f, paint)
        canvas.drawText("Verified Reference & Audit Copy", 40f, 90f, paint)

        // Receipt Badge
        paint.color = Color.parseColor("#415A77")
        canvas.drawRoundRect(420f, 32f, 555f, 82f, 8f, 8f, paint)
        paint.color = Color.WHITE
        paint.textSize = 14f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("PAYMENT RECEIPT", 432f, 54f, paint)
        paint.textSize = 10f
        canvas.drawText(payment.id.take(12).uppercase(), 438f, 72f, paint)

        // Customer & Payment Info
        paint.color = Color.parseColor("#1B263B")
        paint.textSize = 13f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

        var y = 145f
        canvas.drawText("RECEIPT DETAILS", 40f, y, paint)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

        // Divider
        paint.color = Color.LTGRAY
        paint.strokeWidth = 1.2f
        canvas.drawLine(40f, y + 6f, 555f, y + 6f, paint)

        y += 30f
        paint.color = Color.DKGRAY
        canvas.drawText("Received From:", 40f, y, paint)
        paint.color = Color.BLACK
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(payment.customerName, 170f, y, paint)

        y += 24f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = Color.DKGRAY
        canvas.drawText("Payment Date:", 40f, y, paint)
        paint.color = Color.BLACK
        canvas.drawText(dateFormat.format(Date(payment.paymentDate)), 170f, y, paint)

        y += 24f
        paint.color = Color.DKGRAY
        canvas.drawText("Payment Method:", 40f, y, paint)
        paint.color = Color.BLACK
        canvas.drawText(payment.paymentMethod, 170f, y, paint)

        y += 24f
        paint.color = Color.DKGRAY
        canvas.drawText("Transaction / UTR Ref:", 40f, y, paint)
        paint.color = Color.BLACK
        canvas.drawText(payment.transactionReference.ifBlank { "N/A" }, 170f, y, paint)

        y += 24f
        paint.color = Color.DKGRAY
        canvas.drawText("Associated Record Type:", 40f, y, paint)
        paint.color = Color.BLACK
        canvas.drawText("${payment.recordType} (${payment.recordId.take(8)})", 170f, y, paint)

        // Highlight Amount Box
        y += 40f
        paint.color = Color.parseColor("#E0E1DD")
        canvas.drawRoundRect(40f, y, 555f, y + 70f, 10f, 10f, paint)

        paint.color = Color.parseColor("#0D1B2A")
        paint.textSize = 14f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("TOTAL AMOUNT RECEIVED", 60f, y + 32f, paint)

        paint.textSize = 22f
        paint.color = Color.parseColor("#1B263B")
        canvas.drawText(formatRupee(payment.amount), 60f, y + 58f, paint)

        // Notes
        y += 110f
        if (payment.notes.isNotBlank()) {
            paint.textSize = 12f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.color = Color.DKGRAY
            canvas.drawText("Notes / Remarks:", 40f, y, paint)
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText(payment.notes, 40f, y + 18f, paint)
            y += 40f
        }

        // Terms & Authorized Signatory
        y = 700f
        paint.color = Color.LTGRAY
        canvas.drawLine(40f, y, 555f, y, paint)

        y += 20f
        paint.color = Color.GRAY
        paint.textSize = 10f
        canvas.drawText("• This is a computer generated official payment acknowledgment.", 40f, y, paint)
        canvas.drawText("• For inquiries or admission support, reach Waqar at official inquiry contact.", 40f, y + 14f, paint)

        // Signatory box
        paint.color = Color.BLACK
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Authorized Signatory", 420f, y + 40f, paint)
        canvas.drawLine(410f, y + 30f, 545f, y + 30f, paint)
        paint.color = Color.DKGRAY
        canvas.drawText("Waqar Management", 420f, y + 56f, paint)

        document.finishPage(page)

        FileOutputStream(file).use { out ->
            document.writeTo(out)
        }
        document.close()
        return file
    }

    fun generateInvoicePdf(context: Context, invoice: InvoiceEntity): File {
        val pdfDir = File(context.cacheDir, "pdf").apply { mkdirs() }
        val file = File(pdfDir, "${invoice.invoiceNumber}.pdf")

        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Top Header
        paint.color = Color.parseColor("#0D1B2A")
        canvas.drawRect(0f, 0f, 595f, 100f, paint)

        paint.color = Color.WHITE
        paint.textSize = 20f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("WAQAR WEBSITE INQUIRY", 40f, 45f, paint)
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Tax Invoice & Billing Statement", 40f, 68f, paint)

        // Invoice Number
        paint.textSize = 14f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("INVOICE #${invoice.invoiceNumber}", 380f, 45f, paint)
        paint.textSize = 10f
        canvas.drawText("Date: ${dateOnlyFormat.format(Date(invoice.invoiceDate))}", 380f, 65f, paint)
        canvas.drawText("Due: ${dateOnlyFormat.format(Date(invoice.dueDate))}", 380f, 80f, paint)

        // Customer Info
        var y = 140f
        paint.color = Color.DKGRAY
        paint.textSize = 11f
        canvas.drawText("Billed To:", 40f, y, paint)
        paint.color = Color.BLACK
        paint.textSize = 14f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(invoice.customerName, 40f, y + 18f, paint)
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Phone: ${invoice.customerPhone}", 40f, y + 36f, paint)
        canvas.drawText("Address: ${invoice.customerAddress.ifBlank { "Not Specified" }}", 40f, y + 52f, paint)

        // Invoice Summary Table
        y += 85f
        paint.color = Color.parseColor("#1B263B")
        canvas.drawRect(40f, y, 555f, y + 26f, paint)
        paint.color = Color.WHITE
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Description", 50f, y + 18f, paint)
        canvas.drawText("Amount", 480f, y + 18f, paint)

        y += 45f
        paint.color = Color.BLACK
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Inquiry & Admission Processing Services", 50f, y, paint)
        canvas.drawText(formatRupee(invoice.subtotal), 470f, y, paint)

        // Calculation rows
        y += 60f
        paint.color = Color.LTGRAY
        canvas.drawLine(40f, y, 555f, y, paint)

        y += 24f
        paint.color = Color.DKGRAY
        canvas.drawText("Subtotal:", 350f, y, paint)
        canvas.drawText(formatRupee(invoice.subtotal), 470f, y, paint)

        y += 20f
        canvas.drawText("Tax (${invoice.taxRate}%):", 350f, y, paint)
        canvas.drawText(formatRupee(invoice.taxAmount), 470f, y, paint)

        if (invoice.discount > 0) {
            y += 20f
            canvas.drawText("Discount:", 350f, y, paint)
            canvas.drawText("- " + formatRupee(invoice.discount), 470f, y, paint)
        }

        y += 24f
        paint.color = Color.parseColor("#0D1B2A")
        paint.textSize = 13f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Grand Total:", 350f, y, paint)
        canvas.drawText(formatRupee(invoice.grandTotal), 470f, y, paint)

        y += 22f
        paint.color = Color.parseColor("#1B263B")
        canvas.drawText("Amount Paid:", 350f, y, paint)
        canvas.drawText(formatRupee(invoice.amountPaid), 470f, y, paint)

        y += 22f
        paint.color = if (invoice.remainingAmount > 0) Color.RED else Color.parseColor("#2E7D32")
        canvas.drawText("Remaining Due:", 350f, y, paint)
        canvas.drawText(formatRupee(invoice.remainingAmount), 470f, y, paint)

        // Terms
        y = 680f
        paint.color = Color.DKGRAY
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("TERMS & CONDITIONS", 40f, y, paint)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText(invoice.terms, 40f, y + 16f, paint)

        // Signature
        canvas.drawLine(410f, y + 45f, 545f, y + 45f, paint)
        paint.color = Color.BLACK
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(invoice.signatureName, 410f, y + 60f, paint)

        document.finishPage(page)
        FileOutputStream(file).use { out -> document.writeTo(out) }
        document.close()
        return file
    }

    fun sharePdf(context: Context, pdfFile: File, title: String = "Share Document") {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", pdfFile)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(intent, title).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }
}
