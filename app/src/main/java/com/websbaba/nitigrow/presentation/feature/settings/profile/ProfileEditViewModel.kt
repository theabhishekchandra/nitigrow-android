package com.websbaba.nitigrow.presentation.feature.settings.profile

import androidx.lifecycle.viewModelScope
import com.websbaba.nitigrow.core.network.ApiResult
import com.websbaba.nitigrow.domain.model.User
import com.websbaba.nitigrow.domain.repository.ProfileRepository
import com.websbaba.nitigrow.domain.usecase.profile.ObserveProfileUseCase
import com.websbaba.nitigrow.domain.usecase.profile.UpdateProfileUseCase
import com.websbaba.nitigrow.domain.usecase.profile.UploadAvatarUseCase
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

data class ProfileEditUiState(
    val profile: User? = null,
    val name: String = "",
    val email: String = "",
    val avatarUrl: String? = null,
    val saving: Boolean = false,
    val uploading: Boolean = false,
    val error: String? = null
)

sealed interface ProfileEditEffect {
    data object Saved : ProfileEditEffect
}

@HiltViewModel
class ProfileEditViewModel @Inject constructor(
    observeProfile: ObserveProfileUseCase,
    private val repo: ProfileRepository,
    private val update: UpdateProfileUseCase,
    private val uploadAvatar: UploadAvatarUseCase
) : BaseViewModel() {

    private val _state = MutableStateFlow(ProfileEditUiState())
    val state: StateFlow<ProfileEditUiState> = _state.asStateFlow()

    private val _effects = Channel<ProfileEditEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        var hydratedFromBackend = false
        observeProfile()
            .onEach { p ->
                if (p == null) return@onEach
                _state.update {
                    if (!hydratedFromBackend) {
                        hydratedFromBackend = true
                        it.copy(profile = p, name = p.name, email = p.email)
                    } else {
                        it.copy(profile = p)
                    }
                }
            }
            .launchIn(viewModelScope)
        viewModelScope.launch { repo.refreshProfile() }
    }

    fun onName(v: String) = _state.update { it.copy(name = v) }
    fun onEmail(v: String) = _state.update { it.copy(email = v) }

    fun save() {
        viewModelScope.launch {
            val s = _state.value
            _state.update { it.copy(saving = true, error = null) }
            when (val res = update(s.name, s.email)) {
                is ApiResult.Success -> {
                    _state.update { it.copy(saving = false) }
                    _effects.send(ProfileEditEffect.Saved)
                }
                is ApiResult.Error -> _state.update {
                    it.copy(saving = false, error = res.message)
                }
            }
        }
    }

    fun upload(bytes: ByteArray, mime: String) {
        viewModelScope.launch {
            _state.update { it.copy(uploading = true, error = null) }
            when (val res = uploadAvatar(bytes, mime)) {
                is ApiResult.Success -> _state.update {
                    it.copy(uploading = false, avatarUrl = res.data)
                }
                is ApiResult.Error -> _state.update {
                    it.copy(uploading = false, error = res.message)
                }
            }
        }
    }
}
