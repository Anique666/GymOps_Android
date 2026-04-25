package com.example.gymmanagement.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "maintenance_records",
    foreignKeys = [
        ForeignKey(
            entity = EquipmentEntity::class,
            parentColumns = ["id"],
            childColumns = ["equipmentId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["equipmentId"]), Index(value = ["status"])]
)
data class MaintenanceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val equipmentId: Int,
    val issueDescription: String,
    val reportedDate: Long,
    val resolvedDate: Long? = null,
    val status: MaintenanceStatus
)
