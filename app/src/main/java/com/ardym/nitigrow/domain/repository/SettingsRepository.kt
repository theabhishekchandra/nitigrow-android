package com.ardym.nitigrow.domain.repository

import com.ardym.nitigrow.core.network.ApiResult
import com.ardym.nitigrow.domain.model.AppSettings

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
