package com.websbaba.nitigrow.domain.repository

import com.websbaba.nitigrow.core.network.ApiResult
import com.websbaba.nitigrow.domain.model.AppSettings

interface SettingsRepository {
    suspend fun get(): ApiResult<AppSettings>
    suspend fun updateProfile(businessName: String): ApiResult<Unit>
    suspend fun updateAutoReplies(
        welcomeEnabled: Boolean,
        welcomeMessage: String,
        awayEnabled: Boolean,
        awayMessage: String
    ): ApiResult<Unit>
}
