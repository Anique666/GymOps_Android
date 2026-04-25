package com.example.gymmanagement.data.local

import androidx.room.TypeConverter
import com.example.gymmanagement.data.local.entity.EquipmentStatus
import com.example.gymmanagement.data.local.entity.MaintenanceStatus

class GymTypeConverters {

    @TypeConverter
    fun fromEquipmentStatus(status: EquipmentStatus): String = status.name

    @TypeConverter
    fun toEquipmentStatus(value: String): EquipmentStatus {
        return runCatching { EquipmentStatus.valueOf(value) }.getOrDefault(EquipmentStatus.ACTIVE)
    }

    @TypeConverter
    fun fromMaintenanceStatus(status: MaintenanceStatus): String = status.name

    @TypeConverter
    fun toMaintenanceStatus(value: String): MaintenanceStatus {
        return runCatching { MaintenanceStatus.valueOf(value) }.getOrDefault(MaintenanceStatus.OPEN)
    }
}
