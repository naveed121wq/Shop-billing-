package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.entity.SaleEntity
import com.example.data.entity.SaleItemEntity
import com.example.data.entity.ShopSettingsEntity
import com.example.ui.theme.FinancialProfitGreen
import com.example.ui.theme.PrimaryGreen
import com.example.util.CurrencyFormatter
import com.example.util.PdfInvoiceHelper

@Composable
fun InvoiceDialog(
    settings: ShopSettingsEntity,
    sale: SaleEntity,
    items: List<SaleItemEntity>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .padding(8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header Bar with Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sale Receipt (رسید)",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Scrollable Receipt Body
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = settings.shopName,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                )
                            )
                            if (settings.shopTagline.isNotBlank()) {
                                Text(
                                    text = settings.shopTagline,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (settings.address.isNotBlank()) {
                                Text(
                                    text = settings.address,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (settings.phone.isNotBlank()) {
                                Text(
                                    text = "Tel: ${settings.phone}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(thickness = 1.dp)
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Invoice #: ${sale.invoiceNumber}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text(text = CurrencyFormatter.formatDate(sale.createdAt), fontSize = 12.sp)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Customer: ${sale.customerName}", fontSize = 13.sp)
                                if (sale.customerPhone.isNotBlank()) {
                                    Text(text = sale.customerPhone, fontSize = 12.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(thickness = 1.dp)
                            Spacer(modifier = Modifier.height(8.dp))

                            // Table Headers
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Item", fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(2f))
                                Text(text = "Qty", fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(1f))
                                Text(text = "Rate", fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(1f))
                                Text(text = "Total", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }

                    items(items) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = item.productName, fontSize = 13.sp, modifier = Modifier.weight(2f), maxLines = 1)
                            Text(text = "${item.quantity.toInt()} ${item.unit}", fontSize = 13.sp, modifier = Modifier.weight(1f))
                            Text(text = "${item.unitPrice.toInt()}", fontSize = 13.sp, modifier = Modifier.weight(1f))
                            Text(text = CurrencyFormatter.formatPkr(item.lineTotal, settings.currencySymbol), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    item {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(thickness = 1.dp)
                            Spacer(modifier = Modifier.height(8.dp))

                            ReceiptRow(label = "Subtotal:", value = CurrencyFormatter.formatPkr(sale.subtotal, settings.currencySymbol))
                            if (sale.discountAmount > 0) {
                                ReceiptRow(label = "Discount:", value = "-${CurrencyFormatter.formatPkr(sale.discountAmount, settings.currencySymbol)}")
                            }

                            ReceiptRow(
                                label = "Grand Total:",
                                value = CurrencyFormatter.formatPkr(sale.grandTotal, settings.currencySymbol),
                                isBold = true
                            )
                            ReceiptRow(
                                label = "Paid (${sale.paymentMethod}):",
                                value = CurrencyFormatter.formatPkr(sale.paidAmount, settings.currencySymbol)
                            )
                            if (sale.remainingAmount > 0) {
                                ReceiptRow(
                                    label = "Udhaar (Remaining):",
                                    value = CurrencyFormatter.formatPkr(sale.remainingAmount, settings.currencySymbol),
                                    isBold = true,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "شکریہ! دوبارہ تشریف لائیں۔\nThank you for your business!",
                                style = MaterialTheme.typography.bodySmall.copy(textAlign = androidx.compose.ui.text.style.TextAlign.Center),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val pdfFile = PdfInvoiceHelper.generateSaleInvoicePdf(context, settings, sale, items)
                            if (pdfFile != null) {
                                PdfInvoiceHelper.sharePdf(context, pdfFile)
                            } else {
                                Toast.makeText(context, "Could not create PDF", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                    ) {
                        Icon(imageVector = Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "PDF / Print")
                    }

                    Button(
                        onClick = {
                            val phone = sale.customerPhone.ifBlank { settings.whatsapp }
                            val msg = "السلام علیکم! ${settings.shopName} کی طرف سے آپ کی رسید #${sale.invoiceNumber}۔ کل بل: ${CurrencyFormatter.formatPkr(sale.grandTotal, settings.currencySymbol)}۔ ادا شدہ: ${CurrencyFormatter.formatPkr(sale.paidAmount, settings.currencySymbol)}۔ بقایا: ${CurrencyFormatter.formatPkr(sale.remainingAmount, settings.currencySymbol)}۔ تشریف آوری کا شکریہ!"
                            PdfInvoiceHelper.shareViaWhatsApp(context, phone, msg)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = FinancialProfitGreen)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "WhatsApp")
                    }
                }
            }
        }
    }
}

@Composable
fun ReceiptRow(
    label: String,
    value: String,
    isBold: Boolean = false,
    color: Color = Color.Unspecified
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = if (isBold) 14.sp else 13.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = value,
            fontSize = if (isBold) 14.sp else 13.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = color
        )
    }
}
