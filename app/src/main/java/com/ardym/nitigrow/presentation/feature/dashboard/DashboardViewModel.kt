package com.ardym.nitigrow.presentation.feature.dashboard

import androidx.lifecycle.viewModelScope
import com.ardym.nitigrow.core.network.ApiResult
import com.ardym.nitigrow.domain.repository.ProfileRepository
import com.ardym.nitigrow.domain.usecase.dashboard.GetDashboardStatsUseCase
import com.ardym.nitigrow.domain.usecase.dashboard.RefreshDashboardUseCase
import com.ardym.nitigrow.domain.usecase.inbox.ObserveConversationsUseCase
import com.ardym.nitigrow.domain.usecase.inbox.RefreshInboxUseCase
import com.ardym.nitigrow.domain.usecase.profile.ObserveProfileUseCase
import com.ardym.nitigrow.presentation.base.BaseViewModel
import com.ardym.nitigrow.presentation.feature.inbox.list.windowExpiryLabel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    getStats: GetDashboardStatsUseCase,
    observeProfile: ObserveProfileUseCase,
    profileRepo: ProfileRepository,
    observeConversations: ObserveConversationsUseCase,
    private val refreshInbox: RefreshInboxUseCase,
    private val refreshStats: RefreshDashboardUseCase
) : BaseViewModel() {

    private val _state = MutableStateFlow(DashboardUiState(isRefreshing = true))
    val state: StateFlow<DashboardUiState> = _state.asStateFlow()

    init {
        getStats()
            .onEach { stats -> if (stats != null) _state.update { it.copy(stats = stats) } }
            .launchIn(viewModelScope)

        // Header identity — same sources SettingsHub uses (ProfileRepository-backed).
        observeProfile()
            .onEach { user -> if (user != null) _state.update { it.copy(userName = user.name) } }
            .launchIn(viewModelScope)
        profileRepo.observeTenant()
            .onEach { tenant ->
                if (tenant != null) _state.update { it.copy(businessName = tenant.name) }
            }
            .launchIn(viewModelScope)

        // Expiring 24h-window + unread signals for the alert banner and bell dot.
        // Reuses the exact `windowExpiryLabel` rule the Inbox filter chips use,
        // so the dashboard count always matches the Inbox "Expiring" chip.
        observeConversations("")
            .onEach { conversations ->
                _state.update {
                    it.copy(
                        expiringWindowCount =
                            conversations.count { c -> c.windowExpiryLabel() != null },
                        hasUnreadConversations =
                            conversations.any { c -> c.unreadCount > 0 }
                    )
                }
            }
            .launchIn(viewModelScope)

        // Best-effort one-shot fetch so the greeting is populated on first entry;
        // failures are silent (the header simply omits the names until cached).
        viewModelScope.launch {
            profileRepo.refreshProfile()
            profileRepo.refreshTenant()
        }

        refresh()
    }

    fun refresh() {
        // Keep the conversation cache warm too (banner + bell dot). Best-effort:
        // its failures never surface here — the stats path owns the error state.
        viewModelScope.launch { refreshInbox() }
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true, error = null) }
            when (val res = refreshStats()) {
                is ApiResult.Success -> _state.update { it.copy(isRefreshing = false) }
                is ApiResult.Error -> _state.update {
                    it.copy(isRefreshing = false, error = res.message)
                }
            }
        }
    }

    fun dismissError() = _state.update { it.copy(error = null) }
}
