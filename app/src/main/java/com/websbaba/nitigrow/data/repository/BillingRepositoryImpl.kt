package com.websbaba.nitigrow.data.repository

import com.websbaba.nitigrow.core.network.ApiResult
import com.websbaba.nitigrow.core.network.safeApiCall
import com.websbaba.nitigrow.core.util.DispatcherProvider
import com.websbaba.nitigrow.data.local.dao.PaymentDao
import com.websbaba.nitigrow.data.local.dao.PlanDao
import com.websbaba.nitigrow.data.local.dao.SubscriptionDao
import com.websbaba.nitigrow.data.mapper.toDomain
import com.websbaba.nitigrow.data.mapper.toEntity
import com.websbaba.nitigrow.data.remote.api.BillingApi
import com.websbaba.nitigrow.data.remote.dto.CreateOrderRequest
import com.websbaba.nitigrow.data.remote.dto.ReportFailureRequest
import com.websbaba.nitigrow.data.remote.dto.VerifyPaymentRequest
import com.websbaba.nitigrow.domain.model.CheckoutOrder
import com.websbaba.nitigrow.domain.model.PaymentRecord
import com.websbaba.nitigrow.domain.model.Plan
import com.websbaba.nitigrow.domain.model.Subscription
import com.websbaba.nitigrow.domain.repository.BillingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillingRepositoryImpl @Inject constructor(
    private val api: BillingApi,
    private val planDao: PlanDao,
    private val subDao: SubscriptionDao,
    private val payDao: PaymentDao,
    private val dispatchers: DispatcherProvider
) : BillingRepository {

    override fun observePlans(): Flow<List<Plan>> =
        planDao.observeAll().map { rows -> rows.map { it.toDomain() } }

    override fun observeSubscription(): Flow<Subscription?> =
        subDao.observe().map { it?.toDomain() }

    override fun observePayments(): Flow<List<PaymentRecord>> =
        payDao.observeAll().map { rows -> rows.map { it.toDomain() } }

    override suspend fun refreshPlans(): ApiResult<Unit> =
        when (val res = safeApiCall(dispatchers.io) { api.listPlans() }) {
            is ApiResult.Success -> {
                planDao.upsertAll((res.data.data ?: emptyList()).map { it.toEntity() })
                ApiResult.Success(Unit)
            }
            is ApiResult.Error -> res
        }

    override suspend fun refreshSubscription(): ApiResult<Unit> =
        when (val res = safeApiCall(dispatchers.io) { api.subscription() }) {
            is ApiResult.Success -> {
                subDao.upsert(res.data.toEntity())
                ApiResult.Success(Unit)
            }
            is ApiResult.Error -> res
        }

    override suspend fun refreshPayments(): ApiResult<Unit> =
        when (val res = safeApiCall(dispatchers.io) { api.listPayments() }) {
            is ApiResult.Success -> {
                payDao.upsertAll((res.data.data ?: emptyList()).map { it.toEntity() })
                ApiResult.Success(Unit)
            }
            is ApiResult.Error -> res
        }

    override suspend fun createOrder(planId: String): ApiResult<CheckoutOrder> =
        when (val res = safeApiCall(dispatchers.io) {
            api.createOrder(CreateOrderRequest(planId))
        }) {
            is ApiResult.Success -> ApiResult.Success(
                CheckoutOrder(
                    razorpayOrderId = res.data.razorpayOrderId,
                    keyId = res.data.keyId,
                    amountPaise = res.data.amountPaise,
                    currency = res.data.currency,
                    name = res.data.name,
                    description = res.data.description,
                    prefillEmail = res.data.prefillEmail,
                    prefillContact = res.data.prefillContact
                )
            )
            is ApiResult.Error -> res
        }

    override suspend fun verifyPayment(
        razorpayOrderId: String,
        razorpayPaymentId: String,
        razorpaySignature: String
    ): ApiResult<Unit> {
        val res = safeApiCall(dispatchers.io) {
            api.verify(VerifyPaymentRequest(razorpayOrderId, razorpayPaymentId, razorpaySignature))
            Unit
        }
        // refresh subscription + payments so UI reflects new state
        if (res is ApiResult.Success) {
            refreshSubscription()
            refreshPayments()
        }
        return res
    }

    override suspend fun reportFailure(
        razorpayOrderId: String,
        reason: String
    ): ApiResult<Unit> = safeApiCall(dispatchers.io) {
        api.reportFailure(ReportFailureRequest(razorpayOrderId, reason)); Unit
    }
}
