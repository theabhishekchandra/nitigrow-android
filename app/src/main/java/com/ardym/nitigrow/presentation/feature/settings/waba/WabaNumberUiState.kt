package com.ardym.nitigrow.presentation.feature.settings.waba

/**
 * UI state for the WhatsApp Business Account (WABA) number sub-screen.
 *
 * Mirrors the shape of `/api/tenant/waba` — refresh + re-verify use cases
 * will write into this state once the parent wires real repositories.
 */
data class WabaNumberUiState(
    val phone: String = "",
    val displayName: String = "",
    val status: com.ardym.nitigrow.domain.model.WabaStatus =
        com.ardym.nitigrow.domain.model.WabaStatus.NOT_LINKED,
    val qualityRating: String = "GREEN",
    val verifiedAt: java.time.Instant? = null,
    val messagingLimit: String = "1K/24h"
)
