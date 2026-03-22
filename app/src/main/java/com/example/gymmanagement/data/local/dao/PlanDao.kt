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

    @Update
    fun updatePlan(plan: Plan)

    @Delete
    fun deletePlan(plan: Plan)

    @Query("SELECT * FROM plans ORDER BY name ASC")
    fun getAllPlans(): LiveData<List<Plan>>

    @Query("SELECT * FROM plans WHERE id = :planId LIMIT 1")
    fun getPlanById(planId: Int): Plan?
}
