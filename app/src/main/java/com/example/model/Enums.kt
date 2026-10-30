package com.example.model

enum class PaymentMethod(val displayName: String) {
    CASH("Cash"),
    BANK("Bank Transfer"),
    QR("QR / Fonepay / Digital"),
    CREDIT("Udhaar (Credit)"),
    SPLIT("Split Payment")
}

enum class SaleStatus {
    COMPLETED,
    REFUNDED,
    VOID
}

enum class StaffRole(val title: String, val canManageInventory: Boolean, val canViewReports: Boolean, val canManageSettings: Boolean) {
    OWNER("Owner / Admin", true, true, true),
    MANAGER("Manager", true, true, false),
    CASHIER("Cashier (POS)", false, false, false),
    INVENTORY_STAFF("Inventory Clerk", true, false, false)
}

enum class StockReason(val displayName: String) {
    PURCHASE("Stock Purchase / Inward"),
    DAMAGE("Damaged Goods"),
    EXPIRED("Expired Stock"),
    THEFT("Inventory Loss / Theft"),
    AUDIT("Physical Count Audit Correction"),
    RESTOCK("Manual Restock")
}

enum class ExpiryStatus {
    GOOD,
    EXPIRING_SOON,
    EXPIRED,
    NO_EXPIRY
}
