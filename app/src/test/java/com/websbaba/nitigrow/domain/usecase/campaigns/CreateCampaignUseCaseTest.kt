package com.websbaba.nitigrow.domain.usecase.campaigns

import com.google.common.truth.Truth.assertThat
import com.websbaba.nitigrow.core.network.ApiResult
import com.websbaba.nitigrow.core.network.ErrorType
import com.websbaba.nitigrow.domain.model.Campaign
import com.websbaba.nitigrow.domain.model.CampaignStatus
import com.websbaba.nitigrow.domain.repository.CampaignRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.time.Instant

/**
 * A "send now" campaign (no scheduledAt) is only ever queued by a separate
 * POST .../launch call — creating it leaves it a `draft` the backend never sends
 * (backend/src/controllers/campaignController.js createCampaign/launchCampaign).
 * This use case must call launch itself, or the broadcast silently never goes out.
 */
class CreateCampaignUseCaseTest {

    private val repo: CampaignRepository = mockk()
    private val useCase = CreateCampaignUseCase(repo)

    private fun campaign(id: String = "c1") = Campaign(
        id = id, name = "Diwali", templateId = "t1", templateName = "festive_offer",
        audienceTags = listOf("vip"), audienceSize = 10, status = CampaignStatus.DRAFT,
        scheduledAt = null, sentCount = 0, deliveredCount = 0, readCount = 0,
        failedCount = 0, createdAt = Instant.now()
    )

    @Test
    fun `send now launches the campaign right after creating it`() = runTest {
        coEvery { repo.create("Diwali", "t1", listOf("vip"), null) } returns ApiResult.Success(campaign())
        coEvery { repo.launch("c1") } returns ApiResult.Success(Unit)

        val result = useCase("Diwali", "t1", listOf("vip"), scheduledAt = null)

        assertThat(result).isInstanceOf(ApiResult.Success::class.java)
        coVerify(exactly = 1) { repo.launch("c1") }
    }

    @Test
    fun `a scheduled campaign is not launched — the backend enqueues it itself`() = runTest {
        val at = Instant.now().plusSeconds(3600)
        coEvery { repo.create("Diwali", "t1", listOf("vip"), at) } returns ApiResult.Success(campaign())

        val result = useCase("Diwali", "t1", listOf("vip"), scheduledAt = at)

        assertThat(result).isInstanceOf(ApiResult.Success::class.java)
        coVerify(exactly = 0) { repo.launch(any()) }
    }

    @Test
    fun `a launch failure is reported, not swallowed as a silent success`() = runTest {
        coEvery { repo.create("Diwali", "t1", listOf("vip"), null) } returns ApiResult.Success(campaign())
        coEvery { repo.launch("c1") } returns ApiResult.Error(message = "WhatsApp not connected", type = ErrorType.Unknown)

        val result = useCase("Diwali", "t1", listOf("vip"), scheduledAt = null) as ApiResult.Error

        assertThat(result.message).contains("draft")
        assertThat(result.message).contains("WhatsApp not connected")
    }

    @Test
    fun `a create failure is returned as-is and never attempts to launch`() = runTest {
        coEvery { repo.create("Diwali", "t1", listOf("vip"), null) } returns
            ApiResult.Error(message = "Name required")

        val result = useCase("Diwali", "t1", listOf("vip"), scheduledAt = null)

        assertThat((result as ApiResult.Error).message).isEqualTo("Name required")
        coVerify(exactly = 0) { repo.launch(any()) }
    }
}
