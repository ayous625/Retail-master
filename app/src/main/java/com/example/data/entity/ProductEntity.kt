package com.example.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.model.ExpiryStatus

@Entity(
    tableName = "products",
    indices = [
        Index(value = ["sku"], unique = true),
        Index(value = ["barcode"]),
        Index(value = ["category"])
    ]
)
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sku: String,
    val barcode: String = "",
    val name: String,
    val category: String,
    val unit: String = "pcs", // pcs, kg, packet, liter, etc.
    val purchasePrice: Double, // Cost price in NPR
    val sellingPrice: Double, // Retail selling price in NPR
    val stockQuantity: Double = 0.0,
    val minStockThreshold: Double = 5.0,
    val expiryDate: Long? = null, // epoch millis, nullable
    val imageUri: String? = null, // local photo URI or asset path
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    val isLowStock: Boolean
        get() = stockQuantity <= minStockThreshold

    fun getExpiryStatus(currentTime: Long = System.currentTimeMillis()): ExpiryStatus {
        val exp = expiryDate ?: return ExpiryStatus.NO_EXPIRY
        val daysUntilExpiry = (exp - currentTime) / (1000 * 60 * 60 * 24)
        return when {
            daysUntilExpiry < 0 -> ExpiryStatus.EXPIRED
            daysUntilExpiry <= 30 -> ExpiryStatus.EXPIRING_SOON
            else -> ExpiryStatus.GOOD
        }
    }

    val profitMarginPercent: Double
        get() = if (purchasePrice > 0) ((sellingPrice - purchasePrice) / purchasePrice) * 100.0 else 0.0
}
