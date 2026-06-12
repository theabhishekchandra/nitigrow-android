package com.ardym.nitigrow.data.repository

import com.ardym.nitigrow.core.network.ApiResult
import com.ardym.nitigrow.core.network.safeApiCall
import com.ardym.nitigrow.core.util.DispatcherProvider
import com.ardym.nitigrow.data.remote.api.PaymentLinksApi
import com.ardym.nitigrow.data.remote.dto.CreatePaymentLinkRequest
import com.ardym.nitigrow.data.remote.dto.PaymentLinkDto
import com.ardym.nitigrow.domain.model.PaymentLink
import com.ardym.nitigrow.domain.repository.PaymentLinksRepository
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
        when (val r = safeApiCall(dispatchers.io) { api.create(CreatePaymentLinkRequest(contactId, amount, description)) }) {
            is ApiResult.Success -> ApiResult.Success(r.data.toDomain())
            is ApiResult.Error -> r
        }

    override suspend fun list(): ApiResult<List<PaymentLink>> =
        when (val r = safeApiCall(dispatchers.io) { api.list() }) {
            is ApiResult.Success -> ApiResult.Success((r.data.data ?: emptyList()).map { it.toDomain() })
            is ApiResult.Error -> r
        }
}
