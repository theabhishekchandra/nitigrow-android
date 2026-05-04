package com.ardym.nitigrow.data.repository

import com.ardym.nitigrow.core.network.ApiResult
import com.ardym.nitigrow.core.network.safeApiCall
import com.ardym.nitigrow.core.util.DispatcherProvider
import com.ardym.nitigrow.data.local.dao.DashboardDao
import com.ardym.nitigrow.data.mapper.toDomain
import com.ardym.nitigrow.data.mapper.toEntity
import com.ardym.nitigrow.data.remote.api.DashboardApi
import com.ardym.nitigrow.domain.model.DashboardStats
import com.ardym.nitigrow.domain.repository.DashboardRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DashboardRepositoryImpl @Inject constructor(
    private val api: DashboardApi,
    private val dao: DashboardDao,
    private val dispatchers: DispatcherProvider
) : DashboardRepository {

    override fun observeStats(): Flow<DashboardStats?> =
        dao.observe().map { it?.toDomain() }

    override suspend fun refresh(): ApiResult<Unit> =
        when (val res = safeApiCall(dispatchers.io) { api.getStats() }) {
            is ApiResult.Success -> {
                dao.upsert(res.data.toEntity())
                ApiResult.Success(Unit)
            }
            is ApiResult.Error -> res
        }
}
