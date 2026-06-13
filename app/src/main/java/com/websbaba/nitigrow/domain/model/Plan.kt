package com.websbaba.nitigrow.domain.model

data class Plan(
    val id: String,
    val name: String,
    val priceInr: Long,         // paise = priceInr * 100
    val periodDays: Int,
    val features: List<String>,
    val isPopular: Boolean
)

data class Subscription(
    val planId: String,
    val planName: String,
    val status: SubscriptionStatus,
    val renewsAt: java.time.Instant?,
    val cancelledAt: java.time.Instant?
)

enum class SubscriptionStatus {
    ACTIVE, GRACE, EXPIRED, CANCELLED, NONE;

    companion object {
        fun safeValueOf(raw: String) =
            runCatching { valueOf(raw.uppercase()) }.getOrDefault(NONE)
    }
}

data class PaymentRecord(
    val id: String,
    val orderId: String,
    val amountInr: Long,
    val status: PaymentStatus,
    val method: String?,
    val createdAt: java.time.Instant,
    val planName: String?
)

enum class PaymentStatus {
    CREATED, AUTHORIZED, CAPTURED, FAILED, REFUNDED;

    companion object {
        fun safeValueOf(raw: String) =
            runCatching { valueOf(raw.uppercase()) }.getOrDefault(CREATED)
    }
}

/** Server response after creating Razorpay order. Used to launch checkout. */
data class CheckoutOrder(
    val razorpayOrderId: String,
    val keyId: String,
    val amountPaise: Long,
    val currency: String,
    val name: String,
    val description: String,
    val prefillEmail: String?,
    val prefillContact: String?
)
