package com.example.gymmanagement.data.repository

import androidx.lifecycle.LiveData
import com.example.gymmanagement.data.local.dao.MemberDao

class DashboardRepository(private val memberDao: MemberDao) {

    fun getTotalMembersCount(): LiveData<Int> = memberDao.getTotalMembersCount()

    fun getActiveMembersCount(currentTime: Long): LiveData<Int> =
        memberDao.getActiveMembersCount(currentTime)

    fun getExpiredMembersCount(currentTime: Long): LiveData<Int> =
        memberDao.getExpiredMembersCount(currentTime)

    fun getExpiringSoonCount(currentTime: Long, expiringThreshold: Long): LiveData<Int> =
        memberDao.getExpiringSoonCount(currentTime, expiringThreshold)
}
