package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.ProductEntity
import com.example.data.entity.SaleEntity
import com.example.data.entity.ShopSettingsEntity
import com.example.model.ExpiryStatus
import com.example.model.FinancialSummary
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.AquaAccent
import com.example.ui.theme.CrimsonError
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.RoyalBlueBright
import com.example.ui.viewmodel.Screen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    financialSummary: FinancialSummary,
    products: List<ProductEntity>,
    recentSales: List<SaleEntity>,
    settings: ShopSettingsEntity,
    onNavigate: (Screen) -> Unit,
    onViewSaleReceipt: (SaleEntity) -> Unit
) {
    val lowStockItems = products.filter { it.isLowStock }
    val expiringItems = products.filter {
        val s = it.getExpiryStatus()
        s == ExpiryStatus.EXPIRING_SOON || s == ExpiryStatus.EXPIRED
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("dashboard_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Welcome & Store Status Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Store Overview",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Currency: ${settings.currencyCode} (${settings.currencySymbol}) • Timezone: ${settings.timezone}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = EmeraldSuccess.copy(alpha = 0.12f),
                    modifier = Modifier.padding(4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(8.dp).background(EmeraldSuccess, CircleShape))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Online Store POS",
                            style = MaterialTheme.typography.labelSmall,
                            color = EmeraldSuccess,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Quick Actions Flow Row
        item {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onNavigate(Screen.POS) },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("quick_action_pos")
                ) {
                    Icon(Icons.Default.PointOfSale, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("New Sale (POS)")
                }

                Button(
                    onClick = { onNavigate(Screen.INVENTORY) },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlueBright),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("quick_action_add_product")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Stock / Products")
                }

                OutlinedButton(
                    onClick = { onNavigate(Screen.CUSTOMERS) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("quick_action_khata")
                ) {
                    Icon(Icons.Default.Group, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Customer Khata")
                }

                OutlinedButton(
                    onClick = { onNavigate(Screen.EXPENSES) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("quick_action_expense")
                ) {
                    Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Record Expense")
                }
            }
        }

        // Low stock & Expiry Warnings if any
        if (lowStockItems.isNotEmpty() || expiringItems.isNotEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (lowStockItems.isNotEmpty()) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = AmberWarning.copy(alpha = 0.12f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigate(Screen.INVENTORY) }
                                .testTag("low_stock_alert_banner")
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = AmberWarning, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${lowStockItems.size} Product(s) Below Minimum Stock!",
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFB45309),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = lowStockItems.take(3).joinToString(", ") { "${it.name} (${it.stockQuantity} left)" } +
                                            if (lowStockItems.size > 3) " and ${lowStockItems.size - 3} more" else "",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF92400E)
                                    )
                                }
                                Icon(Icons.Default.ArrowForward, contentDescription = null, tint = AmberWarning, modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    if (expiringItems.isNotEmpty()) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CrimsonError.copy(alpha = 0.12f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigate(Screen.INVENTORY) }
                                .testTag("expiry_alert_banner")
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = CrimsonError, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${expiringItems.size} Product(s) Expiring Soon or Expired!",
                                        fontWeight = FontWeight.Bold,
                                        color = CrimsonError,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = expiringItems.take(3).joinToString(", ") { it.name },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF991B1B)
                                    )
                                }
                                Icon(Icons.Default.ArrowForward, contentDescription = null, tint = CrimsonError, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }

        // Primary Metrics 2x2 Grid
        item {
            Text(
                text = "Key Financial Performance",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        modifier = Modifier.weight(1f),
                        title = "Net Sales Revenue",
                        value = "Rs. ${String.format(Locale.US, "%.0f", financialSummary.netSales)}",
                        subtitle = "${financialSummary.totalCompletedSalesCount} Completed Sales",
                        icon = Icons.Default.TrendingUp,
                        accentColor = EmeraldSuccess,
                        testTag = "kpi_net_sales"
                    )

                    MetricCard(
                        modifier = Modifier.weight(1f),
                        title = "Gross Profit",
                        value = "Rs. ${String.format(Locale.US, "%.0f", financialSummary.grossProfit)}",
                        subtitle = "Margin: ${String.format(Locale.US, "%.1f", financialSummary.grossMarginPercent)}%",
                        icon = Icons.Default.AttachMoney,
                        accentColor = RoyalBlueBright,
                        testTag = "kpi_gross_profit"
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        modifier = Modifier.weight(1f),
                        title = "Est. Net Operating Profit",
                        value = "Rs. ${String.format(Locale.US, "%.0f", financialSummary.netOperatingProfit)}",
                        subtitle = "After Rent/Expenses",
                        icon = Icons.Default.CheckCircle,
                        accentColor = if (financialSummary.netOperatingProfit >= 0) EmeraldSuccess else CrimsonError,
                        testTag = "kpi_net_profit"
                    )

                    MetricCard(
                        modifier = Modifier.weight(1f),
                        title = "Operating Expenses",
                        value = "Rs. ${String.format(Locale.US, "%.0f", financialSummary.totalExpenses)}",
                        subtitle = "Rent, Power, Salaries",
                        icon = Icons.Default.ArrowDownward,
                        accentColor = CrimsonError,
                        testTag = "kpi_expenses"
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        modifier = Modifier.weight(1f),
                        title = "Customer Credit (Udhaar)",
                        value = "Rs. ${String.format(Locale.US, "%.0f", financialSummary.totalReceivables)}",
                        subtitle = "Pending Receivables",
                        icon = Icons.Default.Group,
                        accentColor = AmberWarning,
                        testTag = "kpi_receivables"
                    )

                    MetricCard(
                        modifier = Modifier.weight(1f),
                        title = "Supplier Dues (Payables)",
                        value = "Rs. ${String.format(Locale.US, "%.0f", financialSummary.totalPayables)}",
                        subtitle = "Outstanding to Vendors",
                        icon = Icons.Default.Payments,
                        accentColor = CrimsonError,
                        testTag = "kpi_payables"
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        modifier = Modifier.weight(1f),
                        title = "Inventory Valuation",
                        value = "Rs. ${String.format(Locale.US, "%.0f", financialSummary.inventoryCostValue)}",
                        subtitle = "Retail: Rs. ${String.format(Locale.US, "%.0f", financialSummary.inventoryRetailValue)}",
                        icon = Icons.Default.Inventory,
                        accentColor = AquaAccent,
                        testTag = "kpi_inventory_value"
                    )

                    MetricCard(
                        modifier = Modifier.weight(1f),
                        title = "Estimated Cash Flow",
                        value = "Rs. ${String.format(Locale.US, "%.0f", financialSummary.cashInHand)}",
                        subtitle = "Cash Net Balance",
                        icon = Icons.Default.Payments,
                        accentColor = NavyPrimary,
                        testTag = "kpi_cash_in_hand"
                    )
                }
            }
        }

        // Recent Invoices / Transactions
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Sales Invoices",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "View All",
                    style = MaterialTheme.typography.labelMedium,
                    color = RoyalBlueBright,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable { onNavigate(Screen.SALES_HISTORY) }
                        .padding(4.dp)
                        .testTag("view_all_sales_button")
                )
            }
        }

        if (recentSales.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Receipt, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No sales completed yet.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                    }
                }
            }
        } else {
            items(recentSales.take(5)) { sale ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onViewSaleReceipt(sale) }
                        .testTag("recent_sale_item_${sale.invoiceNumber}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = sale.invoiceNumber,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = NavyPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFF1F5F9)
                                ) {
                                    Text(
                                        text = sale.paymentMethod.displayName,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF475569)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${sale.customerName} • ${SimpleDateFormat("dd MMM, hh:mm a", Locale.US).format(Date(sale.timestamp))}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Rs. ${String.format(Locale.US, "%.2f", sale.totalAmount)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldSuccess
                            )
                            if (sale.balanceDue > 0) {
                                Text(
                                    text = "Udhaar: Rs. ${String.format(Locale.US, "%.0f", sale.balanceDue)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CrimsonError,
                                    fontWeight = FontWeight.SemiBold
                                )
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
fun MetricCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    testTag: String
) {
    Card(
        modifier = modifier.testTag(testTag),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF64748B),
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(accentColor.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF94A3B8),
                maxLines = 1
            )
        }
    }
}
