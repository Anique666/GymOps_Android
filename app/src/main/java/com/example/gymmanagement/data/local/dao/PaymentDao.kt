package com.example.gymmanagement.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.gymmanagement.data.local.entity.PaymentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPayment(payment: PaymentEntity): Long

    @Query("SELECT * FROM payments WHERE memberId = :memberId ORDER BY paymentDate DESC LIMIT 1")
    fun getLatestPaymentForMember(memberId: Int): Flow<PaymentEntity?>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM payments WHERE status = 'PAID' AND paymentDate BETWEEN :startTime AND :endTime")
    fun observeRevenueTotal(startTime: Long, endTime: Long): Flow<Double>

    @Query(
        """
        SELECT date(paymentDate / 1000, 'unixepoch') AS label, COALESCE(SUM(amount), 0) AS value
        FROM payments
        WHERE status = 'PAID' AND paymentDate BETWEEN :startTime AND :endTime
        GROUP BY label
        ORDER BY label ASC
        """
    )
    fun observeDailyRevenue(startTime: Long, endTime: Long): Flow<List<RevenuePointRow>>

    @Query(
        """
        SELECT strftime('%Y-W%W', paymentDate / 1000, 'unixepoch') AS label, COALESCE(SUM(amount), 0) AS value
        FROM payments
        WHERE status = 'PAID' AND paymentDate BETWEEN :startTime AND :endTime
        GROUP BY label
        ORDER BY label ASC
        """
    )
    fun observeWeeklyRevenue(startTime: Long, endTime: Long): Flow<List<RevenuePointRow>>

    @Query(
        """
        SELECT strftime('%Y-%m', paymentDate / 1000, 'unixepoch') AS label, COALESCE(SUM(amount), 0) AS value
        FROM payments
        WHERE status = 'PAID' AND paymentDate BETWEEN :startTime AND :endTime
        GROUP BY label
        ORDER BY label ASC
        """
    )
    fun observeMonthlyRevenue(startTime: Long, endTime: Long): Flow<List<RevenuePointRow>>

    @Query(
        """
        SELECT paymentMethod AS label, COUNT(*) AS value
        FROM payments
        WHERE status = 'PAID' AND paymentDate BETWEEN :startTime AND :endTime
        GROUP BY paymentMethod
        ORDER BY value DESC
        """
    )
    fun observePaymentMethodDistribution(startTime: Long, endTime: Long): Flow<List<PaymentDistributionRow>>

    @Query(
        """
        SELECT COUNT(DISTINCT memberId)
        FROM payments
        WHERE status = 'PAID' AND isRenewal = 1 AND paymentDate BETWEEN :startTime AND :endTime
        """
    )
    fun observeRenewedMembersCount(startTime: Long, endTime: Long): Flow<Int>
}

data class RevenuePointRow(
    val label: String,
    val value: Double
)

data class PaymentDistributionRow(
    val label: String,
    val value: Int
)