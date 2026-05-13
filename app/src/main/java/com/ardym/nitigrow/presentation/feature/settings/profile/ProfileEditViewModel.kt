package com.ardym.nitigrow.presentation.feature.settings.profile

import androidx.lifecycle.viewModelScope
import com.ardym.nitigrow.core.network.ApiResult
import com.ardym.nitigrow.domain.model.User
import com.ardym.nitigrow.domain.repository.ProfileRepository
import com.ardym.nitigrow.domain.usecase.profile.ObserveProfileUseCase
import com.ardym.nitigrow.domain.usecase.profile.UpdateProfileUseCase
import com.ardym.nitigrow.domain.usecase.profile.UploadAvatarUseCase
import com.ardym.nitigrow.presentation.base.BaseViewModel
import com.ardym.nitigrow.presentation.dummy.DummyData
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

    // TODO: This is dummy data we need to delete when development is complete and connect with real APIs.
    // Seeded so the form pre-fills even when offline / before the first refreshProfile() succeeds.
    private val seed = DummyData.profile()

    private val _state = MutableStateFlow(
        ProfileEditUiState(profile = seed, name = seed.name, email = seed.email)
    )
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
