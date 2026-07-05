package com.websbaba.nitigrow.presentation.feature.billing

import androidx.lifecycle.viewModelScope
import com.websbaba.nitigrow.core.network.ApiResult
import com.websbaba.nitigrow.domain.repository.BillingRepository
import com.websbaba.nitigrow.domain.usecase.billing.CancelSubscriptionUseCase
import com.websbaba.nitigrow.domain.usecase.billing.ObserveBillingStatusUseCase
import com.websbaba.nitigrow.domain.usecase.billing.ObserveInvoicesUseCase
import com.websbaba.nitigrow.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Read-only billing. Mobile shows plan / usage / invoices and can cancel; subscribing
 * and upgrading happen on the web to avoid the Apple/Google IAP mandate.
 */
@HiltViewModel
class BillingViewModel @Inject constructor(
    observeStatus: ObserveBillingStatusUseCase,
    observeInvoices: ObserveInvoicesUseCase,
    private val repo: BillingRepository,
    private val cancelSubscription: CancelSubscriptionUseCase,
) : BaseViewModel() {

    private val _state = MutableStateFlow(BillingUiState(isRefreshing = true))
    val state: StateFlow<BillingUiState> = _state.asStateFlow()

    init {
        observeStatus()
            .onEach { s -> _state.update { it.copy(status = s) } }
            .launchIn(viewModelScope)
        observeInvoices()
            .onEach { inv -> _state.update { it.copy(invoices = inv) } }
            .launchIn(viewModelScope)
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true, error = null) }
            val statusRes = repo.refreshStatus()
            val invoicesRes = repo.refreshInvoices()
            val err = (statusRes as? ApiResult.Error)?.message
                ?: (invoicesRes as? ApiResult.Error)?.message
            _state.update { it.copy(isRefreshing = false, error = err) }
        }
    }

    fun cancel() {
        if (_state.value.isWorking) return
        viewModelScope.launch {
            _state.update { it.copy(isWorking = true, error = null) }
            when (val res = cancelSubscription()) {
                is ApiResult.Success -> _state.update {
                    it.copy(
                        isWorking = false,
                        message = res.data
                            ?: "Your subscription will be cancelled at the end of the billing period.",
                    )
                }
                is ApiResult.Error -> _state.update {
                    it.copy(isWorking = false, error = res.message)
                }
            }
        }
    }

    fun consumeMessage() {
        _state.update { it.copy(message = null) }
    }
}
