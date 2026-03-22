package com.example.gymmanagement.ui.member.addedit

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.gymmanagement.data.local.entity.Member
import com.example.gymmanagement.data.local.entity.Plan
import com.example.gymmanagement.data.repository.MemberRepository
import com.example.gymmanagement.data.repository.PlanRepository
import com.example.gymmanagement.di.AppContainer
import com.example.gymmanagement.utils.DateUtils

class AddEditMemberViewModel(application: Application) : AndroidViewModel(application) {

    private val memberRepository: MemberRepository = AppContainer(application).memberRepository
    private val planRepository: PlanRepository = AppContainer(application).planRepository

    fun getAllPlans(): LiveData<List<Plan>> = planRepository.getAllPlans()

    fun getMemberById(memberId: Int): LiveData<Member?> = memberRepository.getMemberById(memberId)

    fun saveMember(
        existingId: Int?,
        name: String,
        phone: String,
        joinDate: Long,
        planId: Int,
        durationDays: Int,
        paymentStatus: Boolean
    ) {
        val expiryDate = DateUtils.calculateExpiryDate(joinDate, durationDays)

        val member = Member(
            id = existingId ?: 0,
            name = name,
            phone = phone,
            joinDate = joinDate,
            expiryDate = expiryDate,
            planId = planId,
            paymentStatus = paymentStatus
        )

        if (existingId == null) {
            memberRepository.insertMember(member)
        } else {
            memberRepository.updateMember(member)
        }
    }
}
