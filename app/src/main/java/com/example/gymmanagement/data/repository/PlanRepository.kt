package com.example.gymmanagement.data.repository

import androidx.lifecycle.LiveData
import com.example.gymmanagement.data.local.dao.PlanDao
import com.example.gymmanagement.data.local.entity.Plan
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class PlanRepository(private val planDao: PlanDao) {

    private val ioExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    fun getAllPlans(): LiveData<List<Plan>> = planDao.getAllPlans()

    fun insertPlan(plan: Plan) {
        ioExecutor.execute { planDao.insertPlan(plan) }
    }

    fun updatePlan(plan: Plan) {
        ioExecutor.execute { planDao.updatePlan(plan) }
    }

    fun deletePlan(plan: Plan) {
        ioExecutor.execute { planDao.deletePlan(plan) }
    }
}
