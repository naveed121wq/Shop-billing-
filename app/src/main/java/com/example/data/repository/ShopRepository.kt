package com.example.data.repository

import com.example.data.dao.ShopDao
import com.example.data.database.DemoDataLoader
import com.example.data.entity.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.*

class ShopRepository(private val dao: ShopDao) {

    // --- PRODUCTS ---
    val allProducts: Flow<List<ProductEntity>> = dao.getAllProducts()
    val activeProducts: Flow<List<ProductEntity>> = dao.getActiveProducts()
    val lowStockProducts: Flow<List<ProductEntity>> = dao.getLowStockProducts()
    val outOfStockProducts: Flow<List<ProductEntity>> = dao.getOutOfStockProducts()
    val categories: Flow<List<String>> = dao.getCategories()

    fun searchProducts(query: String): Flow<List<ProductEntity>> = dao.searchProducts(query)
    fun getProductById(id: Long): Flow<ProductEntity?> = dao.getProductById(id)
    suspend fun getProductByBarcode(barcode: String): ProductEntity? = dao.getProductByBarcode(barcode)
    suspend fun insertProduct(product: ProductEntity): Long = dao.insertProduct(product)
    suspend fun updateProduct(product: ProductEntity) = dao.updateProduct(product)
    suspend fun deleteProduct(product: ProductEntity) = dao.deleteProduct(product)
    suspend fun getProductCount(): Int = dao.getProductCount()

    // --- CUSTOMERS ---
    val allCustomers: Flow<List<CustomerEntity>> = dao.getAllCustomers()
    val totalCustomerReceivables: Flow<Double?> = dao.getTotalCustomerReceivables()
    fun searchCustomers(query: String): Flow<List<CustomerEntity>> = dao.searchCustomers(query)
    fun getCustomerById(id: Long): Flow<CustomerEntity?> = dao.getCustomerById(id)
    suspend fun insertCustomer(customer: CustomerEntity): Long = dao.insertCustomer(customer)
    suspend fun updateCustomer(customer: CustomerEntity) = dao.updateCustomer(customer)
    suspend fun deleteCustomer(customer: CustomerEntity) = dao.deleteCustomer(customer)
    fun getCustomerLedger(customerId: Long): Flow<List<CustomerLedgerEntity>> = dao.getCustomerLedger(customerId)

    // --- SUPPLIERS ---
    val allSuppliers: Flow<List<SupplierEntity>> = dao.getAllSuppliers()
    val totalSupplierPayables: Flow<Double?> = dao.getTotalSupplierPayables()
    fun searchSuppliers(query: String): Flow<List<SupplierEntity>> = dao.searchSuppliers(query)
    fun getSupplierById(id: Long): Flow<SupplierEntity?> = dao.getSupplierById(id)
    suspend fun insertSupplier(supplier: SupplierEntity): Long = dao.insertSupplier(supplier)
    suspend fun updateSupplier(supplier: SupplierEntity) = dao.updateSupplier(supplier)
    suspend fun deleteSupplier(supplier: SupplierEntity) = dao.deleteSupplier(supplier)
    fun getSupplierLedger(supplierId: Long): Flow<List<SupplierLedgerEntity>> = dao.getSupplierLedger(supplierId)

    // --- SALES ---
    val allSales: Flow<List<SaleEntity>> = dao.getAllSales()
    fun getSaleById(id: Long): Flow<SaleEntity?> = dao.getSaleById(id)
    suspend fun getSaleByIdDirect(id: Long): SaleEntity? = dao.getSaleByIdDirect(id)
    fun getSaleItems(saleId: Long): Flow<List<SaleItemEntity>> = dao.getSaleItems(saleId)
    suspend fun getSaleItemsDirect(saleId: Long): List<SaleItemEntity> = dao.getSaleItemsDirect(saleId)
    fun getSalesByTimeRange(startTime: Long, endTime: Long): Flow<List<SaleEntity>> =
        dao.getSalesByTimeRange(startTime, endTime)

    suspend fun completeSale(sale: SaleEntity, items: List<SaleItemEntity>): Long =
        dao.completeSaleAtomic(sale, items)

