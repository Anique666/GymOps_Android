package com.example.gymmanagement.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.gymmanagement.data.local.entity.Member
import com.example.gymmanagement.data.local.model.MemberBillingSummary

@Dao
interface MemberDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMember(member: Member): Long

    @Update
    fun updateMember(member: Member)

    @Delete
    fun deleteMember(member: Member)

    @Query("SELECT * FROM members ORDER BY name ASC")
    fun getAllMembers(): LiveData<List<Member>>

    @Query(
        """
        SELECT
            m.*,
            p.name AS planName,
            p.durationDays AS planDurationDays,
            p.price AS planPrice,
            COALESCE(lp.amount, 0) AS latestPaymentAmount,
            COALESCE(lp.paymentMethod, 'CASH') AS latestPaymentMethod,
            COALESCE(lp.status, 'PENDING') AS latestPaymentStatus,
            COALESCE(lp.paymentDate, 0) AS latestPaymentDate,
            CASE
                WHEN p.price > COALESCE(lp.amount, 0) THEN p.price - COALESCE(lp.amount, 0)
                ELSE 0
            END AS pendingAmount
        FROM members m
        INNER JOIN plans p ON p.id = m.planId
        LEFT JOIN payments lp ON lp.id = (
            SELECT id
            FROM payments
            WHERE memberId = m.id
            ORDER BY paymentDate DESC
            LIMIT 1
        )
        ORDER BY m.name ASC
        """
    )
    fun getAllMemberBillingSummaries(): LiveData<List<MemberBillingSummary>>

    @Query(
        """
        SELECT
            m.*,
            p.name AS planName,
            p.durationDays AS planDurationDays,
            p.price AS planPrice,
            COALESCE(lp.amount, 0) AS latestPaymentAmount,
            COALESCE(lp.paymentMethod, 'CASH') AS latestPaymentMethod,
            COALESCE(lp.status, 'PENDING') AS latestPaymentStatus,
            COALESCE(lp.paymentDate, 0) AS latestPaymentDate,
            CASE
                WHEN p.price > COALESCE(lp.amount, 0) THEN p.price - COALESCE(lp.amount, 0)
                ELSE 0
            END AS pendingAmount
        FROM members m
        INNER JOIN plans p ON p.id = m.planId
        LEFT JOIN payments lp ON lp.id = (
            SELECT id
            FROM payments
            WHERE memberId = m.id
            ORDER BY paymentDate DESC
            LIMIT 1
        )
        WHERE m.id = :memberId
        LIMIT 1
        """
    )
    fun getMemberBillingSummaryById(memberId: Int): LiveData<MemberBillingSummary?>

    @Query("SELECT * FROM members WHERE id = :memberId LIMIT 1")
    fun getMemberById(memberId: Int): LiveData<Member?>

    @Query(
        "SELECT * FROM members WHERE name LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%' ORDER BY name ASC"
    )
    fun searchMembers(query: String): LiveData<List<Member>>

    @Query(
        """
        SELECT
            m.*,
            p.name AS planName,
            p.durationDays AS planDurationDays,
            p.price AS planPrice,
            COALESCE(lp.amount, 0) AS latestPaymentAmount,
            COALESCE(lp.paymentMethod, 'CASH') AS latestPaymentMethod,
            COALESCE(lp.status, 'PENDING') AS latestPaymentStatus,
            COALESCE(lp.paymentDate, 0) AS latestPaymentDate,
            CASE
                WHEN p.price > COALESCE(lp.amount, 0) THEN p.price - COALESCE(lp.amount, 0)
                ELSE 0
            END AS pendingAmount
        FROM members m
        INNER JOIN plans p ON p.id = m.planId
        LEFT JOIN payments lp ON lp.id = (
            SELECT id
            FROM payments
            WHERE memberId = m.id
            ORDER BY paymentDate DESC
            LIMIT 1
        )
        WHERE m.name LIKE '%' || :query || '%' OR m.phone LIKE '%' || :query || '%'
        ORDER BY m.name ASC
        """
    )
    fun searchMemberBillingSummaries(query: String): LiveData<List<MemberBillingSummary>>

    @Query("SELECT COUNT(*) FROM members")
    fun getTotalMembersCount(): LiveData<Int>

    @Query("SELECT COUNT(*) FROM members")
    fun getMembersCountImmediate(): Int

    @Query("SELECT EXISTS(SELECT 1 FROM members WHERE phone = :phone LIMIT 1)")
    fun memberExistsByPhone(phone: String): Boolean

    @Query("SELECT COUNT(*) FROM members WHERE expiryDate >= :currentTime")
    fun getActiveMembersCount(currentTime: Long): LiveData<Int>

    @Query("SELECT COUNT(*) FROM members WHERE expiryDate < :currentTime")
    fun getExpiredMembersCount(currentTime: Long): LiveData<Int>

    @Query("SELECT COUNT(*) FROM members WHERE expiryDate >= :currentTime AND expiryDate <= :expiringThreshold")
    fun getExpiringSoonCount(currentTime: Long, expiringThreshold: Long): LiveData<Int>
}
