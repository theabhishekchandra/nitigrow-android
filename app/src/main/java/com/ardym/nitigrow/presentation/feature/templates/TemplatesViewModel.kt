package com.ardym.nitigrow.presentation.feature.templates

import com.ardym.nitigrow.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class TemplatesViewModel @Inject constructor() : BaseViewModel() {

    // TODO: This is dummy data we need to delete when development is complete and connect with real APIs.
    private val _state = MutableStateFlow(
        TemplatesUiState(templates = DummyTemplatesData.all())
    )
    val state: StateFlow<TemplatesUiState> = _state.asStateFlow()

    /** Switch the active status filter (chip row on TemplatesScreen). */
    fun setFilter(f: TemplateFilter) {
        _state.update { it.copy(filter = f) }
    }
}
