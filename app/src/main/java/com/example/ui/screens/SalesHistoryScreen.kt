package com.example.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.SaleEntity
import com.example.model.SaleStatus
import com.example.ui.theme.CrimsonError
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.NavyPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SalesHistoryScreen(
    sales: List<SaleEntity>,
    onSelectSale: (SaleEntity) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf("ALL") } // ALL, COMPLETED, REFUNDED

    val filteredSales = remember(sales, searchQuery, statusFilter) {
        sales.filter { sale ->
            val matchesStatus = when (statusFilter) {
                "COMPLETED" -> sale.status == SaleStatus.COMPLETED
                "REFUNDED" -> sale.status == SaleStatus.REFUNDED
                else -> true
            }
            val matchesSearch = searchQuery.isBlank() ||
                sale.invoiceNumber.contains(searchQuery, ignoreCase = true) ||
                sale.customerName.contains(searchQuery, ignoreCase = true) ||
                sale.customerPhone.contains(searchQuery, ignoreCase = true)
            matchesStatus && matchesSearch
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("sales_history_screen")
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search by Invoice # or Customer") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().testTag("sales_search_input"),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = statusFilter == "ALL",
                onClick = { statusFilter = "ALL" },
                label = { Text("All (${sales.size})") },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = NavyPrimary, selectedLabelColor = Color.White),
                modifier = Modifier.testTag("sales_filter_all")
            )
            val compCount = sales.count { it.status == SaleStatus.COMPLETED }
            FilterChip(
                selected = statusFilter == "COMPLETED",
                onClick = { statusFilter = "COMPLETED" },
                label = { Text("Completed ($compCount)") },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = EmeraldSuccess, selectedLabelColor = Color.White),
                modifier = Modifier.testTag("sales_filter_completed")
            )
            val refCount = sales.count { it.status == SaleStatus.REFUNDED }
            FilterChip(
                selected = statusFilter == "REFUNDED",
                onClick = { statusFilter = "REFUNDED" },
                label = { Text("Refunded ($refCount)") },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = CrimsonError, selectedLabelColor = Color.White),
                modifier = Modifier.testTag("sales_filter_refunded")
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredSales.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Receipt, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No sales invoices found.", color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredSales, key = { it.id }) { sale ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectSale(sale) }
                            .testTag("sale_card_${sale.invoiceNumber}")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = sale.invoiceNumber,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = NavyPrimary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = if (sale.status == SaleStatus.REFUNDED) CrimsonError.copy(alpha = 0.12f) else EmeraldSuccess.copy(alpha = 0.12f)
                                    ) {
                                        Text(
                                            text = sale.status.name,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (sale.status == SaleStatus.REFUNDED) CrimsonError else EmeraldSuccess
                                        )
                                    }
                                }

                                Text(
                                    text = "Rs. ${String.format(Locale.US, "%.2f", sale.totalAmount)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (sale.status == SaleStatus.REFUNDED) CrimsonError else EmeraldSuccess
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${sale.customerName} • ${SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.US).format(Date(sale.timestamp))}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )

                                Surface(
                                    shape = RoundedCornerShape(4.dp),
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

                            if (sale.balanceDue > 0) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Customer Udhaar (Credit Due): Rs. ${String.format(Locale.US, "%.2f", sale.balanceDue)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = CrimsonError
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