    // --- PURCHASES ---
    val allPurchases: Flow<List<PurchaseEntity>> = dao.getAllPurchases()
    fun getPurchaseById(id: Long): Flow<PurchaseEntity?> = dao.getPurchaseById(id)
    suspend fun getPurchaseByIdDirect(id: Long): PurchaseEntity? = dao.getPurchaseByIdDirect(id)
    fun getPurchaseItems(purchaseId: Long): Flow<List<PurchaseItemEntity>> = dao.getPurchaseItems(purchaseId)
    suspend fun getPurchaseItemsDirect(purchaseId: Long): List<PurchaseItemEntity> = dao.getPurchaseItemsDirect(purchaseId)
    fun getPurchasesByTimeRange(startTime: Long, endTime: Long): Flow<List<PurchaseEntity>> =
        dao.getPurchasesByTimeRange(startTime, endTime)

    suspend fun completePurchase(purchase: PurchaseEntity, items: List<PurchaseItemEntity>): Long =
        dao.completePurchaseAtomic(purchase, items)

    // --- EXPENSES ---
    val allExpenses: Flow<List<ExpenseEntity>> = dao.getAllExpenses()
    fun getExpensesByTimeRange(startTime: Long, endTime: Long): Flow<List<ExpenseEntity>> =
        dao.getExpensesByTimeRange(startTime, endTime)

    suspend fun recordExpense(expense: ExpenseEntity): Long = dao.recordExpenseAtomic(expense)
    suspend fun deleteExpense(expense: ExpenseEntity) = dao.deleteExpense(expense)

    // --- PAYMENTS ---
    suspend fun recordCustomerPayment(customerId: Long, amount: Double, account: String, notes: String) =
        dao.recordCustomerPaymentAtomic(customerId, amount, account, notes)

    suspend fun recordSupplierPayment(supplierId: Long, amount: Double, account: String, notes: String) =
        dao.recordSupplierPaymentAtomic(supplierId, amount, account, notes)

    // --- STOCK MANAGEMENT ---
    val allStockTransactions: Flow<List<StockTransactionEntity>> = dao.getAllStockTransactions()
    fun getStockTransactionsForProduct(productId: Long): Flow<List<StockTransactionEntity>> =
        dao.getStockTransactionsForProduct(productId)

    suspend fun recordStockAdjustment(productId: Long, diff: Double, reason: String, type: String) =
        dao.recordStockAdjustmentAtomic(productId, diff, reason, type)

    // --- CASH & BANK ACCOUNTS ---
    val allAccounts: Flow<List<CashAccountEntity>> = dao.getAllAccounts()
    val allAccountTransactions: Flow<List<AccountTransactionEntity>> = dao.getAllAccountTransactions()
    suspend fun insertAccount(account: CashAccountEntity): Long = dao.insertAccount(account)
    suspend fun updateAccount(account: CashAccountEntity) = dao.updateAccount(account)
    suspend fun transferAccount(fromId: Long, toId: Long, amount: Double, notes: String) =
        dao.recordAccountTransferAtomic(fromId, toId, amount, notes)

    // --- RETURNS ---
    val allSaleReturns: Flow<List<SaleReturnEntity>> = dao.getAllSaleReturns()
    val allPurchaseReturns: Flow<List<PurchaseReturnEntity>> = dao.getAllPurchaseReturns()

    // --- OTHER INCOME ---
    val allOtherIncome: Flow<List<OtherIncomeEntity>> = dao.getAllOtherIncome()
    suspend fun insertOtherIncome(income: OtherIncomeEntity): Long = dao.insertOtherIncome(income)

    // --- SETTINGS ---
    val settingsFlow: Flow<ShopSettingsEntity?> = dao.getSettingsFlow()
    suspend fun getSettingsDirect(): ShopSettingsEntity? = dao.getSettingsDirect()
    suspend fun updateSettings(settings: ShopSettingsEntity) = dao.insertOrUpdateSettings(settings)

    // --- AUDIT LOGS ---
    val allAuditLogs: Flow<List<AuditLogEntity>> = dao.getAllAuditLogs()

    // --- DEMO DATA / RESET ---
    suspend fun loadDemoData() = DemoDataLoader.loadDemoData(dao)
    suspend fun clearAllData() = dao.clearAllData()

    // Generate Invoice Number
    suspend fun generateNextInvoiceNumber(): String {
        val settings = dao.getSettingsDirect()
        val prefix = settings?.invoicePrefix ?: "INV-"
        val count = (System.currentTimeMillis() % 100000)
        return "$prefix${1000 + count}"
    }

    // Generate Purchase Number
    suspend fun generateNextPurchaseNumber(): String {
        val settings = dao.getSettingsDirect()
        val prefix = settings?.purchasePrefix ?: "PUR-"
        val count = (System.currentTimeMillis() % 100000)
        return "$prefix${5000 + count}"
    }
}
