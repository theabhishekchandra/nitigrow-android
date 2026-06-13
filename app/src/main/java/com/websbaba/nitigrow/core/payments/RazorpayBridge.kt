package com.websbaba.nitigrow.core.payments

import com.razorpay.PaymentData
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * MainActivity implements `PaymentResultListener`/`PaymentResultWithDataListener` and
 * forwards results here. Billing VMs collect from `events`.
 */
sealed interface RazorpayResult {
    data class Success(
        val paymentId: String,
        val orderId: String?,
        val signature: String?
    ) : RazorpayResult
    data class Failure(val code: Int, val message: String, val orderId: String?) : RazorpayResult
}

@Singleton
class RazorpayBridge @Inject constructor() {
    private val _events = MutableSharedFlow<RazorpayResult>(extraBufferCapacity = 4)
    val events: SharedFlow<RazorpayResult> = _events.asSharedFlow()

    fun onSuccess(paymentId: String, data: PaymentData?) {
        _events.tryEmit(
            RazorpayResult.Success(
                paymentId = paymentId,
                orderId = data?.orderId,
                signature = data?.signature
            )
        )
    }

    fun onError(code: Int, message: String, data: PaymentData?) {
        _events.tryEmit(RazorpayResult.Failure(code, message, data?.orderId))
    }
}
