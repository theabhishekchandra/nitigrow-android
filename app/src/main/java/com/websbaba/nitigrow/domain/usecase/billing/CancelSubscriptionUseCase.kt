package com.websbaba.nitigrow.domain.usecase.billing

import com.websbaba.nitigrow.core.network.ApiResult
import com.websbaba.nitigrow.domain.repository.BillingRepository
import javax.inject.Inject

class CancelSubscriptionUseCase @Inject constructor(private val repo: BillingRepository) {
    suspend operator fun invoke(): ApiResult<String?> = repo.cancel()
}
