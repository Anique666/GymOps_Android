package com.example.gymmanagement.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ReportsDao {

    @Query("SELECT COUNT(*) FROM members WHERE deleted = 0 AND joinDate BETWEEN :startTime AND :endTime")
    fun observeNewMembersCount(startTime: Long, endTime: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM members WHERE deleted = 0 AND expiryDate BETWEEN :startTime AND :endTime")
    fun observeExpiredMembersCount(startTime: Long, endTime: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM members WHERE deleted = 0 AND expiryDate BETWEEN :now AND :threshold")
    fun observeExpiringSoonCount(now: Long, threshold: Long): Flow<Int>

    @Query(
        """
        SELECT COALESCE(NULLIF(UPPER(TRIM(gender)), ''), 'UNSPECIFIED') AS label, COUNT(*) AS value
        FROM members
        WHERE deleted = 0
        GROUP BY label
        ORDER BY value DESC
        """
    )
    fun observeGenderDistribution(): Flow<List<DemographicCountRow>>

    @Query("SELECT dateOfBirth FROM members WHERE deleted = 0 AND dateOfBirth > 0")
    fun observeDateOfBirths(): Flow<List<Long>>
}

data class DemographicCountRow(
    val label: String,
    val value: Int
)