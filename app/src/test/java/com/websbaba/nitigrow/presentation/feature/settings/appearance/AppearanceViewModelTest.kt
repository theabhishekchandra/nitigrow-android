package com.websbaba.nitigrow.presentation.feature.settings.appearance

import app.cash.turbine.test
import com.websbaba.nitigrow.core.network.ApiResult
import com.websbaba.nitigrow.core.storage.ThemeDataStore
import com.websbaba.nitigrow.domain.model.BillingStatus
import com.websbaba.nitigrow.domain.model.Usage
import com.websbaba.nitigrow.domain.model.UsageMeter
import com.websbaba.nitigrow.domain.repository.BillingRepository
import com.websbaba.nitigrow.core.ui.theme.AppTheme
import com.websbaba.nitigrow.core.ui.theme.PlanTier
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AppearanceViewModelTest {

    private val themeStore: ThemeDataStore = mockk(relaxed = true)
    private val billing: BillingRepository = mockk()

    private fun status(plan: String?) = BillingStatus(
        plan = plan ?: "trial",
        accountStatus = "active",
        subscription = null,
        usage = Usage(UsageMeter(0, 0), UsageMeter(0, 0), UsageMeter(0, 0), UsageMeter(0, 0)),
        prices = emptyMap(),
        referralCreditPaise = 0,
        branding = false,
    )

    @Before fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        every { themeStore.theme } returns MutableStateFlow(AppTheme.SOFT_PAPER)
        every { billing.observeStatus() } returns MutableStateFlow(status("growth"))
        coEvery { billing.refreshStatus() } returns ApiResult.Success(Unit)
    }

    @After fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `state mirrors persisted theme and plan tier, and refreshes status on init`() = runTest {
        val vm = AppearanceViewModel(themeStore, billing)

        assertThat(vm.state.value.selected).isEqualTo(AppTheme.SOFT_PAPER)
        assertThat(vm.state.value.tier).isEqualTo(PlanTier.GROWTH)
        coVerify(exactly = 1) { billing.refreshStatus() }
    }

    @Test
    fun `selecting an unlocked theme persists it`() = runTest {
        val vm = AppearanceViewModel(themeStore, billing)

        vm.onSelect(AppTheme.BRAND_FORWARD) // growth unlocks brand forward

        coVerify(exactly = 1) { themeStore.setTheme(AppTheme.BRAND_FORWARD) }
    }

    @Test
    fun `selecting a locked theme emits UpgradeRequired and does not persist`() = runTest {
        val vm = AppearanceViewModel(themeStore, billing)

        vm.effects.test {
            vm.onSelect(AppTheme.ESPRESSO_PREMIUM) // growth does NOT unlock espresso
            assertThat(awaitItem())
                .isEqualTo(AppearanceEffect.UpgradeRequired(AppTheme.ESPRESSO_PREMIUM))
        }
        coVerify(exactly = 0) { themeStore.setTheme(any()) }
    }

    @Test
    fun `no plan means starter tier and everything premium locked`() = runTest {
        every { billing.observeStatus() } returns MutableStateFlow<BillingStatus?>(null)
        val vm = AppearanceViewModel(themeStore, billing)

        assertThat(vm.state.value.tier).isEqualTo(PlanTier.STARTER)

        vm.effects.test {
            vm.onSelect(AppTheme.BRAND_FORWARD)
            assertThat(awaitItem())
                .isEqualTo(AppearanceEffect.UpgradeRequired(AppTheme.BRAND_FORWARD))
        }
    }
}
