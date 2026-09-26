package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.print.PrintAttributes
import android.print.PrintManager
import androidx.core.content.FileProvider
import com.example.data.entity.CustomerEntity
import com.example.data.entity.CustomerLedgerEntity
import com.example.data.entity.SaleEntity
import com.example.data.entity.SaleItemEntity
import com.example.data.entity.ShopSettingsEntity
import java.io.File
import java.io.FileOutputStream

object PdfInvoiceHelper {

    fun generateSaleInvoicePdf(
        context: Context,
        settings: ShopSettingsEntity,
        sale: SaleEntity,
        items: List<SaleItemEntity>
    ): File? {
        val document = PdfDocument()
        val is80mm = settings.thermalPrinterWidth == 80
        val pageWidth = if (is80mm) 576 else 384 // 80mm ~ 576 points, 58mm ~ 384 points
        val dynamicHeight = 350 + (items.size * 30) + 200

        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, dynamicHeight, 1).create()
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val paint = Paint().apply {
            color = Color.BLACK
            isAntiAlias = true
        }

        var y = 35f

        // Shop Header
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 20f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(settings.shopName, pageWidth / 2f, y, paint)

        y += 20f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 12f
        if (settings.shopTagline.isNotBlank()) {
            canvas.drawText(settings.shopTagline, pageWidth / 2f, y, paint)
            y += 18f
        }
        if (settings.address.isNotBlank()) {
            canvas.drawText(settings.address, pageWidth / 2f, y, paint)
            y += 18f
        }
        if (settings.phone.isNotBlank()) {
            canvas.drawText("Tel: ${settings.phone}", pageWidth / 2f, y, paint)
            y += 18f
        }

        // Divider
        y += 5f
        paint.strokeWidth = 1.5f
        canvas.drawLine(20f, y, pageWidth - 20f, y, paint)
        y += 18f

        // Invoice Meta
        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 11f
        canvas.drawText("Invoice #: ${sale.invoiceNumber}", 20f, y, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Date: ${CurrencyFormatter.formatDateOnly(sale.createdAt)}", pageWidth - 20f, y, paint)

        y += 16f
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("Customer: ${sale.customerName}", 20f, y, paint)
        if (sale.customerPhone.isNotBlank()) {
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText("Phone: ${sale.customerPhone}", pageWidth - 20f, y, paint)
        }

        // Table Header
        y += 18f
        paint.strokeWidth = 1f
        canvas.drawLine(20f, y, pageWidth - 20f, y, paint)
        y += 15f

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("Item", 20f, y, paint)
        canvas.drawText("Qty", pageWidth * 0.52f, y, paint)
        canvas.drawText("Rate", pageWidth * 0.68f, y, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Total", pageWidth - 20f, y, paint)

        y += 8f
        canvas.drawLine(20f, y, pageWidth - 20f, y, paint)
        y += 18f

        // Table Items
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        for (item in items) {
            paint.textAlign = Paint.Align.LEFT
            val itemName = if (item.productName.length > 20) item.productName.take(19) + "…" else item.productName
            canvas.drawText(itemName, 20f, y, paint)

            canvas.drawText("${item.quantity.toInt()} ${item.unit}", pageWidth * 0.52f, y, paint)
            canvas.drawText("${item.unitPrice.toInt()}", pageWidth * 0.68f, y, paint)

            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText("${item.lineTotal.toInt()}", pageWidth - 20f, y, paint)
            y += 20f
        }

        // Divider
        paint.strokeWidth = 1f
        canvas.drawLine(20f, y, pageWidth - 20f, y, paint)
        y += 18f

        // Totals
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("Subtotal:", pageWidth * 0.50f, y, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText(CurrencyFormatter.formatPkr(sale.subtotal, settings.currencySymbol), pageWidth - 20f, y, paint)

        if (sale.discountAmount > 0) {
            y += 18f
            paint.textAlign = Paint.Align.LEFT
            canvas.drawText("Discount:", pageWidth * 0.50f, y, paint)
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText("-${CurrencyFormatter.formatPkr(sale.discountAmount, settings.currencySymbol)}", pageWidth - 20f, y, paint)
        }

        y += 20f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 13f
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("Grand Total:", pageWidth * 0.50f, y, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText(CurrencyFormatter.formatPkr(sale.grandTotal, settings.currencySymbol), pageWidth - 20f, y, paint)

        y += 18f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 11f
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("Paid (${sale.paymentMethod}):", pageWidth * 0.50f, y, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText(CurrencyFormatter.formatPkr(sale.paidAmount, settings.currencySymbol), pageWidth - 20f, y, paint)

        if (sale.remainingAmount > 0) {
            y += 18f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textAlign = Paint.Align.LEFT
            canvas.drawText("Remaining (Udhaar):", pageWidth * 0.50f, y, paint)
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText(CurrencyFormatter.formatPkr(sale.remainingAmount, settings.currencySymbol), pageWidth - 20f, y, paint)
        }

        // Footer Thank You
        y += 35f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = 12f
        canvas.drawText("Thank you for your visit!", pageWidth / 2f, y, paint)
        y += 16f
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("تشریف آوری کا شکریہ! برائے مہربانی رسید سنبھال کر رکھیں۔", pageWidth / 2f, y, paint)

        document.finishPage(page)

        return try {
            val cacheDir = File(context.cacheDir, "invoices")
            if (!cacheDir.exists()) cacheDir.mkdirs()
            val file = File(cacheDir, "${sale.invoiceNumber}.pdf")
            val outputStream = FileOutputStream(file)
            document.writeTo(outputStream)
            document.close()
            outputStream.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            document.close()
            null
        }
    }

    fun sharePdf(context: Context, pdfFile: File) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Share Invoice PDF"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun shareViaWhatsApp(context: Context, phone: String, message: String) {
        try {
            val cleanPhone = phone.replace(Regex("[^0-9]"), "")
            val finalNumber = if (cleanPhone.startsWith("0")) "92" + cleanPhone.substring(1) else cleanPhone
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://api.whatsapp.com/send?phone=$finalNumber&text=${Uri.encode(message)}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
