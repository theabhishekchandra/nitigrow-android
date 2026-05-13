package com.ardym.nitigrow.domain.usecase.profile

import com.ardym.nitigrow.core.network.ApiResult
import com.ardym.nitigrow.domain.model.TeamMember
import com.ardym.nitigrow.domain.repository.ProfileRepository
import javax.inject.Inject

class InviteMemberUseCase @Inject constructor(private val repo: ProfileRepository) {
    suspend operator fun invoke(email: String, role: String): ApiResult<TeamMember> {
        if (email.isBlank()) return ApiResult.Error(message = "Email required")
        if (role !in listOf("ADMIN", "AGENT")) return ApiResult.Error(message = "Invalid role")
        return repo.inviteMember(email.trim(), role)
    }
}
