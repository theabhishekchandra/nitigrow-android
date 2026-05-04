package com.ardym.nitigrow.presentation.feature.settings

import androidx.lifecycle.viewModelScope
import com.ardym.nitigrow.core.network.ApiResult
import com.ardym.nitigrow.core.util.LocaleManager
import com.ardym.nitigrow.domain.model.NotificationPreferences
import com.ardym.nitigrow.domain.model.Tenant
import com.ardym.nitigrow.domain.model.User
import com.ardym.nitigrow.domain.repository.ProfileRepository
import com.ardym.nitigrow.domain.repository.PushTokenRepository
import com.ardym.nitigrow.domain.usecase.auth.LogoutUseCase
import com.ardym.nitigrow.domain.usecase.profile.ObserveProfileUseCase
import com.ardym.nitigrow.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsHubUiState(
    val profile: User? = null,
    val tenant: Tenant? = null,
    val notificationPrefs: NotificationPreferences = NotificationPreferences(),
    val languageTag: String = "",
    val isExporting: Boolean = false,
    val isDeleting: Boolean = false,
    val message: String? = null,
    val error: String? = null
)

sealed interface SettingsHubEffect {
    data class Toast(val text: String) : SettingsHubEffect
    data object DeleteConfirmed : SettingsHubEffect
    data object LoggedOut : SettingsHubEffect
}

@HiltViewModel
class SettingsHubViewModel @Inject constructor(
    observeProfile: ObserveProfileUseCase,
    private val repo: ProfileRepository,
    private val logout: LogoutUseCase,
    private val pushRepo: PushTokenRepository
) : BaseViewModel() {

    private val _state = MutableStateFlow(SettingsHubUiState())
    val state: StateFlow<SettingsHubUiState> = _state.asStateFlow()

    private val _effects = Channel<SettingsHubEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        observeProfile()
            .onEach { p -> _state.update { it.copy(profile = p) } }
            .launchIn(viewModelScope)
        repo.observeTenant()
            .onEach { t -> _state.update { it.copy(tenant = t) } }
            .launchIn(viewModelScope)
        repo.observeNotificationPreferences()
            .onEach { n -> _state.update { it.copy(notificationPrefs = n) } }
            .launchIn(viewModelScope)
        repo.observeLanguage()
            .onEach { tag -> _state.update { it.copy(languageTag = tag) } }
            .launchIn(viewModelScope)

        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            repo.refreshProfile()
            repo.refreshTenant()
        }
    }

    fun toggleNotif(transform: (NotificationPreferences) -> NotificationPreferences) {
        viewModelScope.launch {
            val next = transform(_state.value.notificationPrefs)
            repo.updateNotificationPreferences(next)
        }
    }

    fun setLanguage(tag: String) {
        viewModelScope.launch {
            repo.setLanguage(tag)
            LocaleManager.apply(tag)
        }
    }

    fun requestExport() {
        viewModelScope.launch {
            _state.update { it.copy(isExporting = true) }
            when (val res = repo.requestDataExport()) {
                is ApiResult.Success -> {
                    _state.update { it.copy(isExporting = false) }
                    _effects.send(SettingsHubEffect.Toast("Export queued. Email link in 24h."))
                }
                is ApiResult.Error -> _state.update {
                    it.copy(isExporting = false, error = res.message)
                }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            runCatching { pushRepo.unregisterCurrentToken() }
            logout()
            _effects.send(SettingsHubEffect.LoggedOut)
        }
    }

    fun confirmDelete(reason: String?) {
        viewModelScope.launch {
            _state.update { it.copy(isDeleting = true) }
            when (val res = repo.requestAccountDelete(reason)) {
                is ApiResult.Success -> {
                    _state.update { it.copy(isDeleting = false) }
                    _effects.send(SettingsHubEffect.DeleteConfirmed)
                }
                is ApiResult.Error -> _state.update {
                    it.copy(isDeleting = false, error = res.message)
                }
            }
        }
    }
}
