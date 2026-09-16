package com.example.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "suppliers")
data class SupplierEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val shopName: String = "",
    val phone: String = "",
    val address: String = "",
    val city: String = "تهران",
    val balance: Double = 0.0, // positive = we owe them, negative = advance
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "carriers")
data class CarrierEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String, // e.g. "باربری پیشتاز", "باربری وطن", "باربری عصر صادق"
    val phone: String = "",
    val notes: String = "",
    val isActive: Boolean = true
)

@Entity(
    tableName = "freight_bills",
    foreignKeys = [
        ForeignKey(
            entity = CarrierEntity::class,
            parentColumns = ["id"],
            childColumns = ["carrierId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index(value = ["carrierId"])]
)
data class FreightBillEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val receiptNumber: String, // قبض بار / شماره رسید
    val date: Long = System.currentTimeMillis(),
    val carrierId: Long,
    val carrierName: String = "",
    val freightType: String = "طاقه پارچه و ملزومات",
    val totalFreightAmount: Double, // e.g. 1,000,000 تومان
    val notes: String = "",
    val allocationBasis: String = "WEIGHT", // WEIGHT, QUANTITY, PURCHASE_AMOUNT, MANUAL
    val isAllocated: Boolean = false
)

@Entity(
    tableName = "freight_allocations",
    foreignKeys = [
        ForeignKey(
            entity = FreightBillEntity::class,
            parentColumns = ["id"],
            childColumns = ["freightBillId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["freightBillId"])]
)
data class FreightAllocationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val freightBillId: Long,
    val itemType: String, // "FABRIC_ROLL" or "MATERIAL"
    val itemId: Long,
    val itemName: String,
    val itemWeight: Double = 0.0,
    val itemQuantity: Double = 0.0,
    val itemPurchaseAmount: Double = 0.0,
    val allocatedAmount: Double = 0.0, // Freight portion allocated to this item
    val allocationBasis: String = "WEIGHT"
)

@Entity(
    tableName = "purchases",
    foreignKeys = [
        ForeignKey(
            entity = SupplierEntity::class,
            parentColumns = ["id"],
            childColumns = ["supplierId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index(value = ["supplierId"])]
)
data class PurchaseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceNumber: String,
    val date: Long = System.currentTimeMillis(),
    val supplierId: Long,
    val supplierName: String = "",
    val notes: String = "",
    val subtotal: Double = 0.0,
    val discount: Double = 0.0,
    val tax: Double = 0.0,
    val freightCost: Double = 0.0,
    val totalAmount: Double = 0.0,
    val status: String = "تکمیل شده" // تکمیل شده, لغو شده
)

@Entity(
    tableName = "purchase_items",
    foreignKeys = [
        ForeignKey(
            entity = PurchaseEntity::class,
            parentColumns = ["id"],
            childColumns = ["purchaseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["purchaseId"])]
)
data class PurchaseItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val purchaseId: Long,
    val itemType: String, // "FABRIC_ROLL" or "MATERIAL"
    val itemId: Long,
    val itemName: String,
    val quantity: Double,
    val unit: String,
    val unitPrice: Double,
    val totalPrice: Double,
    val allocatedFreight: Double = 0.0
)
