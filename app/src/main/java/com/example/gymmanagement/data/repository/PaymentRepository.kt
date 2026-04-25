package com.example.gymmanagement.data.repository

import com.example.gymmanagement.data.local.dao.PaymentDao
import com.example.gymmanagement.data.local.entity.PaymentEntity
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class PaymentRepository(private val paymentDao: PaymentDao) {

    private val ioExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    fun insertPayment(payment: PaymentEntity) {
        ioExecutor.execute { paymentDao.insertPayment(payment) }
    }

    fun getLatestPaymentForMember(memberId: Int): Flow<PaymentEntity?> =
        paymentDao.getLatestPaymentForMember(memberId)
}