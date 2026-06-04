package com.ardym.nitigrow.presentation.feature.settings.business

import androidx.lifecycle.viewModelScope
import com.ardym.nitigrow.core.network.ApiResult
import com.ardym.nitigrow.domain.repository.SettingsRepository
import com.ardym.nitigrow.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BusinessProfileViewModel @Inject constructor(
    private val settings: SettingsRepository
) : BaseViewModel() {

    private val _state = MutableStateFlow(BusinessProfileUiState())
    val state: StateFlow<BusinessProfileUiState> = _state.asStateFlow()

    private val _effects = Channel<BusinessProfileEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init { load() }

    private fun load() {
        viewModelScope.launch {
            when (val r = settings.get()) {
                is ApiResult.Success -> _state.update {
                    // name + email come from the backend; address/website/GSTIN/logo
                    // have no tenant field yet, so they stay locally editable.
                    it.copy(name = r.data.businessName, email = r.data.email)
                }
                is ApiResult.Error -> _state.update { it.copy(error = r.message) }
            }
        }
    }

    fun onName(v: String) = _state.update { it.copy(name = v, error = null) }
    fun onAddress(v: String) = _state.update { it.copy(address = v, error = null) }
    fun onWebsite(v: String) = _state.update { it.copy(website = v, error = null) }
    fun onEmail(v: String) = _state.update { it.copy(email = v, error = null) }
    fun onGstin(v: String) = _state.update { it.copy(gstin = v.uppercase(), error = null) }

    fun onLogoTap() {
        viewModelScope.launch {
            _effects.send(BusinessProfileEffect.Toast("Change logo coming soon"))
        }
    }

    fun save() {
        val name = _state.value.name.trim()
        if (name.isEmpty()) {
            _state.update { it.copy(error = "Business name is required") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, error = null) }
            val res = settings.updateProfile(name)
            _state.update { it.copy(isSaving = false) }
            when (res) {
                is ApiResult.Success -> {
                    _effects.send(BusinessProfileEffect.Toast("Business profile saved"))
                    _effects.send(BusinessProfileEffect.Saved)
                }
                is ApiResult.Error -> _state.update { it.copy(error = res.message) }
            }
        }
    }
}
