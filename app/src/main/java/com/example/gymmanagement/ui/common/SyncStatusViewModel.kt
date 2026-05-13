package com.example.gymmanagement.ui.common

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymmanagement.data.sync.SyncManager
import com.example.gymmanagement.data.sync.SyncState
import com.example.gymmanagement.di.AppContainer
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SyncStatusViewModel(application: Application) : AndroidViewModel(application) {

    private val syncManager: SyncManager = AppContainer(application).syncManager()

    val syncState: StateFlow<SyncState> = syncManager.state

    fun refresh() {
        viewModelScope.launch {
            try {
                syncManager.syncAll()
            } catch (_: Exception) {
                // Errors are reflected in SyncState.
            }
        }
    }
}
