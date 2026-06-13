package com.websbaba.nitigrow.domain.usecase.profile

import com.websbaba.nitigrow.core.network.ApiResult
import com.websbaba.nitigrow.domain.model.TeamMember
import com.websbaba.nitigrow.domain.repository.ProfileRepository
import javax.inject.Inject

class InviteMemberUseCase @Inject constructor(private val repo: ProfileRepository) {
    suspend operator fun invoke(email: String, role: String): ApiResult<TeamMember> {
        if (email.isBlank()) return ApiResult.Error(message = "Email required")
        if (role !in listOf("ADMIN", "AGENT")) return ApiResult.Error(message = "Invalid role")
        return repo.inviteMember(email.trim(), role)
    }
}
