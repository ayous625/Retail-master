package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
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
import com.example.model.SaleStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products WHERE isArchived = 0 ORDER BY name ASC")
    fun getAllActiveProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Long): ProductEntity?

    @Query("SELECT * FROM products WHERE sku = :sku LIMIT 1")
    suspend fun getProductBySku(sku: String): ProductEntity?

    @Query("SELECT * FROM products WHERE barcode = :barcode AND isArchived = 0 LIMIT 1")
    suspend fun getProductByBarcode(barcode: String): ProductEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertProduct(product: ProductEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Query("UPDATE products SET stockQuantity = :newStock WHERE id = :productId")
    suspend fun updateStock(productId: Long, newStock: Double)

    @Query("UPDATE products SET isArchived = 1 WHERE id = :productId")
    suspend fun archiveProduct(productId: Long)

    @Query("SELECT COUNT(*) FROM products WHERE isArchived = 0")
    suspend fun getProductCount(): Int
}

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getAllCustomers(): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getCustomerById(id: Long): CustomerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomers(customers: List<CustomerEntity>)

    @Update
    suspend fun updateCustomer(customer: CustomerEntity)

    @Query("UPDATE customers SET balanceOwed = :newBalance WHERE id = :id")
    suspend fun updateCustomerBalance(id: Long, newBalance: Double)
}

@Dao
interface SupplierDao {
    @Query("SELECT * FROM suppliers ORDER BY company ASC, name ASC")
    fun getAllSuppliers(): Flow<List<SupplierEntity>>

    @Query("SELECT * FROM suppliers WHERE id = :id")
    suspend fun getSupplierById(id: Long): SupplierEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplier(supplier: SupplierEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSuppliers(suppliers: List<SupplierEntity>)

    @Update
    suspend fun updateSupplier(supplier: SupplierEntity)

    @Query("UPDATE suppliers SET balanceOwed = :newBalance WHERE id = :id")
    suspend fun updateSupplierBalance(id: Long, newBalance: Double)
}

@Dao
interface SaleDao {
    @Query("SELECT * FROM sales ORDER BY timestamp DESC")
    fun getAllSales(): Flow<List<SaleEntity>>

    @Query("SELECT * FROM sales WHERE id = :id")
    suspend fun getSaleById(id: Long): SaleEntity?

    @Query("SELECT * FROM sales WHERE invoiceNumber = :invoiceNumber LIMIT 1")
    suspend fun getSaleByInvoiceNumber(invoiceNumber: String): SaleEntity?

    @Query("SELECT * FROM sale_items WHERE saleId = :saleId")
    suspend fun getSaleItems(saleId: Long): List<SaleItemEntity>

    @Query("SELECT * FROM sale_items WHERE saleId = :saleId")
    fun getSaleItemsFlow(saleId: Long): Flow<List<SaleItemEntity>>

    @Query("SELECT * FROM sale_items")
    suspend fun getAllSaleItems(): List<SaleItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: SaleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaleItems(items: List<SaleItemEntity>)

    @Query("UPDATE sales SET status = :status WHERE id = :saleId")
    suspend fun updateSaleStatus(saleId: Long, status: SaleStatus)

    @Query("SELECT * FROM sales WHERE customerId = :customerId ORDER BY timestamp DESC")
    fun getSalesByCustomer(customerId: Long): Flow<List<SaleEntity>>

    @Query("SELECT * FROM sales WHERE timestamp >= :startTime AND timestamp <= :endTime")
    suspend fun getSalesBetween(startTime: Long, endTime: Long): List<SaleEntity>
}

@Dao
interface PurchaseDao {
    @Query("SELECT * FROM purchases ORDER BY timestamp DESC")
    fun getAllPurchases(): Flow<List<PurchaseEntity>>

    @Query("SELECT * FROM purchases WHERE id = :id")
    suspend fun getPurchaseById(id: Long): PurchaseEntity?

    @Query("SELECT * FROM purchase_items WHERE purchaseId = :purchaseId")
    suspend fun getPurchaseItems(purchaseId: Long): List<PurchaseItemEntity>

    @Query("SELECT * FROM purchase_items")
    suspend fun getAllPurchaseItems(): List<PurchaseItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchase(purchase: PurchaseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchaseItems(items: List<PurchaseItemEntity>)

    @Query("SELECT * FROM purchases WHERE supplierId = :supplierId ORDER BY timestamp DESC")
    fun getPurchasesBySupplier(supplierId: Long): Flow<List<PurchaseEntity>>
}

@Dao
interface CustomerPaymentDao {
    @Query("SELECT * FROM customer_payments ORDER BY timestamp DESC")
    fun getAllCustomerPayments(): Flow<List<CustomerPaymentEntity>>

    @Query("SELECT * FROM customer_payments WHERE customerId = :customerId ORDER BY timestamp DESC")
    fun getPaymentsByCustomer(customerId: Long): Flow<List<CustomerPaymentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: CustomerPaymentEntity): Long
}

@Dao
interface SupplierPaymentDao {
    @Query("SELECT * FROM supplier_payments ORDER BY timestamp DESC")
    fun getAllSupplierPayments(): Flow<List<SupplierPaymentEntity>>

    @Query("SELECT * FROM supplier_payments WHERE supplierId = :supplierId ORDER BY timestamp DESC")
    fun getPaymentsBySupplier(supplierId: Long): Flow<List<SupplierPaymentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: SupplierPaymentEntity): Long
}

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses ORDER BY timestamp DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    suspend fun getExpensesBetween(startTime: Long, endTime: Long): List<ExpenseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenses(expenses: List<ExpenseEntity>)
}

@Dao
interface IncomeDao {
    @Query("SELECT * FROM other_income ORDER BY timestamp DESC")
    fun getAllIncome(): Flow<List<OtherIncomeEntity>>

    @Query("SELECT * FROM other_income WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    suspend fun getIncomeBetween(startTime: Long, endTime: Long): List<OtherIncomeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncome(income: OtherIncomeEntity): Long
}

@Dao
interface StockAdjustmentDao {
    @Query("SELECT * FROM stock_adjustments ORDER BY timestamp DESC")
    fun getAllAdjustments(): Flow<List<StockAdjustmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdjustment(adjustment: StockAdjustmentEntity): Long
}

@Dao
interface AuditLogDao {
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT 200")
    fun getRecentAuditLogs(): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLogEntity): Long
}

@Dao
interface ShopSettingsDao {
    @Query("SELECT * FROM shop_settings WHERE id = 1 LIMIT 1")
    fun getSettingsFlow(): Flow<ShopSettingsEntity?>

    @Query("SELECT * FROM shop_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettings(): ShopSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(settings: ShopSettingsEntity)
}
