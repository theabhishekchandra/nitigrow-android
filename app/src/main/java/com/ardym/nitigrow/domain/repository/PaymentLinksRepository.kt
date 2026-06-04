package com.ardym.nitigrow.domain.repository

import com.ardym.nitigrow.core.network.ApiResult
import com.ardym.nitigrow.domain.model.PaymentLink

interface PaymentLinksRepository {
    suspend fun create(contactId: String, amount: Long, description: String?): ApiResult<PaymentLink>
    suspend fun list(): ApiResult<List<PaymentLink>>
}
