package com.example.data.repository

import androidx.room.withTransaction
import com.example.data.db.AppDatabase
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
import kotlinx.coroutines.flow.Flow
import kotlin.math.roundToInt

class WorkshopRepository(private val db: AppDatabase) {

    private val settingsDao = db.settingsDao()
    private val productDao = db.productDao()
    private val inventoryDao = db.inventoryDao()
    private val pfDao = db.purchasingAndFreightDao()
    private val productionDao = db.productionDao()
    private val salesDao = db.salesAndFinanceDao()

    // --- Settings ---
    val settings: Flow<SettingsEntity?> = settingsDao.getSettings()
    suspend fun getSettingsSync(): SettingsEntity = settingsDao.getSettingsSync() ?: SettingsEntity()
    suspend fun updateSettings(settings: SettingsEntity) = settingsDao.insertOrUpdate(settings)

    // --- Products & Models ---
    val allProducts: Flow<List<ProductEntity>> = productDao.getAllProducts()
    val allModels: Flow<List<ProductModelEntity>> = productDao.getAllModels()
    val allSizeGroups: Flow<List<SizeGroupEntity>> = productDao.getAllSizeGroups()
    val allSizes: Flow<List<SizeEntity>> = productDao.getAllSizes()
    val allColors: Flow<List<ColorEntity>> = productDao.getAllColors()

    fun getModelsForProduct(productId: Long): Flow<List<ProductModelEntity>> = productDao.getModelsForProduct(productId)
    fun getSizesForGroup(groupId: Long): Flow<List<SizeEntity>> = productDao.getSizesForGroup(groupId)

    suspend fun addProduct(product: ProductEntity): Long = productDao.insertProduct(product)
    suspend fun updateProduct(product: ProductEntity) = productDao.updateProduct(product)
    suspend fun deleteProduct(product: ProductEntity) = productDao.deleteProduct(product)

    suspend fun addModel(model: ProductModelEntity): Long = productDao.insertModel(model)
    suspend fun updateModel(model: ProductModelEntity) = productDao.updateModel(model)
    suspend fun deleteModel(model: ProductModelEntity) = productDao.deleteModel(model)

    suspend fun addSizeGroup(group: SizeGroupEntity): Long = productDao.insertSizeGroup(group)
    suspend fun addSize(size: SizeEntity): Long = productDao.insertSize(size)
    suspend fun deleteSize(size: SizeEntity) = productDao.deleteSize(size)

    suspend fun addColor(color: ColorEntity): Long = productDao.insertColor(color)
    suspend fun deleteColor(color: ColorEntity) = productDao.deleteColor(color)

    // --- Fabric & Materials ---
    val allFabricTypes: Flow<List<FabricTypeEntity>> = inventoryDao.getAllFabricTypes()
    val allFabricRolls: Flow<List<FabricRollEntity>> = inventoryDao.getAllFabricRolls()
    val availableFabricRolls: Flow<List<FabricRollEntity>> = inventoryDao.getAvailableFabricRolls()
    val allMaterials: Flow<List<MaterialEntity>> = inventoryDao.getAllMaterials()
    val allPriceHistory: Flow<List<PriceHistoryEntity>> = inventoryDao.getAllPriceHistory()
    val allTransactions: Flow<List<InventoryTransactionEntity>> = inventoryDao.getAllInventoryTransactions()

    suspend fun addFabricType(type: FabricTypeEntity): Long = inventoryDao.insertFabricType(type)
    suspend fun updateFabricType(type: FabricTypeEntity) = inventoryDao.updateFabricType(type)

    // Register / Purchase Fabric Roll
    suspend fun recordFabricRoll(roll: FabricRollEntity): Result<Long> = runCatching {
        db.withTransaction {
            val ratio = if (roll.fabricTypeId > 0) {
                inventoryDao.getFabricTypeById(roll.fabricTypeId)?.kgToMeterRatio ?: 2.4
            } else 2.4
            val calculatedMeters = if (roll.totalMeters > 0) roll.totalMeters else (roll.purchaseWeightKg * ratio)
            val costPerKg = roll.purchasePricePerUnit
            val costPerMeter = if (calculatedMeters > 0) (costPerKg * roll.purchaseWeightKg) / calculatedMeters else 0.0

            val rollToInsert = roll.copy(
                totalMeters = calculatedMeters,
                remainingQuantity = roll.purchaseWeightKg,
                consumedQuantity = 0.0,
                actualCostPerKg = costPerKg,
                actualCostPerMeter = costPerMeter,
                status = "موجود"
            )
            val rollId = inventoryDao.insertFabricRoll(rollToInsert)

            // Inventory transaction
            inventoryDao.insertInventoryTransaction(
                InventoryTransactionEntity(
                    itemType = "FABRIC_ROLL",
                    itemId = rollId,
                    itemName = roll.fabricName,
                    transactionType = "PURCHASE_IN",
                    quantityChange = roll.purchaseWeightKg,
                    remainingAfter = roll.purchaseWeightKg,
                    referenceType = "PURCHASE",
                    referenceId = rollId,
                    notes = "ورود طاقه با وزن ${roll.purchaseWeightKg} کیلوگرم"
                )
            )

            salesDao.insertAuditLog(
                AuditLogEntity(
                    actionType = "CREATE",
                    entityName = "FabricRoll",
                    entityId = rollId,
                    newValue = "طاقه ${roll.rollNumber} - ${roll.fabricName} (${roll.purchaseWeightKg} KG)",
                    reason = "ثبت خرید طاقه جدید"
                )
            )
            rollId
        }
    }

    suspend fun updateFabricRoll(roll: FabricRollEntity) = inventoryDao.updateFabricRoll(roll)

    // Materials CRUD
    suspend fun addMaterial(material: MaterialEntity): Long {
        val id = inventoryDao.insertMaterial(material)
        inventoryDao.insertInventoryTransaction(
            InventoryTransactionEntity(
                itemType = "MATERIAL",
                itemId = id,
                itemName = material.name,
                transactionType = "PURCHASE_IN",
                quantityChange = material.stockQuantity,
                remainingAfter = material.stockQuantity,
                referenceType = "MATERIAL_INIT",
                notes = "ثبت اولیه خرج‌کار/ملزومات"
            )
        )
        return id
    }

    suspend fun updateMaterial(material: MaterialEntity) = inventoryDao.updateMaterial(material)

