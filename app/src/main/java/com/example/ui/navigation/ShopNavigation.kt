package com.example.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.*
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.FinancialLossRed
import com.example.ui.theme.FinancialProfitGreen
import com.example.ui.theme.PrimaryGreen
import com.example.ui.viewmodel.ShopViewModel

enum class MainTab(val title: String, val icon: ImageVector) {
    DASHBOARD("Dashboard", Icons.Default.Dashboard),
    POS("POS Sale", Icons.Default.PointOfSale),
    INVENTORY("Inventory", Icons.Default.Inventory2),
    KHATA("Udhaar Khata", Icons.Default.AccountBalance),
    MORE("More", Icons.Default.Menu)
}

enum class SubScreen {
    NONE,
    PURCHASES,
    EXPENSES,
    CASH_BANK,
    REPORTS,
    AI_ASSISTANT,
    SETTINGS,
    BARCODE_SCAN
}

@Composable
fun ShopAppNavHost(viewModel: ShopViewModel) {
    var selectedTab by remember { mutableStateOf(MainTab.DASHBOARD) }
    var currentSubScreen by remember { mutableStateOf(SubScreen.NONE) }

    // Back handling
    BackHandler(enabled = currentSubScreen != SubScreen.NONE || selectedTab != MainTab.DASHBOARD) {
        if (currentSubScreen != SubScreen.NONE) {
            currentSubScreen = SubScreen.NONE
        } else {
            selectedTab = MainTab.DASHBOARD
        }
    }

    if (currentSubScreen != SubScreen.NONE) {
        when (currentSubScreen) {
            SubScreen.PURCHASES -> PurchasesScreen(viewModel, onBack = { currentSubScreen = SubScreen.NONE })
            SubScreen.EXPENSES -> ExpensesScreen(viewModel, onBack = { currentSubScreen = SubScreen.NONE })
            SubScreen.CASH_BANK -> CashBankScreen(viewModel, onBack = { currentSubScreen = SubScreen.NONE })
            SubScreen.REPORTS -> ReportsScreen(viewModel, onBack = { currentSubScreen = SubScreen.NONE })
            SubScreen.AI_ASSISTANT -> AiAssistantScreen(viewModel, onBack = { currentSubScreen = SubScreen.NONE })
            SubScreen.SETTINGS -> SettingsScreen(viewModel, onBack = { currentSubScreen = SubScreen.NONE })
            SubScreen.BARCODE_SCAN -> BarcodeScanScreen(viewModel, onBack = { currentSubScreen = SubScreen.NONE })
            SubScreen.NONE -> {}
        }
    } else {
        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp
                ) {
                    MainTab.values().forEach { tab ->
                        NavigationBarItem(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            icon = { Icon(imageVector = tab.icon, contentDescription = tab.title) },
                            label = { Text(tab.title, fontSize = 11.sp, maxLines = 1) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = PrimaryGreen,
                                selectedTextColor = PrimaryGreen,
                                indicatorColor = PrimaryGreen.copy(alpha = 0.15f)
                            )
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (selectedTab) {
                    MainTab.DASHBOARD -> DashboardScreen(
                        viewModel = viewModel,
                        onNavigateToPos = { selectedTab = MainTab.POS },
                        onNavigateToInventory = { selectedTab = MainTab.INVENTORY },
                        onNavigateToKhata = { selectedTab = MainTab.KHATA },
                        onNavigateToPurchases = { currentSubScreen = SubScreen.PURCHASES },
                        onNavigateToExpenses = { currentSubScreen = SubScreen.EXPENSES },
                        onNavigateToReports = { currentSubScreen = SubScreen.REPORTS },
                        onNavigateToBarcodeScan = { currentSubScreen = SubScreen.BARCODE_SCAN }
                    )
                    MainTab.POS -> PosScreen(
                        viewModel = viewModel,
                        onNavigateToBarcodeScan = { currentSubScreen = SubScreen.BARCODE_SCAN }
                    )
                    MainTab.INVENTORY -> ProductsScreen(
                        viewModel = viewModel,
                        onNavigateToBarcodeScan = { currentSubScreen = SubScreen.BARCODE_SCAN }
                    )
                    MainTab.KHATA -> KhataScreen(viewModel = viewModel)
                    MainTab.MORE -> MoreMenuScreen(
                        onNavigate = { sub -> currentSubScreen = sub }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreMenuScreen(
    onNavigate: (SubScreen) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("More Modules (دیگر شعبہ جات)", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                MoreMenuItem(
                    title = "Purchases (مال کی خریداری)",
                    subtitle = "Supplier purchases & automatic stock in",
                    icon = Icons.Default.ShoppingBag,
                    iconColor = AccentPurple,
                    onClick = { onNavigate(SubScreen.PURCHASES) }
                )
            }
            item {
                MoreMenuItem(
                    title = "Shop Expenses (دکان کے اخراجات)",
                    subtitle = "Rent, electricity, salaries, tea & maintenance",
                    icon = Icons.Default.MoneyOff,
                    iconColor = FinancialLossRed,
                    onClick = { onNavigate(SubScreen.EXPENSES) }
                )
            }
            item {
                MoreMenuItem(
                    title = "Cash & Bank Accounts (بینک و کیش)",
                    subtitle = "Cash drawer, bank, JazzCash & day closing",
                    icon = Icons.Default.AccountBalance,
                    iconColor = AccentBlue,
                    onClick = { onNavigate(SubScreen.CASH_BANK) }
                )
            }
            item {
                MoreMenuItem(
                    title = "Reports & Profit / Loss (رپورٹس و نفع نقصان)",
                    subtitle = "Sales, cost of goods, net profit & stock value",
                    icon = Icons.Default.BarChart,
                    iconColor = FinancialProfitGreen,
                    onClick = { onNavigate(SubScreen.REPORTS) }
                )
            }
            item {
                MoreMenuItem(
                    title = "Shop AI Assistant (شاپ اسسٹنٹ)",
                    subtitle = "Ask questions in Urdu/English about sales & stock",
                    icon = Icons.Default.Psychology,
                    iconColor = PrimaryGreen,
                    onClick = { onNavigate(SubScreen.AI_ASSISTANT) }
                )
            }
            item {
                MoreMenuItem(
                    title = "Settings & Shop Profile (سیٹنگز)",
                    subtitle = "Name, printer width, negative stock, demo data",
                    icon = Icons.Default.Settings,
                    iconColor = Color.DarkGray,
                    onClick = { onNavigate(SubScreen.SETTINGS) }
                )
            }
        }
    }
}

@Composable
fun MoreMenuItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(iconColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(text = title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
                    Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
        }
    }
}
