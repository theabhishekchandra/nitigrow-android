package com.ardym.nitigrow.presentation.feature.analytics

import androidx.lifecycle.viewModelScope
import com.ardym.nitigrow.core.network.ApiResult
import com.ardym.nitigrow.domain.repository.AnalyticsRepository
import com.ardym.nitigrow.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val repo: AnalyticsRepository
) : BaseViewModel() {

    private val _state = MutableStateFlow(AnalyticsUiState(isLoading = true))
    val state: StateFlow<AnalyticsUiState> = _state.asStateFlow()

    init { load(DateRange.WEEK) }

    /** Switch the active range chip and reload from the API. */
    fun setRange(r: DateRange) {
        _state.update { it.copy(range = r) }
        load(r)
    }

    private fun load(range: DateRange) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val from = fromDateFor(range)?.toString()

            val series = when (val r = repo.messagesPerDay(from, null)) {
                is ApiResult.Success -> r.data.map {
                    DailyPoint(
                        day = runCatching { LocalDate.parse(it.date) }.getOrElse { LocalDate.now() },
                        sent = it.sent,
                        delivered = it.delivered,
                        read = it.read
                    )
                }
                is ApiResult.Error -> emptyList()
            }

            val agents = (repo.topAgents() as? ApiResult.Success)?.data
                ?.map { AgentPerf(name = it.name, chats = it.replies, avgResponseSeconds = 0) }
                ?: emptyList()

            _state.update {
                it.copy(
                    range = range,
                    timeSeries = series,
                    agentPerformance = agents,
                    isLoading = false
                )
            }
        }
    }

    // Backend defaults to ~14 days; we constrain the start per chip. CUSTOM falls
    // back to the month window until the date-picker lands.
    private fun fromDateFor(range: DateRange): LocalDate? = when (range) {
        DateRange.TODAY -> LocalDate.now()
        DateRange.WEEK -> LocalDate.now().minusDays(6)
        DateRange.MONTH -> LocalDate.now().minusDays(29)
        DateRange.QUARTER -> LocalDate.now().minusDays(89)
        DateRange.CUSTOM -> LocalDate.now().minusDays(29)
    }
}
