package com.websbaba.nitigrow.presentation.feature.flows

import com.websbaba.nitigrow.domain.model.FlowSubmission
import com.websbaba.nitigrow.domain.model.WaFlow

data class FlowsUiState(
    val isLoading: Boolean = true,
    val flows: List<WaFlow> = emptyList(),
    val error: String? = null,
    val sending: Boolean = false,
    val sentMessage: String? = null,
    val submissionsFor: WaFlow? = null,
    val submissions: List<FlowSubmission> = emptyList(),
    val loadingSubmissions: Boolean = false,
)
