package com.ardym.nitigrow.data.remote.api

import com.ardym.nitigrow.data.remote.dto.CreatePaymentLinkRequest
import com.ardym.nitigrow.data.remote.dto.PaymentLinkDto
import com.ardym.nitigrow.data.remote.dto.PaymentLinkListDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface PaymentLinksApi {

    @POST("payments/link")
    suspend fun create(@Body body: CreatePaymentLinkRequest): PaymentLinkDto

    @GET("payments/links")
    suspend fun list(): PaymentLinkListDto
}
