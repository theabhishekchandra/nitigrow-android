package com.websbaba.nitigrow.domain.repository

import com.websbaba.nitigrow.core.network.ApiResult
import com.websbaba.nitigrow.domain.model.AgentMetric
import com.websbaba.nitigrow.domain.model.DailyMetric

interface AnalyticsRepository {
    suspend fun messagesPerDay(from: String?, to: String?): ApiResult<List<DailyMetric>>
    suspend fun topAgents(): ApiResult<List<AgentMetric>>
}
