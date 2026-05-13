package com.example.gymmanagement.data.remote.model

data class PlanRemote(
    val remoteId: String = "",
    val name: String = "",
    val durationDays: Int = 0,
    val price: Double = 0.0,
    val updatedAt: Long = 0L,
    val deleted: Boolean = false
)

data class MemberRemote(
    val remoteId: String = "",
    val name: String = "",
    val phone: String = "",
    val joinDate: Long = 0L,
    val expiryDate: Long = 0L,
    val planRemoteId: String = "",
    val paymentStatus: Boolean = false,
    val gender: String = "UNSPECIFIED",
    val dateOfBirth: Long = 0L,
    val source: String = "Unknown",
    val updatedAt: Long = 0L,
    val deleted: Boolean = false
)

data class PaymentRemote(
    val remoteId: String = "",
    val memberRemoteId: String = "",
    val planRemoteId: String = "",
    val amount: Double = 0.0,
    val paymentMethod: String = "",
    val paymentDate: Long = 0L,
    val isRenewal: Boolean = false,
    val status: String = "",
    val updatedAt: Long = 0L,
    val deleted: Boolean = false
)

data class EquipmentRemote(
    val remoteId: String = "",
    val name: String = "",
    val serialNumber: String = "",
    val category: String = "",
    val status: String = "",
    val purchaseDate: Long = 0L,
    val lastServiceDate: Long = 0L,
    val usageHours: Int? = null,
    val notes: String? = null,
    val updatedAt: Long = 0L,
    val deleted: Boolean = false
)

data class MaintenanceRemote(
    val remoteId: String = "",
    val equipmentRemoteId: String = "",
    val issueDescription: String = "",
    val reportedDate: Long = 0L,
    val resolvedDate: Long? = null,
    val status: String = "",
    val updatedAt: Long = 0L,
    val deleted: Boolean = false
)
