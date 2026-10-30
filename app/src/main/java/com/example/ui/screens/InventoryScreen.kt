package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.ProductEntity
import com.example.data.entity.ShopSettingsEntity
import com.example.model.ExpiryStatus
import com.example.model.StockReason
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.AquaAccent
import com.example.ui.theme.CrimsonError
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.RoyalBlueBright
import java.util.Locale

@Composable
fun InventoryScreen(
    products: List<ProductEntity>,
    settings: ShopSettingsEntity,
    canManage: Boolean,
    onCreateProduct: (
        name: String,
        category: String,
        unit: String,
        purchasePrice: Double,
        sellingPrice: Double,
        initialStock: Double,
        minStock: Double,
        expiryDate: Long?,
        imageUri: String?
    ) -> Unit,
    onUpdateProduct: (ProductEntity) -> Unit,
    onArchiveProduct: (productId: Long, name: String) -> Unit,
    onAdjustStock: (productId: Long, qty: Double, reason: StockReason, note: String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var filterType by remember { mutableStateOf("ALL") } // ALL, LOW_STOCK, EXPIRING
    var showAddDialog by remember { mutableStateOf(false) }
    var adjustingProduct by remember { mutableStateOf<ProductEntity?>(null) }
    var editingProduct by remember { mutableStateOf<ProductEntity?>(null) }

    val filteredProducts = remember(products, searchQuery, filterType) {
        products.filter { p ->
            val matchesFilter = when (filterType) {
                "LOW_STOCK" -> p.isLowStock
                "EXPIRING" -> p.getExpiryStatus() == ExpiryStatus.EXPIRING_SOON || p.getExpiryStatus() == ExpiryStatus.EXPIRED
                else -> true
            }
            val matchesSearch = searchQuery.isBlank() ||
                p.name.contains(searchQuery, ignoreCase = true) ||
                p.sku.contains(searchQuery, ignoreCase = true) ||
                p.category.contains(searchQuery, ignoreCase = true) ||
                p.barcode.contains(searchQuery, ignoreCase = true)
            matchesFilter && matchesSearch
        }
    }

    val totalCostValue = remember(products) { products.sumOf { it.purchasePrice * it.stockQuantity } }
    val totalRetailValue = remember(products) { products.sumOf { it.sellingPrice * it.stockQuantity } }
    val potentialProfit = totalRetailValue - totalCostValue

    Box(modifier = Modifier.fillMaxSize().testTag("inventory_screen")) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Inventory Valuation Banner
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = NavyPrimary),
                modifier = Modifier.fillMaxWidth().testTag("inventory_valuation_card")
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Inventory Valuation", style = MaterialTheme.typography.labelMedium, color = AquaAccent)
                        Text(
                            text = "Rs. ${String.format(Locale.US, "%.0f", totalCostValue)}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "At Cost (${products.size} Active SKUs)",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("Expected Retail", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                        Text(
                            text = "Rs. ${String.format(Locale.US, "%.0f", totalRetailValue)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldSuccess
                        )
                        Text(
                            text = "+Rs. ${String.format(Locale.US, "%.0f", potentialProfit)} Margin",
                            style = MaterialTheme.typography.labelSmall,
                            color = AquaAccent,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search Inventory (Name, SKU, Barcode)") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().testTag("inventory_search_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Filter Pills (All, Low Stock, Expiring)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterPill(
                    label = "All (${products.size})",
                    isSelected = filterType == "ALL",
                    onClick = { filterType = "ALL" },
                    testTag = "filter_all"
                )
                val lowCount = products.count { it.isLowStock }
                FilterPill(
                    label = "Low Stock ($lowCount)",
                    isSelected = filterType == "LOW_STOCK",
                    onClick = { filterType = "LOW_STOCK" },
                    color = if (lowCount > 0) AmberWarning else null,
                    testTag = "filter_low_stock"
                )
                val expCount = products.count {
                    val s = it.getExpiryStatus()
                    s == ExpiryStatus.EXPIRING_SOON || s == ExpiryStatus.EXPIRED
                }
                FilterPill(
                    label = "Expiring ($expCount)",
                    isSelected = filterType == "EXPIRING",
                    onClick = { filterType = "EXPIRING" },
                    color = if (expCount > 0) CrimsonError else null,
                    testTag = "filter_expiring"
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Products list
            if (filteredProducts.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Inventory, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No products found matching filters.", color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredProducts, key = { it.id }) { product ->
                        ProductItemCard(
                            product = product,
                            canManage = canManage,
                            onAdjust = { adjustingProduct = product },
                            onEdit = { editingProduct = product },
                            onArchive = { onArchiveProduct(product.id, product.name) }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp)) // padding for FAB
                    }
                }
            }
        }

        // FAB to add new product
        if (canManage) {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = RoyalBlueBright,
                contentColor = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp)
                    .testTag("add_product_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Product")
            }
        }
    }

    // Add Product Dialog
    if (showAddDialog) {
        AddProductDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, cat, unit, cost, sell, stock, minStock, exp, imageUri ->
                showAddDialog = false
                onCreateProduct(name, cat, unit, cost, sell, stock, minStock, exp, imageUri)
            }
        )
    }

    // Stock Adjustment Dialog
    if (adjustingProduct != null) {
        StockAdjustmentDialog(
            product = adjustingProduct!!,
            onDismiss = { adjustingProduct = null },
            onConfirm = { qty, reason, note ->
                val p = adjustingProduct!!
                adjustingProduct = null
                onAdjustStock(p.id, qty, reason, note)
            }
        )
    }

    // Edit Product Dialog
    if (editingProduct != null) {
        EditProductDialog(
            product = editingProduct!!,
            onDismiss = { editingProduct = null },
            onConfirm = { updated ->
                editingProduct = null
                onUpdateProduct(updated)
            }
        )
    }
}

