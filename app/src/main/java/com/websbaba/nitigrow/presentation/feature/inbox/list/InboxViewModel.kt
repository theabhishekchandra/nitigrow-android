package com.websbaba.nitigrow.presentation.feature.inbox.list

import androidx.lifecycle.viewModelScope
import com.websbaba.nitigrow.core.network.ApiResult
import com.websbaba.nitigrow.domain.usecase.inbox.ObserveConversationsUseCase
import com.websbaba.nitigrow.domain.usecase.inbox.RefreshInboxUseCase
import com.websbaba.nitigrow.domain.usecase.inbox.TogglePinUseCase
import com.websbaba.nitigrow.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class InboxViewModel @Inject constructor(
    observe: ObserveConversationsUseCase,
    private val refreshInbox: RefreshInboxUseCase,
    private val togglePin: TogglePinUseCase
) : BaseViewModel() {

    private val _state = MutableStateFlow(InboxUiState(isRefreshing = true))
    val state: StateFlow<InboxUiState> = _state.asStateFlow()

    private val _query = MutableStateFlow("")

    init {
        _query
            .debounce { if (it.isEmpty()) 0L else SEARCH_DEBOUNCE_MS }
            .distinctUntilChanged()
            .flatMapLatest { observe(it) }
            .onEach { items -> _state.update { it.copy(items = items) } }
            .launchIn(viewModelScope)
        refresh()
    }

    fun onQueryChange(value: String) {
        _state.update { it.copy(query = value) }
        _query.value = value
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true, error = null) }
            when (val res = refreshInbox()) {
                is ApiResult.Success -> _state.update { it.copy(isRefreshing = false) }
                is ApiResult.Error -> _state.update {
                    it.copy(isRefreshing = false, error = res.message)
                }
            }
        }
    }

    fun onTogglePin(conversationId: String, currentlyPinned: Boolean) {
        viewModelScope.launch { togglePin(conversationId, !currentlyPinned) }
    }

    fun onFilterChange(filter: InboxFilter) {
        _state.update { it.copy(filter = filter) }
    }

    fun onFilterSheetVisibleChange(visible: Boolean) {
        _state.update { it.copy(isFilterSheetVisible = visible) }
    }

    /** Clears the quick filter and the sheet selections back to their defaults. */
    fun onResetFilters() {
        _state.update {
            it.copy(
                filter = InboxFilter.ALL,
                assigneeFilter = InboxUiState.DEFAULT_ASSIGNEE,
                labelFilter = InboxUiState.DEFAULT_LABEL
            )
        }
    }

    fun onAssigneeFilterChange(value: String) {
        _state.update { it.copy(assigneeFilter = value) }
    }

    fun onLabelFilterChange(value: String) {
        _state.update { it.copy(labelFilter = value) }
    }

    companion object { private const val SEARCH_DEBOUNCE_MS = 300L }
}
