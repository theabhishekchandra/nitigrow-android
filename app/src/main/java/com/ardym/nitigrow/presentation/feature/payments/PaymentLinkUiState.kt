package com.ardym.nitigrow.presentation.feature.payments

import java.time.Instant

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
// SentPaymentLink mirrors the eventual server payload shape — `id` is server
// assigned, `status` advances PENDING → PAID/EXPIRED/FAILED via webhook. Until
// the backend is wired we synthesise these locally inside the ViewModel.
// ─────────────────────────────────────────────────────────────────────────────

// TODO: This is dummy data we need to delete when development is complete and connect with real APIs.
data class PaymentLinkUiState(
    val amountInr: String = "",
    val selectedContactId: String? = null,
    val selectedContactName: String? = null,
    val description: String = "",
    val recentLinks: List<SentPaymentLink> = emptyList(),
    val isSending: Boolean = false,
    val error: String? = null,
) {
    /** Submit-button gate — at least ₹1 and a contact selected. */
    val isReadyToSend: Boolean
        get() = (amountInr.toLongOrNull() ?: 0L) > 0L && selectedContactId != null
}

// TODO: This is dummy data we need to delete when development is complete and connect with real APIs.
data class SentPaymentLink(
    val id: String,
    val contactName: String,
    val amountInr: Long,
    val status: SentLinkStatus,
    val sentAt: Instant,
)

enum class SentLinkStatus { PENDING, PAID, EXPIRED, FAILED }
