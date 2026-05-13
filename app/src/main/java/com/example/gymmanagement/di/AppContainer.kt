package com.example.gymmanagement.di

import android.content.Context
import com.example.gymmanagement.data.local.GymDatabase
import com.example.gymmanagement.data.repository.DashboardRepository
import com.example.gymmanagement.data.repository.InventoryRepository
import com.example.gymmanagement.data.repository.MemberRepository
import com.example.gymmanagement.data.repository.PaymentRepository
import com.example.gymmanagement.data.repository.PlanRepository
import com.example.gymmanagement.data.repository.ReportsRepository
import com.example.gymmanagement.data.sync.SyncManager
import com.google.firebase.FirebaseApp

class AppContainer(context: Context) {

    private val database: GymDatabase = GymDatabase.getInstance(context)

    private val syncManager = run {
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context)
        }
        SyncManager(context, database)
    }

    init {
        syncManager.enqueueSync()
    }

    val memberRepository = MemberRepository(database.memberDao(), syncManager)
    val planRepository = PlanRepository(database.planDao(), syncManager)
    val dashboardRepository = DashboardRepository(database.memberDao())
    val paymentRepository = PaymentRepository(database.paymentDao(), syncManager)
    val reportsRepository = ReportsRepository(database.paymentDao(), database.reportsDao())
    val inventoryRepository = InventoryRepository(database.equipmentDao(), database.maintenanceDao(), syncManager)

    fun syncManager(): SyncManager = syncManager
}
