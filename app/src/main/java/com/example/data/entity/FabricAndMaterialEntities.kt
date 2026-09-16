package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fabric_types")
data class FabricTypeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String, // e.g. "دورس دونخ پنبه", "دورس سه نخ", "گلکسی پنبه", "تیپ لاکرا", "اسپان"
    val kgToMeterRatio: Double = 2.4, // 1 KG = 2.4 Meters (user configurable)
    val kgToYardRatio: Double = 2.62, // 1 KG = 2.62 Yards
    val description: String = ""
)

@Entity(tableName = "fabric_rolls")
data class FabricRollEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val rollNumber: String, // e.g. "R-101"
    val invoiceNumber: String = "",
    val fabricName: String, // e.g. "دورس دونخ مشکی"
    val fabricTypeId: Long = 0,
    val colorName: String = "مشکی",
    val purchaseWeightKg: Double, // e.g. 100.0 KG
    val totalMeters: Double = 0.0, // calculated or recorded
    val purchaseUnit: String = "کیلوگرم", // کیلوگرم, متر, یارد
    val consumptionUnit: String = "کیلوگرم", // کیلوگرم, متر, یارد
    val purchasePricePerUnit: Double, // e.g. 850,000 تومان/کیلو
    val currentPricePerUnit: Double, // e.g. 1,000,000 تومان/کیلو (updated price)
    val supplierId: Long = 0,
    val supplierName: String = "",
    val purchaseDate: Long = System.currentTimeMillis(),
    val allocatedFreightCost: Double = 0.0, // Total freight allocated to this roll
    val actualCostPerKg: Double = 0.0, // (purchasePrice * purchaseWeight + allocatedFreight) / purchaseWeight
    val actualCostPerMeter: Double = 0.0,
    val consumedQuantity: Double = 0.0, // KG consumed
    val remainingQuantity: Double = 0.0, // KG remaining
    val status: String = "موجود", // موجود, نیمه‌مصرف, تمام‌شده
    val notes: String = ""
)

@Entity(tableName = "materials")
data class MaterialEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String, // e.g. "کش پهن 4 سانت", "نخ پلی‌استر", "زیپ فلزی 18 سانت", "نوار اسلش چاپدار"
    val category: String, // کش, نخ, آستر, چاپ, نوار, دکمه, زیپ, لیبل, بسته‌بندی, خرج‌کار, سایر
    val unit: String = "متر", // متر, یارد, کیلوگرم, عدد, بسته, جفت
    val purchasePrice: Double, // Historical purchase unit price
    val currentPrice: Double, // Latest market unit price
    val supplierId: Long = 0,
    val supplierName: String = "",
    val stockQuantity: Double = 0.0,
    val minStockQuantity: Double = 10.0,
    val purchaseDate: Long = System.currentTimeMillis(),
    val notes: String = ""
)

@Entity(tableName = "price_history")
data class PriceHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val itemType: String, // "FABRIC_ROLL", "FABRIC_TYPE", "MATERIAL"
    val itemId: Long,
    val itemName: String,
    val date: Long = System.currentTimeMillis(),
    val previousPrice: Double,
    val newPrice: Double,
    val changePercent: Double,
    val reason: String = "",
    val recordedBy: String = "مدیریت"
)
