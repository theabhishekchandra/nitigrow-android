package com.websbaba.nitigrow.presentation.feature.campaigns.list

import com.websbaba.nitigrow.domain.model.Campaign
import com.websbaba.nitigrow.domain.model.CampaignStatus

/** Quick filters shown as chips under the Broadcasts header. */
enum class BroadcastFilter(val label: String) {
    ALL("All"),
    SENDING("Sending"),
    SCHEDULED("Scheduled"),
    COMPLETED("Completed"),
    DRAFT("Draft");

    fun matches(status: CampaignStatus): Boolean = when (this) {
        ALL -> true
        SENDING -> status == CampaignStatus.RUNNING
        SCHEDULED -> status == CampaignStatus.SCHEDULED
        COMPLETED -> status == CampaignStatus.COMPLETED
        DRAFT -> status == CampaignStatus.DRAFT
    }
}

data class CampaignsUiState(
    val items: List<Campaign> = emptyList(),
    /** Approved WhatsApp templates in the local cache; 0 until they've loaded. */
    val approvedTemplates: Int = 0,
    val filter: BroadcastFilter = BroadcastFilter.ALL,
    val isSearching: Boolean = false,
    val query: String = "",
    val isRefreshing: Boolean = false,
    val error: String? = null
) {
    val isInitialLoading: Boolean get() = items.isEmpty() && isRefreshing && error == null

    val sendingCount: Int get() = items.count { it.status == CampaignStatus.RUNNING }
    val scheduledCount: Int get() = items.count { it.status == CampaignStatus.SCHEDULED }

    /** Items after the status chip and the search query (name or template). */
    val visibleItems: List<Campaign>
        get() {
            val needle = query.trim()
            return items.filter { c ->
                filter.matches(c.status) &&
                    (needle.isEmpty() ||
                        c.name.contains(needle, ignoreCase = true) ||
                        c.templateName.contains(needle, ignoreCase = true))
            }
        }
}
