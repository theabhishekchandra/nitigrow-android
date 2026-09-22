package com.websbaba.nitigrow.domain.usecase.profile

import com.websbaba.nitigrow.core.network.ApiResult
import com.websbaba.nitigrow.domain.model.TeamMember
import com.websbaba.nitigrow.domain.repository.ProfileRepository
import javax.inject.Inject

class InviteMemberUseCase @Inject constructor(private val repo: ProfileRepository) {

    suspend operator fun invoke(name: String, email: String, role: String): ApiResult<TeamMember> {
        if (name.isBlank()) return ApiResult.Error(message = "Name required")
        if (email.isBlank()) return ApiResult.Error(message = "Email required")
        val backendRole = ROLE_TO_BACKEND[role]
            ?: return ApiResult.Error(message = "Invalid role")
        return repo.inviteMember(name.trim(), email.trim(), backendRole)
    }

    private companion object {
        // The invite sheet only offers these two picks; the backend's own role vocabulary
        // (backend/src/controllers/teamController.js ROLES) is finer-grained — manager,
        // sales_agent, support_agent, campaign_manager, analyst, accountant — with no plain
        // "admin"/"agent". Chosen so each still covers what the app lets a teammate do:
        // Admin -> manager (team, campaigns, settings); Agent -> sales_agent (inbox +
        // contacts read/write, per backend/src/config/permissions.js).
        val ROLE_TO_BACKEND = mapOf(
            "ADMIN" to "manager",
            "AGENT" to "sales_agent"
        )
    }
}
