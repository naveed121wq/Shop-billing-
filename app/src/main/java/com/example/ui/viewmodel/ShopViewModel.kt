package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.entity.*
import com.example.data.repository.ShopRepository
import com.example.util.CurrencyFormatter
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale
import java.util.UUID

data class CartItem(
    val product: ProductEntity,
    val quantity: Double = 1.0,
    val unitPrice: Double = product.salePrice,
    val discount: Double = 0.0
) {
    val lineTotal: Double get() = (quantity * unitPrice) - discount
    val costTotal: Double get() = quantity * product.purchasePrice
}

data class DashboardData(
    val todaySales: Double = 0.0,
    val todayPurchases: Double = 0.0,
    val todayExpenses: Double = 0.0,
    val todayCostOfGoods: Double = 0.0,
    val todayGrossProfit: Double = 0.0,
    val todayNetProfit: Double = 0.0,
    val cashInHand: Double = 0.0,
    val totalReceivables: Double = 0.0,
    val totalPayables: Double = 0.0,
    val totalStockQty: Double = 0.0,
    val stockPurchaseValue: Double = 0.0,
    val stockSaleValue: Double = 0.0,
    val lowStockCount: Int = 0,
    val outOfStockCount: Int = 0,
    val todayInvoicesCount: Int = 0
)

data class AiMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: String, // "user" or "ai"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

class ShopViewModel(val repository: ShopRepository) : ViewModel() {

    // --- SETTINGS FLOW ---
    val settings: StateFlow<ShopSettingsEntity> = repository.settingsFlow
        .map { it ?: ShopSettingsEntity() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ShopSettingsEntity())

    // --- PRODUCTS ---
    val allProducts = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lowStockProducts = repository.lowStockProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val outOfStockProducts = repository.outOfStockProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories = repository.categories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- CUSTOMERS & SUPPLIERS ---
    val allCustomers = repository.allCustomers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalCustomerReceivables = repository.totalCustomerReceivables
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val allSuppliers = repository.allSuppliers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalSupplierPayables = repository.totalSupplierPayables
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // --- SALES & PURCHASES & EXPENSES ---
    val allSales = repository.allSales
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPurchases = repository.allPurchases
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allExpenses = repository.allExpenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAccounts = repository.allAccounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allStockTransactions = repository.allStockTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAccountTransactions = repository.allAccountTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- DASHBOARD FILTER ---
    private val _dashboardDateFilter = MutableStateFlow("Today") // Today, Yesterday, This Week, This Month, All Time
    val dashboardDateFilter: StateFlow<String> = _dashboardDateFilter.asStateFlow()

    fun setDashboardDateFilter(filter: String) {
        _dashboardDateFilter.value = filter
    }

