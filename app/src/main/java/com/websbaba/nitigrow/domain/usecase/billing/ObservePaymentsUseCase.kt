package com.websbaba.nitigrow.domain.usecase.billing

import com.websbaba.nitigrow.domain.model.PaymentRecord
import com.websbaba.nitigrow.domain.repository.BillingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObservePaymentsUseCase @Inject constructor(private val repo: BillingRepository) {
    operator fun invoke(): Flow<List<PaymentRecord>> = repo.observePayments()
}
