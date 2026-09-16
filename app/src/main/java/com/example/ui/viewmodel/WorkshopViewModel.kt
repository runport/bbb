package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.entity.CarrierEntity
import com.example.data.entity.ColorEntity
import com.example.data.entity.CustomerEntity
import com.example.data.entity.ExpenseEntity
import com.example.data.entity.FabricRollEntity
import com.example.data.entity.FabricTypeEntity
import com.example.data.entity.FinishedGoodEntity
import com.example.data.entity.FreightBillEntity
import com.example.data.entity.MaterialEntity
import com.example.data.entity.OrderEntity
import com.example.data.entity.PaymentEntity
import com.example.data.entity.PriceHistoryEntity
import com.example.data.entity.ProductEntity
import com.example.data.entity.ProductModelEntity
import com.example.data.entity.ProductionBatchEntity
import com.example.data.entity.ReturnEntity
import com.example.data.entity.SaleEntity
import com.example.data.entity.SettingsEntity
import com.example.data.entity.SizeEntity
import com.example.data.entity.SizeGroupEntity
import com.example.data.entity.SupplierEntity
import com.example.data.entity.WasteEntity
import com.example.data.repository.WorkshopRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WorkshopViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repository = WorkshopRepository(db)

    // Snackbar notifications
    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    // Time filter for Dashboard & Reports ("TODAY", "THIS_MONTH", "ALL")
    private val _timeFilter = MutableStateFlow("THIS_MONTH")
    val timeFilter: StateFlow<String> = _timeFilter.asStateFlow()

    // Dashboard KPIs
    private val _dashboardKpis = MutableStateFlow(WorkshopRepository.DashboardKpis())
    val dashboardKpis: StateFlow<WorkshopRepository.DashboardKpis> = _dashboardKpis.asStateFlow()

    // Base Entities Streams
    val settings: StateFlow<SettingsEntity?> = repository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsEntity())

    val products: StateFlow<List<ProductEntity>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val models: StateFlow<List<ProductModelEntity>> = repository.allModels
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sizeGroups: StateFlow<List<SizeGroupEntity>> = repository.allSizeGroups
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sizes: StateFlow<List<SizeEntity>> = repository.allSizes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val colors: StateFlow<List<ColorEntity>> = repository.allColors
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val fabricTypes: StateFlow<List<FabricTypeEntity>> = repository.allFabricTypes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val fabricRolls: StateFlow<List<FabricRollEntity>> = repository.allFabricRolls
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val availableRolls: StateFlow<List<FabricRollEntity>> = repository.availableFabricRolls
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val materials: StateFlow<List<MaterialEntity>> = repository.allMaterials
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val finishedGoods: StateFlow<List<FinishedGoodEntity>> = repository.allFinishedGoods
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val availableFinishedGoods: StateFlow<List<FinishedGoodEntity>> = repository.availableFinishedGoods
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customers: StateFlow<List<CustomerEntity>> = repository.allCustomers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val suppliers: StateFlow<List<SupplierEntity>> = repository.allSuppliers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val carriers: StateFlow<List<CarrierEntity>> = repository.allCarriers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val freightBills: StateFlow<List<FreightBillEntity>> = repository.allFreightBills
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val orders: StateFlow<List<OrderEntity>> = repository.allOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sales: StateFlow<List<SaleEntity>> = repository.allSales
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cuttingBatches = repository.allCuttingBatches
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cuttingItems = repository.allCuttingItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val productionBatches: StateFlow<List<ProductionBatchEntity>> = repository.allProductionBatches
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val payments: StateFlow<List<PaymentEntity>> = repository.allPayments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenses: StateFlow<List<ExpenseEntity>> = repository.allExpenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val returns: StateFlow<List<ReturnEntity>> = repository.allReturns
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allWastes: StateFlow<List<WasteEntity>> = repository.allWastes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val priceHistory: StateFlow<List<PriceHistoryEntity>> = repository.allPriceHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val auditLogs = repository.allAuditLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val inventoryTransactions = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        refreshKpis()
    }

    fun setTimeFilter(filter: String) {
        _timeFilter.value = filter
        refreshKpis()
    }

    fun refreshKpis() {
        viewModelScope.launch {
            try {
                val kpis = repository.computeDashboardKpis(_timeFilter.value)
                _dashboardKpis.value = kpis
            } catch (e: Exception) {
                // handle quietly or fallback
            }
        }
    }

    // --- Actions ---

    fun addFabricRoll(roll: FabricRollEntity, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val result = repository.recordFabricRoll(roll)
            result.onSuccess {
                _userMessage.emit("طاقه پارچه ${roll.rollNumber} با موفقیت ثبت شد")
                refreshKpis()
                onComplete()
            }.onFailure {
                _userMessage.emit("خطا در ثبت طاقه: ${it.localizedMessage}")
            }
        }
    }

    fun addMaterial(material: MaterialEntity, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                repository.addMaterial(material)
                _userMessage.emit("قلم ملزومات '${material.name}' ثبت شد")
                onComplete()
            } catch (e: Exception) {
                _userMessage.emit("خطا در ثبت ملزومات: ${e.localizedMessage}")
            }
        }
    }

    fun updateRawMaterialPrice(
        itemType: String,
        itemId: Long,
        newPrice: Double,
        reason: String,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val res = repository.updateRawMaterialPrice(itemType, itemId, newPrice, reason)
            res.onSuccess {
                _userMessage.emit("قیمت روز بروزرسانی و در تاریخچه ثبت شد")
                onComplete()
            }.onFailure {
                _userMessage.emit("خطا: ${it.localizedMessage}")
            }
        }
    }

    fun createFreightBill(bill: FreightBillEntity, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                repository.createFreightBill(bill)
                _userMessage.emit("قبض باربری با موفقیت ثبت شد")
                onComplete()
            } catch (e: Exception) {
                _userMessage.emit("خطا در ثبت قبض باربری: ${e.localizedMessage}")
            }
        }
    }

    fun allocateFreight(
        billId: Long,
        basis: String,
        items: List<WorkshopRepository.AllocationItemSpec>,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val res = repository.executeFreightAllocation(billId, basis, items)
            res.onSuccess {
                _userMessage.emit("کرایه با موفقیت بین اقلام تخصیص یافت و بهای تمام‌شده طاقه‌ها بروزرسانی شد")
                refreshKpis()
                onComplete()
            }.onFailure {
                _userMessage.emit("خطا در تخصیص کرایه: ${it.localizedMessage}")
            }
        }
    }

    fun executeCutting(
        rollId: Long,
        batchNumber: String,
        specs: List<WorkshopRepository.CutSpecification>,
        notes: String = "",
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val res = repository.executeCutting(rollId, batchNumber, specs, notes)
            res.onSuccess {
                _userMessage.emit("عملیات برش با موفقیت ثبت شد و از موجودی طاقه کسر گردید")
                refreshKpis()
                onComplete()
            }.onFailure {
                _userMessage.emit("خطا در ثبت برش: ${it.localizedMessage}")
            }
        }
    }

    fun completeProductionBatch(
        batchId: Long,
        wholesalePrice: Double = 0.0,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val res = repository.completeProductionBatch(batchId, wholesalePrice)
            res.onSuccess {
                _userMessage.emit("دسته تولید تکمیل و به انبار کالای آماده منتقل شد")
                refreshKpis()
                onComplete()
            }.onFailure {
                _userMessage.emit("خطا: ${it.localizedMessage}")
            }
        }
    }

    fun createOrder(
        orderNumber: String,
        customerId: Long,
        items: List<WorkshopRepository.OrderItemSpec>,
        discount: Double = 0.0,
        notes: String = "",
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val res = repository.createOrder(orderNumber, customerId, items, discount, notes)
            res.onSuccess {
                _userMessage.emit("سفارش ${orderNumber} ثبت و موجودی مربوطه رزرو شد")
                refreshKpis()
                onComplete()
            }.onFailure {
                _userMessage.emit("خطا در ثبت سفارش: ${it.localizedMessage}")
            }
        }
    }

    fun finalizeSale(param: WorkshopRepository.SaleFinalizeParam, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val res = repository.finalizeSale(param)
            res.onSuccess {
                _userMessage.emit("فاکتور فروش ${param.saleNumber} ثبت قطعی و از انبار کسر شد")
                refreshKpis()
                onComplete()
            }.onFailure {
                _userMessage.emit("خطا در ثبت فاکتور: ${it.localizedMessage}")
            }
        }
    }

    fun recordPayment(
        referenceType: String,
        referenceId: Long?,
        customerId: Long,
        amount: Double,
        paymentMethod: String = "انتقال بانکی",
        trackingNumber: String = "",
        notes: String = "",
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val res = repository.recordPayment(referenceType, referenceId, customerId, amount, paymentMethod, trackingNumber, notes)
            res.onSuccess {
                _userMessage.emit("دریافت وجه با موفقیت ثبت شد و مانده بدهی مشتری کسر گردید")
                refreshKpis()
                onComplete()
            }.onFailure {
                _userMessage.emit("خطا: ${it.localizedMessage}")
            }
        }
    }

    fun processReturn(
        saleId: Long,
        returnNumber: String,
        returnItems: List<Pair<Long, Int>>,
        reason: String,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val res = repository.processSalesReturn(saleId, returnNumber, returnItems, reason)
            res.onSuccess {
                _userMessage.emit("برگشت کالا ثبت شد و اقلام به موجودی انبار بازگردانده شدند")
                refreshKpis()
                onComplete()
            }.onFailure {
                _userMessage.emit("خطا: ${it.localizedMessage}")
            }
        }
    }

    fun recordExpense(
        category: String,
        title: String,
        amount: Double,
        paymentMethod: String = "کارت",
        notes: String = "",
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                repository.recordExpense(category, title, amount, paymentMethod, notes)
                _userMessage.emit("هزینه '${title}' به مبلغ ${amount} ثبت شد")
                refreshKpis()
                onComplete()
            } catch (e: Exception) {
                _userMessage.emit("خطا در ثبت هزینه: ${e.localizedMessage}")
            }
        }
    }

    fun recordWaste(waste: WasteEntity, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                repository.recordWaste(waste)
                _userMessage.emit("ضایعات ثبت گردید")
                onComplete()
            } catch (e: Exception) {
                _userMessage.emit("خطا: ${e.localizedMessage}")
            }
        }
    }

    fun addCustomer(customer: CustomerEntity, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                repository.addCustomer(customer)
                _userMessage.emit("مشتری '${customer.name}' اضافه شد")
                onComplete()
            } catch (e: Exception) {
                _userMessage.emit("خطا: ${e.localizedMessage}")
            }
        }
    }

    fun addSupplier(supplier: SupplierEntity, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                repository.addSupplier(supplier)
                _userMessage.emit("تأمین‌کننده '${supplier.name}' اضافه شد")
                onComplete()
            } catch (e: Exception) {
                _userMessage.emit("خطا: ${e.localizedMessage}")
            }
        }
    }

    fun addCarrier(carrier: CarrierEntity, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                repository.addCarrier(carrier)
                _userMessage.emit("باربری '${carrier.name}' اضافه شد")
                onComplete()
            } catch (e: Exception) {
                _userMessage.emit("خطا: ${e.localizedMessage}")
            }
        }
    }

    fun addProduct(product: ProductEntity, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                repository.addProduct(product)
                _userMessage.emit("محصول '${product.name}' اضافه شد")
                onComplete()
            } catch (e: Exception) {
                _userMessage.emit("خطا: ${e.localizedMessage}")
            }
        }
    }

    fun addProductModel(model: ProductModelEntity, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                repository.addModel(model)
                _userMessage.emit("مدل '${model.modelName}' اضافه شد")
                onComplete()
            } catch (e: Exception) {
                _userMessage.emit("خطا: ${e.localizedMessage}")
            }
        }
    }

    fun addFinishedGood(fg: FinishedGoodEntity, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                repository.addFinishedGood(fg)
                _userMessage.emit("کالای آماده '${fg.productName}' با موجودی ${fg.availableQuantity} ثبت شد")
                refreshKpis()
                onComplete()
            } catch (e: Exception) {
                _userMessage.emit("خطا: ${e.localizedMessage}")
            }
        }
    }

    fun updateSettings(settings: SettingsEntity, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                repository.updateSettings(settings)
                _userMessage.emit("تنظیمات کارگاه ذخیره شد")
                onComplete()
            } catch (e: Exception) {
                _userMessage.emit("خطا: ${e.localizedMessage}")
            }
        }
    }
}
