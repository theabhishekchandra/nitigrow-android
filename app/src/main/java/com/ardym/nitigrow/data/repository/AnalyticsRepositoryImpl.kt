package com.ardym.nitigrow.data.repository

import com.ardym.nitigrow.core.network.ApiResult
import com.ardym.nitigrow.core.network.safeApiCall
import com.ardym.nitigrow.core.util.DispatcherProvider
import com.ardym.nitigrow.data.remote.api.AnalyticsApi
import com.ardym.nitigrow.domain.model.AgentMetric
import com.ardym.nitigrow.domain.model.DailyMetric
import com.ardym.nitigrow.domain.repository.AnalyticsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsRepositoryImpl @Inject constructor(
    private val api: AnalyticsApi,
    private val dispatchers: DispatcherProvider
) : AnalyticsRepository {

    override suspend fun messagesPerDay(from: String?, to: String?): ApiResult<List<DailyMetric>> =
        when (val r = safeApiCall(dispatchers.io) { api.messagesPerDay(from, to) }) {
            is ApiResult.Success -> ApiResult.Success(
                r.data.map { DailyMetric(date = it.date, sent = it.outbound, delivered = it.delivered, read = it.read) }
            )
            is ApiResult.Error -> r
        }

    override suspend fun topAgents(): ApiResult<List<AgentMetric>> =
        when (val r = safeApiCall(dispatchers.io) { api.topAgents() }) {
            is ApiResult.Success -> ApiResult.Success((r.data.data ?: emptyList()).map { AgentMetric(it.name, it.replies) })
            is ApiResult.Error -> r
        }
}
