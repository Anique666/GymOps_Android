package com.example.gymmanagement.ui.member.addedit

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.gymmanagement.data.local.entity.Member
import com.example.gymmanagement.data.local.entity.PaymentEntity
import com.example.gymmanagement.data.local.model.PaymentMethod
import com.example.gymmanagement.data.local.model.PaymentStatus
import com.example.gymmanagement.data.local.entity.Plan
import com.example.gymmanagement.data.repository.MemberRepository
import com.example.gymmanagement.data.repository.PaymentRepository
import com.example.gymmanagement.data.repository.PlanRepository
import com.example.gymmanagement.di.AppContainer
import com.example.gymmanagement.utils.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddEditMemberViewModel(application: Application) : AndroidViewModel(application) {

    private val memberRepository: MemberRepository = AppContainer(application).memberRepository
    private val planRepository: PlanRepository = AppContainer(application).planRepository
    private val paymentRepository: PaymentRepository = AppContainer(application).paymentRepository

    fun getAllPlans(): LiveData<List<Plan>> = planRepository.getAllPlans()

    fun getMemberById(memberId: Int): LiveData<Member?> = memberRepository.getMemberById(memberId)

    fun saveMember(
        existingId: Int?,
        name: String,
        phone: String,
        joinDate: Long,
        planId: Int,
        planPrice: Double,
        durationDays: Int,
        gender: String,
        dateOfBirth: Long,
        source: String,
        paymentMethod: String,
        paymentAmount: Double,
        paymentStatus: String
    ) {
        val expiryDate = DateUtils.calculateExpiryDate(joinDate, durationDays)
        val paymentState = runCatching { PaymentStatus.valueOf(paymentStatus.uppercase()) }
            .getOrDefault(PaymentStatus.PAID)
        val isFullyPaid = paymentState == PaymentStatus.PAID && paymentAmount >= planPrice

        val member = Member(
            id = existingId ?: 0,
            name = name,
            phone = phone,
            joinDate = joinDate,
            expiryDate = expiryDate,
            planId = planId,
            paymentStatus = isFullyPaid,
            gender = gender,
            dateOfBirth = dateOfBirth,
            source = source.ifBlank { "Unknown" }
        )

        viewModelScope.launch(Dispatchers.IO) {
            if (existingId == null) {
                val memberId = memberRepository.insertMemberAndReturnId(member).toInt()
                paymentRepository.insertPayment(
                    PaymentEntity(
                        memberId = memberId,
                        amount = paymentAmount,
                        paymentMethod = runCatching { PaymentMethod.valueOf(paymentMethod.uppercase()) }.getOrDefault(PaymentMethod.CASH).name,
                        paymentDate = System.currentTimeMillis(),
                        planId = planId,
                        isRenewal = false,
                        status = paymentState.name
                    )
                )
            } else {
                memberRepository.updateMember(member)
            }
        }
    }
}
