package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.ChatMessage
import com.example.data.ai.GeminiApiClient
import com.example.data.ai.GeminiModel
import com.example.data.ai.MessageRole
import com.example.data.database.RetailDatabase
import com.example.data.sync.SyncManager
import com.example.data.sync.SyncReport
import com.example.data.sync.SyncStatus
import com.example.data.entity.CustomerEntity
import com.example.data.entity.CustomerPaymentEntity
import com.example.data.entity.ExpenseEntity
import com.example.data.entity.OtherIncomeEntity
import com.example.data.entity.ProductEntity
import com.example.data.entity.PurchaseEntity
import com.example.data.entity.PurchaseItemEntity
import com.example.data.entity.SaleEntity
import com.example.data.entity.SaleItemEntity
import com.example.data.entity.ShopSettingsEntity
import com.example.data.entity.SupplierEntity
import com.example.data.entity.SupplierPaymentEntity
import com.example.data.repository.RetailRepository
import com.example.model.CartItem
import com.example.model.FinancialSummary
import com.example.model.PaymentMethod
import com.example.model.StaffRole
import com.example.model.StockReason
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class Screen(val title: String) {
    DASHBOARD("Dashboard"),
    POS("Point of Sale"),
    INVENTORY("Inventory"),
    SALES_HISTORY("Sales & Invoices"),
    CUSTOMERS("Customers & Khata"),
    SUPPLIERS("Suppliers & Purchases"),
    EXPENSES("Expenses & Income"),
    REPORTS("Profit & Loss / Reports"),
    SETTINGS("Shop Settings & Staff"),
    GEMINI_CHAT("AI Store Assistant"),
    VOICE_LIVE("Gemini 3.8 Live Voice")
}

class RetailViewModel(application: Application) : AndroidViewModel(application) {

    private val database = RetailDatabase.getDatabase(application)
    val repository: RetailRepository = RetailRepository(database)
    val syncManager: SyncManager = SyncManager(application, database, viewModelScope)

    // Cloud Synchronization State
    val syncStatus: StateFlow<SyncStatus> = syncManager.syncStatus
    val isOnline: StateFlow<Boolean> = syncManager.isOnline
    val lastSyncTimestamp: StateFlow<Long> = syncManager.lastSyncTimestamp
    val pendingChangesCount: StateFlow<Int> = syncManager.pendingChangesCount
    val lastSyncReport: StateFlow<SyncReport?> = syncManager.lastSyncReport

    fun triggerSyncNow() {
        viewModelScope.launch {
            val report = syncManager.performSync()
            showMessage(report.message)
        }
    }

    suspend fun exportDatabaseBackup(): String {
        return syncManager.exportFullBackupJson()
    }

    // Navigation and Role
    private val _currentScreen = MutableStateFlow(Screen.DASHBOARD)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _currentRole = MutableStateFlow(StaffRole.OWNER)
    val currentRole: StateFlow<StaffRole> = _currentRole.asStateFlow()

