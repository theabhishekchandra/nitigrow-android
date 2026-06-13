package com.websbaba.nitigrow.domain.usecase.billing

import com.websbaba.nitigrow.core.network.ApiResult
import com.websbaba.nitigrow.domain.model.CheckoutOrder
import com.websbaba.nitigrow.domain.repository.BillingRepository
import javax.inject.Inject

class StartCheckoutUseCase @Inject constructor(private val repo: BillingRepository) {
    suspend operator fun invoke(planId: String): ApiResult<CheckoutOrder> {
        if (planId.isBlank()) return ApiResult.Error(message = "Pick a plan")
        return repo.createOrder(planId)
    }
}
