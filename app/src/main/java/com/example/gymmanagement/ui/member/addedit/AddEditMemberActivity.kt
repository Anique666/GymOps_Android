package com.example.gymmanagement.ui.member.addedit

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.gymmanagement.R
import com.example.gymmanagement.data.local.entity.Plan
import com.example.gymmanagement.utils.DateUtils
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import java.util.Calendar

class AddEditMemberActivity : AppCompatActivity() {

    private val viewModel: AddEditMemberViewModel by viewModels()

    private lateinit var etName: TextInputEditText
    private lateinit var etPhone: TextInputEditText
    private lateinit var tvJoinDate: TextView
    private lateinit var btnPickDate: Button
    private lateinit var spinnerPlans: Spinner
    private lateinit var switchPaymentStatus: SwitchMaterial
    private lateinit var btnSaveMember: MaterialButton

    private var joinDateMillis: Long = DateUtils.todayStartMillis()
    private var editingMemberId: Int? = null
    private var editingPlanId: Int? = null

    private var plans: List<Plan> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_member)

        bindViews()
        setupListeners()
        observePlans()

        editingMemberId = intent.getIntExtra(EXTRA_MEMBER_ID, -1).takeIf { it != -1 }
        if (editingMemberId != null) {
            title = "Edit Member"
            observeEditingMember(editingMemberId!!)
        } else {
            tvJoinDate.text = DateUtils.formatDate(joinDateMillis)
        }
    }

    private fun bindViews() {
        etName = findViewById(R.id.etName)
        etPhone = findViewById(R.id.etPhone)
        tvJoinDate = findViewById(R.id.tvJoinDate)
        btnPickDate = findViewById(R.id.btnPickDate)
        spinnerPlans = findViewById(R.id.spinnerPlans)
        switchPaymentStatus = findViewById(R.id.switchPaymentStatus)
        btnSaveMember = findViewById(R.id.btnSaveMember)
    }

    private fun setupListeners() {
        btnPickDate.setOnClickListener {
            val calendar = Calendar.getInstance().apply { timeInMillis = joinDateMillis }
            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    val picked = Calendar.getInstance().apply {
                        set(Calendar.YEAR, year)
                        set(Calendar.MONTH, month)
                        set(Calendar.DAY_OF_MONTH, dayOfMonth)
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    joinDateMillis = picked.timeInMillis
                    tvJoinDate.text = DateUtils.formatDate(joinDateMillis)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        btnSaveMember.setOnClickListener {
            saveMember()
        }
    }

    private fun observePlans() {
        viewModel.getAllPlans().observe(this) { planList ->
            plans = planList
            val names = if (planList.isEmpty()) {
                listOf("No plans available")
            } else {
                planList.map { "${it.name} (${it.durationDays} days)" }
            }

            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, names)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerPlans.adapter = adapter

            val planId = editingPlanId
            if (planId != null) {
                val index = plans.indexOfFirst { it.id == planId }
                if (index >= 0) {
                    spinnerPlans.setSelection(index)
                }
            }
        }
    }

    private fun observeEditingMember(memberId: Int) {
        viewModel.getMemberById(memberId).observe(this) { member ->
            member ?: return@observe
            etName.setText(member.name)
            etPhone.setText(member.phone)
            joinDateMillis = member.joinDate
            tvJoinDate.text = DateUtils.formatDate(joinDateMillis)
            switchPaymentStatus.isChecked = member.paymentStatus
            editingPlanId = member.planId

            val index = plans.indexOfFirst { it.id == member.planId }
            if (index >= 0) {
                spinnerPlans.setSelection(index)
            }
        }
    }

    private fun saveMember() {
        val name = etName.text?.toString()?.trim().orEmpty()
        val phone = etPhone.text?.toString()?.trim().orEmpty()

        if (name.isBlank()) {
            etName.error = "Name is required"
            return
        }

        if (phone.isBlank()) {
            etPhone.error = "Phone is required"
            return
        }

        if (plans.isEmpty()) {
            Toast.makeText(this, "Please create a plan first", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedPlan = plans[spinnerPlans.selectedItemPosition]

        viewModel.saveMember(
            existingId = editingMemberId,
            name = name,
            phone = phone,
            joinDate = joinDateMillis,
            planId = selectedPlan.id,
            durationDays = selectedPlan.durationDays,
            paymentStatus = switchPaymentStatus.isChecked
        )

        finish()
    }

    companion object {
        const val EXTRA_MEMBER_ID = "extra_member_id"
    }
}
