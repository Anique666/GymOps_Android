package com.example.gymmanagement.ui.inventory

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymmanagement.data.local.entity.EquipmentEntity
import com.example.gymmanagement.data.local.entity.EquipmentStatus
import com.example.gymmanagement.data.repository.InventoryRepository
import com.example.gymmanagement.di.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

data class InventoryUiState(
    val searchQuery: String = "",
    val selectedFilter: EquipmentStatus? = null,
    val totalAssets: Int = 0,
    val underMaintenanceCount: Int = 0,
    val readyPercent: Int = 0,
    val equipment: List<EquipmentEntity> = emptyList()
)

data class AddEquipmentDraft(
    val name: String,
    val serialNumber: String,
    val category: String,
    val purchaseDate: Long,
    val status: EquipmentStatus,
    val notes: String?
)

class InventoryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: InventoryRepository = AppContainer(application).inventoryRepository

    private val searchQuery = MutableStateFlow("")
    private val selectedFilter = MutableStateFlow<EquipmentStatus?>(null)

    val uiState: StateFlow<InventoryUiState> = combine(
        repository.observeEquipment(),
        searchQuery,
        selectedFilter
    ) { allEquipment, query, filter ->
        val normalizedQuery = query.trim().lowercase()
        val filtered = allEquipment.filter { equipment ->
            val queryMatches = normalizedQuery.isBlank() ||
                equipment.name.lowercase().contains(normalizedQuery) ||
                equipment.serialNumber.lowercase().contains(normalizedQuery)
            val filterMatches = filter == null || equipment.status == filter
            queryMatches && filterMatches
        }

        val total = allEquipment.size
        val underMaintenance = allEquipment.count { it.status == EquipmentStatus.IN_REPAIR }
        val activeCount = allEquipment.count { it.status == EquipmentStatus.ACTIVE }
        val readyPercent = if (total == 0) {
            0
        } else {
            ((activeCount.toFloat() / total.toFloat()) * 100f).roundToInt()
        }

        InventoryUiState(
            searchQuery = query,
            selectedFilter = filter,
            totalAssets = total,
            underMaintenanceCount = underMaintenance,
            readyPercent = readyPercent,
            equipment = filtered
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = InventoryUiState()
    )

    fun onSearchChanged(query: String) {
        searchQuery.value = query
    }

    fun onFilterSelected(status: EquipmentStatus?) {
        selectedFilter.value = status
    }

    fun addEquipment(draft: AddEquipmentDraft) {
        viewModelScope.launch {
            repository.addEquipment(
                EquipmentEntity(
                    name = draft.name.trim(),
                    serialNumber = draft.serialNumber.trim(),
                    category = draft.category.trim(),
                    status = draft.status,
                    purchaseDate = draft.purchaseDate,
                    lastServiceDate = draft.purchaseDate,
                    usageHours = null,
                    notes = draft.notes?.trim()?.takeIf { it.isNotEmpty() }
                )
            )
        }
    }

    fun runPrimaryAction(equipment: EquipmentEntity) {
        viewModelScope.launch {
            repository.runPrimaryAction(equipment)
        }
    }

    fun updateStatus(equipment: EquipmentEntity, status: EquipmentStatus) {
        viewModelScope.launch {
            repository.updateStatus(equipment, status)
        }
    }
}
