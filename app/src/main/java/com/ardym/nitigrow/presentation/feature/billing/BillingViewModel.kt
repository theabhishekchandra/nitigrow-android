package com.ardym.nitigrow.presentation.feature.billing

import androidx.lifecycle.viewModelScope
import com.ardym.nitigrow.core.network.ApiResult
import com.ardym.nitigrow.core.payments.RazorpayBridge
import com.ardym.nitigrow.core.payments.RazorpayResult
import com.ardym.nitigrow.domain.repository.BillingRepository
import com.ardym.nitigrow.domain.usecase.billing.ObservePaymentsUseCase
import com.ardym.nitigrow.domain.usecase.billing.ObservePlansUseCase
import com.ardym.nitigrow.domain.usecase.billing.ObserveSubscriptionUseCase
import com.ardym.nitigrow.domain.usecase.billing.ReportPaymentFailureUseCase
import com.ardym.nitigrow.domain.usecase.billing.StartCheckoutUseCase
import com.ardym.nitigrow.domain.usecase.billing.VerifyPaymentUseCase
import com.ardym.nitigrow.presentation.base.BaseViewModel
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

@HiltViewModel
class BillingViewModel @Inject constructor(
    observePlans: ObservePlansUseCase,
    observeSubscription: ObserveSubscriptionUseCase,
    observePayments: ObservePaymentsUseCase,
    private val repo: BillingRepository,
    private val startCheckout: StartCheckoutUseCase,
    private val verify: VerifyPaymentUseCase,
    private val reportFailure: ReportPaymentFailureUseCase,
    private val razorpay: RazorpayBridge
) : BaseViewModel() {

    private val _state = MutableStateFlow(BillingUiState())
    val state: StateFlow<BillingUiState> = _state.asStateFlow()

    private val _effects = Channel<BillingEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        observePlans()
            .onEach { plans -> _state.update { it.copy(plans = plans) } }
            .launchIn(viewModelScope)
        observeSubscription()
            .onEach { sub -> _state.update { it.copy(subscription = sub) } }
            .launchIn(viewModelScope)
        observePayments()
            .onEach { p -> _state.update { it.copy(payments = p) } }
            .launchIn(viewModelScope)

        razorpay.events
            .onEach(::handleRazorpay)
            .launchIn(viewModelScope)

        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true, error = null) }
            repo.refreshPlans()
            repo.refreshSubscription()
            repo.refreshPayments()
            _state.update { it.copy(isRefreshing = false) }
        }
    }

    fun onBuy(planId: String) {
        viewModelScope.launch {
            _state.update { it.copy(phase = CheckoutPhase.CREATING_ORDER, error = null) }
            when (val res = startCheckout(planId)) {
                is ApiResult.Success -> {
                    _state.update {
                        it.copy(
                            phase = CheckoutPhase.AWAITING_PAYMENT,
                            pendingOrder = res.data
                        )
                    }
                    _effects.send(BillingEffect.LaunchCheckout(res.data))
                }
                is ApiResult.Error -> _state.update {
                    it.copy(phase = CheckoutPhase.FAILED, error = res.message)
                }
            }
        }
    }

    private fun handleRazorpay(result: RazorpayResult) {
        when (result) {
            is RazorpayResult.Success -> verifyOnServer(result)
            is RazorpayResult.Failure -> markFailed(result)
        }
    }

    private fun verifyOnServer(s: RazorpayResult.Success) {
        viewModelScope.launch {
            _state.update { it.copy(phase = CheckoutPhase.VERIFYING) }
            val orderId = s.orderId ?: _state.value.pendingOrder?.razorpayOrderId.orEmpty()
            val signature = s.signature.orEmpty()
            val res = verify(orderId, s.paymentId, signature)
            when (res) {
                is ApiResult.Success -> {
                    _state.update { it.copy(phase = CheckoutPhase.SUCCESS, pendingOrder = null) }
                    _effects.send(BillingEffect.PaymentSucceeded)
                }
                is ApiResult.Error -> {
                    _state.update {
                        it.copy(phase = CheckoutPhase.FAILED, error = res.message)
                    }
                    _effects.send(BillingEffect.PaymentFailed(res.message))
                }
            }
        }
    }

    private fun markFailed(f: RazorpayResult.Failure) {
        viewModelScope.launch {
            val orderId = f.orderId ?: _state.value.pendingOrder?.razorpayOrderId
            if (!orderId.isNullOrBlank()) reportFailure(orderId, f.message)
            _state.update {
                it.copy(phase = CheckoutPhase.FAILED, error = f.message, pendingOrder = null)
            }
            _effects.send(BillingEffect.PaymentFailed(f.message))
        }
    }

    fun resetPhase() {
        _state.update { it.copy(phase = CheckoutPhase.IDLE, error = null) }
    }
}
