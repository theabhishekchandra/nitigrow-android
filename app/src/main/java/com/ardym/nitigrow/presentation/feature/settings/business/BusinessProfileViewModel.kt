package com.ardym.nitigrow.presentation.feature.settings.business

import androidx.lifecycle.viewModelScope
import com.ardym.nitigrow.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val SIMULATED_SAVE_LATENCY_MS = 600L

@HiltViewModel
class BusinessProfileViewModel @Inject constructor() : BaseViewModel() {

    // TODO: This is dummy data we need to delete when development is complete and connect with real APIs.
    private val _state = MutableStateFlow(DummyBusinessData.profile())
    val state: StateFlow<BusinessProfileUiState> = _state.asStateFlow()

    private val _effects = Channel<BusinessProfileEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    fun onName(v: String) = _state.update { it.copy(name = v, error = null) }
    fun onAddress(v: String) = _state.update { it.copy(address = v, error = null) }
    fun onWebsite(v: String) = _state.update { it.copy(website = v, error = null) }
    fun onEmail(v: String) = _state.update { it.copy(email = v, error = null) }
    fun onGstin(v: String) = _state.update { it.copy(gstin = v.uppercase(), error = null) }

    fun onLogoTap() {
        viewModelScope.launch {
            // TODO: Wire to image picker + uploadLogo() use-case once backend endpoint exists.
            _effects.send(BusinessProfileEffect.Toast("Change logo coming soon"))
        }
    }

    fun save() {
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, error = null) }
            // TODO: Replace simulated latency with real `tenantRepository.updateBusinessProfile(...)`.
            delay(SIMULATED_SAVE_LATENCY_MS)
            _state.update { it.copy(isSaving = false) }
            _effects.send(BusinessProfileEffect.Toast("Business profile saved"))
            _effects.send(BusinessProfileEffect.Saved)
        }
    }
}
