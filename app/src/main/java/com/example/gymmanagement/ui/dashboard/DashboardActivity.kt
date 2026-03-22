package com.example.gymmanagement.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.gymmanagement.R
import com.example.gymmanagement.ui.member.list.MemberListActivity
import com.example.gymmanagement.ui.plan.PlanManagementActivity
import com.google.android.material.button.MaterialButton

class DashboardActivity : AppCompatActivity() {

    private val viewModel: DashboardViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val tvTotal: TextView = findViewById(R.id.tvTotalMembers)
        val tvActive: TextView = findViewById(R.id.tvActiveMembers)
        val tvExpired: TextView = findViewById(R.id.tvExpiredMembers)
        val tvExpiringSoon: TextView = findViewById(R.id.tvExpiringSoon)

        val btnManageMembers: MaterialButton = findViewById(R.id.btnManageMembers)
        val btnManagePlans: MaterialButton = findViewById(R.id.btnManagePlans)

        viewModel.totalMembers.observe(this) { tvTotal.text = it.toString() }
        viewModel.activeMembers.observe(this) { tvActive.text = it.toString() }
        viewModel.expiredMembers.observe(this) { tvExpired.text = it.toString() }
        viewModel.expiringSoonMembers.observe(this) { tvExpiringSoon.text = it.toString() }

        btnManageMembers.setOnClickListener {
            startActivity(Intent(this, MemberListActivity::class.java))
        }

        btnManagePlans.setOnClickListener {
            startActivity(Intent(this, PlanManagementActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        // Refreshes time-sensitive dashboard counts (active/expired/expiring soon).
        viewModel.refreshDashboard()
    }
}
