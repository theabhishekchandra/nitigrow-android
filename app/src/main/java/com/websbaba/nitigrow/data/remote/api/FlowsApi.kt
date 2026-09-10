package com.websbaba.nitigrow.data.remote.api

import com.websbaba.nitigrow.data.remote.dto.FlowSubmissionListResponse
import com.websbaba.nitigrow.data.remote.dto.SendFlowRequest
import com.websbaba.nitigrow.data.remote.dto.SendFlowResultDto
import com.websbaba.nitigrow.data.remote.dto.WaFlowListResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/** Meta-native WhatsApp Flows: list, send a published flow, read responses. */
interface FlowsApi {
    @GET("wa-flows")
    suspend fun listFlows(): WaFlowListResponse

    @POST("wa-flows/{id}/send")
    suspend fun sendFlow(@Path("id") id: String, @Body body: SendFlowRequest): SendFlowResultDto

    @GET("wa-flows/{id}/submissions")
    suspend fun submissions(@Path("id") id: String): FlowSubmissionListResponse
}
