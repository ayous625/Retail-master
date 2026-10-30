package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.entity.SaleEntity
import com.example.data.entity.SaleItemEntity
import com.example.data.entity.ShopSettingsEntity
import com.example.model.SaleStatus
import com.example.ui.theme.CrimsonError
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.NavyPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReceiptDialog(
    sale: SaleEntity,
    items: List<SaleItemEntity>,
    settings: ShopSettingsEntity,
    onDismiss: () -> Unit,
    onRefund: (saleId: Long, reason: String) -> Unit
) {
    val context = LocalContext.current
    var showRefundConfirm by remember { mutableStateOf(false) }
    var refundReason by remember { mutableStateOf("Customer returned goods") }

    val dateStr = remember(sale.timestamp) {
        SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.US).format(Date(sale.timestamp))
    }

    val receiptText = remember(sale, items, settings) {
        buildString {
            appendLine("================================")
            appendLine("      ${settings.shopName.uppercase()}")
            appendLine("  ${settings.address}")
            appendLine("  Tel: ${settings.phone} | PAN: ${settings.panVatNumber}")
            appendLine("================================")
            appendLine("Invoice No: ${sale.invoiceNumber}")
            appendLine("Date: $dateStr")
            appendLine("Cashier: ${sale.cashierName}")
            appendLine("Customer: ${sale.customerName} (${sale.customerPhone.ifBlank { "Walk-in" }})")
            appendLine("--------------------------------")
            appendLine(String.format("%-18s %4s %8s", "ITEM", "QTY", "TOTAL"))
            appendLine("--------------------------------")
            for (item in items) {
                appendLine(String.format("%-18s %4.1f %8.2f", item.productName.take(18), item.quantity, item.total))
            }
            appendLine("--------------------------------")
            appendLine(String.format("%-22s %9.2f", "Subtotal:", sale.subtotal))
            if (sale.discountAmount > 0) {
                appendLine(String.format("%-22s %9.2f", "Discount:", -sale.discountAmount))
            }
            if (sale.taxAmount > 0) {
                appendLine(String.format("%-22s %9.2f", "VAT (${sale.taxRatePercent}%):", sale.taxAmount))
            }
            appendLine("================================")
            appendLine(String.format("%-22s %9.2f", "TOTAL (NPR):", sale.totalAmount))
            appendLine(String.format("%-22s %9.2f", "Paid (${sale.paymentMethod.displayName}):", sale.paidAmount))
            if (sale.balanceDue > 0) {
                appendLine(String.format("%-22s %9.2f", "Credit / Due:", sale.balanceDue))
            }
            appendLine("================================")
            appendLine(settings.receiptFooter)
            appendLine("================================")
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("receipt_dialog"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sale Receipt",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_receipt_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Thermal receipt paper styling container
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFCFDFE), RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(8.dp))
                        .padding(16.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = settings.shopName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = settings.address,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF475569),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Phone: ${settings.phone} | PAN/VAT: ${settings.panVatNumber}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF64748B),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Divider(color = Color(0xFFCBD5E1), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Invoice: ${sale.invoiceNumber}", style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily.Monospace)
                            Text(
                                text = if (sale.status == SaleStatus.REFUNDED) "REFUNDED" else "PAID",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (sale.status == SaleStatus.REFUNDED) CrimsonError else EmeraldSuccess
                            )
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Date: $dateStr", style = MaterialTheme.typography.labelSmall, color = Color(0xFF64748B))
                            Text("Cashier: ${sale.cashierName}", style = MaterialTheme.typography.labelSmall, color = Color(0xFF64748B))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Customer: ${sale.customerName}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                            Text(sale.customerPhone, style = MaterialTheme.typography.labelSmall, color = Color(0xFF64748B))
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Divider(color = Color(0xFFCBD5E1), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(6.dp))

                        // Items header
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text("Item", modifier = Modifier.weight(2f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            Text("Qty", modifier = Modifier.weight(0.8f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                            Text("Price", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
                            Text("Total", modifier = Modifier.weight(1.2f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
                        }
                        Divider(modifier = Modifier.padding(vertical = 4.dp), color = Color(0xFFE2E8F0))

                        // Items
                        for (item in items) {
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                                Text(item.productName, modifier = Modifier.weight(2f), style = MaterialTheme.typography.bodySmall, maxLines = 1)
                                Text("${item.quantity} ${item.unit}", modifier = Modifier.weight(0.8f), style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                                Text("Rs.${item.unitPrice.toInt()}", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.End)
                                Text("Rs.${item.total.toInt()}", modifier = Modifier.weight(1.2f), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.End)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Divider(color = Color(0xFFCBD5E1), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(8.dp))

                        // Subtotals & totals
                        BillRow("Subtotal", "Rs. ${String.format(Locale.US, "%.2f", sale.subtotal)}")
                        if (sale.discountAmount > 0) {
                            BillRow("Discount", "- Rs. ${String.format(Locale.US, "%.2f", sale.discountAmount)}", color = EmeraldSuccess)
                        }
                        if (sale.taxAmount > 0) {
                            BillRow("VAT (${sale.taxRatePercent}%)", "+ Rs. ${String.format(Locale.US, "%.2f", sale.taxAmount)}")
                        }

                        Divider(modifier = Modifier.padding(vertical = 6.dp), color = Color(0xFF94A3B8), thickness = 1.5.dp)

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Grand Total", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = NavyPrimary)
                            Text("Rs. ${String.format(Locale.US, "%.2f", sale.totalAmount)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = NavyPrimary)
                        }

                        BillRow("Payment Method", sale.paymentMethod.displayName)
                        BillRow("Paid Amount", "Rs. ${String.format(Locale.US, "%.2f", sale.paidAmount)}", color = EmeraldSuccess)
                        if (sale.balanceDue > 0) {
                            BillRow("Credit Due (Udhaar)", "Rs. ${String.format(Locale.US, "%.2f", sale.balanceDue)}", color = CrimsonError)
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = settings.receiptFooter,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF64748B),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Receipt", receiptText))
                            Toast.makeText(context, "Receipt text copied to clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("copy_receipt_button")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Copy", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copy", fontSize = 13.sp)
                    }

                    Button(
                        onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, receiptText)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Receipt via WhatsApp/SMS"))
                        },
                        modifier = Modifier
                            .weight(1.2f)
                            .testTag("print_receipt_button")
                    ) {
                        Icon(Icons.Default.Print, contentDescription = "Print", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Share/Print", fontSize = 13.sp)
                    }
                }

                if (sale.status == SaleStatus.COMPLETED) {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = { showRefundConfirm = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CrimsonError),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("refund_sale_button")
                    ) {
                        Icon(Icons.Default.Replay, contentDescription = "Refund", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Return & Refund Invoice")
                    }
                }
            }
        }
    }

    if (showRefundConfirm) {
        AlertDialog(
            onDismissRequest = { showRefundConfirm = false },
            title = { Text("Refund Sale Invoice #${sale.invoiceNumber}") },
            text = {
                Column {
                    Text("Refunding will restock all ${items.size} item(s) back into inventory and reverse any customer credit owed.")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = refundReason,
                        onValueChange = { refundReason = it },
                        label = { Text("Reason for Return") },
                        modifier = Modifier.fillMaxWidth().testTag("refund_reason_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRefundConfirm = false
                        onRefund(sale.id, refundReason)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonError),
                    modifier = Modifier.testTag("confirm_refund_button")
                ) {
                    Text("Confirm Refund")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showRefundConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun BillRow(label: String, value: String, color: Color = Color(0xFF334155)) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = Color(0xFF64748B))
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = color)
    }
}
