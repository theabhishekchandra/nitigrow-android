package com.websbaba.nitigrow.domain.usecase.profile

import com.google.common.truth.Truth.assertThat
import com.websbaba.nitigrow.core.network.ApiResult
import com.websbaba.nitigrow.domain.model.TeamMember
import com.websbaba.nitigrow.domain.model.UserRole
import com.websbaba.nitigrow.domain.repository.ProfileRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * POST /team/invite requires `name` (backend/src/controllers/teamController.js emails the
 * invite and has nothing else to address it by) and validates `role` against its own
 * vocabulary — not the sheet's "ADMIN"/"AGENT" — so both need translating before the
 * repository call, never passed through raw.
 */
class InviteMemberUseCaseTest {

    private val repo: ProfileRepository = mockk()
    private val useCase = InviteMemberUseCase(repo)

    private fun member() = TeamMember(
        id = "u1", name = "Asha", email = "asha@nitigrow.in",
        role = UserRole.AGENT, isOwner = false, joinedAtMs = 0
    )

    @Test
    fun `Admin and Agent map to real backend role strings, never the UI token`() = runTest {
        coEvery { repo.inviteMember(any(), any(), any()) } returns ApiResult.Success(member())

        useCase("Asha", "asha@nitigrow.in", "ADMIN")
        useCase("Rohan", "rohan@nitigrow.in", "AGENT")

        coVerify { repo.inviteMember("Asha", "asha@nitigrow.in", "manager") }
        coVerify { repo.inviteMember("Rohan", "rohan@nitigrow.in", "sales_agent") }
    }

    @Test
    fun `a blank name is rejected before any network call`() = runTest {
        val result = useCase("  ", "a@b.com", "AGENT")

        assertThat((result as ApiResult.Error).message).contains("Name")
        coVerify(exactly = 0) { repo.inviteMember(any(), any(), any()) }
    }

    @Test
    fun `an unrecognised role is rejected rather than sent to the backend`() = runTest {
        val result = useCase("Asha", "asha@nitigrow.in", "SUPERADMIN")

        assertThat(result).isInstanceOf(ApiResult.Error::class.java)
        coVerify(exactly = 0) { repo.inviteMember(any(), any(), any()) }
    }
}
