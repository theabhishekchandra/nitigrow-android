package com.ardym.nitigrow.data.repository

import com.ardym.nitigrow.core.network.ApiResult
import com.ardym.nitigrow.core.network.safeApiCall
import com.ardym.nitigrow.core.util.DispatcherProvider
import com.ardym.nitigrow.data.remote.api.TemplatesApi
import com.ardym.nitigrow.data.remote.dto.CreateTemplateComponent
import com.ardym.nitigrow.data.remote.dto.CreateTemplateRequest
import com.ardym.nitigrow.data.remote.dto.WaTemplateDto
import com.ardym.nitigrow.domain.model.MessageTemplate
import com.ardym.nitigrow.domain.repository.TemplatesRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TemplatesRepositoryImpl @Inject constructor(
    private val api: TemplatesApi,
    private val dispatchers: DispatcherProvider
) : TemplatesRepository {

    private fun WaTemplateDto.toDomain(): MessageTemplate {
        val body = components.firstOrNull { it.type.equals("BODY", ignoreCase = true) }?.text.orEmpty()
        return MessageTemplate(
            id = id,
            name = name,
            category = category,
            language = language,
            status = status,
            body = body,
            rejectionReason = rejectionReason,
            updatedAt = updatedAt
        )
    }

    override suspend fun list(): ApiResult<List<MessageTemplate>> =
        when (val r = safeApiCall(dispatchers.io) { api.list() }) {
            is ApiResult.Success -> ApiResult.Success(r.data.map { it.toDomain() })
            is ApiResult.Error -> r
        }

    override suspend fun create(
        name: String,
        category: String,
        language: String,
        body: String
    ): ApiResult<MessageTemplate> {
        val req = CreateTemplateRequest(
            name = name,
            category = category,
            language = language,
            components = listOf(CreateTemplateComponent(type = "BODY", format = "TEXT", text = body))
        )
        return when (val r = safeApiCall(dispatchers.io) { api.create(req) }) {
            is ApiResult.Success -> ApiResult.Success(r.data.toDomain())
            is ApiResult.Error -> r
        }
    }

    override suspend fun delete(id: String): ApiResult<Unit> =
        when (val r = safeApiCall(dispatchers.io) { api.delete(id); Unit }) {
            is ApiResult.Success -> ApiResult.Success(Unit)
            is ApiResult.Error -> r
        }
}
