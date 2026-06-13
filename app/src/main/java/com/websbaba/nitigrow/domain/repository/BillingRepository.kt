package com.websbaba.nitigrow.domain.repository

import com.websbaba.nitigrow.core.network.ApiResult
import com.websbaba.nitigrow.domain.model.CheckoutOrder
import com.websbaba.nitigrow.domain.model.PaymentRecord
import com.websbaba.nitigrow.domain.model.Plan
import com.websbaba.nitigrow.domain.model.Subscription
import kotlinx.coroutines.flow.Flow

interface BillingRepository {
    fun observePlans(): Flow<List<Plan>>
    fun observeSubscription(): Flow<Subscription?>
    fun observePayments(): Flow<List<PaymentRecord>>

    suspend fun refreshPlans(): ApiResult<Unit>
    suspend fun refreshSubscription(): ApiResult<Unit>
    suspend fun refreshPayments(): ApiResult<Unit>

    /** Server creates Razorpay order. Returns checkout params. */
    suspend fun createOrder(planId: String): ApiResult<CheckoutOrder>

    /** Posts payment id + signature to backend for HMAC verification. */
    suspend fun verifyPayment(
        razorpayOrderId: String,
        razorpayPaymentId: String,
        razorpaySignature: String
    ): ApiResult<Unit>

    /** Marks payment failed server-side so retry/refund flow can proceed. */
    suspend fun reportFailure(razorpayOrderId: String, reason: String): ApiResult<Unit>
}
