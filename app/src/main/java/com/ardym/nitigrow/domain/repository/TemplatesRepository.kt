package com.ardym.nitigrow.domain.repository

import com.ardym.nitigrow.core.network.ApiResult
import com.ardym.nitigrow.domain.model.MessageTemplate

interface TemplatesRepository {
    suspend fun list(): ApiResult<List<MessageTemplate>>
    suspend fun create(name: String, category: String, language: String, body: String): ApiResult<MessageTemplate>
    suspend fun delete(id: String): ApiResult<Unit>
}
