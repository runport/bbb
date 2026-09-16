package com.example.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.entity.CarrierEntity
import com.example.data.entity.ExpenseEntity
import com.example.data.entity.FabricRollEntity
import com.example.data.entity.FreightBillEntity
import com.example.data.entity.WasteEntity
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
fun FreightAndFinanceScreen(
    viewModel: WorkshopViewModel,
    initialTab: Int = 0
) {
    var selectedTab by remember { mutableIntStateOf(initialTab) }
    val tabTitles = listOf("باربری و تخصیص کرایه", "هزینه‌های جاری کارگاه", "ضایعات پارچه و تولید")

    val freightBills by viewModel.freightBills.collectAsState()
    val carriers by viewModel.carriers.collectAsState()
    val rolls by viewModel.fabricRolls.collectAsState()
    val expenses by viewModel.expenses.collectAsState()
    val wastes by viewModel.allWastes.collectAsState()

    var showNewBillDialog by remember { mutableStateOf(false) }
    var showAllocateDialog by remember { mutableStateOf(false) }
    var selectedBillForAlloc by remember { mutableStateOf<FreightBillEntity?>(null) }
    var showNewExpenseDialog by remember { mutableStateOf(false) }
    var showNewWasteDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (selectedTab == 0) showNewBillDialog = true
                    else if (selectedTab == 1) showNewExpenseDialog = true
                    else if (selectedTab == 2) showNewWasteDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("finance_fab_add")
            ) {
                Icon(Icons.Default.Add, contentDescription = "افزودن")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .testTag("freight_finance_screen")
        ) {
            PrimaryTabRow(selectedTabIndex = selectedTab) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold) }
                    )
                }
            }

            when (selectedTab) {
                0 -> {
                    // Freight Bills
                    if (freightBills.isEmpty()) {
                        EmptyStateView(
                            message = "قبض باربری ثبت نشده است",
                            subMessage = "قبوض باربری طاقه‌ها را ثبت کرده و کرایه را روی بهای تمام‌شده طاقه‌ها تسهیم کنید",
                            icon = Icons.Default.LocalShipping
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(freightBills, key = { it.id }) { bill ->
                                FreightBillCard(
                                    bill = bill,
                                    onAllocate = {
                                        selectedBillForAlloc = bill
                                        showAllocateDialog = true
                                    }
                                )
                            }
                            item { Spacer(modifier = Modifier.height(72.dp)) }
                        }
                    }
                }
                1 -> {
                    // Expenses
                    if (expenses.isEmpty()) {
                        EmptyStateView(
                            message = "هزینه‌ای ثبت نشده است",
                            subMessage = "هزینه‌های کارگاه نظیر اجاره، قبوض برق و گاز، تعمیرات چرخ و بسته‌بندی را ثبت کنید",
                            icon = Icons.Default.AttachMoney
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(expenses, key = { it.id }) { exp ->
                                ExpenseCard(expense = exp)
                            }
                            item { Spacer(modifier = Modifier.height(72.dp)) }
                        }
                    }
                }
                2 -> {
                    // Waste
                    if (wastes.isEmpty()) {
                        EmptyStateView(
                            message = "ضایعاتی ثبت نشده است",
                            subMessage = "ضایعات طاقه، دورریز برش، خرابی چاپ و دوخت را جهت کنترل راندمان ثبت کنید",
                            icon = Icons.Default.DeleteSweep
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(wastes, key = { it.id }) { waste ->
                                WasteCard(waste = waste)
                            }
                            item { Spacer(modifier = Modifier.height(72.dp)) }
                        }
                    }
                }
            }
        }
    }

    // New Bill Dialog
    if (showNewBillDialog) {
        NewFreightBillDialog(
            carriers = carriers,
            onDismiss = { showNewBillDialog = false },
            onConfirm = { bill ->
                viewModel.createFreightBill(bill) {
                    showNewBillDialog = false
                }
            }
        )
    }

    // Allocate Dialog
    if (showAllocateDialog && selectedBillForAlloc != null) {
        SmartFreightAllocationDialog(
            bill = selectedBillForAlloc!!,
            rolls = rolls,
            onDismiss = {
                showAllocateDialog = false
                selectedBillForAlloc = null
            },
            onConfirm = { billId, basis, items ->
                viewModel.allocateFreight(billId, basis, items) {
                    showAllocateDialog = false
                    selectedBillForAlloc = null
                }
            }
        )
    }

    // New Expense Dialog
    if (showNewExpenseDialog) {
        NewExpenseDialog(
            onDismiss = { showNewExpenseDialog = false },
            onConfirm = { cat, title, amt, method, notes ->
                viewModel.recordExpense(cat, title, amt, method, notes) {
                    showNewExpenseDialog = false
                }
            }
        )
    }

    // New Waste Dialog
    if (showNewWasteDialog) {
        NewWasteDialog(
            onDismiss = { showNewWasteDialog = false },
            onConfirm = { waste ->
                viewModel.recordWaste(waste) {
                    showNewWasteDialog = false
                }
            }
        )
    }
}

