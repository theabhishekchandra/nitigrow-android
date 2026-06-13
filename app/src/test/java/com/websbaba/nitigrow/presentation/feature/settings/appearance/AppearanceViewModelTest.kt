package com.websbaba.nitigrow.presentation.feature.settings.appearance

import app.cash.turbine.test
import com.websbaba.nitigrow.core.network.ApiResult
import com.websbaba.nitigrow.core.storage.ThemeDataStore
import com.websbaba.nitigrow.domain.model.Subscription
import com.websbaba.nitigrow.domain.model.SubscriptionStatus
import com.websbaba.nitigrow.domain.repository.BillingRepository
import com.websbaba.nitigrow.ui.theme.AppTheme
import com.websbaba.nitigrow.ui.theme.PlanTier
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

    private fun subscription(planId: String) = Subscription(
        planId = planId,
        planName = planId,
        status = SubscriptionStatus.ACTIVE,
        renewsAt = null,
        cancelledAt = null
    )

    @Before fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        every { themeStore.theme } returns MutableStateFlow(AppTheme.SOFT_PAPER)
        every { billing.observeSubscription() } returns MutableStateFlow(subscription("growth"))
        coEvery { billing.refreshSubscription() } returns ApiResult.Success(Unit)
    }

    @After fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `state mirrors persisted theme and plan tier, and refreshes subscription on init`() = runTest {
        val vm = AppearanceViewModel(themeStore, billing)

        assertThat(vm.state.value.selected).isEqualTo(AppTheme.SOFT_PAPER)
        assertThat(vm.state.value.tier).isEqualTo(PlanTier.GROWTH)
        coVerify(exactly = 1) { billing.refreshSubscription() }
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
    fun `no subscription means starter tier and everything premium locked`() = runTest {
        every { billing.observeSubscription() } returns MutableStateFlow<Subscription?>(null)
        val vm = AppearanceViewModel(themeStore, billing)

        assertThat(vm.state.value.tier).isEqualTo(PlanTier.STARTER)

        vm.effects.test {
            vm.onSelect(AppTheme.BRAND_FORWARD)
            assertThat(awaitItem())
                .isEqualTo(AppearanceEffect.UpgradeRequired(AppTheme.BRAND_FORWARD))
        }
    }
}
