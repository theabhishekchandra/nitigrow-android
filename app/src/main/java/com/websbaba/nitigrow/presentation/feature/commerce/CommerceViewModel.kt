package com.websbaba.nitigrow.presentation.feature.commerce

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.websbaba.nitigrow.core.network.ApiResult
import com.websbaba.nitigrow.domain.repository.CommerceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CommerceViewModel @Inject constructor(
    private val repo: CommerceRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(CommerceUiState())
    val state: StateFlow<CommerceUiState> = _state.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (val r = repo.getProducts()) {
                is ApiResult.Success -> _state.update { it.copy(products = r.data, isLoading = false) }
                is ApiResult.Error -> _state.update { it.copy(error = r.message, isLoading = false) }
            }
            _state.update { it.copy(catalogId = repo.getCatalogId()) }
        }
    }

    fun sendProduct(productRetailerId: String, to: String, body: String?) {
        val catalogId = _state.value.catalogId
        if (catalogId.isNullOrBlank()) {
            _state.update { it.copy(error = "No Meta catalog connected — sync your catalog first.") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(sending = true, error = null, sentMessage = null) }
            when (val r = repo.sendProduct(to, catalogId, productRetailerId, body)) {
                is ApiResult.Success -> _state.update { it.copy(sending = false, sentMessage = "Sent to the customer") }
                is ApiResult.Error -> _state.update { it.copy(sending = false, error = r.message) }
            }
        }
    }

    fun clearSent() = _state.update { it.copy(sentMessage = null) }
}
