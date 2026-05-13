package com.example.gymmanagement.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "members",
    foreignKeys = [
        ForeignKey(
            entity = Plan::class,
            parentColumns = ["id"],
            childColumns = ["planId"],
            onDelete = ForeignKey.RESTRICT,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["planId"])]
)
data class Member(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val remoteId: String = "",
    val name: String,
    val phone: String,
    val joinDate: Long,
    val expiryDate: Long,
    val planId: Int,
    val paymentStatus: Boolean,
    val gender: String = "UNSPECIFIED",
    val dateOfBirth: Long = 0L,
    val source: String = "Unknown",
    val updatedAt: Long = 0L,
    val synced: Boolean = false,
    val deleted: Boolean = false
)
