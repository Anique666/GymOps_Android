package com.example.gymmanagement.data.sync

data class SyncState(
    val inProgress: Boolean = false,
    val lastSuccessAt: Long? = null,
    val lastError: String? = null
)
