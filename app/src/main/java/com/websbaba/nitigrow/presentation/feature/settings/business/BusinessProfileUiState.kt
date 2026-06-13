package com.websbaba.nitigrow.presentation.feature.settings.business

/**
 * UI state for the Business Profile sub-screen.
 *
 * Only [name] is editable+persisted: it maps to the tenant `businessName`
 * exposed by GET /api/settings and saved via PATCH /api/settings/profile.
 * [email] is read-only (the backend has no profile-email update path).
 *
 * Address / website / GSTIN / logo are intentionally absent — the Tenant
 * model and the settings/profile endpoint have no fields to back them, so we
 * do not surface inputs that would silently discard the user's edits.
 */
data class BusinessProfileUiState(
    val name: String = "",
    val email: String = "",
    val isSaving: Boolean = false,
    val error: String? = null
)

sealed interface BusinessProfileEffect {
    data class Toast(val text: String) : BusinessProfileEffect
    data object Saved : BusinessProfileEffect
}
