package com.websbaba.nitigrow.data.repository

import com.websbaba.nitigrow.core.network.ApiResult
import com.websbaba.nitigrow.core.network.andThen
import com.websbaba.nitigrow.core.network.safeApiCall
import com.websbaba.nitigrow.core.util.DispatcherProvider
import com.websbaba.nitigrow.data.local.dao.BillingStatusDao
import com.websbaba.nitigrow.data.local.dao.InvoiceDao
import com.websbaba.nitigrow.data.mapper.toDomain
import com.websbaba.nitigrow.data.mapper.toEntity
import com.websbaba.nitigrow.data.remote.api.BillingApi
import com.websbaba.nitigrow.domain.model.BillingStatus
import com.websbaba.nitigrow.domain.model.Invoice
import com.websbaba.nitigrow.domain.repository.BillingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillingRepositoryImpl @Inject constructor(
    private val api: BillingApi,
    private val statusDao: BillingStatusDao,
    private val invoiceDao: InvoiceDao,
    private val dispatchers: DispatcherProvider
) : BillingRepository {

    override fun observeStatus(): Flow<BillingStatus?> =
        statusDao.observe().map { it?.toDomain() }

    override fun observeInvoices(): Flow<List<Invoice>> =
        invoiceDao.observeAll().map { rows -> rows.map { it.toDomain() } }

    override suspend fun refreshStatus(): ApiResult<Unit> =
        safeApiCall(dispatchers.io) { api.status() }.andThen { res ->
            statusDao.upsert(res.toEntity())
            ApiResult.Success(Unit)
        }

    override suspend fun refreshInvoices(): ApiResult<Unit> =
        safeApiCall(dispatchers.io) { api.invoices() }.andThen { res ->
            val rows = (res.invoices ?: emptyList())
                .mapIndexed { i, dto -> dto.toEntity(fallbackId = "inv_$i") }
            invoiceDao.clear()
            invoiceDao.upsertAll(rows)
            ApiResult.Success(Unit)
        }

    override suspend fun cancel(): ApiResult<String?> =
        safeApiCall(dispatchers.io) { api.cancel() }.andThen { res ->
            refreshStatus()
            ApiResult.Success(res.message)
        }
}