@Composable
fun FreightBillCard(
    bill: FreightBillEntity,
    onAllocate: () -> Unit
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
                    Text("قبض باربری ${bill.receiptNumber}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("${bill.carrierName} • ${PersianUtils.formatDateOnly(bill.date)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                StatusBadge(status = if (bill.isAllocated) "تخصیص‌یافته" else "تخصیص‌نیافته")
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("مبلغ کل کرایه:", style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = PersianUtils.formatPrice(bill.totalFreightAmount),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (!bill.isAllocated) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onAllocate,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TealSecondary)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("تخصیص کرایه به طاقه‌های این بارنامه")
                }
            } else {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "بر مبنای: ${bill.allocationBasis} به طاقه‌ها تسهیم شد",
                    style = MaterialTheme.typography.labelSmall,
                    color = SuccessGreen
                )
            }
        }
    }
}

@Composable
fun ExpenseCard(expense: ExpenseEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(expense.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    Text("دسته: ${expense.category}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(
                    text = PersianUtils.formatPrice(expense.amount),
                    fontWeight = FontWeight.Bold,
                    color = ErrorRed
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${PersianUtils.formatDate(expense.date)} • پرداخت با ${expense.paymentMethod}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun WasteCard(waste: WasteEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(waste.wasteType, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                Text(
                    text = "${PersianUtils.formatNumber(waste.quantity)} ${waste.unit}",
                    fontWeight = FontWeight.Bold,
                    color = ErrorRed
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("هزینه تقریبی: ${PersianUtils.formatPrice(waste.estimatedCost)}", style = MaterialTheme.typography.bodySmall)
                Text(PersianUtils.formatDateOnly(waste.date), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (waste.reason.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text("علت: ${waste.reason}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

// Dialog: New Freight Bill
@Composable
fun NewFreightBillDialog(
    carriers: List<CarrierEntity>,
    onDismiss: () -> Unit,
    onConfirm: (FreightBillEntity) -> Unit
) {
    var selectedCarrier by remember { mutableStateOf(carriers.firstOrNull()) }
    var receiptNumber by remember { mutableStateOf("BOL-${(1000..9999).random()}") }
    var amountText by remember { mutableStateOf("1200000") }
    var notes by remember { mutableStateOf("بارنامه حمل طاقه‌های جدید") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("ثبت قبض باربری جدید", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Carrier selector
                var carrierExpanded by remember { mutableStateOf(false) }
                Box {
                    OutlinedTextField(
                        value = selectedCarrier?.name ?: "انتخاب باربری",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("باربری") },
                        trailingIcon = { IconButton(onClick = { carrierExpanded = true }) { Icon(Icons.Default.KeyboardArrowDown, contentDescription = null) } },
                        modifier = Modifier.fillMaxWidth().clickable { carrierExpanded = true }
                    )
                    DropdownMenu(expanded = carrierExpanded, onDismissRequest = { carrierExpanded = false }) {
                        carriers.forEach { c ->
                            DropdownMenuItem(text = { Text(c.name) }, onClick = { selectedCarrier = c; carrierExpanded = false })
                        }
                    }
                }

                OutlinedTextField(value = receiptNumber, onValueChange = { receiptNumber = it }, label = { Text("شماره رسید بارنامه") }, modifier = Modifier.fillMaxWidth())

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("مبلغ کل کرایه (تومان)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("توضیحات بارنامه") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = {
                val amt = amountText.toDoubleOrNull() ?: 0.0
                if (amt > 0) {
                    onConfirm(
                        FreightBillEntity(
                            receiptNumber = receiptNumber,
                            carrierId = selectedCarrier?.id ?: 1,
                            carrierName = selectedCarrier?.name ?: "باربری وطن",
                            totalFreightAmount = amt,
                            notes = notes
                        )
                    )
                }
            }) {
                Text("ثبت قبض")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("انصراف") }
        }
    )
}

// Dialog: Smart Freight Allocation
@Composable
fun SmartFreightAllocationDialog(
    bill: FreightBillEntity,
    rolls: List<FabricRollEntity>,
    onDismiss: () -> Unit,
    onConfirm: (Long, String, List<WorkshopRepository.AllocationItemSpec>) -> Unit
) {
    var basis by remember { mutableStateOf("WEIGHT") } // WEIGHT, QUANTITY, PURCHASE_AMOUNT
    var selectedRolls by remember { mutableStateOf(rolls.take(3)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تخصیص هوشمند کرایه باربری", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "مبلغ کرایه برای تسهیم: ${PersianUtils.formatPrice(bill.totalFreightAmount)}",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Basis dropdown
                var basisExpanded by remember { mutableStateOf(false) }
                Box {
                    val label = when (basis) {
                        "WEIGHT" -> "بر اساس وزن (کیلوگرم)"
                        "QUANTITY" -> "بر اساس تعداد طاقه‌ها"
                        "PURCHASE_AMOUNT" -> "بر اساس ارزش خرید طاقه‌ها"
                        else -> "دستی"
                    }
                    OutlinedTextField(
                        value = label,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("مبنای تسهیم کرایه") },
                        trailingIcon = { IconButton(onClick = { basisExpanded = true }) { Icon(Icons.Default.KeyboardArrowDown, contentDescription = null) } },
                        modifier = Modifier.fillMaxWidth().clickable { basisExpanded = true }
                    )
                    DropdownMenu(expanded = basisExpanded, onDismissRequest = { basisExpanded = false }) {
                        DropdownMenuItem(text = { Text("بر اساس وزن (کیلوگرم)") }, onClick = { basis = "WEIGHT"; basisExpanded = false })
                        DropdownMenuItem(text = { Text("بر اساس تعداد طاقه‌ها") }, onClick = { basis = "QUANTITY"; basisExpanded = false })
                        DropdownMenuItem(text = { Text("بر اساس ارزش خرید طاقه‌ها") }, onClick = { basis = "PURCHASE_AMOUNT"; basisExpanded = false })
                    }
                }

                Text("تخصیص به ${selectedRolls.size} طاقه موجود انجام می‌شود. با این کار بهای تمام‌شده واقعی هر کیلو و متر پارچه با در نظر گرفتن کرایه حمل دقیقاً محاسبه خواهد شد.", style = MaterialTheme.typography.bodySmall)

                selectedRolls.forEach { roll ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${roll.rollNumber} (${roll.fabricName})", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                            Text("${roll.purchaseWeightKg} KG", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val specs = selectedRolls.map { r ->
                    WorkshopRepository.AllocationItemSpec(
                        itemType = "FABRIC_ROLL",
                        itemId = r.id,
                        itemName = "${r.rollNumber} - ${r.fabricName}",
                        itemWeight = r.purchaseWeightKg,
                        itemQuantity = 1.0,
                        itemPurchaseAmount = r.purchasePricePerUnit * r.purchaseWeightKg
                    )
                }
                onConfirm(bill.id, basis, specs)
            }) {
                Text("محاسبه و تسهیم کرایه")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("انصراف") }
        }
    )
}

// Dialog: New Expense
@Composable
fun NewExpenseDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, Double, String, String) -> Unit
) {
    var category by remember { mutableStateOf("اجاره کارگاه") }
    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("1500000") }
    var method by remember { mutableStateOf("کارت") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("ثبت هزینه جاری کارگاه", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                var catExpanded by remember { mutableStateOf(false) }
                Box {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("دسته‌بندی هزینه") },
                        trailingIcon = { IconButton(onClick = { catExpanded = true }) { Icon(Icons.Default.KeyboardArrowDown, contentDescription = null) } },
                        modifier = Modifier.fillMaxWidth().clickable { catExpanded = true }
                    )
                    DropdownMenu(expanded = catExpanded, onDismissRequest = { catExpanded = false }) {
                        listOf("اجاره کارگاه", "برق و روشنایی", "گاز و آب", "تعمیرات چرخ و دستگاه", "دستمزد دوخت", "پذیرایی و ناهار", "ملزومات بسته‌بندی", "سایر هزینه‌ها").forEach { c ->
                            DropdownMenuItem(text = { Text(c) }, onClick = { category = c; catExpanded = false })
                        }
                    }
                }

                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("عنوان هزینه (مثلاً اجاره ماه مهر)") }, modifier = Modifier.fillMaxWidth())

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("مبلغ هزینه (تومان)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("توضیحات") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = {
                val amt = amountText.toDoubleOrNull() ?: 0.0
                if (amt > 0 && title.isNotBlank()) {
                    onConfirm(category, title, amt, method, notes)
                }
            }) {
                Text("ثبت هزینه")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("انصراف") }
        }
    )
}

