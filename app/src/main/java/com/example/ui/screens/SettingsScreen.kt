package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
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
import com.example.data.entity.ProductEntity
import com.example.data.entity.ProductModelEntity
import com.example.data.entity.SettingsEntity
import com.example.data.entity.SupplierEntity
import com.example.ui.components.SectionHeader
import com.example.ui.viewmodel.WorkshopViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: WorkshopViewModel,
    initialTab: Int = 0
) {
    var selectedTab by remember { mutableIntStateOf(initialTab) }
    val tabTitles = listOf("مشخصات کارگاه", "کاتالوگ مدل‌ها", "تأمین‌کنندگان و باربری")

    val settings by viewModel.settings.collectAsState()
    val products by viewModel.products.collectAsState()
    val models by viewModel.models.collectAsState()
    val suppliers by viewModel.suppliers.collectAsState()
    val carriers by viewModel.carriers.collectAsState()

    var showAddProductDialog by remember { mutableStateOf(false) }
    var showAddModelDialog by remember { mutableStateOf(false) }
    var showAddSupplierDialog by remember { mutableStateOf(false) }
    var showAddCarrierDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("settings_screen")
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
                // Workshop Profile form
                var workshopName by remember(settings) { mutableStateOf(settings?.workshopName ?: "") }
                var managerName by remember(settings) { mutableStateOf(settings?.managerName ?: "") }
                var phone by remember(settings) { mutableStateOf(settings?.phone ?: "") }
                var address by remember(settings) { mutableStateOf(settings?.address ?: "") }
                var sewingCostText by remember(settings) { mutableStateOf(settings?.defaultSewingCost?.toLong()?.toString() ?: "45000") }
                var overheadCostText by remember(settings) { mutableStateOf(settings?.defaultOverheadCost?.toLong()?.toString() ?: "15000") }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("اطلاعات عمومی کارگاه", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

                                OutlinedTextField(value = workshopName, onValueChange = { workshopName = it }, label = { Text("نام کارگاه تولیدی") }, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = managerName, onValueChange = { managerName = it }, label = { Text("نام مدیر / مسئول") }, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("تلفن کارگاه") }, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("آدرس کارگاه") }, modifier = Modifier.fillMaxWidth())
                            }
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("تنظیمات هزینه‌های تولید و بهای تمام‌شده", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

                                OutlinedTextField(
                                    value = sewingCostText,
                                    onValueChange = { sewingCostText = it },
                                    label = { Text("دستمزد پیش‌فرض دوخت هر عدد (تومان)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = overheadCostText,
                                    onValueChange = { overheadCostText = it },
                                    label = { Text("سربار پیش‌فرض هر عدد (تومان)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    item {
                        Button(
                            onClick = {
                                val sCost = sewingCostText.toDoubleOrNull() ?: 45000.0
                                val oCost = overheadCostText.toDoubleOrNull() ?: 15000.0
                                val current = settings ?: SettingsEntity()
                                viewModel.updateSettings(
                                    current.copy(
                                        workshopName = workshopName,
                                        managerName = managerName,
                                        phone = phone,
                                        address = address,
                                        defaultSewingCost = sCost,
                                        defaultOverheadCost = oCost
                                    )
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null)
                            Spacer(modifier = Modifier.padding(4.dp))
                            Text("ذخیره تنظیمات کارگاه")
                        }
                    }

                    item { Spacer(modifier = Modifier.height(32.dp)) }
                }
            }

            1 -> {
                // Models & Products
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        SectionHeader(
                            title = "انواع پوشاک کارگاه",
                            subtitle = "گروه‌های اصلی تولیدی کارگاه",
                            actionText = "افزودن نوع جدید",
                            onActionClick = { showAddProductDialog = true }
                        )
                    }

                    items(products, key = { it.id }) { product ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(product.name, fontWeight = FontWeight.Bold)
                                Text("کد: ${product.code}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        SectionHeader(
                            title = "مدل‌های الگو و برش",
                            subtitle = "مدل‌های مختلف مانند ۶ جیب، نواردار و چاپی",
                            actionText = "افزودن مدل جدید",
                            onActionClick = { showAddModelDialog = true }
                        )
                    }

                    items(models, key = { it.id }) { model ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(model.modelName, fontWeight = FontWeight.Bold)
                                if (model.bomNotes.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("خرج‌کار مورد نیاز: ${model.bomNotes}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(32.dp)) }
                }
            }

            2 -> {
                // Suppliers & Carriers
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        SectionHeader(
                            title = "تأمین‌کنندگان پارچه و خرج‌کار",
                            subtitle = "نساجی‌ها و بنکداران پارچه",
                            actionText = "افزودن تأمین‌کننده",
                            onActionClick = { showAddSupplierDialog = true }
                        )
                    }

                    items(suppliers, key = { it.id }) { supplier ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(supplier.name, fontWeight = FontWeight.Bold)
                                Text("${supplier.city} • تلفن: ${supplier.phone}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        SectionHeader(
                            title = "شرکت‌های باربری طرف حساب",
                            subtitle = "باربری‌های حمل طاقه‌ها از نساجی",
                            actionText = "افزودن باربری",
                            onActionClick = { showAddCarrierDialog = true }
                        )
                    }

                    items(carriers, key = { it.id }) { carrier ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(carrier.name, fontWeight = FontWeight.Bold)
                                Text("تلفن: ${carrier.phone} ${if (carrier.notes.isNotBlank()) "• ${carrier.notes}" else ""}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(32.dp)) }
                }
            }
        }
    }

    // Add Product Dialog
    if (showAddProductDialog) {
        var pName by remember { mutableStateOf("") }
        var pCode by remember { mutableStateOf("PRD-${(10..99).random()}") }
        AlertDialog(
            onDismissRequest = { showAddProductDialog = false },
            title = { Text("افزودن نوع پوشاک جدید", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = pName, onValueChange = { pName = it }, label = { Text("نام پوشاک (مثلاً تیشرت یقه گرد)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = pCode, onValueChange = { pCode = it }, label = { Text("کد شناسه") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (pName.isNotBlank()) {
                        viewModel.addProduct(ProductEntity(name = pName, code = pCode)) {
                            showAddProductDialog = false
                        }
                    }
                }) {
                    Text("ثبت")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddProductDialog = false }) { Text("انصراف") }
            }
        )
    }

    // Add Model Dialog
    if (showAddModelDialog) {
        var mName by remember { mutableStateOf("") }
        var bomNotes by remember { mutableStateOf("زیپ + کش ۵ سانت") }
        AlertDialog(
            onDismissRequest = { showAddModelDialog = false },
            title = { Text("افزودن مدل جدید", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = mName, onValueChange = { mName = it }, label = { Text("نام مدل الگو (مثلاً ۶ جیب کتان)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = bomNotes, onValueChange = { bomNotes = it }, label = { Text("خرج‌کار و نیازمندی‌های این مدل") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (mName.isNotBlank()) {
                        viewModel.addProductModel(ProductModelEntity(productId = products.firstOrNull()?.id ?: 1, modelName = mName, bomNotes = bomNotes)) {
                            showAddModelDialog = false
                        }
                    }
                }) {
                    Text("ثبت مدل")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddModelDialog = false }) { Text("انصراف") }
            }
        )
    }

    // Add Supplier Dialog
    if (showAddSupplierDialog) {
        var sName by remember { mutableStateOf("") }
        var sPhone by remember { mutableStateOf("") }
        var sCity by remember { mutableStateOf("اصفهان") }
        AlertDialog(
            onDismissRequest = { showAddSupplierDialog = false },
            title = { Text("افزودن تأمین‌کننده پارچه", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = sName, onValueChange = { sName = it }, label = { Text("نام نساجی / بافندگی") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = sCity, onValueChange = { sCity = it }, label = { Text("شهر") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = sPhone, onValueChange = { sPhone = it }, label = { Text("تلفن تماس") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (sName.isNotBlank()) {
                        viewModel.addSupplier(SupplierEntity(name = sName, phone = sPhone, city = sCity)) {
                            showAddSupplierDialog = false
                        }
                    }
                }) {
                    Text("ثبت")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddSupplierDialog = false }) { Text("انصراف") }
            }
        )
    }

    // Add Carrier Dialog
    if (showAddCarrierDialog) {
        var cName by remember { mutableStateOf("") }
        var cPhone by remember { mutableStateOf("") }
        var cNotes by remember { mutableStateOf("شعبه تهران - شوش") }
        AlertDialog(
            onDismissRequest = { showAddCarrierDialog = false },
            title = { Text("افزودن شرکت باربری", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = cName, onValueChange = { cName = it }, label = { Text("نام باربری (مثلاً پیشتاز بار)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = cNotes, onValueChange = { cNotes = it }, label = { Text("شعبه و توضیحات") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = cPhone, onValueChange = { cPhone = it }, label = { Text("تلفن") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (cName.isNotBlank()) {
                        viewModel.addCarrier(CarrierEntity(name = cName, phone = cPhone, notes = cNotes)) {
                            showAddCarrierDialog = false
                        }
                    }
                }) {
                    Text("ثبت")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCarrierDialog = false }) { Text("انصراف") }
            }
        )
    }
}
