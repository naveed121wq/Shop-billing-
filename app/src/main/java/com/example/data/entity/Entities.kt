package com.example.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "products",
    indices = [
        Index(value = ["barcode"], unique = false),
        Index(value = ["sku"], unique = false),
        Index(value = ["category"])
    ]
)
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val sku: String = "",
    val barcode: String = "",
    val category: String = "General",
    val subcategory: String = "",
    val brand: String = "",
    val unit: String = "Piece", // Piece, Box, Pack, Kg, Gram, Liter, Meter, Dozen, etc.
    val purchasePrice: Double = 0.0,
    val salePrice: Double = 0.0,
    val wholesalePrice: Double = 0.0,
    val retailPrice: Double = 0.0,
    val minSalePrice: Double = 0.0,
    val currentStock: Double = 0.0,
    val minStockLevel: Double = 5.0,
    val maxStockLevel: Double = 1000.0,
    val supplierName: String = "",
    val description: String = "",
    val locationRack: String = "",
    val expiryDate: String = "",
    val batchNumber: String = "",
    val isActive: Boolean = true,
    val imageUri: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String = "",
    val whatsapp: String = "",
    val address: String = "",
    val openingBalance: Double = 0.0, // positive means customer owes shop (udhaar)
    val currentBalance: Double = 0.0,
    val creditLimit: Double = 50000.0,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "suppliers")
data class SupplierEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String = "",
    val whatsapp: String = "",
    val address: String = "",
    val openingBalance: Double = 0.0, // positive means shop owes supplier (payable)
    val currentBalance: Double = 0.0,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "sales",
    indices = [
        Index(value = ["invoiceNumber"], unique = true),
        Index(value = ["customerId"]),
        Index(value = ["dateDayString"])
    ]
)
data class SaleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceNumber: String,
    val customerId: Long? = null,
    val customerName: String = "Walk-in Customer",
    val customerPhone: String = "",
    val subtotal: Double = 0.0,
    val discountAmount: Double = 0.0,
    val discountPercentage: Double = 0.0,
    val taxAmount: Double = 0.0,
    val additionalCharges: Double = 0.0,
    val roundOff: Double = 0.0,
    val grandTotal: Double = 0.0,
    val paidAmount: Double = 0.0,
    val remainingAmount: Double = 0.0, // credit/udhaar
    val paymentMethod: String = "Cash", // Cash, Bank, JazzCash, Easypaisa, Card, Udhaar
    val paymentAccount: String = "Cash in Hand",
    val costTotal: Double = 0.0, // COGS for accurate gross profit
    val notes: String = "",
    val status: String = "COMPLETED", // COMPLETED, HELD, RETURNED, CANCELLED
    val createdAt: Long = System.currentTimeMillis(),
    val dateDayString: String = "" // YYYY-MM-DD
)

@Entity(
    tableName = "sale_items",
    indices = [
        Index(value = ["saleId"]),
        Index(value = ["productId"])
    ]
)
data class SaleItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val saleId: Long,
    val productId: Long,
    val productName: String,
    val barcode: String = "",
    val unit: String = "Piece",
    val quantity: Double,
    val unitPrice: Double,
    val purchaseCost: Double = 0.0, // cost price at time of sale
    val discount: Double = 0.0,
    val lineTotal: Double
)

@Entity(
    tableName = "purchases",
    indices = [
        Index(value = ["invoiceNumber"], unique = true),
        Index(value = ["supplierId"]),
        Index(value = ["dateDayString"])
    ]
)
data class PurchaseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceNumber: String,
    val supplierId: Long? = null,
    val supplierName: String = "",
    val subtotal: Double = 0.0,
    val discountAmount: Double = 0.0,
    val taxAmount: Double = 0.0,
    val additionalCharges: Double = 0.0,
    val grandTotal: Double = 0.0,
    val paidAmount: Double = 0.0,
    val remainingAmount: Double = 0.0,
    val paymentMethod: String = "Cash",
    val paymentAccount: String = "Cash in Hand",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val dateDayString: String = ""
)

@Entity(
    tableName = "purchase_items",
    indices = [
        Index(value = ["purchaseId"]),
        Index(value = ["productId"])
    ]
)
data class PurchaseItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val purchaseId: Long,
    val productId: Long,
    val productName: String,
    val unit: String = "Piece",
    val quantity: Double,
    val unitPrice: Double,
    val lineTotal: Double
)

@Entity(
    tableName = "expenses",
    indices = [
        Index(value = ["category"]),
        Index(value = ["dateDayString"])
    ]
)
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String, // Rent, Electricity, Salary, Transport, Tea/Food, Maintenance, etc.
    val amount: Double,
    val paymentMethod: String = "Cash",
    val paymentAccount: String = "Cash in Hand",
    val description: String = "",
    val notes: String = "",
    val receiptUri: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val dateDayString: String = ""
)

