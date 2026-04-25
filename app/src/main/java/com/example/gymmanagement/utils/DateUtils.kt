package com.example.gymmanagement.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {

    private const val ONE_DAY_MILLIS = 24L * 60L * 60L * 1000L

    fun calculateExpiryDate(joinDate: Long, durationDays: Int): Long {
        return joinDate + (durationDays * ONE_DAY_MILLIS)
    }

    fun calculateAge(dateOfBirth: Long, now: Long = System.currentTimeMillis()): Int {
        if (dateOfBirth <= 0L) return 0

        val birth = Calendar.getInstance().apply { timeInMillis = dateOfBirth }
        val current = Calendar.getInstance().apply { timeInMillis = now }
        var age = current.get(Calendar.YEAR) - birth.get(Calendar.YEAR)
        if (current.get(Calendar.DAY_OF_YEAR) < birth.get(Calendar.DAY_OF_YEAR)) {
            age -= 1
        }
        return age.coerceAtLeast(0)
    }

    fun formatDate(timestamp: Long): String {
        val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return formatter.format(Date(timestamp))
    }

    fun formatCardDate(timestamp: Long): String {
        val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return formatter.format(Date(timestamp))
    }

    fun todayStartMillis(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}
