package com.websbaba.nitigrow.domain.repository

import com.websbaba.nitigrow.core.network.ApiResult
import com.websbaba.nitigrow.domain.model.PaymentLink

interface PaymentLinksRepository {
    suspend fun create(contactId: String, amount: Long, description: String?): ApiResult<PaymentLink>
    suspend fun list(): ApiResult<List<PaymentLink>>
}
