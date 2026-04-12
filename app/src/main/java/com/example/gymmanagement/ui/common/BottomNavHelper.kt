package com.example.gymmanagement.ui.common

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import com.example.gymmanagement.R
import com.example.gymmanagement.ui.dashboard.DashboardActivity
import com.example.gymmanagement.ui.member.list.MemberListActivity
import com.example.gymmanagement.ui.plan.PlanManagementActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

object BottomNavHelper {

    fun setup(bottomNav: BottomNavigationView, currentItemId: Int, activity: Activity) {
        bottomNav.selectedItemId = currentItemId
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navDashboard -> navigateIfNeeded(activity, DashboardActivity::class.java, currentItemId, item.itemId)
                R.id.navMembers -> navigateIfNeeded(activity, MemberListActivity::class.java, currentItemId, item.itemId)
                R.id.navPlans -> navigateIfNeeded(activity, PlanManagementActivity::class.java, currentItemId, item.itemId)
                R.id.navReports -> {
                    Toast.makeText(activity, activity.getString(R.string.toast_reports_coming_soon), Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
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
