package com.websbaba.nitigrow.data.remote.dto

import com.google.gson.annotations.SerializedName

// GET /api/billing/status
data class BillingStatusDto(
    @SerializedName("plan") val plan: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("subscription") val subscription: SubscriptionDto? = null,
    @SerializedName("usage") val usage: UsageDto? = null,
    @SerializedName("prices") val prices: Map<String, Int>? = null,
    @SerializedName("referralCreditPaise") val referralCreditPaise: Int? = null,
    @SerializedName("branding") val branding: Boolean? = null,
)

// tenant.subscription sub-document. Field renamed razorpaySubscriptionId ->
// gatewaySubscriptionId in the Cashfree migration.
data class SubscriptionDto(
    @SerializedName("status") val status: String? = null,
    @SerializedName("trialEndsAt") val trialEndsAt: String? = null,
    @SerializedName("currentPeriodStart") val currentPeriodStart: String? = null,
    @SerializedName("currentPeriodEnd") val currentPeriodEnd: String? = null,
    @SerializedName("gatewaySubscriptionId") val gatewaySubscriptionId: String? = null,
    @SerializedName("cancelAtPeriodEnd") val cancelAtPeriodEnd: Boolean? = null,
    @SerializedName("billingCycle") val billingCycle: String? = null,
)

data class UsageDto(
    @SerializedName("messages") val messages: UsageMeterDto? = null,
    @SerializedName("ai") val ai: UsageMeterDto? = null,
    @SerializedName("contacts") val contacts: UsageMeterDto? = null,
    @SerializedName("users") val users: UsageMeterDto? = null,
)

data class UsageMeterDto(
    @SerializedName("used") val used: Int? = null,
    @SerializedName("limit") val limit: Int? = null,
)

// GET /api/billing/invoices -> { invoices: [...] }. Raw gateway fields vary.
data class InvoiceDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("invoice_number") val invoiceNumber: String? = null,
    @SerializedName("receipt") val receipt: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("amount") val amount: Long? = null,
    @SerializedName("amount_paid") val amountPaid: Long? = null,
    @SerializedName("paid_at") val paidAt: Long? = null,
    @SerializedName("created_at") val createdAt: Long? = null,
    @SerializedName("date") val date: Long? = null,
)

data class InvoicesResponse(
    @SerializedName("invoices") val invoices: List<InvoiceDto>? = null,
)

// POST /api/billing/cancel
data class CancelResponse(
    @SerializedName("message") val message: String? = null,
)
