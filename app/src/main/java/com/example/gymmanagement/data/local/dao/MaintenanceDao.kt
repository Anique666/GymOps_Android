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

    @Query(
        """
        UPDATE maintenance_records
        SET status = :status, resolvedDate = :resolvedDate
        WHERE id = (
            SELECT id FROM maintenance_records
            WHERE equipmentId = :equipmentId AND status IN ('OPEN', 'IN_PROGRESS')
            ORDER BY reportedDate DESC
            LIMIT 1
        )
        """
    )
    fun resolveLatestTicket(equipmentId: Int, status: String, resolvedDate: Long): Int
}
