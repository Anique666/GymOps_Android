package com.example.gymmanagement.ui.member.addedit

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.example.gymmanagement.R
import com.example.gymmanagement.data.local.entity.Plan
import com.example.gymmanagement.data.local.model.PaymentMethod
import com.example.gymmanagement.data.local.model.PaymentStatus
import com.example.gymmanagement.ui.common.AppBottomBar
import com.example.gymmanagement.ui.common.BottomNavHelper
import com.example.gymmanagement.utils.DateUtils
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.appbar.MaterialToolbar
import java.util.Calendar

class AddEditMemberActivity : AppCompatActivity() {

    private val viewModel: AddEditMemberViewModel by viewModels()

    private lateinit var etName: TextInputEditText
    private lateinit var etPhone: TextInputEditText
    private lateinit var tvJoinDate: TextView
    private lateinit var tvDateOfBirth: TextInputEditText
    private lateinit var tilDateOfBirth: TextInputLayout
    private lateinit var btnPickDate: ImageButton
    private lateinit var spinnerPlans: Spinner
    private lateinit var spinnerGender: Spinner
    private lateinit var spinnerSource: Spinner
    private lateinit var spinnerPaymentMethod: Spinner
    private lateinit var etPaymentAmount: TextInputEditText
    private lateinit var spinnerPaymentStatus: Spinner
    private lateinit var btnSaveMember: MaterialButton

    private var joinDateMillis: Long = DateUtils.todayStartMillis()
    private var dateOfBirthMillis: Long = 0L
    private var editingMemberId: Int? = null
    private var editingPlanId: Int? = null
    private var editingGender: String = "UNSPECIFIED"

