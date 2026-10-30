package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import coil.compose.AsyncImage
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.CustomerEntity
import com.example.data.entity.ProductEntity
import com.example.data.entity.ShopSettingsEntity
import com.example.model.CartItem
import com.example.model.PaymentMethod
import com.example.ui.theme.AquaAccent
import com.example.ui.theme.CrimsonError
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.RoyalBlueBright
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosScreen(
    products: List<ProductEntity>,
    customers: List<CustomerEntity>,
    cart: List<CartItem>,
    selectedCustomer: CustomerEntity?,
    discountAmount: Double,
    applyTax: Boolean,
    settings: ShopSettingsEntity,
    onAddToCart: (ProductEntity) -> Unit,
    onUpdateQuantity: (productId: Long, newQty: Double) -> Unit,
    onRemoveFromCart: (productId: Long) -> Unit,
    onClearCart: () -> Unit,
    onSelectCustomer: (CustomerEntity?) -> Unit,
    onSetDiscount: (Double) -> Unit,
    onSetApplyTax: (Boolean) -> Unit,
    onCheckout: (
        paymentMethod: PaymentMethod,
        cashTendered: Double,
        splitCash: Double,
        splitDigital: Double,
        notes: String
    ) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var showCartSheet by remember { mutableStateOf(false) }
    var showPaymentDialog by remember { mutableStateOf(false) }

    val categories = remember(products) {
        listOf("All") + products.map { it.category }.distinct().sorted()
    }

    val filteredProducts = remember(products, searchQuery, selectedCategory) {
        products.filter { p ->
            val matchesCategory = selectedCategory == "All" || p.category.equals(selectedCategory, ignoreCase = true)
            val matchesSearch = searchQuery.isBlank() ||
                p.name.contains(searchQuery, ignoreCase = true) ||
                p.sku.contains(searchQuery, ignoreCase = true) ||
                p.barcode.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    val subtotal = remember(cart) { cart.sumOf { it.subtotal } }
    val discountedSubtotal = remember(subtotal, discountAmount) { maxOf(0.0, subtotal - discountAmount) }
    val taxAmount = remember(discountedSubtotal, applyTax, settings) {
        if (applyTax) (discountedSubtotal * settings.taxRatePercent) / 100.0 else 0.0
    }
    val grandTotal = remember(discountedSubtotal, taxAmount) { discountedSubtotal + taxAmount }

    Box(modifier = Modifier.fillMaxSize().testTag("pos_screen")) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Search Bar & SKU/Barcode input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search by name, SKU, or Barcode") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pos_search_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Category Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { category ->
                    val isSelected = selectedCategory == category
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = category },
                        label = { Text(category, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NavyPrimary,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("category_chip_$category")
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Product List
            if (filteredProducts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No matching items found.", color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredProducts, key = { it.id }) { product ->
                        val cartItem = cart.find { it.product.id == product.id }
                        val inCartQty = cartItem?.quantity ?: 0.0

                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onAddToCart(product) }
                                .testTag("pos_product_card_${product.id}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFFF1F5F9)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (!product.imageUri.isNullOrBlank()) {
                                            AsyncImage(
                                                model = product.imageUri,
                                                contentDescription = product.name,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                            )
                                        } else {
                                            Text(
                                                text = when {
                                                    product.category.contains("Grain", true) || product.category.contains("Rice", true) -> "🌾"
                                                    product.category.contains("Oil", true) -> "🛢️"
                                                    product.category.contains("Dairy", true) || product.category.contains("Milk", true) -> "🥛"
                                                    product.category.contains("Snack", true) || product.category.contains("Bakery", true) -> "🍪"
                                                    product.category.contains("Beverage", true) || product.category.contains("Tea", true) -> "🫖"
                                                    product.category.contains("Spice", true) -> "🧂"
                                                    product.category.contains("Clean", true) -> "🧼"
                                                    product.category.contains("Fruit", true) || product.category.contains("Veg", true) -> "🍎"
                                                    else -> "📦"
                                                },
                                                fontSize = 22.sp
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column {
                                        Text(
                                            text = product.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1
                                        )
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = Color(0xFFF1F5F9)
                                            ) {
                                                Text(
                                                    text = product.category,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color(0xFF475569)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Stock: ${product.stockQuantity} ${product.unit}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = if (product.isLowStock) CrimsonError else Color(0xFF64748B),
                                                fontWeight = if (product.isLowStock) FontWeight.Bold else FontWeight.Normal
                                            )
                                        }
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Rs. ${product.sellingPrice.toInt()}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = RoyalBlueBright
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    if (inCartQty > 0) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .background(Color(0xFFE0E7FF), RoundedCornerShape(20.dp))
                                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            IconButton(
                                                onClick = { onUpdateQuantity(product.id, inCartQty - 1.0) },
                                                modifier = Modifier.size(28.dp).testTag("pos_minus_${product.id}")
                                            ) {
                                                Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(16.dp))
                                            }
                                            Text(
                                                text = "${inCartQty.toInt()}",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = NavyPrimary,
                                                modifier = Modifier.padding(horizontal = 6.dp)
                                            )
                                            IconButton(
                                                onClick = { onAddToCart(product) },
                                                modifier = Modifier.size(28.dp).testTag("pos_plus_${product.id}")
                                            ) {
                                                Icon(Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    } else {
                                        Button(
                                            onClick = { onAddToCart(product) },
                                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                            modifier = Modifier.testTag("pos_add_${product.id}")
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Add", fontSize = 13.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(90.dp)) // Padding for bottom cart bar
                    }
                }
            }
        }

        // Floating Bottom Cart Bar (if cart has items)
        if (cart.isNotEmpty()) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                color = NavyPrimary,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .clickable { showCartSheet = true }
                            .testTag("open_cart_bar")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${cart.size} Items in Cart",
                                style = MaterialTheme.typography.titleSmall,
                                color = AquaAccent,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("• Edit Cart", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                        }
                        Text(
                            text = "Total: Rs. ${String.format(Locale.US, "%.2f", grandTotal)}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Button(
                        onClick = { showPaymentDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("pos_checkout_button")
                    ) {
                        Icon(Icons.Default.PointOfSale, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Checkout", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Modal Bottom Sheet for Cart Detail
    if (showCartSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCartSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Current Cart (${cart.size} items)",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedButton(
                        onClick = {
                            onClearCart()
                            showCartSheet = false
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CrimsonError)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Clear")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Cart item list
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(cart) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.product.name, fontWeight = FontWeight.SemiBold, maxLines = 1)
                                Text("Rs. ${item.customPrice.toInt()} each", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { onUpdateQuantity(item.product.id, item.quantity - 1.0) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "Minus")
                                }
                                Text("${item.quantity.toInt()}", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp))
                                IconButton(
                                    onClick = { onUpdateQuantity(item.product.id, item.quantity + 1.0) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Plus")
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Rs. ${item.subtotal.toInt()}",
                                    fontWeight = FontWeight.Bold,
                                    color = RoyalBlueBright,
                                    modifier = Modifier.width(60.dp),
                                    textAlign = TextAlign.End
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Divider()
                Spacer(modifier = Modifier.height(10.dp))

                // Customer Selection
                CustomerPickerRow(
                    customers = customers,
                    selectedCustomer = selectedCustomer,
                    onSelectCustomer = onSelectCustomer
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Discount and Tax Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    var discountInput by remember(discountAmount) { mutableStateOf(if (discountAmount > 0) discountAmount.toString() else "") }
                    OutlinedTextField(
                        value = discountInput,
                        onValueChange = {
                            discountInput = it
                            onSetDiscount(it.toDoubleOrNull() ?: 0.0)
                        },
                        label = { Text("Discount (Rs.)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.width(150.dp).testTag("cart_discount_input"),
                        singleLine = true
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Add VAT (${settings.taxRatePercent}%)", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.width(6.dp))
                        Switch(
                            checked = applyTax,
                            onCheckedChange = { onSetApplyTax(it) },
                            modifier = Modifier.testTag("cart_tax_switch")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Order summary breakdown
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Subtotal", color = Color.Gray)
                    Text("Rs. ${String.format(Locale.US, "%.2f", subtotal)}", fontWeight = FontWeight.SemiBold)
                }
                if (discountAmount > 0) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Discount", color = EmeraldSuccess)
                        Text("- Rs. ${String.format(Locale.US, "%.2f", discountAmount)}", color = EmeraldSuccess, fontWeight = FontWeight.SemiBold)
                    }
                }
                if (taxAmount > 0) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("VAT (${settings.taxRatePercent}%)", color = Color.Gray)
                        Text("+ Rs. ${String.format(Locale.US, "%.2f", taxAmount)}", fontWeight = FontWeight.SemiBold)
                    }
                }
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Payable Total", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Rs. ${String.format(Locale.US, "%.2f", grandTotal)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = RoyalBlueBright)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        showCartSheet = false
                        showPaymentDialog = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("cart_proceed_to_payment"),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PointOfSale, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Proceed to Payment", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Payment Selection and Tender Dialog
    if (showPaymentDialog) {
        PaymentMethodDialog(
            grandTotal = grandTotal,
            selectedCustomer = selectedCustomer,
            onDismiss = { showPaymentDialog = false },
            onConfirm = { method, cashTendered, splitCash, splitDigital, notes ->
                showPaymentDialog = false
                onCheckout(method, cashTendered, splitCash, splitDigital, notes)
            }
        )
    }
}

@Composable
fun CustomerPickerRow(
    customers: List<CustomerEntity>,
    selectedCustomer: CustomerEntity?,
    onSelectCustomer: (CustomerEntity?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable { expanded = true }
                .background(Color(0xFFF1F5F9))
                .padding(12.dp),
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFFF1F5F9)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = NavyPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = selectedCustomer?.name ?: "Walk-in Customer (No Khata)",
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary
                        )
                        if (selectedCustomer != null) {
                            Text(
                                text = "Owes: Rs. ${selectedCustomer.balanceOwed.toInt()} (Limit: Rs. ${selectedCustomer.creditLimit.toInt()})",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (selectedCustomer.balanceOwed > 0) CrimsonError else Color.Gray
                            )
                        }
                    }
                }
                Text("Change", style = MaterialTheme.typography.labelMedium, color = RoyalBlueBright, fontWeight = FontWeight.Bold)
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            DropdownMenuItem(
                text = { Text("Walk-in Customer (Cash / Digital)") },
                onClick = {
                    onSelectCustomer(null)
                    expanded = false
                }
            )
            Divider()
            customers.forEach { customer ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(customer.name, fontWeight = FontWeight.Bold)
                            Text("Phone: ${customer.phone} • Balance: Rs. ${customer.balanceOwed.toInt()}", fontSize = 12.sp, color = Color.Gray)
                        }
                    },
                    onClick = {
                        onSelectCustomer(customer)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun PaymentMethodDialog(
    grandTotal: Double,
    selectedCustomer: CustomerEntity?,
    onDismiss: () -> Unit,
    onConfirm: (PaymentMethod, Double, Double, Double, String) -> Unit
) {
    var selectedMethod by remember { mutableStateOf(PaymentMethod.CASH) }
    var cashTenderedInput by remember { mutableStateOf(grandTotal.toInt().toString()) }
    var splitCashInput by remember { mutableStateOf((grandTotal / 2).toInt().toString()) }
    var splitDigitalInput by remember { mutableStateOf((grandTotal - (grandTotal / 2).toInt()).toInt().toString()) }
    var notesInput by remember { mutableStateOf("") }

    val cashTendered = cashTenderedInput.toDoubleOrNull() ?: 0.0
    val changeReturn = remember(cashTendered, grandTotal) { maxOf(0.0, cashTendered - grandTotal) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Select Payment Method", fontWeight = FontWeight.Bold)
                Text(
                    "Amount Payable: Rs. ${String.format(Locale.US, "%.2f", grandTotal)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = RoyalBlueBright,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Radio options
                PaymentMethod.values().forEach { method ->
                    val isAvailable = !(method == PaymentMethod.CREDIT && selectedCustomer == null)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(enabled = isAvailable) { selectedMethod = method }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedMethod == method,
                            onClick = { if (isAvailable) selectedMethod = method },
                            enabled = isAvailable,
                            modifier = Modifier.testTag("payment_radio_${method.name.lowercase()}")
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = method.displayName,
                                fontWeight = if (selectedMethod == method) FontWeight.Bold else FontWeight.Normal,
                                color = if (isAvailable) MaterialTheme.colorScheme.onSurface else Color.Gray
                            )
                            if (method == PaymentMethod.CREDIT && selectedCustomer == null) {
                                Text("(Select a registered customer for Udhaar)", style = MaterialTheme.typography.labelSmall, color = CrimsonError)
                            }
                        }
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 6.dp))

                // Cash Tender Calculation
                if (selectedMethod == PaymentMethod.CASH) {
                    OutlinedTextField(
                        value = cashTenderedInput,
                        onValueChange = { cashTenderedInput = it },
                        label = { Text("Cash Received from Customer (Rs.)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("cash_tendered_input"),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Change to Return:", fontWeight = FontWeight.SemiBold)
                        Text(
                            "Rs. ${String.format(Locale.US, "%.2f", changeReturn)}",
                            fontWeight = FontWeight.Bold,
                            color = if (changeReturn >= 0) EmeraldSuccess else CrimsonError
                        )
                    }
                } else if (selectedMethod == PaymentMethod.SPLIT) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = splitCashInput,
                            onValueChange = { splitCashInput = it },
                            label = { Text("Cash Amount") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f).testTag("split_cash_input"),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = splitDigitalInput,
                            onValueChange = { splitDigitalInput = it },
                            label = { Text("QR / Digital") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f).testTag("split_digital_input"),
                            singleLine = true
                        )
                    }
                }

                OutlinedTextField(
                    value = notesInput,
                    onValueChange = { notesInput = it },
                    label = { Text("Optional Note / Reference") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val splitCash = splitCashInput.toDoubleOrNull() ?: 0.0
                    val splitDigital = splitDigitalInput.toDoubleOrNull() ?: 0.0
                    onConfirm(selectedMethod, cashTendered, splitCash, splitDigital, notesInput)
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                modifier = Modifier.testTag("confirm_payment_button")
            ) {
                Text("Complete & Print Bill")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
