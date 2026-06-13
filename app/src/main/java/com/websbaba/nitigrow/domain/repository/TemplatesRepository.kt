package com.websbaba.nitigrow.domain.repository

import com.websbaba.nitigrow.core.network.ApiResult
import com.websbaba.nitigrow.domain.model.MessageTemplate

interface TemplatesRepository {
    suspend fun list(): ApiResult<List<MessageTemplate>>
    suspend fun create(name: String, category: String, language: String, body: String): ApiResult<MessageTemplate>
    suspend fun delete(id: String): ApiResult<Unit>
}
