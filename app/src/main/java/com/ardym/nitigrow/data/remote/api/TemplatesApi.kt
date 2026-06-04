package com.ardym.nitigrow.data.remote.api

import com.ardym.nitigrow.data.remote.dto.CreateTemplateRequest
import com.ardym.nitigrow.data.remote.dto.GenericMessageDto
import com.ardym.nitigrow.data.remote.dto.WaTemplateDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface TemplatesApi {

    @GET("templates")
    suspend fun list(): List<WaTemplateDto>

    @POST("templates")
    suspend fun create(@Body body: CreateTemplateRequest): WaTemplateDto

    @DELETE("templates/{id}")
    suspend fun delete(@Path("id") id: String): GenericMessageDto
}
