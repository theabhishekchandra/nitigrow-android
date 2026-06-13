package com.websbaba.nitigrow.domain.usecase.billing

import com.websbaba.nitigrow.domain.model.Subscription
import com.websbaba.nitigrow.domain.repository.BillingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveSubscriptionUseCase @Inject constructor(private val repo: BillingRepository) {
    operator fun invoke(): Flow<Subscription?> = repo.observeSubscription()
}
