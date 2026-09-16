package com.example.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "cutting_batches")
data class CuttingBatchEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val batchNumber: String, // e.g. "CUT-1403-01"
    val date: Long = System.currentTimeMillis(),
    val totalFabricConsumedKg: Double = 0.0,
    val totalPiecesCut: Int = 0,
    val notes: String = "",
    val status: String = "تکمیل شده" // تکمیل شده, لغو شده
)

@Entity(
    tableName = "cutting_items",
    foreignKeys = [
        ForeignKey(
            entity = CuttingBatchEntity::class,
            parentColumns = ["id"],
            childColumns = ["batchId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["batchId"])]
)
data class CuttingItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val batchId: Long,
    val fabricRollId: Long,
    val rollNumber: String = "",
    val productId: Long,
    val productName: String,
    val productModelId: Long,
    val modelName: String,
    val colorName: String = "مشکی",
    val sizeName: String = "L",
    val quantityCut: Int, // Number of pieces cut
    val fabricConsumedKg: Double, // KG used for this specific item
    val fabricCostActual: Double = 0.0, // Historical cost of fabric used
    val fabricCostCurrent: Double = 0.0, // Current replacement cost
    val accessoriesCost: Double = 0.0,
    val printingCost: Double = 0.0,
    val date: Long = System.currentTimeMillis(),
    val status: String = "برش‌خورده" // برش‌خورده, در حال چاپ, در حال دوخت, تکمیل شده, لغو شده
)

@Entity(tableName = "production_batches")
data class ProductionBatchEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val batchNumber: String, // e.g. "PRD-101"
    val cuttingItemId: Long? = null,
    val productId: Long,
    val productName: String,
    val productModelId: Long,
    val modelName: String,
    val colorName: String = "مشکی",
    val sizeName: String = "L",
    val quantity: Int, // Number of garments in batch
    val stage: String = "دوخت", // برش, چاپ, دوخت, تکمیل, آماده
    val startDate: Long = System.currentTimeMillis(),
    val endDate: Long? = null,
    val sewingCostPerPiece: Double = 45000.0,
    val overheadCostPerPiece: Double = 15000.0,
    val unitCostActual: Double = 0.0,
    val unitCostCurrent: Double = 0.0,
    val notes: String = "",
    val status: String = "در حال تولید" // در حال تولید, آماده, لغو شده
)

@Entity(tableName = "finished_goods")
data class FinishedGoodEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productId: Long,
    val productName: String,
    val productModelId: Long,
    val modelName: String,
    val colorName: String = "مشکی",
    val sizeName: String = "L",
    val producedQuantity: Int = 0, // Total produced
    val soldQuantity: Int = 0,     // Total delivered/sold
    val reservedQuantity: Int = 0, // In pending orders
    val availableQuantity: Int = 0,// producedQuantity - soldQuantity - reservedQuantity
    val unitCostActual: Double = 0.0, // Actual historical cost to manufacture
    val unitCostCurrent: Double = 0.0, // Current replacement cost
    val wholesalePrice: Double = 0.0,  // Wholesale selling price
    val productionDate: Long = System.currentTimeMillis(),
    val lastUpdated: Long = System.currentTimeMillis()
)
