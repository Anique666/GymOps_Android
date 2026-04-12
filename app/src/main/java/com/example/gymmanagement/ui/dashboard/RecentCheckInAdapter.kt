package com.example.gymmanagement.ui.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.gymmanagement.R
import com.example.gymmanagement.data.local.entity.Member
import com.example.gymmanagement.utils.MembershipStatusHelper

class RecentCheckInAdapter(
    private val onClick: (Member) -> Unit
) : RecyclerView.Adapter<RecentCheckInAdapter.RecentViewHolder>() {

    private val members = mutableListOf<Member>()

    fun submitList(list: List<Member>) {
        members.clear()
        members.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_dashboard_checkin, parent, false)
        return RecentViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecentViewHolder, position: Int) {
        holder.bind(members[position], position)
    }

    override fun getItemCount(): Int = members.size

    inner class RecentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvAvatar: TextView = itemView.findViewById(R.id.tvAvatar)
        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val tvSubtitle: TextView = itemView.findViewById(R.id.tvSubtitle)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)

        fun bind(member: Member, position: Int) {
            val firstChar = member.name.firstOrNull()?.uppercaseChar()?.toString().orEmpty()
            tvAvatar.text = firstChar
            tvName.text = member.name
            tvSubtitle.text = if (position == 0) {
                itemView.context.getString(R.string.placeholder_checked_in_now)
            } else {
                itemView.context.getString(R.string.placeholder_checked_in_recently)
            }

            val status = MembershipStatusHelper.statusLabel(member.expiryDate)
            tvStatus.text = status.uppercase()
            when (status) {
                "Active" -> {
                    tvStatus.setBackgroundResource(R.drawable.bg_chip_active)
                    tvStatus.setTextColor(ContextCompat.getColor(itemView.context, R.color.chip_active_text))
                }
                "Expiring Soon" -> {
                    tvStatus.setBackgroundResource(R.drawable.bg_chip_warning)
                    tvStatus.setTextColor(ContextCompat.getColor(itemView.context, R.color.chip_warning_text))
                }
                else -> {
                    tvStatus.setBackgroundResource(R.drawable.bg_chip_expired)
                    tvStatus.setTextColor(ContextCompat.getColor(itemView.context, R.color.chip_expired_text))
                }
            }

            itemView.setOnClickListener { onClick(member) }
        }
    }
}
