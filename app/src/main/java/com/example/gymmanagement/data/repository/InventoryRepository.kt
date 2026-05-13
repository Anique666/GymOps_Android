package com.example.gymmanagement.data.repository

import com.example.gymmanagement.data.local.dao.EquipmentDao
import com.example.gymmanagement.data.local.dao.MaintenanceDao
import com.example.gymmanagement.data.local.entity.EquipmentEntity
import com.example.gymmanagement.data.local.entity.EquipmentStatus
import com.example.gymmanagement.data.local.entity.MaintenanceEntity
import com.example.gymmanagement.data.local.entity.MaintenanceStatus
import com.example.gymmanagement.data.sync.SyncManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.UUID

class InventoryRepository(
    private val equipmentDao: EquipmentDao,
    private val maintenanceDao: MaintenanceDao,
    private val syncManager: SyncManager? = null
) {

    fun observeEquipment(): Flow<List<EquipmentEntity>> = equipmentDao.observeAllEquipment()

    suspend fun addEquipment(equipment: EquipmentEntity) {
        withContext(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            val prepared = equipment.copy(
                remoteId = equipment.remoteId.ifBlank { UUID.randomUUID().toString() },
                updatedAt = now,
                synced = false,
                deleted = false
            )
            equipmentDao.insertEquipment(prepared)
            syncManager?.enqueueSync()
        }
    }

    suspend fun runPrimaryAction(equipment: EquipmentEntity) {
        withContext(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            when (equipment.status) {
                EquipmentStatus.ACTIVE -> {
                    equipmentDao.updateEquipment(
                        equipment.copy(lastServiceDate = now, updatedAt = now, synced = false)
                    )
                    syncManager?.enqueueSync()
                }

                EquipmentStatus.MAINTENANCE_DUE -> {
                    maintenanceDao.insertMaintenanceRecord(
                        MaintenanceEntity(
                            remoteId = UUID.randomUUID().toString(),
                            equipmentId = equipment.id,
                            issueDescription = "Scheduled maintenance",
                            reportedDate = now,
                            status = MaintenanceStatus.IN_PROGRESS,
                            updatedAt = now,
                            synced = false,
                            deleted = false
                        )
                    )
                    equipmentDao.updateEquipment(
                        equipment.copy(status = EquipmentStatus.IN_REPAIR, updatedAt = now, synced = false)
                    )
                    syncManager?.enqueueSync()
                }

                EquipmentStatus.IN_REPAIR -> {
                    maintenanceDao.resolveLatestTicket(
                        equipmentId = equipment.id,
                        status = MaintenanceStatus.RESOLVED.name,
                        resolvedDate = now,
                        updatedAt = now
                    )
                    equipmentDao.updateEquipment(
                        equipment.copy(
                            status = EquipmentStatus.ACTIVE,
                            lastServiceDate = now,
                            updatedAt = now,
                            synced = false
                        )
                    )
                    syncManager?.enqueueSync()
                }
            }
        }
    }

    suspend fun updateStatus(equipment: EquipmentEntity, status: EquipmentStatus) {
        withContext(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            if (equipment.status != EquipmentStatus.IN_REPAIR && status == EquipmentStatus.IN_REPAIR) {
                maintenanceDao.insertMaintenanceRecord(
                    MaintenanceEntity(
                        remoteId = UUID.randomUUID().toString(),
                        equipmentId = equipment.id,
                        issueDescription = "Moved to repair queue",
                        reportedDate = now,
                        status = MaintenanceStatus.OPEN,
                        updatedAt = now,
                        synced = false,
                        deleted = false
                    )
                )
            }
            if (equipment.status == EquipmentStatus.IN_REPAIR && status == EquipmentStatus.ACTIVE) {
                maintenanceDao.resolveLatestTicket(
                    equipmentId = equipment.id,
                    status = MaintenanceStatus.RESOLVED.name,
                    resolvedDate = now,
                    updatedAt = now
                )
            }
            equipmentDao.updateEquipment(
                equipment.copy(
                    status = status,
                    lastServiceDate = if (status == EquipmentStatus.ACTIVE) now else equipment.lastServiceDate,
                    updatedAt = now,
                    synced = false
                )
            )
            syncManager?.enqueueSync()
        }
    }
}
