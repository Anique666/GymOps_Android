package com.example.gymmanagement.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import com.example.gymmanagement.data.repository.DashboardRepository
import com.example.gymmanagement.data.repository.MemberRepository
import com.example.gymmanagement.data.local.entity.Member
import com.example.gymmanagement.di.AppContainer

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val dashboardRepository: DashboardRepository = AppContainer(application).dashboardRepository
    private val memberRepository: MemberRepository = AppContainer(application).memberRepository

    private val currentTime = MutableLiveData<Long>()

    val totalMembers: LiveData<Int> = dashboardRepository.getTotalMembersCount()

    val activeMembers: LiveData<Int> = currentTime.switchMap { now ->
        dashboardRepository.getActiveMembersCount(now)
    }

    val expiredMembers: LiveData<Int> = currentTime.switchMap { now ->
        dashboardRepository.getExpiredMembersCount(now)
    }

    val expiringSoonMembers: LiveData<Int> = currentTime.switchMap { now ->
        val threshold = now + (5L * 24L * 60L * 60L * 1000L)
        dashboardRepository.getExpiringSoonCount(now, threshold)
    }

    val recentMembers: LiveData<List<Member>> = memberRepository.getAllMembers().map { members ->
        members.take(3)
    }

    init {
        refreshDashboard()
    }

    fun refreshDashboard() {
        currentTime.value = System.currentTimeMillis()
    }
}
