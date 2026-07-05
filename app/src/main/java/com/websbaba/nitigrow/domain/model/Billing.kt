package com.websbaba.nitigrow.domain.model

import java.time.Instant

/**
 * Read-only billing snapshot for the mobile app. NitiGrow subscriptions are sold on
 * the web (app.nitigrow.in) to stay outside the Apple/Google in-app-purchase mandate,
 * so mobile only *displays* plan, usage and invoices — it does not run checkout.
 *
 * Mirrors GET /api/billing/status.
 */
data class BillingStatus(
    val plan: String,                 // free | trial | starter | growth | pro | enterprise
    val accountStatus: String?,       // tenant.status
    val subscription: SubscriptionInfo?,
    val usage: Usage,
    val prices: Map<String, Int>,     // monthly price (₹) by plan id
    val referralCreditPaise: Int,
    val branding: Boolean,
) {
    /** Pretty plan label, e.g. "Growth". */
    val planLabel: String
        get() = plan.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}

/** tenant.subscription sub-document. */
data class SubscriptionInfo(
    val status: String?,
    val trialEndsAt: Instant?,
    val currentPeriodStart: Instant?,
    val currentPeriodEnd: Instant?,
    val gatewaySubscriptionId: String?,
    val cancelAtPeriodEnd: Boolean,
    val billingCycle: String?,        // monthly | annual
) {
    /** A live Cashfree subscription that can be cancelled. */
    val isActive: Boolean
        get() = status == "active" && !gatewaySubscriptionId.isNullOrBlank()
}

data class Usage(
    val messages: UsageMeter,
    val ai: UsageMeter,
    val contacts: UsageMeter,
    val users: UsageMeter,
)

data class UsageMeter(val used: Int, val limit: Int) {
    val isUnlimited: Boolean get() = limit < 0

    /** Fill 0f..1f; unlimited / zero-limit caps report 0 (no fill). */
    val fraction: Float
        get() = if (limit > 0) (used.toFloat() / limit.toFloat()).coerceIn(0f, 1f) else 0f
}

/**
 * One invoice row. Raw Cashfree fields vary (mock uses receipt/amount/date; live uses
 * invoice_number/amount_paid/paid_at) — the DTO decodes all of them tolerantly.
 */
data class Invoice(
    val id: String,
    val number: String?,
    val status: String?,
    val amountPaise: Long,
    val paidAt: Instant?,
)
