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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PrecisionManufacturing
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.entity.CuttingBatchEntity
import com.example.data.entity.FabricRollEntity
import com.example.data.entity.FinishedGoodEntity
import com.example.data.entity.ProductEntity
import com.example.data.entity.ProductModelEntity
import com.example.data.entity.ProductionBatchEntity
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
fun ProductionScreen(
    viewModel: WorkshopViewModel,
    initialTab: Int = 0
) {
    var selectedTab by remember { mutableIntStateOf(initialTab) }
    val tabTitles = listOf("عملیات برش طاقه", "خط تولید و دوخت", "انبار کالای آماده")

    val cuttingBatches by viewModel.cuttingBatches.collectAsState()
    val productionBatches by viewModel.productionBatches.collectAsState()
    val finishedGoods by viewModel.finishedGoods.collectAsState()
    val availableRolls by viewModel.availableRolls.collectAsState()
    val products by viewModel.products.collectAsState()
    val models by viewModel.models.collectAsState()

    var showNewCutDialog by remember { mutableStateOf(false) }
    var showAddFinishedGoodDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (selectedTab == 0 || selectedTab == 1) {
                        showNewCutDialog = true
                    } else {
                        showAddFinishedGoodDialog = true
                    }
                },
                containerColor = TealSecondary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("production_fab_add")
            ) {
                Icon(Icons.Default.Add, contentDescription = "ایجاد")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .testTag("production_screen")
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
                    // Cutting Batches list
                    if (cuttingBatches.isEmpty()) {
                        EmptyStateView(
                            message = "هنوز برشی ثبت نشده است",
                            subMessage = "با زدن دکمه + طاقه پارچه را انتخاب کرده و قطعات مدل‌ها را برش بزنید",
                            icon = Icons.Default.ContentCut
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(cuttingBatches, key = { it.id }) { batch ->
                                CuttingBatchCard(batch = batch)
                            }
                            item { Spacer(modifier = Modifier.height(72.dp)) }
                        }
                    }
                }
                1 -> {
                    // Production Batches
                    if (productionBatches.isEmpty()) {
                        EmptyStateView(
                            message = "دسته‌ای در خط تولید نیست",
                            subMessage = "پس از برش، دسته‌های دوخت و چاپ به طور خودکار در اینجا قرار می‌گیرند",
                            icon = Icons.Default.PrecisionManufacturing
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(productionBatches, key = { it.id }) { pBatch ->
                                ProductionBatchCard(
                                    batch = pBatch,
                                    onComplete = {
                                        viewModel.completeProductionBatch(pBatch.id, wholesalePrice = 0.0)
                                    }
                                )
                            }
                            item { Spacer(modifier = Modifier.height(72.dp)) }
                        }
                    }
                }
                2 -> {
                    // Finished Goods
                    if (finishedGoods.isEmpty()) {
                        EmptyStateView(
                            message = "محصولی در انبار کالای آماده نیست",
                            subMessage = "با اتمام دسته‌های دوخت، موجودی آماده فروش به طور خودکار اینجا اضافه می‌شود",
                            icon = Icons.Default.Inventory2
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(finishedGoods, key = { it.id }) { fg ->
                                FinishedGoodCard(fg = fg)
                            }
                            item { Spacer(modifier = Modifier.height(72.dp)) }
                        }
                    }
                }
            }
        }
    }

    // New Cut Dialog
    if (showNewCutDialog) {
        NewCutDialog(
            rolls = availableRolls,
            products = products,
            models = models,
            onDismiss = { showNewCutDialog = false },
            onConfirm = { rollId, batchNumber, specs, notes ->
                viewModel.executeCutting(rollId, batchNumber, specs, notes) {
                    showNewCutDialog = false
                }
            }
        )
    }

    // Add Finished Good Direct Dialog
    if (showAddFinishedGoodDialog) {
        AddFinishedGoodDialog(
            products = products,
            models = models,
            onDismiss = { showAddFinishedGoodDialog = false },
            onConfirm = { fg ->
                viewModel.addFinishedGood(fg) {
                    showAddFinishedGoodDialog = false
                }
            }
        )
    }
}

