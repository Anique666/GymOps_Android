package com.example.gymmanagement.utils

object MembershipStatusHelper {

    private const val FIVE_DAYS_MILLIS = 5L * 24L * 60L * 60L * 1000L

    fun isExpired(expiryDate: Long): Boolean {
        return expiryDate < System.currentTimeMillis()
    }

    fun isExpiringSoon(expiryDate: Long): Boolean {
        val now = System.currentTimeMillis()
        return expiryDate in now..(now + FIVE_DAYS_MILLIS)
    }

    fun statusLabel(expiryDate: Long): String {
        return when {
            isExpired(expiryDate) -> "Expired"
            isExpiringSoon(expiryDate) -> "Expiring Soon"
            else -> "Active"
        }
    }
}
