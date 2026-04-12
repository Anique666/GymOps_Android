package com.example.gymmanagement.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gymmanagement.R
import com.example.gymmanagement.ui.common.BottomNavHelper
import com.example.gymmanagement.ui.member.addedit.AddEditMemberActivity
import com.example.gymmanagement.ui.member.detail.MemberDetailActivity
import com.example.gymmanagement.ui.member.list.MemberListActivity
import com.example.gymmanagement.ui.plan.PlanManagementActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class DashboardActivity : AppCompatActivity() {

    private val viewModel: DashboardViewModel by viewModels()

    private lateinit var recentAdapter: RecentCheckInAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val tvTotal: TextView = findViewById(R.id.tvTotalMembers)
        val tvActive: TextView = findViewById(R.id.tvActiveMembers)
        val tvExpired: TextView = findViewById(R.id.tvExpiredMembers)
        val tvExpiringSoon: TextView = findViewById(R.id.tvExpiringSoon)

        val btnManageMembers: View = findViewById(R.id.btnManageMembers)
        val btnManagePlans: View = findViewById(R.id.btnManagePlans)
        val btnQuickAddMember: View = findViewById(R.id.btnQuickAddMember)
        val tvViewAll: TextView = findViewById(R.id.tvViewAll)
        val bottomNav: BottomNavigationView = findViewById(R.id.bottomNav)
        val rvRecentCheckins: RecyclerView = findViewById(R.id.rvRecentCheckins)

        recentAdapter = RecentCheckInAdapter { member ->
            val intent = Intent(this, MemberDetailActivity::class.java)
            intent.putExtra(MemberDetailActivity.EXTRA_MEMBER_ID, member.id)
            startActivity(intent)
        }
        rvRecentCheckins.layoutManager = LinearLayoutManager(this)
        rvRecentCheckins.adapter = recentAdapter

        BottomNavHelper.setup(bottomNav, R.id.navDashboard, this)

        viewModel.totalMembers.observe(this) { tvTotal.text = it.toString() }
        viewModel.activeMembers.observe(this) { tvActive.text = it.toString() }
        viewModel.expiredMembers.observe(this) { tvExpired.text = it.toString() }
        viewModel.expiringSoonMembers.observe(this) { tvExpiringSoon.text = it.toString() }
        viewModel.recentMembers.observe(this) { recentAdapter.submitList(it) }

        btnManageMembers.setOnClickListener {
            startActivity(Intent(this, MemberListActivity::class.java))
        }

        btnManagePlans.setOnClickListener {
            startActivity(Intent(this, PlanManagementActivity::class.java))
        }

        tvViewAll.setOnClickListener {
            startActivity(Intent(this, MemberListActivity::class.java))
        }

        btnQuickAddMember.setOnClickListener {
            startActivity(Intent(this, AddEditMemberActivity::class.java))
        }

        findViewById<View>(R.id.cardHero).apply {
            alpha = 0f
            translationY = 20f
            animate().alpha(1f).translationY(0f).setDuration(280).start()
        }
    }

    override fun onResume() {
        super.onResume()
        // Refreshes time-sensitive dashboard counts (active/expired/expiring soon).
        viewModel.refreshDashboard()
    }
}