@Composable
fun CuttingBatchCard(batch: CuttingBatchEntity) {
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ContentCut, contentDescription = null, tint = TealSecondary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "دسته برش ${batch.batchNumber}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                StatusBadge(status = batch.status)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "مصرف پارچه: ${PersianUtils.formatNumber(batch.totalFabricConsumedKg)} کیلوگرم",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "تعداد کل: ${batch.totalPiecesCut} قطعه",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = TealSecondary
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = PersianUtils.formatDate(batch.date),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ProductionBatchCard(
    batch: ProductionBatchEntity,
    onComplete: () -> Unit
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
                        text = "${batch.productName} (${batch.modelName})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "رنگ: ${batch.colorName} • سایز: ${batch.sizeName} • تعداد: ${batch.quantity} عدد",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                StatusBadge(status = batch.stage)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "بهای تمام‌شده هر عدد: ${PersianUtils.formatPrice(batch.unitCostActual)}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "مرحله: ${batch.stage}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TealSecondary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (batch.status != "آماده") {
                Button(
                    onClick = onComplete,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("اتمام دوخت و انتقال به انبار کالای آماده")
                }
            } else {
                Text("کالاها با موفقیت به انبار منتقل شدند", color = SuccessGreen, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
fun FinishedGoodCard(fg: FinishedGoodEntity) {
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
                        text = "${fg.productName} - ${fg.modelName}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "رنگ: ${fg.colorName} • سایز: ${fg.sizeName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (fg.availableQuantity > 0) SuccessGreen.copy(alpha = 0.1f) else ErrorRed.copy(alpha = 0.1f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "موجود آماده فروش: ${fg.availableQuantity} عدد",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (fg.availableQuantity > 0) SuccessGreen else ErrorRed
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Inventory breakdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("تولید کل: ${fg.producedQuantity}", style = MaterialTheme.typography.bodySmall)
                Text("فروخته‌شده: ${fg.soldQuantity}", style = MaterialTheme.typography.bodySmall)
                Text("رزرو در سفارشات: ${fg.reservedQuantity}", style = MaterialTheme.typography.bodySmall, color = AmberTertiary)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Progress bar
            val total = fg.producedQuantity.coerceAtLeast(1).toFloat()
            LinearProgressIndicator(
                progress = { (fg.soldQuantity.toFloat() / total).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "بهای تمام‌شده واقعی: ${PersianUtils.formatPrice(fg.unitCostActual)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "قیمت عمده: ${PersianUtils.formatPrice(fg.wholesalePrice)}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

// Dialog: New Cutting Batch with Multi-Model Split & No Negative Stock Guard
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewCutDialog(
    rolls: List<FabricRollEntity>,
    products: List<ProductEntity>,
    models: List<ProductModelEntity>,
    onDismiss: () -> Unit,
    onConfirm: (Long, String, List<WorkshopRepository.CutSpecification>, String) -> Unit
) {
    var selectedRoll by remember { mutableStateOf(rolls.firstOrNull()) }
    var batchNumber by remember { mutableStateOf("CUT-${(100..999).random()}") }
    var notes by remember { mutableStateOf("") }

    var selectedProduct by remember { mutableStateOf(products.firstOrNull()) }
    var selectedModel by remember { mutableStateOf(models.firstOrNull()) }
    var colorName by remember { mutableStateOf(selectedRoll?.colorName ?: "مشکی") }
    var sizeName by remember { mutableStateOf("L") }
    var quantityText by remember { mutableStateOf("50") }
    var fabricKgText by remember { mutableStateOf("30.0") } // Fabric consumed in KG
    var accCostText by remember { mutableStateOf("15000") } // Accessories cost per piece
    var printCostText by remember { mutableStateOf("10000") } // Print cost per piece

    var specsList by remember { mutableStateOf(listOf<WorkshopRepository.CutSpecification>()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("ثبت عملیات برش طاقه پارچه", fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    // Roll selector
                    var rollExpanded by remember { mutableStateOf(false) }
                    Box {
                        OutlinedTextField(
                            value = selectedRoll?.let { "${it.rollNumber} - ${it.fabricName} (مانده: ${it.remainingQuantity} KG)" } ?: "انتخاب طاقه",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("طاقه پارچه برای برش") },
                            trailingIcon = {
                                IconButton(onClick = { rollExpanded = true }) {
                                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
                                }
                            },
                            modifier = Modifier.fillMaxWidth().clickable { rollExpanded = true }
                        )
                        DropdownMenu(expanded = rollExpanded, onDismissRequest = { rollExpanded = false }) {
                            rolls.forEach { r ->
                                DropdownMenuItem(
                                    text = { Text("${r.rollNumber} - ${r.fabricName} • موجودی: ${r.remainingQuantity} کیلوگرم") },
                                    onClick = {
                                        selectedRoll = r
                                        colorName = r.colorName
                                        rollExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = batchNumber,
                        onValueChange = { batchNumber = it },
                        label = { Text("شماره دوره برش") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Add Cut Item form
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("افزودن مدل و سایز برش خورده:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)

                            // Product & Model picker
                            var pExpanded by remember { mutableStateOf(false) }
                            Box {
                                OutlinedTextField(
                                    value = selectedProduct?.name ?: "انتخاب نوع محصول",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("نوع محصول") },
                                    trailingIcon = { IconButton(onClick = { pExpanded = true }) { Icon(Icons.Default.KeyboardArrowDown, contentDescription = null) } },
                                    modifier = Modifier.fillMaxWidth().clickable { pExpanded = true }
                                )
                                DropdownMenu(expanded = pExpanded, onDismissRequest = { pExpanded = false }) {
                                    products.forEach { p ->
                                        DropdownMenuItem(
                                            text = { Text(p.name) },
                                            onClick = { selectedProduct = p; pExpanded = false }
                                        )
                                    }
                                }
                            }

                            var mExpanded by remember { mutableStateOf(false) }
                            Box {
                                OutlinedTextField(
                                    value = selectedModel?.modelName ?: "انتخاب مدل (۶ جیب، نواردار، ...)",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("مدل الگو") },
                                    trailingIcon = { IconButton(onClick = { mExpanded = true }) { Icon(Icons.Default.KeyboardArrowDown, contentDescription = null) } },
                                    modifier = Modifier.fillMaxWidth().clickable { mExpanded = true }
                                )
                                DropdownMenu(expanded = mExpanded, onDismissRequest = { mExpanded = false }) {
                                    models.forEach { m ->
                                        DropdownMenuItem(
                                            text = { Text("${m.modelName} (${m.bomNotes})") },
                                            onClick = { selectedModel = m; mExpanded = false }
                                        )
                                    }
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = sizeName,
                                    onValueChange = { sizeName = it },
                                    label = { Text("سایز") },
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = colorName,
                                    onValueChange = { colorName = it },
                                    label = { Text("رنگ") },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = quantityText,
                                    onValueChange = { quantityText = it },
                                    label = { Text("تعداد برش (عدد)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = fabricKgText,
                                    onValueChange = { fabricKgText = it },
                                    label = { Text("مصرف پارچه (KG)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Button(
                                onClick = {
                                    val p = selectedProduct
                                    val m = selectedModel
                                    val q = quantityText.toIntOrNull() ?: 0
                                    val kg = fabricKgText.toDoubleOrNull() ?: 0.0
                                    val acc = accCostText.toDoubleOrNull() ?: 0.0
                                    val prn = printCostText.toDoubleOrNull() ?: 0.0

                                    if (p != null && m != null && q > 0 && kg > 0) {
                                        val roll = selectedRoll
                                        val totalUsedSoFar = specsList.sumOf { it.fabricConsumedKg } + kg
                                        if (roll != null && totalUsedSoFar > roll.remainingQuantity) {
                                            errorMessage = "مجموع مصرف درخواستی (${totalUsedSoFar} KG) بیشتر از موجودی طاقه (${roll.remainingQuantity} KG) است!"
                                        } else {
                                            specsList = specsList + WorkshopRepository.CutSpecification(
                                                productId = p.id,
                                                productName = p.name,
                                                productModelId = m.id,
                                                modelName = m.modelName,
                                                colorName = colorName,
                                                sizeName = sizeName,
                                                quantityCut = q,
                                                fabricConsumedKg = kg,
                                                accessoriesCostPerPiece = acc,
                                                printingCostPerPiece = prn
                                            )
                                            errorMessage = null
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("افزودن به اقلام برش این طاقه")
                            }
                        }
                    }
                }

                // Specs breakdown
                items(specsList) { spec ->
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
                                Text("${spec.productName} (${spec.modelName} - ${spec.sizeName})", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                                Text("${spec.quantityCut} عدد • ${spec.fabricConsumedKg} کیلوگرم پارچه", style = MaterialTheme.typography.labelSmall)
                            }
                            IconButton(onClick = { specsList = specsList - spec }) {
                                Icon(Icons.Default.Delete, contentDescription = "حذف", tint = ErrorRed)
                            }
                        }
                    }
                }

                item {
                    val totalKg = specsList.sumOf { it.fabricConsumedKg }
                    val totalPcs = specsList.sumOf { it.quantityCut }
                    Text(
                        "مجموع: $totalPcs قطعه • $totalKg کیلوگرم پارچه",
                        fontWeight = FontWeight.Bold,
                        color = TealSecondary
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
            Button(onClick = {
                val r = selectedRoll
                if (r == null) {
                    errorMessage = "طاقه پارچه را انتخاب کنید"
                    return@Button
                }
                if (specsList.isEmpty()) {
                    errorMessage = "حداقل یک ردیف برش اضافه کنید"
                    return@Button
                }
                onConfirm(r.id, batchNumber, specsList, notes)
            }) {
                Text("تأیید و اجرای برش")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("انصراف") }
        }
    )
}

// Dialog: Direct Finished Good stock entry
@Composable
fun AddFinishedGoodDialog(
    products: List<ProductEntity>,
    models: List<ProductModelEntity>,
    onDismiss: () -> Unit,
    onConfirm: (FinishedGoodEntity) -> Unit
) {
    var selectedProduct by remember { mutableStateOf(products.firstOrNull()) }
    var selectedModel by remember { mutableStateOf(models.firstOrNull()) }
    var colorName by remember { mutableStateOf("مشکی") }
    var sizeName by remember { mutableStateOf("L") }
    var qtyText by remember { mutableStateOf("100") }
    var costText by remember { mutableStateOf("210000") }
    var priceText by remember { mutableStateOf("320000") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("ثبت مستقیم موجودی انبار محصول", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Product & Model selectors
                var pExpanded by remember { mutableStateOf(false) }
                Box {
                    OutlinedTextField(
                        value = selectedProduct?.name ?: "محصول",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("نوع محصول") },
                        trailingIcon = { IconButton(onClick = { pExpanded = true }) { Icon(Icons.Default.KeyboardArrowDown, contentDescription = null) } },
                        modifier = Modifier.fillMaxWidth().clickable { pExpanded = true }
                    )
                    DropdownMenu(expanded = pExpanded, onDismissRequest = { pExpanded = false }) {
                        products.forEach { p ->
                            DropdownMenuItem(text = { Text(p.name) }, onClick = { selectedProduct = p; pExpanded = false })
                        }
                    }
                }

                var mExpanded by remember { mutableStateOf(false) }
                Box {
                    OutlinedTextField(
                        value = selectedModel?.modelName ?: "مدل",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("مدل") },
                        trailingIcon = { IconButton(onClick = { mExpanded = true }) { Icon(Icons.Default.KeyboardArrowDown, contentDescription = null) } },
                        modifier = Modifier.fillMaxWidth().clickable { mExpanded = true }
                    )
                    DropdownMenu(expanded = mExpanded, onDismissRequest = { mExpanded = false }) {
                        models.forEach { m ->
                            DropdownMenuItem(text = { Text(m.modelName) }, onClick = { selectedModel = m; mExpanded = false })
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = colorName, onValueChange = { colorName = it }, label = { Text("رنگ") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = sizeName, onValueChange = { sizeName = it }, label = { Text("سایز") }, modifier = Modifier.weight(1f))
                }

                OutlinedTextField(
                    value = qtyText,
                    onValueChange = { qtyText = it },
                    label = { Text("تعداد موجودی (عدد)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = costText,
                    onValueChange = { costText = it },
                    label = { Text("بهای تمام‌شده هر عدد (تومان)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text("قیمت فروش عمده (تومان)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val p = selectedProduct ?: return@Button
                val m = selectedModel ?: return@Button
                val q = qtyText.toIntOrNull() ?: 0
                val c = costText.toDoubleOrNull() ?: 0.0
                val pr = priceText.toDoubleOrNull() ?: 0.0

                onConfirm(
                    FinishedGoodEntity(
                        productId = p.id,
                        productName = p.name,
                        productModelId = m.id,
                        modelName = m.modelName,
                        colorName = colorName,
                        sizeName = sizeName,
                        producedQuantity = q,
                        soldQuantity = 0,
                        reservedQuantity = 0,
                        availableQuantity = q,
                        unitCostActual = c,
                        unitCostCurrent = c,
                        wholesalePrice = pr
                    )
                )
            }) {
                Text("ثبت در انبار")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("انصراف") }
        }
    )
}
