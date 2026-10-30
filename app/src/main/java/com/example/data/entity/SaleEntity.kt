package com.example.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.model.PaymentMethod
import com.example.model.SaleStatus

@Entity(
    tableName = "sales",
    indices = [
        Index(value = ["invoiceNumber"], unique = true),
        Index(value = ["customerId"]),
        Index(value = ["timestamp"])
    ]
)
data class SaleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val invoiceNumber: String,
    val customerId: Long? = null,
    val customerName: String = "Walk-in Customer",
    val customerPhone: String = "",
    val subtotal: Double,
    val discountAmount: Double = 0.0,
    val taxRatePercent: Double = 0.0,
    val taxAmount: Double = 0.0,
    val totalAmount: Double,
    val paidAmount: Double,
    val balanceDue: Double = 0.0, // Remaining due credited to customer
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val splitCashAmount: Double = 0.0,
    val splitDigitalAmount: Double = 0.0,
    val status: SaleStatus = SaleStatus.COMPLETED,
    val timestamp: Long = System.currentTimeMillis(),
    val cashierName: String = "Admin",
    val notes: String = ""
)

@Entity(
    tableName = "sale_items",
    foreignKeys = [
        ForeignKey(
            entity = SaleEntity::class,
            parentColumns = ["id"],
            childColumns = ["saleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["saleId"]),
        Index(value = ["productId"])
    ]
)
data class SaleItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val saleId: Long,
    val productId: Long,
    val productName: String,
    val unit: String = "pcs",
    val unitPrice: Double, // Selling price
    val costPrice: Double, // Purchase cost at time of sale for accurate COGS
    val quantity: Double,
    val total: Double
) {
    val grossProfit: Double
        get() = (unitPrice - costPrice) * quantity
}
