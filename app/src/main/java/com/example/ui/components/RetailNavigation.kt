package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Sync
import com.example.data.sync.SyncStatus
import com.example.model.StaffRole
import com.example.ui.theme.AquaAccent
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.RoyalBlueBright
import com.example.ui.viewmodel.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RetailTopBar(
    currentScreen: Screen,
    currentRole: StaffRole,
    shopName: String,
    cartItemCount: Int,
    syncStatus: SyncStatus = SyncStatus.SYNCED,
    onMenuClick: () -> Unit,
    onRoleSelected: (StaffRole) -> Unit,
    onOpenCart: () -> Unit,
    onOpenAiChat: () -> Unit,
    onOpenVoiceLive: () -> Unit,
    onOpenSyncSettings: () -> Unit = {}
) {
    var showRoleMenu by remember { mutableStateOf(false) }

    CenterAlignedTopAppBar(
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = currentScreen.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onOpenSyncSettings() }
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(
                                when (syncStatus) {
                                    SyncStatus.SYNCED -> Color(0xFF10B981)
                                    SyncStatus.SYNCING -> Color(0xFF38BDF8)
                                    SyncStatus.PENDING -> Color(0xFFF59E0B)
                                    SyncStatus.OFFLINE -> Color(0xFFEF4444)
                                    SyncStatus.ERROR -> Color(0xFFEF4444)
                                },
                                shape = CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$shopName • ${if (syncStatus == SyncStatus.OFFLINE) "Room DB (Offline)" else "Cloud Ready"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = AquaAccent,
                        fontSize = 10.sp
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(
                onClick = onMenuClick,
                modifier = Modifier.testTag("drawer_menu_button")
            ) {
                Icon(
                    Icons.Default.Menu,
                    contentDescription = "Open navigation menu",
                    tint = Color.White
                )
            }
        },
        actions = {
            // Role switcher pill
            Box {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White.copy(alpha = 0.15f),
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { showRoleMenu = true }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("role_switcher_pill")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Role",
                            tint = AquaAccent,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = currentRole.name.take(4),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                DropdownMenu(
                    expanded = showRoleMenu,
                    onDismissRequest = { showRoleMenu = false }
                ) {
                    Text(
                        "Switch Staff Mode",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Divider()
                    StaffRole.values().forEach { role ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(role.title, fontWeight = if (role == currentRole) FontWeight.Bold else FontWeight.Normal)
                                    Text(
                                        when (role) {
                                            StaffRole.OWNER -> "Full administrative & financial access"
                                            StaffRole.MANAGER -> "Inventory, purchases, & sales access"
                                            StaffRole.CASHIER -> "POS checkout & sales only"
                                            StaffRole.INVENTORY_STAFF -> "Stock adjustment & cataloging only"
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.Gray
                                    )
                                }
                            },
                            onClick = {
                                showRoleMenu = false
                                onRoleSelected(role)
                            },
                            modifier = Modifier.testTag("role_option_${role.name.lowercase()}")
                        )
                    }
                }
            }

            // Quick Gemini Voice Live Button
            IconButton(
                onClick = onOpenVoiceLive,
                modifier = Modifier.testTag("top_bar_voice_btn")
            ) {
                Icon(
                    Icons.Default.Mic,
                    contentDescription = "Voice Live",
                    tint = AquaAccent
                )
            }

            // Quick Gemini Chatbot Button
            IconButton(
                onClick = onOpenAiChat,
                modifier = Modifier.testTag("top_bar_ai_chat_btn")
            ) {
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = "Gemini AI",
                    tint = Color.White
                )
            }

            // Quick POS Cart Badge if on POS screen or items in cart
            if (cartItemCount > 0) {
                IconButton(
                    onClick = onOpenCart,
                    modifier = Modifier.testTag("top_bar_cart_button")
                ) {
                    BadgedBox(
                        badge = {
                            Badge(
                                containerColor = RoyalBlueBright,
                                contentColor = Color.White
                            ) {
                                Text("$cartItemCount")
                            }
                        }
                    ) {
                        Icon(
                            Icons.Default.ShoppingCart,
                            contentDescription = "Cart",
                            tint = Color.White
                        )
                    }
                }
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = NavyPrimary
        )
    )
}