    // Update Raw Material Market Price (Current Price) & Record History
    suspend fun updateRawMaterialPrice(
        itemType: String, // "FABRIC_ROLL", "MATERIAL", "FABRIC_TYPE"
        itemId: Long,
        newPrice: Double,
        reason: String,
        user: String = "مدیر سیستم"
    ): Result<Unit> = runCatching {
        db.withTransaction {
            var prevPrice = 0.0
            var itemName = ""

            when (itemType) {
                "FABRIC_ROLL" -> {
                    val roll = inventoryDao.getFabricRollById(itemId)
                        ?: throw IllegalArgumentException("طاقه یافت نشد")
                    prevPrice = roll.currentPricePerUnit
                    itemName = roll.fabricName
                    inventoryDao.updateFabricRoll(roll.copy(currentPricePerUnit = newPrice))
                }
                "MATERIAL" -> {
                    val material = inventoryDao.getMaterialById(itemId)
                        ?: throw IllegalArgumentException("قلم ملزومات یافت نشد")
                    prevPrice = material.currentPrice
                    itemName = material.name
                    inventoryDao.updateMaterial(material.copy(currentPrice = newPrice))
                }
            }

            val changePercent = if (prevPrice > 0) ((newPrice - prevPrice) / prevPrice) * 100.0 else 0.0

            inventoryDao.insertPriceHistory(
                PriceHistoryEntity(
                    itemType = itemType,
                    itemId = itemId,
                    itemName = itemName,
                    previousPrice = prevPrice,
                    newPrice = newPrice,
                    changePercent = changePercent,
                    reason = reason,
                    recordedBy = user
                )
            )

            salesDao.insertAuditLog(
                AuditLogEntity(
                    actionType = "PRICE_UPDATE",
                    entityName = itemType,
                    entityId = itemId,
                    previousValue = prevPrice.toString(),
                    newValue = newPrice.toString(),
                    reason = reason,
                    user = user
                )
            )
        }
    }

    // --- Freight & Allocation ---
    val allCarriers: Flow<List<CarrierEntity>> = pfDao.getAllCarriers()
    val allFreightBills: Flow<List<FreightBillEntity>> = pfDao.getAllFreightBills()
    fun getAllocationsForBill(billId: Long): Flow<List<FreightAllocationEntity>> = pfDao.getAllocationsForBill(billId)

    suspend fun addCarrier(carrier: CarrierEntity): Long = pfDao.insertCarrier(carrier)
    suspend fun updateCarrier(carrier: CarrierEntity) = pfDao.updateCarrier(carrier)
    suspend fun deleteCarrier(carrier: CarrierEntity) = pfDao.deleteCarrier(carrier)

    suspend fun createFreightBill(bill: FreightBillEntity): Long = pfDao.insertFreightBill(bill)

    // Execute Freight Allocation to Rolls / Items
    data class AllocationItemSpec(
        val itemType: String, // "FABRIC_ROLL" or "MATERIAL"
        val itemId: Long,
        val itemName: String,
        val itemWeight: Double,
        val itemQuantity: Double,
        val itemPurchaseAmount: Double,
        val manualAmount: Double = 0.0
    )

    suspend fun executeFreightAllocation(
        billId: Long,
        basis: String, // "WEIGHT", "QUANTITY", "PURCHASE_AMOUNT", "MANUAL"
        items: List<AllocationItemSpec>
    ): Result<Unit> = runCatching {
        db.withTransaction {
            val bill = pfDao.getFreightBillById(billId)
                ?: throw IllegalArgumentException("قبض باربری یافت نشد")
            val totalFreight = bill.totalFreightAmount

            if (items.isEmpty()) {
                throw IllegalArgumentException("حداقل یک قلم برای تخصیص کرایه الزامی است")
            }

            // Remove existing allocations for this bill if re-allocating
            pfDao.deleteAllocationsForBill(billId)

            val allocations = mutableListOf<FreightAllocationEntity>()

            when (basis) {
                "WEIGHT" -> {
                    val totalWeight = items.sumOf { it.itemWeight }
                    if (totalWeight <= 0) throw IllegalArgumentException("مجموع وزن اقلام صفر است")
                    var runningSum = 0.0
                    items.forEachIndexed { index, item ->
                        val allocated = if (index == items.lastIndex) {
                            totalFreight - runningSum // Ensure exact match
                        } else {
                            val part = (totalFreight * (item.itemWeight / totalWeight))
                            runningSum += part
                            part
                        }
                        allocations.add(
                            FreightAllocationEntity(
                                freightBillId = billId,
                                itemType = item.itemType,
                                itemId = item.itemId,
                                itemName = item.itemName,
                                itemWeight = item.itemWeight,
                                itemQuantity = item.itemQuantity,
                                itemPurchaseAmount = item.itemPurchaseAmount,
                                allocatedAmount = allocated,
                                allocationBasis = basis
                            )
                        )
                    }
                }
                "QUANTITY" -> {
                    val totalQty = items.sumOf { it.itemQuantity }
                    if (totalQty <= 0) throw IllegalArgumentException("مجموع تعداد اقلام صفر است")
                    var runningSum = 0.0
                    items.forEachIndexed { index, item ->
                        val allocated = if (index == items.lastIndex) {
                            totalFreight - runningSum
                        } else {
                            val part = (totalFreight * (item.itemQuantity / totalQty))
                            runningSum += part
                            part
                        }
                        allocations.add(
                            FreightAllocationEntity(
                                freightBillId = billId,
                                itemType = item.itemType,
                                itemId = item.itemId,
                                itemName = item.itemName,
                                itemWeight = item.itemWeight,
                                itemQuantity = item.itemQuantity,
                                itemPurchaseAmount = item.itemPurchaseAmount,
                                allocatedAmount = allocated,
                                allocationBasis = basis
                            )
                        )
                    }
                }
                "PURCHASE_AMOUNT" -> {
                    val totalAmt = items.sumOf { it.itemPurchaseAmount }
                    if (totalAmt <= 0) throw IllegalArgumentException("مجموع مبلغ خرید اقلام صفر است")
                    var runningSum = 0.0
                    items.forEachIndexed { index, item ->
                        val allocated = if (index == items.lastIndex) {
                            totalFreight - runningSum
                        } else {
                            val part = (totalFreight * (item.itemPurchaseAmount / totalAmt))
                            runningSum += part
                            part
                        }
                        allocations.add(
                            FreightAllocationEntity(
                                freightBillId = billId,
                                itemType = item.itemType,
                                itemId = item.itemId,
                                itemName = item.itemName,
                                itemWeight = item.itemWeight,
                                itemQuantity = item.itemQuantity,
                                itemPurchaseAmount = item.itemPurchaseAmount,
                                allocatedAmount = allocated,
                                allocationBasis = basis
                            )
                        )
                    }
                }
                else -> { // MANUAL
                    val sumManual = items.sumOf { it.manualAmount }
                    if ((sumManual - totalFreight).let { it < -1 || it > 1 }) {
                        throw IllegalArgumentException("مجموع مبالغ دستی (${sumManual}) با مبلغ قبض (${totalFreight}) برابر نیست")
                    }
                    items.forEach { item ->
                        allocations.add(
                            FreightAllocationEntity(
                                freightBillId = billId,
                                itemType = item.itemType,
                                itemId = item.itemId,
                                itemName = item.itemName,
                                itemWeight = item.itemWeight,
                                itemQuantity = item.itemQuantity,
                                itemPurchaseAmount = item.itemPurchaseAmount,
                                allocatedAmount = item.manualAmount,
                                allocationBasis = basis
                            )
                        )
                    }
                }
            }

            // Save allocations
            pfDao.insertAllocations(allocations)

            // Update Roll/Material actual unit costs!
            allocations.forEach { alloc ->
                if (alloc.itemType == "FABRIC_ROLL") {
                    val roll = inventoryDao.getFabricRollById(alloc.itemId)
                    if (roll != null) {
                        val newAllocatedFreight = roll.allocatedFreightCost + alloc.allocatedAmount
                        val weight = roll.purchaseWeightKg
                        val actualCostKg = if (weight > 0) {
                            ((roll.purchasePricePerUnit * weight) + newAllocatedFreight) / weight
                        } else roll.purchasePricePerUnit

                        val actualCostMeter = if (roll.totalMeters > 0) {
                            ((roll.purchasePricePerUnit * weight) + newAllocatedFreight) / roll.totalMeters
                        } else 0.0

                        inventoryDao.updateFabricRoll(
                            roll.copy(
                                allocatedFreightCost = newAllocatedFreight,
                                actualCostPerKg = actualCostKg,
                                actualCostPerMeter = actualCostMeter
                            )
                        )
                    }
                }
            }

            pfDao.updateFreightBill(bill.copy(isAllocated = true, allocationBasis = basis))

            salesDao.insertAuditLog(
                AuditLogEntity(
                    actionType = "ALLOCATION",
                    entityName = "FreightBill",
                    entityId = billId,
                    newValue = "تخصیص کرایه مبلغ ${totalFreight} به ${allocations.size} قلم بر اساس ${basis}",
                    reason = "تخصیص کرایه باربری"
                )
            )
        }
    }

