package com.websbaba.nitigrow.presentation.feature.leads

import com.websbaba.nitigrow.domain.model.Lead
import com.websbaba.nitigrow.domain.model.LeadStage

data class LeadsUiState(
    val leads: List<Lead> = emptyList(),
    val isRefreshing: Boolean = false,
    val error: String? = null
) {
    val grouped: Map<LeadStage, List<Lead>> by lazy {
        LeadStage.entries.associateWith { stage -> leads.filter { it.stage == stage } }
    }
}
