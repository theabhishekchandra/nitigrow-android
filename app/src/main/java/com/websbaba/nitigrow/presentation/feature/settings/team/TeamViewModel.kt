package com.websbaba.nitigrow.presentation.feature.settings.team

import androidx.lifecycle.viewModelScope
import com.websbaba.nitigrow.core.network.ApiResult
import com.websbaba.nitigrow.domain.model.TeamMember
import com.websbaba.nitigrow.domain.repository.ProfileRepository
import com.websbaba.nitigrow.domain.usecase.profile.InviteMemberUseCase
import com.websbaba.nitigrow.presentation.base.BaseViewModel
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

data class TeamUiState(
    val members: List<TeamMember> = emptyList(),
    val isRefreshing: Boolean = false,
    val sheetOpen: Boolean = false,
    val saving: Boolean = false,
    val error: String? = null
)

sealed interface TeamEffect { data class Toast(val text: String) : TeamEffect }

@HiltViewModel
class TeamViewModel @Inject constructor(
    private val repo: ProfileRepository,
    private val invite: InviteMemberUseCase
) : BaseViewModel() {

    private val _state = MutableStateFlow(TeamUiState())
    val state: StateFlow<TeamUiState> = _state.asStateFlow()

    private val _effects = Channel<TeamEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        repo.observeTeam()
            .onEach { items -> _state.update { it.copy(members = items) } }
            .launchIn(viewModelScope)
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true, error = null) }
            when (val res = repo.refreshTeam()) {
                is ApiResult.Success -> _state.update { it.copy(isRefreshing = false) }
                is ApiResult.Error -> _state.update {
                    it.copy(isRefreshing = false, error = res.message)
                }
            }
        }
    }

    fun openInviteSheet() = _state.update { it.copy(sheetOpen = true, error = null) }
    fun closeInviteSheet() = _state.update { it.copy(sheetOpen = false) }

    fun inviteMember(email: String, role: String) {
        viewModelScope.launch {
            _state.update { it.copy(saving = true, error = null) }
            when (val res = invite(email, role)) {
                is ApiResult.Success -> {
                    _state.update { it.copy(saving = false, sheetOpen = false) }
                    _effects.send(TeamEffect.Toast("Invite sent to $email"))
                }
                is ApiResult.Error -> _state.update {
                    it.copy(saving = false, error = res.message)
                }
            }
        }
    }

    fun removeMember(id: String) {
        viewModelScope.launch { repo.removeMember(id) }
    }
}