    // --- Cutting Module ---
    val allCuttingBatches: Flow<List<CuttingBatchEntity>> = productionDao.getAllCuttingBatches()
    val allCuttingItems: Flow<List<CuttingItemEntity>> = productionDao.getAllCuttingItems()
    fun getCuttingItemsForRoll(rollId: Long): Flow<List<CuttingItemEntity>> = productionDao.getCuttingItemsForRoll(rollId)
    suspend fun getCuttingItemsForRollSync(rollId: Long): List<CuttingItemEntity> = productionDao.getCuttingItemsForRollSync(rollId)

    data class CutSpecification(
        val productId: Long,
        val productName: String,
        val productModelId: Long,
        val modelName: String,
        val colorName: String,
        val sizeName: String,
        val quantityCut: Int,
        val fabricConsumedKg: Double,
        val accessoriesCostPerPiece: Double = 0.0,
        val printingCostPerPiece: Double = 0.0
    )

    // Atomic Cutting Execution (with support for multi-model split and roll stock check)
    suspend fun executeCutting(
        rollId: Long,
        batchNumber: String,
        specs: List<CutSpecification>,
        notes: String = ""
    ): Result<Long> = runCatching {
        db.withTransaction {
            val roll = inventoryDao.getFabricRollById(rollId)
                ?: throw IllegalArgumentException("طاقه یافت نشد")

            val totalFabricNeeded = specs.sumOf { it.fabricConsumedKg }

            // Guard against negative stock (MANDATORY REQUIREMENT)
            if (totalFabricNeeded > roll.remainingQuantity + 0.0001) {
                throw IllegalArgumentException("موجودی طاقه کافی نیست! موجودی: ${roll.remainingQuantity} کیلوگرم، مصرف درخواستی: ${totalFabricNeeded} کیلوگرم")
            }

            val totalPieces = specs.sumOf { it.quantityCut }

            // 1. Create Cutting Batch
            val batchId = productionDao.insertCuttingBatch(
                CuttingBatchEntity(
                    batchNumber = batchNumber,
                    totalFabricConsumedKg = totalFabricNeeded,
                    totalPiecesCut = totalPieces,
                    notes = notes
                )
            )

            // 2. Insert Cutting Items & create linked Production Batches
            val settings = getSettingsSync()
            val cuttingItems = specs.map { spec ->
                val fabricCostActual = spec.fabricConsumedKg * roll.actualCostPerKg
                val fabricCostCurrent = spec.fabricConsumedKg * roll.currentPricePerUnit
                val totalAccCost = spec.accessoriesCostPerPiece * spec.quantityCut
                val totalPrintCost = spec.printingCostPerPiece * spec.quantityCut

                val cItem = CuttingItemEntity(
                    batchId = batchId,
                    fabricRollId = rollId,
                    rollNumber = roll.rollNumber,
                    productId = spec.productId,
                    productName = spec.productName,
                    productModelId = spec.productModelId,
                    modelName = spec.modelName,
                    colorName = spec.colorName,
                    sizeName = spec.sizeName,
                    quantityCut = spec.quantityCut,
                    fabricConsumedKg = spec.fabricConsumedKg,
                    fabricCostActual = fabricCostActual,
                    fabricCostCurrent = fabricCostCurrent,
                    accessoriesCost = totalAccCost,
                    printingCost = totalPrintCost,
                    status = "برش‌خورده"
                )
                val cItemId = productionDao.insertCuttingItem(cItem)

                // Also initialize Production Batch
                val unitCostActual = if (spec.quantityCut > 0) {
                    (fabricCostActual + totalAccCost + totalPrintCost + (settings.defaultSewingCost * spec.quantityCut) + (settings.defaultOverheadCost * spec.quantityCut)) / spec.quantityCut
                } else 0.0

                val unitCostCurrent = if (spec.quantityCut > 0) {
                    (fabricCostCurrent + totalAccCost + totalPrintCost + (settings.defaultSewingCost * spec.quantityCut) + (settings.defaultOverheadCost * spec.quantityCut)) / spec.quantityCut
                } else 0.0

                productionDao.insertProductionBatch(
                    ProductionBatchEntity(
                        batchNumber = "PRD-${spec.modelName.take(3)}-${batchNumber}",
                        cuttingItemId = cItemId,
                        productId = spec.productId,
                        productName = spec.productName,
                        productModelId = spec.productModelId,
                        modelName = spec.modelName,
                        colorName = spec.colorName,
                        sizeName = spec.sizeName,
                        quantity = spec.quantityCut,
                        stage = "دوخت",
                        sewingCostPerPiece = settings.defaultSewingCost,
                        overheadCostPerPiece = settings.defaultOverheadCost,
                        unitCostActual = unitCostActual,
                        unitCostCurrent = unitCostCurrent,
                        status = "در حال تولید"
                    )
                )

                cItemId
            }

            // 3. Atomically Deduct Roll Stock
            val newRemaining = (roll.remainingQuantity - totalFabricNeeded).coerceAtLeast(0.0)
            val newConsumed = roll.consumedQuantity + totalFabricNeeded
            val newStatus = if (newRemaining <= 0.05) "تمام‌شده" else "نیمه‌مصرف"

            inventoryDao.updateFabricRoll(
                roll.copy(
                    remainingQuantity = newRemaining,
                    consumedQuantity = newConsumed,
                    status = newStatus
                )
            )

            // 4. Record Inventory Transaction
            inventoryDao.insertInventoryTransaction(
                InventoryTransactionEntity(
                    itemType = "FABRIC_ROLL",
                    itemId = rollId,
                    itemName = "${roll.rollNumber} - ${roll.fabricName}",
                    transactionType = "CUTTING_OUT",
                    quantityChange = -totalFabricNeeded,
                    remainingAfter = newRemaining,
                    referenceType = "CUTTING_BATCH",
                    referenceId = batchId,
                    notes = "برش ${totalPieces} عدد برای ${specs.size} مدل/سایز"
                )
            )

            salesDao.insertAuditLog(
                AuditLogEntity(
                    actionType = "CREATE",
                    entityName = "CuttingBatch",
                    entityId = batchId,
                    newValue = "مصرف ${totalFabricNeeded} KG از طاقه ${roll.rollNumber} برای ${totalPieces} عدد",
                    reason = "ثبت عملیات برش"
                )
            )

            batchId
        }
    }

