package com.ardym.nitigrow.data.remote.api

import com.ardym.nitigrow.data.remote.dto.CreateOrderRequest
import com.ardym.nitigrow.data.remote.dto.CreateOrderResponse
import com.ardym.nitigrow.data.remote.dto.GenericMessageDto
import com.ardym.nitigrow.data.remote.dto.PaymentListResponse
import com.ardym.nitigrow.data.remote.dto.PlanListResponse
import com.ardym.nitigrow.data.remote.dto.ReportFailureRequest
import com.ardym.nitigrow.data.remote.dto.SubscriptionDto
import com.ardym.nitigrow.data.remote.dto.VerifyPaymentRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface BillingApi {

    @GET("billing/plans")
    suspend fun listPlans(): PlanListResponse

    @GET("billing/subscription")
    suspend fun subscription(): SubscriptionDto

    @GET("billing/payments")
    suspend fun listPayments(): PaymentListResponse

    @POST("billing/orders")
    suspend fun createOrder(@Body body: CreateOrderRequest): CreateOrderResponse

    @POST("billing/verify")
    suspend fun verify(@Body body: VerifyPaymentRequest): GenericMessageDto

    @POST("billing/failure")
    suspend fun reportFailure(@Body body: ReportFailureRequest): GenericMessageDto
}
