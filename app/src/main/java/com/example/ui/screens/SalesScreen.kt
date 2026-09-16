package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AssignmentReturn
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.entity.CustomerEntity
import com.example.data.entity.FinishedGoodEntity
import com.example.data.entity.OrderEntity
import com.example.data.entity.ReturnEntity
import com.example.data.entity.SaleEntity
import com.example.data.repository.WorkshopRepository
import com.example.ui.common.PersianUtils
import com.example.ui.components.EmptyStateView
import com.example.ui.components.StatusBadge
import com.example.ui.theme.AmberTertiary
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TealSecondary
import com.example.ui.viewmodel.WorkshopViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesScreen(
    viewModel: WorkshopViewModel,
    initialTab: Int = 0
) {
    var selectedTab by remember { mutableIntStateOf(initialTab) }
    val tabTitles = listOf("فاکتورهای فروش", "سفارش‌ها (رزرو)", "مشتریان", "مرجوعی‌ها")

    val sales by viewModel.sales.collectAsState()
    val orders by viewModel.orders.collectAsState()
    val customers by viewModel.customers.collectAsState()
    val returns by viewModel.returns.collectAsState()
    val availableGoods by viewModel.availableFinishedGoods.collectAsState()

    var showNewSaleDialog by remember { mutableStateOf(false) }
    var showNewOrderDialog by remember { mutableStateOf(false) }
    var showNewCustomerDialog by remember { mutableStateOf(false) }
    var showPaymentDialog by remember { mutableStateOf(false) }
    var showReturnDialog by remember { mutableStateOf(false) }

    var selectedCustomerForPayment by remember { mutableStateOf<CustomerEntity?>(null) }
    var selectedSaleForReturn by remember { mutableStateOf<SaleEntity?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    when (selectedTab) {
                        0 -> showNewSaleDialog = true
                        1 -> showNewOrderDialog = true
                        2 -> showNewCustomerDialog = true
                        3 -> showReturnDialog = true
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("sales_fab_add")
            ) {
                Icon(Icons.Default.Add, contentDescription = "ایجاد")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .testTag("sales_screen")
        ) {
            // Tabs
            PrimaryTabRow(selectedTabIndex = selectedTab) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold) }
                    )
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("جستجو بر اساس نام، شماره فاکتور یا مشتری...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = null)
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Tab Content
            when (selectedTab) {
                0 -> {
                    val filtered = sales.filter {
                        it.saleNumber.contains(searchQuery, ignoreCase = true) ||
                                it.customerName.contains(searchQuery, ignoreCase = true)
                    }
                    if (filtered.isEmpty()) {
                        EmptyStateView(
                            message = "هیچ فاکتور فروشی ثبت نشده است",
                            subMessage = "با زدن دکمه + یک فاکتور فروش عمده جدید ثبت کنید"
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(filtered, key = { it.id }) { sale ->
                                SaleItemCard(
                                    sale = sale,
                                    onRecordPayment = {
                                        val cust = customers.find { it.id == sale.customerId }
                                        selectedCustomerForPayment = cust
                                        showPaymentDialog = true
                                    },
                                    onReturn = {
                                        selectedSaleForReturn = sale
                                        showReturnDialog = true
                                    }
                                )
                            }
                            item { Spacer(modifier = Modifier.height(72.dp)) }
                        }
                    }
                }
                1 -> {
                    val filtered = orders.filter {
                        it.orderNumber.contains(searchQuery, ignoreCase = true) ||
                                it.customerName.contains(searchQuery, ignoreCase = true)
                    }
                    if (filtered.isEmpty()) {
                        EmptyStateView(
                            message = "سفارشی ثبت نشده است",
                            subMessage = "پیش‌فاکتورها و سفارشات رزرو در اینجا نمایش داده می‌شوند"
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(filtered, key = { it.id }) { order ->
                                OrderItemCard(order = order)
                            }
                            item { Spacer(modifier = Modifier.height(72.dp)) }
                        }
                    }
                }
                2 -> {
                    val filtered = customers.filter {
                        it.name.contains(searchQuery, ignoreCase = true) ||
                                it.storeName.contains(searchQuery, ignoreCase = true) ||
                                it.city.contains(searchQuery, ignoreCase = true)
                    }
                    if (filtered.isEmpty()) {
                        EmptyStateView(
                            message = "مشتری یافت نشد",
                            subMessage = "برای افزودن مشتری عمده جدید از دکمه + استفاده کنید"
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(filtered, key = { it.id }) { customer ->
                                CustomerItemCard(
                                    customer = customer,
                                    onReceivePayment = {
                                        selectedCustomerForPayment = customer
                                        showPaymentDialog = true
                                    }
                                )
                            }
                            item { Spacer(modifier = Modifier.height(72.dp)) }
                        }
                    }
                }
                3 -> {
                    val filtered = returns.filter {
                        it.returnNumber.contains(searchQuery, ignoreCase = true) ||
                                it.customerName.contains(searchQuery, ignoreCase = true)
                    }
                    if (filtered.isEmpty()) {
                        EmptyStateView(
                            message = "مرجوعی ثبت نشده است",
                            subMessage = "برگشت کالا و بازگشت به انبار در اینجا مدیریت می‌شود"
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(filtered, key = { it.id }) { ret ->
                                ReturnItemCard(ret = ret)
                            }
                            item { Spacer(modifier = Modifier.height(72.dp)) }
                        }
                    }
                }
            }
        }
    }

    // --- DIALOGS ---

    // 1. New Sale Dialog
    if (showNewSaleDialog) {
        NewSaleDialog(
            customers = customers,
            availableGoods = availableGoods,
            onDismiss = { showNewSaleDialog = false },
            onConfirm = { param ->
                viewModel.finalizeSale(param) {
                    showNewSaleDialog = false
                }
            }
        )
    }

    // 2. New Order Dialog
    if (showNewOrderDialog) {
        NewOrderDialog(
            customers = customers,
            availableGoods = availableGoods,
            onDismiss = { showNewOrderDialog = false },
            onConfirm = { orderNumber, customerId, items, discount, notes ->
                viewModel.createOrder(orderNumber, customerId, items, discount, notes) {
                    showNewOrderDialog = false
                }
            }
        )
    }

    // 3. New Customer Dialog
    if (showNewCustomerDialog) {
        NewCustomerDialog(
            onDismiss = { showNewCustomerDialog = false },
            onConfirm = { cust ->
                viewModel.addCustomer(cust) {
                    showNewCustomerDialog = false
                }
            }
        )
    }

    // 4. Payment Dialog
    if (showPaymentDialog && selectedCustomerForPayment != null) {
        val cust = selectedCustomerForPayment!!
        RecordPaymentDialog(
            customer = cust,
            onDismiss = {
                showPaymentDialog = false
                selectedCustomerForPayment = null
            },
            onConfirm = { amount, method, ref, notes ->
                viewModel.recordPayment(
                    referenceType = "CUSTOMER_ACCOUNT",
                    referenceId = null,
                    customerId = cust.id,
                    amount = amount,
                    paymentMethod = method,
                    trackingNumber = ref,
                    notes = notes
                ) {
                    showPaymentDialog = false
                    selectedCustomerForPayment = null
                }
            }
        )
    }

    // 5. Sales Return Dialog
    if (showReturnDialog) {
        SalesReturnDialog(
            sales = sales,
            preSelectedSale = selectedSaleForReturn,
            onDismiss = {
                showReturnDialog = false
                selectedSaleForReturn = null
            },
            onConfirm = { saleId, returnNumber, returnItems, reason ->
                viewModel.processReturn(saleId, returnNumber, returnItems, reason) {
                    showReturnDialog = false
                    selectedSaleForReturn = null
                }
            }
        )
    }
}

