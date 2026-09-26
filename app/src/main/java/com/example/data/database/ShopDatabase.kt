package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.ShopDao
import com.example.data.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ProductEntity::class,
        CustomerEntity::class,
        SupplierEntity::class,
        SaleEntity::class,
        SaleItemEntity::class,
        PurchaseEntity::class,
        PurchaseItemEntity::class,
        ExpenseEntity::class,
        CustomerLedgerEntity::class,
        SupplierLedgerEntity::class,
        StockTransactionEntity::class,
        CashAccountEntity::class,
        AccountTransactionEntity::class,
        SaleReturnEntity::class,
        SaleReturnItemEntity::class,
        PurchaseReturnEntity::class,
        PurchaseReturnItemEntity::class,
        OtherIncomeEntity::class,
        ShopSettingsEntity::class,
        AuditLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ShopDatabase : RoomDatabase() {

    abstract fun shopDao(): ShopDao

    companion object {
        @Volatile
        private var INSTANCE: ShopDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): ShopDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ShopDatabase::class.java,
                    "shop_hisab_kitab.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateDefaultData(database.shopDao())
                    }
                }
            }
        }

        suspend fun populateDefaultData(dao: ShopDao) {
            // Default Settings
            dao.insertOrUpdateSettings(
                ShopSettingsEntity(
                    id = 1,
                    shopName = "Shop Hisab Kitab",
                    shopTagline = "دکان کا بااعتماد حساب کتاب",
                    ownerName = "Muhammad Naveed",
                    phone = "+92 300 1234567",
                    whatsapp = "+92 300 1234567",
                    address = "Shop # 12, Main Market, Lahore, Pakistan",
                    currencySymbol = "PKR",
                    invoicePrefix = "INV-",
                    purchasePrefix = "PUR-",
                    allowNegativeStock = false,
                    lowStockThreshold = 5.0,
                    defaultLanguage = "en",
                    firstRunCompleted = false
                )
            )

            // Default Payment Accounts
            val defaultAccounts = listOf(
                CashAccountEntity(name = "Cash in Hand", balance = 25000.0, accountNumber = "Cash Drawer"),
                CashAccountEntity(name = "Meezan Bank", balance = 150000.0, accountNumber = "PK92MEZN0001234567"),
                CashAccountEntity(name = "JazzCash", balance = 35000.0, accountNumber = "03001234567"),
                CashAccountEntity(name = "Easypaisa", balance = 20000.0, accountNumber = "03451234567")
            )
            dao.insertAccounts(defaultAccounts)
        }
    }
}