    // --- Production & Finished Goods ---
    val allProductionBatches: Flow<List<ProductionBatchEntity>> = productionDao.getAllProductionBatches()
    val allFinishedGoods: Flow<List<FinishedGoodEntity>> = productionDao.getAllFinishedGoods()
    val availableFinishedGoods: Flow<List<FinishedGoodEntity>> = productionDao.getAvailableFinishedGoods()

    // Complete Production Batch -> Transfer to Finished Goods Inventory
    suspend fun completeProductionBatch(
        batchId: Long,
        wholesalePrice: Double = 0.0
    ): Result<Unit> = runCatching {
        db.withTransaction {
            val batch = productionDao.getProductionBatchById(batchId)
                ?: throw IllegalArgumentException("دسته تولید یافت نشد")

            productionDao.updateProductionBatch(
                batch.copy(
                    stage = "آماده",
                    status = "آماده",
                    endDate = System.currentTimeMillis()
                )
            )

            // Find or create FinishedGood record
            val existing = productionDao.findFinishedGood(
                productId = batch.productId,
                modelId = batch.productModelId,
                colorName = batch.colorName,
                sizeName = batch.sizeName
            )

            val priceToSet = if (wholesalePrice > 0) wholesalePrice else (existing?.wholesalePrice ?: 0.0)

            val finishedGoodId = if (existing != null) {
                val newProduced = existing.producedQuantity + batch.quantity
                val newAvailable = existing.availableQuantity + batch.quantity
                productionDao.updateFinishedGood(
                    existing.copy(
                        producedQuantity = newProduced,
                        availableQuantity = newAvailable,
                        unitCostActual = batch.unitCostActual,
                        unitCostCurrent = batch.unitCostCurrent,
                        wholesalePrice = if (priceToSet > 0) priceToSet else existing.wholesalePrice,
                        lastUpdated = System.currentTimeMillis()
                    )
                )
                existing.id
            } else {
                productionDao.insertFinishedGood(
                    FinishedGoodEntity(
                        productId = batch.productId,
                        productName = batch.productName,
                        productModelId = batch.productModelId,
                        modelName = batch.modelName,
                        colorName = batch.colorName,
                        sizeName = batch.sizeName,
                        producedQuantity = batch.quantity,
                        soldQuantity = 0,
                        reservedQuantity = 0,
                        availableQuantity = batch.quantity,
                        unitCostActual = batch.unitCostActual,
                        unitCostCurrent = batch.unitCostCurrent,
                        wholesalePrice = priceToSet,
                        productionDate = System.currentTimeMillis(),
                        lastUpdated = System.currentTimeMillis()
                    )
                )
            }

            // Inventory transaction
            inventoryDao.insertInventoryTransaction(
                InventoryTransactionEntity(
                    itemType = "FINISHED_GOOD",
                    itemId = finishedGoodId,
                    itemName = "${batch.productName} (${batch.modelName} - ${batch.colorName} - ${batch.sizeName})",
                    transactionType = "PRODUCTION_IN",
                    quantityChange = batch.quantity.toDouble(),
                    remainingAfter = ((existing?.availableQuantity ?: 0) + batch.quantity).toDouble(),
                    referenceType = "PRODUCTION_BATCH",
                    referenceId = batchId,
                    notes = "ورود ${batch.quantity} عدد به انبار کالای آماده"
                )
            )

            salesDao.insertAuditLog(
                AuditLogEntity(
                    actionType = "UPDATE",
                    entityName = "ProductionBatch",
                    entityId = batchId,
                    newValue = "تکمیل تولید ${batch.quantity} عدد ${batch.productName}",
                    reason = "اتمام تولید و ورود به انبار محصول"
                )
            )
        }
    }

    // Direct entry for finished goods (e.g. initial count or warehouse stock)
    suspend fun addFinishedGood(fg: FinishedGoodEntity): Long {
        val id = productionDao.insertFinishedGood(fg)
        inventoryDao.insertInventoryTransaction(
            InventoryTransactionEntity(
                itemType = "FINISHED_GOOD",
                itemId = id,
                itemName = "${fg.productName} (${fg.modelName})",
                transactionType = "PRODUCTION_IN",
                quantityChange = fg.availableQuantity.toDouble(),
                remainingAfter = fg.availableQuantity.toDouble(),
                referenceType = "INIT_STOCK",
                notes = "ثبت موجودی اولیه کالای آماده"
            )
        )
        return id
    }

