package com.websbaba.nitigrow.data.repository

import com.websbaba.nitigrow.core.network.ApiResult
import com.websbaba.nitigrow.core.network.andThen
import com.websbaba.nitigrow.core.network.safeApiCall
import com.websbaba.nitigrow.core.util.DispatcherProvider
import com.websbaba.nitigrow.data.remote.api.FlowsApi
import com.websbaba.nitigrow.data.remote.dto.FlowSubmissionDto
import com.websbaba.nitigrow.data.remote.dto.SendFlowRequest
import com.websbaba.nitigrow.data.remote.dto.WaFlowDto
import com.websbaba.nitigrow.domain.model.FlowSubmission
import com.websbaba.nitigrow.domain.model.WaFlow
import com.websbaba.nitigrow.domain.repository.FlowsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FlowsRepositoryImpl @Inject constructor(
    private val api: FlowsApi,
    private val dispatchers: DispatcherProvider,
) : FlowsRepository {

    private fun WaFlowDto.toDomain() = WaFlow(
        id = id,
        metaFlowId = metaFlowId,
        name = name,
        categories = categories.orEmpty(),
        status = status,
        createdAt = createdAt,
    )

    private fun FlowSubmissionDto.toDomain(): FlowSubmission {
        // responseJson is arbitrary { field: value } — flatten to displayable strings,
        // skipping the internal flow_token.
        val fields = responseJson?.entrySet()
            ?.filter { it.key != "flow_token" && it.key != "screen" }
            ?.associate { (k, v) ->
                k to (if (v.isJsonPrimitive) v.asString else v.toString())
            }
            .orEmpty()
        return FlowSubmission(
            id = id,
            flowName = flowName,
            contactName = contact?.name,
            contactPhone = contact?.phone,
            fields = fields,
            createdAt = createdAt,
        )
    }

    override suspend fun getFlows(): ApiResult<List<WaFlow>> =
        safeApiCall(dispatchers.io) { api.listFlows() }.andThen { r ->
            ApiResult.Success(r.data.orEmpty().map { it.toDomain() })
        }

    override suspend fun sendFlow(flowId: String, to: String, bodyText: String?): ApiResult<Unit> =
        safeApiCall(dispatchers.io) {
            api.sendFlow(flowId, SendFlowRequest(to = to, cta = "Open form", bodyText = bodyText))
        }.andThen { r ->
            ApiResult.Success(Unit)
        }

    override suspend fun getSubmissions(flowId: String): ApiResult<List<FlowSubmission>> =
        safeApiCall(dispatchers.io) { api.submissions(flowId) }.andThen { r ->
            ApiResult.Success(r.data.orEmpty().map { it.toDomain() })
        }
}
