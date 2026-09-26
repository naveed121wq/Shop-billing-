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
import com.example.data.entity.CustomerEntity
import com.example.data.entity.CustomerLedgerEntity
import com.example.data.entity.SupplierEntity
import com.example.data.entity.SupplierLedgerEntity
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.FinancialLossRed
import com.example.ui.theme.FinancialProfitGreen
import com.example.ui.theme.PrimaryGreen
import com.example.ui.viewmodel.ShopViewModel
import com.example.util.CurrencyFormatter
import com.example.util.PdfInvoiceHelper
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KhataScreen(
    viewModel: ShopViewModel
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val customers by viewModel.allCustomers.collectAsStateWithLifecycle()
    val suppliers by viewModel.allSuppliers.collectAsStateWithLifecycle()
    val totalReceivables by viewModel.totalCustomerReceivables.collectAsStateWithLifecycle()
    val totalPayables by viewModel.totalSupplierPayables.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Customers, 1: Suppliers
    var searchQuery by remember { mutableStateOf("") }
    var showAddPersonDialog by remember { mutableStateOf(false) }

    // Detail Ledger View states
    var selectedCustomerForLedger by remember { mutableStateOf<CustomerEntity?>(null) }
    var selectedSupplierForLedger by remember { mutableStateOf<SupplierEntity?>(null) }
    var showPaymentDialogForCustomer by remember { mutableStateOf<CustomerEntity?>(null) }
    var showPaymentDialogForSupplier by remember { mutableStateOf<SupplierEntity?>(null) }

    val filteredCustomers = remember(customers, searchQuery) {
        customers.filter {
            searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true) || it.phone.contains(searchQuery)
        }
    }

    val filteredSuppliers = remember(suppliers, searchQuery) {
        suppliers.filter {
            searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true) || it.phone.contains(searchQuery)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Udhaar Khata (ادھار کھاتہ)", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddPersonDialog = true },
                containerColor = PrimaryGreen,
                contentColor = Color.White
            ) {
                Icon(imageVector = Icons.Default.PersonAdd, contentDescription = "Add")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Summary Cards Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = AccentBlue.copy(alpha = 0.12f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Customer Udhaar (وصولی)", fontSize = 12.sp, color = AccentBlue)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = CurrencyFormatter.formatPkr(totalReceivables, settings.currencySymbol),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = AccentBlue
                        )
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = FinancialLossRed.copy(alpha = 0.12f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Supplier Payables (ادائیگی)", fontSize = 12.sp, color = FinancialLossRed)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = CurrencyFormatter.formatPkr(totalPayables, settings.currencySymbol),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = FinancialLossRed
                        )
                    }
                }
            }

            // Tabs Row
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Customers (${customers.size})") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Suppliers (${suppliers.size})") }
                )
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                placeholder = { Text(if (selectedTab == 0) "Search customer name or phone..." else "Search supplier...") },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(10.dp)
            )

            // Content List
            if (selectedTab == 0) {
                if (filteredCustomers.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No customers found.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(filteredCustomers) { cust ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedCustomerForLedger = cust },
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
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = cust.name,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        if (cust.phone.isNotBlank()) {
                                            Text(
                                                text = "Phone: ${cust.phone}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        if (cust.notes.isNotBlank()) {
                                            Text(
                                                text = cust.notes,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                            )
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = CurrencyFormatter.formatPkr(cust.currentBalance, settings.currencySymbol),
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = if (cust.currentBalance > 0) MaterialTheme.colorScheme.error else FinancialProfitGreen
                                            )
                                        )
                                        TextButton(
                                            onClick = { showPaymentDialogForCustomer = cust },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text("Receive Payment", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                if (filteredSuppliers.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No suppliers found.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(filteredSuppliers) { sup ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedSupplierForLedger = sup },
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
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = sup.name,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        if (sup.phone.isNotBlank()) {
                                            Text(
                                                text = "Phone: ${sup.phone}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = CurrencyFormatter.formatPkr(sup.currentBalance, settings.currencySymbol),
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = if (sup.currentBalance > 0) MaterialTheme.colorScheme.error else FinancialProfitGreen
                                            )
                                        )
                                        TextButton(
                                            onClick = { showPaymentDialogForSupplier = sup },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text("Pay Supplier", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Customer or Supplier Dialog
    if (showAddPersonDialog) {
        var name by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }
        var address by remember { mutableStateOf("") }
        var openingBal by remember { mutableStateOf("") }
        var notes by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddPersonDialog = false },
            title = { Text(if (selectedTab == 0) "Add New Customer (نیا گاہک)" else "Add New Supplier (نیا سپلائر)") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone / WhatsApp") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Address") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = openingBal,
                        onValueChange = { openingBal = it },
                        label = { Text("Opening Udhaar Balance") },
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
                        if (name.isBlank()) {
                            Toast.makeText(context, "Name cannot be empty", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val bal = openingBal.toDoubleOrNull() ?: 0.0
                        if (selectedTab == 0) {
                            val cust = CustomerEntity(
                                name = name,
                                phone = phone,
                                whatsapp = phone,
                                address = address,
                                openingBalance = bal,
                                currentBalance = bal,
                                notes = notes
                            )
                            viewModel.saveCustomer(cust) {
                                Toast.makeText(context, "Customer added!", Toast.LENGTH_SHORT).show()
                                showAddPersonDialog = false
                            }
                        } else {
                            val sup = SupplierEntity(
                                name = name,
                                phone = phone,
                                whatsapp = phone,
                                address = address,
                                openingBalance = bal,
                                currentBalance = bal,
                                notes = notes
                            )
                            viewModel.saveSupplier(sup) {
                                Toast.makeText(context, "Supplier added!", Toast.LENGTH_SHORT).show()
                                showAddPersonDialog = false
                            }
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddPersonDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Customer Payment Dialog
    showPaymentDialogForCustomer?.let { customer ->
        var amountStr by remember { mutableStateOf("") }
        var account by remember { mutableStateOf("Cash in Hand") }
        var notes by remember { mutableStateOf("Payment received from customer") }

        AlertDialog(
            onDismissRequest = { showPaymentDialogForCustomer = null },
            title = { Text("Receive Payment: ${customer.name}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Current Udhaar: ${CurrencyFormatter.formatPkr(customer.currentBalance, settings.currencySymbol)}")
                    OutlinedTextField(
                        value = amountStr,
                        onValueChange = { amountStr = it },
                        label = { Text("Received Amount (رقم) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes / Receipt Ref") },
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
                        viewModel.recordCustomerPayment(customer.id, amount, account, notes) {
                            Toast.makeText(context, "Payment of Rs $amount received!", Toast.LENGTH_SHORT).show()
                            showPaymentDialogForCustomer = null
                        }
                    }
                ) {
                    Text("Record Payment")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPaymentDialogForCustomer = null }) { Text("Cancel") }
            }
        )
    }

    // Supplier Payment Dialog
    showPaymentDialogForSupplier?.let { supplier ->
        var amountStr by remember { mutableStateOf("") }
        var account by remember { mutableStateOf("Cash in Hand") }
        var notes by remember { mutableStateOf("Payment made to supplier") }

        AlertDialog(
            onDismissRequest = { showPaymentDialogForSupplier = null },
            title = { Text("Pay Supplier: ${supplier.name}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Outstanding Payable: ${CurrencyFormatter.formatPkr(supplier.currentBalance, settings.currencySymbol)}")
                    OutlinedTextField(
                        value = amountStr,
                        onValueChange = { amountStr = it },
                        label = { Text("Payment Amount (رقم) *") },
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
                        viewModel.recordSupplierPayment(supplier.id, amount, account, notes) {
                            Toast.makeText(context, "Paid Rs $amount to supplier!", Toast.LENGTH_SHORT).show()
                            showPaymentDialogForSupplier = null
                        }
                    }
                ) {
                    Text("Pay Now")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPaymentDialogForSupplier = null }) { Text("Cancel") }
            }
        )
    }

    // Customer Detailed Ledger Sheet
    selectedCustomerForLedger?.let { customer ->
        var ledgerEntries by remember { mutableStateOf<List<CustomerLedgerEntity>>(emptyList()) }

        LaunchedEffect(customer.id) {
            viewModel.repository.getCustomerLedger(customer.id).collect {
                ledgerEntries = it
            }
        }

        AlertDialog(
            onDismissRequest = { selectedCustomerForLedger = null },
            title = {
                Column {
                    Text(customer.name, fontWeight = FontWeight.Bold)
                    Text("Current Udhaar: ${CurrencyFormatter.formatPkr(customer.currentBalance, settings.currencySymbol)}", fontSize = 13.sp, color = MaterialTheme.colorScheme.error)
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val phone = customer.whatsapp.ifBlank { customer.phone }
                                val msg = "محترم ${customer.name} صاحب! ${settings.shopName} کی طرف سے یاد دہانی کہ آپ کا بقایا ادھار بیلنس ${CurrencyFormatter.formatPkr(customer.currentBalance, settings.currencySymbol)} ہے۔ برائے مہربانی ادائی فرما کر حساب کلیر کریں۔ شکریہ!"
                                PdfInvoiceHelper.shareViaWhatsApp(context, phone, msg)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = FinancialProfitGreen),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("WhatsApp", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                showPaymentDialogForCustomer = customer
                            },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("+ Payment", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Ledger History (کھاتہ تفصیل):", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))

                    if (ledgerEntries.isEmpty()) {
                        Text("No ledger transactions recorded yet.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        LazyColumn(modifier = Modifier.heightIn(max = 280.dp)) {
                            items(ledgerEntries.reversed()) { entry ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(text = "${entry.referenceType} • ${entry.referenceId}", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                            Text(text = CurrencyFormatter.formatDate(entry.createdAt), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            if (entry.notes.isNotBlank()) {
                                                Text(text = entry.notes, fontSize = 10.sp)
                                            }
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            if (entry.debit > 0) {
                                                Text(text = "+${CurrencyFormatter.formatPkr(entry.debit, settings.currencySymbol)}", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            }
                                            if (entry.credit > 0) {
                                                Text(text = "-${CurrencyFormatter.formatPkr(entry.credit, settings.currencySymbol)}", color = FinancialProfitGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            }
                                            Text(text = "Bal: ${CurrencyFormatter.formatPkr(entry.runningBalance, settings.currencySymbol)}", fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedCustomerForLedger = null }) { Text("Close") }
            }
        )
    }

    // Supplier Detailed Ledger Dialog
    selectedSupplierForLedger?.let { supplier ->
        var ledgerEntries by remember { mutableStateOf<List<SupplierLedgerEntity>>(emptyList()) }

        LaunchedEffect(supplier.id) {
            viewModel.repository.getSupplierLedger(supplier.id).collect {
                ledgerEntries = it
            }
        }

        AlertDialog(
            onDismissRequest = { selectedSupplierForLedger = null },
            title = {
                Column {
                    Text(supplier.name, fontWeight = FontWeight.Bold)
                    Text("Outstanding Payable: ${CurrencyFormatter.formatPkr(supplier.currentBalance, settings.currencySymbol)}", fontSize = 13.sp, color = MaterialTheme.colorScheme.error)
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { showPaymentDialogForSupplier = supplier },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Pay Supplier")
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Supplier Ledger History:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))

                    if (ledgerEntries.isEmpty()) {
                        Text("No entries yet.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        LazyColumn(modifier = Modifier.heightIn(max = 280.dp)) {
                            items(ledgerEntries.reversed()) { entry ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(text = "${entry.referenceType} • ${entry.referenceId}", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                            Text(text = CurrencyFormatter.formatDate(entry.createdAt), fontSize = 10.sp)
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            if (entry.credit > 0) {
                                                Text(text = "Bill: +${CurrencyFormatter.formatPkr(entry.credit, settings.currencySymbol)}", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                                            }
                                            if (entry.debit > 0) {
                                                Text(text = "Paid: -${CurrencyFormatter.formatPkr(entry.debit, settings.currencySymbol)}", color = FinancialProfitGreen, fontSize = 12.sp)
                                            }
                                            Text(text = "Bal: ${CurrencyFormatter.formatPkr(entry.runningBalance, settings.currencySymbol)}", fontSize = 10.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedSupplierForLedger = null }) { Text("Close") }
            }
        )
    }
}
