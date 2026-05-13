package com.example.gymmanagement.data.repository

import androidx.lifecycle.LiveData
import com.example.gymmanagement.data.local.dao.PlanDao
import com.example.gymmanagement.data.local.entity.Plan
import com.example.gymmanagement.data.sync.SyncManager
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.UUID

class PlanRepository(
    private val planDao: PlanDao,
    private val syncManager: SyncManager? = null
) {

    private val ioExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    fun getAllPlans(): LiveData<List<Plan>> = planDao.getAllPlans()

    fun insertPlan(plan: Plan) {
        ioExecutor.execute {
            val now = System.currentTimeMillis()
            val prepared = plan.copy(
                remoteId = plan.remoteId.ifBlank { UUID.randomUUID().toString() },
                updatedAt = now,
                synced = false,
                deleted = false
            )
            planDao.insertPlan(prepared)
            syncManager?.enqueueSync()
        }
    }

    fun updatePlan(plan: Plan) {
        ioExecutor.execute {
            val now = System.currentTimeMillis()
            val prepared = plan.copy(
                remoteId = plan.remoteId.ifBlank { UUID.randomUUID().toString() },
                updatedAt = now,
                synced = false
            )
            planDao.updatePlan(prepared)
            syncManager?.enqueueSync()
        }
    }

    fun deletePlan(plan: Plan) {
        ioExecutor.execute {
            val now = System.currentTimeMillis()
            planDao.softDeletePlan(plan.id, now)
            syncManager?.enqueueSync()
        }
    }
}
