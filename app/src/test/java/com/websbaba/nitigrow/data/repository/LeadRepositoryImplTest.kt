package com.websbaba.nitigrow.data.repository

import com.google.common.truth.Truth.assertThat
import com.websbaba.nitigrow.core.network.ApiResult
import com.websbaba.nitigrow.core.util.DispatcherProvider
import com.websbaba.nitigrow.data.local.dao.LeadDao
import com.websbaba.nitigrow.data.local.entity.LeadEntity
import com.websbaba.nitigrow.data.remote.api.LeadsApi
import com.websbaba.nitigrow.data.remote.dto.LeadDto
import com.websbaba.nitigrow.domain.model.LeadStage
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * A rejected stage move (offline, backend refusal — see LeadStageWireFormatTest for the
 * vocabulary bug this used to hit on every move) must not leave the board showing a stage
 * the server never accepted; the optimistic write has to be rolled back.
 */
class LeadRepositoryImplTest {

    private val api: LeadsApi = mockk()
    private val dao: LeadDao = mockk(relaxUnitFun = true)
    private val dispatchers: DispatcherProvider = mockk {
        every { io } returns Dispatchers.Unconfined
    }
    private val repo = LeadRepositoryImpl(api, dao, dispatchers)

    private fun entity(stage: String) = LeadEntity(
        id = "l1", contactId = "c1", contactName = "Priya", contactPhone = "9198",
        source = "website", stage = stage, valueInr = 1000, ownerName = null, notes = null,
        createdAtEpochMs = 0, updatedAtEpochMs = 0
    )

    @Test
    fun `a successful move leaves the optimistic write in place`() = runTest {
        coEvery { dao.get("l1") } returns entity("CONTACTED")
        coEvery { api.moveStage("l1", any()) } returns mockk<LeadDto>(relaxed = true)

        val result = repo.moveStage("l1", LeadStage.QUALIFIED)

        assertThat(result).isInstanceOf(ApiResult.Success::class.java)
        coVerify(exactly = 1) { dao.setStage("l1", "QUALIFIED", any()) }
        coVerify(exactly = 0) { dao.setStage("l1", "CONTACTED", any()) }
    }

    @Test
    fun `a rejected move rolls the optimistic write back to what it was before`() = runTest {
        coEvery { dao.get("l1") } returns entity("CONTACTED")
        coEvery { api.moveStage("l1", any()) } throws java.io.IOException("offline")

        val result = repo.moveStage("l1", LeadStage.QUALIFIED)

        assertThat(result).isInstanceOf(ApiResult.Error::class.java)
        coVerifyOrder {
            dao.setStage("l1", "QUALIFIED", any())  // optimistic
            dao.setStage("l1", "CONTACTED", any())  // rolled back
        }
    }

    @Test
    fun `a rejected move on a lead Room has never seen just skips the rollback, no crash`() = runTest {
        coEvery { dao.get("l1") } returns null
        coEvery { api.moveStage("l1", any()) } throws java.io.IOException("offline")

        val result = repo.moveStage("l1", LeadStage.QUALIFIED)

        assertThat(result).isInstanceOf(ApiResult.Error::class.java)
        coVerify(exactly = 1) { dao.setStage("l1", "QUALIFIED", any()) }
    }
}
