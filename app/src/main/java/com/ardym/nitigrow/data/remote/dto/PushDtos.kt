package com.ardym.nitigrow.data.remote.dto

import com.google.gson.annotations.SerializedName

data class RegisterTokenRequest(
    @SerializedName("token") val token: String,
    @SerializedName("platform") val platform: String = "android",
    @SerializedName("deviceId") val deviceId: String? = null,
    @SerializedName("appVersion") val appVersion: String? = null
)
