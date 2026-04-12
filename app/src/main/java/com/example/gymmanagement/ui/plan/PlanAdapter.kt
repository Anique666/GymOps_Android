package com.example.gymmanagement.ui.plan

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
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
        private val tvPlanPrice: TextView = itemView.findViewById(R.id.tvPlanPrice)
        private val tvPlanDuration: TextView = itemView.findViewById(R.id.tvPlanDuration)
        private val tvPlanTag: TextView = itemView.findViewById(R.id.tvPlanTag)
        private val tvPlanMeta: TextView = itemView.findViewById(R.id.tvPlanMeta)
        private val ivPlanIcon: ImageView = itemView.findViewById(R.id.ivPlanIcon)
        private val btnEdit: ImageButton = itemView.findViewById(R.id.btnEditPlan)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDeletePlan)

        fun bind(plan: Plan) {
            tvName.text = plan.name
            tvDetails.text = when {
                plan.durationDays >= 365 -> itemView.context.getString(R.string.plan_subtitle_lifestyle)
                plan.durationDays >= 90 -> itemView.context.getString(R.string.plan_subtitle_results)
                else -> itemView.context.getString(R.string.plan_subtitle_standard)
            }
            tvPlanPrice.text = itemView.context.getString(R.string.placeholder_currency, plan.price)
            tvPlanDuration.text = itemView.context.getString(R.string.label_per_days, plan.durationDays)

            when {
                plan.durationDays >= 365 -> {
                    tvPlanTag.text = itemView.context.getString(R.string.label_vip_status)
                    tvPlanMeta.text = itemView.context.getString(R.string.label_best_deal)
                    ivPlanIcon.setImageResource(android.R.drawable.btn_star_big_on)
                }
                plan.durationDays >= 90 -> {
                    tvPlanTag.text = itemView.context.getString(R.string.label_value_tier)
                    tvPlanMeta.text = itemView.context.getString(R.string.label_save_percent)
                    ivPlanIcon.setImageResource(android.R.drawable.ic_media_play)
                }
                else -> {
                    tvPlanTag.text = itemView.context.getString(R.string.label_popular_choice)
                    tvPlanMeta.text = itemView.context.getString(R.string.label_best_deal)
                    ivPlanIcon.setImageResource(android.R.drawable.ic_menu_today)
                }
            }

            btnEdit.setOnClickListener { onEditClick(plan) }
            btnDelete.setOnClickListener { onDeleteClick(plan) }
        }
    }
}