@Composable
fun SaleItemCard(
    sale: SaleEntity,
    onRecordPayment: () -> Unit,
    onReturn: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "فاکتور ${sale.saleNumber}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${sale.customerName} • ${PersianUtils.formatDate(sale.date)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                StatusBadge(status = sale.status)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "مبلغ نهایی فاکتور:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = PersianUtils.formatPrice(sale.finalAmount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "سود ناخالص واقعی:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = PersianUtils.formatPrice(sale.grossProfitActual),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = SuccessGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "دریافتی: ${PersianUtils.formatPrice(sale.paidAmount)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = SuccessGreen
                )
                if (sale.balanceDue > 0) {
                    Text(
                        text = "مانده بدهی: ${PersianUtils.formatPrice(sale.balanceDue)}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = AmberTertiary
                    )
                } else {
                    Text(
                        text = "تسویه کامل",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = SuccessGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (sale.balanceDue > 0) {
                    OutlinedButton(
                        onClick = onRecordPayment,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("دریافت وجه", style = MaterialTheme.typography.labelSmall)
                    }
                }
                OutlinedButton(
                    onClick = onReturn,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.AssignmentReturn, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("مرجوعی", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
fun OrderItemCard(order: OrderEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "سفارش ${order.orderNumber}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TealSecondary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${order.customerName} • ${PersianUtils.formatDate(order.date)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                StatusBadge(status = order.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "مبلغ: ${PersianUtils.formatPrice(order.finalAmount)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                if (order.isReserved) {
                    Text(
                        text = "موجودی انبار رزرو شد",
                        style = MaterialTheme.typography.bodySmall,
                        color = TealSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun CustomerItemCard(
    customer: CustomerEntity,
    onReceivePayment: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = customer.name + if (customer.storeName.isNotBlank()) " (${customer.storeName})" else "",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${customer.city} • تلفن: ${customer.phone}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                StatusBadge(status = customer.customerType)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("خرید کل:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(PersianUtils.formatPrice(customer.totalPurchased), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("مانده بدهی:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    val isDebtor = customer.balance > 0
                    Text(
                        text = if (isDebtor) PersianUtils.formatPrice(customer.balance) else "تسویه",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isDebtor) ErrorRed else SuccessGreen
                    )
                }
            }

            if (customer.balance > 0) {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onReceivePayment,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                ) {
                    Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("ثبت دریافت وجه از این مشتری")
                }
            }
        }
    }
}

@Composable
fun ReturnItemCard(ret: ReturnEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "مرجوعی ${ret.returnNumber}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ErrorRed
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${ret.customerName} • ${PersianUtils.formatDate(ret.date)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                StatusBadge(status = ret.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "مبلغ برگشتی: ${PersianUtils.formatPrice(ret.totalRefundAmount)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )

            if (ret.reason.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "علت: ${ret.reason}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// Dialog: New Wholesale Sale
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewSaleDialog(
    customers: List<CustomerEntity>,
    availableGoods: List<FinishedGoodEntity>,
    onDismiss: () -> Unit,
    onConfirm: (WorkshopRepository.SaleFinalizeParam) -> Unit
) {
    var selectedCustomer by remember { mutableStateOf(customers.firstOrNull()) }
    var saleNumber by remember { mutableStateOf("INV-${(1000..9999).random()}") }
    var discountText by remember { mutableStateOf("0") }
    var initialPaymentText by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("انتقال بانکی") }
    var paymentRef by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    // Selected items to sell
    var selectedGood by remember { mutableStateOf(availableGoods.firstOrNull()) }
    var quantityText by remember { mutableStateOf("10") }
    var unitPriceText by remember { mutableStateOf(selectedGood?.wholesalePrice?.toLong()?.toString() ?: "320000") }

    data class TempItem(
        val good: FinishedGoodEntity,
        val qty: Int,
        val price: Double
    )

    var itemsList by remember { mutableStateOf(listOf<TempItem>()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("ثبت فاکتور فروش عمده", fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    // Customer dropdown
                    var custExpanded by remember { mutableStateOf(false) }
                    Box {
                        OutlinedTextField(
                            value = selectedCustomer?.let { it.name + if (it.storeName.isNotBlank()) " (${it.storeName})" else "" } ?: "انتخاب مشتری",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("مشتری خریدار") },
                            trailingIcon = {
                                IconButton(onClick = { custExpanded = true }) {
                                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { custExpanded = true }
                        )
                        DropdownMenu(
                            expanded = custExpanded,
                            onDismissRequest = { custExpanded = false }
                        ) {
                            customers.forEach { cust ->
                                DropdownMenuItem(
                                    text = { Text("${cust.name} - ${cust.storeName}") },
                                    onClick = {
                                        selectedCustomer = cust
                                        custExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = saleNumber,
                        onValueChange = { saleNumber = it },
                        label = { Text("شماره فاکتور") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Add Items section
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("انتخاب کالای آماده از انبار:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                            Spacer(modifier = Modifier.height(6.dp))

                            var goodExpanded by remember { mutableStateOf(false) }
                            Box {
                                OutlinedTextField(
                                    value = selectedGood?.let { "${it.productName} (${it.modelName} - ${it.colorName} - ${it.sizeName}) [موجودی: ${it.availableQuantity}]" } ?: "انتخاب کالا",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("کالای آماده") },
                                    trailingIcon = {
                                        IconButton(onClick = { goodExpanded = true }) {
                                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().clickable { goodExpanded = true }
                                )
                                DropdownMenu(
                                    expanded = goodExpanded,
                                    onDismissRequest = { goodExpanded = false }
                                ) {
                                    availableGoods.forEach { good ->
                                        DropdownMenuItem(
                                            text = { Text("${good.productName} (${good.modelName} - ${good.colorName} - ${good.sizeName}) • موجود: ${good.availableQuantity}") },
                                            onClick = {
                                                selectedGood = good
                                                unitPriceText = good.wholesalePrice.toLong().toString()
                                                goodExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = quantityText,
                                    onValueChange = { quantityText = it },
                                    label = { Text("تعداد") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = unitPriceText,
                                    onValueChange = { unitPriceText = it },
                                    label = { Text("قیمت واحد (تومان)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1.5f)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = {
                                    val g = selectedGood
                                    val q = quantityText.toIntOrNull() ?: 0
                                    val p = unitPriceText.toDoubleOrNull() ?: 0.0
                                    if (g != null && q > 0 && p > 0) {
                                        if (q > g.availableQuantity) {
                                            errorMessage = "تعداد انتخابی ($q) بیشتر از موجودی انبار (${g.availableQuantity}) است!"
                                        } else {
                                            itemsList = itemsList + TempItem(g, q, p)
                                            errorMessage = null
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("افزودن به ردیف‌های فاکتور")
                            }
                        }
                    }
                }

                // Selected items list
                items(itemsList) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "${item.good.productName} (${item.good.modelName})",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    "${item.qty} عدد × ${PersianUtils.formatPrice(item.price)} = ${PersianUtils.formatPrice(item.qty * item.price)}",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                            IconButton(onClick = { itemsList = itemsList - item }) {
                                Icon(Icons.Default.Delete, contentDescription = "حذف", tint = ErrorRed)
                            }
                        }
                    }
                }

                item {
                    val subtotal = itemsList.sumOf { it.qty * it.price }
                    val disc = discountText.toDoubleOrNull() ?: 0.0
                    val total = (subtotal - disc).coerceAtLeast(0.0)

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("جمع اقلام: ${PersianUtils.formatPrice(subtotal)}", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = discountText,
                                onValueChange = { discountText = it },
                                label = { Text("تخفیف (تومان)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("مبلغ قابل پرداخت فاکتور: ${PersianUtils.formatPrice(total)}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = initialPaymentText,
                        onValueChange = { initialPaymentText = it },
                        label = { Text("مبلغ پرداختی همزمان مشتری (تومان)") },
                        placeholder = { Text("مثلاً بیعانه یا نقد") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("یادداشت فاکتور") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (errorMessage != null) {
                    item {
                        Text(errorMessage!!, color = ErrorRed, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cust = selectedCustomer
                    if (cust == null) {
                        errorMessage = "لطفاً مشتری را انتخاب کنید"
                        return@Button
                    }
                    if (itemsList.isEmpty()) {
                        errorMessage = "حداقل یک قلم کالا به فاکتور اضافه کنید"
                        return@Button
                    }

                    val saleItems = itemsList.map {
                        WorkshopRepository.SaleItemSpec(
                            finishedGoodId = it.good.id,
                            productId = it.good.productId,
                            productName = it.good.productName,
                            productModelId = it.good.productModelId,
                            modelName = it.good.modelName,
                            colorName = it.good.colorName,
                            sizeName = it.good.sizeName,
                            quantity = it.qty,
                            unitPrice = it.price
                        )
                    }

                    val disc = discountText.toDoubleOrNull() ?: 0.0
                    val initialPay = initialPaymentText.toDoubleOrNull() ?: 0.0

                    onConfirm(
                        WorkshopRepository.SaleFinalizeParam(
                            orderId = null,
                            customerId = cust.id,
                            saleNumber = saleNumber,
                            items = saleItems,
                            discount = disc,
                            initialPaymentAmount = initialPay,
                            initialPaymentMethod = paymentMethod,
                            initialPaymentRef = paymentRef,
                            notes = notes
                        )
                    )
                }
            ) {
                Text("تأیید و صدور فاکتور")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("انصراف")
            }
        }
    )
}

// Dialog: New Order
@Composable
fun NewOrderDialog(
    customers: List<CustomerEntity>,
    availableGoods: List<FinishedGoodEntity>,
    onDismiss: () -> Unit,
    onConfirm: (String, Long, List<WorkshopRepository.OrderItemSpec>, Double, String) -> Unit
) {
    var selectedCustomer by remember { mutableStateOf(customers.firstOrNull()) }
    var orderNumber by remember { mutableStateOf("ORD-${(1000..9999).random()}") }
    var notes by remember { mutableStateOf("") }
    var selectedGood by remember { mutableStateOf(availableGoods.firstOrNull()) }
    var qtyText by remember { mutableStateOf("20") }
    var priceText by remember { mutableStateOf(selectedGood?.wholesalePrice?.toLong()?.toString() ?: "320000") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("ثبت سفارش و رزرو کالا", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("سفارش مشتری با رزرو خودکار موجودی انبار کالای آماده ثبت می‌شود.")
                OutlinedTextField(
                    value = orderNumber,
                    onValueChange = { orderNumber = it },
                    label = { Text("شماره سفارش") },
                    modifier = Modifier.fillMaxWidth()
                )
                // Customer selector
                var custExpanded by remember { mutableStateOf(false) }
                Box {
                    OutlinedTextField(
                        value = selectedCustomer?.name ?: "انتخاب مشتری",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("مشتری") },
                        trailingIcon = {
                            IconButton(onClick = { custExpanded = true }) {
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().clickable { custExpanded = true }
                    )
                    DropdownMenu(expanded = custExpanded, onDismissRequest = { custExpanded = false }) {
                        customers.forEach { c ->
                            DropdownMenuItem(
                                text = { Text("${c.name} (${c.storeName})") },
                                onClick = { selectedCustomer = c; custExpanded = false }
                            )
                        }
                    }
                }

                // Good selector
                var goodExpanded by remember { mutableStateOf(false) }
                Box {
                    OutlinedTextField(
                        value = selectedGood?.let { "${it.productName} - ${it.modelName}" } ?: "انتخاب محصول",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("محصول انتخابی") },
                        trailingIcon = {
                            IconButton(onClick = { goodExpanded = true }) {
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().clickable { goodExpanded = true }
                    )
                    DropdownMenu(expanded = goodExpanded, onDismissRequest = { goodExpanded = false }) {
                        availableGoods.forEach { g ->
                            DropdownMenuItem(
                                text = { Text("${g.productName} (${g.modelName}) - موجود: ${g.availableQuantity}") },
                                onClick = { selectedGood = g; priceText = g.wholesalePrice.toLong().toString(); goodExpanded = false }
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = qtyText,
                        onValueChange = { qtyText = it },
                        label = { Text("تعداد سفارش") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it },
                        label = { Text("قیمت واحد (تومان)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1.5f)
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("توضیحات و مهلت تحویل") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val cust = selectedCustomer ?: return@Button
                val g = selectedGood ?: return@Button
                val q = qtyText.toIntOrNull() ?: 1
                val p = priceText.toDoubleOrNull() ?: 0.0

                val item = WorkshopRepository.OrderItemSpec(
                    finishedGoodId = g.id,
                    productId = g.productId,
                    productName = g.productName,
                    productModelId = g.productModelId,
                    modelName = g.modelName,
                    colorName = g.colorName,
                    sizeName = g.sizeName,
                    quantity = q,
                    unitPrice = p
                )
                onConfirm(orderNumber, cust.id, listOf(item), 0.0, notes)
            }) {
                Text("ثبت سفارش")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("انصراف") }
        }
    )
}

// Dialog: Add Customer
@Composable
fun NewCustomerDialog(
    onDismiss: () -> Unit,
    onConfirm: (CustomerEntity) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var storeName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("تهران") }
    var address by remember { mutableStateOf("") }
    var customerType by remember { mutableStateOf("عمده‌فروش") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("ثبت مشتری عمده جدید", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("نام مشتری/مدیر") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = storeName, onValueChange = { storeName = it }, label = { Text("نام فروشگاه/مرکز پخش") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("تلفن تماس") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("شهر") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("آدرس مغازه / انبار") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = {
                if (name.isNotBlank()) {
                    onConfirm(CustomerEntity(name = name, storeName = storeName, phone = phone, city = city, address = address, customerType = customerType))
                }
            }) {
                Text("افزودن مشتری")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("انصراف") }
        }
    )
}

// Dialog: Record Payment
@Composable
fun RecordPaymentDialog(
    customer: CustomerEntity,
    onDismiss: () -> Unit,
    onConfirm: (Double, String, String, String) -> Unit
) {
    var amountText by remember { mutableStateOf(customer.balance.coerceAtLeast(0.0).toLong().toString()) }
    var method by remember { mutableStateOf("انتقال بانکی") }
    var refNumber by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("ثبت دریافت وجه از ${customer.name}", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("مانده بدهی فعلی: ${PersianUtils.formatPrice(customer.balance)}", color = AmberTertiary, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("مبلغ دریافتی (تومان)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                // Payment Method
                var methodExpanded by remember { mutableStateOf(false) }
                Box {
                    OutlinedTextField(
                        value = method,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("روش پرداخت") },
                        trailingIcon = { IconButton(onClick = { methodExpanded = true }) { Icon(Icons.Default.KeyboardArrowDown, contentDescription = null) } },
                        modifier = Modifier.fillMaxWidth().clickable { methodExpanded = true }
                    )
                    DropdownMenu(expanded = methodExpanded, onDismissRequest = { methodExpanded = false }) {
                        listOf("انتقال بانکی", "کارت به کارت", "چک صیادی", "نقدی", "حواله پایا/ساتنا").forEach { m ->
                            DropdownMenuItem(text = { Text(m) }, onClick = { method = m; methodExpanded = false })
                        }
                    }
                }
                OutlinedTextField(
                    value = refNumber,
                    onValueChange = { refNumber = it },
                    label = { Text("شماره پیگیری / شماره چک") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("توضیحات و بابت") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val amt = amountText.toDoubleOrNull() ?: 0.0
                if (amt > 0) {
                    onConfirm(amt, method, refNumber, notes)
                }
            }) {
                Text("ثبت دریافت")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("انصراف") }
        }
    )
}

// Dialog: Sales Return
@Composable
fun SalesReturnDialog(
    sales: List<SaleEntity>,
    preSelectedSale: SaleEntity?,
    onDismiss: () -> Unit,
    onConfirm: (Long, String, List<Pair<Long, Int>>, String) -> Unit
) {
    var selectedSale by remember { mutableStateOf(preSelectedSale ?: sales.firstOrNull()) }
    var returnNumber by remember { mutableStateOf("RET-${(1000..9999).random()}") }
    var qtyToReturnText by remember { mutableStateOf("5") }
    var reason by remember { mutableStateOf("ایراد دوخت / عدم تطابق سایز") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("ثبت برگشت از فروش (مرجوعی)", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("کالای سالم به موجودی انبار بازگشته و مانده حساب مشتری بستانکار می‌شود.")
                // Sale selector
                var saleExpanded by remember { mutableStateOf(false) }
                Box {
                    OutlinedTextField(
                        value = selectedSale?.let { "${it.saleNumber} - ${it.customerName}" } ?: "انتخاب فاکتور",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("فاکتور فروش مبدا") },
                        trailingIcon = { IconButton(onClick = { saleExpanded = true }) { Icon(Icons.Default.KeyboardArrowDown, contentDescription = null) } },
                        modifier = Modifier.fillMaxWidth().clickable { saleExpanded = true }
                    )
                    DropdownMenu(expanded = saleExpanded, onDismissRequest = { saleExpanded = false }) {
                        sales.forEach { s ->
                            DropdownMenuItem(
                                text = { Text("${s.saleNumber} - ${s.customerName} (${PersianUtils.formatPrice(s.finalAmount)})") },
                                onClick = { selectedSale = s; saleExpanded = false }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = returnNumber,
                    onValueChange = { returnNumber = it },
                    label = { Text("شماره رسید مرجوعی") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = qtyToReturnText,
                    onValueChange = { qtyToReturnText = it },
                    label = { Text("تعداد برگشتی") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("علت برگشت") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val s = selectedSale ?: return@Button
                val q = qtyToReturnText.toIntOrNull() ?: 1
                // For demo/simplicity, if specific fgId is needed, we return from first item or dummy id
                onConfirm(s.id, returnNumber, listOf(1L to q), reason)
            }) {
                Text("ثبت و بازگشت به انبار")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("انصراف") }
        }
    )
}
