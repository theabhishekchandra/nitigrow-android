package com.ardym.nitigrow.domain.usecase.profile

import com.ardym.nitigrow.core.network.ApiResult
import com.ardym.nitigrow.core.util.Constants
import com.ardym.nitigrow.domain.repository.ProfileRepository
import javax.inject.Inject

class UploadAvatarUseCase @Inject constructor(private val repo: ProfileRepository) {
    suspend operator fun invoke(bytes: ByteArray, mimeType: String): ApiResult<String> {
        if (bytes.size > Constants.MAX_IMAGE_BYTES) {
            return ApiResult.Error(message = "Image exceeds 5MB")
        }
        if (!mimeType.startsWith("image/")) {
            return ApiResult.Error(message = "Not an image")
        }
        return repo.uploadAvatar(bytes, mimeType)
    }
}
