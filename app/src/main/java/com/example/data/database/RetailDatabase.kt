package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.dao.AuditLogDao
import com.example.data.dao.CustomerDao
import com.example.data.dao.CustomerPaymentDao
import com.example.data.dao.ExpenseDao
import com.example.data.dao.IncomeDao
import com.example.data.dao.ProductDao
import com.example.data.dao.PurchaseDao
import com.example.data.dao.SaleDao
import com.example.data.dao.ShopSettingsDao
import com.example.data.dao.StockAdjustmentDao
import com.example.data.dao.SupplierDao
import com.example.data.dao.SupplierPaymentDao
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

@Database(
    entities = [
        ProductEntity::class,
        CustomerEntity::class,
        SupplierEntity::class,
        SaleEntity::class,
        SaleItemEntity::class,
        PurchaseEntity::class,
        PurchaseItemEntity::class,
        CustomerPaymentEntity::class,
        SupplierPaymentEntity::class,
        ExpenseEntity::class,
        OtherIncomeEntity::class,
        StockAdjustmentEntity::class,
        AuditLogEntity::class,
        ShopSettingsEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(DataConverters::class)
abstract class RetailDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun customerDao(): CustomerDao
    abstract fun supplierDao(): SupplierDao
    abstract fun saleDao(): SaleDao
    abstract fun purchaseDao(): PurchaseDao
    abstract fun customerPaymentDao(): CustomerPaymentDao
    abstract fun supplierPaymentDao(): SupplierPaymentDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun incomeDao(): IncomeDao
    abstract fun stockAdjustmentDao(): StockAdjustmentDao
    abstract fun auditLogDao(): AuditLogDao
    abstract fun shopSettingsDao(): ShopSettingsDao

    companion object {
        @Volatile
        private var INSTANCE: RetailDatabase? = null

        fun getDatabase(context: Context): RetailDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RetailDatabase::class.java,
                    "retail_master.db"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
