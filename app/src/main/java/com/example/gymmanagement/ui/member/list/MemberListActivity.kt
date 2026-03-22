package com.example.gymmanagement.ui.member.list

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gymmanagement.R
import com.example.gymmanagement.ui.member.addedit.AddEditMemberActivity
import com.example.gymmanagement.ui.member.detail.MemberDetailActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText

class MemberListActivity : AppCompatActivity() {

    private val viewModel: MemberListViewModel by viewModels()

    private lateinit var memberAdapter: MemberAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_member_list)

        setupRecyclerView()
        setupSearch()
        setupFab()
        observeMembers()
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
        viewModel.members.observe(this) { list ->
            memberAdapter.submitList(list)
        }
    }
}
