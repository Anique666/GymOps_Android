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
import java.util.Locale

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
        private val tvAvatar: TextView = itemView.findViewById(R.id.tvAvatar)
        private val tvPhone: TextView = itemView.findViewById(R.id.tvPhone)
        private val tvExpiry: TextView = itemView.findViewById(R.id.tvExpiry)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        private val tvCta: TextView = itemView.findViewById(R.id.tvCta)

        fun bind(member: Member) {
            tvName.text = member.name
            tvPhone.text = member.phone
            tvAvatar.text = member.name.firstOrNull()?.uppercaseChar()?.toString().orEmpty()
            tvExpiry.text = DateUtils.formatCardDate(member.expiryDate)

            val status = MembershipStatusHelper.statusLabel(member.expiryDate)
            tvStatus.text = status.uppercase(Locale.getDefault())
            when (status) {
                "Expired" -> {
                    tvStatus.setBackgroundResource(R.drawable.bg_chip_expired)
                    tvStatus.setTextColor(Color.parseColor("#9D1C1C"))
                    tvCta.text = itemView.context.getString(R.string.action_renew_now)
                    tvCta.setTextColor(Color.parseColor("#9D1C1C"))
                }
                "Expiring Soon" -> {
                    tvStatus.setBackgroundResource(R.drawable.bg_chip_warning)
                    tvStatus.setTextColor(Color.parseColor("#A54B00"))
                    tvCta.text = itemView.context.getString(R.string.action_renew_now)
                    tvCta.setTextColor(Color.parseColor("#A54B00"))
                }
                else -> {
                    tvStatus.setBackgroundResource(R.drawable.bg_chip_active)
                    tvStatus.setTextColor(Color.parseColor("#0A7B58"))
                    tvCta.text = itemView.context.getString(R.string.action_view_details)
                    tvCta.setTextColor(Color.parseColor("#111316"))
                }
            }

            itemView.setOnClickListener { onItemClick(member) }
            tvCta.setOnClickListener { onItemClick(member) }
        }
    }
}
