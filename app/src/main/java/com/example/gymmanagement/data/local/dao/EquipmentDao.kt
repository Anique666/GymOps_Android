package com.example.gymmanagement.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.gymmanagement.data.local.entity.EquipmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EquipmentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertEquipment(equipment: EquipmentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertEquipments(equipment: List<EquipmentEntity>)

    @Update
    fun updateEquipment(equipment: EquipmentEntity)

    @Query("SELECT * FROM equipment WHERE deleted = 0 ORDER BY name ASC")
    fun observeAllEquipment(): Flow<List<EquipmentEntity>>

    @Query("SELECT COUNT(*) FROM equipment WHERE deleted = 0")
    fun getEquipmentCountImmediate(): Int

    @Query("SELECT * FROM equipment WHERE synced = 0")
    fun getPendingSyncEquipment(): List<EquipmentEntity>

    @Query("SELECT * FROM equipment WHERE remoteId = :remoteId LIMIT 1")
    fun getEquipmentByRemoteId(remoteId: String): EquipmentEntity?

    @Query("SELECT id FROM equipment WHERE remoteId = :remoteId LIMIT 1")
    fun getEquipmentIdByRemoteId(remoteId: String): Int?

    @Query("SELECT remoteId FROM equipment WHERE id = :equipmentId LIMIT 1")
    fun getEquipmentRemoteIdById(equipmentId: Int): String?

    @Query("UPDATE equipment SET synced = 1 WHERE id IN (:ids)")
    fun markEquipmentSynced(ids: List<Int>)

    @Query("UPDATE equipment SET deleted = 1, synced = 0, updatedAt = :updatedAt WHERE id = :equipmentId")
    fun softDeleteEquipment(equipmentId: Int, updatedAt: Long)
}
