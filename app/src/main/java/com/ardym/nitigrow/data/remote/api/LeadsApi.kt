package com.ardym.nitigrow.data.remote.api

import com.ardym.nitigrow.data.remote.dto.CreateLeadRequest
import com.ardym.nitigrow.data.remote.dto.LeadDto
import com.ardym.nitigrow.data.remote.dto.LeadListResponse
import com.ardym.nitigrow.data.remote.dto.MoveLeadStageRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface LeadsApi {

    @GET("leads")
    suspend fun list(): LeadListResponse

    @POST("leads")
    suspend fun create(@Body body: CreateLeadRequest): LeadDto

    @PATCH("leads/{id}/stage")
    suspend fun moveStage(
        @Path("id") id: String,
        @Body body: MoveLeadStageRequest
    ): LeadDto
}
