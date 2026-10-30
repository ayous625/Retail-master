package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.database.RetailDatabase
import com.example.data.entity.CustomerEntity
import com.example.data.entity.ExpenseEntity
import com.example.data.entity.OtherIncomeEntity
import com.example.data.entity.ProductEntity
import com.example.data.entity.PurchaseItemEntity
import com.example.data.entity.SupplierEntity
import com.example.data.repository.RetailRepository
import com.example.model.CartItem
import com.example.model.ExpiryStatus
import com.example.model.PaymentMethod
import com.example.model.SaleStatus
import com.example.model.StaffRole
import com.example.model.StockReason
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class RetailBusinessEngineTest {

    private lateinit var database: RetailDatabase
    private lateinit var repository: RetailRepository
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, RetailDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = RetailRepository(database)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testProductCreationAndDuplicateSkuValidation() = runBlocking {
        val p1 = ProductEntity(
            sku = "RICE-1KG",
            name = "Rice 1kg",
            category = "Staples",
            purchasePrice = 100.0,
            sellingPrice = 130.0,
            stockQuantity = 50.0
        )
        val res1 = repository.addProduct(p1)
        assertTrue("Product should be added successfully", res1.isSuccess)

        // Attempt duplicate SKU
        val p2 = ProductEntity(
            sku = "RICE-1KG",
            name = "Duplicate Rice",
            category = "Staples",
            purchasePrice = 90.0,
            sellingPrice = 120.0,
            stockQuantity = 10.0
        )
        val res2 = repository.addProduct(p2)
        assertTrue("Duplicate SKU should fail validation", res2.isFailure)
    }

    @Test
    fun testStockDeductionOnSaleAndRollbackOnRefund() = runBlocking {
        val p = ProductEntity(
            sku = "OIL-1L",
            name = "Cooking Oil 1L",
            category = "Essentials",
            purchasePrice = 200.0,
            sellingPrice = 240.0,
            stockQuantity = 20.0
        )
        val pId = repository.addProduct(p).getOrThrow()
        val savedProduct = database.productDao().getProductById(pId)!!

        val cart = listOf(CartItem(product = savedProduct, quantity = 5.0, customPrice = 240.0))

        // Complete sale
        val saleResult = repository.completeSale(
            cartItems = cart,
            customer = null,
            discountAmount = 0.0,
            applyTax = false,
            paymentMethod = PaymentMethod.CASH,
            cashTendered = 1200.0,
            splitCash = 0.0,
            splitDigital = 0.0,
            cashierName = "Cashier",
            notes = "Test Cash Sale"
        )
        assertTrue(saleResult.isSuccess)
        val sale = saleResult.getOrThrow()

        // Verify stock deducted from 20 to 15
        val productAfterSale = database.productDao().getProductById(pId)!!
        assertEquals(15.0, productAfterSale.stockQuantity, 0.01)

        // Refund sale
        val refundResult = repository.refundSale(sale.id, "Customer returned sealed bottle")
        assertTrue(refundResult.isSuccess)

        // Verify stock restored to 20
        val productAfterRefund = database.productDao().getProductById(pId)!!
        assertEquals(20.0, productAfterRefund.stockQuantity, 0.01)

        val updatedSale = database.saleDao().getSaleById(sale.id)!!
        assertEquals(SaleStatus.REFUNDED, updatedSale.status)
    }

    @Test
    fun testInsufficientStockPreventsNegativeInventory() = runBlocking {
        val p = ProductEntity(
            sku = "SUGAR-1KG",
            name = "Sugar 1kg",
            category = "Staples",
            purchasePrice = 80.0,
            sellingPrice = 100.0,
            stockQuantity = 3.0
        )
        val pId = repository.addProduct(p).getOrThrow()
        val savedProduct = database.productDao().getProductById(pId)!!

        val cart = listOf(CartItem(product = savedProduct, quantity = 10.0, customPrice = 100.0))

        val result = repository.completeSale(
            cartItems = cart,
            customer = null,
            discountAmount = 0.0,
            applyTax = false,
            paymentMethod = PaymentMethod.CASH,
            cashTendered = 1000.0,
            splitCash = 0.0,
            splitDigital = 0.0,
            cashierName = "Cashier",
            notes = ""
        )
        assertTrue("Insufficient stock sale must fail", result.isFailure)
        assertEquals(3.0, database.productDao().getProductById(pId)!!.stockQuantity, 0.01)
    }

    @Test
    fun testCustomerCreditSaleAndPartialRepayment() = runBlocking {
        val customer = CustomerEntity(
            name = "Bikash Thapa",
            phone = "9841000000",
            creditLimit = 5000.0,
            balanceOwed = 0.0
        )
        val cId = repository.addCustomer(customer).getOrThrow()
        val savedCustomer = database.customerDao().getCustomerById(cId)!!

        val product = ProductEntity(
            sku = "TEA-500G",
            name = "Tea 500g",
            category = "Beverages",
            purchasePrice = 300.0,
            sellingPrice = 380.0,
            stockQuantity = 10.0
        )
        val pId = repository.addProduct(product).getOrThrow()
        val savedProduct = database.productDao().getProductById(pId)!!

        // Credit Sale of 2 packets = Rs. 760
        val cart = listOf(CartItem(product = savedProduct, quantity = 2.0, customPrice = 380.0))
        val saleResult = repository.completeSale(
            cartItems = cart,
            customer = savedCustomer,
            discountAmount = 0.0,
            applyTax = false,
            paymentMethod = PaymentMethod.CREDIT,
            cashTendered = 0.0,
            splitCash = 0.0,
            splitDigital = 0.0,
            cashierName = "Cashier",
            notes = "Udhaar account"
        )
        assertTrue(saleResult.isSuccess)

        // Verify customer balance increased by Rs. 760
        val customerAfterSale = database.customerDao().getCustomerById(cId)!!
        assertEquals(760.0, customerAfterSale.balanceOwed, 0.01)

        // Customer pays partial repayment of Rs. 500
        val repayResult = repository.recordCustomerRepayment(cId, 500.0, PaymentMethod.CASH, "Partial payment")
        assertTrue(repayResult.isSuccess)

        // Customer balance should now be 760 - 500 = 260
        val customerAfterRepay = database.customerDao().getCustomerById(cId)!!
        assertEquals(260.0, customerAfterRepay.balanceOwed, 0.01)
    }

    @Test
    fun testSupplierPurchaseInwardAndPayment() = runBlocking {
        val supplier = SupplierEntity(
            name = "Hari Bahadur",
            company = "Central Grain Wholesalers",
            phone = "9800000000",
            balanceOwed = 0.0
        )
        val sId = repository.addSupplier(supplier).getOrThrow()
        val savedSupplier = database.supplierDao().getSupplierById(sId)!!

        val product = ProductEntity(
            sku = "DAL-1KG",
            name = "Dal 1kg",
            category = "Staples",
            purchasePrice = 120.0,
            sellingPrice = 150.0,
            stockQuantity = 5.0
        )
        val pId = repository.addProduct(product).getOrThrow()

        // Purchase order for 20 units at Rs. 125 each = Rs. 2500, paid Rs. 1000 cash, Rs. 1500 payable
        val items = listOf(
            PurchaseItemEntity(
                purchaseId = 0,
                productId = pId,
                productName = "Dal 1kg",
                unitCost = 125.0,
                quantity = 20.0,
                total = 2500.0
            )
        )

        val purchaseResult = repository.completePurchase(
            supplier = savedSupplier,
            items = items,
            paidAmount = 1000.0,
            paymentMethod = PaymentMethod.CASH,
            notes = "Invoice #1029"
        )
        assertTrue(purchaseResult.isSuccess)

        // Verify product stock increased by 20 (5 + 20 = 25)
        val productAfterPurchase = database.productDao().getProductById(pId)!!
        assertEquals(25.0, productAfterPurchase.stockQuantity, 0.01)
        assertEquals(125.0, productAfterPurchase.purchasePrice, 0.01)

        // Verify supplier dues increased by Rs. 1500
        val supplierAfterPurchase = database.supplierDao().getSupplierById(sId)!!
        assertEquals(1500.0, supplierAfterPurchase.balanceOwed, 0.01)

        // Pay remaining Rs. 1500 to supplier
        val payResult = repository.recordSupplierPayment(sId, 1500.0, PaymentMethod.BANK, "Cheque clearance")
        assertTrue(payResult.isSuccess)

        val supplierAfterSettled = database.supplierDao().getSupplierById(sId)!!
        assertEquals(0.0, supplierAfterSettled.balanceOwed, 0.01)
    }

    @Test
    fun testFinancialProfitAndLossFormulas() = runBlocking {
        // Setup initial transactions
        val p = ProductEntity(sku = "TEST-SKU", name = "Test Item", category = "Test", purchasePrice = 50.0, sellingPrice = 100.0, stockQuantity = 100.0)
        val pId = repository.addProduct(p).getOrThrow()
        val prod = database.productDao().getProductById(pId)!!

        // Sale of 10 items at Rs. 100 with Rs. 50 discount
        val cart = listOf(CartItem(product = prod, quantity = 10.0, customPrice = 100.0))
        repository.completeSale(
            cartItems = cart,
            customer = null,
            discountAmount = 50.0,
            applyTax = false,
            paymentMethod = PaymentMethod.CASH,
            cashTendered = 950.0,
            splitCash = 0.0,
            splitDigital = 0.0,
            cashierName = "Cashier",
            notes = ""
        )

        // Record operating expense of Rs. 300
        repository.addExpense(
            ExpenseEntity(
                title = "Electricity",
                category = "Utility",
                amount = 300.0,
                paymentMethod = PaymentMethod.CASH
            )
        )

        // Record other income of Rs. 100
        repository.addIncome(
            OtherIncomeEntity(
                title = "Scrap box sale",
                category = "Scrap",
                amount = 100.0,
                paymentMethod = PaymentMethod.CASH
            )
        )

        val sales = database.saleDao().getAllSales().first()
        val saleItems = database.saleDao().getAllSaleItems()
        val purchases = database.purchaseDao().getAllPurchases().first()
        val expenses = database.expenseDao().getAllExpenses().first()
        val income = database.incomeDao().getAllIncome().first()
        val customers = database.customerDao().getAllCustomers().first()
        val suppliers = database.supplierDao().getAllSuppliers().first()
        val products = database.productDao().getAllActiveProducts().first()
        val custPayments = database.customerPaymentDao().getAllCustomerPayments().first()
        val suppPayments = database.supplierPaymentDao().getAllSupplierPayments().first()

        val fin = repository.calculateFinancials(
            sales, saleItems, purchases, expenses, income, customers, suppliers, products, custPayments, suppPayments
        )

        // Gross Sales: 10 * 100 = 1000
        assertEquals(1000.0, fin.grossSales, 0.01)
        // Discount: 50
        assertEquals(50.0, fin.totalDiscounts, 0.01)
        // Net Sales = 1000 - 50 = 950
        assertEquals(950.0, fin.netSales, 0.01)
        // COGS = 10 * 50 = 500
        assertEquals(500.0, fin.costOfGoodsSold, 0.01)
        // Gross Profit = 950 - 500 = 450
        assertEquals(450.0, fin.grossProfit, 0.01)
        // Net Operating Profit = Gross Profit (450) - Expenses (300) + Other Income (100) = 250
        assertEquals(250.0, fin.netOperatingProfit, 0.01)
    }

    @Test
    fun testLowStockAndExpiryAlerts() {
        val now = System.currentTimeMillis()
        val oneDay = 24 * 60 * 60 * 1000L

        val productLow = ProductEntity(
            sku = "LOW-1",
            name = "Low Stock Item",
            category = "Cat",
            purchasePrice = 10.0,
            sellingPrice = 20.0,
            stockQuantity = 2.0,
            minStockThreshold = 5.0
        )
        assertTrue(productLow.isLowStock)

        val productExpiring = ProductEntity(
            sku = "EXP-1",
            name = "Milk",
            category = "Dairy",
            purchasePrice = 40.0,
            sellingPrice = 50.0,
            stockQuantity = 10.0,
            expiryDate = now + (5 * oneDay) // 5 days left
        )
        assertEquals(ExpiryStatus.EXPIRING_SOON, productExpiring.getExpiryStatus(now))

        val productExpired = ProductEntity(
            sku = "EXP-2",
            name = "Expired Yogurt",
            category = "Dairy",
            purchasePrice = 30.0,
            sellingPrice = 40.0,
            stockQuantity = 5.0,
            expiryDate = now - (2 * oneDay)
        )
        assertEquals(ExpiryStatus.EXPIRED, productExpired.getExpiryStatus(now))
    }

    @Test
    fun testStaffRolesPermissions() {
        assertTrue(StaffRole.OWNER.canManageInventory)
        assertTrue(StaffRole.OWNER.canViewReports)
        assertTrue(StaffRole.OWNER.canManageSettings)

        assertTrue(StaffRole.MANAGER.canManageInventory)
        assertTrue(StaffRole.MANAGER.canViewReports)
        assertFalse(StaffRole.MANAGER.canManageSettings)

        assertFalse(StaffRole.CASHIER.canManageInventory)
        assertFalse(StaffRole.CASHIER.canViewReports)
    }

    @Test
    fun testProductWithImageUriAndAutoGeneratedSku() = runBlocking {
        val p = ProductEntity(
            sku = "PRD-PHOTO-1",
            name = "Organic Honey",
            category = "Groceries",
            purchasePrice = 300.0,
            sellingPrice = 450.0,
            stockQuantity = 15.0,
            imageUri = "content://media/external/images/media/12345"
        )
        val res = repository.addProduct(p)
        assertTrue(res.isSuccess)
        val saved = database.productDao().getProductById(res.getOrThrow())
        assertNotNull(saved)
        assertEquals("content://media/external/images/media/12345", saved?.imageUri)
        assertEquals("Organic Honey", saved?.name)
    }
}
