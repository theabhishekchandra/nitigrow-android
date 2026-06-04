package com.ardym.nitigrow.presentation.feature.payments

import androidx.lifecycle.viewModelScope
import com.ardym.nitigrow.core.network.ApiResult
import com.ardym.nitigrow.domain.model.PaymentLink
import com.ardym.nitigrow.domain.repository.ContactRepository
import com.ardym.nitigrow.domain.repository.PaymentLinksRepository
import com.ardym.nitigrow.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

private const val MAX_AMOUNT_DIGITS = 7 // ₹9,999,999 — well past SMB ceiling

@HiltViewModel
class PaymentLinkViewModel @Inject constructor(
    private val repo: PaymentLinksRepository,
    private val contacts: ContactRepository
) : BaseViewModel() {

    private val _state = MutableStateFlow(PaymentLinkUiState())
    val state: StateFlow<PaymentLinkUiState> = _state.asStateFlow()

    init {
        // Real contacts feed the picker.
        contacts.observeContacts()
            .onEach { list -> _state.update { it.copy(contacts = list) } }
            .launchIn(viewModelScope)
        viewModelScope.launch { contacts.refresh() }
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            (repo.list() as? ApiResult.Success)?.let { r ->
                _state.update { it.copy(recentLinks = r.data.map(::toUi)) }
            }
        }
    }

    /** Accept digits-only, capped at MAX_AMOUNT_DIGITS chars. */
    fun onAmountChange(s: String) {
        if (s.length > MAX_AMOUNT_DIGITS) return
        if (!s.all { it.isDigit() }) return
        _state.update { it.copy(amountInr = s, error = null) }
    }

    fun onPickContact(id: String, name: String) {
        val newId = id.ifBlank { null }
        val newName = if (newId == null) null else name
        _state.update { it.copy(selectedContactId = newId, selectedContactName = newName, error = null) }
    }

    fun onDescriptionChange(s: String) {
        _state.update { it.copy(description = s) }
    }

    fun send() {
        val s = _state.value
        if (!s.isReadyToSend || s.isSending) return
        val amount = s.amountInr.toLongOrNull() ?: return
        val contactId = s.selectedContactId ?: return

        viewModelScope.launch {
            _state.update { it.copy(isSending = true, error = null) }
            when (val res = repo.create(contactId, amount, s.description.ifBlank { null })) {
                is ApiResult.Success -> _state.update {
                    it.copy(
                        isSending = false,
                        amountInr = "",
                        selectedContactId = null,
                        selectedContactName = null,
                        description = "",
                        recentLinks = listOf(toUi(res.data)) + it.recentLinks
                    )
                }
                is ApiResult.Error -> _state.update { it.copy(isSending = false, error = res.message) }
            }
        }
    }

    private fun toUi(p: PaymentLink): SentPaymentLink = SentPaymentLink(
        id = p.id,
        contactName = p.contactName,
        amountInr = p.amount,
        status = when (p.status.lowercase()) {
            "captured" -> SentLinkStatus.PAID
            "failed" -> SentLinkStatus.FAILED
            "refunded" -> SentLinkStatus.EXPIRED
            else -> SentLinkStatus.PENDING
        },
        sentAt = p.sentAt?.let { runCatching { Instant.parse(it) }.getOrNull() } ?: Instant.now()
    )
}
