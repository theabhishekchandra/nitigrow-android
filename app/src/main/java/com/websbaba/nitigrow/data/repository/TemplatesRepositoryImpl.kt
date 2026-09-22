package com.websbaba.nitigrow.data.repository

import com.websbaba.nitigrow.core.network.ApiResult
import com.websbaba.nitigrow.core.network.andThen
import com.websbaba.nitigrow.core.network.safeApiCall
import com.websbaba.nitigrow.core.util.DispatcherProvider
import com.websbaba.nitigrow.data.remote.api.TemplatesApi
import com.websbaba.nitigrow.data.remote.dto.CreateTemplateComponent
import com.websbaba.nitigrow.data.remote.dto.CreateTemplateRequest
import com.websbaba.nitigrow.data.remote.dto.WaTemplateDto
import com.websbaba.nitigrow.domain.model.MessageTemplate
import com.websbaba.nitigrow.domain.repository.TemplatesRepository
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
        safeApiCall(dispatchers.io) { api.list() }.andThen { r ->
            ApiResult.Success(r.map { it.toDomain() })
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
        return safeApiCall(dispatchers.io) { api.create(req) }.andThen { r ->
            ApiResult.Success(r.toDomain())
        }
    }

    override suspend fun delete(id: String): ApiResult<Unit> =
        safeApiCall(dispatchers.io) { api.delete(id); Unit }.andThen { r ->
            ApiResult.Success(Unit)
        }
}
