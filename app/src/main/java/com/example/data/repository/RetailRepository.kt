package com.example.data.repository

import androidx.room.withTransaction
import com.example.data.database.RetailDatabase
import com.example.data.entity.AuditLogEntity
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
import com.example.data.entity.StockAdjustmentEntity
import com.example.data.entity.SupplierEntity
import com.example.data.entity.SupplierPaymentEntity
import com.example.model.CartItem
import com.example.model.ExpiryStatus
import com.example.model.FinancialSummary
import com.example.model.PaymentMethod
import com.example.model.SaleStatus
import com.example.model.StockReason
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RetailRepository(private val database: RetailDatabase) {

    private val productDao = database.productDao()
    private val customerDao = database.customerDao()
    private val supplierDao = database.supplierDao()
    private val saleDao = database.saleDao()
    private val purchaseDao = database.purchaseDao()
    private val customerPaymentDao = database.customerPaymentDao()
    private val supplierPaymentDao = database.supplierPaymentDao()
    private val expenseDao = database.expenseDao()
    private val incomeDao = database.incomeDao()
    private val stockAdjustmentDao = database.stockAdjustmentDao()
    private val auditLogDao = database.auditLogDao()
    private val settingsDao = database.shopSettingsDao()

    // Reactive Flows
    val allProducts: Flow<List<ProductEntity>> = productDao.getAllActiveProducts()
    val allCustomers: Flow<List<CustomerEntity>> = customerDao.getAllCustomers()
    val allSuppliers: Flow<List<SupplierEntity>> = supplierDao.getAllSuppliers()
    val allSales: Flow<List<SaleEntity>> = saleDao.getAllSales()
    val allPurchases: Flow<List<PurchaseEntity>> = purchaseDao.getAllPurchases()
    val allExpenses: Flow<List<ExpenseEntity>> = expenseDao.getAllExpenses()
    val allIncome: Flow<List<OtherIncomeEntity>> = incomeDao.getAllIncome()
    val allCustomerPayments: Flow<List<CustomerPaymentEntity>> = customerPaymentDao.getAllCustomerPayments()
    val allSupplierPayments: Flow<List<SupplierPaymentEntity>> = supplierPaymentDao.getAllSupplierPayments()
    val allStockAdjustments: Flow<List<StockAdjustmentEntity>> = stockAdjustmentDao.getAllAdjustments()
    val recentAuditLogs: Flow<List<AuditLogEntity>> = auditLogDao.getRecentAuditLogs()
    val shopSettings: Flow<ShopSettingsEntity?> = settingsDao.getSettingsFlow()

    suspend fun getSettings(): ShopSettingsEntity {
        return settingsDao.getSettings() ?: ShopSettingsEntity().also {
            settingsDao.insertOrUpdate(it)
        }
    }

    suspend fun updateSettings(settings: ShopSettingsEntity) = withContext(Dispatchers.IO) {
        settingsDao.insertOrUpdate(settings)
        auditLogDao.insertAuditLog(
            AuditLogEntity(
                action = "SETTINGS_UPDATED",
                details = "Shop settings updated: ${settings.shopName}",
                staffRole = "Owner"
            )
        )
    }

    // Product Operations
    suspend fun addProduct(product: ProductEntity): Result<Long> = withContext(Dispatchers.IO) {
        try {
            val existing = productDao.getProductBySku(product.sku.trim())
            if (existing != null) {
                return@withContext Result.failure(IllegalArgumentException("Product with SKU '${product.sku}' already exists!"))
            }
            val id = productDao.insertProduct(product)
            auditLogDao.insertAuditLog(
                AuditLogEntity(
                    action = "PRODUCT_CREATED",
                    details = "Added product: ${product.name} (SKU: ${product.sku})",
                    staffRole = "Manager"
                )
            )
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProduct(product: ProductEntity) = withContext(Dispatchers.IO) {
        productDao.updateProduct(product)
        auditLogDao.insertAuditLog(
            AuditLogEntity(
                action = "PRODUCT_UPDATED",
                details = "Updated product: ${product.name} (Price: Rs. ${product.sellingPrice})",
                staffRole = "Manager"
            )
        )
    }

    suspend fun archiveProduct(productId: Long, productName: String) = withContext(Dispatchers.IO) {
        productDao.archiveProduct(productId)
        auditLogDao.insertAuditLog(
            AuditLogEntity(
                action = "PRODUCT_ARCHIVED",
                details = "Archived product: $productName (ID: $productId)",
                staffRole = "Manager"
            )
        )
    }

    suspend fun adjustStock(
        productId: Long,
        adjustmentQuantity: Double,
        reason: StockReason,
        note: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val product = productDao.getProductById(productId)
                ?: return@withContext Result.failure(IllegalArgumentException("Product not found"))

            val newStock = product.stockQuantity + adjustmentQuantity
            if (newStock < 0) {
                return@withContext Result.failure(IllegalArgumentException("Stock cannot be negative ($newStock)"))
            }

            productDao.updateStock(productId, newStock)
            stockAdjustmentDao.insertAdjustment(
                StockAdjustmentEntity(
                    productId = productId,
                    productName = product.name,
                    previousStock = product.stockQuantity,
                    adjustmentQuantity = adjustmentQuantity,
                    newStock = newStock,
                    reason = reason,
                    note = note
                )
            )
            auditLogDao.insertAuditLog(
                AuditLogEntity(
                    action = "STOCK_ADJUSTED",
                    details = "${product.name}: ${if (adjustmentQuantity >= 0) "+$adjustmentQuantity" else "$adjustmentQuantity"} (${reason.displayName})",
                    staffRole = "Inventory"
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Customer Operations
    suspend fun addCustomer(customer: CustomerEntity): Result<Long> = withContext(Dispatchers.IO) {
        try {
            val id = customerDao.insertCustomer(customer)
            auditLogDao.insertAuditLog(
                AuditLogEntity(
                    action = "CUSTOMER_ADDED",
                    details = "New customer: ${customer.name} (Phone: ${customer.phone})",
                    staffRole = "Cashier"
                )
            )
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateCustomer(customer: CustomerEntity) = withContext(Dispatchers.IO) {
        customerDao.updateCustomer(customer)
    }

    suspend fun recordCustomerRepayment(
        customerId: Long,
        amount: Double,
        paymentMethod: PaymentMethod,
        note: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            database.withTransaction {
                val customer = customerDao.getCustomerById(customerId)
                    ?: throw IllegalArgumentException("Customer not found")

                val updatedBalance = customer.balanceOwed - amount
                customerDao.updateCustomerBalance(customerId, updatedBalance)

                customerPaymentDao.insertPayment(
                    CustomerPaymentEntity(
                        customerId = customerId,
                        customerName = customer.name,
                        amount = amount,
                        paymentMethod = paymentMethod,
                        note = note
                    )
                )

                auditLogDao.insertAuditLog(
                    AuditLogEntity(
                        action = "CUSTOMER_REPAYMENT",
                        details = "${customer.name} paid Rs. $amount (${paymentMethod.displayName}). New balance: Rs. $updatedBalance",
                        staffRole = "Cashier"
                    )
                )
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Supplier Operations
    suspend fun addSupplier(supplier: SupplierEntity): Result<Long> = withContext(Dispatchers.IO) {
        try {
            val id = supplierDao.insertSupplier(supplier)
            auditLogDao.insertAuditLog(
                AuditLogEntity(
                    action = "SUPPLIER_ADDED",
                    details = "New supplier: ${supplier.company} (${supplier.name})",
                    staffRole = "Manager"
                )
            )
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateSupplier(supplier: SupplierEntity) = withContext(Dispatchers.IO) {
        supplierDao.updateSupplier(supplier)
    }

    suspend fun recordSupplierPayment(
        supplierId: Long,
        amount: Double,
        paymentMethod: PaymentMethod,
        note: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            database.withTransaction {
                val supplier = supplierDao.getSupplierById(supplierId)
                    ?: throw IllegalArgumentException("Supplier not found")

                val updatedBalance = supplier.balanceOwed - amount
                supplierDao.updateSupplierBalance(supplierId, updatedBalance)

                supplierPaymentDao.insertPayment(
                    SupplierPaymentEntity(
                        supplierId = supplierId,
                        supplierName = supplier.company,
                        amount = amount,
                        paymentMethod = paymentMethod,
                        note = note
                    )
                )

                auditLogDao.insertAuditLog(
                    AuditLogEntity(
                        action = "SUPPLIER_PAYMENT",
                        details = "Paid Rs. $amount to ${supplier.company} (${paymentMethod.displayName}). Remaining due: Rs. $updatedBalance",
                        staffRole = "Manager"
                    )
                )
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // POS & Sales Engine
    suspend fun completeSale(
        cartItems: List<CartItem>,
        customer: CustomerEntity?,
        discountAmount: Double,
        applyTax: Boolean,
        paymentMethod: PaymentMethod,
        cashTendered: Double,
        splitCash: Double,
        splitDigital: Double,
        cashierName: String,
        notes: String
    ): Result<SaleEntity> = withContext(Dispatchers.IO) {
        if (cartItems.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("Cart is empty!"))
        }

        try {
            val resultSale = database.withTransaction {
                // 1. Verify stock availability for all items to prevent negative stock
                for (item in cartItems) {
                    val p = productDao.getProductById(item.product.id)
                        ?: throw IllegalStateException("Product '${item.product.name}' no longer exists")
                    if (p.stockQuantity < item.quantity) {
                        throw IllegalStateException("Insufficient stock for '${p.name}'. Available: ${p.stockQuantity}, Requested: ${item.quantity}")
                    }
                }

                val settings = settingsDao.getSettings() ?: ShopSettingsEntity()
                val subtotal = cartItems.sumOf { it.subtotal }
                val discountedSubtotal = maxOf(0.0, subtotal - discountAmount)
                val taxRate = if (applyTax) settings.taxRatePercent else 0.0
                val taxAmount = (discountedSubtotal * taxRate) / 100.0
                val totalAmount = discountedSubtotal + taxAmount

                val (paidAmount, balanceDue) = when (paymentMethod) {
                    PaymentMethod.CREDIT -> {
                        if (customer == null) {
                            throw IllegalArgumentException("Credit sale requires selecting a registered customer")
                        }
                        Pair(0.0, totalAmount)
                    }
                    PaymentMethod.SPLIT -> {
                        val totalPaid = splitCash + splitDigital
                        val due = maxOf(0.0, totalAmount - totalPaid)
                        if (due > 0 && customer == null) {
                            throw IllegalArgumentException("Unpaid split amount (Rs. $due) requires a registered customer")
                        }
                        Pair(totalPaid, due)
                    }
                    else -> {
                        // CASH, BANK, QR
                        Pair(totalAmount, 0.0)
                    }
                }

                // Check customer credit limit
                if (balanceDue > 0 && customer != null) {
                    if (customer.balanceOwed + balanceDue > customer.creditLimit) {
                        throw IllegalStateException("Customer credit limit exceeded! Limit: Rs. ${customer.creditLimit}, Current balance: Rs. ${customer.balanceOwed}, New charge: Rs. $balanceDue")
                    }
                }

                val dateStr = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date())
                val invoiceNumber = "${settings.invoicePrefix}$dateStr"

                val saleEntity = SaleEntity(
                    invoiceNumber = invoiceNumber,
                    customerId = customer?.id,
                    customerName = customer?.name ?: "Walk-in Customer",
                    customerPhone = customer?.phone ?: "",
                    subtotal = subtotal,
                    discountAmount = discountAmount,
                    taxRatePercent = taxRate,
                    taxAmount = taxAmount,
                    totalAmount = totalAmount,
                    paidAmount = paidAmount,
                    balanceDue = balanceDue,
                    paymentMethod = paymentMethod,
                    splitCashAmount = if (paymentMethod == PaymentMethod.SPLIT) splitCash else 0.0,
                    splitDigitalAmount = if (paymentMethod == PaymentMethod.SPLIT) splitDigital else 0.0,
                    status = SaleStatus.COMPLETED,
                    timestamp = System.currentTimeMillis(),
                    cashierName = cashierName,
                    notes = notes
                )

                val saleId = saleDao.insertSale(saleEntity)

                // Insert sale items and deduct stock
                val saleItems = cartItems.map { item ->
                    val p = productDao.getProductById(item.product.id)!!
                    val newStock = p.stockQuantity - item.quantity
                    productDao.updateStock(p.id, newStock)

                    SaleItemEntity(
                        saleId = saleId,
                        productId = p.id,
                        productName = p.name,
                        unit = p.unit,
                        unitPrice = item.customPrice,
                        costPrice = p.purchasePrice,
                        quantity = item.quantity,
                        total = item.subtotal
                    )
                }
                saleDao.insertSaleItems(saleItems)

                // Update customer balance if credit sale
                if (balanceDue > 0 && customer != null) {
                    customerDao.updateCustomerBalance(customer.id, customer.balanceOwed + balanceDue)
                }

                auditLogDao.insertAuditLog(
                    AuditLogEntity(
                        action = "SALE_COMPLETED",
                        details = "Invoice $invoiceNumber for Rs. $totalAmount ($paymentMethod, Items: ${cartItems.size})",
                        staffRole = cashierName
                    )
                )

                saleEntity.copy(id = saleId)
            }
            Result.success(resultSale)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSaleItems(saleId: Long): List<SaleItemEntity> = withContext(Dispatchers.IO) {
        saleDao.getSaleItems(saleId)
    }

    suspend fun refundSale(saleId: Long, reason: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            database.withTransaction {
                val sale = saleDao.getSaleById(saleId)
                    ?: throw IllegalArgumentException("Sale not found")
                if (sale.status == SaleStatus.REFUNDED) {
                    throw IllegalStateException("Sale is already refunded")
                }

                // Restore inventory stock
                val items = saleDao.getSaleItems(saleId)
                for (item in items) {
                    val product = productDao.getProductById(item.productId)
                    if (product != null) {
                        val restoredStock = product.stockQuantity + item.quantity
                        productDao.updateStock(product.id, restoredStock)
                        stockAdjustmentDao.insertAdjustment(
                            StockAdjustmentEntity(
                                productId = product.id,
                                productName = product.name,
                                previousStock = product.stockQuantity,
                                adjustmentQuantity = item.quantity,
                                newStock = restoredStock,
                                reason = StockReason.RESTOCK,
                                note = "Refund Invoice #${sale.invoiceNumber}: $reason"
                            )
                        )
                    }
                }

                // Reverse customer credit if owed
                if (sale.balanceDue > 0 && sale.customerId != null) {
                    val customer = customerDao.getCustomerById(sale.customerId)
                    if (customer != null) {
                        customerDao.updateCustomerBalance(customer.id, maxOf(0.0, customer.balanceOwed - sale.balanceDue))
                    }
                }

                saleDao.updateSaleStatus(saleId, SaleStatus.REFUNDED)
                auditLogDao.insertAuditLog(
                    AuditLogEntity(
                        action = "SALE_REFUNDED",
                        details = "Refunded Invoice #${sale.invoiceNumber} (Rs. ${sale.totalAmount}). Reason: $reason",
                        staffRole = "Manager"
                    )
                )
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Purchase Management
    suspend fun completePurchase(
        supplier: SupplierEntity,
        items: List<PurchaseItemEntity>,
        paidAmount: Double,
        paymentMethod: PaymentMethod,
        notes: String
    ): Result<Long> = withContext(Dispatchers.IO) {
        if (items.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("Purchase items cannot be empty"))
        }

        try {
            val purchaseId = database.withTransaction {
                val totalAmount = items.sumOf { it.total }
                val balanceDue = maxOf(0.0, totalAmount - paidAmount)

                val dateStr = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date())
                val invoiceNumber = "PUR-$dateStr"

                val purchase = PurchaseEntity(
                    invoiceNumber = invoiceNumber,
                    supplierId = supplier.id,
                    supplierName = supplier.company,
                    totalAmount = totalAmount,
                    paidAmount = paidAmount,
                    balanceDue = balanceDue,
                    paymentMethod = paymentMethod,
                    timestamp = System.currentTimeMillis(),
                    isReceived = true,
                    notes = notes
                )

                val id = purchaseDao.insertPurchase(purchase)

                // Add items and update product stock & purchase cost
                val itemsWithId = items.map { item ->
                    val product = productDao.getProductById(item.productId)
                    if (product != null) {
                        val newStock = product.stockQuantity + item.quantity
                        productDao.updateProduct(
                            product.copy(
                                stockQuantity = newStock,
                                purchasePrice = item.unitCost
                            )
                        )
                    }
                    item.copy(purchaseId = id)
                }
                purchaseDao.insertPurchaseItems(itemsWithId)

                // Update supplier balance if dues remaining
                if (balanceDue > 0) {
                    supplierDao.updateSupplierBalance(supplier.id, supplier.balanceOwed + balanceDue)
                }

                auditLogDao.insertAuditLog(
                    AuditLogEntity(
                        action = "PURCHASE_RECEIVED",
                        details = "Purchase $invoiceNumber from ${supplier.company} for Rs. $totalAmount (Stock updated for ${items.size} items)",
                        staffRole = "Manager"
                    )
                )

                id
            }
            Result.success(purchaseId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Expenses & Income
    suspend fun addExpense(expense: ExpenseEntity): Result<Long> = withContext(Dispatchers.IO) {
        try {
            val id = expenseDao.insertExpense(expense)
            auditLogDao.insertAuditLog(
                AuditLogEntity(
                    action = "EXPENSE_RECORDED",
                    details = "${expense.title} (Rs. ${expense.amount}, ${expense.category})",
                    staffRole = "Manager"
                )
            )
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addIncome(income: OtherIncomeEntity): Result<Long> = withContext(Dispatchers.IO) {
        try {
            val id = incomeDao.insertIncome(income)
            auditLogDao.insertAuditLog(
                AuditLogEntity(
                    action = "INCOME_RECORDED",
                    details = "${income.title} (Rs. ${income.amount}, ${income.category})",
                    staffRole = "Manager"
                )
            )
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Comprehensive Financial Calculations
    fun calculateFinancials(
        sales: List<SaleEntity>,
        saleItems: List<SaleItemEntity>,
        purchases: List<PurchaseEntity>,
        expenses: List<ExpenseEntity>,
        income: List<OtherIncomeEntity>,
        customers: List<CustomerEntity>,
        suppliers: List<SupplierEntity>,
        products: List<ProductEntity>,
        customerPayments: List<CustomerPaymentEntity>,
        supplierPayments: List<SupplierPaymentEntity>
    ): FinancialSummary {
        val completedSales = sales.filter { it.status == SaleStatus.COMPLETED }
        val refundedSales = sales.filter { it.status == SaleStatus.REFUNDED }

        val grossSales = completedSales.sumOf { it.subtotal }
        val totalDiscounts = completedSales.sumOf { it.discountAmount }
        val totalTax = completedSales.sumOf { it.taxAmount }
        val totalRefunds = refundedSales.sumOf { it.totalAmount }

        val netSales = maxOf(0.0, grossSales - totalDiscounts)

        // COGS for completed sales: line item unit cost * quantity
        val completedSaleIds = completedSales.map { it.id }.toSet()
        val cogs = saleItems
            .filter { it.saleId in completedSaleIds }
            .sumOf { it.costPrice * it.quantity }

        val grossProfit = netSales - cogs
        val grossMarginPercent = if (netSales > 0) (grossProfit / netSales) * 100.0 else 0.0

        val totalExpenses = expenses.sumOf { it.amount }
        val totalOtherIncome = income.sumOf { it.amount }

        val netOperatingProfit = grossProfit - totalExpenses + totalOtherIncome
        val netMarginPercent = if (netSales > 0) (netOperatingProfit / netSales) * 100.0 else 0.0

        val totalReceivables = customers.sumOf { maxOf(0.0, it.balanceOwed) }
        val totalPayables = suppliers.sumOf { maxOf(0.0, it.balanceOwed) }

        val inventoryCostValue = products.sumOf { it.purchasePrice * it.stockQuantity }
        val inventoryRetailValue = products.sumOf { it.sellingPrice * it.stockQuantity }

        // Cash flow in hand estimation
        val cashFromSales = completedSales
            .filter { it.paymentMethod == PaymentMethod.CASH }
            .sumOf { it.paidAmount } +
            completedSales.filter { it.paymentMethod == PaymentMethod.SPLIT }.sumOf { it.splitCashAmount }
        val cashFromRepayments = customerPayments
            .filter { it.paymentMethod == PaymentMethod.CASH }
            .sumOf { it.amount }
        val cashSpentExpenses = expenses
            .filter { it.paymentMethod == PaymentMethod.CASH }
            .sumOf { it.amount }
        val cashSpentPurchases = purchases
            .filter { it.paymentMethod == PaymentMethod.CASH }
            .sumOf { it.paidAmount }
        val cashSpentSupplierPayments = supplierPayments
            .filter { it.paymentMethod == PaymentMethod.CASH }
            .sumOf { it.amount }

        val cashInHand = (cashFromSales + cashFromRepayments) - (cashSpentExpenses + cashSpentPurchases + cashSpentSupplierPayments)

        val lowStockCount = products.count { it.isLowStock }
        val expiringCount = products.count { it.getExpiryStatus() == ExpiryStatus.EXPIRING_SOON || it.getExpiryStatus() == ExpiryStatus.EXPIRED }

        return FinancialSummary(
            grossSales = grossSales,
            totalDiscounts = totalDiscounts,
            totalTaxCollected = totalTax,
            totalRefunds = totalRefunds,
            netSales = netSales,
            costOfGoodsSold = cogs,
            grossProfit = grossProfit,
            grossMarginPercent = grossMarginPercent,
            totalExpenses = totalExpenses,
            totalOtherIncome = totalOtherIncome,
            netOperatingProfit = netOperatingProfit,
            netMarginPercent = netMarginPercent,
            totalReceivables = totalReceivables,
            totalPayables = totalPayables,
            inventoryCostValue = inventoryCostValue,
            inventoryRetailValue = inventoryRetailValue,
            cashInHand = cashInHand,
            totalCompletedSalesCount = completedSales.size,
            lowStockCount = lowStockCount,
            expiringSoonCount = expiringCount
        )
    }

    // Seed Development Data
    suspend fun seedSampleData() = withContext(Dispatchers.IO) {
        val count = productDao.getProductCount()
        if (count > 0) return@withContext

        // Initialize shop settings
        settingsDao.insertOrUpdate(
            ShopSettingsEntity(
                id = 1,
                shopName = "Pasal Kendra Retail & Mart",
                ownerName = "Ayoush Karn",
                phone = "+977-9801234567",
                address = "New Road, Kathmandu, Nepal",
                panVatNumber = "601234567",
                currencyCode = "NPR",
                currencySymbol = "Rs.",
                taxRatePercent = 13.0,
                isTaxEnabledByDefault = false,
                invoicePrefix = "INV-",
                receiptFooter = "Dhanyabad! Thank you for shopping with us! Please visit again.",
                timezone = "Asia/Kathmandu"
            )
        )

        // Seed Suppliers
        val suppliers = listOf(
            SupplierEntity(
                name = "Binod Shrestha",
                company = "Bishal Agro & Grain Distributors",
                phone = "9841230001",
                address = "Kalimati, Kathmandu",
                balanceOwed = 14500.0,
                notes = "Primary supplier for rice, grains, and lentils"
            ),
            SupplierEntity(
                name = "Prakash Thapa",
                company = "Himalayan FMCG Wholesalers",
                phone = "9851098765",
                address = "Balaju Industrial Area, Kathmandu",
                balanceOwed = 8200.0,
                notes = "Supplier for soaps, toiletries, packaged foods"
            ),
            SupplierEntity(
                name = "Ramesh Chaudhary",
                company = "Chaudhary Group (CG) Foods Depot",
                phone = "9801223344",
                address = "Sanepa, Lalitpur",
                balanceOwed = 0.0,
                notes = "Wai Wai and beverage distribution"
            )
        )
        supplierDao.insertSuppliers(suppliers)

        // Seed Customers
        val customers = listOf(
            CustomerEntity(
                name = "Sita Sharma",
                phone = "9841122334",
                address = "Thamel, Kathmandu",
                creditLimit = 15000.0,
                balanceOwed = 3200.0,
                notes = "Local family account. Clears dues every month."
            ),
            CustomerEntity(
                name = "Ramesh Adhikari",
                phone = "9851234567",
                address = "Ason, Kathmandu",
                creditLimit = 10000.0,
                balanceOwed = 1450.0,
                notes = "Tea stall owner, frequent bulk purchaser"
            ),
            CustomerEntity(
                name = "Sunita Gurung",
                phone = "9818765432",
                address = "Baneshwor, Kathmandu",
                creditLimit = 8000.0,
                balanceOwed = 0.0,
                notes = "Regular cash payer"
            ),
            CustomerEntity(
                name = "Kishore Maharjan",
                phone = "9803456789",
                address = "Patan, Lalitpur",
                creditLimit = 12000.0,
                balanceOwed = 5400.0,
                notes = "Requested reminder before 1st of month"
            )
        )
        customerDao.insertCustomers(customers)

        // Seed Products with realistic Nepali grocery pricing
        val now = System.currentTimeMillis()
        val oneDayMillis = 24 * 60 * 60 * 1000L

        val products = listOf(
            ProductEntity(
                sku = "RICE-BAS-25",
                barcode = "890123400001",
                name = "Basmati Premium Rice (25kg Bag)",
                category = "Staples & Grains",
                unit = "bag",
                purchasePrice = 2200.0,
                sellingPrice = 2550.0,
                stockQuantity = 14.0,
                minStockThreshold = 5.0
            ),
            ProductEntity(
                sku = "OIL-SUN-1L",
                barcode = "890123400002",
                name = "Sunflow Pure Sunflower Oil (1 Liter)",
                category = "Cooking Essentials",
                unit = "bottle",
                purchasePrice = 210.0,
                sellingPrice = 250.0,
                stockQuantity = 38.0,
                minStockThreshold = 10.0
            ),
            ProductEntity(
                sku = "DAL-MUS-1KG",
                barcode = "890123400003",
                name = "Musuro Dal / Red Lentils (1 kg)",
                category = "Staples & Grains",
                unit = "kg",
                purchasePrice = 135.0,
                sellingPrice = 165.0,
                stockQuantity = 28.0,
                minStockThreshold = 8.0
            ),
            ProductEntity(
                sku = "SALT-TAT-1KG",
                barcode = "890123400004",
                name = "Tata Iodized Salt (1 kg packet)",
                category = "Cooking Essentials",
                unit = "pkt",
                purchasePrice = 22.0,
                sellingPrice = 28.0,
                stockQuantity = 55.0,
                minStockThreshold = 15.0
            ),
            ProductEntity(
                sku = "TEA-TOK-500",
                barcode = "890123400005",
                name = "Tokla Gold Orthodox CTC Tea (500g)",
                category = "Beverages",
                unit = "pkt",
                purchasePrice = 310.0,
                sellingPrice = 380.0,
                stockQuantity = 22.0,
                minStockThreshold = 6.0
            ),
            ProductEntity(
                sku = "SUG-CHI-1KG",
                barcode = "890123400006",
                name = "White Refined Sugar / Chini (1 kg)",
                category = "Staples & Grains",
                unit = "kg",
                purchasePrice = 85.0,
                sellingPrice = 105.0,
                stockQuantity = 64.0,
                minStockThreshold = 15.0
            ),
            ProductEntity(
                sku = "NOD-WAI-CTN",
                barcode = "890123400007",
                name = "Wai Wai Quick Instant Noodles (Carton of 30)",
                category = "Snacks & Packaged",
                unit = "carton",
                purchasePrice = 630.0,
                sellingPrice = 750.0,
                stockQuantity = 12.0,
                minStockThreshold = 5.0
            ),
            ProductEntity(
                sku = "NOD-CUR-75G",
                barcode = "890123400008",
                name = "Current 2X Hot & Spicy Noodles (75g)",
                category = "Snacks & Packaged",
                unit = "pcs",
                purchasePrice = 42.0,
                sellingPrice = 55.0,
                stockQuantity = 3.0, // Low stock triggered!
                minStockThreshold = 10.0
            ),
            ProductEntity(
                sku = "DAIRY-MILK-POUCH",
                barcode = "890123400009",
                name = "DDC Fresh Cow Milk Pouch (500ml)",
                category = "Dairy & Bakery",
                unit = "pouch",
                purchasePrice = 45.0,
                sellingPrice = 55.0,
                stockQuantity = 18.0,
                minStockThreshold = 8.0,
                expiryDate = now + (3 * oneDayMillis) // Expiring in 3 days!
            ),
            ProductEntity(
                sku = "AMUL-BUT-500",
                barcode = "890123400010",
                name = "Amul Pasteurized Butter (500g)",
                category = "Dairy & Bakery",
                unit = "block",
                purchasePrice = 410.0,
                sellingPrice = 485.0,
                stockQuantity = 4.0, // Low stock!
                minStockThreshold = 6.0,
                expiryDate = now + (25 * oneDayMillis)
            ),
            ProductEntity(
                sku = "SOAP-DET-125",
                barcode = "890123400011",
                name = "Dettol Original Germ Protection Soap (125g)",
                category = "Personal & Home Care",
                unit = "pcs",
                purchasePrice = 64.0,
                sellingPrice = 80.0,
                stockQuantity = 34.0,
                minStockThreshold = 10.0
            ),
            ProductEntity(
                sku = "DET-SUN-1KG",
                barcode = "890123400012",
                name = "Sunlight Detergent Powder (1kg pack)",
                category = "Personal & Home Care",
                unit = "pkt",
                purchasePrice = 115.0,
                sellingPrice = 145.0,
                stockQuantity = 24.0,
                minStockThreshold = 8.0
            ),
            ProductEntity(
                sku = "SHAM-HEAD-180",
                barcode = "890123400013",
                name = "Head & Shoulders Anti-Dandruff Shampoo (180ml)",
                category = "Personal & Home Care",
                unit = "bottle",
                purchasePrice = 215.0,
                sellingPrice = 265.0,
                stockQuantity = 9.0,
                minStockThreshold = 5.0
            ),
            ProductEntity(
                sku = "SPICE-TUR-200",
                barcode = "890123400014",
                name = "Everest Pure Turmeric Powder / Besar (200g)",
                category = "Cooking Essentials",
                unit = "pkt",
                purchasePrice = 75.0,
                sellingPrice = 95.0,
                stockQuantity = 30.0,
                minStockThreshold = 10.0
            ),
            ProductEntity(
                sku = "BIS-MAR-800",
                barcode = "890123400015",
                name = "Britannia Marie Gold Family Pack (800g)",
                category = "Snacks & Packaged",
                unit = "pkt",
                purchasePrice = 130.0,
                sellingPrice = 160.0,
                stockQuantity = 25.0,
                minStockThreshold = 8.0
            )
        )
        productDao.insertProducts(products)

        // Seed Operating Expenses
        val expenses = listOf(
            ExpenseEntity(
                title = "Monthly Shop Rent (New Road)",
                category = "Rent",
                amount = 25000.0,
                paymentMethod = PaymentMethod.BANK,
                reference = "TXN-RENT-OCT",
                note = "Paid to Landlord Mr. Bajracharya",
                timestamp = now - (5 * oneDayMillis)
            ),
            ExpenseEntity(
                title = "NEA Electricity Bill (Meter 9942)",
                category = "Electricity / Utility",
                amount = 3450.0,
                paymentMethod = PaymentMethod.QR,
                reference = "FONEPAY-NEA-9942",
                note = "Nepal Electricity Authority online bill",
                timestamp = now - (3 * oneDayMillis)
            ),
            ExpenseEntity(
                title = "Helper & Delivery Staff Advance",
                category = "Staff Salary",
                amount = 8000.0,
                paymentMethod = PaymentMethod.CASH,
                reference = "CASH-ADV",
                note = "Staff advance to Ram Bahadur",
                timestamp = now - (2 * oneDayMillis)
            ),
            ExpenseEntity(
                title = "Carry Bags & Packaging Rolls",
                category = "Packaging",
                amount = 1200.0,
                paymentMethod = PaymentMethod.CASH,
                reference = "CASH-BAGS",
                note = "Biodegradable paper & cloth bags",
                timestamp = now - oneDayMillis
            )
        )
        expenseDao.insertExpenses(expenses)

        // Seed Other Income
        incomeDao.insertIncome(
            OtherIncomeEntity(
                title = "Cardboard Box & Plastic Scrap Sale",
                category = "Scrap Sales",
                amount = 950.0,
                paymentMethod = PaymentMethod.CASH,
                note = "Sold carton boxes to local recycler",
                timestamp = now - (2 * oneDayMillis)
            )
        )

        // Seed Sample Completed Sales
        val s1 = SaleEntity(
            invoiceNumber = "INV-20261027-1001",
            customerId = null,
            customerName = "Walk-in Customer",
            subtotal = 690.0,
            discountAmount = 0.0,
            taxRatePercent = 0.0,
            taxAmount = 0.0,
            totalAmount = 690.0,
            paidAmount = 690.0,
            balanceDue = 0.0,
            paymentMethod = PaymentMethod.CASH,
            status = SaleStatus.COMPLETED,
            timestamp = now - (2 * oneDayMillis),
            cashierName = "Ayoush"
        )
        val s1Id = saleDao.insertSale(s1)
        saleDao.insertSaleItems(
            listOf(
                SaleItemEntity(saleId = s1Id, productId = 2, productName = "Sunflow Pure Sunflower Oil (1 Liter)", unit = "bottle", unitPrice = 250.0, costPrice = 210.0, quantity = 2.0, total = 500.0),
                SaleItemEntity(saleId = s1Id, productId = 15, productName = "Britannia Marie Gold Family Pack (800g)", unit = "pkt", unitPrice = 160.0, costPrice = 130.0, quantity = 1.0, total = 160.0),
                SaleItemEntity(saleId = s1Id, productId = 4, productName = "Tata Iodized Salt (1 kg packet)", unit = "pkt", unitPrice = 30.0, costPrice = 22.0, quantity = 1.0, total = 30.0)
            )
        )

        val s2 = SaleEntity(
            invoiceNumber = "INV-20261028-1002",
            customerId = 1,
            customerName = "Sita Sharma",
            customerPhone = "9841122334",
            subtotal = 3450.0,
            discountAmount = 50.0,
            taxRatePercent = 0.0,
            taxAmount = 0.0,
            totalAmount = 3400.0,
            paidAmount = 2000.0,
            balanceDue = 1400.0,
            paymentMethod = PaymentMethod.SPLIT,
            splitCashAmount = 2000.0,
            splitDigitalAmount = 0.0,
            status = SaleStatus.COMPLETED,
            timestamp = now - (4 * 3600 * 1000L),
            cashierName = "Ayoush"
        )
        val s2Id = saleDao.insertSale(s2)
        saleDao.insertSaleItems(
            listOf(
                SaleItemEntity(saleId = s2Id, productId = 1, productName = "Basmati Premium Rice (25kg Bag)", unit = "bag", unitPrice = 2550.0, costPrice = 2200.0, quantity = 1.0, total = 2550.0),
                SaleItemEntity(saleId = s2Id, productId = 3, productName = "Musuro Dal / Red Lentils (1 kg)", unit = "kg", unitPrice = 165.0, costPrice = 135.0, quantity = 3.0, total = 495.0),
                SaleItemEntity(saleId = s2Id, productId = 6, productName = "White Refined Sugar / Chini (1 kg)", unit = "kg", unitPrice = 105.0, costPrice = 85.0, quantity = 3.0, total = 315.0),
                SaleItemEntity(saleId = s2Id, productId = 14, productName = "Everest Pure Turmeric Powder / Besar (200g)", unit = "pkt", unitPrice = 90.0, costPrice = 75.0, quantity = 1.0, total = 90.0)
            )
        )

        auditLogDao.insertAuditLog(
            AuditLogEntity(
                action = "SYSTEM_INITIALIZED",
                details = "Retail Master initialized with Nepali retail store inventory, pricing, and master ledgers.",
                staffRole = "System"
            )
        )
    }
}
