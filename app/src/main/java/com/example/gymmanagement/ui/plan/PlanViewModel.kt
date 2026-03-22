package com.example.gymmanagement.ui.plan

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.gymmanagement.data.local.entity.Plan
import com.example.gymmanagement.data.repository.PlanRepository
import com.example.gymmanagement.di.AppContainer

class PlanViewModel(application: Application) : AndroidViewModel(application) {

    private val planRepository: PlanRepository = AppContainer(application).planRepository

    val plans: LiveData<List<Plan>> = planRepository.getAllPlans()

    fun insertPlan(name: String, durationDays: Int, price: Double) {
        planRepository.insertPlan(
            Plan(
                name = name,
                durationDays = durationDays,
                price = price
            )
        )
    }

    fun updatePlan(plan: Plan) {
        planRepository.updatePlan(plan)
    }

    fun deletePlan(plan: Plan) {
        planRepository.deletePlan(plan)
    }
}
