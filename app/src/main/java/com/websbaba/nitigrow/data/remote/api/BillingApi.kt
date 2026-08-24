package com.websbaba.nitigrow.data.remote.api

import com.websbaba.nitigrow.data.remote.dto.BillingStatusDto
import com.websbaba.nitigrow.data.remote.dto.CancelResponse
import com.websbaba.nitigrow.data.remote.dto.InvoicesResponse
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * Read-only subscription billing for mobile. Subscribing/upgrading happens on the web
 * (avoids the Apple/Google IAP mandate), so there is no order/checkout endpoint here —
 * only status, invoices, and cancellation (which moves no money in).
 */
interface BillingApi {

    @GET("billing/status")
    suspend fun status(): BillingStatusDto

    @GET("billing/invoices")
    suspend fun invoices(): InvoicesResponse

    @POST("billing/cancel")
    suspend fun cancel(): CancelResponse
}
