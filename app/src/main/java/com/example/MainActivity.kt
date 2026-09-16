package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.data.entity.CustomerEntity
import com.example.data.entity.FabricRollEntity
import com.example.data.repository.WorkshopRepository
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.FreightAndFinanceScreen
import com.example.ui.screens.InventoryScreen
import com.example.ui.screens.NewCutDialog
import com.example.ui.screens.NewExpenseDialog
import com.example.ui.screens.NewFabricRollDialog
import com.example.ui.screens.NewSaleDialog
import com.example.ui.screens.ProductionScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SalesScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.WorkshopViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: WorkshopViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    WorkshopApp(viewModel = viewModel)
                }
            }
        }
    }
}

enum class NavDestination(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    DASHBOARD("داشبورد", Icons.Default.Dashboard),
    SALES("فروش عمده", Icons.Default.PointOfSale),
    PRODUCTION("تولید و برش", Icons.Default.PrecisionManufacturing),
    INVENTORY("انبار پارچه", Icons.Default.Inventory2),
    FINANCE("هزینه و باربری", Icons.Default.AttachMoney),
    REPORTS("سود و گزارشات", Icons.Default.Assessment),
    SETTINGS("تنظیمات", Icons.Default.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkshopApp(viewModel: WorkshopViewModel) {
    var currentNav by remember { mutableStateOf(NavDestination.DASHBOARD) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val settings by viewModel.settings.collectAsState()
    val customers by viewModel.customers.collectAsState()
    val availableGoods by viewModel.availableFinishedGoods.collectAsState()
    val availableRolls by viewModel.availableRolls.collectAsState()
    val products by viewModel.products.collectAsState()
    val models by viewModel.models.collectAsState()
    val fabricTypes by viewModel.fabricTypes.collectAsState()
    val suppliers by viewModel.suppliers.collectAsState()

    // Global quick action dialogs (invocable from Dashboard)
    var showQuickSaleDialog by remember { mutableStateOf(false) }
    var showQuickCutDialog by remember { mutableStateOf(false) }
    var showQuickRollDialog by remember { mutableStateOf(false) }
    var showQuickExpenseDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.userMessage.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 24.dp)
                ) {
                    Text(
                        text = settings?.workshopName ?: "سیستم مدیریت کارگاه پوشاک",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "مدیریت: ${settings?.managerName ?: "کارگاه"} • نسخه ۱.۰.۰",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                Spacer(modifier = Modifier.height(12.dp))

                NavDestination.values().forEach { destination ->
                    NavigationDrawerItem(
                        icon = { Icon(destination.icon, contentDescription = null) },
                        label = { Text(destination.title, fontWeight = FontWeight.Medium) },
                        selected = currentNav == destination,
                        onClick = {
                            currentNav = destination
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = currentNav.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = settings?.workshopName ?: "کارگاه پوشاک",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } },
                            modifier = Modifier.testTag("menu_drawer_button")
                        ) {
                            Icon(Icons.Default.Menu, contentDescription = "منو")
                        }
                    },
                    actions = {
                        IconButton(onClick = { currentNav = NavDestination.SETTINGS }) {
                            Icon(Icons.Default.Settings, contentDescription = "تنظیمات")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            bottomBar = {
                // Bottom bar for the 5 most frequent tabs
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    listOf(
                        NavDestination.DASHBOARD,
                        NavDestination.SALES,
                        NavDestination.PRODUCTION,
                        NavDestination.INVENTORY,
                        NavDestination.FINANCE
                    ).forEach { dest ->
                        NavigationBarItem(
                            selected = currentNav == dest,
                            onClick = { currentNav = dest },
                            icon = { Icon(dest.icon, contentDescription = dest.title) },
                            label = { Text(dest.title, style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.testTag("nav_item_${dest.name}")
                        )
                    }
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (currentNav) {
                    NavDestination.DASHBOARD -> {
                        DashboardScreen(
                            viewModel = viewModel,
                            onNavigateToSales = { currentNav = NavDestination.SALES },
                            onNavigateToProduction = { currentNav = NavDestination.PRODUCTION },
                            onNavigateToInventory = { currentNav = NavDestination.INVENTORY },
                            onOpenNewSaleDialog = { showQuickSaleDialog = true },
                            onOpenNewCutDialog = { showQuickCutDialog = true },
                            onOpenNewRollDialog = { showQuickRollDialog = true },
                            onOpenNewExpenseDialog = { showQuickExpenseDialog = true }
                        )
                    }
                    NavDestination.SALES -> {
                        SalesScreen(viewModel = viewModel)
                    }
                    NavDestination.PRODUCTION -> {
                        ProductionScreen(viewModel = viewModel)
                    }
                    NavDestination.INVENTORY -> {
                        InventoryScreen(viewModel = viewModel)
                    }
                    NavDestination.FINANCE -> {
                        FreightAndFinanceScreen(viewModel = viewModel)
                    }
                    NavDestination.REPORTS -> {
                        ReportsScreen(viewModel = viewModel)
                    }
                    NavDestination.SETTINGS -> {
                        SettingsScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }

    // Quick Action Dialogs accessible anywhere
    if (showQuickSaleDialog) {
        NewSaleDialog(
            customers = customers,
            availableGoods = availableGoods,
            onDismiss = { showQuickSaleDialog = false },
            onConfirm = { param ->
                viewModel.finalizeSale(param) {
                    showQuickSaleDialog = false
                }
            }
        )
    }

    if (showQuickCutDialog) {
        NewCutDialog(
            rolls = availableRolls,
            products = products,
            models = models,
            onDismiss = { showQuickCutDialog = false },
            onConfirm = { rollId, batchNumber, specs, notes ->
                viewModel.executeCutting(rollId, batchNumber, specs, notes) {
                    showQuickCutDialog = false
                }
            }
        )
    }

    if (showQuickRollDialog) {
        NewFabricRollDialog(
            fabricTypes = fabricTypes,
            suppliers = suppliers,
            onDismiss = { showQuickRollDialog = false },
            onConfirm = { roll ->
                viewModel.addFabricRoll(roll) {
                    showQuickRollDialog = false
                }
            }
        )
    }

    if (showQuickExpenseDialog) {
        NewExpenseDialog(
            onDismiss = { showQuickExpenseDialog = false },
            onConfirm = { cat, title, amt, method, notes ->
                viewModel.recordExpense(cat, title, amt, method, notes) {
                    showQuickExpenseDialog = false
                }
            }
        )
    }
}
