package com.ardym.nitigrow.domain.usecase.billing

import com.ardym.nitigrow.core.network.ApiResult
import com.ardym.nitigrow.domain.repository.BillingRepository
import javax.inject.Inject

class VerifyPaymentUseCase @Inject constructor(private val repo: BillingRepository) {
    suspend operator fun invoke(
        orderId: String,
        paymentId: String,
        signature: String
    ): ApiResult<Unit> {
        if (orderId.isBlank() || paymentId.isBlank() || signature.isBlank()) {
            return ApiResult.Error(message = "Missing payment fields")
        }
        return repo.verifyPayment(orderId, paymentId, signature)
    }
}
