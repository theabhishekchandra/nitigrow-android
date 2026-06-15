package com.websbaba.nitigrow.presentation.feature.payments

import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId

// ─────────────────────────────────────────────────────────────────────────────
// PaymentLinkUiState — "Send a payment link" form + recent activity list.
//
// Spec: docs/phase-3-mobile.md §1.3 "Payments Screen"
//   - Send payment link to contact (enter amount → select contact → send)
//   - Payment history list (amount, contact, status, date)
//
// We deliberately model the form fields as raw strings (amountInr: String, not
// Long) so:
//   • the TextField has full control over what the user is typing,
//   • leading-zero / empty / partial inputs render naturally,
//   • parsing happens once at submission time (isReadyToSend).
//
// SentPaymentLink mirrors the server payload shape — `id` is server assigned,
// `status` advances PENDING → PAID/EXPIRED/FAILED via webhook. These come from
// the real backend (PaymentLinksRepository) in PaymentLinkViewModel.
// ─────────────────────────────────────────────────────────────────────────────

data class PaymentLinkUiState(
    val amountInr: String = "",
    val selectedContactId: String? = null,
    val selectedContactName: String? = null,
    val description: String = "",
    val recentLinks: List<SentPaymentLink> = emptyList(),
    val contacts: List<com.websbaba.nitigrow.domain.model.Contact> = emptyList(),
    val isSending: Boolean = false,
    val error: String? = null,
) {
    /** Submit-button gate — at least ₹1 and a contact selected. */
    val isReadyToSend: Boolean
        get() = (amountInr.toLongOrNull() ?: 0L) > 0L && selectedContactId != null

    /**
     * Sum of links PAID in the current calendar month — drives the
     * "₹N collected in {month}" header subtitle. 0 when nothing was
     * collected this month (the subtitle is omitted in that case).
     */
    val collectedThisMonthInr: Long
        get() {
            val now = YearMonth.now()
            return recentLinks
                .filter {
                    it.status == SentLinkStatus.PAID &&
                        YearMonth.from(it.sentAt.atZone(ZoneId.systemDefault())) == now
                }
                .sumOf { it.amountInr }
        }
}

data class SentPaymentLink(
    val id: String,
    val contactName: String,
    val amountInr: Long,
    val status: SentLinkStatus,
    val sentAt: Instant,
    /** Shareable payment URL — null until the backend returns one; the copy button hides then. */
    val linkUrl: String? = null,
)

enum class SentLinkStatus { PENDING, PAID, EXPIRED, FAILED }
