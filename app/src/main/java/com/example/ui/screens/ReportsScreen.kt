package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.FinancialLossRed
import com.example.ui.theme.FinancialProfitGreen
import com.example.ui.theme.PrimaryGreen
import com.example.ui.viewmodel.ShopViewModel
import com.example.util.CurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: ShopViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val allSales by viewModel.allSales.collectAsStateWithLifecycle()
    val allExpenses by viewModel.allExpenses.collectAsStateWithLifecycle()
    val allProducts by viewModel.allProducts.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Sales, 1: Profit & Loss, 2: Stock Valuation

    // Calculations
    val totalRevenue = allSales.sumOf { it.grandTotal }
    val totalCostOfGoods = allSales.sumOf { it.costTotal }
    val grossProfit = totalRevenue - totalCostOfGoods
    val totalExpenses = allExpenses.sumOf { it.amount }
    val netProfit = grossProfit - totalExpenses
    val profitMargin = if (totalRevenue > 0) (netProfit / totalRevenue) * 100 else 0.0

    val totalStockQty = allProducts.sumOf { it.currentStock }
    val totalStockCost = allProducts.sumOf { it.currentStock * it.purchasePrice }
    val totalStockRetail = allProducts.sumOf { it.currentStock * it.salePrice }
    val potentialStockProfit = totalStockRetail - totalStockCost

    fun exportCsvReport() {
        val sb = StringBuilder()
        sb.append("Report: Shop Hisab Kitab Financial Summary\n")
        sb.append("Shop: ${settings.shopName}\n")
        sb.append("Date: ${CurrencyFormatter.formatDate(System.currentTimeMillis())}\n\n")

        sb.append("--- PROFIT & LOSS ---\n")
        sb.append("Total Sales Revenue,${totalRevenue}\n")
        sb.append("Cost of Goods Sold (COGS),${totalCostOfGoods}\n")
        sb.append("Gross Profit,${grossProfit}\n")
        sb.append("Total Operating Expenses,${totalExpenses}\n")
        sb.append("Net Profit,${netProfit}\n")
        sb.append("Profit Margin,${String.format("%.1f", profitMargin)}%\n\n")

        sb.append("--- INVENTORY VALUATION ---\n")
        sb.append("Total Stock Units,${totalStockQty.toInt()}\n")
        sb.append("Total Stock Cost Investment,${totalStockCost}\n")
        sb.append("Total Stock Retail Value,${totalStockRetail}\n")
        sb.append("Expected Inventory Profit,${potentialStockProfit}\n")

        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, sb.toString())
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(sendIntent, "Export Report Summary"))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reports & Profit/Loss (رپورٹس و نفع نقصان)", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { exportCsvReport() }) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Share Report")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Sales") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Profit & Loss") }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Stock Value") }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            when (selectedTab) {
                0 -> {
                    // Sales Report Tab
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        item {
                            ReportMetricRow(
                                title = "Total Sales Invoices",
                                value = "${allSales.size}",
                                icon = Icons.Default.Receipt
                            )
                        }
                        item {
                            ReportMetricRow(
                                title = "Gross Sales Revenue",
                                value = CurrencyFormatter.formatPkr(totalRevenue, settings.currencySymbol),
                                icon = Icons.Default.TrendingUp,
                                color = PrimaryGreen
                            )
                        }
                        item {
                            val totalPaid = allSales.sumOf { it.paidAmount }
                            ReportMetricRow(
                                title = "Cash & Digital Received",
                                value = CurrencyFormatter.formatPkr(totalPaid, settings.currencySymbol),
                                icon = Icons.Default.Payments
                            )
                        }
                        item {
                            val totalCreditGiven = allSales.sumOf { it.remainingAmount }
                            ReportMetricRow(
                                title = "Sales on Credit (Udhaar)",
                                value = CurrencyFormatter.formatPkr(totalCreditGiven, settings.currencySymbol),
                                icon = Icons.Default.CreditCard,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        item {
                            val avgBill = if (allSales.isNotEmpty()) totalRevenue / allSales.size else 0.0
                            ReportMetricRow(
                                title = "Average Invoice Value",
                                value = CurrencyFormatter.formatPkr(avgBill, settings.currencySymbol),
                                icon = Icons.Default.Calculate
                            )
                        }
                    }
                }
                1 -> {
                    // Profit & Loss Tab
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Profit & Loss Statement (مکمل نفع و نقصان)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Spacer(modifier = Modifier.height(12.dp))

                                    FinancialStatementLine("1. Total Sales Revenue (آمدن):", CurrencyFormatter.formatPkr(totalRevenue, settings.currencySymbol), isPositive = true)
                                    FinancialStatementLine("2. Cost of Goods Sold (خریداری لاگت):", "-${CurrencyFormatter.formatPkr(totalCostOfGoods, settings.currencySymbol)}", isPositive = false)
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                    FinancialStatementLine("Gross Profit (خام منافع):", CurrencyFormatter.formatPkr(grossProfit, settings.currencySymbol), isBold = true, isPositive = grossProfit >= 0)

                                    Spacer(modifier = Modifier.height(10.dp))
                                    FinancialStatementLine("3. Operating Expenses (دکان کے اخراجات):", "-${CurrencyFormatter.formatPkr(totalExpenses, settings.currencySymbol)}", isPositive = false)
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                    FinancialStatementLine(
                                        "Net Profit (خالص منافع):",
                                        CurrencyFormatter.formatPkr(netProfit, settings.currencySymbol),
                                        isBold = true,
                                        isPositive = netProfit >= 0,
                                        fontSize = 17.sp
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Net Profit Margin: ${String.format("%.1f", profitMargin)}%",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = if (profitMargin >= 0) FinancialProfitGreen else FinancialLossRed
                                    )
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // Stock Valuation Tab
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        item {
                            ReportMetricRow(
                                title = "Total Items in Inventory",
                                value = "${allProducts.size} Products (${totalStockQty.toInt()} units)",
                                icon = Icons.Default.Inventory2
                            )
                        }
                        item {
                            ReportMetricRow(
                                title = "Total Purchase Cost Investment",
                                value = CurrencyFormatter.formatPkr(totalStockCost, settings.currencySymbol),
                                icon = Icons.Default.AccountBalanceWallet
                            )
                        }
                        item {
                            ReportMetricRow(
                                title = "Total Retail Market Value",
                                value = CurrencyFormatter.formatPkr(totalStockRetail, settings.currencySymbol),
                                icon = Icons.Default.Storefront,
                                color = PrimaryGreen
                            )
                        }
                        item {
                            ReportMetricRow(
                                title = "Potential Gross Profit",
                                value = CurrencyFormatter.formatPkr(potentialStockProfit, settings.currencySymbol),
                                icon = Icons.Default.Savings,
                                color = FinancialProfitGreen
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReportMetricRow(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color = Color.Unspecified
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                Icon(imageVector = icon, contentDescription = null, tint = if (color != Color.Unspecified) color else PrimaryGreen, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(title, style = MaterialTheme.typography.bodyMedium)
            }
            Text(
                value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = color
            )
        }
    }
}

@Composable
fun FinancialStatementLine(
    label: String,
    value: String,
    isBold: Boolean = false,
    isPositive: Boolean = true,
    fontSize: androidx.compose.ui.unit.TextUnit = 14.sp
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal, fontSize = fontSize)
        Text(
            text = value,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            fontSize = fontSize,
            color = if (isPositive) FinancialProfitGreen else FinancialLossRed
        )
    }
}
