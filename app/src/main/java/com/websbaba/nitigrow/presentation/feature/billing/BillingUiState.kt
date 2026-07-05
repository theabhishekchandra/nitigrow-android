package com.websbaba.nitigrow.presentation.feature.billing

import com.websbaba.nitigrow.domain.model.BillingStatus
import com.websbaba.nitigrow.domain.model.Invoice

data class BillingUiState(
    val status: BillingStatus? = null,
    val invoices: List<Invoice> = emptyList(),
    val isRefreshing: Boolean = false,
    val isWorking: Boolean = false,   // cancel in flight
    val error: String? = null,
    val message: String? = null,      // one-shot snackbar text
)
