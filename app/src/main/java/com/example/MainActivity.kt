package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.StaffRole
import com.example.ui.components.ReceiptDialog
import com.example.ui.components.RetailBottomBar
import com.example.ui.components.RetailNavDrawerContent
import com.example.ui.components.RetailTopBar
import com.example.ui.screens.CustomersScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ExpensesScreen
import com.example.ui.screens.GeminiChatScreen
import com.example.ui.screens.InventoryScreen
import com.example.ui.screens.PosScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SalesHistoryScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SuppliersScreen
import com.example.ui.screens.VoiceLiveScreen
import com.example.data.ai.VoiceConversationManager
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.RetailViewModel
import com.example.ui.viewmodel.Screen
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: RetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                RetailApp(viewModel)
            }
        }
    }
}

@Composable
fun RetailApp(viewModel: RetailViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val currentRole by viewModel.currentRole.collectAsStateWithLifecycle()
    val products by viewModel.products.collectAsStateWithLifecycle()
    val customers by viewModel.customers.collectAsStateWithLifecycle()
    val suppliers by viewModel.suppliers.collectAsStateWithLifecycle()
    val sales by viewModel.sales.collectAsStateWithLifecycle()
    val purchases by viewModel.purchases.collectAsStateWithLifecycle()
    val expenses by viewModel.expenses.collectAsStateWithLifecycle()
    val income by viewModel.income.collectAsStateWithLifecycle()
    val customerPayments by viewModel.customerPayments.collectAsStateWithLifecycle()
    val auditLogs by viewModel.auditLogs.collectAsStateWithLifecycle()
    val shopSettings by viewModel.shopSettings.collectAsStateWithLifecycle()
    val financialSummary by viewModel.financialSummary.collectAsStateWithLifecycle()
    val cart by viewModel.cart.collectAsStateWithLifecycle()
    val selectedCustomer by viewModel.selectedCustomer.collectAsStateWithLifecycle()
    val discountAmount by viewModel.discountAmount.collectAsStateWithLifecycle()
    val applyTax by viewModel.applyTax.collectAsStateWithLifecycle()
    val lastSaleReceipt by viewModel.lastSaleReceipt.collectAsStateWithLifecycle()
    val lastSaleItems by viewModel.lastSaleItems.collectAsStateWithLifecycle()
    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Display user messages / alerts
    LaunchedEffect(userMessage) {
        userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearMessage()
        }
    }

    // Handle back button: close drawer first, or return to dashboard
    BackHandler(enabled = drawerState.isOpen || currentScreen != Screen.DASHBOARD) {
        if (drawerState.isOpen) {
            scope.launch { drawerState.close() }
        } else {
            viewModel.navigateTo(Screen.DASHBOARD)
        }
    }

    val context = androidx.compose.ui.platform.LocalContext.current
    val voiceHelper = remember {
        VoiceConversationManager(context = context) { recognizedSpeech ->
            viewModel.sendChatMessage(recognizedSpeech)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            RetailNavDrawerContent(
                currentScreen = currentScreen,
                currentRole = currentRole,
                shopName = shopSettings?.shopName ?: "Retail Master",
                onSelectScreen = { screen ->
                    viewModel.navigateTo(screen)
                    scope.launch { drawerState.close() }
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                if (currentScreen != Screen.VOICE_LIVE) {
                    RetailTopBar(
                        currentScreen = currentScreen,
                        currentRole = currentRole,
                        shopName = shopSettings?.shopName ?: "Retail Master",
                        cartItemCount = cart.size,
                        syncStatus = viewModel.syncStatus.collectAsStateWithLifecycle().value,
                        onMenuClick = { scope.launch { drawerState.open() } },
                        onRoleSelected = { role -> viewModel.switchRole(role) },
                        onOpenCart = { viewModel.navigateTo(Screen.POS) },
                        onOpenAiChat = { viewModel.navigateTo(Screen.GEMINI_CHAT) },
                        onOpenVoiceLive = { viewModel.navigateTo(Screen.VOICE_LIVE) },
                        onOpenSyncSettings = { viewModel.navigateTo(Screen.SETTINGS) }
                    )
                }
            },
            bottomBar = {
                if (currentScreen != Screen.VOICE_LIVE) {
                    RetailBottomBar(
                        currentScreen = currentScreen,
                        cartCount = cart.size,
                        onNavigate = { screen -> viewModel.navigateTo(screen) }
                    )
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                val canManage = currentRole.canManageInventory || currentRole == StaffRole.OWNER
                val activeSettings = shopSettings ?: com.example.data.entity.ShopSettingsEntity()

                when (currentScreen) {
                    Screen.DASHBOARD -> DashboardScreen(
                        financialSummary = financialSummary,
                        products = products,
                        recentSales = sales,
                        settings = activeSettings,
                        onNavigate = { viewModel.navigateTo(it) },
                        onViewSaleReceipt = { viewModel.viewInvoiceReceipt(it) }
                    )

                    Screen.POS -> PosScreen(
                        products = products,
                        customers = customers,
                        cart = cart,
                        selectedCustomer = selectedCustomer,
                        discountAmount = discountAmount,
                        applyTax = applyTax,
                        settings = activeSettings,
                        onAddToCart = { viewModel.addToCart(it) },
                        onUpdateQuantity = { id, qty -> viewModel.updateCartQuantity(id, qty) },
                        onRemoveFromCart = { viewModel.removeFromCart(it) },
                        onClearCart = { viewModel.clearCart() },
                        onSelectCustomer = { viewModel.selectCustomer(it) },
                        onSetDiscount = { viewModel.setDiscount(it) },
                        onSetApplyTax = { viewModel.setApplyTax(it) },
                        onCheckout = { method, cash, sCash, sDigital, notes ->
                            viewModel.checkout(method, cash, sCash, sDigital, notes)
                        }
                    )

                    Screen.INVENTORY -> InventoryScreen(
                        products = products,
                        settings = activeSettings,
                        canManage = canManage,
                        onCreateProduct = { name, cat, unit, cost, sell, stock, minStock, exp, imageUri ->
                            viewModel.createProduct(
                                name = name,
                                category = cat,
                                unit = unit,
                                purchasePrice = cost,
                                sellingPrice = sell,
                                initialStock = stock,
                                minStock = minStock,
                                expiryDate = exp,
                                imageUri = imageUri
                            )
                        },
                        onUpdateProduct = { viewModel.updateProduct(it) },
                        onArchiveProduct = { id, name -> viewModel.archiveProduct(id, name) },
                        onAdjustStock = { id, qty, reason, note ->
                            viewModel.adjustStock(id, qty, reason, note)
                        }
                    )

                    Screen.SALES_HISTORY -> SalesHistoryScreen(
                        sales = sales,
                        onSelectSale = { viewModel.viewInvoiceReceipt(it) }
                    )

                    Screen.CUSTOMERS -> CustomersScreen(
                        customers = customers,
                        payments = customerPayments,
                        settings = activeSettings,
                        onCreateCustomer = { name, phone, addr, limit, notes ->
                            viewModel.createCustomer(name, phone, addr, limit, notes)
                        },
                        onRecordRepayment = { id, amt, method, note ->
                            viewModel.recordCustomerRepayment(id, amt, method, note)
                        }
                    )

                    Screen.SUPPLIERS -> SuppliersScreen(
                        suppliers = suppliers,
                        purchases = purchases,
                        products = products,
                        canManage = canManage,
                        onCreateSupplier = { name, comp, phone, addr, notes ->
                            viewModel.createSupplier(name, comp, phone, addr, notes)
                        },
                        onRecordPurchase = { sup, items, paid, method, notes ->
                            viewModel.recordPurchase(sup, items, paid, method, notes)
                        },
                        onRecordSupplierPayment = { id, amt, method, note ->
                            viewModel.recordSupplierPayment(id, amt, method, note)
                        }
                    )

                    Screen.EXPENSES -> ExpensesScreen(
                        expenses = expenses,
                        income = income,
                        canManage = canManage,
                        onAddExpense = { title, cat, amt, method, ref, note ->
                            viewModel.addExpense(title, cat, amt, method, ref, note)
                        },
                        onAddIncome = { title, cat, amt, method, ref, note ->
                            viewModel.addIncome(title, cat, amt, method, ref, note)
                        }
                    )

                    Screen.REPORTS -> ReportsScreen(
                        financialSummary = financialSummary,
                        products = products,
                        sales = sales,
                        saleItems = viewModel.allSaleItems.collectAsStateWithLifecycle().value,
                        settings = activeSettings
                    )

                    Screen.SETTINGS -> SettingsScreen(
                        settings = activeSettings,
                        currentRole = currentRole,
                        auditLogs = auditLogs,
                        syncStatus = viewModel.syncStatus.collectAsStateWithLifecycle().value,
                        isOnline = viewModel.isOnline.collectAsStateWithLifecycle().value,
                        pendingChangesCount = viewModel.pendingChangesCount.collectAsStateWithLifecycle().value,
                        lastSyncTimestamp = viewModel.lastSyncTimestamp.collectAsStateWithLifecycle().value,
                        onTriggerSync = { viewModel.triggerSyncNow() },
                        onExportBackup = {
                            scope.launch {
                                val json = viewModel.exportDatabaseBackup()
                                val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                                clipboard?.setPrimaryClip(android.content.ClipData.newPlainText("RetailOS_Backup", json))
                                viewModel.showMessage("Full Database JSON copied to clipboard (${json.length} characters)!")
                            }
                        },
                        onUpdateSettings = { viewModel.updateShopSettings(it) },
                        onSwitchRole = { viewModel.switchRole(it) },
                        onResetData = { viewModel.resetAndReseedData() }
                    )

                    Screen.GEMINI_CHAT -> GeminiChatScreen(
                        messages = viewModel.chatMessages.collectAsStateWithLifecycle().value,
                        selectedModel = viewModel.selectedChatModel.collectAsStateWithLifecycle().value,
                        isGenerating = viewModel.isAiGenerating.collectAsStateWithLifecycle().value,
                        onSendMessage = { viewModel.sendChatMessage(it) },
                        onSelectModel = { viewModel.setSelectedChatModel(it) },
                        onClearChat = { viewModel.clearChatHistory() },
                        onOpenVoiceMode = { viewModel.navigateTo(Screen.VOICE_LIVE) },
                        onSpeakText = { text -> voiceHelper.speak(text) }
                    )

                    Screen.VOICE_LIVE -> VoiceLiveScreen(
                        voiceMessages = viewModel.voiceMessages.collectAsStateWithLifecycle().value,
                        isAiGenerating = viewModel.isAiGenerating.collectAsStateWithLifecycle().value,
                        onSendVoiceQuery = { query, onReply ->
                            viewModel.sendLiveVoiceMessage(query, onReply)
                        },
                        onBack = { viewModel.navigateTo(Screen.DASHBOARD) }
                    )
                }
            }
        }
    }

    // Modal Receipt Dialog for newly completed sales or viewing history
    lastSaleReceipt?.let { sale ->
        ReceiptDialog(
            sale = sale,
            items = lastSaleItems,
            settings = shopSettings ?: com.example.data.entity.ShopSettingsEntity(),
            onDismiss = { viewModel.dismissReceipt() },
            onRefund = { id, reason -> viewModel.refundSale(id, reason) }
        )
    }
}
