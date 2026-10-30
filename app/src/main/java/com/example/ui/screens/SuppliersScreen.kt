package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Store
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import com.example.data.entity.ProductEntity
import com.example.data.entity.PurchaseEntity
import com.example.data.entity.PurchaseItemEntity
import com.example.data.entity.SupplierEntity
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
fun SuppliersScreen(
    suppliers: List<SupplierEntity>,
    purchases: List<PurchaseEntity>,
    products: List<ProductEntity>,
    canManage: Boolean,
    onCreateSupplier: (name: String, company: String, phone: String, address: String, notes: String) -> Unit,
    onRecordPurchase: (supplier: SupplierEntity, items: List<PurchaseItemEntity>, paidAmount: Double, method: PaymentMethod, notes: String) -> Unit,
    onRecordSupplierPayment: (supplierId: Long, amount: Double, method: PaymentMethod, note: String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var showAddSupplierDialog by remember { mutableStateOf(false) }
    var showNewPurchaseDialog by remember { mutableStateOf(false) }
    var payingSupplier by remember { mutableStateOf<SupplierEntity?>(null) }

    val totalPayables = remember(suppliers) { suppliers.sumOf { maxOf(0.0, it.balanceOwed) } }

    val filteredSuppliers = remember(suppliers, searchQuery) {
        suppliers.filter { s ->
            searchQuery.isBlank() ||
                s.company.contains(searchQuery, ignoreCase = true) ||
                s.name.contains(searchQuery, ignoreCase = true) ||
                s.phone.contains(searchQuery, ignoreCase = true)
        }
    }

    Box(modifier = Modifier.fillMaxSize().testTag("suppliers_screen")) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Payables Banner
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = NavyPrimary),
                modifier = Modifier.fillMaxWidth().testTag("total_payables_card")
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Supplier Dues (Accounts Payable)", style = MaterialTheme.typography.labelMedium, color = AquaAccent)
                        Text(
                            text = "Rs. ${String.format(Locale.US, "%.0f", totalPayables)}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Total Balance Owed to Wholesalers",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }

                    if (canManage) {
                        Button(
                            onClick = { showNewPurchaseDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalBlueBright),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("inward_purchase_btn")
                        ) {
                            Icon(Icons.Default.AddShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New Purchase", fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search Suppliers / Distributors") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().testTag("supplier_search_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (filteredSuppliers.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Store, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No suppliers found.", color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredSuppliers, key = { it.id }) { supplier ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier.fillMaxWidth().testTag("supplier_card_${supplier.id}")
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = supplier.company,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Contact: ${supplier.name} • ${supplier.phone}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray
                                        )
                                        if (supplier.address.isNotBlank()) {
                                            Text(supplier.address, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "Rs. ${String.format(Locale.US, "%.0f", supplier.balanceOwed)}",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = if (supplier.balanceOwed > 0) CrimsonError else EmeraldSuccess
                                        )
                                        Text(
                                            text = if (supplier.balanceOwed > 0) "Payable Due" else "Settled",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (supplier.balanceOwed > 0) CrimsonError else EmeraldSuccess,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                if (canManage && supplier.balanceOwed > 0) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Divider(color = Color(0xFFF1F5F9))
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                        Button(
                                            onClick = { payingSupplier = supplier },
                                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.testTag("pay_supplier_btn_${supplier.id}")
                                        ) {
                                            Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Pay Supplier", fontSize = 12.sp)
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

        if (canManage) {
            FloatingActionButton(
                onClick = { showAddSupplierDialog = true },
                containerColor = RoyalBlueBright,
                contentColor = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp)
                    .testTag("add_supplier_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Supplier")
            }
        }
    }

    if (showAddSupplierDialog) {
        AddSupplierDialog(
            onDismiss = { showAddSupplierDialog = false },
            onConfirm = { name, company, phone, addr, notes ->
                showAddSupplierDialog = false
                onCreateSupplier(name, company, phone, addr, notes)
            }
        )
    }

    if (payingSupplier != null) {
        PaySupplierDialog(
            supplier = payingSupplier!!,
            onDismiss = { payingSupplier = null },
            onConfirm = { amount, method, note ->
                val s = payingSupplier!!
                payingSupplier = null
                onRecordSupplierPayment(s.id, amount, method, note)
            }
        )
    }

    if (showNewPurchaseDialog) {
        NewPurchaseDialog(
            suppliers = suppliers,
            products = products,
            onDismiss = { showNewPurchaseDialog = false },
            onConfirm = { sup, items, paid, method, notes ->
                showNewPurchaseDialog = false
                onRecordPurchase(sup, items, paid, method, notes)
            }
        )
    }
}

@Composable
fun AddSupplierDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, company: String, phone: String, addr: String, notes: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var company by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Supplier / Distributor", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (error != null) {
                    Text(error!!, color = CrimsonError, style = MaterialTheme.typography.bodySmall)
                }

                OutlinedTextField(
                    value = company,
                    onValueChange = { company = it },
                    label = { Text("Company / Distributor Name *") },
                    modifier = Modifier.fillMaxWidth().testTag("supplier_company_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Contact Person Name *") },
                    modifier = Modifier.fillMaxWidth().testTag("supplier_name_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth().testTag("supplier_phone_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Warehouse / Depot Address") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Remarks") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (company.isBlank() || phone.isBlank()) {
                        error = "Company name and phone are required!"
                        return@Button
                    }
                    onConfirm(name, company, phone, address, notes)
                },
                modifier = Modifier.testTag("confirm_add_supplier")
            ) {
                Text("Save Supplier")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun PaySupplierDialog(
    supplier: SupplierEntity,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, method: PaymentMethod, note: String) -> Unit
) {
    var amountInput by remember { mutableStateOf(supplier.balanceOwed.toInt().toString()) }
    var selectedMethod by remember { mutableStateOf(PaymentMethod.BANK) }
    var noteInput by remember { mutableStateOf("Payment for purchase bill") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Pay Supplier Dues", fontWeight = FontWeight.Bold)
                Text(
                    text = "${supplier.company} • Balance Due: Rs. ${supplier.balanceOwed.toInt()}",
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
                    label = { Text("Payment Amount (Rs.) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("supplier_pay_amount_input"),
                    singleLine = true
                )

                Text("Payment Method:", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelMedium)
                listOf(PaymentMethod.BANK, PaymentMethod.QR, PaymentMethod.CASH).forEach { method ->
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
                    label = { Text("Bank Reference / Note") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountInput.toDoubleOrNull() ?: 0.0
                    if (amt > 0) onConfirm(amt, selectedMethod, noteInput)
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                modifier = Modifier.testTag("confirm_supplier_pay_btn")
            ) {
                Text("Record Payment")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun NewPurchaseDialog(
    suppliers: List<SupplierEntity>,
    products: List<ProductEntity>,
    onDismiss: () -> Unit,
    onConfirm: (SupplierEntity, List<PurchaseItemEntity>, Double, PaymentMethod, String) -> Unit
) {
    var selectedSupplier by remember { mutableStateOf(suppliers.firstOrNull()) }
    val purchaseItems = remember { mutableStateListOf<PurchaseItemEntity>() }
    var selectedProduct by remember { mutableStateOf<ProductEntity?>(null) }
    var qtyInput by remember { mutableStateOf("10") }
    var costInput by remember { mutableStateOf("") }
    var paidAmountInput by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf(PaymentMethod.CASH) }
    var notes by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    val totalPurchaseAmount = remember(purchaseItems.size) { purchaseItems.sumOf { it.total } }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record Inward Purchase Bill", fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (error != null) {
                    item { Text(error!!, color = CrimsonError, style = MaterialTheme.typography.bodySmall) }
                }

                // Supplier Selector
                item {
                    Text("Select Supplier:", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelMedium)
                    var supDropdown by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { supDropdown = true }
                                .background(Color(0xFFF1F5F9))
                                .padding(10.dp)
                        ) {
                            Text(selectedSupplier?.company ?: "Select Supplier", fontWeight = FontWeight.Bold)
                        }
                        DropdownMenu(expanded = supDropdown, onDismissRequest = { supDropdown = false }) {
                            suppliers.forEach { s ->
                                DropdownMenuItem(
                                    text = { Text("${s.company} (${s.name})") },
                                    onClick = {
                                        selectedSupplier = s
                                        supDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                item { Divider(modifier = Modifier.padding(vertical = 4.dp)) }

                // Add item to purchase
                item {
                    Text("Add Inward Product Item:", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelMedium)
                    var prodDropdown by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { prodDropdown = true }
                                .background(Color(0xFFF1F5F9))
                                .padding(10.dp)
                        ) {
                            Text(selectedProduct?.name ?: "Select Product to Restock", color = if (selectedProduct != null) Color.Black else Color.Gray)
                        }
                        DropdownMenu(expanded = prodDropdown, onDismissRequest = { prodDropdown = false }) {
                            products.forEach { p ->
                                DropdownMenuItem(
                                    text = { Text("${p.name} (Current Cost: Rs. ${p.purchasePrice.toInt()})") },
                                    onClick = {
                                        selectedProduct = p
                                        costInput = p.purchasePrice.toInt().toString()
                                        prodDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = qtyInput,
                            onValueChange = { qtyInput = it },
                            label = { Text("Quantity") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = costInput,
                            onValueChange = { costInput = it },
                            label = { Text("Unit Cost (Rs.)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1.2f),
                            singleLine = true
                        )
                        Button(
                            onClick = {
                                val p = selectedProduct
                                val q = qtyInput.toDoubleOrNull() ?: 0.0
                                val c = costInput.toDoubleOrNull() ?: 0.0
                                if (p != null && q > 0 && c > 0) {
                                    purchaseItems.add(
                                        PurchaseItemEntity(
                                            purchaseId = 0,
                                            productId = p.id,
                                            productName = p.name,
                                            unitCost = c,
                                            quantity = q,
                                            total = q * c
                                        )
                                    )
                                    selectedProduct = null
                                    costInput = ""
                                }
                            },
                            modifier = Modifier.align(Alignment.CenterVertically)
                        ) {
                            Text("Add")
                        }
                    }
                }

                // List of added items
                items(purchaseItems) { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth().background(Color(0xFFF8FAFC), RoundedCornerShape(6.dp)).padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(item.productName, fontWeight = FontWeight.Bold, maxLines = 1)
                            Text("${item.quantity} x Rs. ${item.unitCost.toInt()} = Rs. ${item.total.toInt()}", fontSize = 12.sp, color = Color.Gray)
                        }
                        IconButton(onClick = { purchaseItems.remove(item) }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove", tint = CrimsonError)
                        }
                    }
                }

                item {
                    Divider(modifier = Modifier.padding(vertical = 4.dp))
                    Text(
                        "Total Purchase Bill: Rs. ${String.format(Locale.US, "%.2f", totalPurchaseAmount)}",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = NavyPrimary
                    )
                }

                item {
                    OutlinedTextField(
                        value = paidAmountInput,
                        onValueChange = { paidAmountInput = it },
                        label = { Text("Amount Paid Now (Rs.)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Supplier Bill No. / Note") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val s = selectedSupplier
                    if (s == null) {
                        error = "Please select a supplier!"
                        return@Button
                    }
                    if (purchaseItems.isEmpty()) {
                        error = "Please add at least one purchase item!"
                        return@Button
                    }
                    val paid = paidAmountInput.toDoubleOrNull() ?: totalPurchaseAmount
                    onConfirm(s, purchaseItems.toList(), paid, paymentMethod, notes)
                },
                modifier = Modifier.testTag("confirm_inward_purchase")
            ) {
                Text("Confirm Purchase")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
