package com.websbaba.nitigrow.data.repository

import com.websbaba.nitigrow.core.network.ApiResult
import com.websbaba.nitigrow.core.network.andThen
import com.websbaba.nitigrow.core.network.safeApiCall
import com.websbaba.nitigrow.core.util.DispatcherProvider
import com.websbaba.nitigrow.data.remote.api.AnalyticsApi
import com.websbaba.nitigrow.domain.model.AgentMetric
import com.websbaba.nitigrow.domain.model.DailyMetric
import com.websbaba.nitigrow.domain.repository.AnalyticsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsRepositoryImpl @Inject constructor(
    private val api: AnalyticsApi,
    private val dispatchers: DispatcherProvider
) : AnalyticsRepository {

    override suspend fun messagesPerDay(from: String?, to: String?): ApiResult<List<DailyMetric>> =
        safeApiCall(dispatchers.io) { api.messagesPerDay(from, to) }.andThen { r ->
            ApiResult.Success(
                r.map { DailyMetric(date = it.date, sent = it.outbound, delivered = it.delivered, read = it.read) }
            )
        }

    override suspend fun topAgents(): ApiResult<List<AgentMetric>> =
        safeApiCall(dispatchers.io) { api.topAgents() }.andThen { r ->
            ApiResult.Success((r.data ?: emptyList()).map { AgentMetric(it.name, it.replies) })
        }
}
