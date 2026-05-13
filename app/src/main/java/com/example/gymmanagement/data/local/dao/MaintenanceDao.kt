package com.example.gymmanagement.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.gymmanagement.data.local.entity.MaintenanceEntity

@Dao
interface MaintenanceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMaintenanceRecord(record: MaintenanceEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMaintenanceRecords(records: List<MaintenanceEntity>)

    @Query(
        """
        UPDATE maintenance_records
        SET status = :status, resolvedDate = :resolvedDate, updatedAt = :updatedAt, synced = 0
        WHERE id = (
            SELECT id FROM maintenance_records
            WHERE equipmentId = :equipmentId AND status IN ('OPEN', 'IN_PROGRESS')
            ORDER BY reportedDate DESC
            LIMIT 1
        )
        """
    )
    fun resolveLatestTicket(equipmentId: Int, status: String, resolvedDate: Long, updatedAt: Long): Int

    @Query("SELECT * FROM maintenance_records WHERE synced = 0")
    fun getPendingSyncMaintenance(): List<MaintenanceEntity>

    @Query("SELECT * FROM maintenance_records WHERE remoteId = :remoteId LIMIT 1")
    fun getMaintenanceByRemoteId(remoteId: String): MaintenanceEntity?

    @Query("UPDATE maintenance_records SET synced = 1 WHERE id IN (:ids)")
    fun markMaintenanceSynced(ids: List<Int>)
}
