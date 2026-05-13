package com.ardym.nitigrow.domain.usecase.billing

import com.ardym.nitigrow.core.network.ApiResult
import com.ardym.nitigrow.domain.model.CheckoutOrder
import com.ardym.nitigrow.domain.repository.BillingRepository
import javax.inject.Inject

class StartCheckoutUseCase @Inject constructor(private val repo: BillingRepository) {
    suspend operator fun invoke(planId: String): ApiResult<CheckoutOrder> {
        if (planId.isBlank()) return ApiResult.Error(message = "Pick a plan")
        return repo.createOrder(planId)
    }
}