@Composable
fun RetailBottomBar(
    currentScreen: Screen,
    cartCount: Int,
    onNavigate: (Screen) -> Unit
) {
    val items = listOf(
        NavigationItem(Screen.DASHBOARD, Icons.Default.Dashboard, "Home"),
        NavigationItem(Screen.POS, Icons.Default.PointOfSale, "POS", badge = if (cartCount > 0) "$cartCount" else null),
        NavigationItem(Screen.INVENTORY, Icons.Default.Inventory, "Stock"),
        NavigationItem(Screen.CUSTOMERS, Icons.Default.Group, "Khata"),
        NavigationItem(Screen.REPORTS, Icons.Default.Assessment, "P&L")
    )

    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .testTag("bottom_nav_bar"),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        items.forEach { item ->
            val selected = currentScreen == item.screen
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(item.screen) },
                icon = {
                    if (item.badge != null) {
                        BadgedBox(badge = { Badge { Text(item.badge) } }) {
                            Icon(item.icon, contentDescription = item.label)
                        }
                    } else {
                        Icon(item.icon, contentDescription = item.label)
                    }
                },
                label = { Text(item.label, fontSize = 11.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = NavyPrimary,
                    selectedTextColor = NavyPrimary,
                    indicatorColor = Color(0xFFE0E7FF)
                ),
                modifier = Modifier.testTag("nav_item_${item.screen.name.lowercase()}")
            )
        }
    }
}

@Composable
fun RetailNavDrawerContent(
    currentScreen: Screen,
    currentRole: StaffRole,
    shopName: String,
    onSelectScreen: (Screen) -> Unit
) {
    ModalDrawerSheet(
        modifier = Modifier.width(300.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(NavyPrimary)
                .padding(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.White.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Store,
                    contentDescription = null,
                    tint = AquaAccent,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = shopName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Retail Shop Management System",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Mode: ${currentRole.title}",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = AquaAccent,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        val menuItems = listOf(
            NavigationItem(Screen.DASHBOARD, Icons.Default.Dashboard, "Dashboard"),
            NavigationItem(Screen.POS, Icons.Default.PointOfSale, "Point of Sale (POS)"),
            NavigationItem(Screen.INVENTORY, Icons.Default.Inventory, "Inventory & Stock"),
            NavigationItem(Screen.SALES_HISTORY, Icons.Default.ReceiptLong, "Sales & Invoices"),
            NavigationItem(Screen.CUSTOMERS, Icons.Default.Group, "Customers & Khata (Udhaar)"),
            NavigationItem(Screen.SUPPLIERS, Icons.Default.Store, "Suppliers & Purchases"),
            NavigationItem(Screen.EXPENSES, Icons.Default.AccountBalanceWallet, "Expenses & Other Income"),
            NavigationItem(Screen.REPORTS, Icons.Default.Assessment, "Profit & Loss / Reports"),
            NavigationItem(Screen.GEMINI_CHAT, Icons.Default.AutoAwesome, "Gemini AI Store Assistant"),
            NavigationItem(Screen.VOICE_LIVE, Icons.Default.Mic, "Gemini 3.8 Live Voice"),
            NavigationItem(Screen.SETTINGS, Icons.Default.Settings, "Shop Settings & Staff")
        )

        menuItems.forEach { item ->
            val selected = currentScreen == item.screen
            NavigationDrawerItem(
                label = { Text(item.label, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) },
                icon = { Icon(item.icon, contentDescription = item.label) },
                selected = selected,
                onClick = { onSelectScreen(item.screen) },
                modifier = Modifier
                    .padding(NavigationDrawerItemDefaults.ItemPadding)
                    .testTag("drawer_item_${item.screen.name.lowercase()}"),
                colors = NavigationDrawerItemDefaults.colors(
                    selectedContainerColor = Color(0xFFE0E7FF),
                    selectedIconColor = NavyPrimary,
                    selectedTextColor = NavyPrimary
                )
            )
        }
    }
}

private data class NavigationItem(
    val screen: Screen,
    val icon: ImageVector,
    val label: String,
    val badge: String? = null
)
