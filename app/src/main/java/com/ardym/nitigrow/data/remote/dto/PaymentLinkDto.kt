package com.ardym.nitigrow.data.remote.dto

data class PaymentLinkDto(
    val id: String = "",
    val contactName: String = "",
    val amount: Long = 0,
    val status: String = "created",
    val linkUrl: String? = null,
    val sentAt: String? = null
)

data class PaymentLinkListDto(
    val data: List<PaymentLinkDto> = emptyList()
)

data class CreatePaymentLinkRequest(
    val contactId: String,
    val amount: Long,
    val description: String?
)