    suspend fun updateFinishedGood(fg: FinishedGoodEntity) = productionDao.updateFinishedGood(fg)

    // --- Customers ---
    val allCustomers: Flow<List<CustomerEntity>> = salesDao.getAllCustomers()
    suspend fun getCustomerById(id: Long): CustomerEntity? = salesDao.getCustomerById(id)
    suspend fun addCustomer(c: CustomerEntity): Long = salesDao.insertCustomer(c)
    suspend fun updateCustomer(c: CustomerEntity) = salesDao.updateCustomer(c)
    suspend fun deleteCustomer(c: CustomerEntity) = salesDao.deleteCustomer(c)

    // --- Suppliers ---
    val allSuppliers: Flow<List<SupplierEntity>> = pfDao.getAllSuppliers()
    suspend fun addSupplier(s: SupplierEntity): Long = pfDao.insertSupplier(s)
    suspend fun updateSupplier(s: SupplierEntity) = pfDao.updateSupplier(s)
    suspend fun deleteSupplier(s: SupplierEntity) = pfDao.deleteSupplier(s)

    // --- Orders & Reservation ---
    val allOrders: Flow<List<OrderEntity>> = salesDao.getAllOrders()
    fun getOrderItems(orderId: Long): Flow<List<OrderItemEntity>> = salesDao.getOrderItems(orderId)

    data class OrderItemSpec(
        val finishedGoodId: Long?,
        val productId: Long,
        val productName: String,
        val productModelId: Long,
        val modelName: String,
        val colorName: String,
        val sizeName: String,
        val quantity: Int,
        val unitPrice: Double
    )

    // Create Order with Stock Reservation Logic
    suspend fun createOrder(
        orderNumber: String,
        customerId: Long,
        items: List<OrderItemSpec>,
        discount: Double = 0.0,
        notes: String = ""
    ): Result<Long> = runCatching {
        db.withTransaction {
            val customer = salesDao.getCustomerById(customerId)
                ?: throw IllegalArgumentException("مشتری یافت نشد")

            val totalAmount = items.sumOf { it.quantity * it.unitPrice }
            val finalAmount = (totalAmount - discount).coerceAtLeast(0.0)

            val orderId = salesDao.insertOrder(
                OrderEntity(
                    orderNumber = orderNumber,
                    customerId = customerId,
                    customerName = customer.name + if (customer.storeName.isNotBlank()) " (${customer.storeName})" else "",
                    totalAmount = totalAmount,
                    discount = discount,
                    finalAmount = finalAmount,
                    paidAmount = 0.0,
                    balanceDue = finalAmount,
                    status = "ثبت شده",
                    notes = notes,
                    isReserved = true
                )
            )

            // Insert items & reserve stock
            var allReserved = true
            items.forEach { spec ->
                val oItemId = salesDao.insertOrderItems(
                    listOf(
                        OrderItemEntity(
                            orderId = orderId,
                            productId = spec.productId,
                            productName = spec.productName,
                            productModelId = spec.productModelId,
                            modelName = spec.modelName,
                            colorName = spec.colorName,
                            sizeName = spec.sizeName,
                            quantity = spec.quantity,
                            unitPrice = spec.unitPrice,
                            totalPrice = spec.quantity * spec.unitPrice
                        )
                    )
                )

                // Stock Reservation (Available Stock -> Reserved Stock)
                val fg = if (spec.finishedGoodId != null && spec.finishedGoodId > 0) {
                    productionDao.getFinishedGoodById(spec.finishedGoodId)
                } else {
                    productionDao.findFinishedGood(
                        spec.productId,
                        spec.productModelId,
                        spec.colorName,
                        spec.sizeName
                    )
                }

                if (fg != null && fg.availableQuantity >= spec.quantity) {
                    // Reserve
                    val newReserved = fg.reservedQuantity + spec.quantity
                    val newAvailable = fg.availableQuantity - spec.quantity
                    productionDao.updateFinishedGood(
                        fg.copy(
                            reservedQuantity = newReserved,
                            availableQuantity = newAvailable,
                            lastUpdated = System.currentTimeMillis()
                        )
                    )

                    salesDao.insertReservation(
                        ReservationEntity(
                            orderId = orderId,
                            orderItemId = oItemId.firstOrNull() ?: 0,
                            finishedGoodId = fg.id,
                            quantityReserved = spec.quantity,
                            status = "فعال"
                        )
                    )

                    inventoryDao.insertInventoryTransaction(
                        InventoryTransactionEntity(
                            itemType = "FINISHED_GOOD",
                            itemId = fg.id,
                            itemName = "${spec.productName} (${spec.modelName})",
                            transactionType = "ORDER_RESERVE",
                            quantityChange = -spec.quantity.toDouble(), // reduced from available
                            remainingAfter = newAvailable.toDouble(),
                            referenceType = "ORDER",
                            referenceId = orderId,
                            notes = "رزرو ${spec.quantity} عدد برای سفارش ${orderNumber}"
                        )
                    )
                } else {
                    allReserved = false
                }
            }

            val finalStatus = if (allReserved) "رزرو شده" else "در انتظار تولید"
            val order = salesDao.getOrderById(orderId)
            if (order != null) {
                salesDao.updateOrder(order.copy(status = finalStatus))
            }

            salesDao.insertAuditLog(
                AuditLogEntity(
                    actionType = "CREATE",
                    entityName = "Order",
                    entityId = orderId,
                    newValue = "سفارش ${orderNumber} به مبلغ ${finalAmount} (${finalStatus})",
                    reason = "ثبت سفارش مشتری"
                )
            )

            orderId
        }
    }

    // --- Sales Module ---
    val allSales: Flow<List<SaleEntity>> = salesDao.getAllSales()
    val allPayments: Flow<List<PaymentEntity>> = salesDao.getAllPayments()
    val allExpenses: Flow<List<ExpenseEntity>> = salesDao.getAllExpenses()
    val allReturns: Flow<List<ReturnEntity>> = salesDao.getAllReturns()
    val allWastes: Flow<List<WasteEntity>> = salesDao.getAllWastes()
    val allAuditLogs: Flow<List<AuditLogEntity>> = salesDao.getAllAuditLogs()

    fun getSaleItems(saleId: Long): Flow<List<SaleItemEntity>> = salesDao.getSaleItems(saleId)

