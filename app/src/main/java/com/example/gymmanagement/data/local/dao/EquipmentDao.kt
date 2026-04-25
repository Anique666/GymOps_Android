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

    @Update
    fun updateEquipment(equipment: EquipmentEntity)

    @Query("SELECT * FROM equipment ORDER BY name ASC")
    fun observeAllEquipment(): Flow<List<EquipmentEntity>>

    @Query("SELECT COUNT(*) FROM equipment")
    fun getEquipmentCountImmediate(): Int
}
