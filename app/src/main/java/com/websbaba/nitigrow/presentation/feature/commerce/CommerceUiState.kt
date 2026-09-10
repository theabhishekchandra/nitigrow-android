package com.websbaba.nitigrow.presentation.feature.commerce

import com.websbaba.nitigrow.domain.model.Product

data class CommerceUiState(
    val isLoading: Boolean = true,
    val products: List<Product> = emptyList(),
    val catalogId: String? = null,
    val error: String? = null,
    val sending: Boolean = false,
    val sentMessage: String? = null,
)