    // Notification toast / banner
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    // Repository Data Streams
    val products = repository.allProducts.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val customers = repository.allCustomers.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val suppliers = repository.allSuppliers.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val sales = repository.allSales.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val purchases = repository.allPurchases.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val expenses = repository.allExpenses.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val income = repository.allIncome.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val customerPayments = repository.allCustomerPayments.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val supplierPayments = repository.allSupplierPayments.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val stockAdjustments = repository.allStockAdjustments.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val auditLogs = repository.recentAuditLogs.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val shopSettings = repository.shopSettings.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ShopSettingsEntity())

    // All sale items cache for accurate COGS
    private val _allSaleItems = MutableStateFlow<List<SaleItemEntity>>(emptyList())
    val allSaleItems: StateFlow<List<SaleItemEntity>> = _allSaleItems.asStateFlow()

    // POS Cart State
    private val _cart = MutableStateFlow<List<CartItem>>(emptyList())
    val cart: StateFlow<List<CartItem>> = _cart.asStateFlow()

    private val _selectedCustomer = MutableStateFlow<CustomerEntity?>(null)
    val selectedCustomer: StateFlow<CustomerEntity?> = _selectedCustomer.asStateFlow()

    private val _discountAmount = MutableStateFlow(0.0)
    val discountAmount: StateFlow<Double> = _discountAmount.asStateFlow()

    private val _applyTax = MutableStateFlow(false)
    val applyTax: StateFlow<Boolean> = _applyTax.asStateFlow()

    // Receipt display state
    private val _lastSaleReceipt = MutableStateFlow<SaleEntity?>(null)
    val lastSaleReceipt: StateFlow<SaleEntity?> = _lastSaleReceipt.asStateFlow()

    private val _lastSaleItems = MutableStateFlow<List<SaleItemEntity>>(emptyList())
    val lastSaleItems: StateFlow<List<SaleItemEntity>> = _lastSaleItems.asStateFlow()

    // Search and filter
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // Reactive Financial Summary
    val financialSummary: StateFlow<FinancialSummary> = combine(
        sales, _allSaleItems, purchases, expenses, income, customers, suppliers, products, customerPayments, supplierPayments
    ) { args: Array<Any> ->
        @Suppress("UNCHECKED_CAST")
        repository.calculateFinancials(
            sales = args[0] as List<SaleEntity>,
            saleItems = args[1] as List<SaleItemEntity>,
            purchases = args[2] as List<PurchaseEntity>,
            expenses = args[3] as List<ExpenseEntity>,
            income = args[4] as List<OtherIncomeEntity>,
            customers = args[5] as List<CustomerEntity>,
            suppliers = args[6] as List<SupplierEntity>,
            products = args[7] as List<ProductEntity>,
            customerPayments = args[8] as List<CustomerPaymentEntity>,
            supplierPayments = args[9] as List<SupplierPaymentEntity>
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FinancialSummary())

    init {
        // Trigger auto seed if empty, and fetch sale items
        viewModelScope.launch {
            repository.seedSampleData()
            refreshSaleItems()
        }
    }

    private suspend fun refreshSaleItems() {
        val database = RetailDatabase.getDatabase(getApplication())
        _allSaleItems.value = database.saleDao().getAllSaleItems()
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun switchRole(role: StaffRole) {
        _currentRole.value = role
        showMessage("Switched mode to: ${role.title}")
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    fun showMessage(msg: String) {
        _userMessage.value = msg
    }

    fun clearMessage() {
        _userMessage.value = null
    }

    // Cart Operations
    fun addToCart(product: ProductEntity) {
        val currentList = _cart.value.toMutableList()
        val index = currentList.indexOfFirst { it.product.id == product.id }
        if (index >= 0) {
            val item = currentList[index]
            if (item.quantity + 1.0 > product.stockQuantity) {
                showMessage("Cannot exceed available stock of ${product.stockQuantity} ${product.unit}!")
                return
            }
            currentList[index] = item.copy(quantity = item.quantity + 1.0)
        } else {
            if (product.stockQuantity < 1.0) {
                showMessage("Item '${product.name}' is out of stock!")
                return
            }
            currentList.add(CartItem(product = product, quantity = 1.0, customPrice = product.sellingPrice))
        }
        _cart.value = currentList
    }

    fun updateCartQuantity(productId: Long, newQuantity: Double) {
        val currentList = _cart.value.toMutableList()
        val index = currentList.indexOfFirst { it.product.id == productId }
        if (index >= 0) {
            if (newQuantity <= 0.0) {
                currentList.removeAt(index)
            } else {
                val item = currentList[index]
                if (newQuantity > item.product.stockQuantity) {
                    showMessage("Stock limit: Only ${item.product.stockQuantity} available!")
                    return
                }
                currentList[index] = item.copy(quantity = newQuantity)
            }
            _cart.value = currentList
        }
    }

    fun removeFromCart(productId: Long) {
        _cart.value = _cart.value.filterNot { it.product.id == productId }
    }

    fun clearCart() {
        _cart.value = emptyList()
        _selectedCustomer.value = null
        _discountAmount.value = 0.0
        _applyTax.value = false
    }

    fun setDiscount(amount: Double) {
        _discountAmount.value = maxOf(0.0, amount)
    }

    fun setApplyTax(apply: Boolean) {
        _applyTax.value = apply
    }

    fun selectCustomer(customer: CustomerEntity?) {
        _selectedCustomer.value = customer
    }

    // Checkout
    fun checkout(
        paymentMethod: PaymentMethod,
        cashTendered: Double = 0.0,
        splitCash: Double = 0.0,
        splitDigital: Double = 0.0,
        notes: String = ""
    ) {
        val cartItems = _cart.value
        if (cartItems.isEmpty()) {
            showMessage("Cart is empty!")
            return
        }

        viewModelScope.launch {
            val result = repository.completeSale(
                cartItems = cartItems,
                customer = _selectedCustomer.value,
                discountAmount = _discountAmount.value,
                applyTax = _applyTax.value,
                paymentMethod = paymentMethod,
                cashTendered = cashTendered,
                splitCash = splitCash,
                splitDigital = splitDigital,
                cashierName = _currentRole.value.title,
                notes = notes
            )

            result.onSuccess { sale ->
                syncManager.markLocalDataModified()
                _lastSaleReceipt.value = sale
                _lastSaleItems.value = repository.getSaleItems(sale.id)
                refreshSaleItems()
                clearCart()
                showMessage("Sale recorded successfully! Invoice #${sale.invoiceNumber}")
            }.onFailure { error ->
                showMessage("Checkout failed: ${error.message}")
            }
        }
    }

    fun dismissReceipt() {
        _lastSaleReceipt.value = null
        _lastSaleItems.value = emptyList()
    }

    fun viewInvoiceReceipt(sale: SaleEntity) {
        viewModelScope.launch {
            _lastSaleReceipt.value = sale
            _lastSaleItems.value = repository.getSaleItems(sale.id)
        }
    }

    fun refundSale(saleId: Long, reason: String) {
        viewModelScope.launch {
            val result = repository.refundSale(saleId, reason)
            result.onSuccess {
                refreshSaleItems()
                dismissReceipt()
                showMessage("Sale refunded and stock returned to inventory.")
            }.onFailure { error ->
                showMessage("Refund failed: ${error.message}")
            }
        }
    }

    // Product Management
    fun createProduct(
        name: String,
        category: String,
        unit: String,
        purchasePrice: Double,
        sellingPrice: Double,
        initialStock: Double,
        minStock: Double,
        expiryDate: Long?,
        imageUri: String? = null,
        sku: String = "",
        barcode: String = ""
    ) {
        viewModelScope.launch {
            val generatedSku = if (sku.isBlank()) "PRD-${System.currentTimeMillis() % 1000000}" else sku.trim().uppercase()
            val p = ProductEntity(
                name = name.trim(),
                sku = generatedSku,
                barcode = barcode.trim(),
                category = category.trim().ifBlank { "General" },
                unit = unit.trim().ifBlank { "pcs" },
                purchasePrice = purchasePrice,
                sellingPrice = sellingPrice,
                stockQuantity = initialStock,
                minStockThreshold = minStock,
                expiryDate = expiryDate,
                imageUri = imageUri
            )
            val result = repository.addProduct(p)
            result.onSuccess {
                syncManager.markLocalDataModified()
                showMessage("Product '${p.name}' created successfully!")
            }.onFailure { error ->
                showMessage("Error adding product: ${error.message}")
            }
        }
    }

    fun updateProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.updateProduct(product)
            syncManager.markLocalDataModified()
            showMessage("Product '${product.name}' updated.")
        }
    }

    fun archiveProduct(productId: Long, name: String) {
        viewModelScope.launch {
            repository.archiveProduct(productId, name)
            syncManager.markLocalDataModified()
            showMessage("Product '$name' archived.")
        }
    }

    fun adjustStock(productId: Long, adjustmentQty: Double, reason: StockReason, note: String) {
        viewModelScope.launch {
            val res = repository.adjustStock(productId, adjustmentQty, reason, note)
            res.onSuccess {
                syncManager.markLocalDataModified()
                showMessage("Inventory stock adjusted successfully.")
            }.onFailure {
                showMessage("Stock adjustment failed: ${it.message}")
            }
        }
    }

    // Customer
    fun createCustomer(name: String, phone: String, address: String, creditLimit: Double, notes: String) {
        viewModelScope.launch {
            val c = CustomerEntity(
                name = name.trim(),
                phone = phone.trim(),
                address = address.trim(),
                creditLimit = creditLimit,
                notes = notes.trim()
            )
            repository.addCustomer(c).onSuccess {
                syncManager.markLocalDataModified()
                showMessage("Customer '${c.name}' added.")
            }.onFailure {
                showMessage("Failed to add customer: ${it.message}")
            }
        }
    }

    fun recordCustomerRepayment(customerId: Long, amount: Double, method: PaymentMethod, note: String) {
        viewModelScope.launch {
            repository.recordCustomerRepayment(customerId, amount, method, note).onSuccess {
                syncManager.markLocalDataModified()
                showMessage("Repayment of Rs. $amount recorded.")
            }.onFailure {
                showMessage("Repayment failed: ${it.message}")
            }
        }
    }

    // Supplier
    fun createSupplier(name: String, company: String, phone: String, address: String, notes: String) {
        viewModelScope.launch {
            val s = SupplierEntity(
                name = name.trim(),
                company = company.trim(),
                phone = phone.trim(),
                address = address.trim(),
                notes = notes.trim()
            )
            repository.addSupplier(s).onSuccess {
                syncManager.markLocalDataModified()
                showMessage("Supplier '${s.company}' added.")
            }.onFailure {
                showMessage("Failed to add supplier: ${it.message}")
            }
        }
    }

    fun recordSupplierPayment(supplierId: Long, amount: Double, method: PaymentMethod, note: String) {
        viewModelScope.launch {
            repository.recordSupplierPayment(supplierId, amount, method, note).onSuccess {
                syncManager.markLocalDataModified()
                showMessage("Payment of Rs. $amount to supplier recorded.")
            }.onFailure {
                showMessage("Supplier payment failed: ${it.message}")
            }
        }
    }

    // Purchase Inward
    fun recordPurchase(
        supplier: SupplierEntity,
        items: List<PurchaseItemEntity>,
        paidAmount: Double,
        method: PaymentMethod,
        notes: String
    ) {
        viewModelScope.launch {
            repository.completePurchase(supplier, items, paidAmount, method, notes).onSuccess {
                syncManager.markLocalDataModified()
                showMessage("Purchase order recorded and inventory replenished!")
            }.onFailure {
                showMessage("Purchase failed: ${it.message}")
            }
        }
    }

    // Expense & Income
    fun addExpense(title: String, category: String, amount: Double, method: PaymentMethod, ref: String, note: String) {
        viewModelScope.launch {
            val exp = ExpenseEntity(
                title = title.trim(),
                category = category.trim(),
                amount = amount,
                paymentMethod = method,
                reference = ref.trim(),
                note = note.trim()
            )
            repository.addExpense(exp).onSuccess {
                syncManager.markLocalDataModified()
                showMessage("Expense of Rs. $amount recorded.")
            }.onFailure {
                showMessage("Expense recording failed: ${it.message}")
            }
        }
    }

    fun addIncome(title: String, category: String, amount: Double, method: PaymentMethod, ref: String, note: String) {
        viewModelScope.launch {
            val inc = OtherIncomeEntity(
                title = title.trim(),
                category = category.trim(),
                amount = amount,
                paymentMethod = method,
                reference = ref.trim(),
                note = note.trim()
            )
            repository.addIncome(inc).onSuccess {
                syncManager.markLocalDataModified()
                showMessage("Income of Rs. $amount recorded.")
            }.onFailure {
                showMessage("Income recording failed: ${it.message}")
            }
        }
    }

    fun updateShopSettings(settings: ShopSettingsEntity) {
        viewModelScope.launch {
            repository.updateSettings(settings)
            syncManager.markLocalDataModified()
            showMessage("Shop settings saved.")
        }
    }

    fun resetAndReseedData() {
        viewModelScope.launch {
            repository.seedSampleData()
            refreshSaleItems()
            showMessage("Sample data reloaded.")
        }
    }

    // ==========================================
    // Gemini Chatbot & Live Voice API Integration
    // ==========================================
    private val geminiClient = GeminiApiClient()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                role = MessageRole.MODEL,
                text = "Namaste! I am Retail Master AI, your store advisor powered by Gemini. Ask me anything about your current inventory, today's sales, customer Udhaar, or strategies to grow your shop."
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _voiceMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val voiceMessages: StateFlow<List<ChatMessage>> = _voiceMessages.asStateFlow()

    private val _selectedChatModel = MutableStateFlow(GeminiModel.FLASH)
    val selectedChatModel: StateFlow<GeminiModel> = _selectedChatModel.asStateFlow()

    private val _isAiGenerating = MutableStateFlow(false)
    val isAiGenerating: StateFlow<Boolean> = _isAiGenerating.asStateFlow()

    fun setSelectedChatModel(model: GeminiModel) {
        _selectedChatModel.value = model
    }

    fun clearChatHistory() {
        _chatMessages.value = listOf(
            ChatMessage(
                role = MessageRole.MODEL,
                text = "Chat history cleared. How can I help your store today?"
            )
        )
    }

    fun clearVoiceHistory() {
        _voiceMessages.value = emptyList()
    }

    private fun buildStoreContextPrompt(): String {
        val s = shopSettings.value ?: ShopSettingsEntity()
        val fin = financialSummary.value
        val lowStockNames = products.value.filter { it.isLowStock }.map { it.name }.take(5).joinToString(", ")
        val topDebtors = customers.value.filter { it.balanceOwed > 0 }.sortedByDescending { it.balanceOwed }.take(3)
            .joinToString("; ") { "${it.name} owes Rs. ${it.balanceOwed.toInt()}" }

        return """
            You are 'Retail Master AI', an intelligent, helpful retail store assistant and business analyst for '${s.shopName}' owned by '${s.ownerName}', located in ${s.address}.
            Currency is ${s.currencyCode} (${s.currencySymbol}), standard VAT is ${s.taxRatePercent}%.
            
            Current Live Store Snapshot:
            - Net Sales Revenue: Rs. ${fin.netSales.toInt()} (${fin.totalCompletedSalesCount} transactions)
            - Gross Profit: Rs. ${fin.grossProfit.toInt()} (Margin: ${String.format(java.util.Locale.US, "%.1f", fin.grossMarginPercent)}%)
            - Operating Expenses: Rs. ${fin.totalExpenses.toInt()}
            - Net Operating Profit: Rs. ${fin.netOperatingProfit.toInt()}
            - Customer Credit (Udhaar) Outstanding: Rs. ${fin.totalReceivables.toInt()} (Key debtors: ${topDebtors.ifBlank { "None" }})
            - Supplier Dues: Rs. ${fin.totalPayables.toInt()}
            - Total Inventory SKUs: ${products.value.size} items (Cost Value: Rs. ${fin.inventoryCostValue.toInt()})
            - Low Stock Items: ${lowStockNames.ifBlank { "None, all well stocked" }}
            
            Instructions:
            1. Respond concisely, politely, and practically.
            2. When asked about inventory, sales, customer debts, or finance, use the live numbers above.
            3. When asked to draft messages (e.g., payment reminders), provide polite, professional text in both English and Nepali.
            4. Keep responses well-formatted and easy to read on mobile.
        """.trimIndent()
    }

    private fun generateLocalStoreAssistantAnswer(query: String): String {
        val q = query.lowercase().trim()
        val fin = financialSummary.value
        val lowStock = products.value.filter { it.isLowStock }
        val debtors = customers.value.filter { it.balanceOwed > 0 }.sortedByDescending { it.balanceOwed }

        return when {
            q.contains("hello") || q.contains("hi") || q.contains("namaste") || q.contains("hey") -> {
                "Namaste! I am Retail Master AI, your store advisor.\n\n" +
                "• Today's Sales: Rs. ${fin.netSales.toInt()} (${fin.totalCompletedSalesCount} bills)\n" +
                "• Est. Gross Profit: Rs. ${fin.grossProfit.toInt()} (${String.format(java.util.Locale.US, "%.1f", fin.grossMarginPercent)}% margin)\n" +
                "• Active Inventory: ${products.value.size} items (Cost: Rs. ${fin.inventoryCostValue.toInt()})\n" +
                "• Customer Udhaar Due: Rs. ${fin.totalReceivables.toInt()}\n\n" +
                "How can I help you manage your store today?"
            }
            q.contains("sale") || q.contains("revenue") || q.contains("today") || q.contains("earn") -> {
                "📊 **Today's Sales Breakdown**:\n" +
                "• Gross Revenue: Rs. ${fin.grossSales.toInt()}\n" +
                "• Net Sales: Rs. ${fin.netSales.toInt()}\n" +
                "• Transactions Completed: ${fin.totalCompletedSalesCount}\n" +
                "• Gross Profit: Rs. ${fin.grossProfit.toInt()} (${String.format(java.util.Locale.US, "%.1f", fin.grossMarginPercent)}%)\n" +
                "• Operating Expenses: Rs. ${fin.totalExpenses.toInt()}\n" +
                "• Net Operating Profit: Rs. ${fin.netOperatingProfit.toInt()}"
            }
            q.contains("stock") || q.contains("inventory") || q.contains("low") || q.contains("item") -> {
                if (lowStock.isEmpty()) {
                    "📦 **Inventory Status**: All products are well-stocked! Total active inventory is valued at Rs. ${fin.inventoryCostValue.toInt()} across ${products.value.size} items."
                } else {
                    val itemsList = lowStock.take(5).joinToString("\n") { "• ${it.name}: ${it.stockQuantity} ${it.unit} left (alert threshold: ${it.minStockThreshold})" }
                    "⚠️ **Low Stock Alert (${lowStock.size} items need reorder)**:\n$itemsList\n\nTotal stock valuation: Rs. ${fin.inventoryCostValue.toInt()}."
                }
            }
            q.contains("udhaar") || q.contains("credit") || q.contains("debt") || q.contains("customer") || q.contains("owe") || q.contains("khata") -> {
                if (debtors.isEmpty()) {
                    "🤝 **Customer Credit (Udhaar)**: No customers currently have outstanding credit balances. All accounts are settled!"
                } else {
                    val debtorsList = debtors.take(4).joinToString("\n") { "• ${it.name}: Rs. ${it.balanceOwed.toInt()} (Phone: ${it.phone.ifBlank { "N/A" }})" }
                    "📝 **Outstanding Customer Udhaar**: Rs. ${fin.totalReceivables.toInt()} total across ${debtors.size} customer accounts.\n\nTop balances:\n$debtorsList"
                }
            }
            q.contains("supplier") || q.contains("payable") || q.contains("vendor") -> {
                "🚚 **Supplier Accounts**: Total outstanding dues payable to suppliers: Rs. ${fin.totalPayables.toInt()}."
            }
            q.contains("profit") || q.contains("margin") || q.contains("loss") -> {
                "💰 **Profit & Loss Overview**:\n" +
                "• Net Sales: Rs. ${fin.netSales.toInt()}\n" +
                "• Cost of Goods Sold: Rs. ${fin.costOfGoodsSold.toInt()}\n" +
                "• Gross Profit: Rs. ${fin.grossProfit.toInt()} (${String.format(java.util.Locale.US, "%.1f", fin.grossMarginPercent)}%)\n" +
                "• Operating Expenses: Rs. ${fin.totalExpenses.toInt()}\n" +
                "• Net Profit: Rs. ${fin.netOperatingProfit.toInt()}"
            }
            else -> {
                "Namaste! Here is your store summary:\n" +
                "• Today's Sales: Rs. ${fin.netSales.toInt()} (${fin.totalCompletedSalesCount} transactions)\n" +
                "• Inventory: ${products.value.size} items in catalog (Rs. ${fin.inventoryCostValue.toInt()} cost)\n" +
                "• Customer Udhaar Due: Rs. ${fin.totalReceivables.toInt()}\n" +
                "• Low Stock Items: ${lowStock.size}\n\n" +
                "Ask me about your 'today's sales', 'low stock', 'customer credit', or 'profit' anytime!"
            }
        }
    }

    fun sendChatMessage(userText: String) {
        val text = userText.trim()
        if (text.isBlank()) return

        val userMsg = ChatMessage(role = MessageRole.USER, text = text)
        _chatMessages.value = _chatMessages.value + userMsg

        viewModelScope.launch {
            _isAiGenerating.value = true
            val systemPrompt = buildStoreContextPrompt()
            val model = _selectedChatModel.value

            val result = geminiClient.sendChatMessage(
                model = model,
                history = _chatMessages.value.dropLast(1),
                systemInstruction = systemPrompt,
                userPrompt = text
            )

            result.onSuccess { reply ->
                _chatMessages.value = _chatMessages.value + ChatMessage(
                    role = MessageRole.MODEL,
                    text = reply,
                    modelUsed = model.displayName
                )
            }.onFailure { error ->
                // Provide intelligent local fallback instead of leaving the user with a dead-end timeout error
                val localAnswer = generateLocalStoreAssistantAnswer(text)
                _chatMessages.value = _chatMessages.value + ChatMessage(
                    role = MessageRole.MODEL,
                    text = localAnswer,
                    modelUsed = "Retail Master Smart Advisor"
                )
            }

            _isAiGenerating.value = false
        }
    }

    fun sendLiveVoiceMessage(userText: String, onReplyReceived: (String) -> Unit) {
        val text = userText.trim()
        if (text.isBlank()) return

        val userMsg = ChatMessage(role = MessageRole.USER, text = text, isVoice = true)
        _voiceMessages.value = _voiceMessages.value + userMsg

        viewModelScope.launch {
            _isAiGenerating.value = true
            val systemPrompt = buildStoreContextPrompt() + "\nThis is a real-time voice conversation with model gemini-3.8-live. Keep spoken responses concise, natural, and conversational (1-3 sentences max)."

            val result = geminiClient.sendChatMessage(
                model = GeminiModel.LIVE,
                history = _voiceMessages.value.dropLast(1),
                systemInstruction = systemPrompt,
                userPrompt = text
            )

            result.onSuccess { reply ->
                _voiceMessages.value = _voiceMessages.value + ChatMessage(
                    role = MessageRole.MODEL,
                    text = reply,
                    modelUsed = GeminiModel.LIVE.displayName,
                    isVoice = true
                )
                onReplyReceived(reply)
            }.onFailure { error ->
                val localAnswer = generateLocalStoreAssistantAnswer(text)
                val spokenSummary = localAnswer.lines().firstOrNull { it.isNotBlank() && !it.startsWith("📊") && !it.startsWith("📦") && !it.startsWith("⚠️") }
                    ?: "Namaste! Here are your live store numbers."
                _voiceMessages.value = _voiceMessages.value + ChatMessage(
                    role = MessageRole.MODEL,
                    text = localAnswer,
                    modelUsed = "Retail Master Live Voice",
                    isVoice = true
                )
                onReplyReceived(spokenSummary)
            }

            _isAiGenerating.value = false
        }
    }
}
