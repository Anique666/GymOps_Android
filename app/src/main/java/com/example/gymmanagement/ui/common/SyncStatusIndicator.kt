package com.example.gymmanagement.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.gymmanagement.data.sync.SyncState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SyncStatusIndicator(syncState: SyncState, modifier: Modifier = Modifier) {
    val label = when {
        syncState.inProgress -> "Syncing"
        syncState.lastError != null -> "Sync error"
        syncState.lastSuccessAt != null -> "Synced"
        else -> "Not synced"
    }
    val details = when {
        syncState.lastError != null -> syncState.lastError
        syncState.lastSuccessAt != null -> {
            val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            "Last: ${formatter.format(Date(syncState.lastSuccessAt))}"
        }
        else -> null
    }

    val background = when {
        syncState.inProgress -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
        syncState.lastError != null -> Color(0x33FF6B6B)
        syncState.lastSuccessAt != null -> Color(0x334CAF50)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    Row(
        modifier = modifier
            .background(background, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = label, style = MaterialTheme.typography.labelLarge)
        if (details != null) {
            Text(text = details, style = MaterialTheme.typography.labelSmall)
        }
    }
}