@Composable
private fun FilterPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    color: Color? = null,
    testTag: String
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) NavyPrimary else Color(0xFFF1F5F9),
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color.White else (color ?: Color(0xFF475569))
        )
    }
}

@Composable
fun ProductItemCard(
    product: ProductEntity,
    canManage: Boolean,
    onAdjust: () -> Unit,
    onEdit: () -> Unit,
    onArchive: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth().testTag("product_item_${product.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
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
                                fontSize = 24.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = product.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
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
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Rs. ${product.sellingPrice.toInt()}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = RoyalBlueBright
                    )
                    Text(
                        text = "Cost: Rs. ${product.purchasePrice.toInt()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Stock: ",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Text(
                        text = "${product.stockQuantity} ${product.unit}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (product.isLowStock) CrimsonError else EmeraldSuccess
                    )
                    if (product.isLowStock) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("(Low)", style = MaterialTheme.typography.labelSmall, color = CrimsonError, fontWeight = FontWeight.Bold)
                    }
                }

                // Expiry Status
                val expStatus = product.getExpiryStatus()
                if (expStatus != ExpiryStatus.NO_EXPIRY) {
                    val (tagColor, tagText) = when (expStatus) {
                        ExpiryStatus.EXPIRED -> Pair(CrimsonError, "EXPIRED")
                        ExpiryStatus.EXPIRING_SOON -> Pair(AmberWarning, "Expiring Soon")
                        else -> Pair(EmeraldSuccess, "Good")
                    }
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = tagColor.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = tagText,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = tagColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Profit margin
                Text(
                    text = "${String.format(Locale.US, "%.1f", product.profitMarginPercent)}% Margin",
                    style = MaterialTheme.typography.labelSmall,
                    color = AquaAccent,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (canManage) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onAdjust,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("adjust_stock_btn_${product.id}")
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Adjust Stock", fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp).testTag("edit_product_${product.id}")) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp))
                    }

                    IconButton(onClick = onArchive, modifier = Modifier.size(32.dp).testTag("archive_product_${product.id}")) {
                        Icon(Icons.Default.Archive, contentDescription = "Archive", tint = Color.Gray, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun AddProductDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        name: String,
        cat: String,
        unit: String,
        cost: Double,
        sell: Double,
        stock: Double,
        minStock: Double,
        expiryDate: Long?,
        imageUri: String?
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<String?>(null) }
    var category by remember { mutableStateOf("Staples & Grains") }
    var unit by remember { mutableStateOf("pcs") }
    var costPrice by remember { mutableStateOf("") }
    var sellPrice by remember { mutableStateOf("") }
    var stockQty by remember { mutableStateOf("10") }
    var minThreshold by remember { mutableStateOf("5") }
    var expiryDays by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedImageUri = uri.toString()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Product", fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (errorMessage != null) {
                    item {
                        Text(errorMessage!!, color = CrimsonError, style = MaterialTheme.typography.bodySmall)
                    }
                }

                // Product Picture Upload Option
                item {
                    Text("Product Picture", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                            .testTag("upload_product_photo_card")
                    ) {
                        if (!selectedImageUri.isNullOrBlank()) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFE2E8F0))
                                ) {
                                    AsyncImage(
                                        model = selectedImageUri,
                                        contentDescription = "Selected product photo",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Picture Attached", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = NavyPrimary)
                                    Text("Tap to change picture from device", style = MaterialTheme.typography.labelSmall, color = RoyalBlueBright)
                                }
                                IconButton(onClick = { selectedImageUri = null }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Remove photo", tint = CrimsonError)
                                }
                            }
                        } else {
                            Column(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .background(Color(0xFFE0E7FF), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Upload", tint = RoyalBlueBright)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Upload Product Picture", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = NavyPrimary)
                                Text("Tap to pick photo from your device", style = MaterialTheme.typography.labelSmall, color = Color(0xFF64748B))
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Product Name *") },
                        modifier = Modifier.fillMaxWidth().testTag("product_name_input"),
                        singleLine = true
                    )
                }

                item {
                    Text("Category Preset:", style = MaterialTheme.typography.labelSmall, color = Color(0xFF64748B))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        val presets = listOf(
                            "🌾 Grains" to "Staples & Grains",
                            "🛢️ Oils" to "Oils & Ghee",
                            "🥛 Dairy" to "Dairy & Milk",
                            "🍪 Snacks" to "Bakery & Snacks",
                            "🫖 Tea/Coffee" to "Beverages",
                            "🧼 Household" to "Household & Cleaners",
                            "🧂 Spices" to "Spices & Masala",
                            "🍎 Produce" to "Fresh Produce"
                        )
                        items(presets) { (presetLabel, presetCat) ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (category == presetCat) RoyalBlueBright else Color(0xFFF1F5F9),
                                modifier = Modifier.clickable { category = presetCat }
                            ) {
                                Text(
                                    presetLabel,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    fontSize = 11.sp,
                                    color = if (category == presetCat) Color.White else Color(0xFF334155)
                                )
                            }
                        }
                    }
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = category,
                            onValueChange = { category = it },
                            label = { Text("Category") },
                            modifier = Modifier.weight(1.2f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = unit,
                            onValueChange = { unit = it },
                            label = { Text("Unit (pcs/kg/pkt)") },
                            modifier = Modifier.weight(0.8f),
                            singleLine = true
                        )
                    }
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = costPrice,
                            onValueChange = { costPrice = it },
                            label = { Text("Cost Price (Rs.) *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f).testTag("product_cost_input"),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = sellPrice,
                            onValueChange = { sellPrice = it },
                            label = { Text("Selling Price (Rs.) *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f).testTag("product_sell_input"),
                            singleLine = true
                        )
                    }
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = stockQty,
                            onValueChange = { stockQty = it },
                            label = { Text("Initial Stock") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f).testTag("product_stock_input"),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = minThreshold,
                            onValueChange = { minThreshold = it },
                            label = { Text("Low Stock Alert") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = expiryDays,
                        onValueChange = { expiryDays = it },
                        label = { Text("Optional: Expires in X Days") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank() || costPrice.isBlank() || sellPrice.isBlank()) {
                        errorMessage = "Please fill product name and pricing fields (*)!"
                        return@Button
                    }
                    val cost = costPrice.toDoubleOrNull()
                    val sell = sellPrice.toDoubleOrNull()
                    if (cost == null || sell == null || cost < 0 || sell < 0) {
                        errorMessage = "Please enter valid numeric prices."
                        return@Button
                    }
                    val stock = stockQty.toDoubleOrNull() ?: 0.0
                    val minStock = minThreshold.toDoubleOrNull() ?: 5.0
                    val days = expiryDays.toLongOrNull()
                    val expDate = if (days != null && days > 0) System.currentTimeMillis() + (days * 24 * 60 * 60 * 1000L) else null

                    onConfirm(name, category, unit, cost, sell, stock, minStock, expDate, selectedImageUri)
                },
                colors = ButtonDefaults.buttonColors(containerColor = RoyalBlueBright),
                modifier = Modifier.testTag("confirm_create_product")
            ) {
                Text("Create Product")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun StockAdjustmentDialog(
    product: ProductEntity,
    onDismiss: () -> Unit,
    onConfirm: (qty: Double, reason: StockReason, note: String) -> Unit
) {
    var adjustmentQtyInput by remember { mutableStateOf("") }
    var selectedReason by remember { mutableStateOf(StockReason.RESTOCK) }
    var noteInput by remember { mutableStateOf("") }
    var isDeduction by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Adjust Stock: ${product.name}", fontWeight = FontWeight.Bold)
                Text("Current Stock: ${product.stockQuantity} ${product.unit}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            isDeduction = false
                            selectedReason = StockReason.RESTOCK
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = if (!isDeduction) EmeraldSuccess else Color(0xFFE2E8F0)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("+ Add Stock", color = if (!isDeduction) Color.White else Color.Black)
                    }

                    Button(
                        onClick = {
                            isDeduction = true
                            selectedReason = StockReason.DAMAGE
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isDeduction) CrimsonError else Color(0xFFE2E8F0)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("- Deduct Stock", color = if (isDeduction) Color.White else Color.Black)
                    }
                }

                OutlinedTextField(
                    value = adjustmentQtyInput,
                    onValueChange = { adjustmentQtyInput = it },
                    label = { Text("Quantity (${product.unit})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("adjustment_qty_input"),
                    singleLine = true
                )

                Text("Reason:", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelMedium)
                val reasons = if (isDeduction) {
                    listOf(StockReason.DAMAGE, StockReason.EXPIRED, StockReason.THEFT, StockReason.AUDIT)
                } else {
                    listOf(StockReason.RESTOCK, StockReason.PURCHASE, StockReason.AUDIT)
                }

                reasons.forEach { reason ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedReason = reason }
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = selectedReason == reason, onClick = { selectedReason = reason })
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(reason.displayName, fontSize = 13.sp)
                    }
                }

                OutlinedTextField(
                    value = noteInput,
                    onValueChange = { noteInput = it },
                    label = { Text("Note / Audit Remark") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val rawQty = adjustmentQtyInput.toDoubleOrNull() ?: 0.0
                    val finalQty = if (isDeduction) -rawQty else rawQty
                    onConfirm(finalQty, selectedReason, noteInput)
                },
                modifier = Modifier.testTag("confirm_stock_adjustment")
            ) {
                Text("Save Adjustment")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EditProductDialog(
    product: ProductEntity,
    onDismiss: () -> Unit,
    onConfirm: (ProductEntity) -> Unit
) {
    var name by remember { mutableStateOf(product.name) }
    var selectedImageUri by remember { mutableStateOf(product.imageUri) }
    var costPrice by remember { mutableStateOf(product.purchasePrice.toString()) }
    var sellPrice by remember { mutableStateOf(product.sellingPrice.toString()) }
    var minStock by remember { mutableStateOf(product.minStockThreshold.toString()) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedImageUri = uri.toString()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit ${product.name}", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Product Picture Upload Option
                Text("Product Picture", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelMedium)
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                ) {
                    if (!selectedImageUri.isNullOrBlank()) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFE2E8F0))
                            ) {
                                AsyncImage(
                                    model = selectedImageUri,
                                    contentDescription = "Selected product photo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Picture Attached", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, color = NavyPrimary)
                                Text("Tap to change picture", style = MaterialTheme.typography.labelSmall, color = RoyalBlueBright)
                            }
                            IconButton(onClick = { selectedImageUri = null }) {
                                Icon(Icons.Default.Clear, contentDescription = "Remove photo", tint = CrimsonError)
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Upload", tint = RoyalBlueBright)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Upload New Photo", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, color = NavyPrimary)
                        }
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Product Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = costPrice,
                    onValueChange = { costPrice = it },
                    label = { Text("Cost Price (Rs.)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = sellPrice,
                    onValueChange = { sellPrice = it },
                    label = { Text("Selling Price (Rs.)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = minStock,
                    onValueChange = { minStock = it },
                    label = { Text("Low Stock Threshold") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cost = costPrice.toDoubleOrNull() ?: product.purchasePrice
                    val sell = sellPrice.toDoubleOrNull() ?: product.sellingPrice
                    val threshold = minStock.toDoubleOrNull() ?: product.minStockThreshold
                    onConfirm(
                        product.copy(
                            name = name.trim(),
                            purchasePrice = cost,
                            sellingPrice = sell,
                            minStockThreshold = threshold,
                            imageUri = selectedImageUri
                        )
                    )
                }
            ) {
                Text("Update Product")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
