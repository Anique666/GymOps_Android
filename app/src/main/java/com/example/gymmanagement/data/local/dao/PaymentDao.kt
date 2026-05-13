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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPayments(payments: List<PaymentEntity>)

    @Query("SELECT * FROM payments WHERE memberId = :memberId AND deleted = 0 ORDER BY paymentDate DESC LIMIT 1")
    fun getLatestPaymentForMember(memberId: Int): Flow<PaymentEntity?>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM payments WHERE deleted = 0 AND status = 'PAID' AND paymentDate BETWEEN :startTime AND :endTime")
    fun observeRevenueTotal(startTime: Long, endTime: Long): Flow<Double>

    @Query(
        """
        SELECT date(paymentDate / 1000, 'unixepoch') AS label, COALESCE(SUM(amount), 0) AS value
        FROM payments
        WHERE deleted = 0 AND status = 'PAID' AND paymentDate BETWEEN :startTime AND :endTime
        GROUP BY label
        ORDER BY label ASC
        """
    )
    fun observeDailyRevenue(startTime: Long, endTime: Long): Flow<List<RevenuePointRow>>

    @Query(
        """
        SELECT strftime('%Y-W%W', paymentDate / 1000, 'unixepoch') AS label, COALESCE(SUM(amount), 0) AS value
        FROM payments
        WHERE deleted = 0 AND status = 'PAID' AND paymentDate BETWEEN :startTime AND :endTime
        GROUP BY label
        ORDER BY label ASC
        """
    )
    fun observeWeeklyRevenue(startTime: Long, endTime: Long): Flow<List<RevenuePointRow>>

    @Query(
        """
        SELECT strftime('%Y-%m', paymentDate / 1000, 'unixepoch') AS label, COALESCE(SUM(amount), 0) AS value
        FROM payments
        WHERE deleted = 0 AND status = 'PAID' AND paymentDate BETWEEN :startTime AND :endTime
        GROUP BY label
        ORDER BY label ASC
        """
    )
    fun observeMonthlyRevenue(startTime: Long, endTime: Long): Flow<List<RevenuePointRow>>

    @Query(
        """
        SELECT paymentMethod AS label, COUNT(*) AS value
        FROM payments
        WHERE deleted = 0 AND status = 'PAID' AND paymentDate BETWEEN :startTime AND :endTime
        GROUP BY paymentMethod
        ORDER BY value DESC
        """
    )
    fun observePaymentMethodDistribution(startTime: Long, endTime: Long): Flow<List<PaymentDistributionRow>>

    @Query(
        """
        SELECT COUNT(DISTINCT memberId)
        FROM payments
        WHERE deleted = 0 AND status = 'PAID' AND isRenewal = 1 AND paymentDate BETWEEN :startTime AND :endTime
        """
    )
    fun observeRenewedMembersCount(startTime: Long, endTime: Long): Flow<Int>

    @Query(
        """
        SELECT COALESCE(SUM(
            CASE
                WHEN p.price > COALESCE(lp.amount, 0) THEN p.price - COALESCE(lp.amount, 0)
                ELSE 0
            END
        ), 0)
        FROM members m
        INNER JOIN plans p ON p.id = m.planId AND p.deleted = 0
        LEFT JOIN payments lp ON lp.id = (
            SELECT id
            FROM payments
            WHERE memberId = m.id AND deleted = 0
            ORDER BY paymentDate DESC
            LIMIT 1
        )
        WHERE m.deleted = 0
        """
    )
    fun observePendingCashTotal(): Flow<Double>

    @Query("SELECT * FROM payments WHERE synced = 0")
    fun getPendingSyncPayments(): List<PaymentEntity>

    @Query("SELECT * FROM payments WHERE remoteId = :remoteId LIMIT 1")
    fun getPaymentByRemoteId(remoteId: String): PaymentEntity?

    @Query("UPDATE payments SET synced = 1 WHERE id IN (:ids)")
    fun markPaymentsSynced(ids: List<Int>)

    @Query("UPDATE payments SET deleted = 1, synced = 0, updatedAt = :updatedAt WHERE id = :paymentId")
    fun softDeletePayment(paymentId: Int, updatedAt: Long)
}

data class RevenuePointRow(
    val label: String,
    val value: Double
)

data class PaymentDistributionRow(
    val label: String,
    val value: Int
)