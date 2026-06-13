package com.websbaba.nitigrow.presentation.feature.settings.business

import androidx.lifecycle.viewModelScope
import com.websbaba.nitigrow.core.network.ApiResult
import com.websbaba.nitigrow.domain.repository.SettingsRepository
import com.websbaba.nitigrow.presentation.base.BaseViewModel
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
                // businessName is editable + persisted; email is read-only.
                is ApiResult.Success -> _state.update {
                    it.copy(name = r.data.businessName, email = r.data.email)
                }
                is ApiResult.Error -> _state.update { it.copy(error = r.message) }
            }
        }
    }

    fun onName(v: String) = _state.update { it.copy(name = v, error = null) }

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
