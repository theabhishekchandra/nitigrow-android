package com.ardym.nitigrow.presentation.feature.leads

import com.ardym.nitigrow.domain.model.Lead
import com.ardym.nitigrow.domain.model.LeadStage

data class LeadsUiState(
    val leads: List<Lead> = emptyList(),
    val isRefreshing: Boolean = false,
    val error: String? = null
) {
    val grouped: Map<LeadStage, List<Lead>> by lazy {
        LeadStage.entries.associateWith { stage -> leads.filter { it.stage == stage } }
    }
}
