package com.example.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.model.PaymentMethod

@Entity(
    tableName = "customer_payments",
    indices = [Index(value = ["customerId"]), Index(value = ["timestamp"])]
)
data class CustomerPaymentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val customerId: Long,
    val customerName: String,
    val amount: Double,
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "supplier_payments",
    indices = [Index(value = ["supplierId"]), Index(value = ["timestamp"])]
)
data class SupplierPaymentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val supplierId: Long,
    val supplierName: String,
    val amount: Double,
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
