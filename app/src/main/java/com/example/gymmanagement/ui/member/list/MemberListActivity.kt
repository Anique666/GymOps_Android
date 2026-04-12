package com.example.gymmanagement.ui.member.list

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gymmanagement.R
import com.example.gymmanagement.ui.common.BottomNavHelper
import com.example.gymmanagement.ui.member.addedit.AddEditMemberActivity
import com.example.gymmanagement.ui.member.detail.MemberDetailActivity
import com.example.gymmanagement.utils.MembershipStatusHelper
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText

class MemberListActivity : AppCompatActivity() {

    private val viewModel: MemberListViewModel by viewModels()

    private lateinit var memberAdapter: MemberAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_member_list)

        setupBottomNav()
        setupRecyclerView()
        setupSearch()
        setupFab()
        observeMembers()
    }

    private fun setupBottomNav() {
        val bottomNav: BottomNavigationView = findViewById(R.id.bottomNav)
        BottomNavHelper.setup(bottomNav, R.id.navMembers, this)
    }

    private fun setupRecyclerView() {
        val recyclerView: RecyclerView = findViewById(R.id.rvMembers)
        memberAdapter = MemberAdapter { member ->
            val intent = Intent(this, MemberDetailActivity::class.java)
            intent.putExtra(MemberDetailActivity.EXTRA_MEMBER_ID, member.id)
            startActivity(intent)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = memberAdapter
    }

    private fun setupSearch() {
        val etSearch: TextInputEditText = findViewById(R.id.etSearch)
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.setSearchQuery(s?.toString().orEmpty())
            }

            override fun afterTextChanged(s: Editable?) = Unit
        })
    }

    private fun setupFab() {
        val fabAddMember: FloatingActionButton = findViewById(R.id.fabAddMember)
        fabAddMember.setOnClickListener {
            startActivity(Intent(this, AddEditMemberActivity::class.java))
        }
    }

    private fun observeMembers() {
        val tvTotalMembersCount: TextView = findViewById(R.id.tvTotalMembersCount)
        val tvActiveNowCount: TextView = findViewById(R.id.tvActiveNowCount)
        val tvExpiringSoonCount: TextView = findViewById(R.id.tvExpiringSoonCount)

        viewModel.members.observe(this) { list ->
            memberAdapter.submitList(list)

            tvTotalMembersCount.text = list.size.toString()
            val activeCount = list.count { !MembershipStatusHelper.isExpired(it.expiryDate) }
            val expiringSoonCount = list.count { MembershipStatusHelper.isExpiringSoon(it.expiryDate) }
            tvActiveNowCount.text = activeCount.toString()
            tvExpiringSoonCount.text = expiringSoonCount.toString()
        }
    }
}
