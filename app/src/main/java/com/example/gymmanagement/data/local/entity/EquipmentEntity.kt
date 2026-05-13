package com.example.gymmanagement.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "equipment",
    indices = [Index(value = ["serialNumber"]), Index(value = ["status"])]
)
data class EquipmentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val remoteId: String = "",
    val name: String,
    val serialNumber: String,
    val category: String,
    val status: EquipmentStatus,
    val purchaseDate: Long,
    val lastServiceDate: Long,
    val usageHours: Int? = null,
    val notes: String? = null,
    val updatedAt: Long = 0L,
    val synced: Boolean = false,
    val deleted: Boolean = false
)
