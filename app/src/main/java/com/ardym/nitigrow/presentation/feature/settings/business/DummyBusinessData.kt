package com.ardym.nitigrow.presentation.feature.settings.business

// TODO: This is dummy data we need to delete when development is complete and connect with real APIs.
// Seeds [BusinessProfileViewModel] until the backend `/api/tenant/profile` endpoint is wired.
object DummyBusinessData {

    // TODO: This is dummy data we need to delete when development is complete and connect with real APIs.
    fun profile(): BusinessProfileUiState = BusinessProfileUiState(
        name = "ARDYM Trading Co.",
        address = "112, Connaught Place\nNew Delhi 110001\nIndia",
        website = "ardym.in",
        email = "hello@ardym.in",
        logoUrl = null,
        gstin = "07AABCA1234X1Z5"
    )
}
