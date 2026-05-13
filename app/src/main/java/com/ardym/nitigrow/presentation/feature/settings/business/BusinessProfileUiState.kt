package com.ardym.nitigrow.presentation.feature.settings.business

/**
 * UI state for the Business Profile sub-screen.
 *
 * Fields are seeded from [DummyBusinessData.profile] until the backend
 * `GET /api/tenant/profile` endpoint is wired in by the parent.
 */
data class BusinessProfileUiState(
    val name: String = "",
    val address: String = "",
    val website: String = "",
    val email: String = "",
    val logoUrl: String? = null,
    val gstin: String = "",
    val isSaving: Boolean = false,
    val error: String? = null
)

sealed interface BusinessProfileEffect {
    data class Toast(val text: String) : BusinessProfileEffect
    data object Saved : BusinessProfileEffect
}
