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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.entity.AuditLogEntity
import com.example.ui.common.PersianUtils
import com.example.ui.components.EmptyStateView
import com.example.ui.components.KpiCard
import com.example.ui.components.SectionHeader
import com.example.ui.theme.AmberTertiary
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.SuccessContainer
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TealSecondary
import com.example.ui.viewmodel.WorkshopViewModel

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: WorkshopViewModel,
    initialTab: Int = 0
) {
    var selectedTab by remember { mutableIntStateOf(initialTab) }
    val tabTitles = listOf("صورت سود و زیان (P&L)", "راندمان طاقه‌ها", "بدهکاران و مطالبات", "ردپای حسابرسی")

    val kpis by viewModel.dashboardKpis.collectAsState()
    val timeFilter by viewModel.timeFilter.collectAsState()
    val rolls by viewModel.fabricRolls.collectAsState()
    val customers by viewModel.customers.collectAsState()
    val auditLogs by viewModel.auditLogs.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("reports_screen")
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

        // Time Filters for Reports
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                "TODAY" to "امروز",
                "THIS_MONTH" to "این ماه",
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

        when (selectedTab) {
            0 -> {
                // Profit and Loss Statement (صورت سود و زیان جامع با تفکیک بهای واقعی و جاری)
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "صورت سود و زیان کارگاه تولید پوشاک",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(14.dp))

                                ReportRow(title = "(+) درآمد حاصل از فروش عمده:", value = PersianUtils.formatPrice(kpis.totalSales), isBold = true)
                                ReportRow(title = "(-) بهای تمام‌شده کالای فروش‌رفته (واقعی):", value = PersianUtils.formatPrice(kpis.totalCogs), valueColor = ErrorRed)

                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                                ReportRow(title = "(=) سود ناخالص واقعی:", value = PersianUtils.formatPrice(kpis.grossProfit), valueColor = SuccessGreen, isBold = true)
                                ReportRow(title = "(-) هزینه‌های جاری و عملیاتی کارگاه:", value = PersianUtils.formatPrice(kpis.totalExpenses), valueColor = ErrorRed)

                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                                ReportRow(
                                    title = "(=) سود خالص نهایی کارگاه:",
                                    value = PersianUtils.formatPrice(kpis.netProfit),
                                    valueColor = if (kpis.netProfit >= 0) SuccessGreen else ErrorRed,
                                    isBold = true
                                )

                                val margin = if (kpis.totalSales > 0) (kpis.netProfit / kpis.totalSales) * 100 else 0.0
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "حاشیه سود خالص: ${PersianUtils.formatNumber(margin)}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    item {
                        // Real vs Replacement Cost Analysis
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "تحلیل بهای تمام‌شده واقعی در برابر بهای روز (جایگزینی)",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "سیستم بهای تمام‌شده واقعی (Actual Cost) را بر مبنای قیمت خرید همان طاقه + کرایه دقیق باربری محاسبه می‌کند، در حالی که سود مبتنی بر بهای روز (Current/Replacement Cost) نشان می‌دهد در صورت جایگزینی مواد با نرخ امروز بازار چه مقدار سود باقی می‌ماند.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(32.dp)) }
                }
            }

            1 -> {
                // Fabric Rolls Efficiency
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        SectionHeader(
                            title = "گزارش وضعیت و راندمان مصرف طاقه‌ها",
                            subtitle = "کنترل دقیق وزن اولیه، مصرف‌شده و باقیمانده هر طاقه"
                        )
                    }

                    items(rolls, key = { it.id }) { roll ->
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
                                    Text("${roll.rollNumber} - ${roll.fabricName}", fontWeight = FontWeight.Bold)
                                    Text(roll.status, color = if (roll.status == "تمام‌شده") ErrorRed else SuccessGreen, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("وزن اولیه: ${PersianUtils.formatNumber(roll.purchaseWeightKg)} KG", style = MaterialTheme.typography.bodySmall)
                                    Text("مصرف: ${PersianUtils.formatNumber(roll.consumedQuantity)} KG", style = MaterialTheme.typography.bodySmall)
                                    Text("مانده: ${PersianUtils.formatNumber(roll.remainingQuantity)} KG", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(32.dp)) }
                }
            }

            2 -> {
                // Customer Receivables (بدهکاران)
                val debtors = customers.filter { it.balance > 0 }.sortedByDescending { it.balance }
                if (debtors.isEmpty()) {
                    EmptyStateView(
                        message = "تمامی مشتریان تسویه هستند",
                        subMessage = "هیچ مانده بدهی معوقی در سیستم ثبت نشده است"
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            SectionHeader(
                                title = "لیست بدهکاران کارگاه",
                                subtitle = "مشتریان دارای مانده حساب بدهکاری"
                            )
                        }
                        items(debtors, key = { it.id }) { cust ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(cust.name + if (cust.storeName.isNotBlank()) " (${cust.storeName})" else "", fontWeight = FontWeight.Bold)
                                        Text("شهر: ${cust.city} • تلفن: ${cust.phone}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Text(
                                        text = PersianUtils.formatPrice(cust.balance),
                                        fontWeight = FontWeight.Bold,
                                        color = ErrorRed
                                    )
                                }
                            }
                        }
                        item { Spacer(modifier = Modifier.height(32.dp)) }
                    }
                }
            }

            3 -> {
                // Audit Trail
                if (auditLogs.isEmpty()) {
                    EmptyStateView(
                        message = "لاگی ثبت نشده است",
                        subMessage = "تمامی فعالیت‌های کاربران و تراکنش‌ها در اینجا ردیابی می‌شوند",
                        icon = Icons.Default.History
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(auditLogs, key = { it.id }) { log ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("${log.actionType}: ${log.entityName}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                                        Text(PersianUtils.formatDate(log.timestamp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        if (log.reason.isNotBlank()) "علت: ${log.reason} • مقدار: ${log.newValue}" else "ثبت تغییر: ${log.newValue}",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                        item { Spacer(modifier = Modifier.height(32.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
fun ReportRow(
    title: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    isBold: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = if (isBold) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodySmall,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = value,
            style = if (isBold) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodySmall,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = valueColor
        )
    }
}
