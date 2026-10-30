package com.example.data.database

import androidx.room.TypeConverter
import com.example.model.PaymentMethod
import com.example.model.SaleStatus
import com.example.model.StockReason

class DataConverters {
    @TypeConverter
    fun fromPaymentMethod(value: PaymentMethod): String = value.name

    @TypeConverter
    fun toPaymentMethod(value: String): PaymentMethod =
        runCatching { PaymentMethod.valueOf(value) }.getOrDefault(PaymentMethod.CASH)

    @TypeConverter
    fun fromSaleStatus(value: SaleStatus): String = value.name

    @TypeConverter
    fun toSaleStatus(value: String): SaleStatus =
        runCatching { SaleStatus.valueOf(value) }.getOrDefault(SaleStatus.COMPLETED)

    @TypeConverter
    fun fromStockReason(value: StockReason): String = value.name

    @TypeConverter
    fun toStockReason(value: String): StockReason =
        runCatching { StockReason.valueOf(value) }.getOrDefault(StockReason.RESTOCK)
}
