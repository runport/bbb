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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.common.PersianUtils
import com.example.ui.components.KpiCard
import com.example.ui.components.SectionHeader
import com.example.ui.theme.AmberTertiary
import com.example.ui.theme.ErrorContainer
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.InfoBlue
import com.example.ui.theme.SuccessContainer
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TealSecondary
import com.example.ui.viewmodel.WorkshopViewModel

@Composable
fun DashboardScreen(
    viewModel: WorkshopViewModel,
    onNavigateToSales: () -> Unit,
    onNavigateToProduction: () -> Unit,
    onNavigateToInventory: () -> Unit,
    onOpenNewSaleDialog: () -> Unit,
    onOpenNewCutDialog: () -> Unit,
    onOpenNewRollDialog: () -> Unit,
    onOpenNewExpenseDialog: () -> Unit
) {
    val kpis by viewModel.dashboardKpis.collectAsState()
    val timeFilter by viewModel.timeFilter.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val rolls by viewModel.fabricRolls.collectAsState()
    val materials by viewModel.materials.collectAsState()

    val lowStockRolls = rolls.filter { it.status != "تمام‌شده" && it.remainingQuantity in 0.01..15.0 }
    val lowStockMaterials = materials.filter { it.stockQuantity <= it.minStockQuantity }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_screen")
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header Banner
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = settings?.workshopName ?: "کارگاه تولید پوشاک",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "مدیر کارگاه: ${settings?.managerName ?: "مدیریت"} • ${PersianUtils.formatDateOnly(System.currentTimeMillis())}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PointOfSale,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Time Filters Chips
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "TODAY" to "امروز (۲۴ ساعت)",
                            "THIS_MONTH" to "این ماه (۳۰ روز)",
                            "ALL" to "کل دوره"
                        ).forEach { (key, label) ->
                            FilterChip(
                                selected = timeFilter == key,
                                onClick = { viewModel.setTimeFilter(key) },
                                label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }
                }
            }
        }

        // 2. Quick Action Shortcuts
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "دسترسی سریع به عملیات کارگاه",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onOpenNewSaleDialog,
                            modifier = Modifier.weight(1f).testTag("quick_action_new_sale"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("فروش جدید", style = MaterialTheme.typography.labelSmall)
                        }

                        Button(
                            onClick = onOpenNewCutDialog,
                            modifier = Modifier.weight(1f).testTag("quick_action_new_cut"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TealSecondary)
                        ) {
                            Icon(Icons.Default.ContentCut, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("برش طاقه", style = MaterialTheme.typography.labelSmall)
                        }

                        Button(
                            onClick = onOpenNewRollDialog,
                            modifier = Modifier.weight(1f).testTag("quick_action_new_roll"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AmberTertiary)
                        ) {
                            Icon(Icons.Default.Inventory, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("ورود طاقه", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }

        // 3. Low Stock Warnings (Alert Banner)
        if (lowStockRolls.isNotEmpty() || lowStockMaterials.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ErrorContainer)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = ErrorRed)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "هشدار موجودی رو به اتمام!",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = ErrorRed
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        lowStockRolls.take(2).forEach { roll ->
                            Text(
                                text = "• طاقه ${roll.rollNumber} (${roll.fabricName}): فقط ${roll.remainingQuantity} کیلوگرم باقی مانده",
                                style = MaterialTheme.typography.bodySmall,
                                color = ErrorRed
                            )
                        }
                        lowStockMaterials.take(2).forEach { mat ->
                            Text(
                                text = "• ملزومات ${mat.name}: موجودی ${mat.stockQuantity} ${mat.unit} (حداقل مجاز: ${mat.minStockQuantity})",
                                style = MaterialTheme.typography.bodySmall,
                                color = ErrorRed
                            )
                        }
                    }
                }
            }
        }

        // 4. Financial Performance KPIs
        item {
            SectionHeader(
                title = "عملکرد مالی و سودآوری",
                subtitle = "محاسبه دقیق بر مبنای بهای تمام‌شده واقعی و هزینه‌ها",
                actionText = "مشاهده فاکتورها",
                onActionClick = onNavigateToSales
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                KpiCard(
                    title = "فروش کل عمده",
                    value = PersianUtils.formatPrice(kpis.totalSales),
                    subtitle = "${kpis.salesCount} فقره فاکتور نهایی",
                    icon = Icons.Default.PointOfSale,
                    accentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )

                KpiCard(
                    title = "سود خالص نهایی",
                    value = PersianUtils.formatPrice(kpis.netProfit),
                    subtitle = "پس از کسر هزینه جاری",
                    icon = Icons.Default.TrendingUp,
                    accentColor = SuccessGreen,
                    containerColor = SuccessContainer.copy(alpha = 0.6f),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                KpiCard(
                    title = "دریافتی نقد و حواله",
                    value = PersianUtils.formatPrice(kpis.totalReceived),
                    subtitle = "تسویه شده در حساب",
                    icon = Icons.Default.CheckCircle,
                    accentColor = SuccessGreen,
                    modifier = Modifier.weight(1f)
                )

                KpiCard(
                    title = "مانده بدهی مشتریان",
                    value = PersianUtils.formatPrice(kpis.customerReceivables),
                    subtitle = "مطالبات بازار و فروشگاه‌ها",
                    icon = Icons.Default.AccountBalanceWallet,
                    accentColor = AmberTertiary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                KpiCard(
                    title = "بهای تمام‌شده (COGS)",
                    value = PersianUtils.formatPrice(kpis.totalCogs),
                    subtitle = "هزینه پارچه + خرج‌کار + دوخت",
                    icon = Icons.Default.Receipt,
                    accentColor = InfoBlue,
                    modifier = Modifier.weight(1f)
                )

                KpiCard(
                    title = "هزینه‌های جاری کارگاه",
                    value = PersianUtils.formatPrice(kpis.totalExpenses),
                    subtitle = "اجاره، برق، تعمیرات و ...",
                    icon = Icons.Default.AttachMoney,
                    accentColor = ErrorRed,
                    modifier = Modifier.weight(1f),
                    onClick = onOpenNewExpenseDialog
                )
            }
        }

        // 5. Production & Inventory Real-Time Status
        item {
            SectionHeader(
                title = "وضعیت انبار پارچه و خط تولید",
                subtitle = "کنترل موجودی فیزیکی و رزرو سفارشات",
                actionText = "مدیریت انبار",
                onActionClick = onNavigateToInventory
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                KpiCard(
                    title = "پارچه موجود در انبار",
                    value = "${PersianUtils.formatNumber(kpis.totalFabricRemainingKg)} کیلو",
                    subtitle = "${kpis.activeRollsCount} طاقه فعال و نیمه‌مصرف",
                    icon = Icons.Default.Inventory,
                    accentColor = TealSecondary,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToInventory
                )

                KpiCard(
                    title = "کالای آماده قابل فروش",
                    value = "${kpis.availablePieces} عدد",
                    subtitle = "آماده تحویل در انبار",
                    icon = Icons.Default.CheckCircle,
                    accentColor = SuccessGreen,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToProduction
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                KpiCard(
                    title = "رزرو در سفارشات",
                    value = "${kpis.reservedPieces} عدد",
                    subtitle = "تخصیص‌یافته به پیش‌فاکتور",
                    icon = Icons.Default.LocalShipping,
                    accentColor = AmberTertiary,
                    modifier = Modifier.weight(1f)
                )

                KpiCard(
                    title = "کل برش‌های انجام‌شده",
                    value = "${kpis.cutsCount} دوره",
                    subtitle = "مصرف ${PersianUtils.formatNumber(kpis.totalFabricConsumedKg)} KG",
                    icon = Icons.Default.ContentCut,
                    accentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToProduction
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
