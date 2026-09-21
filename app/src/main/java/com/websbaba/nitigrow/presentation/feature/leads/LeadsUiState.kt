package com.websbaba.nitigrow.presentation.feature.leads

import com.websbaba.nitigrow.domain.model.Lead
import com.websbaba.nitigrow.domain.model.LeadStage

data class LeadsUiState(
    val leads: List<Lead> = emptyList(),
    /** Stage chip on the list screen; null shows every stage. */
    val stageFilter: LeadStage? = null,
    val isSearching: Boolean = false,
    val query: String = "",
    val isRefreshing: Boolean = false,
    val error: String? = null
) {
    val grouped: Map<LeadStage, List<Lead>> by lazy {
        LeadStage.entries.associateWith { stage -> leads.filter { it.stage == stage } }
    }

    /** Leads still in play — everything except Won and Lost. */
    val openCount: Int get() = leads.count { it.stage != LeadStage.WON && it.stage != LeadStage.LOST }

    fun countIn(stage: LeadStage): Int = grouped[stage].orEmpty().size

    /** Leads after the stage chip and the search text (name, phone or source). */
    val visibleLeads: List<Lead>
        get() {
            val needle = query.trim()
            return leads.filter { lead ->
                (stageFilter == null || lead.stage == stageFilter) &&
                    (needle.isEmpty() ||
                        lead.contactName.contains(needle, ignoreCase = true) ||
                        lead.contactPhone.contains(needle, ignoreCase = true) ||
                        lead.source.contains(needle, ignoreCase = true))
            }
        }
}
