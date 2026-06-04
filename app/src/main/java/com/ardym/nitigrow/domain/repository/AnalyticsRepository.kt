package com.ardym.nitigrow.domain.repository

import com.ardym.nitigrow.core.network.ApiResult
import com.ardym.nitigrow.domain.model.AgentMetric
import com.ardym.nitigrow.domain.model.DailyMetric

interface AnalyticsRepository {
    suspend fun messagesPerDay(from: String?, to: String?): ApiResult<List<DailyMetric>>
    suspend fun topAgents(): ApiResult<List<AgentMetric>>
}