    // Finalize Sale (Confirm wholesale sale, update stock, customer balance, COGS, profit, payments)
    data class SaleFinalizeParam(
        val orderId: Long? = null,
        val customerId: Long,
        val saleNumber: String,
        val items: List<SaleItemSpec>,
        val discount: Double = 0.0,
        val initialPaymentAmount: Double = 0.0,
        val initialPaymentMethod: String = "انتقال بانکی",
        val initialPaymentRef: String = "",
        val notes: String = ""
    )

    data class SaleItemSpec(
        val finishedGoodId: Long,
        val productId: Long,
        val productName: String,
        val productModelId: Long,
        val modelName: String,
        val colorName: String,
        val sizeName: String,
        val quantity: Int,
        val unitPrice: Double
    )

    suspend fun finalizeSale(param: SaleFinalizeParam): Result<Long> = runCatching {
        db.withTransaction {
            val customer = salesDao.getCustomerById(param.customerId)
                ?: throw IllegalArgumentException("مشتری یافت نشد")

            val totalAmount = param.items.sumOf { it.quantity * it.unitPrice }
            val finalAmount = (totalAmount - param.discount).coerceAtLeast(0.0)

            var totalCogsActual = 0.0
            var totalCogsCurrent = 0.0

            // 1. Process items and verify/deduct stock
            val processedItems = param.items.map { itemSpec ->
                val fg = productionDao.getFinishedGoodById(itemSpec.finishedGoodId)
                    ?: throw IllegalArgumentException("محصول ${itemSpec.productName} یافت نشد")

                // If sale is from order, it might have been reserved
                val wasReserved = param.orderId != null
                if (!wasReserved && fg.availableQuantity < itemSpec.quantity) {
                    throw IllegalArgumentException("موجودی قابل فروش کافی نیست! موجودی: ${fg.availableQuantity}، مقدار درخواستی: ${itemSpec.quantity}")
                }

                val unitCostActual = fg.unitCostActual
                val unitCostCurrent = fg.unitCostCurrent
                totalCogsActual += (unitCostActual * itemSpec.quantity)
                totalCogsCurrent += (unitCostCurrent * itemSpec.quantity)

                // Stock update:
                // soldQuantity increases, reserved decreases (if was reserved) or available decreases (if direct)
                val newSold = fg.soldQuantity + itemSpec.quantity
                val newReserved = if (wasReserved) (fg.reservedQuantity - itemSpec.quantity).coerceAtLeast(0) else fg.reservedQuantity
                val newAvailable = (fg.producedQuantity - newSold - newReserved).coerceAtLeast(0)

                productionDao.updateFinishedGood(
                    fg.copy(
                        soldQuantity = newSold,
                        reservedQuantity = newReserved,
                        availableQuantity = newAvailable,
                        lastUpdated = System.currentTimeMillis()
                    )
                )

                // Inventory Transaction (SALE_OUT)
                inventoryDao.insertInventoryTransaction(
                    InventoryTransactionEntity(
                        itemType = "FINISHED_GOOD",
                        itemId = fg.id,
                        itemName = "${fg.productName} (${fg.modelName} - ${fg.colorName} - ${fg.sizeName})",
                        transactionType = "SALE_OUT",
                        quantityChange = -itemSpec.quantity.toDouble(),
                        remainingAfter = newAvailable.toDouble(),
                        referenceType = "SALE",
                        notes = "خروج قطعی برای فروش ${param.saleNumber}"
                    )
                )

                SaleItemEntity(
                    saleId = 0, // will be updated
                    finishedGoodId = fg.id,
                    productId = itemSpec.productId,
                    productName = itemSpec.productName,
                    productModelId = itemSpec.productModelId,
                    modelName = itemSpec.modelName,
                    colorName = itemSpec.colorName,
                    sizeName = itemSpec.sizeName,
                    quantity = itemSpec.quantity,
                    unitPrice = itemSpec.unitPrice,
                    totalPrice = itemSpec.quantity * itemSpec.unitPrice,
                    unitCostActual = unitCostActual,
                    unitCostCurrent = unitCostCurrent
                )
            }

            val initialPay = param.initialPaymentAmount.coerceAtMost(finalAmount)
            val balanceDue = finalAmount - initialPay
            val grossProfit = finalAmount - totalCogsActual

            // 2. Insert Sale
            val saleId = salesDao.insertSale(
                SaleEntity(
                    saleNumber = param.saleNumber,
                    orderId = param.orderId,
                    customerId = param.customerId,
                    customerName = customer.name + if (customer.storeName.isNotBlank()) " (${customer.storeName})" else "",
                    totalAmount = totalAmount,
                    discount = param.discount,
                    finalAmount = finalAmount,
                    paidAmount = initialPay,
                    balanceDue = balanceDue,
                    totalCogsActual = totalCogsActual,
                    totalCogsCurrent = totalCogsCurrent,
                    grossProfitActual = grossProfit,
                    notes = param.notes,
                    status = "تأیید نهایی"
                )
            )

            // 3. Insert Sale Items
            salesDao.insertSaleItems(processedItems.map { it.copy(saleId = saleId) })

            // 4. Update Customer Balance
            val newCustBalance = customer.balance + balanceDue
            val newTotalPurchased = customer.totalPurchased + finalAmount
            salesDao.updateCustomer(
                customer.copy(
                    balance = newCustBalance,
                    totalPurchased = newTotalPurchased,
                    orderCount = customer.orderCount + 1
                )
            )

            // 5. Initial Payment if any
            if (initialPay > 0) {
                salesDao.insertPayment(
                    PaymentEntity(
                        referenceType = "SALE",
                        referenceId = saleId,
                        customerId = param.customerId,
                        personName = customer.name,
                        amount = initialPay,
                        paymentMethod = param.initialPaymentMethod,
                        trackingNumber = param.initialPaymentRef,
                        notes = "پرداخت همزمان با فاکتور ${param.saleNumber}",
                        status = "تأیید شده"
                    )
                )
            }

            // 6. Update Order Status if linked
            if (param.orderId != null) {
                val order = salesDao.getOrderById(param.orderId)
                if (order != null) {
                    salesDao.updateOrder(
                        order.copy(
                            status = "تکمیل شده",
                            paidAmount = order.paidAmount + initialPay,
                            balanceDue = (order.finalAmount - (order.paidAmount + initialPay)).coerceAtLeast(0.0)
                        )
                    )
                }
            }

            salesDao.insertAuditLog(
                AuditLogEntity(
                    actionType = "CREATE",
                    entityName = "Sale",
                    entityId = saleId,
                    newValue = "فروش ${param.saleNumber} مبلغ ${finalAmount} تومان (بهای تمام‌شده: ${totalCogsActual}، سود: ${grossProfit})",
                    reason = "تأیید نهایی فروش عمده"
                )
            )

            saleId
        }
    }

