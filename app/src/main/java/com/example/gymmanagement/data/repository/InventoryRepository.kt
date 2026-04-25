package com.example.gymmanagement.data.repository

import com.example.gymmanagement.data.local.dao.EquipmentDao
import com.example.gymmanagement.data.local.dao.MaintenanceDao
import com.example.gymmanagement.data.local.entity.EquipmentEntity
import com.example.gymmanagement.data.local.entity.EquipmentStatus
import com.example.gymmanagement.data.local.entity.MaintenanceEntity
import com.example.gymmanagement.data.local.entity.MaintenanceStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class InventoryRepository(
    private val equipmentDao: EquipmentDao,
    private val maintenanceDao: MaintenanceDao
) {

    fun observeEquipment(): Flow<List<EquipmentEntity>> = equipmentDao.observeAllEquipment()

    suspend fun addEquipment(equipment: EquipmentEntity) {
        withContext(Dispatchers.IO) {
            equipmentDao.insertEquipment(equipment)
        }
    }

    suspend fun runPrimaryAction(equipment: EquipmentEntity) {
        withContext(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            when (equipment.status) {
                EquipmentStatus.ACTIVE -> {
                    equipmentDao.updateEquipment(equipment.copy(lastServiceDate = now))
                }

                EquipmentStatus.MAINTENANCE_DUE -> {
                    maintenanceDao.insertMaintenanceRecord(
                        MaintenanceEntity(
                            equipmentId = equipment.id,
                            issueDescription = "Scheduled maintenance",
                            reportedDate = now,
                            status = MaintenanceStatus.IN_PROGRESS
                        )
                    )
                    equipmentDao.updateEquipment(equipment.copy(status = EquipmentStatus.IN_REPAIR))
                }

                EquipmentStatus.IN_REPAIR -> {
                    maintenanceDao.resolveLatestTicket(
                        equipmentId = equipment.id,
                        status = MaintenanceStatus.RESOLVED.name,
                        resolvedDate = now
                    )
                    equipmentDao.updateEquipment(
                        equipment.copy(
                            status = EquipmentStatus.ACTIVE,
                            lastServiceDate = now
                        )
                    )
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
                        equipmentId = equipment.id,
                        issueDescription = "Moved to repair queue",
                        reportedDate = now,
                        status = MaintenanceStatus.OPEN
                    )
                )
            }
            if (equipment.status == EquipmentStatus.IN_REPAIR && status == EquipmentStatus.ACTIVE) {
                maintenanceDao.resolveLatestTicket(
                    equipmentId = equipment.id,
                    status = MaintenanceStatus.RESOLVED.name,
                    resolvedDate = now
                )
            }
            equipmentDao.updateEquipment(
                equipment.copy(
                    status = status,
                    lastServiceDate = if (status == EquipmentStatus.ACTIVE) now else equipment.lastServiceDate
                )
            )
        }
    }
}
