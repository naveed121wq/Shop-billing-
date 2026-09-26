package com.example.ui.screens

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.entity.ProductEntity
import com.example.ui.components.StockBadge
import com.example.ui.theme.FinancialLossRed
import com.example.ui.theme.FinancialProfitGreen
import com.example.ui.theme.PrimaryGreen
import com.example.ui.viewmodel.ShopViewModel
import com.example.util.BarcodeHelper
import com.example.util.CurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(
    viewModel: ShopViewModel,
    onNavigateToBarcodeScan: () -> Unit
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val allProducts by viewModel.allProducts.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var stockFilter by remember { mutableStateOf("All") } // All, Low, Out
    var productToEdit by remember { mutableStateOf<ProductEntity?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var productForStockAdjust by remember { mutableStateOf<ProductEntity?>(null) }

    val filtered = remember(allProducts, searchQuery, selectedCategory, stockFilter) {
        allProducts.filter { p ->
            val matchQuery = searchQuery.isBlank() ||
                    p.name.contains(searchQuery, ignoreCase = true) ||
                    p.barcode.contains(searchQuery, ignoreCase = true) ||
                    p.sku.contains(searchQuery, ignoreCase = true)

            val matchCat = selectedCategory == null || p.category.equals(selectedCategory, ignoreCase = true)

            val matchStock = when (stockFilter) {
                "Low" -> p.currentStock in 0.1..p.minStockLevel
                "Out" -> p.currentStock <= 0.0
                else -> true
            }
            matchQuery && matchCat && matchStock
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inventory (اسٹاک و اشیاء)", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onNavigateToBarcodeScan) {
                        Icon(imageVector = Icons.Default.QrCodeScanner, contentDescription = "Scan")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = PrimaryGreen,
                contentColor = androidx.compose.ui.graphics.Color.White
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Product")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                placeholder = { Text("Search product, barcode or SKU...") },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Stock quick filters
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Low Stock", "Out of Stock").forEach { filterName ->
                    val filterKey = when (filterName) {
                        "Low Stock" -> "Low"
                        "Out of Stock" -> "Out"
                        else -> "All"
                    }
                    FilterChip(
                        selected = stockFilter == filterKey,
                        onClick = { stockFilter = filterKey },
                        label = { Text(filterName, fontSize = 12.sp) }
                    )
                }
            }

            // Category row
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { selectedCategory = null },
                        label = { Text("All Categories") }
                    )
                }
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = if (selectedCategory == cat) null else cat },
                        label = { Text(cat) }
                    )
                }
            }

            // Products List
            if (filtered.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No products found.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filtered) { product ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { productToEdit = product },
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
                                        text = product.name,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Sale: ${CurrencyFormatter.formatPkr(product.salePrice, settings.currencySymbol)} | Buy: ${CurrencyFormatter.formatPkr(product.purchasePrice, settings.currencySymbol)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (product.barcode.isNotBlank()) {
                                        Text(
                                            text = "Barcode: ${product.barcode}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    StockBadge(
                                        currentStock = product.currentStock,
                                        minStock = product.minStockLevel,
                                        unit = product.unit
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    TextButton(
                                        onClick = { productForStockAdjust = product },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text("Adjust", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add or Edit Product Dialog
    if (showAddDialog || productToEdit != null) {
        val editing = productToEdit
        var name by remember { mutableStateOf(editing?.name ?: "") }
        var barcode by remember { mutableStateOf(editing?.barcode ?: "") }
        var sku by remember { mutableStateOf(editing?.sku ?: "") }
        var category by remember { mutableStateOf(editing?.category ?: "Groceries") }
        var unit by remember { mutableStateOf(editing?.unit ?: "Piece") }
        var buyPrice by remember { mutableStateOf(editing?.purchasePrice?.let { if (it > 0) "${it.toInt()}" else "" } ?: "") }
        var salePrice by remember { mutableStateOf(editing?.salePrice?.let { if (it > 0) "${it.toInt()}" else "" } ?: "") }
        var stock by remember { mutableStateOf(editing?.currentStock?.let { "${it.toInt()}" } ?: "0") }
        var minStock by remember { mutableStateOf(editing?.minStockLevel?.let { "${it.toInt()}" } ?: "5") }
        var generatedBarcodeBmp by remember { mutableStateOf<Bitmap?>(null) }

        LaunchedEffect(barcode) {
            if (barcode.isNotBlank()) {
                generatedBarcodeBmp = BarcodeHelper.generateBarcodeBitmap(barcode)
            }
        }

        AlertDialog(
            onDismissRequest = {
                showAddDialog = false
                productToEdit = null
            },
            title = { Text(if (editing == null) "Add New Product (نیا سامان)" else "Edit Product") },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Product Name *") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = salePrice,
                                onValueChange = { salePrice = it },
                                label = { Text("Sale Price *") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = buyPrice,
                                onValueChange = { buyPrice = it },
                                label = { Text("Buy Price") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = stock,
                                onValueChange = { stock = it },
                                label = { Text("Current Stock") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = unit,
                                onValueChange = { unit = it },
                                label = { Text("Unit (Piece/Kg)") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = category,
                                onValueChange = { category = it },
                                label = { Text("Category") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = minStock,
                                onValueChange = { minStock = it },
                                label = { Text("Min Alert Level") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    item {
                        OutlinedTextField(
                            value = barcode,
                            onValueChange = { barcode = it },
                            label = { Text("Barcode") },
                            trailingIcon = {
                                IconButton(onClick = {
                                    val randomCode = "8961${System.currentTimeMillis() % 100000000}"
                                    barcode = randomCode
                                }) {
                                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "Auto Generate")
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Barcode Preview if available
                    generatedBarcodeBmp?.let { bmp ->
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Image(
                                    bitmap = bmp.asImageBitmap(),
                                    contentDescription = "Barcode Preview",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(60.dp)
                                )
                                Text(text = barcode, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (editing != null) {
                        item {
                            Button(
                                onClick = {
                                    viewModel.deleteProduct(editing)
                                    productToEdit = null
                                    Toast.makeText(context, "Product deleted", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = FinancialLossRed),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Delete Product")
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isBlank()) {
                            Toast.makeText(context, "Product name cannot be empty", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val sp = salePrice.toDoubleOrNull() ?: 0.0
                        val bp = buyPrice.toDoubleOrNull() ?: 0.0
                        val st = stock.toDoubleOrNull() ?: 0.0
                        val mst = minStock.toDoubleOrNull() ?: 5.0

                        val toSave = (editing ?: ProductEntity(name = name)).copy(
                            name = name,
                            barcode = barcode,
                            sku = sku.ifBlank { barcode },
                            category = category.ifBlank { "General" },
                            unit = unit.ifBlank { "Piece" },
                            salePrice = sp,
                            retailPrice = sp,
                            purchasePrice = bp,
                            currentStock = st,
                            minStockLevel = mst,
                            updatedAt = System.currentTimeMillis()
                        )

                        viewModel.saveProduct(toSave) {
                            Toast.makeText(context, "Product saved successfully", Toast.LENGTH_SHORT).show()
                            showAddDialog = false
                            productToEdit = null
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAddDialog = false
                    productToEdit = null
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Stock Adjustment Dialog
    productForStockAdjust?.let { product ->
        var adjustmentQty by remember { mutableStateOf("") }
        var reason by remember { mutableStateOf("Manual Count Correction") }
        var type by remember { mutableStateOf("ADJUSTMENT") } // ADJUSTMENT, DAMAGED, LOST

        AlertDialog(
            onDismissRequest = { productForStockAdjust = null },
            title = { Text("Stock Adjustment - ${product.name}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Current Stock: ${product.currentStock.toInt()} ${product.unit}")
                    OutlinedTextField(
                        value = adjustmentQty,
                        onValueChange = { adjustmentQty = it },
                        label = { Text("Change Quantity (+/-)") },
                        placeholder = { Text("e.g. +5 or -2") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text("Reason (وجہ)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val diff = adjustmentQty.toDoubleOrNull()
                        if (diff == null || diff == 0.0) {
                            Toast.makeText(context, "Enter a valid non-zero adjustment quantity", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        viewModel.adjustStock(product.id, diff, reason, type)
                        Toast.makeText(context, "Stock adjusted successfully", Toast.LENGTH_SHORT).show()
                        productForStockAdjust = null
                    }
                ) {
                    Text("Apply Adjustment")
                }
            },
            dismissButton = {
                TextButton(onClick = { productForStockAdjust = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
