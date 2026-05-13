package com.example.gymmanagement.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "payments",
    foreignKeys = [
        ForeignKey(
            entity = Member::class,
            parentColumns = ["id"],
            childColumns = ["memberId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Plan::class,
            parentColumns = ["id"],
            childColumns = ["planId"],
            onDelete = ForeignKey.RESTRICT,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["memberId"]), Index(value = ["planId"])]
)
data class PaymentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val remoteId: String = "",
    val memberId: Int,
    val amount: Double,
    val paymentMethod: String,
    val paymentDate: Long,
    val planId: Int,
    val isRenewal: Boolean,
    val status: String,
    val updatedAt: Long = 0L,
    val synced: Boolean = false,
    val deleted: Boolean = false
)