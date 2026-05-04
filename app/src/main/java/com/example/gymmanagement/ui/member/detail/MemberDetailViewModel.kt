package com.example.gymmanagement.ui.member.detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.gymmanagement.data.local.entity.Member
import com.example.gymmanagement.data.local.entity.PaymentEntity
import com.example.gymmanagement.data.local.model.MemberBillingSummary
import com.example.gymmanagement.data.local.model.PaymentStatus
import com.example.gymmanagement.data.repository.MemberRepository
import com.example.gymmanagement.data.repository.PaymentRepository
import com.example.gymmanagement.di.AppContainer
import com.example.gymmanagement.utils.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MemberDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val memberRepository: MemberRepository = AppContainer(application).memberRepository
    private val paymentRepository: PaymentRepository = AppContainer(application).paymentRepository

    fun getMemberById(memberId: Int): LiveData<Member?> = memberRepository.getMemberById(memberId)

    fun getMemberBillingSummaryById(memberId: Int): LiveData<MemberBillingSummary?> = memberRepository.getMemberBillingSummaryById(memberId)

    fun deleteMember(member: Member) {
        memberRepository.deleteMember(member)
    }

    fun renewMember(member: MemberBillingSummary) {
        val currentMember = member.member
        val nextExpiryBase = maxOf(currentMember.expiryDate, System.currentTimeMillis())
        val renewedExpiry = DateUtils.calculateExpiryDate(nextExpiryBase, member.planDurationDays)
        val isFullyPaid = true
        val paymentMethod = member.latestPaymentMethod.ifBlank { "CASH" }

        viewModelScope.launch(Dispatchers.IO) {
            memberRepository.updateMember(
                currentMember.copy(
                    expiryDate = renewedExpiry,
                    paymentStatus = isFullyPaid
                )
            )

            paymentRepository.insertPayment(
                PaymentEntity(
                    memberId = currentMember.id,
                    amount = member.planPrice,
                    paymentMethod = paymentMethod,
                    paymentDate = System.currentTimeMillis(),
                    planId = currentMember.planId,
                    isRenewal = true,
                    status = PaymentStatus.PAID.name
                )
            )
        }
    }
}
