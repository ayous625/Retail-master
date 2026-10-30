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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.ExpenseEntity
import com.example.data.entity.OtherIncomeEntity
import com.example.model.PaymentMethod
import com.example.ui.theme.AquaAccent
import com.example.ui.theme.CrimsonError
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.RoyalBlueBright
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ExpensesScreen(
    expenses: List<ExpenseEntity>,
    income: List<OtherIncomeEntity>,
    canManage: Boolean,
    onAddExpense: (title: String, category: String, amount: Double, method: PaymentMethod, ref: String, note: String) -> Unit,
    onAddIncome: (title: String, category: String, amount: Double, method: PaymentMethod, ref: String, note: String) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Expenses, 1: Other Income
    var showAddDialog by remember { mutableStateOf(false) }

    val totalExpenses = remember(expenses) { expenses.sumOf { it.amount } }
    val totalIncome = remember(income) { income.sumOf { it.amount } }

    Box(modifier = Modifier.fillMaxSize().testTag("expenses_screen")) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Totals Banner
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = NavyPrimary),
                modifier = Modifier.fillMaxWidth().testTag("expenses_total_card")
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (selectedTab == 0) "Total Operating Costs" else "Other Non-Sales Income",
                            style = MaterialTheme.typography.labelMedium,
                            color = AquaAccent
                        )
                        Text(
                            text = "Rs. ${String.format(Locale.US, "%.0f", if (selectedTab == 0) totalExpenses else totalIncome)}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTab == 0) CrimsonError else EmeraldSuccess
                        )
                        Text(
                            text = if (selectedTab == 0) "${expenses.size} Expense Records" else "${income.size} Income Records",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (selectedTab == 0) Icons.Default.TrendingDown else Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = if (selectedTab == 0) CrimsonError else EmeraldSuccess,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                if (selectedTab == 0) "Outflow" else "Inflow",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = NavyPrimary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Expenses (${expenses.size})", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("tab_expenses")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Other Income (${income.size})", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("tab_income")
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (selectedTab == 0) {
                if (expenses.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        Text("No operating expenses recorded yet.", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(expenses, key = { it.id }) { expense ->
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(expense.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            "${expense.category} • ${SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date(expense.timestamp))}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray
                                        )
                                        if (expense.note.isNotBlank()) {
                                            Text(expense.note, style = MaterialTheme.typography.labelSmall, color = Color(0xFF64748B))
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            "Rs. ${String.format(Locale.US, "%.0f", expense.amount)}",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = CrimsonError
                                        )
                                        Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFF1F5F9)) {
                                            Text(
                                                expense.paymentMethod.displayName,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(0xFF475569)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                }
            } else {
                if (income.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        Text("No other income recorded yet.", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(income, key = { it.id }) { item ->
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            "${item.category} • ${SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date(item.timestamp))}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray
                                        )
                                        if (item.note.isNotBlank()) {
                                            Text(item.note, style = MaterialTheme.typography.labelSmall, color = Color(0xFF64748B))
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            "Rs. ${String.format(Locale.US, "%.0f", item.amount)}",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = EmeraldSuccess
                                        )
                                        Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFF1F5F9)) {
                                            Text(
                                                item.paymentMethod.displayName,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(0xFF475569)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                }
            }
        }

        if (canManage) {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = if (selectedTab == 0) CrimsonError else EmeraldSuccess,
                contentColor = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp)
                    .testTag("add_expense_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    }

    if (showAddDialog) {
        if (selectedTab == 0) {
            AddExpenseDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { title, cat, amt, method, ref, note ->
                    showAddDialog = false
                    onAddExpense(title, cat, amt, method, ref, note)
                }
            )
        } else {
            AddIncomeDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { title, cat, amt, method, ref, note ->
                    showAddDialog = false
                    onAddIncome(title, cat, amt, method, ref, note)
                }
            )
        }
    }
}

@Composable
fun AddExpenseDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, category: String, amount: Double, method: PaymentMethod, ref: String, note: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Rent") }
    var amountInput by remember { mutableStateOf("") }
    var selectedMethod by remember { mutableStateOf(PaymentMethod.CASH) }
    var reference by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    val categories = listOf("Rent", "Staff Salary", "Electricity / Utility", "Transport", "Packaging", "Maintenance", "Tea & Refreshments", "Other")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record Business Expense", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Expense Title (e.g. Monthly Rent) *") },
                    modifier = Modifier.fillMaxWidth().testTag("expense_title_input"),
                    singleLine = true
                )

                // Category dropdown
                var catExpanded by remember { mutableStateOf(false) }
                Box(modifier = Modifier.fillMaxWidth()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { catExpanded = true }
                            .padding(10.dp),
                        color = Color(0xFFF1F5F9)
                    ) {
                        Text("Category: $selectedCategory", fontWeight = FontWeight.SemiBold)
                    }
                    DropdownMenu(expanded = catExpanded, onDismissRequest = { catExpanded = false }) {
                        categories.forEach { cat ->
                            DropdownMenuItem(text = { Text(cat) }, onClick = {
                                selectedCategory = cat
                                catExpanded = false
                            })
                        }
                    }
                }

                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it },
                    label = { Text("Amount (Rs.) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("expense_amount_input"),
                    singleLine = true
                )

                Text("Payment Method:", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelMedium)
                listOf(PaymentMethod.CASH, PaymentMethod.QR, PaymentMethod.BANK).forEach { m ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedMethod = m }
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = selectedMethod == m, onClick = { selectedMethod = m })
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(m.displayName)
                    }
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Optional Note / Voucher #") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountInput.toDoubleOrNull() ?: 0.0
                    if (title.isNotBlank() && amt > 0) {
                        onConfirm(title, selectedCategory, amt, selectedMethod, reference, note)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CrimsonError),
                modifier = Modifier.testTag("confirm_add_expense")
            ) {
                Text("Record Expense")
            }
        },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun AddIncomeDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, category: String, amount: Double, method: PaymentMethod, ref: String, note: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Scrap Sales") }
    var amountInput by remember { mutableStateOf("") }
    var selectedMethod by remember { mutableStateOf(PaymentMethod.CASH) }
    var note by remember { mutableStateOf("") }

    val categories = listOf("Scrap Sales", "Delivery Charges", "Supplier Incentive / Rebate", "Commission", "Other Income")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record Other Non-Sales Income", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Income Source (e.g. Carton Boxes) *") },
                    modifier = Modifier.fillMaxWidth().testTag("income_title_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it },
                    label = { Text("Amount Received (Rs.) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("income_amount_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Remarks") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountInput.toDoubleOrNull() ?: 0.0
                    if (title.isNotBlank() && amt > 0) {
                        onConfirm(title, selectedCategory, amt, selectedMethod, "", note)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                modifier = Modifier.testTag("confirm_add_income")
            ) {
                Text("Record Income")
            }
        },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
