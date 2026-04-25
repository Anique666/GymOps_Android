package com.example.gymmanagement.ui.common

import android.app.Activity
import android.content.Intent
import com.example.gymmanagement.R
import com.example.gymmanagement.ui.dashboard.DashboardActivity
import com.example.gymmanagement.ui.inventory.InventoryActivity
import com.example.gymmanagement.ui.member.list.MemberListActivity
import com.example.gymmanagement.ui.reports.ReportsActivity
import com.example.gymmanagement.ui.plan.PlanManagementActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

object BottomNavHelper {

    fun setup(bottomNav: BottomNavigationView, currentItemId: Int, activity: Activity) {
        bottomNav.selectedItemId = currentItemId
        bottomNav.setOnItemSelectedListener { item ->
            navigate(activity, currentItemId, item.itemId)
        }
    }

    fun navigate(activity: Activity, currentItemId: Int, targetItemId: Int): Boolean {
        val destination = when (targetItemId) {
            R.id.navDashboard -> DashboardActivity::class.java
            R.id.navMembers -> MemberListActivity::class.java
            R.id.navInventory -> InventoryActivity::class.java
            R.id.navPlans -> PlanManagementActivity::class.java
            R.id.navReports -> ReportsActivity::class.java
            else -> return false
        }
        return navigateIfNeeded(activity, destination, currentItemId, targetItemId)
    }

    private fun navigateIfNeeded(
        activity: Activity,
        destination: Class<*>,
        currentItemId: Int,
        targetItemId: Int
    ): Boolean {
        if (currentItemId == targetItemId) {
            return true
        }

        val intent = Intent(activity, destination).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        activity.startActivity(intent)
        return true
    }
}
