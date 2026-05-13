package com.example.gymmanagement.data.sync

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private const val SYNC_PREFERENCES = "sync_preferences"

private val Context.syncDataStore by preferencesDataStore(name = SYNC_PREFERENCES)

class SyncPreferences(private val context: Context) {

    companion object {
        val LAST_SYNC_PLANS = longPreferencesKey("last_sync_plans")
        val LAST_SYNC_MEMBERS = longPreferencesKey("last_sync_members")
        val LAST_SYNC_PAYMENTS = longPreferencesKey("last_sync_payments")
        val LAST_SYNC_EQUIPMENT = longPreferencesKey("last_sync_equipment")
        val LAST_SYNC_MAINTENANCE = longPreferencesKey("last_sync_maintenance")
    }

    suspend fun getLastSync(key: androidx.datastore.preferences.core.Preferences.Key<Long>): Long {
        val prefs = context.syncDataStore.data.first()
        return prefs[key] ?: 0L
    }

    suspend fun setLastSync(key: androidx.datastore.preferences.core.Preferences.Key<Long>, value: Long) {
        context.syncDataStore.edit { prefs ->
            prefs[key] = value
        }
    }
}
