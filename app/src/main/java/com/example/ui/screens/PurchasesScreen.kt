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
import com.example.data.entity.ProductEntity
import com.example.data.entity.PurchaseEntity
import com.example.data.entity.PurchaseItemEntity
import com.example.data.entity.SupplierEntity
import com.example.ui.theme.PrimaryGreen
import com.example.ui.viewmodel.CartItem
import com.example.ui.viewmodel.ShopViewModel
import com.example.util.CurrencyFormatter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchasesScreen(
    viewModel: ShopViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val purchases by viewModel.allPurchases.collectAsStateWithLifecycle()
    val suppliers by viewModel.allSuppliers.collectAsStateWithLifecycle()
    val products by viewModel.allProducts.collectAsStateWithLifecycle()

    var showNewPurchaseDialog by remember { mutableStateOf(false) }
    var selectedPurchaseDetail by remember { mutableStateOf<Pair<PurchaseEntity, List<PurchaseItemEntity>>?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Purchases (مال کی خریداری)", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showNewPurchaseDialog = true },
                containerColor = PrimaryGreen,
                contentColor = Color.White
            ) {
                Icon(imageVector = Icons.Default.AddShoppingCart, contentDescription = "New Purchase")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            if (purchases.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.ShoppingBag,
                            contentDescription = null,
                            modifier = Modifier.size(54.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No purchases recorded yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { showNewPurchaseDialog = true }) {
                            Text("+ Record New Purchase")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 80.dp, top = 8.dp)
                ) {
                    items(purchases) { purchase ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    coroutineScope.launch {
                                        val items = viewModel.repository.getPurchaseItemsDirect(purchase.id)
                                        selectedPurchaseDetail = Pair(purchase, items)
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
                                Column {
                                    Text(
                                        text = "${purchase.invoiceNumber} • ${purchase.supplierName}",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = CurrencyFormatter.formatDate(purchase.createdAt),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (purchase.remainingAmount > 0) {
                                        Text(
                                            text = "Remaining Payable: ${CurrencyFormatter.formatPkr(purchase.remainingAmount, settings.currencySymbol)}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }

                                Text(
                                    text = CurrencyFormatter.formatPkr(purchase.grandTotal, settings.currencySymbol),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryGreen
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // New Purchase Dialog
    if (showNewPurchaseDialog) {
        var selectedSupplier by remember { mutableStateOf<SupplierEntity?>(suppliers.firstOrNull()) }
        var selectedProduct by remember { mutableStateOf<ProductEntity?>(products.firstOrNull()) }
        var quantityStr by remember { mutableStateOf("10") }
        var buyPriceStr by remember { mutableStateOf(selectedProduct?.purchasePrice?.let { "${it.toInt()}" } ?: "100") }
        var paidStr by remember { mutableStateOf("") }
        var notes by remember { mutableStateOf("") }

        LaunchedEffect(selectedProduct) {
            selectedProduct?.let {
                buyPriceStr = "${it.purchasePrice.toInt()}"
            }
        }

        AlertDialog(
            onDismissRequest = { showNewPurchaseDialog = false },
            title = { Text("New Stock Purchase (نئی خریداری)") },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        Text("Select Supplier (سپلائر):", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        if (suppliers.isEmpty()) {
                            Text("No suppliers saved. Will save as General Supplier.", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                        } else {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                suppliers.forEach { sup ->
                                    FilterChip(
                                        selected = selectedSupplier?.id == sup.id,
                                        onClick = { selectedSupplier = sup },
                                        label = { Text(sup.name, fontSize = 11.sp) },
                                        modifier = Modifier.padding(end = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Text("Select Product (آئٹم):", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        if (products.isEmpty()) {
                            Text("Please add products first.", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                        } else {
                            LazyColumn(modifier = Modifier.heightIn(max = 120.dp)) {
                                items(products) { prod ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { selectedProduct = prod }
                                            .padding(vertical = 2.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (selectedProduct?.id == prod.id) PrimaryGreen.copy(alpha = 0.15f)
                                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                        )
                                    ) {
                                        Text(
                                            text = "${prod.name} (Stock: ${prod.currentStock.toInt()})",
                                            modifier = Modifier.padding(6.dp),
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = quantityStr,
                                onValueChange = { quantityStr = it },
                                label = { Text("Quantity") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = buyPriceStr,
                                onValueChange = { buyPriceStr = it },
                                label = { Text("Buy Rate (Rs)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        val qty = quantityStr.toDoubleOrNull() ?: 0.0
                        val rate = buyPriceStr.toDoubleOrNull() ?: 0.0
                        val total = qty * rate

                        Text("Total Bill: ${CurrencyFormatter.formatPkr(total, settings.currencySymbol)}", fontWeight = FontWeight.Bold)

                        OutlinedTextField(
                            value = paidStr,
                            onValueChange = { paidStr = it },
                            label = { Text("Amount Paid Now") },
                            placeholder = { Text("Default: Full Amount") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Notes") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val prod = selectedProduct
                        if (prod == null) {
                            Toast.makeText(context, "Please select a product", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val qty = quantityStr.toDoubleOrNull() ?: 0.0
                        val rate = buyPriceStr.toDoubleOrNull() ?: 0.0
                        if (qty <= 0) {
                            Toast.makeText(context, "Enter a valid quantity", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val total = qty * rate
                        val paid = paidStr.toDoubleOrNull() ?: total

                        val cartItem = CartItem(
                            product = prod,
                            quantity = qty,
                            unitPrice = rate
                        )

                        viewModel.completePurchase(
                            supplier = selectedSupplier,
                            items = listOf(cartItem),
                            paid = paid,
                            paymentMethod = "Cash",
                            paymentAccount = "Cash in Hand",
                            notes = notes,
                            onSuccess = {
                                Toast.makeText(context, "Purchase recorded & stock updated!", Toast.LENGTH_SHORT).show()
                                showNewPurchaseDialog = false
                            },
                            onError = { err ->
                                Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                ) {
                    Text("Save Purchase")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewPurchaseDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Purchase Detail Dialog
    selectedPurchaseDetail?.let { (purchase, items) ->
        AlertDialog(
            onDismissRequest = { selectedPurchaseDetail = null },
            title = { Text("Purchase #${purchase.invoiceNumber}") },
            text = {
                Column {
                    Text("Supplier: ${purchase.supplierName}", fontWeight = FontWeight.Bold)
                    Text("Date: ${CurrencyFormatter.formatDate(purchase.createdAt)}", fontSize = 12.sp)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    items.forEach { itm ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${itm.productName} (${itm.quantity.toInt()} ${itm.unit})", fontSize = 13.sp)
                            Text(CurrencyFormatter.formatPkr(itm.lineTotal, settings.currencySymbol), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Grand Total:", fontWeight = FontWeight.Bold)
                        Text(CurrencyFormatter.formatPkr(purchase.grandTotal, settings.currencySymbol), fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Paid:", fontSize = 13.sp)
                        Text(CurrencyFormatter.formatPkr(purchase.paidAmount, settings.currencySymbol), fontSize = 13.sp)
                    }
                    if (purchase.remainingAmount > 0) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Remaining Payable:", fontSize = 13.sp, color = MaterialTheme.colorScheme.error)
                            Text(CurrencyFormatter.formatPkr(purchase.remainingAmount, settings.currencySymbol), color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedPurchaseDetail = null }) { Text("Close") }
            }
        )
    }
}
