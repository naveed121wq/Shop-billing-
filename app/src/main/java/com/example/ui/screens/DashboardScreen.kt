package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.entity.SaleEntity
import com.example.data.entity.SaleItemEntity
import com.example.ui.components.MetricCard
import com.example.ui.components.QuickActionButton
import com.example.ui.theme.*
import com.example.ui.viewmodel.ShopViewModel
import com.example.util.CurrencyFormatter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: ShopViewModel,
    onNavigateToPos: () -> Unit,
    onNavigateToInventory: () -> Unit,
    onNavigateToKhata: () -> Unit,
    onNavigateToPurchases: () -> Unit,
    onNavigateToExpenses: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToBarcodeScan: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val dashboardData by viewModel.dashboardData.collectAsStateWithLifecycle()
    val filter by viewModel.dashboardDateFilter.collectAsStateWithLifecycle()
    val recentSales by viewModel.allSales.collectAsStateWithLifecycle()
    val recentExpenses by viewModel.allExpenses.collectAsStateWithLifecycle()
    val lowStockList by viewModel.lowStockProducts.collectAsStateWithLifecycle()

    // Dialog state for viewing a recent sale receipt
    var viewingSale by remember { mutableStateOf<Pair<SaleEntity, List<SaleItemEntity>>?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = settings.shopName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = settings.shopTagline.ifBlank { "Smart POS & Accounting" },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToBarcodeScan) {
                        Icon(imageVector = Icons.Default.QrCodeScanner, contentDescription = "Scan Barcode")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Filter Chips
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val filters = listOf("Today", "Yesterday", "This Week", "This Month", "All Time")
                    filters.forEach { itemFilter ->
                        FilterChip(
                            selected = filter == itemFilter,
                            onClick = { viewModel.setDashboardDateFilter(itemFilter) },
                            label = { Text(text = itemFilter, fontSize = 12.sp) }
                        )
                    }
                }
            }

            // Low Stock Warning Banner if items are low
            if (lowStockList.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToInventory() },
                        colors = CardDefaults.cardColors(containerColor = FinancialPendingOrange.copy(alpha = 0.12f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = FinancialPendingOrange,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${lowStockList.size} Items Low or Out of Stock!",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = FinancialPendingOrange
                                )
                                Text(
                                    text = "Tap to review stock levels & reorder",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = FinancialPendingOrange
                            )
                        }
                    }
                }
            }

            // Primary Financial Metrics Grid
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Row 1: Sales & Net Profit
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricCard(
                            title = "$filter's Sales (سیل)",
                            value = CurrencyFormatter.formatPkr(dashboardData.todaySales, settings.currencySymbol),
                            subtitle = "${dashboardData.todayInvoicesCount} invoices",
                            icon = Icons.Default.PointOfSale,
                            iconTint = PrimaryGreen,
                            backgroundColor = PrimaryGreenContainer.copy(alpha = 0.35f),
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToReports
                        )
                        MetricCard(
                            title = "Net Profit (منافع)",
                            value = CurrencyFormatter.formatPkr(dashboardData.todayNetProfit, settings.currencySymbol),
                            subtitle = "Gross: ${CurrencyFormatter.formatPkr(dashboardData.todayGrossProfit, settings.currencySymbol)}",
                            icon = Icons.Default.TrendingUp,
                            iconTint = FinancialProfitGreen,
                            backgroundColor = FinancialProfitGreen.copy(alpha = 0.12f),
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToReports
                        )
                    }

                    // Row 2: Cash in Hand & Expenses
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricCard(
                            title = "Cash in Hand (کیش)",
                            value = CurrencyFormatter.formatPkr(dashboardData.cashInHand, settings.currencySymbol),
                            subtitle = "Shop cash drawer",
                            icon = Icons.Default.AccountBalanceWallet,
                            iconTint = AccentAmber,
                            backgroundColor = AccentAmber.copy(alpha = 0.12f),
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToExpenses
                        )
                        MetricCard(
                            title = "Expenses (اخراجات)",
                            value = CurrencyFormatter.formatPkr(dashboardData.todayExpenses, settings.currencySymbol),
                            subtitle = "Bills, salaries, tea",
                            icon = Icons.Default.ReceiptLong,
                            iconTint = FinancialLossRed,
                            backgroundColor = FinancialLossRed.copy(alpha = 0.12f),
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToExpenses
                        )
                    }

                    // Row 3: Udhaar Receivables & Payables
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricCard(
                            title = "Customers Udhaar (ادھار)",
                            value = CurrencyFormatter.formatPkr(dashboardData.totalReceivables, settings.currencySymbol),
                            subtitle = "Customer receivables",
                            icon = Icons.Default.People,
                            iconTint = AccentBlue,
                            backgroundColor = AccentBlue.copy(alpha = 0.12f),
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToKhata
                        )
                        MetricCard(
                            title = "Supplier Payables (واجبات)",
                            value = CurrencyFormatter.formatPkr(dashboardData.totalPayables, settings.currencySymbol),
                            subtitle = "Suppliers balance",
                            icon = Icons.Default.LocalShipping,
                            iconTint = AccentPurple,
                            backgroundColor = AccentPurple.copy(alpha = 0.12f),
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToKhata
                        )
                    }

                    // Row 4: Total Stock Valuation
                    MetricCard(
                        title = "Total Stock Valuation (اسٹاک ویلیو)",
                        value = CurrencyFormatter.formatPkr(dashboardData.stockSaleValue, settings.currencySymbol),
                        subtitle = "Cost Basis: ${CurrencyFormatter.formatPkr(dashboardData.stockPurchaseValue, settings.currencySymbol)} | Qty: ${dashboardData.totalStockQty.toInt()}",
                        icon = Icons.Default.Inventory2,
                        iconTint = PrimaryGreen,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onNavigateToInventory
                    )
                }
            }

            // Quick Actions Section
            item {
                Text(
                    text = "Quick Actions (فوری ایکشنز)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionButton(
                        title = "+ New Sale",
                        icon = Icons.Default.ShoppingCart,
                        color = PrimaryGreen,
                        onClick = onNavigateToPos,
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionButton(
                        title = "+ New Purchase",
                        icon = Icons.Default.ShoppingBag,
                        color = AccentPurple,
                        onClick = onNavigateToPurchases,
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionButton(
                        title = "Udhaar Khata",
                        icon = Icons.Default.AccountBox,
                        color = AccentBlue,
                        onClick = onNavigateToKhata,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionButton(
                        title = "+ Add Expense",
                        icon = Icons.Default.MoneyOff,
                        color = FinancialLossRed,
                        onClick = onNavigateToExpenses,
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionButton(
                        title = "Scan Barcode",
                        icon = Icons.Default.QrCodeScanner,
                        color = AccentAmber,
                        onClick = onNavigateToBarcodeScan,
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionButton(
                        title = "Reports & P&L",
                        icon = Icons.Default.BarChart,
                        color = FinancialProfitGreen,
                        onClick = onNavigateToReports,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Optional Quick Demo Data Bar
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Sample Pakistani Retail Data",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                        )
                        Button(
                            onClick = {
                                viewModel.loadDemoData {
                                    Toast.makeText(context, "Loaded demo products, customers & sales!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Load Demo Data", fontSize = 12.sp)
                        }
                    }
                }
            }

            // Recent Sales Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Sales (حالیہ بلز)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    TextButton(onClick = onNavigateToReports) {
                        Text("View All")
                    }
                }
            }

            // Recent Sales Items
            if (recentSales.isEmpty()) {
                item {
                    Text(
                        text = "No sales recorded yet. Tap '+ New Sale' to create your first bill!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(recentSales.take(5)) { sale ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                coroutineScope.launch {
                                    val items = viewModel.repository.getSaleItemsDirect(sale.id)
                                    viewingSale = Pair(sale, items)
                                }
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(14.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(PrimaryGreen.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Receipt,
                                        contentDescription = null,
                                        tint = PrimaryGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "${sale.invoiceNumber} • ${sale.customerName}",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                    Text(
                                        text = "${CurrencyFormatter.formatDate(sale.createdAt)} • ${sale.paymentMethod}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = CurrencyFormatter.formatPkr(sale.grandTotal, settings.currencySymbol),
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (sale.remainingAmount > 0) {
                                    Text(
                                        text = "Udhaar: ${CurrencyFormatter.formatPkr(sale.remainingAmount, settings.currencySymbol)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // View Sale Receipt Dialog
    viewingSale?.let { (sale, items) ->
        InvoiceDialog(
            settings = settings,
            sale = sale,
            items = items,
            onDismiss = { viewingSale = null }
        )
    }
}