    val dashboardData: StateFlow<DashboardData> = combine(
        allProducts,
        allSales,
        allPurchases,
        allExpenses,
        _dashboardDateFilter
    ) { products, sales, purchases, expenses, filter ->

        val now = Calendar.getInstance()
        val (startTime, endTime) = when (filter) {
            "Yesterday" -> {
                val c = Calendar.getInstance()
                c.add(Calendar.DAY_OF_YEAR, -1)
                Pair(CurrencyFormatter.getStartOfDay(c), CurrencyFormatter.getEndOfDay(c))
            }
            "This Week" -> {
                val c = Calendar.getInstance()
                c.set(Calendar.DAY_OF_WEEK, c.firstDayOfWeek)
                Pair(CurrencyFormatter.getStartOfDay(c), System.currentTimeMillis())
            }
            "This Month" -> {
                val c = Calendar.getInstance()
                c.set(Calendar.DAY_OF_MONTH, 1)
                Pair(CurrencyFormatter.getStartOfDay(c), System.currentTimeMillis())
            }
            "All Time" -> Pair(0L, Long.MAX_VALUE)
            else -> Pair(CurrencyFormatter.getStartOfDay(now), CurrencyFormatter.getEndOfDay(now)) // Today
        }

        val filteredSales = sales.filter { it.createdAt in startTime..endTime }
        val filteredPurchases = purchases.filter { it.createdAt in startTime..endTime }
        val filteredExpenses = expenses.filter { it.createdAt in startTime..endTime }

        val salesTotal = filteredSales.sumOf { it.grandTotal }
        val cogsTotal = filteredSales.sumOf { it.costTotal }
        val grossProfit = salesTotal - cogsTotal
        val expensesTotal = filteredExpenses.sumOf { it.amount }
        val netProfit = grossProfit - expensesTotal
        val purchasesTotal = filteredPurchases.sumOf { it.grandTotal }

        val accounts = allAccounts.value
        val receivables = totalCustomerReceivables.value
        val payables = totalSupplierPayables.value

        val cashInHand = accounts.find { it.name.contains("Cash", ignoreCase = true) }?.balance
            ?: accounts.firstOrNull()?.balance ?: 0.0

        val totalStockQty = products.sumOf { it.currentStock }
        val stockPurchaseValue = products.sumOf { it.currentStock * it.purchasePrice }
        val stockSaleValue = products.sumOf { it.currentStock * it.salePrice }
        val lowStockCount = products.count { it.currentStock in 0.1..it.minStockLevel }
        val outOfStockCount = products.count { it.currentStock <= 0.0 }

        DashboardData(
            todaySales = salesTotal,
            todayPurchases = purchasesTotal,
            todayExpenses = expensesTotal,
            todayCostOfGoods = cogsTotal,
            todayGrossProfit = grossProfit,
            todayNetProfit = netProfit,
            cashInHand = cashInHand,
            totalReceivables = receivables,
            totalPayables = payables,
            totalStockQty = totalStockQty,
            stockPurchaseValue = stockPurchaseValue,
            stockSaleValue = stockSaleValue,
            lowStockCount = lowStockCount,
            outOfStockCount = outOfStockCount,
            todayInvoicesCount = filteredSales.size
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardData())

    // --- POS STATE ---
    private val _cart = MutableStateFlow<List<CartItem>>(emptyList())
    val cart: StateFlow<List<CartItem>> = _cart.asStateFlow()

    private val _selectedCustomer = MutableStateFlow<CustomerEntity?>(null)
    val selectedCustomer: StateFlow<CustomerEntity?> = _selectedCustomer.asStateFlow()

    private val _invoiceDiscount = MutableStateFlow(0.0)
    val invoiceDiscount: StateFlow<Double> = _invoiceDiscount.asStateFlow()

    private val _paymentMethod = MutableStateFlow("Cash") // Cash, JazzCash, Easypaisa, Bank, Udhaar
    val paymentMethod: StateFlow<String> = _paymentMethod.asStateFlow()

    private val _paymentAccount = MutableStateFlow("Cash in Hand")
    val paymentAccount: StateFlow<String> = _paymentAccount.asStateFlow()

    private val _paidAmount = MutableStateFlow<Double?>(null)
    val paidAmount: StateFlow<Double?> = _paidAmount.asStateFlow()

    private val _heldCarts = MutableStateFlow<List<List<CartItem>>>(emptyList())
    val heldCarts: StateFlow<List<List<CartItem>>> = _heldCarts.asStateFlow()

    // Last completed sale for invoice preview
    private val _lastCompletedSale = MutableStateFlow<Pair<SaleEntity, List<SaleItemEntity>>?>(null)
    val lastCompletedSale: StateFlow<Pair<SaleEntity, List<SaleItemEntity>>?> = _lastCompletedSale.asStateFlow()

    fun addToCart(product: ProductEntity, quantity: Double = 1.0) {
        val current = _cart.value.toMutableList()
        val existingIndex = current.indexOfFirst { it.product.id == product.id }
        if (existingIndex >= 0) {
            val item = current[existingIndex]
            current[existingIndex] = item.copy(quantity = item.quantity + quantity)
        } else {
            current.add(CartItem(product = product, quantity = quantity))
        }
        _cart.value = current
    }

    fun updateCartItemQuantity(productId: Long, newQuantity: Double) {
        if (newQuantity <= 0) {
            removeFromCart(productId)
        } else {
            _cart.value = _cart.value.map {
                if (it.product.id == productId) it.copy(quantity = newQuantity) else it
            }
        }
    }

    fun updateCartItemPrice(productId: Long, newPrice: Double) {
        _cart.value = _cart.value.map {
            if (it.product.id == productId) it.copy(unitPrice = newPrice) else it
        }
    }

    fun updateCartItemDiscount(productId: Long, discount: Double) {
        _cart.value = _cart.value.map {
            if (it.product.id == productId) it.copy(discount = discount) else it
        }
    }

    fun removeFromCart(productId: Long) {
        _cart.value = _cart.value.filter { it.product.id != productId }
    }

    fun clearCart() {
        _cart.value = emptyList()
        _selectedCustomer.value = null
        _invoiceDiscount.value = 0.0
        _paidAmount.value = null
        _paymentMethod.value = "Cash"
    }

    fun setSelectedCustomer(customer: CustomerEntity?) {
        _selectedCustomer.value = customer
    }

    fun setInvoiceDiscount(discount: Double) {
        _invoiceDiscount.value = discount
    }

    fun setPaymentMethod(method: String) {
        _paymentMethod.value = method
        if (method == "Udhaar") {
            _paidAmount.value = 0.0
        }
    }

    fun setPaymentAccount(account: String) {
        _paymentAccount.value = account
    }

    fun setPaidAmount(amount: Double?) {
        _paidAmount.value = amount
    }

    fun holdCart() {
        if (_cart.value.isNotEmpty()) {
            _heldCarts.value = _heldCarts.value + listOf(_cart.value)
            clearCart()
        }
    }

    fun resumeCart(index: Int) {
        val held = _heldCarts.value.toMutableList()
        if (index in held.indices) {
            _cart.value = held.removeAt(index)
            _heldCarts.value = held
        }
    }

    fun dismissLastSaleDialog() {
        _lastCompletedSale.value = null
    }

    fun checkoutSale(onSuccess: (SaleEntity) -> Unit, onError: (String) -> Unit) {
        val items = _cart.value
        if (items.isEmpty()) {
            onError("Cart is empty!")
            return
        }

        // Check stock if negative stock not allowed
        val allowNegative = settings.value.allowNegativeStock
        if (!allowNegative) {
            for (item in items) {
                if (item.product.currentStock < item.quantity) {
                    onError("Insufficient stock for ${item.product.name}! Available: ${item.product.currentStock}")
                    return
                }
            }
        }

        viewModelScope.launch {
            try {
                val subtotal = items.sumOf { it.lineTotal }
                val grandTotal = (subtotal - _invoiceDiscount.value).coerceAtLeast(0.0)
                val customer = _selectedCustomer.value

                val method = _paymentMethod.value
                val paid = if (method == "Udhaar") {
                    0.0
                } else {
                    _paidAmount.value ?: grandTotal
                }
                val remaining = (grandTotal - paid).coerceAtLeast(0.0)

                val costTotal = items.sumOf { it.costTotal }
                val invoiceNum = repository.generateNextInvoiceNumber()
                val todayStr = CurrencyFormatter.getDayString()

                val saleEntity = SaleEntity(
                    invoiceNumber = invoiceNum,
                    customerId = customer?.id,
                    customerName = customer?.name ?: "Walk-in Customer",
                    customerPhone = customer?.phone ?: "",
                    subtotal = subtotal,
                    discountAmount = _invoiceDiscount.value,
                    grandTotal = grandTotal,
                    paidAmount = paid,
                    remainingAmount = remaining,
                    paymentMethod = method,
                    paymentAccount = _paymentAccount.value,
                    costTotal = costTotal,
                    status = "COMPLETED",
                    createdAt = System.currentTimeMillis(),
                    dateDayString = todayStr
                )

                val saleItems = items.map {
                    SaleItemEntity(
                        saleId = 0,
                        productId = it.product.id,
                        productName = it.product.name,
                        barcode = it.product.barcode,
                        unit = it.product.unit,
                        quantity = it.quantity,
                        unitPrice = it.unitPrice,
                        purchaseCost = it.product.purchasePrice,
                        discount = it.discount,
                        lineTotal = it.lineTotal
                    )
                }

                val saleId = repository.completeSale(saleEntity, saleItems)
                val completed = saleEntity.copy(id = saleId)
                _lastCompletedSale.value = Pair(completed, saleItems)
                clearCart()
                onSuccess(completed)
            } catch (e: Exception) {
                onError(e.message ?: "Failed to complete sale")
            }
        }
    }

    // --- PURCHASES ---
    fun completePurchase(
        supplier: SupplierEntity?,
        items: List<CartItem>,
        paid: Double,
        paymentMethod: String,
        paymentAccount: String,
        notes: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (items.isEmpty()) {
            onError("No products added to purchase!")
            return
        }
        viewModelScope.launch {
            try {
                val subtotal = items.sumOf { it.quantity * it.unitPrice }
                val grandTotal = subtotal
                val remaining = (grandTotal - paid).coerceAtLeast(0.0)
                val invoiceNum = repository.generateNextPurchaseNumber()
                val todayStr = CurrencyFormatter.getDayString()

                val purchase = PurchaseEntity(
                    invoiceNumber = invoiceNum,
                    supplierId = supplier?.id,
                    supplierName = supplier?.name ?: "General Supplier",
                    subtotal = subtotal,
                    grandTotal = grandTotal,
                    paidAmount = paid,
                    remainingAmount = remaining,
                    paymentMethod = paymentMethod,
                    paymentAccount = paymentAccount,
                    notes = notes,
                    createdAt = System.currentTimeMillis(),
                    dateDayString = todayStr
                )

                val purchaseItems = items.map {
                    PurchaseItemEntity(
                        purchaseId = 0,
                        productId = it.product.id,
                        productName = it.product.name,
                        unit = it.product.unit,
                        quantity = it.quantity,
                        unitPrice = it.unitPrice,
                        lineTotal = it.quantity * it.unitPrice
                    )
                }

                repository.completePurchase(purchase, purchaseItems)
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Failed to save purchase")
            }
        }
    }

    // --- EXPENSES ---
    fun addExpense(
        category: String,
        amount: Double,
        paymentAccount: String,
        description: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (amount <= 0) {
            onError("Amount must be greater than zero")
            return
        }
        viewModelScope.launch {
            try {
                val expense = ExpenseEntity(
                    category = category,
                    amount = amount,
                    paymentAccount = paymentAccount,
                    description = description,
                    createdAt = System.currentTimeMillis(),
                    dateDayString = CurrencyFormatter.getDayString()
                )
                repository.recordExpense(expense)
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Failed to add expense")
            }
        }
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
        }
    }

