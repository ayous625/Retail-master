package com.example.data.sync

enum class SyncStatus(val label: String) {
    SYNCED("Synced with Cloud"),
    SYNCING("Syncing with Cloud..."),
    PENDING("Changes Saved Locally (Pending Sync)"),
    OFFLINE("Offline Mode (Local Room DB)"),
    ERROR("Sync Warning (Retry Queued)")
}

data class SyncReport(
    val timestamp: Long = System.currentTimeMillis(),
    val productsSynced: Int = 0,
    val salesSynced: Int = 0,
    val customersSynced: Int = 0,
    val suppliersSynced: Int = 0,
    val expensesSynced: Int = 0,
    val success: Boolean = true,
    val message: String = "All records synchronized successfully"
)
