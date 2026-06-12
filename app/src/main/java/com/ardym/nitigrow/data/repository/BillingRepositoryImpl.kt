package com.ardym.nitigrow.data.repository

import com.ardym.nitigrow.core.network.ApiResult
import com.ardym.nitigrow.core.network.safeApiCall
import com.ardym.nitigrow.core.util.DispatcherProvider
import com.ardym.nitigrow.data.local.dao.PaymentDao
import com.ardym.nitigrow.data.local.dao.PlanDao
import com.ardym.nitigrow.data.local.dao.SubscriptionDao
import com.ardym.nitigrow.data.mapper.toDomain
import com.ardym.nitigrow.data.mapper.toEntity
import com.ardym.nitigrow.data.remote.api.BillingApi
import com.ardym.nitigrow.data.remote.dto.CreateOrderRequest
import com.ardym.nitigrow.data.remote.dto.ReportFailureRequest
import com.ardym.nitigrow.data.remote.dto.VerifyPaymentRequest
import com.ardym.nitigrow.domain.model.CheckoutOrder
import com.ardym.nitigrow.domain.model.PaymentRecord
import com.ardym.nitigrow.domain.model.Plan
import com.ardym.nitigrow.domain.model.Subscription
import com.ardym.nitigrow.domain.repository.BillingRepository
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
