package com.example.gymmanagement.data.repository

import com.example.gymmanagement.data.local.dao.PaymentDao
import com.example.gymmanagement.data.local.entity.PaymentEntity
import com.example.gymmanagement.data.sync.SyncManager
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.UUID

class PaymentRepository(
    private val paymentDao: PaymentDao,
    private val syncManager: SyncManager? = null
) {

    private val ioExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    fun insertPayment(payment: PaymentEntity) {
        ioExecutor.execute {
            val now = System.currentTimeMillis()
            val prepared = payment.copy(
                remoteId = payment.remoteId.ifBlank { UUID.randomUUID().toString() },
                updatedAt = now,
                synced = false,
                deleted = false
            )
            paymentDao.insertPayment(prepared)
            syncManager?.enqueueSync()
        }
    }

    fun getLatestPaymentForMember(memberId: Int): Flow<PaymentEntity?> =
        paymentDao.getLatestPaymentForMember(memberId)
}