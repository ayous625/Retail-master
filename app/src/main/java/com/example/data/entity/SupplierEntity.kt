package com.example.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "suppliers",
    indices = [Index(value = ["company"]), Index(value = ["phone"])]
)
data class SupplierEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val company: String,
    val phone: String,
    val address: String = "",
    val balanceOwed: Double = 0.0, // Positive value means shop owes supplier
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
