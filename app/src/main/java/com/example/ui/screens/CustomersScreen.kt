package com.example.ui.screens

import android.content.Intent
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.CustomerEntity
import com.example.data.entity.CustomerPaymentEntity
import com.example.data.entity.ShopSettingsEntity
import com.example.model.PaymentMethod
import com.example.ui.theme.AquaAccent
import com.example.ui.theme.CrimsonError
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.RoyalBlueBright
import java.util.Locale

@Composable
fun CustomersScreen(
    customers: List<CustomerEntity>,
    payments: List<CustomerPaymentEntity>,
    settings: ShopSettingsEntity,
    onCreateCustomer: (name: String, phone: String, address: String, creditLimit: Double, notes: String) -> Unit,
    onRecordRepayment: (customerId: Long, amount: Double, method: PaymentMethod, note: String) -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var repaymentCustomer by remember { mutableStateOf<CustomerEntity?>(null) }

    val totalReceivables = remember(customers) { customers.sumOf { maxOf(0.0, it.balanceOwed) } }

    val filteredCustomers = remember(customers, searchQuery) {
        customers.filter { c ->
            searchQuery.isBlank() ||
                c.name.contains(searchQuery, ignoreCase = true) ||
                c.phone.contains(searchQuery, ignoreCase = true) ||
                c.address.contains(searchQuery, ignoreCase = true)
        }
    }

    Box(modifier = Modifier.fillMaxSize().testTag("customers_screen")) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Total Receivables Banner
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = NavyPrimary),
                modifier = Modifier.fillMaxWidth().testTag("total_receivables_card")
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Customer Udhaar (Credit Ledger)", style = MaterialTheme.typography.labelMedium, color = AquaAccent)
                        Text(
                            text = "Rs. ${String.format(Locale.US, "%.0f", totalReceivables)}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Total Outstanding to Collect",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "${customers.count { it.balanceOwed > 0 }} Debtor Accounts",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = AquaAccent,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search Customers by Name or Phone") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().testTag("customer_search_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (filteredCustomers.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Group, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No customer records found.", color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredCustomers, key = { it.id }) { customer ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier.fillMaxWidth().testTag("customer_card_${customer.id}")
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = customer.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color.Gray)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(customer.phone, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                            if (customer.address.isNotBlank()) {
                                                Text(" • ${customer.address}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                            }
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "Rs. ${String.format(Locale.US, "%.0f", customer.balanceOwed)}",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = if (customer.balanceOwed > 0) CrimsonError else EmeraldSuccess
                                        )
                                        Text(
                                            text = if (customer.balanceOwed > 0) "Udhaar Owed" else "Cleared / No Dues",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (customer.balanceOwed > 0) CrimsonError else EmeraldSuccess,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Divider(color = Color(0xFFF1F5F9))
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Credit Limit: Rs. ${customer.creditLimit.toInt()}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.Gray
                                    )

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        if (customer.balanceOwed > 0) {
                                            OutlinedButton(
                                                onClick = {
                                                    val msg = "Namaste ${customer.name}, your outstanding credit (Udhaar) at ${settings.shopName} is Rs. ${customer.balanceOwed.toInt()}. Kindly clear the payment when convenient. Dhanyabad!"
                                                    val sendIntent = Intent().apply {
                                                        action = Intent.ACTION_SEND
                                                        putExtra(Intent.EXTRA_TEXT, msg)
                                                        type = "text/plain"
                                                    }
                                                    context.startActivity(Intent.createChooser(sendIntent, "Send Payment Reminder via SMS/WhatsApp"))
                                                },
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.testTag("send_reminder_${customer.id}")
                                            ) {
                                                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Reminder", fontSize = 12.sp)
                                            }

                                            Button(
                                                onClick = { repaymentCustomer = customer },
                                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.testTag("receive_payment_${customer.id}")
                                            ) {
                                                Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Receive Payment", fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = RoyalBlueBright,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("add_customer_fab")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Customer")
        }
    }

    if (showAddDialog) {
        AddCustomerDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, phone, addr, limit, notes ->
                showAddDialog = false
                onCreateCustomer(name, phone, addr, limit, notes)
            }
        )
    }

    if (repaymentCustomer != null) {
        CustomerRepaymentDialog(
            customer = repaymentCustomer!!,
            onDismiss = { repaymentCustomer = null },
            onConfirm = { amount, method, note ->
                val c = repaymentCustomer!!
                repaymentCustomer = null
                onRecordRepayment(c.id, amount, method, note)
            }
        )
    }
}

@Composable
fun AddCustomerDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, phone: String, addr: String, limit: Double, notes: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var creditLimitInput by remember { mutableStateOf("10000") }
    var notes by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Customer Account", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (error != null) {
                    Text(error!!, color = CrimsonError, style = MaterialTheme.typography.bodySmall)
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Customer Name *") },
                    modifier = Modifier.fillMaxWidth().testTag("new_customer_name"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Mobile Phone Number *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth().testTag("new_customer_phone"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address / Location") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = creditLimitInput,
                    onValueChange = { creditLimitInput = it },
                    label = { Text("Credit (Udhaar) Limit (Rs.)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Reference") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank() || phone.isBlank()) {
                        error = "Customer Name and Phone are required!"
                        return@Button
                    }
                    val limit = creditLimitInput.toDoubleOrNull() ?: 10000.0
                    onConfirm(name, phone, address, limit, notes)
                },
                modifier = Modifier.testTag("confirm_add_customer")
            ) {
                Text("Save Customer")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun CustomerRepaymentDialog(
    customer: CustomerEntity,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, method: PaymentMethod, note: String) -> Unit
) {
    var amountInput by remember { mutableStateOf(customer.balanceOwed.toInt().toString()) }
    var selectedMethod by remember { mutableStateOf(PaymentMethod.CASH) }
    var noteInput by remember { mutableStateOf("Udhaar repayment") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Receive Repayment", fontWeight = FontWeight.Bold)
                Text(
                    text = "${customer.name} • Total Due: Rs. ${customer.balanceOwed.toInt()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = CrimsonError
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it },
                    label = { Text("Repayment Amount (Rs.) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("repayment_amount_input"),
                    singleLine = true
                )

                Text("Payment Received Via:", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelMedium)
                listOf(PaymentMethod.CASH, PaymentMethod.QR, PaymentMethod.BANK).forEach { method ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedMethod = method }
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = selectedMethod == method, onClick = { selectedMethod = method })
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(method.displayName)
                    }
                }

                OutlinedTextField(
                    value = noteInput,
                    onValueChange = { noteInput = it },
                    label = { Text("Receipt Note") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountInput.toDoubleOrNull() ?: 0.0
                    if (amount > 0) {
                        onConfirm(amount, selectedMethod, noteInput)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                modifier = Modifier.testTag("confirm_repayment_button")
            ) {
                Text("Record Repayment")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
