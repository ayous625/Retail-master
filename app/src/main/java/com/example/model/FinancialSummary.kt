package com.example.model

data class FinancialSummary(
    val grossSales: Double = 0.0,
    val totalDiscounts: Double = 0.0,
    val totalTaxCollected: Double = 0.0,
    val totalRefunds: Double = 0.0,
    val netSales: Double = 0.0, // Gross sales - discounts - refunds
    val costOfGoodsSold: Double = 0.0, // Purchase cost of sold items
    val grossProfit: Double = 0.0, // Net sales - COGS
    val grossMarginPercent: Double = 0.0,
    val totalExpenses: Double = 0.0, // Operating expenses (rent, electricity, salaries, etc.)
    val totalOtherIncome: Double = 0.0, // Scrap, commissions, etc.
    val netOperatingProfit: Double = 0.0, // Gross profit - operating expenses + other income
    val netMarginPercent: Double = 0.0,
    val totalReceivables: Double = 0.0, // Total customer credit outstanding
    val totalPayables: Double = 0.0, // Total supplier balance outstanding
    val inventoryCostValue: Double = 0.0, // Total current stock at purchase cost
    val inventoryRetailValue: Double = 0.0, // Total current stock at retail price
    val cashInHand: Double = 0.0, // Cash collected from sales & customer payments - cash expenses/purchases
    val totalCompletedSalesCount: Int = 0,
    val lowStockCount: Int = 0,
    val expiringSoonCount: Int = 0
)

data class CartItem(
    val product: com.example.data.entity.ProductEntity,
    var quantity: Double = 1.0,
    var customPrice: Double = product.sellingPrice
) {
    val subtotal: Double
        get() = quantity * customPrice
}
