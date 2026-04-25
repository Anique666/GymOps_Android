package com.example.gymmanagement.data.repository

import com.example.gymmanagement.data.local.dao.PaymentDao
import com.example.gymmanagement.data.local.dao.ReportsDao
import com.example.gymmanagement.data.local.model.LabelCount
import com.example.gymmanagement.data.local.model.PaymentMethod
import com.example.gymmanagement.data.local.model.ReportRange
import com.example.gymmanagement.data.local.model.ReportsSnapshot
import com.example.gymmanagement.data.local.model.RevenuePoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class ReportsRepository(
    private val paymentDao: PaymentDao,
    private val reportsDao: ReportsDao
) {

    fun observeReports(range: ReportRange, startTime: Long, endTime: Long): Flow<ReportsSnapshot> {
        val totalRevenue = paymentDao.observeRevenueTotal(startTime, endTime)
        val previousWindow = when (range) {
            ReportRange.TODAY -> 24L * 60L * 60L * 1000L
            ReportRange.WEEK -> 7L * 24L * 60L * 60L * 1000L
            ReportRange.MONTH -> 30L * 24L * 60L * 60L * 1000L
        }
        val previousStart = startTime - previousWindow
        val previousRevenue = paymentDao.observeRevenueTotal(previousStart, startTime)
        val series = when (range) {
            ReportRange.TODAY -> paymentDao.observeDailyRevenue(startTime, endTime)
            ReportRange.WEEK -> paymentDao.observeDailyRevenue(startTime, endTime)
            ReportRange.MONTH -> paymentDao.observeMonthlyRevenue(startTime, endTime)
        }
        val paymentDistribution = paymentDao.observePaymentMethodDistribution(startTime, endTime)
        val newMembers = reportsDao.observeNewMembersCount(startTime, endTime)
        val expiredMembers = reportsDao.observeExpiredMembersCount(startTime, endTime)
        val renewedMembers = paymentDao.observeRenewedMembersCount(startTime, endTime)
        val expiringSoon = reportsDao.observeExpiringSoonCount(startTime, endTime)
        val genderDistribution = reportsDao.observeGenderDistribution()
        val dateOfBirths = reportsDao.observeDateOfBirths()

        return combine(
            totalRevenue,
            previousRevenue,
            series,
            paymentDistribution,
            newMembers,
            expiredMembers,
            renewedMembers,
            expiringSoon,
            genderDistribution,
            dateOfBirths
        ) { values ->
            val currentRevenue = values[0] as Double
            val priorRevenue = values[1] as Double
            val revenueTrendPercent = if (priorRevenue > 0.0) {
                ((currentRevenue - priorRevenue) / priorRevenue) * 100.0
            } else {
                0.0
            }

            val revenueSeries = (values[2] as List<*>).mapNotNull { row ->
                val item = row as? com.example.gymmanagement.data.local.dao.RevenuePointRow ?: return@mapNotNull null
                RevenuePoint(item.label, item.value)
            }

            val paymentMethods = (values[3] as List<*>).mapNotNull { row ->
                val item = row as? com.example.gymmanagement.data.local.dao.PaymentDistributionRow ?: return@mapNotNull null
                LabelCount(item.label, item.value)
            }.let { raw ->
                listOf(
                    PaymentMethod.CASH.name,
                    PaymentMethod.UPI.name,
                    PaymentMethod.CARD.name
                ).map { method ->
                    raw.firstOrNull { it.label.equals(method, ignoreCase = true) } ?: LabelCount(method, 0)
                }
            }

            val newMembersCount = values[4] as Int
            val expiredMembersCount = values[5] as Int
            val renewedMembersCount = values[6] as Int
            val retentionRate = if (expiredMembersCount > 0) {
                (renewedMembersCount.toDouble() / expiredMembersCount.toDouble()) * 100.0
            } else {
                0.0
            }
            val churnRate = if (expiredMembersCount > 0) {
                ((expiredMembersCount - renewedMembersCount).coerceAtLeast(0).toDouble() / expiredMembersCount.toDouble()) * 100.0
            } else {
                0.0
            }

            val expiringSoonCount = values[7] as Int

            val genderRows = (values[8] as List<*>).mapNotNull { row ->
                val item = row as? com.example.gymmanagement.data.local.dao.DemographicCountRow ?: return@mapNotNull null
                LabelCount(item.label, item.value)
            }

            val dobValues = values[9] as List<Long>
            val now = System.currentTimeMillis()
            val ageGroups = dobValues.mapNotNull { dob ->
                val age = calculateAge(dob, now)
                when {
                    age in 18..25 -> "18-25"
                    age in 26..40 -> "26-40"
                    age > 40 -> "40+"
                    else -> null
                }
            }.groupingBy { it }.eachCount().map { (label, count) -> LabelCount(label, count) }

            ReportsSnapshot(
                totalRevenue = currentRevenue,
                revenueTrendPercent = revenueTrendPercent,
                revenueSeries = revenueSeries,
                paymentDistribution = paymentMethods,
                newMembers = newMembersCount,
                expiredMembers = expiredMembersCount,
                renewedMembers = renewedMembersCount,
                retentionRate = retentionRate,
                churnRate = churnRate,
                expiringSoon = expiringSoonCount,
                genderDistribution = genderRows,
                ageGroups = ageGroups.sortedByDescending { it.value }
            )
        }
    }

    private fun calculateAge(dateOfBirth: Long, now: Long): Int {
        val birth = java.util.Calendar.getInstance().apply { timeInMillis = dateOfBirth }
        val current = java.util.Calendar.getInstance().apply { timeInMillis = now }
        var age = current.get(java.util.Calendar.YEAR) - birth.get(java.util.Calendar.YEAR)
        if (current.get(java.util.Calendar.DAY_OF_YEAR) < birth.get(java.util.Calendar.DAY_OF_YEAR)) {
            age -= 1
        }
        return age.coerceAtLeast(0)
    }
}