package com.ardym.nitigrow.data.remote.dto

import com.google.gson.annotations.SerializedName

data class DashboardStatsDto(
    @SerializedName("messagesSent") val messagesSent: Long,
    @SerializedName("messagesDelivered") val messagesDelivered: Long,
    @SerializedName("messagesRead") val messagesRead: Long,
    @SerializedName("leadsTotal") val leadsTotal: Long,
    @SerializedName("leadsNew") val leadsNew: Long,
    @SerializedName("activeCampaigns") val activeCampaigns: Int,
    @SerializedName("revenueInr") val revenueInr: Long
)
