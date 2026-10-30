package com.example.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.model.PaymentMethod

@Entity(
    tableName = "purchases",
    indices = [
        Index(value = ["invoiceNumber"]),
        Index(value = ["supplierId"]),
        Index(value = ["timestamp"])
    ]
)
data class PurchaseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val invoiceNumber: String,
    val supplierId: Long,
    val supplierName: String,
    val totalAmount: Double,
    val paidAmount: Double,
    val balanceDue: Double = 0.0,
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val timestamp: Long = System.currentTimeMillis(),
    val isReceived: Boolean = true,
    val notes: String = ""
)

@Entity(
    tableName = "purchase_items",
    foreignKeys = [
        ForeignKey(
            entity = PurchaseEntity::class,
            parentColumns = ["id"],
            childColumns = ["purchaseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["purchaseId"]),
        Index(value = ["productId"])
    ]
)
data class PurchaseItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val purchaseId: Long,
    val productId: Long,
    val productName: String,
    val unitCost: Double,
    val quantity: Double,
    val total: Double
)
