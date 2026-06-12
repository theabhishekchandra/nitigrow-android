package com.ardym.nitigrow.presentation.feature.dashboard

import com.ardym.nitigrow.domain.model.DashboardStats

data class DashboardUiState(
    val stats: DashboardStats? = null,
    /** Full display name of the signed-in user (from the profile store). */
    val userName: String? = null,
    /** Business (tenant) name shown as the header micro-label. */
    val businessName: String? = null,
    /**
     * Conversations whose 24h service window closes within the next 3 hours —
     * computed with the same `windowExpiryLabel` rule the Inbox filter uses.
     * Drives the tap-to-Inbox alert banner; 0 hides it.
     */
    val expiringWindowCount: Int = 0,
    /** True when any conversation has unread messages — drives the bell dot. */
    val hasUnreadConversations: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null
) {
    val isInitialLoading: Boolean get() = stats == null && isRefreshing

    /** First name only — "Anita Sharma" → "Anita". Null until the profile loads. */
    val firstName: String?
        get() = userName?.trim()?.split(' ')?.firstOrNull()?.takeIf { it.isNotBlank() }
}
