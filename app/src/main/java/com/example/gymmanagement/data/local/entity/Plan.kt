package com.example.gymmanagement.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "plans")
data class Plan(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val remoteId: String = "",
    val name: String,
    val durationDays: Int,
    val price: Double,
    val updatedAt: Long = 0L,
    val synced: Boolean = false,
    val deleted: Boolean = false
)
