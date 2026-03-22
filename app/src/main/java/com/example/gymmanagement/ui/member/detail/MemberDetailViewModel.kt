package com.example.gymmanagement.ui.member.detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.gymmanagement.data.local.entity.Member
import com.example.gymmanagement.data.repository.MemberRepository
import com.example.gymmanagement.di.AppContainer

class MemberDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val memberRepository: MemberRepository = AppContainer(application).memberRepository

    fun getMemberById(memberId: Int): LiveData<Member?> = memberRepository.getMemberById(memberId)

    fun deleteMember(member: Member) {
        memberRepository.deleteMember(member)
    }
}
