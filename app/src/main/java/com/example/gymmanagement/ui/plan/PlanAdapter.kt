package com.example.gymmanagement.ui.plan

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gymmanagement.R
import com.example.gymmanagement.data.local.entity.Plan

class PlanAdapter(
    private val onEditClick: (Plan) -> Unit,
    private val onDeleteClick: (Plan) -> Unit
) : RecyclerView.Adapter<PlanAdapter.PlanViewHolder>() {

    private val plans = mutableListOf<Plan>()

    fun submitList(newList: List<Plan>) {
        plans.clear()
        plans.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlanViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_plan, parent, false)
        return PlanViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlanViewHolder, position: Int) {
        holder.bind(plans[position])
    }

    override fun getItemCount(): Int = plans.size

    inner class PlanViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvName: TextView = itemView.findViewById(R.id.tvPlanName)
        private val tvDetails: TextView = itemView.findViewById(R.id.tvPlanDetails)
        private val btnEdit: ImageButton = itemView.findViewById(R.id.btnEditPlan)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDeletePlan)

        fun bind(plan: Plan) {
            tvName.text = plan.name
            tvDetails.text = "${plan.durationDays} days - Rs. ${plan.price}"

            btnEdit.setOnClickListener { onEditClick(plan) }
            btnDelete.setOnClickListener { onDeleteClick(plan) }
        }
    }
}
