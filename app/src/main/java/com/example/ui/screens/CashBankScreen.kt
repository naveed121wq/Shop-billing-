package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.FinancialLossRed
import com.example.ui.theme.FinancialProfitGreen
import com.example.ui.theme.PrimaryGreen
import com.example.ui.viewmodel.ShopViewModel
import com.example.util.CurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashBankScreen(
    viewModel: ShopViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val accounts by viewModel.allAccounts.collectAsStateWithLifecycle()
    val accountTx by viewModel.allAccountTransactions.collectAsStateWithLifecycle()

    var showTransferDialog by remember { mutableStateOf(false) }
    var showCashClosingDialog by remember { mutableStateOf(false) }

    val totalLiquidFunds = accounts.sumOf { it.balance }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cash & Bank Accounts (بینک و کیش کھاتہ)", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Total Liquid Funds Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = PrimaryGreen.copy(alpha = 0.12f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Total Available Funds (کل رقم)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = PrimaryGreen
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = CurrencyFormatter.formatPkr(totalLiquidFunds, settings.currencySymbol),
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = PrimaryGreen
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { showTransferDialog = true },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                            ) {
                                Icon(imageVector = Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Transfer Money")
                            }

                            OutlinedButton(
                                onClick = { showCashClosingDialog = true },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(imageVector = Icons.Default.CheckCircleOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Cash Closing")
                            }
                        }
                    }
                }
            }

            // Account Cards Header
            item {
                Text(
                    text = "Payment Accounts (کھاتے)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            // Account Cards
            items(accounts) { acc ->
                val icon = when {
                    acc.name.contains("Cash", ignoreCase = true) -> Icons.Default.AccountBalanceWallet
                    acc.name.contains("Bank", ignoreCase = true) -> Icons.Default.AccountBalance
                    else -> Icons.Default.PhoneAndroid
                }
                val iconColor = when {
                    acc.name.contains("Cash", ignoreCase = true) -> AccentAmber
                    acc.name.contains("Bank", ignoreCase = true) -> AccentBlue
                    else -> PrimaryGreen
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(acc.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
                                if (acc.accountNumber.isNotBlank()) {
                                    Text(acc.accountNumber, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }

                        Text(
                            text = CurrencyFormatter.formatPkr(acc.balance, settings.currencySymbol),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (acc.balance >= 0) FinancialProfitGreen else FinancialLossRed
                        )
                    }
                }
            }

            // Recent Account Transactions Header
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Recent Account Transactions",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            if (accountTx.isEmpty()) {
                item {
                    Text("No transactions logged yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                items(accountTx.take(15)) { tx ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = "${tx.transactionType} • ${tx.accountName}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text(text = "${CurrencyFormatter.formatDate(tx.createdAt)} • ${tx.reference}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                if (tx.notes.isNotBlank()) {
                                    Text(text = tx.notes, fontSize = 11.sp)
                                }
                            }

                            Text(
                                text = (if (tx.amount > 0) "+" else "") + CurrencyFormatter.formatPkr(tx.amount, settings.currencySymbol),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (tx.amount >= 0) FinancialProfitGreen else FinancialLossRed
                                )
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Transfer Dialog
    if (showTransferDialog) {
        var fromAccount by remember { mutableStateOf(accounts.firstOrNull()) }
        var toAccount by remember { mutableStateOf(accounts.getOrNull(1) ?: accounts.firstOrNull()) }
        var amountStr by remember { mutableStateOf("") }
        var notes by remember { mutableStateOf("Internal transfer") }

        AlertDialog(
            onDismissRequest = { showTransferDialog = false },
            title = { Text("Transfer Between Accounts") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("From Account:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    accounts.forEach { acc ->
                        FilterChip(
                            selected = fromAccount?.id == acc.id,
                            onClick = { fromAccount = acc },
                            label = { Text("${acc.name} (${CurrencyFormatter.formatPkr(acc.balance, settings.currencySymbol)})", fontSize = 11.sp) }
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("To Account:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    accounts.forEach { acc ->
                        FilterChip(
                            selected = toAccount?.id == acc.id,
                            onClick = { toAccount = acc },
                            label = { Text("${acc.name} (${CurrencyFormatter.formatPkr(acc.balance, settings.currencySymbol)})", fontSize = 11.sp) }
                        )
                    }

                    OutlinedTextField(
                        value = amountStr,
                        onValueChange = { amountStr = it },
                        label = { Text("Amount *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = amountStr.toDoubleOrNull()
                        if (amount == null || amount <= 0) {
                            Toast.makeText(context, "Enter a valid amount", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (fromAccount == null || toAccount == null || fromAccount!!.id == toAccount!!.id) {
                            Toast.makeText(context, "Select two different accounts", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        viewModel.transferAccount(fromAccount!!.id, toAccount!!.id, amount, notes) {
                            Toast.makeText(context, "Transfer completed!", Toast.LENGTH_SHORT).show()
                            showTransferDialog = false
                        }
                    }
                ) {
                    Text("Transfer Now")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTransferDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Day Cash Closing Dialog
    if (showCashClosingDialog) {
        val cashAccount = accounts.find { it.name.contains("Cash", ignoreCase = true) }
        val expectedCash = cashAccount?.balance ?: 0.0
        var actualCashStr by remember { mutableStateOf("${expectedCash.toInt()}") }

        val actualCash = actualCashStr.toDoubleOrNull() ?: expectedCash
        val diff = actualCash - expectedCash

        AlertDialog(
            onDismissRequest = { showCashClosingDialog = false },
            title = { Text("Daily Cash Closing (شام کا کیش حساب)") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Calculated System Cash: ${CurrencyFormatter.formatPkr(expectedCash, settings.currencySymbol)}", fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = actualCashStr,
                        onValueChange = { actualCashStr = it },
                        label = { Text("Actual Cash Counted in Drawer *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (diff == 0.0) {
                        Text("Status: Exact Match! (حساب بالکل ٹھیک ہے)", color = FinancialProfitGreen, fontWeight = FontWeight.Bold)
                    } else if (diff > 0) {
                        Text("Surplus: +${CurrencyFormatter.formatPkr(diff, settings.currencySymbol)} (زیادہ کیش)", color = FinancialProfitGreen, fontWeight = FontWeight.Bold)
                    } else {
                        Text("Shortage: -${CurrencyFormatter.formatPkr(-diff, settings.currencySymbol)} (کیش کی کمی)", color = FinancialLossRed, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        Toast.makeText(context, "Day cash closing recorded!", Toast.LENGTH_SHORT).show()
                        showCashClosingDialog = false
                    }
                ) {
                    Text("Confirm Closing")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCashClosingDialog = false }) { Text("Close") }
            }
        )
    }
}
