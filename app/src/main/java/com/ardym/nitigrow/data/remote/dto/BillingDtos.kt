package com.ardym.nitigrow.data.remote.dto

import com.google.gson.annotations.SerializedName

data class PlanDto(
    @SerializedName("_id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("priceInr") val priceInr: Long,
    @SerializedName("periodDays") val periodDays: Int,
    @SerializedName("features") val features: List<String>,
    @SerializedName("isPopular") val isPopular: Boolean = false
)

data class PlanListResponse(
    @SerializedName("data") val data: List<PlanDto>? = null
)

data class SubscriptionDto(
    @SerializedName("planId") val planId: String,
    @SerializedName("planName") val planName: String,
    @SerializedName("status") val status: String,
    @SerializedName("renewsAt") val renewsAt: String?,
    @SerializedName("cancelledAt") val cancelledAt: String?
)

data class PaymentDto(
    @SerializedName("_id") val id: String,
    @SerializedName("orderId") val orderId: String,
    @SerializedName("amountInr") val amountInr: Long,
    @SerializedName("status") val status: String,
    @SerializedName("method") val method: String?,
    @SerializedName("planName") val planName: String?,
    @SerializedName("createdAt") val createdAt: String
)

data class PaymentListResponse(
    @SerializedName("data") val data: List<PaymentDto>? = null
)

data class CreateOrderRequest(
    @SerializedName("planId") val planId: String
)

data class CreateOrderResponse(
    @SerializedName("razorpayOrderId") val razorpayOrderId: String,
    @SerializedName("keyId") val keyId: String,
    @SerializedName("amountPaise") val amountPaise: Long,
    @SerializedName("currency") val currency: String,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String,
    @SerializedName("prefillEmail") val prefillEmail: String?,
    @SerializedName("prefillContact") val prefillContact: String?
)

data class VerifyPaymentRequest(
    @SerializedName("razorpayOrderId") val razorpayOrderId: String,
    @SerializedName("razorpayPaymentId") val razorpayPaymentId: String,
    @SerializedName("razorpaySignature") val razorpaySignature: String
)

data class ReportFailureRequest(
    @SerializedName("razorpayOrderId") val razorpayOrderId: String,
    @SerializedName("reason") val reason: String
)
