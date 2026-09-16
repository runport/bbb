package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey val id: Int = 1,
    val workshopName: String = "کارگاه تولید پوشاک صبا",
    val managerName: String = "مدیریت",
    val phone: String = "09120000000",
    val address: String = "تهران، بازار بزرگ، کوچه تولیدی‌ها",
    val currency: String = "تومان",
    val profitCalculationType: String = "PERCENTAGE", // PERCENTAGE or FIXED_AMOUNT
    val defaultProfitValue: Double = 25.0, // 25% or fixed amount
    val defaultSewingCost: Double = 45000.0, // Fixed sewing cost per piece
    val defaultOverheadCost: Double = 15000.0, // Default overhead per piece
    val preferredChartType: String = "BAR", // BAR, LINE, AREA
    val autoReserveOnOrder: Boolean = true,
    val updatedAt: Long = System.currentTimeMillis()
)
