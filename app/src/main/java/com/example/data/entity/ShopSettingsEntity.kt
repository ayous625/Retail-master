package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shop_settings")
data class ShopSettingsEntity(
    @PrimaryKey
    val id: Long = 1,
    val shopName: String = "Pasal Kendra Retail & Mart",
    val ownerName: String = "Ayoush Karn",
    val phone: String = "+977-9800000000",
    val address: String = "New Road, Kathmandu, Nepal",
    val panVatNumber: String = "601234567",
    val currencyCode: String = "NPR",
    val currencySymbol: String = "Rs.",
    val taxRatePercent: Double = 13.0,
    val isTaxEnabledByDefault: Boolean = false,
    val invoicePrefix: String = "INV-",
    val receiptFooter: String = "Dhanyabad! Thank you for shopping with us! Please visit again.",
    val timezone: String = "Asia/Kathmandu",
    val lowStockThresholdDefault: Double = 5.0
)