@Entity(
    tableName = "customer_ledger",
    indices = [
        Index(value = ["customerId"]),
        Index(value = ["createdAt"])
    ]
)
data class CustomerLedgerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerId: Long,
    val referenceType: String, // SALE, PAYMENT, RETURN, ADJUSTMENT
    val referenceId: String = "",
    val debit: Double = 0.0, // Sale on credit / amount added to udhaar
    val credit: Double = 0.0, // Payment received / discount / return
    val runningBalance: Double,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "supplier_ledger",
    indices = [
        Index(value = ["supplierId"]),
        Index(value = ["createdAt"])
    ]
)
data class SupplierLedgerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val supplierId: Long,
    val referenceType: String, // PURCHASE, PAYMENT, RETURN, ADJUSTMENT
    val referenceId: String = "",
    val debit: Double = 0.0, // Payment made to supplier
    val credit: Double = 0.0, // Purchase on credit / bill amount
    val runningBalance: Double,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "stock_transactions",
    indices = [
        Index(value = ["productId"]),
        Index(value = ["createdAt"])
    ]
)
data class StockTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productId: Long,
    val productName: String,
    val transactionType: String, // SALE, PURCHASE, SALE_RETURN, PURCHASE_RETURN, ADJUSTMENT, DAMAGED, LOST, OPENING
    val quantityChanged: Double, // positive or negative
    val previousStock: Double,
    val newStock: Double,
    val reason: String = "",
    val referenceId: String = "",
    val userName: String = "Owner",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "cash_accounts")
data class CashAccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String, // "Cash in Hand", "Meezan Bank", "JazzCash", "Easypaisa", "HBL"
    val balance: Double = 0.0,
    val accountNumber: String = "",
    val notes: String = ""
)

@Entity(
    tableName = "account_transactions",
    indices = [Index(value = ["accountId"])]
)
data class AccountTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val accountId: Long,
    val accountName: String,
    val transactionType: String, // SALE, PURCHASE, EXPENSE, CUSTOMER_PAYMENT, SUPPLIER_PAYMENT, TRANSFER_IN, TRANSFER_OUT, OTHER_INCOME
    val amount: Double,
    val reference: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "sale_returns")
data class SaleReturnEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val returnInvoiceNumber: String,
    val originalSaleId: Long,
    val customerId: Long? = null,
    val customerName: String = "",
    val refundAmount: Double = 0.0,
    val paymentMethod: String = "Cash",
    val paymentAccount: String = "Cash in Hand",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "sale_return_items")
data class SaleReturnItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val returnId: Long,
    val productId: Long,
    val productName: String,
    val quantity: Double,
    val unitPrice: Double,
    val refundTotal: Double
)

@Entity(tableName = "purchase_returns")
data class PurchaseReturnEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val returnInvoiceNumber: String,
    val originalPurchaseId: Long,
    val supplierId: Long? = null,
    val supplierName: String = "",
    val refundAmount: Double = 0.0,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "purchase_return_items")
data class PurchaseReturnItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val returnId: Long,
    val productId: Long,
    val productName: String,
    val quantity: Double,
    val unitPrice: Double,
    val refundTotal: Double
)

@Entity(tableName = "other_income")
data class OtherIncomeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val source: String, // Commission, Service Charge, Delivery Fee, Scrap Sale
    val amount: Double,
    val paymentAccount: String = "Cash in Hand",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val dateDayString: String = ""
)

@Entity(tableName = "shop_settings")
data class ShopSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val shopName: String = "Shop Hisab Kitab",
    val shopTagline: String = "دکان کا مکمل حساب کتاب",
    val ownerName: String = "Owner",
    val phone: String = "+92 300 1234567",
    val whatsapp: String = "+92 300 1234567",
    val address: String = "Main Bazar, Lahore, Pakistan",
    val logoUri: String = "",
    val currencySymbol: String = "PKR",
    val invoicePrefix: String = "INV-",
    val purchasePrefix: String = "PUR-",
    val allowNegativeStock: Boolean = false,
    val lowStockThreshold: Double = 5.0,
    val defaultLanguage: String = "en", // "en" or "ur"
    val appPin: String = "",
    val isSecurityEnabled: Boolean = false,
    val showCostToStaff: Boolean = true,
    val firstRunCompleted: Boolean = false,
    val openingCash: Double = 0.0,
    val thermalPrinterWidth: Int = 80 // 58 or 80 mm
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userName: String = "Owner",
    val action: String,
    val entityType: String,
    val entityId: String,
    val oldValue: String = "",
    val newValue: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
