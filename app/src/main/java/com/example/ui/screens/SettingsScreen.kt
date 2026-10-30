package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Sync
import com.example.data.entity.AuditLogEntity
import com.example.data.entity.ShopSettingsEntity
import com.example.data.sync.SyncStatus
import com.example.model.StaffRole
import com.example.ui.theme.AquaAccent
import com.example.ui.theme.CrimsonError
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.RoyalBlueBright
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SettingsScreen(
    settings: ShopSettingsEntity,
    currentRole: StaffRole,
    auditLogs: List<AuditLogEntity>,
    syncStatus: SyncStatus,
    isOnline: Boolean,
    pendingChangesCount: Int,
    lastSyncTimestamp: Long,
    onTriggerSync: () -> Unit,
    onExportBackup: () -> Unit,
    onUpdateSettings: (ShopSettingsEntity) -> Unit,
    onSwitchRole: (StaffRole) -> Unit,
    onResetData: () -> Unit
) {
    val context = LocalContext.current

    var shopName by remember(settings.shopName) { mutableStateOf(settings.shopName) }
    var ownerName by remember(settings.ownerName) { mutableStateOf(settings.ownerName) }
    var phone by remember(settings.phone) { mutableStateOf(settings.phone) }
    var address by remember(settings.address) { mutableStateOf(settings.address) }
    var panVat by remember(settings.panVatNumber) { mutableStateOf(settings.panVatNumber) }
    var taxRate by remember(settings.taxRatePercent) { mutableStateOf(settings.taxRatePercent.toString()) }
    var receiptFooter by remember(settings.receiptFooter) { mutableStateOf(settings.receiptFooter) }
    var currencySymbol by remember(settings.currencySymbol) { mutableStateOf(settings.currencySymbol) }
    var timezone by remember(settings.timezone) { mutableStateOf(settings.timezone) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("settings_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Shop Settings & Configuration",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Store profile, staff roles, and audit trail",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }

        // Shop Profile Form Card
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth().testTag("shop_profile_card")
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Store, contentDescription = null, tint = NavyPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Store Profile & Receipt Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Divider()

                    OutlinedTextField(
                        value = shopName,
                        onValueChange = { shopName = it },
                        label = { Text("Shop / Business Name") },
                        modifier = Modifier.fillMaxWidth().testTag("settings_shop_name"),
                        singleLine = true
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = ownerName,
                            onValueChange = { ownerName = it },
                            label = { Text("Owner Name") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Contact Phone") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Physical Store Address") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = panVat,
                            onValueChange = { panVat = it },
                            label = { Text("PAN / VAT Number") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = taxRate,
                            onValueChange = { taxRate = it },
                            label = { Text("Standard VAT Rate (%)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = currencySymbol,
                            onValueChange = { currencySymbol = it },
                            label = { Text("Currency Symbol") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = timezone,
                            onValueChange = { timezone = it },
                            label = { Text("Default Timezone") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    OutlinedTextField(
                        value = receiptFooter,
                        onValueChange = { receiptFooter = it },
                        label = { Text("Printed Receipt Footer Note") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Button(
                        onClick = {
                            val rate = taxRate.toDoubleOrNull() ?: 13.0
                            onUpdateSettings(
                                settings.copy(
                                    shopName = shopName.trim(),
                                    ownerName = ownerName.trim(),
                                    phone = phone.trim(),
                                    address = address.trim(),
                                    panVatNumber = panVat.trim(),
                                    taxRatePercent = rate,
                                    receiptFooter = receiptFooter.trim(),
                                    currencySymbol = currencySymbol.trim(),
                                    timezone = timezone.trim()
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                        modifier = Modifier.fillMaxWidth().testTag("save_settings_btn")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Store Settings")
                    }
                }
            }
        }

        // Staff Roles & Permissions Card
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth().testTag("staff_roles_card")
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = RoyalBlueBright)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Staff Roles & Access Control", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Divider()

                    Text("Active Staff Mode: ${currentRole.title}", fontWeight = FontWeight.Bold, color = RoyalBlueBright)

                    StaffRole.values().forEach { role ->
                        val isSelected = currentRole == role
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) Color(0xFFE0E7FF) else Color(0xFFF8FAFC),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onSwitchRole(role) }
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(role.title, fontWeight = FontWeight.Bold, color = if (isSelected) NavyPrimary else Color.Black)
                                    Text(
                                        when (role) {
                                            StaffRole.OWNER -> "Full access: POS, Inventory, P&L Reports, Ledger, Settings"
                                            StaffRole.MANAGER -> "Operations: POS, Inventory restock, Purchases, Customers"
                                            StaffRole.CASHIER -> "Front desk: POS sales & customer billing"
                                            StaffRole.INVENTORY_STAFF -> "Warehouse: Stock inward, damage & audit adjustments"
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.Gray
                                    )
                                }
                                if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = NavyPrimary)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Cloud Synchronization & Offline Architecture Card
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth().testTag("cloud_sync_card")
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (isOnline) Icons.Default.CloudDone else Icons.Default.CloudOff,
                                contentDescription = null,
                                tint = if (isOnline) EmeraldSuccess else CrimsonError
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Cloud Sync & Storage Architecture", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = when (syncStatus) {
                                SyncStatus.SYNCED -> Color(0xFFDCFCE7)
                                SyncStatus.SYNCING -> Color(0xFFDBEAFE)
                                SyncStatus.PENDING -> Color(0xFFFEF3C7)
                                SyncStatus.OFFLINE -> Color(0xFFFEE2E2)
                                SyncStatus.ERROR -> Color(0xFFFEE2E2)
                            }
                        ) {
                            Text(
                                text = syncStatus.label,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = when (syncStatus) {
                                    SyncStatus.SYNCED -> Color(0xFF166534)
                                    SyncStatus.SYNCING -> Color(0xFF1E40AF)
                                    SyncStatus.PENDING -> Color(0xFF92400E)
                                    SyncStatus.OFFLINE -> Color(0xFF991B1B)
                                    SyncStatus.ERROR -> Color(0xFF991B1B)
                                }
                            )
                        }
                    }
                    Divider()

                    Text(
                        text = "Data Storage Location:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary
                    )
                    Text(
                        text = "• Local Engine: Android Room SQLite database (retail_master.db)\n" +
                                "• Storage Path: /data/data/com.aistudio.retailmaster.../databases/\n" +
                                "• Offline Support: 100% functional without internet. All POS sales, stock changes, and customer ledger entries persist safely on device.\n" +
                                "• Cloud Sync: Background synchronization coordinates with cloud endpoints when online.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.DarkGray
                    )

                    if (pendingChangesCount > 0) {
                        Text(
                            text = "⚡ $pendingChangesCount local changes waiting to sync to cloud.",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFB45309),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (lastSyncTimestamp > 0) {
                        Text(
                            text = "Last synced: ${SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US).format(Date(lastSyncTimestamp))}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = onTriggerSync,
                            enabled = syncStatus != SyncStatus.SYNCING,
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).testTag("sync_now_btn")
                        ) {
                            Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (syncStatus == SyncStatus.SYNCING) "Syncing..." else "Sync Cloud Now", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = onExportBackup,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).testTag("export_full_json_btn")
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Export Database JSON", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Database Management Card
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth().testTag("db_management_card")
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Backup, contentDescription = null, tint = NavyPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Database Tools & Seed Data", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Divider()

                    Text("Reset or populate realistic grocery & mart test records including products, customers, suppliers, and sales.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = onResetData,
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalBlueBright),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).testTag("reload_sample_data_btn")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Seed Sample Data", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Audit Trail Logs
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth().testTag("audit_logs_card")
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.History, contentDescription = null, tint = NavyPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Audit Trail & Activity Log (${auditLogs.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Divider()

                    if (auditLogs.isEmpty()) {
                        Text("No audit logs recorded yet.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    } else {
                        auditLogs.take(15).forEach { log ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(log.details, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        "${log.action} • By ${log.staffRole} • ${SimpleDateFormat("dd MMM, hh:mm a", Locale.US).format(Date(log.timestamp))}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.Gray
                                    )
                                }
                            }
                            Divider(color = Color(0xFFF8FAFC))
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
