package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.ProductEntity
import com.example.data.entity.SaleEntity
import com.example.data.entity.SaleItemEntity
import com.example.data.entity.ShopSettingsEntity
import com.example.model.FinancialSummary
import com.example.ui.theme.AquaAccent
import com.example.ui.theme.CrimsonError
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.RoyalBlueBright
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReportsScreen(
    financialSummary: FinancialSummary,
    products: List<ProductEntity>,
    sales: List<SaleEntity>,
    saleItems: List<SaleItemEntity>,
    settings: ShopSettingsEntity
) {
    val context = LocalContext.current

    // Compute top selling items by quantity sold
    val topSellingItems = remember(saleItems) {
        saleItems.groupBy { it.productId }
            .map { (prodId, items) ->
                val name = items.firstOrNull()?.productName ?: "Unknown"
                val totalQty = items.sumOf { it.quantity }
                val totalRevenue = items.sumOf { it.total }
                val totalProfit = items.sumOf { it.grossProfit }
                Triple(name, totalQty, Pair(totalRevenue, totalProfit))
            }
            .sortedByDescending { it.second }
            .take(5)
    }

    val exportReportText = remember(financialSummary, settings) {
        val nowStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date())
        buildString {
            appendLine("==================================================")
            appendLine("           ${settings.shopName.uppercase()}")
            appendLine("       PROFIT & LOSS FINANCIAL STATEMENT")
            appendLine("       Generated On: $nowStr")
            appendLine("==================================================")
            appendLine("A. TRADING / GROSS PROFIT ACCOUNT")
            appendLine(String.format("   Gross Sales:                         Rs. %10.2f", financialSummary.grossSales))
            appendLine(String.format("   Less: Discounts Allowed:           - Rs. %10.2f", financialSummary.totalDiscounts))
            appendLine(String.format("   Less: Sales Returns/Refunds:       - Rs. %10.2f", financialSummary.totalRefunds))
            appendLine("   -----------------------------------------------")
            appendLine(String.format("   NET SALES REVENUE:                   Rs. %10.2f", financialSummary.netSales))
            appendLine(String.format("   Less: Cost of Goods Sold (COGS):   - Rs. %10.2f", financialSummary.costOfGoodsSold))
            appendLine("   ===============================================")
            appendLine(String.format("   GROSS PROFIT:                        Rs. %10.2f  (Margin: %.1f%%)", financialSummary.grossProfit, financialSummary.grossMarginPercent))
            appendLine("==================================================")
            appendLine("B. OPERATING PROFIT ACCOUNT")
            appendLine(String.format("   Gross Profit b/d:                    Rs. %10.2f", financialSummary.grossProfit))
            appendLine(String.format("   Add: Other Business Income:        + Rs. %10.2f", financialSummary.totalOtherIncome))
            appendLine(String.format("   Less: Total Operating Expenses:    - Rs. %10.2f", financialSummary.totalExpenses))
            appendLine("   ===============================================")
            appendLine(String.format("   ESTIMATED NET OPERATING PROFIT:      Rs. %10.2f  (Margin: %.1f%%)", financialSummary.netOperatingProfit, financialSummary.netMarginPercent))
            appendLine("==================================================")
            appendLine("C. BALANCE SHEET LEDGER POSITION")
            appendLine(String.format("   Customer Receivables (Udhaar Due):   Rs. %10.2f", financialSummary.totalReceivables))
            appendLine(String.format("   Supplier Payables (Vendor Dues):     Rs. %10.2f", financialSummary.totalPayables))
            appendLine(String.format("   Inventory Valuation (At Cost):       Rs. %10.2f", financialSummary.inventoryCostValue))
            appendLine(String.format("   Inventory Valuation (Retail Value):  Rs. %10.2f", financialSummary.inventoryRetailValue))
            appendLine(String.format("   Estimated Net Cash Position:         Rs. %10.2f", financialSummary.cashInHand))
            appendLine("==================================================")
            appendLine("Note: Net Operating Profit = Gross Profit - Operating Expenses + Other Income.")
            appendLine("Documented Costing Model: FIFO / Transactional Purchase Cost.")
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("reports_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Financial Reports & P&L",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Standard retail accounting statement",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

                Button(
                    onClick = {
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, exportReportText)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Export P&L Report"))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("export_report_btn")
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share / Export", fontSize = 12.sp)
                }
            }
        }

        // Profit & Loss Accounting Statement Card
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth().testTag("pnl_statement_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Statement of Profit & Loss",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary
                    )
                    Text(
                        text = "Documented Financial Formulas (NPR minor-unit accurate)",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(10.dp))

                    ReportLine("Gross Sales Revenue", "Rs. ${String.format(Locale.US, "%.2f", financialSummary.grossSales)}")
                    ReportLine("Less: Sales Discounts", "- Rs. ${String.format(Locale.US, "%.2f", financialSummary.totalDiscounts)}", color = EmeraldSuccess)
                    ReportLine("Less: Sales Returns & Refunds", "- Rs. ${String.format(Locale.US, "%.2f", financialSummary.totalRefunds)}", color = CrimsonError)

                    Divider(modifier = Modifier.padding(vertical = 6.dp))
                    ReportLine("NET SALES REVENUE", "Rs. ${String.format(Locale.US, "%.2f", financialSummary.netSales)}", isBold = true, color = NavyPrimary)
                    ReportLine("Less: Cost of Goods Sold (COGS)", "- Rs. ${String.format(Locale.US, "%.2f", financialSummary.costOfGoodsSold)}", color = Color(0xFF64748B))

                    Divider(modifier = Modifier.padding(vertical = 6.dp))
                    ReportLine(
                        "GROSS PROFIT (Margin: ${String.format(Locale.US, "%.1f", financialSummary.grossMarginPercent)}%)",
                        "Rs. ${String.format(Locale.US, "%.2f", financialSummary.grossProfit)}",
                        isBold = true,
                        color = RoyalBlueBright
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    ReportLine("Less: Total Operating Expenses", "- Rs. ${String.format(Locale.US, "%.2f", financialSummary.totalExpenses)}", color = CrimsonError)
                    ReportLine("Add: Other Operating Income", "+ Rs. ${String.format(Locale.US, "%.2f", financialSummary.totalOtherIncome)}", color = EmeraldSuccess)

                    Divider(modifier = Modifier.padding(vertical = 8.dp), thickness = 1.5.dp, color = NavyPrimary)
                    ReportLine(
                        "ESTIMATED NET OPERATING PROFIT",
                        "Rs. ${String.format(Locale.US, "%.2f", financialSummary.netOperatingProfit)}",
                        isBold = true,
                        color = if (financialSummary.netOperatingProfit >= 0) EmeraldSuccess else CrimsonError,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Net Operating Margin: ${String.format(Locale.US, "%.1f", financialSummary.netMarginPercent)}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
        }

        // Ledger & Balance Positions
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth().testTag("balance_position_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Working Capital & Balance Positions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(10.dp))

                    ReportLine("Customer Receivables (Udhaar Due)", "Rs. ${String.format(Locale.US, "%.2f", financialSummary.totalReceivables)}", color = CrimsonError)
                    ReportLine("Supplier Payables (Vendor Bills)", "Rs. ${String.format(Locale.US, "%.2f", financialSummary.totalPayables)}", color = CrimsonError)
                    ReportLine("Total Inventory Stock (At Cost)", "Rs. ${String.format(Locale.US, "%.2f", financialSummary.inventoryCostValue)}", color = NavyPrimary)
                    ReportLine("Total Inventory Stock (Retail Value)", "Rs. ${String.format(Locale.US, "%.2f", financialSummary.inventoryRetailValue)}", color = EmeraldSuccess)
                    ReportLine("Estimated Net Cash Flow Balance", "Rs. ${String.format(Locale.US, "%.2f", financialSummary.cashInHand)}", isBold = true, color = NavyPrimary)
                }
            }
        }

        // Best-Selling Products Table
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth().testTag("top_products_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Best-Selling Products by Volume",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    if (topSellingItems.isEmpty()) {
                        Text("No sale items recorded yet.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    } else {
                        topSellingItems.forEachIndexed { index, (name, qty, fin) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color(0xFFF1F5F9)
                                    ) {
                                        Text("#${index + 1}", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(name, fontWeight = FontWeight.SemiBold, maxLines = 1, style = MaterialTheme.typography.bodyMedium)
                                        Text("Revenue: Rs. ${fin.first.toInt()} • Profit: Rs. ${fin.second.toInt()}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                    }
                                }

                                Text(
                                    text = "${qty.toInt()} Sold",
                                    fontWeight = FontWeight.Bold,
                                    color = RoyalBlueBright,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            if (index < topSellingItems.size - 1) {
                                Divider(color = Color(0xFFF8FAFC), modifier = Modifier.padding(vertical = 2.dp))
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ReportLine(
    title: String,
    value: String,
    isBold: Boolean = false,
    color: Color = Color.Unspecified,
    fontSize: androidx.compose.ui.unit.TextUnit = 14.sp
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            fontSize = fontSize,
            color = if (color != Color.Unspecified) color else MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.SemiBold,
            fontSize = fontSize,
            color = if (color != Color.Unspecified) color else MaterialTheme.colorScheme.onSurface
        )
    }
}
