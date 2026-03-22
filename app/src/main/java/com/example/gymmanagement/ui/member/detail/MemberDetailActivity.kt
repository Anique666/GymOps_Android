package com.example.gymmanagement.ui.member.detail

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.gymmanagement.R
import com.example.gymmanagement.data.local.entity.Member
import com.example.gymmanagement.ui.member.addedit.AddEditMemberActivity
import com.example.gymmanagement.utils.DateUtils
import com.example.gymmanagement.utils.MembershipStatusHelper
import com.google.android.material.button.MaterialButton

class MemberDetailActivity : AppCompatActivity() {

    private val viewModel: MemberDetailViewModel by viewModels()

    private var currentMember: Member? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_member_detail)

        val memberId = intent.getIntExtra(EXTRA_MEMBER_ID, -1)
        if (memberId == -1) {
            finish()
            return
        }

        val tvName: TextView = findViewById(R.id.tvDetailName)
        val tvPhone: TextView = findViewById(R.id.tvDetailPhone)
        val tvJoinDate: TextView = findViewById(R.id.tvDetailJoinDate)
        val tvExpiryDate: TextView = findViewById(R.id.tvDetailExpiryDate)
        val tvPayment: TextView = findViewById(R.id.tvDetailPayment)
        val tvStatus: TextView = findViewById(R.id.tvDetailStatus)

        val btnEdit: MaterialButton = findViewById(R.id.btnEditMember)
        val btnDelete: MaterialButton = findViewById(R.id.btnDeleteMember)

        viewModel.getMemberById(memberId).observe(this) { member ->
            member ?: return@observe
            currentMember = member

            tvName.text = member.name
            tvPhone.text = "Phone: ${member.phone}"
            tvJoinDate.text = "Join Date: ${DateUtils.formatDate(member.joinDate)}"
            tvExpiryDate.text = "Expiry Date: ${DateUtils.formatDate(member.expiryDate)}"
            tvPayment.text = if (member.paymentStatus) "Payment: Paid" else "Payment: Pending"
            tvStatus.text = "Status: ${MembershipStatusHelper.statusLabel(member.expiryDate)}"
        }

        btnEdit.setOnClickListener {
            val member = currentMember ?: return@setOnClickListener
            val intent = Intent(this, AddEditMemberActivity::class.java)
            intent.putExtra(AddEditMemberActivity.EXTRA_MEMBER_ID, member.id)
            startActivity(intent)
        }

        btnDelete.setOnClickListener {
            val member = currentMember ?: return@setOnClickListener
            AlertDialog.Builder(this)
                .setTitle("Delete Member")
                .setMessage("Are you sure you want to delete this member?")
                .setPositiveButton("Delete") { _, _ ->
                    viewModel.deleteMember(member)
                    finish()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    companion object {
        const val EXTRA_MEMBER_ID = "extra_member_id"
    }
}
