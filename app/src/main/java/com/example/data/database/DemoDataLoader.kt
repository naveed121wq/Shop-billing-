package com.example.data.database

import com.example.data.dao.ShopDao
import com.example.data.entity.*
import java.text.SimpleDateFormat
import java.util.*

object DemoDataLoader {

    suspend fun loadDemoData(dao: ShopDao) {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val now = System.currentTimeMillis()
        val oneDayMs = 24 * 60 * 60 * 1000L

        // 1. Products
        val products = listOf(
            ProductEntity(
                name = "Tapal Danedar Tea 450g",
                sku = "TAP-001",
                barcode = "896101111222",
                category = "Groceries",
                brand = "Tapal",
                unit = "Pack",
                purchasePrice = 580.0,
                salePrice = 650.0,
                retailPrice = 650.0,
                wholesalePrice = 620.0,
                currentStock = 45.0,
                minStockLevel = 10.0,
                supplierName = "Lahore Wholesale FMCG",
                locationRack = "Aisle 1 - Shelf B"
            ),
            ProductEntity(
                name = "Dalda Cooking Oil 5L Pouch",
                sku = "DAL-005",
                barcode = "896102222333",
                category = "Cooking Oil",
                brand = "Dalda",
                unit = "Pouch",
                purchasePrice = 2450.0,
                salePrice = 2650.0,
                retailPrice = 2650.0,
                wholesalePrice = 2550.0,
                currentStock = 18.0,
                minStockLevel = 5.0,
                supplierName = "Metro Cash & Carry Supplier",
                locationRack = "Rack 4"
            ),
            ProductEntity(
                name = "Shan Bombay Biryani Masala 50g",
                sku = "SHN-010",
                barcode = "896103333444",
                category = "Spices",
                brand = "Shan",
                unit = "Box",
                purchasePrice = 95.0,
                salePrice = 120.0,
                retailPrice = 120.0,
                wholesalePrice = 105.0,
                currentStock = 85.0,
                minStockLevel = 20.0,
                supplierName = "Lahore Wholesale FMCG",
                locationRack = "Spice Counter"
            ),
            ProductEntity(
                name = "Olper's Full Cream Milk 1L",
                sku = "OLP-001",
                barcode = "896104444555",
                category = "Dairy",
                brand = "Engro",
                unit = "Pack",
                purchasePrice = 260.0,
                salePrice = 290.0,
                retailPrice = 290.0,
                wholesalePrice = 275.0,
                currentStock = 3.0, // Low stock on purpose
                minStockLevel = 10.0,
                supplierName = "Engro Foods Agency",
                locationRack = "Refrigerator"
            ),
            ProductEntity(
                name = "Rooh Afza Syrup 800ml",
                sku = "ROH-008",
                barcode = "896105555666",
                category = "Beverages",
                brand = "Hamdard",
                unit = "Bottle",
                purchasePrice = 360.0,
                salePrice = 420.0,
                retailPrice = 420.0,
                wholesalePrice = 390.0,
                currentStock = 0.0, // Out of stock on purpose
                minStockLevel = 8.0,
                supplierName = "Hamdard Distributor",
                locationRack = "Beverage Bay"
            ),
            ProductEntity(
                name = "Guard Supreme Super Basmati Rice 5kg",
                sku = "GUR-005",
                barcode = "896106666777",
                category = "Grains",
                brand = "Guard",
                unit = "Bag",
                purchasePrice = 1850.0,
                salePrice = 2100.0,
                retailPrice = 2100.0,
                wholesalePrice = 1980.0,
                currentStock = 24.0,
                minStockLevel = 6.0,
                supplierName = "Punjab Grains Mandi",
                locationRack = "Ground Stack 1"
            ),
            ProductEntity(
                name = "Surf Excel Detergent Powder 1kg",
                sku = "SRF-001",
                barcode = "896107777888",
                category = "Household",
                brand = "Unilever",
                unit = "Pack",
                purchasePrice = 520.0,
                salePrice = 590.0,
                retailPrice = 590.0,
                wholesalePrice = 550.0,
                currentStock = 32.0,
                minStockLevel = 12.0,
                supplierName = "Lahore Wholesale FMCG",
                locationRack = "Shelf C3"
            ),
            ProductEntity(
                name = "National Tomato Ketchup 500g",
                sku = "NAT-005",
                barcode = "896108888999",
                category = "Sauces",
                brand = "National",
                unit = "Pouch",
                purchasePrice = 220.0,
                salePrice = 260.0,
                retailPrice = 260.0,
                wholesalePrice = 240.0,
                currentStock = 20.0,
                minStockLevel = 5.0,
                supplierName = "National Foods Distributor",
                locationRack = "Aisle 2"
            )
        )
        dao.insertProducts(products)

        // 2. Customers
        val customers = listOf(
            CustomerEntity(
                name = "Chaudhry Tariq Mehmood",
                phone = "0301-7654321",
                whatsapp = "+923017654321",
                address = "House # 45, Street 3, Model Town",
                openingBalance = 8500.0,
                currentBalance = 12400.0,
                creditLimit = 30000.0,
                notes = "Regular monthly family khata"
            ),
            CustomerEntity(
                name = "Malik Aslam",
                phone = "0321-4567890",
                whatsapp = "+923214567890",
                address = "Shop # 5, Timber Market",
                openingBalance = 0.0,
                currentBalance = 4500.0,
                creditLimit = 20000.0,
                notes = "Clears account weekly"
            ),
            CustomerEntity(
                name = "Dr. Farooq Ahmed",
                phone = "0333-9876543",
                whatsapp = "+923339876543",
                address = "Farooq Clinic, Main Road",
                openingBalance = 0.0,
                currentBalance = 0.0,
                creditLimit = 50000.0,
                notes = "Pays via JazzCash"
            ),
            CustomerEntity(
                name = "Haji Abdul Rasheed",
                phone = "0345-1122334",
                whatsapp = "+923451122334",
                address = "Bazar Kalan, Lahore",
                openingBalance = 15000.0,
                currentBalance = 18200.0,
                creditLimit = 40000.0,
                notes = "Trusted neighbor"
            )
        )
        dao.insertCustomers(customers)

        // 3. Suppliers
        val suppliers = listOf(
            SupplierEntity(
                name = "Lahore Wholesale FMCG Agency",
                phone = "0300-9988776",
                whatsapp = "+923009988776",
                address = "Akbari Mandi, Lahore",
                openingBalance = 25000.0,
                currentBalance = 42000.0,
                notes = "Main distributor for tea and soaps"
            ),
            SupplierEntity(
                name = "Punjab Grains & Rice Mandi",
                phone = "0322-8877665",
                whatsapp = "+923228877665",
                address = "Grain Market, Kamoke",
                openingBalance = 15000.0,
                currentBalance = 20000.0,
                notes = "Basmati Rice and Pulses supplier"
            ),
            SupplierEntity(
                name = "Engro Foods Agency",
                phone = "0334-7766554",
                whatsapp = "+923347766554",
                address = "Gulberg III, Lahore",
                openingBalance = 0.0,
                currentBalance = 12500.0,
                notes = "Dairy products daily delivery"
            )
        )
        dao.insertSuppliers(suppliers)

        // 4. Sample Completed Sales
        val sale1 = SaleEntity(
            invoiceNumber = "INV-1001",
            customerId = 1,
            customerName = "Chaudhry Tariq Mehmood",
            customerPhone = "0301-7654321",
            subtotal = 3900.0,
            discountAmount = 100.0,
            grandTotal = 3800.0,
            paidAmount = 1000.0,
            remainingAmount = 2800.0,
            paymentMethod = "Udhaar",
            paymentAccount = "Cash in Hand",
            costTotal = 3280.0,
            status = "COMPLETED",
            createdAt = now - (3 * 3600 * 1000L),
            dateDayString = todayStr
        )
        val saleId1 = dao.insertSale(sale1)
        dao.insertSaleItems(
            listOf(
                SaleItemEntity(
                    saleId = saleId1,
                    productId = 1,
                    productName = "Tapal Danedar Tea 450g",
                    barcode = "896101111222",
                    unit = "Pack",
                    quantity = 2.0,
                    unitPrice = 650.0,
                    purchaseCost = 580.0,
                    lineTotal = 1300.0
                ),
                SaleItemEntity(
                    saleId = saleId1,
                    productId = 2,
                    productName = "Dalda Cooking Oil 5L Pouch",
                    barcode = "896102222333",
                    unit = "Pouch",
                    quantity = 1.0,
                    unitPrice = 2650.0,
                    purchaseCost = 2450.0,
                    discount = 50.0,
                    lineTotal = 2600.0
                )
            )
        )

        val sale2 = SaleEntity(
            invoiceNumber = "INV-1002",
            customerName = "Walk-in Customer",
            subtotal = 1460.0,
            discountAmount = 0.0,
            grandTotal = 1460.0,
            paidAmount = 1460.0,
            remainingAmount = 0.0,
            paymentMethod = "Cash",
            paymentAccount = "Cash in Hand",
            costTotal = 1255.0,
            status = "COMPLETED",
            createdAt = now - (1 * 3600 * 1000L),
            dateDayString = todayStr
        )
        val saleId2 = dao.insertSale(sale2)
        dao.insertSaleItems(
            listOf(
                SaleItemEntity(
                    saleId = saleId2,
                    productId = 4,
                    productName = "Olper's Full Cream Milk 1L",
                    barcode = "896104444555",
                    unit = "Pack",
                    quantity = 3.0,
                    unitPrice = 290.0,
                    purchaseCost = 260.0,
                    lineTotal = 870.0
                ),
                SaleItemEntity(
                    saleId = saleId2,
                    productId = 7,
                    productName = "Surf Excel Detergent Powder 1kg",
                    barcode = "896107777888",
                    unit = "Pack",
                    quantity = 1.0,
                    unitPrice = 590.0,
                    purchaseCost = 520.0,
                    lineTotal = 590.0
                )
            )
        )

        // 5. Sample Expenses
        dao.insertExpense(
            ExpenseEntity(
                category = "Electricity",
                amount = 3200.0,
                paymentMethod = "Bank",
                paymentAccount = "Meezan Bank",
                description = "Shop LESCO Bill payment for current month",
                createdAt = now - (5 * 3600 * 1000L),
                dateDayString = todayStr
            )
        )
        dao.insertExpense(
            ExpenseEntity(
                category = "Tea/Food",
                amount = 450.0,
                paymentMethod = "Cash",
                paymentAccount = "Cash in Hand",
                description = "Daily staff afternoon tea and biscuits",
                createdAt = now - (2 * 3600 * 1000L),
                dateDayString = todayStr
            )
        )

        // 6. Customer Ledger Entry
        dao.insertCustomerLedger(
            CustomerLedgerEntity(
                customerId = 1,
                referenceType = "SALE",
                referenceId = "INV-1001",
                debit = 2800.0,
                credit = 0.0,
                runningBalance = 12400.0,
                notes = "Credit balance from Invoice #INV-1001",
                createdAt = now - (3 * 3600 * 1000L)
            )
        )
    }
}
