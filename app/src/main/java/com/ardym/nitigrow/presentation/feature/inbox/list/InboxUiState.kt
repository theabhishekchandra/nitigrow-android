package com.ardym.nitigrow.presentation.feature.inbox.list

import com.ardym.nitigrow.domain.model.Conversation

data class InboxUiState(
    val query: String = "",
    val items: List<Conversation> = emptyList(),
    val isRefreshing: Boolean = false,
    val error: String? = null
) {
    val isInitialLoading: Boolean get() = items.isEmpty() && isRefreshing && error == null
    val isEmpty: Boolean get() = items.isEmpty() && !isRefreshing
}
