package com.websbaba.nitigrow.presentation.feature.campaigns.detail

import androidx.lifecycle.SavedStateHandle
import com.google.common.truth.Truth.assertThat
import com.websbaba.nitigrow.core.network.ApiResult
import com.websbaba.nitigrow.domain.model.Campaign
import com.websbaba.nitigrow.domain.model.CampaignStatus
import com.websbaba.nitigrow.domain.model.Template
import com.websbaba.nitigrow.domain.repository.CampaignRepository
import com.websbaba.nitigrow.domain.usecase.campaigns.CancelCampaignUseCase
import com.websbaba.nitigrow.domain.usecase.campaigns.ObserveCampaignUseCase
import com.websbaba.nitigrow.presentation.feature.campaigns.detail.components.percentOfSent
import com.websbaba.nitigrow.presentation.feature.campaigns.detail.components.startsInLabel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.temporal.ChronoUnit

class CampaignDetailHelpersTest {

    private val now = Instant.parse("2026-09-21T10:00:00Z")

    @Test
    fun `countdown uses days, hours and minutes as they get closer`() {
        assertThat(startsInLabel(now, now.plus(19, ChronoUnit.DAYS))).isEqualTo("Starts in 19 days")
        assertThat(startsInLabel(now, now.plus(1, ChronoUnit.DAYS))).isEqualTo("Starts in 1 day")
        assertThat(startsInLabel(now, now.plus(125, ChronoUnit.MINUTES))).isEqualTo("Starts in 2h 5m")
        assertThat(startsInLabel(now, now.plus(3, ChronoUnit.HOURS))).isEqualTo("Starts in 3h")
        assertThat(startsInLabel(now, now.plus(12, ChronoUnit.MINUTES))).isEqualTo("Starts in 12m")
    }

    @Test
    fun `countdown never reads 0m and handles a due time`() {
        assertThat(startsInLabel(now, now.plusSeconds(20))).isEqualTo("Starts in 1m")
        assertThat(startsInLabel(now, now)).isEqualTo("Starting now")
        assertThat(startsInLabel(now, now.minusSeconds(60))).isEqualTo("Starting now")
    }

    @Test
    fun `percent of sent rounds and is zero when nothing was sent`() {
        assertThat(percentOfSent(787, 820)).isEqualTo(96)
        assertThat(percentOfSent(558, 820)).isEqualTo(68)
        assertThat(percentOfSent(5, 0)).isEqualTo(0)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class CampaignDetailViewModelTest {

    private val repo: CampaignRepository = mockk()
    private val observeCampaign: ObserveCampaignUseCase = mockk()
    private val cancel: CancelCampaignUseCase = mockk()

    private val campaign = Campaign(
        id = "c1", name = "Diwali Early Bird", templateId = "t1", templateName = "diwali_offer",
        audienceTags = listOf("VIP"), audienceSize = 1240, status = CampaignStatus.RUNNING,
        scheduledAt = null, sentCount = 820, deliveredCount = 787, readCount = 558,
        failedCount = 12, createdAt = Instant.EPOCH
    )
    private val template = Template("t1", "diwali_offer", "en", "MARKETING", "APPROVED", "Namaste {{1}}!", 1, Instant.EPOCH)

    @Before fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        every { observeCampaign("c1") } returns flowOf(campaign)
        every { repo.observeTemplates() } returns flowOf(listOf(template, template.copy(id = "other")))
    }

    @After fun tearDown() { Dispatchers.resetMain() }

    private fun vm() = CampaignDetailViewModel(SavedStateHandle(mapOf("campaignId" to "c1")), observeCampaign, repo, cancel)

    @Test
    fun `state carries the campaign and its template body`() = runTest {
        val s = vm().state.value

        assertThat(s.campaign?.name).isEqualTo("Diwali Early Bird")
        assertThat(s.template?.body).isEqualTo("Namaste {{1}}!")
    }

    @Test
    fun `cancelling asks first and only calls the API once confirmed`() = runTest {
        coEvery { cancel("c1") } returns ApiResult.Success(Unit)
        val vm = vm()

        vm.onCancelRequested()
        assertThat(vm.state.value.confirmingCancel).isTrue()
        coVerify(exactly = 0) { cancel(any()) }

        vm.onCancelConfirmed()
        assertThat(vm.state.value.confirmingCancel).isFalse()
        assertThat(vm.state.value.cancelling).isFalse()
        coVerify(exactly = 1) { cancel("c1") }
    }

    @Test
    fun `dismissing the confirmation does not cancel`() = runTest {
        val vm = vm()

        vm.onCancelRequested()
        vm.onCancelDismissed()

        assertThat(vm.state.value.confirmingCancel).isFalse()
        coVerify(exactly = 0) { cancel(any()) }
    }

    @Test
    fun `a failed cancel surfaces the error`() = runTest {
        coEvery { cancel("c1") } returns ApiResult.Error(message = "Already sent")
        val vm = vm()

        vm.onCancelConfirmed()

        assertThat(vm.state.value.error).isEqualTo("Already sent")
        assertThat(vm.state.value.cancelling).isFalse()
    }
}
