package com.example.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String, // e.g. "اسلش مردانه", "اسلش بچگانه", "تیشرت"
    val category: String = "اسلش", // اسلش, تیشرت, پیراهن, سویشرت, بلوز, ست مردانه
    val code: String = "",
    val fabricMaterial: String = "دورس دونخ",
    val description: String = "",
    val baseWholesalePrice: Double = 0.0,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "product_models",
    foreignKeys = [
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["productId"])]
)
data class ProductModelEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productId: Long,
    val modelName: String, // e.g. "مدل 6 جیب", "مدل نواردار", "مدل نوار چاپی", "ساده"
    val modelCode: String = "",
    val bomNotes: String = "", // Fabric 0.65m, elastic 0.8m, thread, label, etc.
    val sewingCostOverride: Double? = null,
    val isActive: Boolean = true
)

@Entity(tableName = "size_groups")
data class SizeGroupEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String, // e.g. "بزرگسال عادی", "سایز بزرگ", "کودک"
    val description: String = ""
)

@Entity(
    tableName = "sizes",
    foreignKeys = [
        ForeignKey(
            entity = SizeGroupEntity::class,
            parentColumns = ["id"],
            childColumns = ["sizeGroupId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["sizeGroupId"])]
)
data class SizeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sizeGroupId: Long,
    val name: String, // e.g. "L", "XL", "2XL" or "42", "44", "46" or "70", "80"
    val sortOrder: Int = 0
)

@Entity(tableName = "colors")
data class ColorEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String, // e.g. "مشکی", "طوسی ملانژ", "سرمه‌ای", "یشمی", "ذغالی"
    val hexCode: String = "#333333"
)