    // Step-by-step Partial Payment (پرداخت مرحله‌ای)
    suspend fun recordPayment(
        referenceType: String, // "ORDER", "SALE", "CUSTOMER_ACCOUNT"
        referenceId: Long?,
        customerId: Long,
        amount: Double,
        paymentMethod: String = "انتقال بانکی",
        trackingNumber: String = "",
        notes: String = ""
    ): Result<Long> = runCatching {
        db.withTransaction {
            val customer = salesDao.getCustomerById(customerId)
                ?: throw IllegalArgumentException("مشتری یافت نشد")

            if (amount <= 0) throw IllegalArgumentException("مبلغ پرداخت باید مثبت باشد")

            val paymentId = salesDao.insertPayment(
                PaymentEntity(
                    referenceType = referenceType,
                    referenceId = referenceId,
                    customerId = customerId,
                    personName = customer.name,
                    amount = amount,
                    paymentMethod = paymentMethod,
                    trackingNumber = trackingNumber,
                    notes = notes,
                    status = "تأیید شده"
                )
            )

            // Reduce customer debt
            val newBalance = customer.balance - amount
            salesDao.updateCustomer(customer.copy(balance = newBalance))

            // Update specific sale/order if provided
            if (referenceType == "SALE" && referenceId != null) {
                val sale = salesDao.getSaleById(referenceId)
                if (sale != null) {
                    val newPaid = sale.paidAmount + amount
                    val newDue = (sale.finalAmount - newPaid).coerceAtLeast(0.0)
                    salesDao.updateSale(sale.copy(paidAmount = newPaid, balanceDue = newDue))
                }
            } else if (referenceType == "ORDER" && referenceId != null) {
                val order = salesDao.getOrderById(referenceId)
                if (order != null) {
                    val newPaid = order.paidAmount + amount
                    val newDue = (order.finalAmount - newPaid).coerceAtLeast(0.0)
                    salesDao.updateOrder(order.copy(paidAmount = newPaid, balanceDue = newDue))
                }
            }

            salesDao.insertAuditLog(
                AuditLogEntity(
                    actionType = "CREATE",
                    entityName = "Payment",
                    entityId = paymentId,
                    newValue = "دریافت وجه ${amount} تومان از ${customer.name}",
                    reason = "ثبت دریافت مالی"
                )
            )

            paymentId
        }
    }

    // Sales Return (برگشت کالا با Reverse Transaction کامل)
    suspend fun processSalesReturn(
        saleId: Long,
        returnNumber: String,
        returnItems: List<Pair<Long, Int>>, // finishedGoodId to return quantity
        reason: String
    ): Result<Long> = runCatching {
        db.withTransaction {
            val sale = salesDao.getSaleById(saleId)
                ?: throw IllegalArgumentException("فاکتور فروش یافت نشد")
            val customer = salesDao.getCustomerById(sale.customerId)
                ?: throw IllegalArgumentException("مشتری یافت نشد")
            val saleItems = salesDao.getSaleItemsSync(saleId)

            var totalRefundAmount = 0.0
            val returnEntities = mutableListOf<ReturnItemEntity>()

            returnItems.forEach { (fgId, qty) ->
                val sItem = saleItems.find { it.finishedGoodId == fgId }
                    ?: throw IllegalArgumentException("کالا در این فاکتور یافت نشد")
                if (qty > sItem.quantity) {
                    throw IllegalArgumentException("تعداد برگشتی نمی‌تواند بیشتر از تعداد فاکتور باشد")
                }

                val lineRefund = qty * sItem.unitPrice
                totalRefundAmount += lineRefund

                returnEntities.add(
                    ReturnItemEntity(
                        returnId = 0,
                        finishedGoodId = fgId,
                        productName = sItem.productName,
                        modelName = sItem.modelName,
                        colorName = sItem.colorName,
                        sizeName = sItem.sizeName,
                        quantity = qty,
                        unitPrice = sItem.unitPrice,
                        totalPrice = lineRefund,
                        itemCondition = "سالم - برگشت به انبار"
                    )
                )

                // Reverse Stock back to FinishedGoods
                val fg = productionDao.getFinishedGoodById(fgId)
                if (fg != null) {
                    val newSold = (fg.soldQuantity - qty).coerceAtLeast(0)
                    val newAvailable = fg.availableQuantity + qty
                    productionDao.updateFinishedGood(
                        fg.copy(
                            soldQuantity = newSold,
                            availableQuantity = newAvailable,
                            lastUpdated = System.currentTimeMillis()
                        )
                    )

                    // Inventory Reverse Transaction
                    inventoryDao.insertInventoryTransaction(
                        InventoryTransactionEntity(
                            itemType = "FINISHED_GOOD",
                            itemId = fgId,
                            itemName = "${sItem.productName} (${sItem.modelName})",
                            transactionType = "RETURN_IN",
                            quantityChange = qty.toDouble(),
                            remainingAfter = newAvailable.toDouble(),
                            referenceType = "RETURN",
                            notes = "برگشت ${qty} عدد از فاکتور ${sale.saleNumber}"
                        )
                    )
                }
            }

            // Create Return Entity
            val returnId = salesDao.insertReturn(
                ReturnEntity(
                    returnNumber = returnNumber,
                    saleId = saleId,
                    customerId = sale.customerId,
                    customerName = customer.name,
                    totalRefundAmount = totalRefundAmount,
                    reason = reason,
                    status = "تأیید برگشت"
                )
            )

            salesDao.insertReturnItems(returnEntities.map { it.copy(returnId = returnId) })

            // Customer Balance adjustment (Reverse Credit)
            val newCustBalance = customer.balance - totalRefundAmount
            salesDao.updateCustomer(customer.copy(balance = newCustBalance))

            salesDao.insertAuditLog(
                AuditLogEntity(
                    actionType = "REVERSE",
                    entityName = "Return",
                    entityId = returnId,
                    newValue = "برگشت ${totalRefundAmount} تومان از فاکتور ${sale.saleNumber}",
                    reason = reason
                )
            )

            returnId
        }
    }

