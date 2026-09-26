package com.example.data.dao

import androidx.room.*
import com.example.data.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ShopDao {

    // --- PRODUCTS ---
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE isActive = 1 ORDER BY name ASC")
    fun getActiveProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id")
    fun getProductById(id: Long): Flow<ProductEntity?>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductByIdDirect(id: Long): ProductEntity?

    @Query("SELECT * FROM products WHERE barcode = :barcode LIMIT 1")
    suspend fun getProductByBarcode(barcode: String): ProductEntity?

    @Query("SELECT * FROM products WHERE name LIKE '%' || :query || '%' OR barcode LIKE '%' || :query || '%' OR sku LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%'")
    fun searchProducts(query: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE currentStock <= minStockLevel AND currentStock > 0")
    fun getLowStockProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE currentStock <= 0")
    fun getOutOfStockProducts(): Flow<List<ProductEntity>>

    @Query("SELECT DISTINCT category FROM products WHERE category != '' ORDER BY category ASC")
    fun getCategories(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Delete
    suspend fun deleteProduct(product: ProductEntity)

    @Query("SELECT COUNT(*) FROM products")
    suspend fun getProductCount(): Int

    // --- CUSTOMERS ---
    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getAllCustomers(): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE id = :id")
    fun getCustomerById(id: Long): Flow<CustomerEntity?>

    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getCustomerByIdDirect(id: Long): CustomerEntity?

    @Query("SELECT * FROM customers WHERE name LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%'")
    fun searchCustomers(query: String): Flow<List<CustomerEntity>>

    @Query("SELECT SUM(currentBalance) FROM customers WHERE currentBalance > 0")
    fun getTotalCustomerReceivables(): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomers(customers: List<CustomerEntity>)

    @Update
    suspend fun updateCustomer(customer: CustomerEntity)

    @Delete
    suspend fun deleteCustomer(customer: CustomerEntity)

    // --- SUPPLIERS ---
    @Query("SELECT * FROM suppliers ORDER BY name ASC")
    fun getAllSuppliers(): Flow<List<SupplierEntity>>

    @Query("SELECT * FROM suppliers WHERE id = :id")
    fun getSupplierById(id: Long): Flow<SupplierEntity?>

    @Query("SELECT * FROM suppliers WHERE id = :id")
    suspend fun getSupplierByIdDirect(id: Long): SupplierEntity?

    @Query("SELECT * FROM suppliers WHERE name LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%'")
    fun searchSuppliers(query: String): Flow<List<SupplierEntity>>

    @Query("SELECT SUM(currentBalance) FROM suppliers WHERE currentBalance > 0")
    fun getTotalSupplierPayables(): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplier(supplier: SupplierEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSuppliers(suppliers: List<SupplierEntity>)

    @Update
    suspend fun updateSupplier(supplier: SupplierEntity)

    @Delete
    suspend fun deleteSupplier(supplier: SupplierEntity)

    // --- SALES ---
    @Query("SELECT * FROM sales ORDER BY createdAt DESC")
    fun getAllSales(): Flow<List<SaleEntity>>

    @Query("SELECT * FROM sales WHERE id = :id")
    fun getSaleById(id: Long): Flow<SaleEntity?>

    @Query("SELECT * FROM sales WHERE id = :id")
    suspend fun getSaleByIdDirect(id: Long): SaleEntity?

    @Query("SELECT * FROM sales WHERE invoiceNumber = :invoiceNumber LIMIT 1")
    suspend fun getSaleByInvoiceNumber(invoiceNumber: String): SaleEntity?

    @Query("SELECT * FROM sales WHERE createdAt >= :startTime AND createdAt <= :endTime ORDER BY createdAt DESC")
    fun getSalesByTimeRange(startTime: Long, endTime: Long): Flow<List<SaleEntity>>

    @Query("SELECT * FROM sales WHERE dateDayString = :dayString ORDER BY createdAt DESC")
    fun getSalesByDayString(dayString: String): Flow<List<SaleEntity>>

    @Query("SELECT * FROM sales WHERE customerId = :customerId ORDER BY createdAt DESC")
    fun getSalesByCustomer(customerId: Long): Flow<List<SaleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: SaleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaleItems(items: List<SaleItemEntity>)

    @Query("SELECT * FROM sale_items WHERE saleId = :saleId")
    fun getSaleItems(saleId: Long): Flow<List<SaleItemEntity>>

    @Query("SELECT * FROM sale_items WHERE saleId = :saleId")
    suspend fun getSaleItemsDirect(saleId: Long): List<SaleItemEntity>

    @Query("SELECT * FROM sale_items ORDER BY id DESC")
    fun getAllSaleItems(): Flow<List<SaleItemEntity>>

    // --- PURCHASES ---
    @Query("SELECT * FROM purchases ORDER BY createdAt DESC")
    fun getAllPurchases(): Flow<List<PurchaseEntity>>

    @Query("SELECT * FROM purchases WHERE id = :id")
    fun getPurchaseById(id: Long): Flow<PurchaseEntity?>

    @Query("SELECT * FROM purchases WHERE id = :id")
    suspend fun getPurchaseByIdDirect(id: Long): PurchaseEntity?

    @Query("SELECT * FROM purchases WHERE createdAt >= :startTime AND createdAt <= :endTime ORDER BY createdAt DESC")
    fun getPurchasesByTimeRange(startTime: Long, endTime: Long): Flow<List<PurchaseEntity>>

    @Query("SELECT * FROM purchases WHERE supplierId = :supplierId ORDER BY createdAt DESC")
    fun getPurchasesBySupplier(supplierId: Long): Flow<List<PurchaseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchase(purchase: PurchaseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchaseItems(items: List<PurchaseItemEntity>)

    @Query("SELECT * FROM purchase_items WHERE purchaseId = :purchaseId")
    fun getPurchaseItems(purchaseId: Long): Flow<List<PurchaseItemEntity>>

    @Query("SELECT * FROM purchase_items WHERE purchaseId = :purchaseId")
    suspend fun getPurchaseItemsDirect(purchaseId: Long): List<PurchaseItemEntity>

    // --- EXPENSES ---
    @Query("SELECT * FROM expenses ORDER BY createdAt DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE createdAt >= :startTime AND createdAt <= :endTime ORDER BY createdAt DESC")
    fun getExpensesByTimeRange(startTime: Long, endTime: Long): Flow<List<ExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity): Long

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)

    // --- LEDGERS ---
    @Query("SELECT * FROM customer_ledger WHERE customerId = :customerId ORDER BY createdAt ASC")
    fun getCustomerLedger(customerId: Long): Flow<List<CustomerLedgerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomerLedger(entry: CustomerLedgerEntity): Long

    @Query("SELECT * FROM supplier_ledger WHERE supplierId = :supplierId ORDER BY createdAt ASC")
    fun getSupplierLedger(supplierId: Long): Flow<List<SupplierLedgerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplierLedger(entry: SupplierLedgerEntity): Long

    // --- STOCK TRANSACTIONS ---
    @Query("SELECT * FROM stock_transactions ORDER BY createdAt DESC")
    fun getAllStockTransactions(): Flow<List<StockTransactionEntity>>

    @Query("SELECT * FROM stock_transactions WHERE productId = :productId ORDER BY createdAt DESC")
    fun getStockTransactionsForProduct(productId: Long): Flow<List<StockTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStockTransaction(tx: StockTransactionEntity): Long

    // --- CASH ACCOUNTS ---
    @Query("SELECT * FROM cash_accounts ORDER BY id ASC")
    fun getAllAccounts(): Flow<List<CashAccountEntity>>

    @Query("SELECT * FROM cash_accounts WHERE name = :name LIMIT 1")
    suspend fun getAccountByName(name: String): CashAccountEntity?

    @Query("SELECT * FROM cash_accounts WHERE id = :id LIMIT 1")
    suspend fun getAccountById(id: Long): CashAccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: CashAccountEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccounts(accounts: List<CashAccountEntity>)

    @Update
    suspend fun updateAccount(account: CashAccountEntity)

    @Query("SELECT * FROM account_transactions ORDER BY createdAt DESC")
    fun getAllAccountTransactions(): Flow<List<AccountTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccountTransaction(tx: AccountTransactionEntity): Long

    // --- RETURNS ---
    @Query("SELECT * FROM sale_returns ORDER BY createdAt DESC")
    fun getAllSaleReturns(): Flow<List<SaleReturnEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaleReturn(saleReturn: SaleReturnEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaleReturnItems(items: List<SaleReturnItemEntity>)

    @Query("SELECT * FROM sale_return_items WHERE returnId = :returnId")
    fun getSaleReturnItems(returnId: Long): Flow<List<SaleReturnItemEntity>>

    @Query("SELECT * FROM purchase_returns ORDER BY createdAt DESC")
    fun getAllPurchaseReturns(): Flow<List<PurchaseReturnEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchaseReturn(purchaseReturn: PurchaseReturnEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchaseReturnItems(items: List<PurchaseReturnItemEntity>)

    @Query("SELECT * FROM purchase_return_items WHERE returnId = :returnId")
    fun getPurchaseReturnItems(returnId: Long): Flow<List<PurchaseReturnItemEntity>>

    // --- OTHER INCOME ---
    @Query("SELECT * FROM other_income ORDER BY createdAt DESC")
    fun getAllOtherIncome(): Flow<List<OtherIncomeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOtherIncome(income: OtherIncomeEntity): Long

    // --- SETTINGS ---
    @Query("SELECT * FROM shop_settings WHERE id = 1 LIMIT 1")
    fun getSettingsFlow(): Flow<ShopSettingsEntity?>

    @Query("SELECT * FROM shop_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettingsDirect(): ShopSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSettings(settings: ShopSettingsEntity)

    // --- AUDIT LOGS ---
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC")
    fun getAllAuditLogs(): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLogEntity): Long

    // --- ATOMIC TRANSACTION METHODS ---

    @Transaction
    suspend fun completeSaleAtomic(
        sale: SaleEntity,
        items: List<SaleItemEntity>
    ): Long {
        // 1. Insert Sale
        val saleId = insertSale(sale)

        // 2. Insert items with saleId
        val updatedItems = items.map { it.copy(saleId = saleId) }
        insertSaleItems(updatedItems)

        // 3. Deduct stock for each product & log stock transaction
        for (item in items) {
            val product = getProductByIdDirect(item.productId)
            if (product != null) {
                val newStock = product.currentStock - item.quantity
                updateProduct(product.copy(currentStock = newStock, updatedAt = System.currentTimeMillis()))
                insertStockTransaction(
                    StockTransactionEntity(
                        productId = item.productId,
                        productName = item.productName,
                        transactionType = "SALE",
                        quantityChanged = -item.quantity,
                        previousStock = product.currentStock,
                        newStock = newStock,
                        reason = "Sale Invoice #${sale.invoiceNumber}",
                        referenceId = sale.invoiceNumber
                    )
                )
            }
        }

        // 4. Update Customer Ledger if credit or customer selected
        if (sale.customerId != null && sale.customerId > 0) {
            val customer = getCustomerByIdDirect(sale.customerId)
            if (customer != null) {
                val newBalance = customer.currentBalance + sale.remainingAmount
                updateCustomer(customer.copy(currentBalance = newBalance))

                // Log debit for credit amount or sale
                if (sale.remainingAmount > 0) {
                    insertCustomerLedger(
                        CustomerLedgerEntity(
                            customerId = customer.id,
                            referenceType = "SALE",
                            referenceId = sale.invoiceNumber,
                            debit = sale.remainingAmount,
                            credit = 0.0,
                            runningBalance = newBalance,
                            notes = "Udhaar/Credit from #${sale.invoiceNumber}"
                        )
                    )
                }
            }
        }

        // 5. Update Cash Account if paidAmount > 0
        if (sale.paidAmount > 0) {
            var account = getAccountByName(sale.paymentAccount)
            if (account == null) {
                account = getAccountByName("Cash in Hand")
            }
            if (account != null) {
                val newBal = account.balance + sale.paidAmount
                updateAccount(account.copy(balance = newBal))
                insertAccountTransaction(
                    AccountTransactionEntity(
                        accountId = account.id,
                        accountName = account.name,
                        transactionType = "SALE",
                        amount = sale.paidAmount,
                        reference = sale.invoiceNumber,
                        notes = "Sale received: ${sale.invoiceNumber}"
                    )
                )
            }
        }

        // 6. Audit Log
        insertAuditLog(
            AuditLogEntity(
                action = "CREATE_SALE",
                entityType = "SALE",
                entityId = sale.invoiceNumber,
                newValue = "Total: ${sale.grandTotal}, Paid: ${sale.paidAmount}"
            )
        )

        return saleId
    }

    @Transaction
    suspend fun completePurchaseAtomic(
        purchase: PurchaseEntity,
        items: List<PurchaseItemEntity>
    ): Long {
        // 1. Insert Purchase
        val purchaseId = insertPurchase(purchase)

        // 2. Insert items
        val updatedItems = items.map { it.copy(purchaseId = purchaseId) }
        insertPurchaseItems(updatedItems)

        // 3. Increase Stock
        for (item in items) {
            val product = getProductByIdDirect(item.productId)
            if (product != null) {
                val newStock = product.currentStock + item.quantity
                // Update purchasePrice if changed
                updateProduct(
                    product.copy(
                        currentStock = newStock,
                        purchasePrice = item.unitPrice,
                        updatedAt = System.currentTimeMillis()
                    )
                )
                insertStockTransaction(
                    StockTransactionEntity(
                        productId = item.productId,
                        productName = item.productName,
                        transactionType = "PURCHASE",
                        quantityChanged = item.quantity,
                        previousStock = product.currentStock,
                        newStock = newStock,
                        reason = "Purchase Invoice #${purchase.invoiceNumber}",
                        referenceId = purchase.invoiceNumber
                    )
                )
            }
        }

        // 4. Update Supplier Ledger if payable/remaining > 0
        if (purchase.supplierId != null && purchase.supplierId > 0) {
            val supplier = getSupplierByIdDirect(purchase.supplierId)
            if (supplier != null) {
                val newBalance = supplier.currentBalance + purchase.remainingAmount
                updateSupplier(supplier.copy(currentBalance = newBalance))

                if (purchase.remainingAmount > 0) {
                    insertSupplierLedger(
                        SupplierLedgerEntity(
                            supplierId = supplier.id,
                            referenceType = "PURCHASE",
                            referenceId = purchase.invoiceNumber,
                            debit = 0.0,
                            credit = purchase.remainingAmount,
                            runningBalance = newBalance,
                            notes = "Credit purchase #${purchase.invoiceNumber}"
                        )
                    )
                }
            }
        }

        // 5. Deduct Cash Account if paidAmount > 0
        if (purchase.paidAmount > 0) {
            var account = getAccountByName(purchase.paymentAccount)
            if (account == null) {
                account = getAccountByName("Cash in Hand")
            }
            if (account != null) {
                val newBal = account.balance - purchase.paidAmount
                updateAccount(account.copy(balance = newBal))
                insertAccountTransaction(
                    AccountTransactionEntity(
                        accountId = account.id,
                        accountName = account.name,
                        transactionType = "PURCHASE",
                        amount = -purchase.paidAmount,
                        reference = purchase.invoiceNumber,
                        notes = "Purchase payment: ${purchase.invoiceNumber}"
                    )
                )
            }
        }

        insertAuditLog(
            AuditLogEntity(
                action = "CREATE_PURCHASE",
                entityType = "PURCHASE",
                entityId = purchase.invoiceNumber,
                newValue = "Total: ${purchase.grandTotal}, Paid: ${purchase.paidAmount}"
            )
        )

        return purchaseId
    }

    @Transaction
    suspend fun recordCustomerPaymentAtomic(
        customerId: Long,
        amount: Double,
        paymentAccount: String,
        notes: String
    ) {
        val customer = getCustomerByIdDirect(customerId) ?: return
        val newBalance = customer.currentBalance - amount
        updateCustomer(customer.copy(currentBalance = newBalance))

        insertCustomerLedger(
            CustomerLedgerEntity(
                customerId = customerId,
                referenceType = "PAYMENT",
                referenceId = "PAY-${System.currentTimeMillis() % 100000}",
                debit = 0.0,
                credit = amount,
                runningBalance = newBalance,
                notes = notes
            )
        )

        var account = getAccountByName(paymentAccount)
        if (account == null) account = getAccountByName("Cash in Hand")
        if (account != null) {
            val newBal = account.balance + amount
            updateAccount(account.copy(balance = newBal))
            insertAccountTransaction(
                AccountTransactionEntity(
                    accountId = account.id,
                    accountName = account.name,
                    transactionType = "CUSTOMER_PAYMENT",
                    amount = amount,
                    reference = "Customer: ${customer.name}",
                    notes = notes
                )
            )
        }

        insertAuditLog(
            AuditLogEntity(
                action = "CUSTOMER_PAYMENT",
                entityType = "CUSTOMER",
                entityId = customer.name,
                newValue = "Received: $amount, New Balance: $newBalance"
            )
        )
    }

    @Transaction
    suspend fun recordSupplierPaymentAtomic(
        supplierId: Long,
        amount: Double,
        paymentAccount: String,
        notes: String
    ) {
        val supplier = getSupplierByIdDirect(supplierId) ?: return
        val newBalance = supplier.currentBalance - amount
        updateSupplier(supplier.copy(currentBalance = newBalance))

        insertSupplierLedger(
            SupplierLedgerEntity(
                supplierId = supplierId,
                referenceType = "PAYMENT",
                referenceId = "PAY-${System.currentTimeMillis() % 100000}",
                debit = amount,
                credit = 0.0,
                runningBalance = newBalance,
                notes = notes
            )
        )

        var account = getAccountByName(paymentAccount)
        if (account == null) account = getAccountByName("Cash in Hand")
        if (account != null) {
            val newBal = account.balance - amount
            updateAccount(account.copy(balance = newBal))
            insertAccountTransaction(
                AccountTransactionEntity(
                    accountId = account.id,
                    accountName = account.name,
                    transactionType = "SUPPLIER_PAYMENT",
                    amount = -amount,
                    reference = "Supplier: ${supplier.name}",
                    notes = notes
                )
            )
        }

        insertAuditLog(
            AuditLogEntity(
                action = "SUPPLIER_PAYMENT",
                entityType = "SUPPLIER",
                entityId = supplier.name,
                newValue = "Paid: $amount, New Balance: $newBalance"
            )
        )
    }

    @Transaction
    suspend fun recordExpenseAtomic(expense: ExpenseEntity): Long {
        val expenseId = insertExpense(expense)
        var account = getAccountByName(expense.paymentAccount)
        if (account == null) account = getAccountByName("Cash in Hand")
        if (account != null) {
            val newBal = account.balance - expense.amount
            updateAccount(account.copy(balance = newBal))
            insertAccountTransaction(
                AccountTransactionEntity(
                    accountId = account.id,
                    accountName = account.name,
                    transactionType = "EXPENSE",
                    amount = -expense.amount,
                    reference = expense.category,
                    notes = expense.description
                )
            )
        }
        insertAuditLog(
            AuditLogEntity(
                action = "RECORD_EXPENSE",
                entityType = "EXPENSE",
                entityId = expense.category,
                newValue = "Amount: ${expense.amount}, Desc: ${expense.description}"
            )
        )
        return expenseId
    }

    @Transaction
    suspend fun recordStockAdjustmentAtomic(
        productId: Long,
        quantityDifference: Double,
        reason: String,
        type: String // ADJUSTMENT, DAMAGED, LOST
    ) {
        val product = getProductByIdDirect(productId) ?: return
        val newStock = product.currentStock + quantityDifference
        updateProduct(product.copy(currentStock = newStock, updatedAt = System.currentTimeMillis()))
        insertStockTransaction(
            StockTransactionEntity(
                productId = productId,
                productName = product.name,
                transactionType = type,
                quantityChanged = quantityDifference,
                previousStock = product.currentStock,
                newStock = newStock,
                reason = reason,
                referenceId = "ADJ-${System.currentTimeMillis() % 100000}"
            )
        )
        insertAuditLog(
            AuditLogEntity(
                action = "STOCK_ADJUSTMENT",
                entityType = "PRODUCT",
                entityId = product.name,
                oldValue = "Stock: ${product.currentStock}",
                newValue = "Stock: $newStock, Reason: $reason"
            )
        )
    }

    @Transaction
    suspend fun recordAccountTransferAtomic(
        fromAccountId: Long,
        toAccountId: Long,
        amount: Double,
        notes: String
    ) {
        val fromAccount = getAccountById(fromAccountId) ?: return
        val toAccount = getAccountById(toAccountId) ?: return

        updateAccount(fromAccount.copy(balance = fromAccount.balance - amount))
        updateAccount(toAccount.copy(balance = toAccount.balance + amount))

        insertAccountTransaction(
            AccountTransactionEntity(
                accountId = fromAccount.id,
                accountName = fromAccount.name,
                transactionType = "TRANSFER_OUT",
                amount = -amount,
                reference = "To: ${toAccount.name}",
                notes = notes
            )
        )
        insertAccountTransaction(
            AccountTransactionEntity(
                accountId = toAccount.id,
                accountName = toAccount.name,
                transactionType = "TRANSFER_IN",
                amount = amount,
                reference = "From: ${fromAccount.name}",
                notes = notes
            )
        )
    }

    @Transaction
    suspend fun clearAllData() {
        // Clear all transactional and entity tables
        deleteAllSales()
        deleteAllSaleItems()
        deleteAllPurchases()
        deleteAllPurchaseItems()
        deleteAllExpenses()
        deleteAllCustomers()
        deleteAllSuppliers()
        deleteAllProducts()
        deleteAllCustomerLedgers()
        deleteAllSupplierLedgers()
        deleteAllStockTransactions()
        deleteAllAccountTransactions()
        deleteAllSaleReturns()
        deleteAllSaleReturnItems()
        deleteAllPurchaseReturns()
        deleteAllPurchaseReturnItems()
        deleteAllOtherIncome()
        deleteAllAuditLogs()
        resetAccounts()
    }

    @Query("DELETE FROM sales")
    suspend fun deleteAllSales()

    @Query("DELETE FROM sale_items")
    suspend fun deleteAllSaleItems()

    @Query("DELETE FROM purchases")
    suspend fun deleteAllPurchases()

    @Query("DELETE FROM purchase_items")
    suspend fun deleteAllPurchaseItems()

    @Query("DELETE FROM expenses")
    suspend fun deleteAllExpenses()

    @Query("DELETE FROM customers")
    suspend fun deleteAllCustomers()

    @Query("DELETE FROM suppliers")
    suspend fun deleteAllSuppliers()

    @Query("DELETE FROM products")
    suspend fun deleteAllProducts()

    @Query("DELETE FROM customer_ledger")
    suspend fun deleteAllCustomerLedgers()

    @Query("DELETE FROM supplier_ledger")
    suspend fun deleteAllSupplierLedgers()

    @Query("DELETE FROM stock_transactions")
    suspend fun deleteAllStockTransactions()

    @Query("DELETE FROM account_transactions")
    suspend fun deleteAllAccountTransactions()

    @Query("DELETE FROM sale_returns")
    suspend fun deleteAllSaleReturns()

    @Query("DELETE FROM sale_return_items")
    suspend fun deleteAllSaleReturnItems()

    @Query("DELETE FROM purchase_returns")
    suspend fun deleteAllPurchaseReturns()

    @Query("DELETE FROM purchase_return_items")
    suspend fun deleteAllPurchaseReturnItems()

    @Query("DELETE FROM other_income")
    suspend fun deleteAllOtherIncome()

    @Query("DELETE FROM audit_logs")
    suspend fun deleteAllAuditLogs()

    @Query("UPDATE cash_accounts SET balance = 0.0")
    suspend fun resetAccounts()
}
