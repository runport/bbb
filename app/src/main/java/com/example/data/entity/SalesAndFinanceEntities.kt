package com.example.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val storeName: String = "", // e.g. "فروشگاه پوشاک آریا"
    val phone: String = "",
    val address: String = "",
    val city: String = "تهران",
    val customerType: String = "عمده‌فروش", // عمده‌فروش, فروشگاه, حضوری, غیرحضوری, آنلاین
    val creditLimit: Double = 0.0,
    val balance: Double = 0.0, // positive = customer owes us (بدهکاری), negative = overpaid
    val totalPurchased: Double = 0.0,
    val orderCount: Int = 0,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "orders",
    foreignKeys = [
        ForeignKey(
            entity = CustomerEntity::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index(value = ["customerId"])]
)
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderNumber: String, // e.g. "ORD-1001"
    val customerId: Long,
    val customerName: String = "",
    val date: Long = System.currentTimeMillis(),
    val totalAmount: Double = 0.0,
    val discount: Double = 0.0,
    val finalAmount: Double = 0.0,
    val paidAmount: Double = 0.0,
    val balanceDue: Double = 0.0,
    val status: String = "ثبت شده", // پیش‌نویس, ثبت شده, در انتظار تولید, آماده, رزرو شده, بخشی تحویل شده, تکمیل شده, لغو شده
    val notes: String = "",
    val isReserved: Boolean = false
)

@Entity(
    tableName = "order_items",
    foreignKeys = [
        ForeignKey(
            entity = OrderEntity::class,
            parentColumns = ["id"],
            childColumns = ["orderId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["orderId"])]
)
data class OrderItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderId: Long,
    val productId: Long,
    val productName: String,
    val productModelId: Long,
    val modelName: String,
    val colorName: String = "مشکی",
    val sizeName: String = "L",
    val quantity: Int,
    val unitPrice: Double,
    val totalPrice: Double
)

@Entity(tableName = "reservations")
data class ReservationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderId: Long,
    val orderItemId: Long,
    val finishedGoodId: Long,
    val quantityReserved: Int,
    val status: String = "فعال", // فعال, آزاد شده, تبدیل به فروش
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "sales",
    foreignKeys = [
        ForeignKey(
            entity = CustomerEntity::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index(value = ["customerId"])]
)
data class SaleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val saleNumber: String, // e.g. "INV-2001"
    val orderId: Long? = null,
    val customerId: Long,
    val customerName: String = "",
    val date: Long = System.currentTimeMillis(),
    val totalAmount: Double = 0.0,
    val discount: Double = 0.0,
    val finalAmount: Double = 0.0,
    val paidAmount: Double = 0.0,
    val balanceDue: Double = 0.0,
    val totalCogsActual: Double = 0.0,   // Cost of goods sold (actual)
    val totalCogsCurrent: Double = 0.0,  // Cost of goods sold (replacement)
    val grossProfitActual: Double = 0.0, // finalAmount - totalCogsActual
    val notes: String = "",
    val status: String = "تأیید نهایی" // تأیید نهایی, برگشت داده شده, لغو شده
)

@Entity(
    tableName = "sale_items",
    foreignKeys = [
        ForeignKey(
            entity = SaleEntity::class,
            parentColumns = ["id"],
            childColumns = ["saleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["saleId"])]
)
data class SaleItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val saleId: Long,
    val finishedGoodId: Long,
    val productId: Long,
    val productName: String,
    val productModelId: Long,
    val modelName: String,
    val colorName: String = "مشکی",
    val sizeName: String = "L",
    val quantity: Int,
    val unitPrice: Double,
    val totalPrice: Double,
    val unitCostActual: Double = 0.0,
    val unitCostCurrent: Double = 0.0
)

@Entity(tableName = "payments")
data class PaymentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val referenceType: String, // "ORDER", "SALE", "SUPPLIER_PURCHASE", "CUSTOMER_ACCOUNT"
    val referenceId: Long? = null,
    val customerId: Long? = null,
    val supplierId: Long? = null,
    val personName: String = "",
    val date: Long = System.currentTimeMillis(),
    val amount: Double,
    val paymentMethod: String = "انتقال بانکی", // کارت به کارت, چک, نقد, واریز به حساب, حواله
    val trackingNumber: String = "",
    val notes: String = "",
    val status: String = "تأیید شده" // تأیید شده, ابطال شده
)

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String, // اجاره کارگاه, برق, آب, گاز, دستمزد دوخت, تعمیرات چرخ, ملزومات مصرفی, بسته‌بندی, ناهار و پذیرایی, سایر
    val title: String,
    val amount: Double,
    val date: Long = System.currentTimeMillis(),
    val paymentMethod: String = "کارت",
    val referenceNumber: String = "",
    val notes: String = "",
    val status: String = "تأیید شده" // تأیید شده, لغو شده
)

@Entity(tableName = "returns")
data class ReturnEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val returnNumber: String, // e.g. "RET-3001"
    val saleId: Long,
    val customerId: Long,
    val customerName: String = "",
    val date: Long = System.currentTimeMillis(),
    val totalRefundAmount: Double = 0.0,
    val reason: String = "",
    val status: String = "تأیید برگشت", // تأیید برگشت, لغو شده
    val notes: String = ""
)

@Entity(
    tableName = "return_items",
    foreignKeys = [
        ForeignKey(
            entity = ReturnEntity::class,
            parentColumns = ["id"],
            childColumns = ["returnId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["returnId"])]
)
data class ReturnItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val returnId: Long,
    val finishedGoodId: Long,
    val productName: String = "",
    val modelName: String = "",
    val colorName: String = "",
    val sizeName: String = "",
    val quantity: Int,
    val unitPrice: Double,
    val totalPrice: Double,
    val itemCondition: String = "سالم - برگشت به انبار" // سالم - برگشت به انبار, ضایعات
)

@Entity(tableName = "wastes")
data class WasteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val wasteType: String, // ضایعات پارچه, ضایعات چاپ, خرابی دوخت, خرابی ملزومات
    val quantity: Double,
    val unit: String = "کیلوگرم",
    val reason: String = "",
    val estimatedCost: Double = 0.0,
    val date: Long = System.currentTimeMillis(),
    val relatedBatchId: Long? = null,
    val notes: String = ""
)

@Entity(tableName = "inventory_transactions")
data class InventoryTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val itemType: String, // "FABRIC_ROLL", "MATERIAL", "FINISHED_GOOD"
    val itemId: Long,
    val itemName: String = "",
    val transactionType: String, // "PURCHASE_IN", "CUTTING_OUT", "PRODUCTION_IN", "ORDER_RESERVE", "ORDER_UNRESERVE", "SALE_OUT", "RETURN_IN", "WASTE_OUT", "ADJUSTMENT"
    val quantityChange: Double,  // negative for out, positive for in
    val remainingAfter: Double,
    val referenceType: String = "", // "PURCHASE", "CUTTING", "ORDER", "SALE", "RETURN"
    val referenceId: Long? = null,
    val date: Long = System.currentTimeMillis(),
    val notes: String = ""
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val actionType: String, // CREATE, UPDATE, CANCEL, REVERSE, PRICE_UPDATE, ALLOCATION
    val entityName: String,
    val entityId: Long,
    val previousValue: String = "",
    val newValue: String = "",
    val reason: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val user: String = "مدیر سیستم"
)
