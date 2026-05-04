package com.example.gymmanagement.data.repository

import androidx.lifecycle.LiveData
import com.example.gymmanagement.data.local.dao.MemberDao
import com.example.gymmanagement.data.local.entity.Member
import com.example.gymmanagement.data.local.model.MemberBillingSummary
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MemberRepository(private val memberDao: MemberDao) {

    private val ioExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    fun getAllMembers(): LiveData<List<Member>> = memberDao.getAllMembers()

    fun getAllMemberBillingSummaries(): LiveData<List<MemberBillingSummary>> = memberDao.getAllMemberBillingSummaries()

    fun getMemberById(memberId: Int): LiveData<Member?> = memberDao.getMemberById(memberId)

    fun getMemberBillingSummaryById(memberId: Int): LiveData<MemberBillingSummary?> = memberDao.getMemberBillingSummaryById(memberId)

    fun searchMembers(query: String): LiveData<List<Member>> = memberDao.searchMembers(query)

    fun searchMemberBillingSummaries(query: String): LiveData<List<MemberBillingSummary>> = memberDao.searchMemberBillingSummaries(query)

    fun insertMember(member: Member) {
        ioExecutor.execute { memberDao.insertMember(member) }
    }

    fun insertMemberAndReturnId(member: Member): Long {
        return ioExecutor.submit<Long> { memberDao.insertMember(member) }.get()
    }

    fun updateMember(member: Member) {
        ioExecutor.execute { memberDao.updateMember(member) }
    }

    fun deleteMember(member: Member) {
        ioExecutor.execute { memberDao.deleteMember(member) }
    }
}
