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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PriceChange
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import com.example.data.entity.FabricRollEntity
import com.example.data.entity.FabricTypeEntity
import com.example.data.entity.InventoryTransactionEntity
import com.example.data.entity.MaterialEntity
import com.example.data.entity.PriceHistoryEntity
import com.example.data.entity.SupplierEntity
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
fun InventoryScreen(
    viewModel: WorkshopViewModel,
    initialTab: Int = 0
) {
    var selectedTab by remember { mutableIntStateOf(initialTab) }
    val tabTitles = listOf("طاقه‌های پارچه", "خرج‌کار و ملزومات", "تاریخچه قیمت‌ها", "کاردکس انبار")

    val fabricRolls by viewModel.fabricRolls.collectAsState()
    val materials by viewModel.materials.collectAsState()
    val fabricTypes by viewModel.fabricTypes.collectAsState()
    val suppliers by viewModel.suppliers.collectAsState()
    val priceHistory by viewModel.priceHistory.collectAsState()
    val transactions by viewModel.inventoryTransactions.collectAsState()

    var showNewRollDialog by remember { mutableStateOf(false) }
    var showNewMaterialDialog by remember { mutableStateOf(false) }
    var showUpdatePriceDialog by remember { mutableStateOf(false) }
    var selectedItemForPriceUpdate by remember { mutableStateOf<Pair<String, Long>?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (selectedTab == 0) showNewRollDialog = true
                    else if (selectedTab == 1) showNewMaterialDialog = true
                    else if (selectedTab == 2) showUpdatePriceDialog = true
                },
                containerColor = AmberTertiary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("inventory_fab_add")
            ) {
                Icon(Icons.Default.Add, contentDescription = "افزودن")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .testTag("inventory_screen")
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
                    // Fabric Rolls
                    if (fabricRolls.isEmpty()) {
                        EmptyStateView(
                            message = "طاقه پارچه‌ای ثبت نشده است",
                            subMessage = "با زدن دکمه + طاقه‌های خریداری شده را با مشخصات وزن، متراژ و قیمت ثبت کنید",
                            icon = Icons.Default.Inventory
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(fabricRolls, key = { it.id }) { roll ->
                                FabricRollCard(
                                    roll = roll,
                                    onUpdatePrice = {
                                        selectedItemForPriceUpdate = "FABRIC_ROLL" to roll.id
                                        showUpdatePriceDialog = true
                                    }
                                )
                            }
                            item { Spacer(modifier = Modifier.height(72.dp)) }
                        }
                    }
                }
                1 -> {
                    // Materials
                    if (materials.isEmpty()) {
                        EmptyStateView(
                            message = "ملزوماتی ثبت نشده است",
                            subMessage = "کش، نخ، زیپ، مارک و نوارهای مصرفی را در این بخش تعریف کنید"
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(materials, key = { it.id }) { mat ->
                                MaterialCard(
                                    material = mat,
                                    onUpdatePrice = {
                                        selectedItemForPriceUpdate = "MATERIAL" to mat.id
                                        showUpdatePriceDialog = true
                                    }
                                )
                            }
                            item { Spacer(modifier = Modifier.height(72.dp)) }
                        }
                    }
                }
                2 -> {
                    // Price History
                    if (priceHistory.isEmpty()) {
                        EmptyStateView(
                            message = "تاریخچه‌ای از تغییر قیمت ثبت نشده است",
                            subMessage = "تغییرات قیمت روز بازار جهت تفکیک بهای تمام‌شده واقعی و جایگزینی ثبت می‌شوند",
                            icon = Icons.Default.PriceChange
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(priceHistory, key = { it.id }) { record ->
                                PriceHistoryCard(record = record)
                            }
                            item { Spacer(modifier = Modifier.height(72.dp)) }
                        }
                    }
                }
                3 -> {
                    // Inventory Ledger / Transactions
                    if (transactions.isEmpty()) {
                        EmptyStateView(
                            message = "تراکنشی در کاردکس ثبت نشده است",
                            subMessage = "ورود و خروج‌های انبار به صورت خودکار در این قسمت ثبت می‌شوند",
                            icon = Icons.Default.History
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(transactions, key = { it.id }) { tx ->
                                TransactionCard(tx = tx)
                            }
                            item { Spacer(modifier = Modifier.height(72.dp)) }
                        }
                    }
                }
            }
        }
    }

    // New Roll Dialog
    if (showNewRollDialog) {
        NewFabricRollDialog(
            fabricTypes = fabricTypes,
            suppliers = suppliers,
            onDismiss = { showNewRollDialog = false },
            onConfirm = { roll ->
                viewModel.addFabricRoll(roll) {
                    showNewRollDialog = false
                }
            }
        )
    }

    // New Material Dialog
    if (showNewMaterialDialog) {
        NewMaterialDialog(
            suppliers = suppliers,
            onDismiss = { showNewMaterialDialog = false },
            onConfirm = { mat ->
                viewModel.addMaterial(mat) {
                    showNewMaterialDialog = false
                }
            }
        )
    }

    // Update Price Dialog
    if (showUpdatePriceDialog) {
        UpdatePriceDialog(
            itemPair = selectedItemForPriceUpdate,
            rolls = fabricRolls,
            materials = materials,
            onDismiss = {
                showUpdatePriceDialog = false
                selectedItemForPriceUpdate = null
            },
            onConfirm = { itemType, itemId, newPrice, reason ->
                viewModel.updateRawMaterialPrice(itemType, itemId, newPrice, reason) {
                    showUpdatePriceDialog = false
                    selectedItemForPriceUpdate = null
                }
            }
        )
    }
}

