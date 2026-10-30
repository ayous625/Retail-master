package com.example.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "customers",
    indices = [Index(value = ["phone"])]
)
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phone: String,
    val address: String = "",
    val creditLimit: Double = 10000.0, // Default credit limit in NPR
    val balanceOwed: Double = 0.0, // Positive value means customer owes shop
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
