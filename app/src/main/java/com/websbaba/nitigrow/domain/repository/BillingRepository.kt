package com.websbaba.nitigrow.domain.repository

import com.websbaba.nitigrow.core.network.ApiResult
import com.websbaba.nitigrow.domain.model.BillingStatus
import com.websbaba.nitigrow.domain.model.Invoice
import kotlinx.coroutines.flow.Flow

/**
 * Read-only mobile billing. Subscribing happens on the web (IAP avoidance); the only
 * write here is cancellation, which moves no money in.
 */
interface BillingRepository {
    fun observeStatus(): Flow<BillingStatus?>
    fun observeInvoices(): Flow<List<Invoice>>

    suspend fun refreshStatus(): ApiResult<Unit>
    suspend fun refreshInvoices(): ApiResult<Unit>

    /** Cancels the active subscription at period end. Returns the server message. */
    suspend fun cancel(): ApiResult<String?>
}
