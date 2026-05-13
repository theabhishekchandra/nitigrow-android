package com.ardym.nitigrow.domain.usecase.billing

import com.ardym.nitigrow.core.network.ApiResult
import com.ardym.nitigrow.domain.repository.BillingRepository
import javax.inject.Inject

class ReportPaymentFailureUseCase @Inject constructor(private val repo: BillingRepository) {
    suspend operator fun invoke(orderId: String, reason: String): ApiResult<Unit> =
        repo.reportFailure(orderId, reason)
}
