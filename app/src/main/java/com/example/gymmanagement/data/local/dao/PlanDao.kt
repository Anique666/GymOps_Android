package com.example.gymmanagement.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.gymmanagement.data.local.entity.Plan

@Dao
interface PlanDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPlan(plan: Plan): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPlans(plans: List<Plan>)

    @Update
    fun updatePlan(plan: Plan)

    @Delete
    fun deletePlan(plan: Plan)

    @Query("SELECT * FROM plans WHERE deleted = 0 ORDER BY name ASC")
    fun getAllPlans(): LiveData<List<Plan>>

    @Query("SELECT * FROM plans WHERE deleted = 0 ORDER BY id ASC")
    fun getAllPlansImmediate(): List<Plan>

    @Query("SELECT * FROM plans WHERE id = :planId AND deleted = 0 LIMIT 1")
    fun getPlanById(planId: Int): Plan?

    @Query("SELECT * FROM plans WHERE synced = 0")
    fun getPendingSyncPlans(): List<Plan>

    @Query("SELECT * FROM plans WHERE remoteId = :remoteId LIMIT 1")
    fun getPlanByRemoteId(remoteId: String): Plan?

    @Query("SELECT id FROM plans WHERE remoteId = :remoteId LIMIT 1")
    fun getPlanIdByRemoteId(remoteId: String): Int?

    @Query("SELECT remoteId FROM plans WHERE id = :planId LIMIT 1")
    fun getPlanRemoteIdById(planId: Int): String?

    @Query("UPDATE plans SET synced = 1 WHERE id IN (:ids)")
    fun markPlansSynced(ids: List<Int>)

    @Query("UPDATE plans SET deleted = 1, synced = 0, updatedAt = :updatedAt WHERE id = :planId")
    fun softDeletePlan(planId: Int, updatedAt: Long)
}
