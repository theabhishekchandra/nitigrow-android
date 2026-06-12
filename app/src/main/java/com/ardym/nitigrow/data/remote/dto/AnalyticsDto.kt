package com.ardym.nitigrow.data.remote.dto

import com.google.gson.annotations.SerializedName

data class MessagesPerDayDto(
    @SerializedName("_id") val date: String = "",
    val total: Int = 0,
    val outbound: Int = 0,
    val inbound: Int = 0,
    val delivered: Int = 0,
    val read: Int = 0
)

data class TopAgentsResponseDto(
    val data: List<AgentDto>? = null
)

data class AgentDto(
    val agentId: String? = null,
    val name: String = "Unknown",
    val replies: Int = 0
)
