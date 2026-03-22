package com.example.gymmanagement.ui.member.list

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gymmanagement.R
import com.example.gymmanagement.data.local.entity.Member
import com.example.gymmanagement.utils.DateUtils
import com.example.gymmanagement.utils.MembershipStatusHelper

class MemberAdapter(
    private val onItemClick: (Member) -> Unit
) : RecyclerView.Adapter<MemberAdapter.MemberViewHolder>() {

    private val members = mutableListOf<Member>()

    fun submitList(newList: List<Member>) {
        members.clear()
        members.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_member, parent, false)
        return MemberViewHolder(view)
    }

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        holder.bind(members[position])
    }

    override fun getItemCount(): Int = members.size

    inner class MemberViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvName: TextView = itemView.findViewById(R.id.tvMemberName)
        private val tvPhone: TextView = itemView.findViewById(R.id.tvPhone)
        private val tvExpiry: TextView = itemView.findViewById(R.id.tvExpiry)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)

        fun bind(member: Member) {
            tvName.text = member.name
            tvPhone.text = member.phone
            tvExpiry.text = "Expiry: ${DateUtils.formatDate(member.expiryDate)}"

            val status = MembershipStatusHelper.statusLabel(member.expiryDate)
            tvStatus.text = status
            tvStatus.setTextColor(
                when (status) {
                    "Expired" -> Color.parseColor("#D32F2F")
                    "Expiring Soon" -> Color.parseColor("#F57C00")
                    else -> Color.parseColor("#388E3C")
                }
            )

            itemView.setOnClickListener { onItemClick(member) }
        }
    }
}
