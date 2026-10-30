package com.example.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.model.PaymentMethod

@Entity(
    tableName = "expenses",
    indices = [Index(value = ["category"]), Index(value = ["timestamp"])]
)
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val category: String, // Rent, Salary, Electricity, Transport, Packaging, Maintenance, Utility, Other
    val amount: Double,
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val reference: String = "",
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "other_income",
    indices = [Index(value = ["category"]), Index(value = ["timestamp"])]
)
data class OtherIncomeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val category: String, // Scrap Sales, Delivery Charges, Commissions, Incentives, Other
    val amount: Double,
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val reference: String = "",
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
