package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.InventoryDao
import com.example.data.dao.ProductDao
import com.example.data.dao.ProductionDao
import com.example.data.dao.PurchasingAndFreightDao
import com.example.data.dao.SalesAndFinanceDao
import com.example.data.dao.SettingsDao
import com.example.data.entity.AuditLogEntity
import com.example.data.entity.CarrierEntity
import com.example.data.entity.ColorEntity
import com.example.data.entity.CuttingBatchEntity
import com.example.data.entity.CuttingItemEntity
import com.example.data.entity.CustomerEntity
import com.example.data.entity.ExpenseEntity
import com.example.data.entity.FabricRollEntity
import com.example.data.entity.FabricTypeEntity
import com.example.data.entity.FinishedGoodEntity
import com.example.data.entity.FreightAllocationEntity
import com.example.data.entity.FreightBillEntity
import com.example.data.entity.InventoryTransactionEntity
import com.example.data.entity.MaterialEntity
import com.example.data.entity.OrderEntity
import com.example.data.entity.OrderItemEntity
import com.example.data.entity.PaymentEntity
import com.example.data.entity.PriceHistoryEntity
import com.example.data.entity.ProductEntity
import com.example.data.entity.ProductModelEntity
import com.example.data.entity.ProductionBatchEntity
import com.example.data.entity.PurchaseEntity
import com.example.data.entity.PurchaseItemEntity
import com.example.data.entity.ReservationEntity
import com.example.data.entity.ReturnEntity
import com.example.data.entity.ReturnItemEntity
import com.example.data.entity.SaleEntity
import com.example.data.entity.SaleItemEntity
import com.example.data.entity.SettingsEntity
import com.example.data.entity.SizeEntity
import com.example.data.entity.SizeGroupEntity
import com.example.data.entity.SupplierEntity
import com.example.data.entity.WasteEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        SettingsEntity::class,
        ProductEntity::class,
        ProductModelEntity::class,
        SizeGroupEntity::class,
        SizeEntity::class,
        ColorEntity::class,
        FabricTypeEntity::class,
        FabricRollEntity::class,
        MaterialEntity::class,
        PriceHistoryEntity::class,
        SupplierEntity::class,
        CarrierEntity::class,
        FreightBillEntity::class,
        FreightAllocationEntity::class,
        PurchaseEntity::class,
        PurchaseItemEntity::class,
        CuttingBatchEntity::class,
        CuttingItemEntity::class,
        ProductionBatchEntity::class,
        FinishedGoodEntity::class,
        CustomerEntity::class,
        OrderEntity::class,
        OrderItemEntity::class,
        ReservationEntity::class,
        SaleEntity::class,
        SaleItemEntity::class,
        PaymentEntity::class,
        ExpenseEntity::class,
        ReturnEntity::class,
        ReturnItemEntity::class,
        WasteEntity::class,
        InventoryTransactionEntity::class,
        AuditLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun settingsDao(): SettingsDao
    abstract fun productDao(): ProductDao
    abstract fun inventoryDao(): InventoryDao
    abstract fun purchasingAndFreightDao(): PurchasingAndFreightDao
    abstract fun productionDao(): ProductionDao
    abstract fun salesAndFinanceDao(): SalesAndFinanceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "garment_workshop.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Seed initial essential data
                            CoroutineScope(Dispatchers.IO).launch {
                                INSTANCE?.let { database ->
                                    seedInitialData(database)
                                }
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun seedInitialData(db: AppDatabase) {
            val settingsDao = db.settingsDao()
            val productDao = db.productDao()
            val inventoryDao = db.inventoryDao()
            val pfDao = db.purchasingAndFreightDao()
            val salesDao = db.salesAndFinanceDao()

            // 1. Settings
            settingsDao.insertOrUpdate(SettingsEntity())

            // 2. Size Groups & Sizes
            val adultGroupId = productDao.insertSizeGroup(
                SizeGroupEntity(name = "بزرگسال استاندارد", description = "L, XL, 2XL")
            )
            productDao.insertSize(SizeEntity(sizeGroupId = adultGroupId, name = "L", sortOrder = 1))
            productDao.insertSize(SizeEntity(sizeGroupId = adultGroupId, name = "XL", sortOrder = 2))
            productDao.insertSize(SizeEntity(sizeGroupId = adultGroupId, name = "2XL", sortOrder = 3))

            val numericGroupId = productDao.insertSizeGroup(
                SizeGroupEntity(name = "سایزبندی عددی", description = "42, 44, 46")
            )
            productDao.insertSize(SizeEntity(sizeGroupId = numericGroupId, name = "42", sortOrder = 1))
            productDao.insertSize(SizeEntity(sizeGroupId = numericGroupId, name = "44", sortOrder = 2))
            productDao.insertSize(SizeEntity(sizeGroupId = numericGroupId, name = "46", sortOrder = 3))

            val kidsGroupId = productDao.insertSizeGroup(
                SizeGroupEntity(name = "بچگانه", description = "70, 80, 90, 95")
            )
            productDao.insertSize(SizeEntity(sizeGroupId = kidsGroupId, name = "70", sortOrder = 1))
            productDao.insertSize(SizeEntity(sizeGroupId = kidsGroupId, name = "80", sortOrder = 2))
            productDao.insertSize(SizeEntity(sizeGroupId = kidsGroupId, name = "90", sortOrder = 3))
            productDao.insertSize(SizeEntity(sizeGroupId = kidsGroupId, name = "95", sortOrder = 4))

            // 3. Colors
            productDao.insertColor(ColorEntity(name = "مشکی", hexCode = "#1A1A1A"))
            productDao.insertColor(ColorEntity(name = "طوسی ملانژ", hexCode = "#7E8287"))
            productDao.insertColor(ColorEntity(name = "سرمه‌ای", hexCode = "#1B2A4A"))
            productDao.insertColor(ColorEntity(name = "ذغالی", hexCode = "#373737"))
            productDao.insertColor(ColorEntity(name = "یشمی", hexCode = "#2D4A3E"))

            // 4. Products & Models (Slash pants, 6-pocket, striped, printed tape)
            val slashPantsId = productDao.insertProduct(
                ProductEntity(
                    name = "شلوار اسلش مردانه",
                    category = "اسلش",
                    code = "SL-MEN-01",
                    fabricMaterial = "دورس دونخ خارخورده",
                    description = "شلوار اسلش مردانه پاییزه و زمستانه با کیفیت عالی",
                    baseWholesalePrice = 320000.0
                )
            )
            productDao.insertModel(
                ProductModelEntity(
                    productId = slashPantsId,
                    modelName = "مدل 6 جیب",
                    modelCode = "M-6POCKET",
                    bomNotes = "پارچه: 0.65 کیلوگرم، کش پهن 4 سانت: 0.8 متر، زیپ جیب: 2 عدد"
                )
            )
            productDao.insertModel(
                ProductModelEntity(
                    productId = slashPantsId,
                    modelName = "مدل نواردار",
                    modelCode = "M-STRIPE",
                    bomNotes = "پارچه: 0.60 کیلوگرم، نوار جانبی: 1.8 متر، کش کمر: 0.8 متر"
                )
            )
            productDao.insertModel(
                ProductModelEntity(
                    productId = slashPantsId,
                    modelName = "مدل نوار چاپی",
                    modelCode = "M-PRINTED-STRIPE",
                    bomNotes = "پارچه: 0.60 کیلوگرم، نوار چاپی اختصاصی: 1.8 متر، کش کمر: 0.8 متر"
                )
            )

            val slashKidsId = productDao.insertProduct(
                ProductEntity(
                    name = "اسلش بچگانه",
                    category = "اسلش",
                    code = "SL-KID-01",
                    fabricMaterial = "دورس دونخ پنبه",
                    description = "اسلش راحتی بچگانه اسپرت",
                    baseWholesalePrice = 210000.0
                )
            )
            productDao.insertModel(
                ProductModelEntity(
                    productId = slashKidsId,
                    modelName = "اسلش ساده بچگانه",
                    modelCode = "M-KID-SIMPLE",
                    bomNotes = "پارچه: 0.35 کیلوگرم، کش کمر: 0.6 متر"
                )
            )

            // 5. Fabric Types & Conversions
            val ft1 = inventoryDao.insertFabricType(
                FabricTypeEntity(
                    name = "دورس دونخ پنبه",
                    kgToMeterRatio = 2.4, // 1 KG = 2.4 M
                    kgToYardRatio = 2.62,
                    description = "عرض 120 دولا، مناسب اسلش و هودی"
                )
            )
            val ft2 = inventoryDao.insertFabricType(
                FabricTypeEntity(
                    name = "گلکسی پنبه لاکرا",
                    kgToMeterRatio = 2.2,
                    kgToYardRatio = 2.4,
                    description = "مناسب اسلش 6 جیب و شلوار راحتی"
                )
            )

            // 6. Materials (خرج‌کار و ملزومات)
            inventoryDao.insertMaterial(
                MaterialEntity(
                    name = "کش پهن 4 سانت سفید",
                    category = "کش",
                    unit = "متر",
                    purchasePrice = 9500.0,
                    currentPrice = 11000.0,
                    stockQuantity = 450.0,
                    minStockQuantity = 100.0
                )
            )
            inventoryDao.insertMaterial(
                MaterialEntity(
                    name = "نخ دوخت 120 پلی‌استر مشکی",
                    category = "نخ",
                    unit = "عدد",
                    purchasePrice = 28000.0,
                    currentPrice = 32000.0,
                    stockQuantity = 60.0,
                    minStockQuantity = 20.0
                )
            )
            inventoryDao.insertMaterial(
                MaterialEntity(
                    name = "نوار اسلش چاپی اسپرت",
                    category = "نوار",
                    unit = "متر",
                    purchasePrice = 12000.0,
                    currentPrice = 14500.0,
                    stockQuantity = 280.0,
                    minStockQuantity = 50.0
                )
            )
            inventoryDao.insertMaterial(
                MaterialEntity(
                    name = "زیپ دنده‌پلاستیک 16 سانت",
                    category = "زیپ",
                    unit = "عدد",
                    purchasePrice = 6500.0,
                    currentPrice = 7500.0,
                    stockQuantity = 200.0,
                    minStockQuantity = 50.0
                )
            )

            // 7. Suppliers & Carriers
            val sup1 = pfDao.insertSupplier(
                SupplierEntity(
                    name = "نساجی بافندگی پارس",
                    shopName = "دفتر مرکزی بازار پارچه",
                    phone = "02155601234",
                    address = "تهران، مولوی، سرای آزادی",
                    city = "تهران"
                )
            )
            val carrier1 = pfDao.insertCarrier(
                CarrierEntity(
                    name = "باربری پیشتاز وطن",
                    phone = "02155800000",
                    notes = "انبار شوش، ارسال سریع طاقه"
                )
            )

            // 8. Customers
            salesDao.insertCustomer(
                CustomerEntity(
                    name = "حاج احمد رضایی",
                    storeName = "پخش عمده پوشاک رضایی",
                    phone = "09121112233",
                    address = "مشهد، بازار سپاد، فاز 2 پلاک 110",
                    city = "مشهد",
                    customerType = "عمده‌فروش",
                    creditLimit = 150000000.0
                )
            )
            salesDao.insertCustomer(
                CustomerEntity(
                    name = "فروشگاه اسپرت نوین",
                    storeName = "فروشگاه زنجیره‌ای نوین",
                    phone = "09132223344",
                    address = "اصفهان، میدان شهدا",
                    city = "اصفهان",
                    customerType = "فروشگاه",
                    creditLimit = 80000000.0
                )
            )
        }
    }
}