// Dialog: New Waste
@Composable
fun NewWasteDialog(
    onDismiss: () -> Unit,
    onConfirm: (WasteEntity) -> Unit
) {
    var wasteType by remember { mutableStateOf("ضایعات دورریز برش پارچه") }
    var qtyText by remember { mutableStateOf("4.5") }
    var costText by remember { mutableStateOf("3800000") }
    var reason by remember { mutableStateOf("دم‌قیچی برش الگوی ۶ جیب") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("ثبت ضایعات کارگاه", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = wasteType, onValueChange = { wasteType = it }, label = { Text("نوع ضایعات") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = qtyText, onValueChange = { qtyText = it }, label = { Text("مقدار (کیلوگرم / عدد)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = costText, onValueChange = { costText = it }, label = { Text("هزینه تقریبی تلف‌شده (تومان)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = reason, onValueChange = { reason = it }, label = { Text("علت یا مرحله رخ‌داد") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = {
                val q = qtyText.toDoubleOrNull() ?: 0.0
                val c = costText.toDoubleOrNull() ?: 0.0
                onConfirm(
                    WasteEntity(
                        wasteType = wasteType,
                        quantity = q,
                        estimatedCost = c,
                        reason = reason
                    )
                )
            }) {
                Text("ثبت ضایعات")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("انصراف") }
        }
    )
}
