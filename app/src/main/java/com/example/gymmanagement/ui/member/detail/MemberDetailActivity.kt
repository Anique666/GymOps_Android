package com.example.gymmanagement.ui.member.detail

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.example.gymmanagement.R
import com.example.gymmanagement.data.local.model.MemberBillingSummary
import com.example.gymmanagement.ui.common.AppBottomBar
import com.example.gymmanagement.ui.common.BottomNavHelper
import com.example.gymmanagement.ui.member.addedit.AddEditMemberActivity
import com.example.gymmanagement.utils.DateUtils
import com.example.gymmanagement.utils.MembershipStatusHelper
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton

class MemberDetailActivity : AppCompatActivity() {

    private val viewModel: MemberDetailViewModel by viewModels()

    private var currentSummary: MemberBillingSummary? = null

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
        val tvPlanSubtitle: TextView = findViewById(R.id.tvPlanSubtitle)
        val tvLastBilling: TextView = findViewById(R.id.tvLastBilling)
        val tvPendingAmount: TextView = findViewById(R.id.tvPendingAmountDetail)
        val tvAvatarLarge: TextView = findViewById(R.id.tvAvatarLarge)
        val ivPaymentState: ImageView = findViewById(R.id.ivPaymentState)

        findViewById<MaterialToolbar>(R.id.toolbarMemberDetail).setNavigationOnClickListener { finish() }
        setupBottomNav()

        val btnEdit: MaterialButton = findViewById(R.id.btnEditMember)
        val btnRenew: MaterialButton = findViewById(R.id.btnRenewPlan)
        val btnDelete: MaterialButton = findViewById(R.id.btnDeleteMember)

        viewModel.getMemberBillingSummaryById(memberId).observe(this) { summary ->
            summary ?: return@observe
            currentSummary = summary
            val member = summary.member

            tvName.text = member.name
            tvPhone.text = member.phone
            tvAvatarLarge.text = member.name.firstOrNull()?.uppercaseChar()?.toString().orEmpty()
            tvJoinDate.text = DateUtils.formatDate(member.joinDate)
            tvExpiryDate.text = DateUtils.formatDate(member.expiryDate)
            tvPayment.text = if (summary.pendingAmount <= 0.0) getString(R.string.label_paid) else getString(R.string.label_pending)
            tvStatus.text = MembershipStatusHelper.statusLabel(member.expiryDate)
            tvPlanSubtitle.text = summary.planName
            tvLastBilling.text = getString(
                R.string.label_last_billing,
                if (summary.latestPaymentDate > 0L) DateUtils.formatCardDate(summary.latestPaymentDate) else DateUtils.formatCardDate(member.joinDate)
            )
            if (summary.pendingAmount > 0.0) {
                tvPendingAmount.text = getString(R.string.label_pending_amount, summary.pendingAmount)
                tvPendingAmount.visibility = View.VISIBLE
            } else {
                tvPendingAmount.visibility = View.GONE
            }
            ivPaymentState.setColorFilter(
                if (summary.pendingAmount <= 0.0) {
                    ContextCompat.getColor(this, R.color.success)
                } else {
                    ContextCompat.getColor(this, R.color.warning)
                }
            )
        }

        btnEdit.setOnClickListener {
            val member = currentSummary?.member ?: return@setOnClickListener
            startActivity(Intent(this, AddEditMemberActivity::class.java).apply {
                putExtra(AddEditMemberActivity.EXTRA_MEMBER_ID, member.id)
            })
        }

        btnRenew.setOnClickListener {
            val summary = currentSummary ?: return@setOnClickListener
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.action_renew_plan))
                .setMessage(getString(R.string.dialog_renew_member_message, summary.planName, summary.member.name))
                .setPositiveButton(getString(R.string.action_renew_plan)) { _, _ ->
                    viewModel.renewMember(summary)
                }
                .setNegativeButton(getString(R.string.dialog_cancel), null)
                .show()
        }

        btnDelete.setOnClickListener {
            val member = currentSummary?.member ?: return@setOnClickListener
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.dialog_delete_member_title))
                .setMessage(getString(R.string.dialog_delete_member_message))
                .setPositiveButton(getString(R.string.action_delete_member)) { _, _ ->
                    viewModel.deleteMember(member)
                    finish()
                }
                .setNegativeButton(getString(R.string.dialog_cancel), null)
                .show()
        }
    }

    companion object {
        const val EXTRA_MEMBER_ID = "extra_member_id"
    }

    private fun setupBottomNav() {
        val bottomNav: ComposeView = findViewById(R.id.bottomNav)
        bottomNav.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        bottomNav.setContent {
            AppBottomBar(selectedItemId = R.id.navMembers) { itemId ->
                BottomNavHelper.navigate(this@MemberDetailActivity, R.id.navMembers, itemId)
            }
        }
    }
}
