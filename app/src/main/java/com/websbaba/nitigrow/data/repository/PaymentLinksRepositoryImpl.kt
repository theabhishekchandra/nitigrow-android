package com.websbaba.nitigrow.data.repository

import com.websbaba.nitigrow.core.network.ApiResult
import com.websbaba.nitigrow.core.network.andThen
import com.websbaba.nitigrow.core.network.safeApiCall
import com.websbaba.nitigrow.core.util.DispatcherProvider
import com.websbaba.nitigrow.data.remote.api.PaymentLinksApi
import com.websbaba.nitigrow.data.remote.dto.CreatePaymentLinkRequest
import com.websbaba.nitigrow.data.remote.dto.PaymentLinkDto
import com.websbaba.nitigrow.domain.model.PaymentLink
import com.websbaba.nitigrow.domain.repository.PaymentLinksRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentLinksRepositoryImpl @Inject constructor(
    private val api: PaymentLinksApi,
    private val dispatchers: DispatcherProvider
) : PaymentLinksRepository {

    private fun PaymentLinkDto.toDomain() =
        PaymentLink(id = id, contactName = contactName, amount = amount, status = status, linkUrl = linkUrl, sentAt = sentAt)

    override suspend fun create(contactId: String, amount: Long, description: String?): ApiResult<PaymentLink> =
        safeApiCall(dispatchers.io) { api.create(CreatePaymentLinkRequest(contactId, amount, description)) }.andThen { r ->
            ApiResult.Success(r.toDomain())
        }

    override suspend fun list(): ApiResult<List<PaymentLink>> =
        safeApiCall(dispatchers.io) { api.list() }.andThen { r ->
            ApiResult.Success((r.data ?: emptyList()).map { it.toDomain() })
        }
}
