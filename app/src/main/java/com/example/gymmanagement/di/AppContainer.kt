package com.example.gymmanagement.di

import android.content.Context
import com.example.gymmanagement.data.local.GymDatabase
import com.example.gymmanagement.data.repository.DashboardRepository
import com.example.gymmanagement.data.repository.InventoryRepository
import com.example.gymmanagement.data.repository.MemberRepository
import com.example.gymmanagement.data.repository.PaymentRepository
import com.example.gymmanagement.data.repository.PlanRepository
import com.example.gymmanagement.data.repository.ReportsRepository

class AppContainer(context: Context) {

    private val database: GymDatabase = GymDatabase.getInstance(context)

    val memberRepository = MemberRepository(database.memberDao())
    val planRepository = PlanRepository(database.planDao())
    val dashboardRepository = DashboardRepository(database.memberDao())
    val paymentRepository = PaymentRepository(database.paymentDao())
    val reportsRepository = ReportsRepository(database.paymentDao(), database.reportsDao())
    val inventoryRepository = InventoryRepository(database.equipmentDao(), database.maintenanceDao())
}