    // Expense Recording
    suspend fun recordExpense(
        category: String,
        title: String,
        amount: Double,
        paymentMethod: String = "کارت",
        notes: String = ""
    ): Long {
        val id = salesDao.insertExpense(
            ExpenseEntity(
                category = category,
                title = title,
                amount = amount,
                paymentMethod = paymentMethod,
                notes = notes
            )
        )
        salesDao.insertAuditLog(
            AuditLogEntity(
                actionType = "CREATE",
                entityName = "Expense",
                entityId = id,
                newValue = "هزینه ${title} (${category}): ${amount} تومان",
                reason = "ثبت هزینه جاری کارگاه"
            )
        )
        return id
    }

    // Waste Recording
    suspend fun recordWaste(waste: WasteEntity): Long {
        return salesDao.insertWaste(waste)
    }

    // --- Dashboard & KPI Aggregations ---
    data class DashboardKpis(
        val totalSales: Double = 0.0,
        val totalReceived: Double = 0.0,
        val customerReceivables: Double = 0.0, // بدهکاری مشتریان
        val totalCogs: Double = 0.0,
        val grossProfit: Double = 0.0,
        val totalExpenses: Double = 0.0,
        val netProfit: Double = 0.0,
        val salesCount: Int = 0,
        val ordersCount: Int = 0,
        val activeRollsCount: Int = 0,
        val totalFabricRemainingKg: Double = 0.0,
        val totalFabricConsumedKg: Double = 0.0,
        val finishedGoodsPieces: Int = 0,
        val reservedPieces: Int = 0,
        val availablePieces: Int = 0,
        val cutsCount: Int = 0,
        val newCustomersCount: Int = 0,
        val recurringCustomersCount: Int = 0
    )

    suspend fun computeDashboardKpis(timeFilter: String): DashboardKpis {
        val now = System.currentTimeMillis()
        val startTime = when (timeFilter) {
            "TODAY" -> now - (24L * 60L * 60L * 1000L) // 24 hours back as defined in prompt
            "THIS_MONTH" -> now - (30L * 24L * 60L * 60L * 1000L)
            "THIS_YEAR" -> now - (365L * 24L * 60L * 60L * 1000L)
            else -> 0L // All time
        }

        // We can compute directly from Room database safely
        val sales = db.openHelper.readableDatabase.let { _ ->
            // Let's query using standard Dao methods or Flow collections
            // Let's use direct DB queries
        }

        return db.withTransaction {
            // Compute real numbers
            val rolls = inventoryDao.getAllFabricRolls().let { flow ->
                // fetch current rolls
                db.openHelper.readableDatabase
            }

            // Let's write simple SQLite aggregates
            var sSales = 0.0
            var sCogs = 0.0
            var sSalesCount = 0

            val saleCursor = db.query(
                "SELECT SUM(finalAmount), SUM(totalCogsActual), COUNT(id) FROM sales WHERE date >= ?",
                arrayOf(startTime)
            )
            if (saleCursor.moveToFirst()) {
                sSales = saleCursor.getDouble(0)
                sCogs = saleCursor.getDouble(1)
                sSalesCount = saleCursor.getInt(2)
            }
            saleCursor.close()

            var sPayments = 0.0
            val payCursor = db.query(
                "SELECT SUM(amount) FROM payments WHERE date >= ?",
                arrayOf(startTime)
            )
            if (payCursor.moveToFirst()) {
                sPayments = payCursor.getDouble(0)
            }
            payCursor.close()

            var sExpenses = 0.0
            val expCursor = db.query(
                "SELECT SUM(amount) FROM expenses WHERE date >= ?",
                arrayOf(startTime)
            )
            if (expCursor.moveToFirst()) {
                sExpenses = expCursor.getDouble(0)
            }
            expCursor.close()

            var sReceivables = 0.0
            var sCustCount = 0
            val custCursor = db.query(
                "SELECT SUM(balance), COUNT(id) FROM customers",
                null
            )
            if (custCursor.moveToFirst()) {
                sReceivables = custCursor.getDouble(0)
                sCustCount = custCursor.getInt(1)
            }
            custCursor.close()

            var sOrdersCount = 0
            val ordCursor = db.query(
                "SELECT COUNT(id) FROM orders WHERE date >= ?",
                arrayOf(startTime)
            )
            if (ordCursor.moveToFirst()) {
                sOrdersCount = ordCursor.getInt(0)
            }
            ordCursor.close()

            var sRollsCount = 0
            var sFabricRemaining = 0.0
            var sFabricConsumed = 0.0
            val rollCursor = db.query(
                "SELECT COUNT(id), SUM(remainingQuantity), SUM(consumedQuantity) FROM fabric_rolls WHERE status != 'تمام‌شده'",
                null
            )
            if (rollCursor.moveToFirst()) {
                sRollsCount = rollCursor.getInt(0)
                sFabricRemaining = rollCursor.getDouble(1)
                sFabricConsumed = rollCursor.getDouble(2)
            }
            rollCursor.close()

            var sFinished = 0
            var sReserved = 0
            var sAvailable = 0
            val fgCursor = db.query(
                "SELECT SUM(producedQuantity), SUM(reservedQuantity), SUM(availableQuantity) FROM finished_goods",
                null
            )
            if (fgCursor.moveToFirst()) {
                sFinished = fgCursor.getInt(0)
                sReserved = fgCursor.getInt(1)
                sAvailable = fgCursor.getInt(2)
            }
            fgCursor.close()

            var sCutsCount = 0
            val cutCursor = db.query(
                "SELECT COUNT(id) FROM cutting_batches WHERE date >= ?",
                arrayOf(startTime)
            )
            if (cutCursor.moveToFirst()) {
                sCutsCount = cutCursor.getInt(0)
            }
            cutCursor.close()

            val gross = sSales - sCogs
            val net = gross - sExpenses

            DashboardKpis(
                totalSales = sSales,
                totalReceived = sPayments,
                customerReceivables = sReceivables,
                totalCogs = sCogs,
                grossProfit = gross,
                totalExpenses = sExpenses,
                netProfit = net,
                salesCount = sSalesCount,
                ordersCount = sOrdersCount,
                activeRollsCount = sRollsCount,
                totalFabricRemainingKg = sFabricRemaining,
                totalFabricConsumedKg = sFabricConsumed,
                finishedGoodsPieces = sFinished,
                reservedPieces = sReserved,
                availablePieces = sAvailable,
                cutsCount = sCutsCount,
                newCustomersCount = sCustCount,
                recurringCustomersCount = (sCustCount - 1).coerceAtLeast(0)
            )
        }
    }
}
