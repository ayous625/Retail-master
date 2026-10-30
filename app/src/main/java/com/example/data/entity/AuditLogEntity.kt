package com.example.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.model.StockReason

@Entity(
    tableName = "stock_adjustments",
    indices = [Index(value = ["productId"]), Index(value = ["timestamp"])]
)
data class StockAdjustmentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val productId: Long,
    val productName: String,
    val previousStock: Double,
    val adjustmentQuantity: Double, // Negative for reduction (damage, theft), positive for correction/addition
    val newStock: Double,
    val reason: StockReason,
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "audit_logs",
    indices = [Index(value = ["timestamp"]), Index(value = ["action"])]
)
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val action: String, // e.g. "SALE_CREATED", "SALE_REFUNDED", "STOCK_ADJUSTED", "CUSTOMER_REPAYMENT"
    val details: String,
    val staffRole: String = "Owner",
    val timestamp: Long = System.currentTimeMillis()
)
