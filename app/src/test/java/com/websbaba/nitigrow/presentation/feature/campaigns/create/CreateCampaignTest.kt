package com.websbaba.nitigrow.presentation.feature.campaigns.create

import com.google.common.truth.Truth.assertThat
import com.websbaba.nitigrow.core.util.collapseWhitespace
import com.websbaba.nitigrow.core.network.ApiResult
import com.websbaba.nitigrow.domain.model.Campaign
import com.websbaba.nitigrow.domain.model.CampaignStatus
import com.websbaba.nitigrow.domain.model.Template
import com.websbaba.nitigrow.domain.repository.CampaignRepository
import com.websbaba.nitigrow.domain.repository.ContactRepository
import com.websbaba.nitigrow.domain.usecase.campaigns.CreateCampaignUseCase
import com.websbaba.nitigrow.domain.usecase.campaigns.EstimateAudienceUseCase
import com.websbaba.nitigrow.domain.usecase.campaigns.ObserveTemplatesUseCase
import io.mockk.coEvery
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
import java.time.ZoneId

class CreateCampaignUiStateTest {

    private val now = Instant.parse("2026-09-21T10:00:00Z")

    private fun state(step: WizardStep, block: CreateCampaignUiState.() -> CreateCampaignUiState = { this }) =
        CreateCampaignUiState(step = step).block()

    @Test
    fun `audience step needs a name, a segment and a non-empty reach`() {
        val ready = state(WizardStep.AUDIENCE) {
            copy(name = "Diwali", selectedTags = setOf("VIP"), audienceEstimate = 54)
        }
        assertThat(ready.canProceed(now)).isTrue()
        assertThat(ready.copy(name = "  ").canProceed(now)).isFalse()
        assertThat(ready.copy(selectedTags = emptySet()).canProceed(now)).isFalse()
        assertThat(ready.copy(audienceEstimate = 0).canProceed(now)).isFalse()
        assertThat(ready.copy(audienceEstimate = null).canProceed(now)).isFalse()
    }

    @Test
    fun `template step needs a chosen template but no longer a name`() {
        assertThat(state(WizardStep.TEMPLATE).canProceed(now)).isFalse()
        assertThat(state(WizardStep.TEMPLATE) { copy(selectedTemplateId = "t1") }.canProceed(now)).isTrue()
    }

    @Test
    fun `schedule step accepts send now or a future time only`() {
        assertThat(state(WizardStep.SCHEDULE) { copy(sendNow = true) }.canProceed(now)).isTrue()
        val later = state(WizardStep.SCHEDULE) { copy(sendNow = false, scheduledAt = now.plusSeconds(3600)) }
        assertThat(later.canProceed(now)).isTrue()
        assertThat(later.copy(scheduledAt = now.minusSeconds(60)).canProceed(now)).isFalse()
        assertThat(later.copy(scheduledAt = null).canProceed(now)).isFalse()
    }
}

class SchedulePresetsTest {

    private val zone = ZoneId.of("Asia/Kolkata")

    @Test
    fun `default later is tomorrow at 10 AM local time`() {
        val now = Instant.parse("2026-09-21T04:30:00Z") // 10:00 IST on the 21st
        val at = SchedulePresets.defaultLater(now, zone).atZone(zone)

        assertThat(at.toLocalDate().toString()).isEqualTo("2026-09-22")
        assertThat(at.hour).isEqualTo(10)
        assertThat(at.minute).isEqualTo(0)
    }

    @Test
    fun `quick picks include tonight only while 6 PM is still ahead`() {
        val morning = Instant.parse("2026-09-21T04:30:00Z") // 10:00 IST
        val night = Instant.parse("2026-09-21T15:30:00Z")   // 21:00 IST

        assertThat(SchedulePresets.quickPicks(morning, zone).map { it.label })
            .containsExactly("Today, 6:00 PM", "Tomorrow, 9:00 AM", "Tomorrow, 10:00 AM").inOrder()
        assertThat(SchedulePresets.quickPicks(night, zone).map { it.label })
            .containsExactly("Tomorrow, 9:00 AM", "Tomorrow, 10:00 AM").inOrder()
    }

    @Test
    fun `every quick pick is in the future`() {
        val now = Instant.parse("2026-09-21T04:30:00Z")
        assertThat(SchedulePresets.quickPicks(now, zone).all { it.at.isAfter(now) }).isTrue()
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class CreateCampaignViewModelTest {

    private val campaignRepo: CampaignRepository = mockk(relaxed = true)
    private val contactRepo: ContactRepository = mockk()
    private val createCampaign: CreateCampaignUseCase = mockk()
    private val estimate: EstimateAudienceUseCase = mockk()
    private val observeTemplates: ObserveTemplatesUseCase = mockk()

    @Before fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        every { observeTemplates() } returns flowOf(emptyList<Template>())
        every { contactRepo.observeContacts() } returns flowOf(emptyList())
        coEvery { campaignRepo.refreshTemplates() } returns ApiResult.Success(Unit)
    }

    @After fun tearDown() { Dispatchers.resetMain() }

    private fun vm() = CreateCampaignViewModel(observeTemplates, campaignRepo, contactRepo, createCampaign, estimate)

    @Test
    fun `choosing to schedule pre-fills a future time and sending now clears it`() = runTest {
        val vm = vm()

        vm.setSendNow(false)
        val prefilled = vm.state.value.scheduledAt
        assertThat(prefilled).isNotNull()
        assertThat(prefilled!!.isAfter(Instant.now())).isTrue()

        vm.setSendNow(true)
        assertThat(vm.state.value.scheduledAt).isNull()
    }

    @Test
    fun `an already chosen time survives toggling between options only until send now`() = runTest {
        val vm = vm()
        val custom = Instant.now().plusSeconds(7200)

        vm.setSendNow(false)
        vm.setScheduledAt(custom)
        vm.setSendNow(false)

        assertThat(vm.state.value.scheduledAt).isEqualTo(custom)
    }
}

class CollapseWhitespaceTest {
    @Test
    fun `newlines and repeated spaces fold into single spaces`() {
        val body = "Hi {{1}},\n\nYour order *#{{2}}* is confirmed.  ETA: {{4}}."
        assertThat(body.collapseWhitespace()).isEqualTo("Hi {{1}}, Your order *#{{2}}* is confirmed. ETA: {{4}}.")
        assertThat("  \n ".collapseWhitespace()).isEmpty()
    }
}
