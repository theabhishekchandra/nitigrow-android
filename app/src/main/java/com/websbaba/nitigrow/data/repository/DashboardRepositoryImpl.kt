package com.websbaba.nitigrow.data.repository

import com.websbaba.nitigrow.core.network.ApiResult
import com.websbaba.nitigrow.core.network.andThen
import com.websbaba.nitigrow.core.network.safeApiCall
import com.websbaba.nitigrow.core.util.DispatcherProvider
import com.websbaba.nitigrow.data.local.dao.DashboardDao
import com.websbaba.nitigrow.data.mapper.toDomain
import com.websbaba.nitigrow.data.mapper.toEntity
import com.websbaba.nitigrow.data.remote.api.DashboardApi
import com.websbaba.nitigrow.domain.model.DashboardStats
import com.websbaba.nitigrow.domain.repository.DashboardRepository
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
        safeApiCall(dispatchers.io) { api.getOverview() }.andThen { res ->
            dao.upsert(res.toStats().toEntity())
            ApiResult.Success(Unit)
        }
}
