package com.websbaba.nitigrow.domain.usecase.billing

import com.websbaba.nitigrow.domain.model.Plan
import com.websbaba.nitigrow.domain.repository.BillingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObservePlansUseCase @Inject constructor(private val repo: BillingRepository) {
    operator fun invoke(): Flow<List<Plan>> = repo.observePlans()
}
