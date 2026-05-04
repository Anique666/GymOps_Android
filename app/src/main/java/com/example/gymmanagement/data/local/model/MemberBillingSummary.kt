package com.example.gymmanagement.data.local.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import com.example.gymmanagement.data.local.entity.Member

data class MemberBillingSummary(
    @Embedded val member: Member,
    @ColumnInfo(name = "planName") val planName: String,
    @ColumnInfo(name = "planDurationDays") val planDurationDays: Int,
    @ColumnInfo(name = "planPrice") val planPrice: Double,
    @ColumnInfo(name = "latestPaymentAmount") val latestPaymentAmount: Double,
    @ColumnInfo(name = "latestPaymentMethod") val latestPaymentMethod: String,
    @ColumnInfo(name = "latestPaymentStatus") val latestPaymentStatus: String,
    @ColumnInfo(name = "latestPaymentDate") val latestPaymentDate: Long,
    @ColumnInfo(name = "pendingAmount") val pendingAmount: Double
)