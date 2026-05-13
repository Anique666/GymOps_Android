package com.example.gymmanagement.data.repository

import androidx.lifecycle.LiveData
import com.example.gymmanagement.data.local.dao.MemberDao
import com.example.gymmanagement.data.local.entity.Member
import com.example.gymmanagement.data.local.model.MemberBillingSummary
import com.example.gymmanagement.data.sync.SyncManager
import java.util.UUID
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MemberRepository(
    private val memberDao: MemberDao,
    private val syncManager: SyncManager? = null
) {

    private val ioExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    fun getAllMembers(): LiveData<List<Member>> = memberDao.getAllMembers()

    fun getAllMemberBillingSummaries(): LiveData<List<MemberBillingSummary>> = memberDao.getAllMemberBillingSummaries()

    fun getMemberById(memberId: Int): LiveData<Member?> = memberDao.getMemberById(memberId)

    fun getMemberBillingSummaryById(memberId: Int): LiveData<MemberBillingSummary?> = memberDao.getMemberBillingSummaryById(memberId)

    fun searchMembers(query: String): LiveData<List<Member>> = memberDao.searchMembers(query)

    fun searchMemberBillingSummaries(query: String): LiveData<List<MemberBillingSummary>> = memberDao.searchMemberBillingSummaries(query)

    fun insertMember(member: Member) {
        ioExecutor.execute {
            val now = System.currentTimeMillis()
            val prepared = member.copy(
                remoteId = member.remoteId.ifBlank { UUID.randomUUID().toString() },
                updatedAt = now,
                synced = false,
                deleted = false
            )
            memberDao.insertMember(prepared)
            syncManager?.enqueueSync()
        }
    }

    fun insertMemberAndReturnId(member: Member): Long {
        return ioExecutor.submit<Long> {
            val now = System.currentTimeMillis()
            val prepared = member.copy(
                remoteId = member.remoteId.ifBlank { UUID.randomUUID().toString() },
                updatedAt = now,
                synced = false,
                deleted = false
            )
            memberDao.insertMember(prepared)
        }.get().also { syncManager?.enqueueSync() }
    }

    fun updateMember(member: Member) {
        ioExecutor.execute {
            val now = System.currentTimeMillis()
            val prepared = member.copy(
                remoteId = member.remoteId.ifBlank { UUID.randomUUID().toString() },
                updatedAt = now,
                synced = false
            )
            memberDao.updateMember(prepared)
            syncManager?.enqueueSync()
        }
    }

    fun deleteMember(member: Member) {
        ioExecutor.execute {
            val now = System.currentTimeMillis()
            memberDao.softDeleteMember(member.id, now)
            syncManager?.enqueueSync()
        }
    }
}