@Composable
fun FabricRollCard(
    roll: FabricRollEntity,
    onUpdatePrice: () -> Unit
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
                        text = "طاقه ${roll.rollNumber} - ${roll.fabricName}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "رنگ: ${roll.colorName} • خرید: ${PersianUtils.formatDateOnly(roll.purchaseDate)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                StatusBadge(status = roll.status)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Stock progress
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("مانده: ${PersianUtils.formatNumber(roll.remainingQuantity)} KG", fontWeight = FontWeight.Bold, color = if (roll.remainingQuantity < 15) ErrorRed else TealSecondary)
                Text("مصرف‌شده: ${PersianUtils.formatNumber(roll.consumedQuantity)} KG", style = MaterialTheme.typography.bodySmall)
                Text("وزن اولیه: ${PersianUtils.formatNumber(roll.purchaseWeightKg)} KG", style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(6.dp))

            val total = roll.purchaseWeightKg.coerceAtLeast(1.0).toFloat()
            LinearProgressIndicator(
                progress = { (roll.remainingQuantity.toFloat() / total).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = if (roll.remainingQuantity < 15) ErrorRed else TealSecondary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("بهای تمام‌شده واقعی:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        "${PersianUtils.formatPrice(roll.actualCostPerKg)} / KG",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("قیمت روز جایگزینی:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        "${PersianUtils.formatPrice(roll.currentPricePerUnit)} / KG",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = AmberTertiary
                    )
                }
            }

            if (roll.allocatedFreightCost > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "کرایه تخصیص‌یافته: ${PersianUtils.formatPrice(roll.allocatedFreightCost)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                OutlinedButton(
                    onClick = onUpdatePrice,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.PriceChange, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("تغییر قیمت روز", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
fun MaterialCard(
    material: MaterialEntity,
    onUpdatePrice: () -> Unit
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
                    Text(material.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("دسته: ${material.category} • واحد: ${material.unit}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                val isLow = material.stockQuantity <= material.minStockQuantity
                StatusBadge(status = if (isLow) "کسری موجودی" else "موجود")
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "موجودی: ${PersianUtils.formatNumber(material.stockQuantity)} ${material.unit}",
                    fontWeight = FontWeight.Bold,
                    color = if (material.stockQuantity <= material.minStockQuantity) ErrorRed else SuccessGreen
                )
                Text("حداقل سفارش: ${material.minStockQuantity} ${material.unit}", style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("قیمت خرید: ${PersianUtils.formatPrice(material.purchasePrice)}", style = MaterialTheme.typography.bodySmall)
                Text("قیمت روز: ${PersianUtils.formatPrice(material.currentPrice)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = AmberTertiary)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                OutlinedButton(onClick = onUpdatePrice, shape = RoundedCornerShape(8.dp)) {
                    Icon(Icons.Default.PriceChange, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("بروزرسانی قیمت", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
fun PriceHistoryCard(record: PriceHistoryEntity) {
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
                Text(record.itemName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                Text(
                    text = "${if (record.changePercent >= 0) "+" else ""}${PersianUtils.formatNumber(record.changePercent)}%",
                    fontWeight = FontWeight.Bold,
                    color = if (record.changePercent >= 0) ErrorRed else SuccessGreen
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("قبلی: ${PersianUtils.formatPrice(record.previousPrice)}", style = MaterialTheme.typography.bodySmall)
                Text("جدید: ${PersianUtils.formatPrice(record.newPrice)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${PersianUtils.formatDate(record.date)} • علت: ${record.reason}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun TransactionCard(tx: InventoryTransactionEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(tx.itemName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                Text(
                    text = "${if (tx.quantityChange > 0) "+" else ""}${PersianUtils.formatNumber(tx.quantityChange)}",
                    fontWeight = FontWeight.Bold,
                    color = if (tx.quantityChange > 0) SuccessGreen else ErrorRed
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("نوع: ${tx.transactionType}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("مانده پس از تراکنش: ${PersianUtils.formatNumber(tx.remainingAfter)}", style = MaterialTheme.typography.labelSmall)
            }
            if (tx.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(tx.notes, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

// Dialog: New Fabric Roll
@Composable
fun NewFabricRollDialog(
    fabricTypes: List<FabricTypeEntity>,
    suppliers: List<SupplierEntity>,
    onDismiss: () -> Unit,
    onConfirm: (FabricRollEntity) -> Unit
) {
    var rollNumber by remember { mutableStateOf("R-${(100..999).random()}") }
    var fabricName by remember { mutableStateOf("دورس دونخ مشکی") }
    var selectedType by remember { mutableStateOf(fabricTypes.firstOrNull()) }
    var selectedSupplier by remember { mutableStateOf(suppliers.firstOrNull()) }
    var colorName by remember { mutableStateOf("مشکی") }
    var weightText by remember { mutableStateOf("100.0") }
    var pricePerKgText by remember { mutableStateOf("850000") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("ثبت خرید طاقه پارچه جدید", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = rollNumber, onValueChange = { rollNumber = it }, label = { Text("شماره طاقه") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = fabricName, onValueChange = { fabricName = it }, label = { Text("نام و جنس پارچه") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = colorName, onValueChange = { colorName = it }, label = { Text("رنگ طاقه") }, modifier = Modifier.fillMaxWidth())

                OutlinedTextField(
                    value = weightText,
                    onValueChange = { weightText = it },
                    label = { Text("وزن طاقه (کیلوگرم)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = pricePerKgText,
                    onValueChange = { pricePerKgText = it },
                    label = { Text("قیمت خرید هر کیلوگرم (تومان)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val w = weightText.toDoubleOrNull() ?: 0.0
                val p = pricePerKgText.toDoubleOrNull() ?: 0.0
                if (w > 0 && p > 0) {
                    onConfirm(
                        FabricRollEntity(
                            rollNumber = rollNumber,
                            fabricName = fabricName,
                            fabricTypeId = selectedType?.id ?: 1,
                            colorName = colorName,
                            purchaseWeightKg = w,
                            purchasePricePerUnit = p,
                            currentPricePerUnit = p,
                            supplierId = selectedSupplier?.id ?: 1,
                            supplierName = selectedSupplier?.name ?: "نساجی بافندگی پارس"
                        )
                    )
                }
            }) {
                Text("ثبت طاقه")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("انصراف") }
        }
    )
}

// Dialog: New Material
@Composable
fun NewMaterialDialog(
    suppliers: List<SupplierEntity>,
    onDismiss: () -> Unit,
    onConfirm: (MaterialEntity) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("کش") }
    var unit by remember { mutableStateOf("متر") }
    var priceText by remember { mutableStateOf("15000") }
    var stockText by remember { mutableStateOf("100") }
    var minStockText by remember { mutableStateOf("20") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("ثبت خرج‌کار و ملزومات جدید", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("نام قلم (مثلاً کش، نخ، زیپ)") }, modifier = Modifier.fillMaxWidth())

                var catExpanded by remember { mutableStateOf(false) }
                Box {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("دسته‌بندی") },
                        trailingIcon = { IconButton(onClick = { catExpanded = true }) { Icon(Icons.Default.KeyboardArrowDown, contentDescription = null) } },
                        modifier = Modifier.fillMaxWidth().clickable { catExpanded = true }
                    )
                    DropdownMenu(expanded = catExpanded, onDismissRequest = { catExpanded = false }) {
                        listOf("کش", "نخ", "زیپ", "نوار", "چاپ", "دکمه", "مارک و لیبل", "بسته‌بندی", "سایر").forEach { c ->
                            DropdownMenuItem(text = { Text(c) }, onClick = { category = c; catExpanded = false })
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = unit, onValueChange = { unit = it }, label = { Text("واحد شمارش") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = priceText, onValueChange = { priceText = it }, label = { Text("قیمت واحد (تومان)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1.5f))
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = stockText, onValueChange = { stockText = it }, label = { Text("موجودی اولیه") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                    OutlinedTextField(value = minStockText, onValueChange = { minStockText = it }, label = { Text("حداقل هشدار") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val p = priceText.toDoubleOrNull() ?: 0.0
                val s = stockText.toDoubleOrNull() ?: 0.0
                val ms = minStockText.toDoubleOrNull() ?: 0.0
                if (name.isNotBlank()) {
                    onConfirm(
                        MaterialEntity(
                            name = name,
                            category = category,
                            unit = unit,
                            purchasePrice = p,
                            currentPrice = p,
                            stockQuantity = s,
                            minStockQuantity = ms
                        )
                    )
                }
            }) {
                Text("ثبت قلم")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("انصراف") }
        }
    )
}

// Dialog: Update Market Price (تاریخچه و قیمت روز)
@Composable
fun UpdatePriceDialog(
    itemPair: Pair<String, Long>?,
    rolls: List<FabricRollEntity>,
    materials: List<MaterialEntity>,
    onDismiss: () -> Unit,
    onConfirm: (String, Long, Double, String) -> Unit
) {
    var itemType by remember { mutableStateOf(itemPair?.first ?: "FABRIC_ROLL") }
    var selectedItemId by remember { mutableStateOf(itemPair?.second ?: rolls.firstOrNull()?.id ?: 1L) }
    var newPriceText by remember { mutableStateOf("950000") }
    var reason by remember { mutableStateOf("افزایش قیمت کارخانه / تورم بازار") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("بروزرسانی قیمت روز بازار", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("بهای تمام‌شده واقعی حفظ شده و بهای جایگزینی بروز می‌شود.", style = MaterialTheme.typography.bodySmall)

                // Item Type
                var typeExpanded by remember { mutableStateOf(false) }
                Box {
                    OutlinedTextField(
                        value = if (itemType == "FABRIC_ROLL") "طاقه پارچه" else "ملزومات و خرج‌کار",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("نوع قلم") },
                        trailingIcon = { IconButton(onClick = { typeExpanded = true }) { Icon(Icons.Default.KeyboardArrowDown, contentDescription = null) } },
                        modifier = Modifier.fillMaxWidth().clickable { typeExpanded = true }
                    )
                    DropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                        DropdownMenuItem(text = { Text("طاقه پارچه") }, onClick = { itemType = "FABRIC_ROLL"; selectedItemId = rolls.firstOrNull()?.id ?: 1; typeExpanded = false })
                        DropdownMenuItem(text = { Text("ملزومات و خرج‌کار") }, onClick = { itemType = "MATERIAL"; selectedItemId = materials.firstOrNull()?.id ?: 1; typeExpanded = false })
                    }
                }

                // Item selection
                var itemExpanded by remember { mutableStateOf(false) }
                val currentName = if (itemType == "FABRIC_ROLL") {
                    rolls.find { it.id == selectedItemId }?.let { "${it.rollNumber} - ${it.fabricName}" } ?: "انتخاب طاقه"
                } else {
                    materials.find { it.id == selectedItemId }?.name ?: "انتخاب قلم"
                }

                Box {
                    OutlinedTextField(
                        value = currentName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("قلم مورد نظر") },
                        trailingIcon = { IconButton(onClick = { itemExpanded = true }) { Icon(Icons.Default.KeyboardArrowDown, contentDescription = null) } },
                        modifier = Modifier.fillMaxWidth().clickable { itemExpanded = true }
                    )
                    DropdownMenu(expanded = itemExpanded, onDismissRequest = { itemExpanded = false }) {
                        if (itemType == "FABRIC_ROLL") {
                            rolls.forEach { r ->
                                DropdownMenuItem(text = { Text("${r.rollNumber} - ${r.fabricName}") }, onClick = { selectedItemId = r.id; newPriceText = r.currentPricePerUnit.toLong().toString(); itemExpanded = false })
                            }
                        } else {
                            materials.forEach { m ->
                                DropdownMenuItem(text = { Text(m.name) }, onClick = { selectedItemId = m.id; newPriceText = m.currentPrice.toLong().toString(); itemExpanded = false })
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = newPriceText,
                    onValueChange = { newPriceText = it },
                    label = { Text("قیمت جدید روز (تومان)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("علت تغییر قیمت") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val p = newPriceText.toDoubleOrNull() ?: 0.0
                if (p > 0) {
                    onConfirm(itemType, selectedItemId, p, reason)
                }
            }) {
                Text("ثبت در تاریخچه")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("انصراف") }
        }
    )
}
