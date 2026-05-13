package com.ardym.nitigrow.presentation.feature.analytics

import com.ardym.nitigrow.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class AnalyticsViewModel @Inject constructor() : BaseViewModel() {

    // TODO: This is dummy data we need to delete when development is complete and connect with real APIs.
    private val _state = MutableStateFlow(DummyAnalyticsData.snapshot(DateRange.WEEK))
    val state: StateFlow<AnalyticsUiState> = _state.asStateFlow()

    /** Switch the range chip — recomputes snapshot from dummy generator. */
    fun setRange(r: DateRange) {
        // TODO: This is dummy data we need to delete when development is complete and connect with real APIs.
        _state.update { DummyAnalyticsData.snapshot(r) }
    }
}
