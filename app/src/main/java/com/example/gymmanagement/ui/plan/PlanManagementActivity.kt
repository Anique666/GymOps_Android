package com.example.gymmanagement.ui.plan

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gymmanagement.R
import com.example.gymmanagement.data.local.entity.Plan
import com.example.gymmanagement.ui.common.AppBottomBar
import com.example.gymmanagement.ui.common.BottomNavHelper
import com.google.android.material.floatingactionbutton.FloatingActionButton

class PlanManagementActivity : AppCompatActivity() {

    private val viewModel: PlanViewModel by viewModels()

    private lateinit var adapter: PlanAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_plan_management)

        setupBottomNav()
        setupRecyclerView()

        findViewById<FloatingActionButton>(R.id.fabAddPlan).setOnClickListener {
            showPlanDialog()
        }

        findViewById<com.google.android.material.card.MaterialCardView>(R.id.cardCreateCustomPlan).setOnClickListener {
            showPlanDialog()
        }

        val tvActivePlansCount: TextView = findViewById(R.id.tvActivePlansCount)

        viewModel.plans.observe(this) { plans ->
            adapter.submitList(plans)
            tvActivePlansCount.text = plans.size.toString().padStart(2, '0')
        }
    }

    private fun setupBottomNav() {
        val bottomNav: ComposeView = findViewById(R.id.bottomNav)
        bottomNav.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        bottomNav.setContent {
            AppBottomBar(selectedItemId = R.id.navPlans) { itemId ->
                BottomNavHelper.navigate(this@PlanManagementActivity, R.id.navPlans, itemId)
            }
        }
    }

    private fun setupRecyclerView() {
        val rvPlans: RecyclerView = findViewById(R.id.rvPlans)
        adapter = PlanAdapter(
            onEditClick = { plan -> showPlanDialog(plan) },
            onDeleteClick = { plan -> viewModel.deletePlan(plan) }
        )

        rvPlans.layoutManager = LinearLayoutManager(this)
        rvPlans.adapter = adapter
    }

    private fun showPlanDialog(existingPlan: Plan? = null) {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_plan, null)
        val etPlanName: EditText = view.findViewById(R.id.etPlanName)
        val etDuration: EditText = view.findViewById(R.id.etDurationDays)
        val etPrice: EditText = view.findViewById(R.id.etPlanPrice)

        if (existingPlan != null) {
            etPlanName.setText(existingPlan.name)
            etDuration.setText(existingPlan.durationDays.toString())
            etPrice.setText(existingPlan.price.toString())
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle(if (existingPlan == null) getString(R.string.dialog_add_plan) else getString(R.string.dialog_edit_plan))
            .setView(view)
            .setPositiveButton(getString(R.string.dialog_save), null)
            .setNegativeButton(getString(R.string.dialog_cancel), null)
            .show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val name = etPlanName.text.toString().trim()
            val duration = etDuration.text.toString().toIntOrNull() ?: 0
            val price = etPrice.text.toString().toDoubleOrNull() ?: 0.0

            var hasError = false
            if (name.isBlank()) {
                etPlanName.error = getString(R.string.error_plan_name_required)
                hasError = true
            }
            if (duration <= 0) {
                etDuration.error = getString(R.string.error_plan_duration_invalid)
                hasError = true
            }
            if (price <= 0.0) {
                etPrice.error = getString(R.string.error_plan_price_invalid)
                hasError = true
            }

            if (hasError) {
                Toast.makeText(this, getString(R.string.toast_valid_plan_details), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (existingPlan == null) {
                viewModel.insertPlan(name, duration, price)
            } else {
                viewModel.updatePlan(
                    existingPlan.copy(
                        name = name,
                        durationDays = duration,
                        price = price
                    )
                )
            }

            dialog.dismiss()
        }
    }
}
