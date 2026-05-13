package com.ardym.nitigrow.presentation.feature.payments

import androidx.lifecycle.viewModelScope
import com.ardym.nitigrow.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

// ─────────────────────────────────────────────────────────────────────────────
// PaymentLinkViewModel — form controller for "Send a payment link".
//
// Why no use-cases yet?
//   The real flow goes through the backend's `POST /payments/links` →
//   Razorpay Payment Link API → outbound WhatsApp template message. None of
//   that is wired up; until it is, this VM fakes a send with a delay and
//   appends to recentLinks so QA can see realistic UI behaviour.
//
// All public surface that touches dummy data carries the standard TODO so a
// project-wide grep finds every removal point.
// ─────────────────────────────────────────────────────────────────────────────

private const val MAX_AMOUNT_DIGITS = 7         // ₹9,999,999 — well past SMB ceiling
private const val FAKE_SEND_DELAY_MS = 800L

@HiltViewModel
class PaymentLinkViewModel @Inject constructor() : BaseViewModel() {

    // TODO: This is dummy data we need to delete when development is complete and connect with real APIs.
    private val _state = MutableStateFlow(
        PaymentLinkUiState(recentLinks = DummyPaymentLinkData.recent())
    )

    // TODO: This is dummy data we need to delete when development is complete and connect with real APIs.
    val state: StateFlow<PaymentLinkUiState> = _state.asStateFlow()

    /** Accept digits-only, capped at MAX_AMOUNT_DIGITS chars. */
    fun onAmountChange(s: String) {
        if (s.length > MAX_AMOUNT_DIGITS) return
        if (!s.all { it.isDigit() }) return
        _state.update { it.copy(amountInr = s, error = null) }
    }

    /**
     * Set the recipient for the payment link. Pass an empty `id` to clear
     * the selection (the chip's ✕ button maps to this).
     */
    fun onPickContact(id: String, name: String) {
        val newId = id.ifBlank { null }
        val newName = if (newId == null) null else name
        _state.update {
            it.copy(selectedContactId = newId, selectedContactName = newName, error = null)
        }
    }

    fun onDescriptionChange(s: String) {
        _state.update { it.copy(description = s) }
    }

    /**
     * TODO: Real flow — call PaymentLinksRepository.create(amountInr, contactId,
     *  description) which routes through Razorpay Payment Links + sends a
     *  WhatsApp template message to the customer. Until that lands, fake the
     *  network call and prepend a PENDING row to the recent list.
     */
    fun send() {
        val s = _state.value
        if (!s.isReadyToSend || s.isSending) return
        val amount = s.amountInr.toLongOrNull() ?: return
        val contactId = s.selectedContactId ?: return
        val contactName = s.selectedContactName ?: contactId

        viewModelScope.launch {
            _state.update { it.copy(isSending = true, error = null) }
            // TODO: This is dummy data we need to delete when development is complete and connect with real APIs.
            delay(FAKE_SEND_DELAY_MS)
            val newLink = SentPaymentLink(
                id = "pl-${UUID.randomUUID().toString().take(8)}",
                contactName = contactName,
                amountInr = amount,
                status = SentLinkStatus.PENDING,
                sentAt = Instant.now(),
            )
            _state.update {
                it.copy(
                    isSending = false,
                    amountInr = "",
                    selectedContactId = null,
                    selectedContactName = null,
                    description = "",
                    recentLinks = listOf(newLink) + it.recentLinks,
                )
            }
        }
    }
}
