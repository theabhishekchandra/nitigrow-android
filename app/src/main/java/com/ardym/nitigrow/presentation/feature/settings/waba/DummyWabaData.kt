package com.ardym.nitigrow.presentation.feature.settings.waba

import com.ardym.nitigrow.domain.model.WabaStatus
import java.time.Instant
import java.time.temporal.ChronoUnit

// TODO: This is dummy data we need to delete when development is complete and connect with real APIs.
// Seeds [WabaNumberViewModel] until `/api/tenant/waba` is wired by the parent.
object DummyWabaData {

    private const val VERIFIED_DAYS_AGO = 30L

    // TODO: This is dummy data we need to delete when development is complete and connect with real APIs.
    fun status(): WabaNumberUiState = WabaNumberUiState(
        phone = "+91 98100 00001",
        displayName = "ARDYM Trading",
        status = WabaStatus.ACTIVE,
        qualityRating = "GREEN",
        verifiedAt = Instant.now().minus(VERIFIED_DAYS_AGO, ChronoUnit.DAYS),
        messagingLimit = "10K/24h"
    )
}
