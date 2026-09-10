package com.websbaba.nitigrow.domain.repository

import com.websbaba.nitigrow.core.network.ApiResult
import com.websbaba.nitigrow.domain.model.FlowSubmission
import com.websbaba.nitigrow.domain.model.WaFlow

interface FlowsRepository {
    suspend fun getFlows(): ApiResult<List<WaFlow>>
    suspend fun sendFlow(flowId: String, to: String, bodyText: String?): ApiResult<Unit>
    suspend fun getSubmissions(flowId: String): ApiResult<List<FlowSubmission>>
}
