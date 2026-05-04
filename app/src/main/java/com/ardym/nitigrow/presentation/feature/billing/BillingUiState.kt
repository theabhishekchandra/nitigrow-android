package com.ardym.nitigrow.presentation.feature.billing

import com.ardym.nitigrow.domain.model.CheckoutOrder
import com.ardym.nitigrow.domain.model.PaymentRecord
import com.ardym.nitigrow.domain.model.Plan
import com.ardym.nitigrow.domain.model.Subscription

enum class CheckoutPhase { IDLE, CREATING_ORDER, AWAITING_PAYMENT, VERIFYING, SUCCESS, FAILED }

data class BillingUiState(
    val plans: List<Plan> = emptyList(),
    val subscription: Subscription? = null,
    val payments: List<PaymentRecord> = emptyList(),
    val isRefreshing: Boolean = false,
    val phase: CheckoutPhase = CheckoutPhase.IDLE,
    val pendingOrder: CheckoutOrder? = null,
    val error: String? = null
)

sealed interface BillingEffect {
    data class LaunchCheckout(val order: CheckoutOrder) : BillingEffect
    data object PaymentSucceeded : BillingEffect
    data class PaymentFailed(val message: String) : BillingEffect
}