    private var plans: List<Plan> = emptyList()
    private val sourceOptions = listOf("referall", "walk in", "advertisments", "instagram", "other")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_member)

        bindViews()
        setupListeners()
        observePlans()
        setupBottomNav()

        editingMemberId = intent.getIntExtra(EXTRA_MEMBER_ID, -1).takeIf { it != -1 }
        if (editingMemberId != null) {
            findViewById<MaterialToolbar>(R.id.toolbarAddEditMember).title = getString(R.string.title_edit_member)
            observeEditingMember(editingMemberId!!)
        } else {
            findViewById<MaterialToolbar>(R.id.toolbarAddEditMember).title = getString(R.string.title_add_member)
            tvJoinDate.text = DateUtils.formatDate(joinDateMillis)
        }
    }

    private fun bindViews() {
        etName = findViewById(R.id.etName)
        etPhone = findViewById(R.id.etPhone)
        tvJoinDate = findViewById(R.id.tvJoinDate)
        tvDateOfBirth = findViewById(R.id.tvDateOfBirth)
        tilDateOfBirth = findViewById(R.id.tilDateOfBirth)
        btnPickDate = findViewById(R.id.btnPickDate)
        spinnerPlans = findViewById(R.id.spinnerPlans)
        spinnerGender = findViewById(R.id.spinnerGender)
        spinnerSource = findViewById(R.id.spinnerSource)
        spinnerPaymentMethod = findViewById(R.id.spinnerPaymentMethod)
        etPaymentAmount = findViewById(R.id.etPaymentAmount)
        spinnerPaymentStatus = findViewById(R.id.spinnerPaymentStatus)
        btnSaveMember = findViewById(R.id.btnSaveMember)
        findViewById<MaterialToolbar>(R.id.toolbarAddEditMember).setNavigationOnClickListener { finish() }
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

        tvDateOfBirth.keyListener = null
        tvDateOfBirth.setOnClickListener { showDobCalendar() }
        tilDateOfBirth.setEndIconOnClickListener { showDobCalendar() }

        btnSaveMember.setOnClickListener {
            saveMember()
        }
    }

    private fun showDobCalendar() {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = if (dateOfBirthMillis > 0L) dateOfBirthMillis else System.currentTimeMillis()
        }
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
                dateOfBirthMillis = picked.timeInMillis
                tvDateOfBirth.setText(DateUtils.formatDate(dateOfBirthMillis))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun observePlans() {
        viewModel.getAllPlans().observe(this) { planList ->
            plans = planList
            val names = if (planList.isEmpty()) {
                listOf(getString(R.string.hint_select_plan))
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

        val genderAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            listOf("UNSPECIFIED", "MALE", "FEMALE", "OTHER")
        )
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerGender.adapter = genderAdapter

        val sourceAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            sourceOptions
        )
        sourceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSource.adapter = sourceAdapter

        val paymentMethodAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            PaymentMethod.values().map { method -> method.name }
        )
        paymentMethodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPaymentMethod.adapter = paymentMethodAdapter

        val paymentStatusAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            PaymentStatus.values().map { status -> status.name }
        )
        paymentStatusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPaymentStatus.adapter = paymentStatusAdapter
    }

    private fun observeEditingMember(memberId: Int) {
        viewModel.getMemberById(memberId).observe(this) { member ->
            member ?: return@observe
            etName.setText(member.name)
            etPhone.setText(member.phone)
            joinDateMillis = member.joinDate
            tvJoinDate.text = DateUtils.formatDate(joinDateMillis)
            dateOfBirthMillis = member.dateOfBirth
            tvDateOfBirth.setText(if (member.dateOfBirth > 0L) DateUtils.formatDate(member.dateOfBirth) else "")
            editingGender = member.gender
            editingPlanId = member.planId

            val index = plans.indexOfFirst { it.id == member.planId }
            if (index >= 0) {
                spinnerPlans.setSelection(index)
            }

            val genderIndex = listOf("UNSPECIFIED", "MALE", "FEMALE", "OTHER").indexOfFirst { it.equals(editingGender, ignoreCase = true) }
            if (genderIndex >= 0) {
                spinnerGender.setSelection(genderIndex)
            }

            val sourceIndex = sourceOptions.indexOfFirst { option -> option.equals(member.source, ignoreCase = true) }
            spinnerSource.setSelection(if (sourceIndex >= 0) sourceIndex else sourceOptions.lastIndex)

            spinnerPaymentStatus.setSelection(if (member.paymentStatus) 0 else 1)
            spinnerPaymentMethod.setSelection(0)
        }
    }

    private fun saveMember() {
        val name = etName.text?.toString()?.trim().orEmpty()
        val phone = etPhone.text?.toString()?.trim().orEmpty()

        if (name.isBlank()) {
            etName.error = getString(R.string.error_name_required)
            return
        }

        if (phone.isBlank()) {
            etPhone.error = getString(R.string.error_phone_required)
            return
        }

        if (dateOfBirthMillis <= 0L) {
            Toast.makeText(this, getString(R.string.hint_date_of_birth), Toast.LENGTH_SHORT).show()
            return
        }

        if (plans.isEmpty()) {
            Toast.makeText(this, getString(R.string.msg_no_plans_available), Toast.LENGTH_SHORT).show()
            return
        }

        val selectedPlan = plans[spinnerPlans.selectedItemPosition]
        val selectedGender = spinnerGender.selectedItem?.toString().orEmpty()
        val selectedPaymentMethod = spinnerPaymentMethod.selectedItem?.toString().orEmpty()
        val selectedPaymentStatus = spinnerPaymentStatus.selectedItem?.toString().orEmpty()
        val paymentAmountInput = etPaymentAmount.text?.toString()?.trim().orEmpty()
        val paymentAmount = paymentAmountInput.toDoubleOrNull()
            ?: if (selectedPaymentStatus.equals(PaymentStatus.PAID.name, ignoreCase = true)) selectedPlan.price else 0.0
        val source = spinnerSource.selectedItem?.toString()?.trim().orEmpty()

        viewModel.saveMember(
            existingId = editingMemberId,
            name = name,
            phone = phone,
            joinDate = joinDateMillis,
            planId = selectedPlan.id,
            durationDays = selectedPlan.durationDays,
            gender = selectedGender,
            dateOfBirth = dateOfBirthMillis,
            source = source,
            paymentMethod = selectedPaymentMethod,
            paymentAmount = paymentAmount,
            paymentStatus = selectedPaymentStatus
        )

        finish()
    }

    private fun setupBottomNav() {
        val bottomNav: ComposeView = findViewById(R.id.bottomNav)
        bottomNav.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        bottomNav.setContent {
            AppBottomBar(selectedItemId = R.id.navMembers) { itemId ->
                BottomNavHelper.navigate(this@AddEditMemberActivity, R.id.navMembers, itemId)
            }
        }
    }

    companion object {
        const val EXTRA_MEMBER_ID = "extra_member_id"
    }
}