    // --- CUSTOMER & SUPPLIER PAYMENTS ---
    fun recordCustomerPayment(
        customerId: Long,
        amount: Double,
        account: String,
        notes: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            repository.recordCustomerPayment(customerId, amount, account, notes)
            onSuccess()
        }
    }

    fun recordSupplierPayment(
        supplierId: Long,
        amount: Double,
        account: String,
        notes: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            repository.recordSupplierPayment(supplierId, amount, account, notes)
            onSuccess()
        }
    }

    // --- PRODUCT CRUD ---
    fun saveProduct(product: ProductEntity, onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (product.id == 0L) {
                repository.insertProduct(product)
            } else {
                repository.updateProduct(product)
            }
            onSuccess()
        }
    }

    fun deleteProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.deleteProduct(product)
        }
    }

    fun adjustStock(productId: Long, diff: Double, reason: String, type: String) {
        viewModelScope.launch {
            repository.recordStockAdjustment(productId, diff, reason, type)
        }
    }

    // --- CUSTOMER / SUPPLIER CRUD ---
    fun saveCustomer(customer: CustomerEntity, onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (customer.id == 0L) {
                val custId = repository.insertCustomer(customer)
                if (customer.openingBalance > 0) {
                    repository.recordCustomerPayment(custId, -customer.openingBalance, "Cash in Hand", "Opening Balance Udhaar")
                }
            } else {
                repository.updateCustomer(customer)
            }
            onSuccess()
        }
    }

    fun saveSupplier(supplier: SupplierEntity, onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (supplier.id == 0L) {
                repository.insertSupplier(supplier)
            } else {
                repository.updateSupplier(supplier)
            }
            onSuccess()
        }
    }

    // --- ACCOUNTS ---
    fun transferAccount(fromId: Long, toId: Long, amount: Double, notes: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.transferAccount(fromId, toId, amount, notes)
            onSuccess()
        }
    }

    // --- SETTINGS ---
    fun updateSettings(updated: ShopSettingsEntity, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.updateSettings(updated)
            onSuccess()
        }
    }

    // --- DEMO DATA / RESET ---
    fun loadDemoData(onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.loadDemoData()
            onSuccess()
        }
    }

    fun clearAllData(onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.clearAllData()
            onSuccess()
        }
    }

    // --- AI ASSISTANT ---
    private val _aiMessages = MutableStateFlow<List<AiMessage>>(
        listOf(
            AiMessage(
                sender = "ai",
                text = "السلام علیکم! میں آپ کا شاپ اسسٹنٹ ہوں۔ آپ مجھ سے دکان کی سیل، منافع، کھاتہ یا اسٹاک کے بارے میں کوئی بھی سوال اردو یا انگریزی میں پوچھ سکتے ہیں۔\n\nHello! I am your Shop Assistant. Ask me anything about your shop's sales, profit, udhaar or inventory."
            )
        )
    )
    val aiMessages: StateFlow<List<AiMessage>> = _aiMessages.asStateFlow()

    fun askAi(query: String) {
        if (query.isBlank()) return
        val userMsg = AiMessage(sender = "user", text = query)
        _aiMessages.value = _aiMessages.value + userMsg

        viewModelScope.launch {
            val dash = dashboardData.value
            val q = query.lowercase(Locale.ROOT)
            val currency = settings.value.currencySymbol

            val response = when {
                q.contains("sale") || q.contains("سیل") || q.contains("فروخت") -> {
                    "آج کی کل سیل ${CurrencyFormatter.formatPkr(dash.todaySales, currency)} ہے۔ کل انوائسز: ${dash.todayInvoicesCount}۔\nToday's total sales: ${CurrencyFormatter.formatPkr(dash.todaySales, currency)} across ${dash.todayInvoicesCount} invoices."
                }
                q.contains("profit") || q.contains("نفع") || q.contains("منافع") -> {
                    "آج کا گراس منافع ${CurrencyFormatter.formatPkr(dash.todayGrossProfit, currency)} اور اخراجات نکال کر خالص منافع (Net Profit) ${CurrencyFormatter.formatPkr(dash.todayNetProfit, currency)} ہے۔\nGross Profit: ${CurrencyFormatter.formatPkr(dash.todayGrossProfit, currency)}, Net Profit: ${CurrencyFormatter.formatPkr(dash.todayNetProfit, currency)}."
                }
                q.contains("expense") || q.contains("خرچہ") || q.contains("اخراجات") -> {
                    "آج کے کل اخراجات ${CurrencyFormatter.formatPkr(dash.todayExpenses, currency)} ہیں۔\nToday's total business expenses: ${CurrencyFormatter.formatPkr(dash.todayExpenses, currency)}."
                }
                q.contains("udhaar") || q.contains("ادھار") || q.contains("receivable") || q.contains("کھاتہ") -> {
                    "گاہکوں سے کل وصول طلب ادھار (Receivables): ${CurrencyFormatter.formatPkr(dash.totalReceivables, currency)} ہے۔\nسپلائرز کو دینا واجب الادا (Payables): ${CurrencyFormatter.formatPkr(dash.totalPayables, currency)} ہے۔\nCustomer receivables: ${CurrencyFormatter.formatPkr(dash.totalReceivables, currency)}."
                }
                q.contains("stock") || q.contains("اسٹاک") || q.contains("low") || q.contains("کم") -> {
                    val lowList = lowStockProducts.value.joinToString(", ") { "${it.name} (${it.currentStock.toInt()})" }
                    val outList = outOfStockProducts.value.joinToString(", ") { it.name }
                    "کم اسٹاک والی آئٹمز (${dash.lowStockCount}): ${if (lowList.isBlank()) "کوئی نہیں" else lowList}\nختم شدہ آئٹمز (${dash.outOfStockCount}): ${if (outList.isBlank()) "کوئی نہیں" else outList}\nاسٹاک کی کل ویلیو: ${CurrencyFormatter.formatPkr(dash.stockSaleValue, currency)}۔"
                }
                q.contains("cash") || q.contains("کیش") -> {
                    "دکان کے دراز میں کیش (Cash in Hand): ${CurrencyFormatter.formatPkr(dash.cashInHand, currency)} موجود ہے۔"
                }
                else -> {
                    "خلاصہ برائے آج:\n• سیل: ${CurrencyFormatter.formatPkr(dash.todaySales, currency)}\n• خالص منافع: ${CurrencyFormatter.formatPkr(dash.todayNetProfit, currency)}\n• کیش ان ہینڈ: ${CurrencyFormatter.formatPkr(dash.cashInHand, currency)}\n• کسٹمر ادھار: ${CurrencyFormatter.formatPkr(dash.totalReceivables, currency)}\n• کم اسٹاک آئٹمز: ${dash.lowStockCount}"
                }
            }

            _aiMessages.value = _aiMessages.value + AiMessage(sender = "ai", text = response)
        }
    }
}
