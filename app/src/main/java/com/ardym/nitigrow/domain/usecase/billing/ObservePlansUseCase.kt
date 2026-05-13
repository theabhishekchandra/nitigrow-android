package com.ardym.nitigrow.domain.usecase.billing

import com.ardym.nitigrow.domain.model.Plan
import com.ardym.nitigrow.domain.repository.BillingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObservePlansUseCase @Inject constructor(private val repo: BillingRepository) {
    operator fun invoke(): Flow<List<Plan>> = repo.observePlans()
}
