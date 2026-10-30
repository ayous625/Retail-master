# Retail Master — Complete Retail Shop Management System

An affordable, modern, user-friendly, and professional business management application designed for grocery shops, mini-marts, small retail stores, and local businesses.

Built with **Kotlin**, **Jetpack Compose (Material 3)**, and **Room Database (SQLite)**.

---

## 🌟 Key Features

### 1. Dashboard & Real-Time Business Intelligence
- Real-time KPI summary:
  - **Net Sales Revenue** & completed transaction counts
  - **Gross Profit** & Gross Margin %
  - **Estimated Net Operating Profit** (after subtracting operating expenses and adding other income)
  - **Customer Credit Outstanding (Udhaar Receivables)**
  - **Supplier Dues (Accounts Payable)**
  - **Total Inventory Valuation** (At purchase cost & expected retail value)
  - **Net Estimated Cash Flow Position**
- **Actionable Alerts**:
  - Low-stock warnings for items below threshold
  - Expiry date alerts (Expired or Expiring soon within 30 days)
- **Quick Action Pills**: Instant shortcuts to "New Sale (POS)", "Add Product", "Record Expense", and "Customer Khata".
- **Recent Invoices Feed**: Detailed clickable invoice list with immediate receipt view.

### 2. Fast Point of Sale (POS) & Billing
- Live search by product name, SKU, or barcode.
- Category filters (Staples & Grains, Cooking Essentials, Beverages, Snacks, Dairy & Bakery, Personal Care).
- Interactive shopping cart with quantity increment/decrement controls.
- Flexible pricing and discount inputs (fixed NPR reduction).
- Configurable Nepal standard 13% VAT tax switch.
- Customer account linking (Walk-in vs Registered customer).
- Multiple payment modes:
  - **Cash** (with cash tendered calculation and change return calculator)
  - **Bank Transfer**
  - **QR / Digital (Fonepay / eSewa / Khalti)**
  - **Udhaar (Customer Credit)** with automatic credit limit verification
  - **Split Payment** (Cash + Digital)
- Instant thermal bill style printable/shareable Receipt Dialog with copy-to-clipboard and WhatsApp/SMS share.
- Return & Refund support with automatic inventory restocking and credit balance reversals.

### 3. Inventory & Warehouse Management
- Comprehensive product details: Name, SKU, Barcode, Category, Unit (pcs, kg, bag, bottle, pkt, etc.), Cost Price, Retail Price, Available Stock, Min Stock Threshold, Expiry Date.
- Inventory Valuation summary banner (Cost Value, Retail Value, Potential Gross Profit).
- Add Product dialog with duplicate SKU validation and input checks.
- Stock Adjustments with reasons:
  - Stock Restock / Inward
  - Damaged Goods
  - Expired Stock
  - Inventory Loss / Theft
  - Physical Count Audit Corrections
- Product Archiving to preserve historical sales records.

### 4. Customer Management & Khata (Udhaar Credit Ledger)
- Customer profiles: Name, Phone, Address, Credit Limit, and Current Outstanding Balance.
- Total Outstanding Receivables tracking across all debtor accounts.
- **Receive Repayment** modal: Records partial or full payments via Cash, QR, or Bank, immediately reducing customer balance owed.
- **One-Tap Payment Reminder**: Formats friendly bilingual SMS / WhatsApp reminder messages with current balance owed and shop name.

### 5. Supplier Management & Purchase Orders
- Supplier profiles: Company name, Contact person, Phone, Address, Outstanding Balance Owed.
- **Record Purchase / Inward Bill**: Select products, adjust quantities and unit costs, record payments made or payable credit.
- Automatically increments inventory stock and updates purchase cost records.
- **Pay Supplier** modal: Reduces debt balance owed to distributors.

### 6. Expense & Other Income Management
- Separation of operational overheads from direct sales.
- Operating Expense Categories: Rent, Staff Salary, Electricity/Utility, Transportation, Packaging, Maintenance, Refreshments, and Other.
- Other Business Income: Scrap box sales, Delivery fees, Supplier rebates/incentives, Commissions.

### 7. Profit & Loss (P&L) Financial Reports
- Clear, audit-grade accounting formulas:
  - **Net Sales** = Gross Sales − Discounts − Sales Returns/Refunds
  - **Gross Profit** = Net Sales − Cost of Goods Sold (COGS)
  - **Estimated Net Operating Profit** = Gross Profit − Operating Expenses + Other Operating Income
  - **Customer Receivables** = Total outstanding customer credit
  - **Supplier Payables** = Total outstanding vendor liabilities
- Working capital position breakdown.
- Top-selling products by quantity, revenue, and gross profit.
- One-tap "Share / Export" statement to clipboard or email.

### 8. Staff Roles & Role-Based Access Control
- **Owner / Admin**: Full access to all screens, financial P&L, configuration, and data management.
- **Manager**: Access to POS, Inventory, Purchases, Customers, and operational expenses.
- **Cashier**: Restricted to POS billing and customer directory.
- **Inventory Clerk**: Restricted to stock cataloging, inward receiving, and adjustments.
- Instant role switcher in the top navigation bar.

### 9. Settings, Customization & Audit Trail
- Configurable Store Profile: Shop Name, Owner, Phone, Address, PAN/VAT Number, Currency Symbol (Default: `Rs.`), Standard VAT % (Default: `13.0%`), Timezone (Default: `Asia/Kathmandu`), Receipt Footer Note.
- Persistent **Audit Trail Log**: Records who performed key actions (sales, refunds, stock adjustments, customer repayments, setting changes).
- **Seed Development Data**: Pre-populates realistic Nepali retail grocery/mini-mart inventory (Basmati rice, cooking oil, lentils, tea, sugar, milk, toiletries, noodles), sample customers with Khata dues, and supplier balances.

---

## 🛠 Tech Stack & Architecture

- **Platform**: Android (minSdk 24, targetSdk 36)
- **Language**: Kotlin 2.2.10
- **UI Framework**: Jetpack Compose (Material Design 3)
- **Architecture**: Clean MVVM (Model-View-ViewModel) + Repository Pattern
- **Local Relational Database**: Room 2.7.0 (SQLite) with KSP
- **Concurrency & Reactivity**: Kotlin Coroutines & `StateFlow`
- **Testing**: Robolectric 4.16.1 & JUnit 4

---

## 🚀 Running and Testing the App

### 1. Build and Run
```bash
gradle assembleDebug
```

### 2. Run Automated Tests
```bash
gradle :app:testDebugUnitTest
```
Tests cover:
- Duplicate SKU rejection
- Stock deduction on sale & restoration on refund
- Prevention of negative inventory
- Customer credit sales & partial repayments
- Supplier inward purchases & payables reduction
- Exact Profit & Loss calculation formulas (Net Sales, COGS, Gross Profit, Net Operating Profit)
- Low-stock & near-expiry flags
- Role-based permissions matrix
