package com.websbaba.nitigrow.presentation.theme

import app.cash.turbine.test
import com.websbaba.nitigrow.core.storage.ThemeDataStore
import com.websbaba.nitigrow.domain.model.BillingStatus
import com.websbaba.nitigrow.domain.model.Usage
import com.websbaba.nitigrow.domain.model.UsageMeter
import com.websbaba.nitigrow.domain.repository.BillingRepository
import com.websbaba.nitigrow.ui.theme.AppTheme
import com.websbaba.nitigrow.ui.theme.PlanTier
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ThemeViewModelTest {

    private val themeDataStore: ThemeDataStore = mockk()
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
    }

    @After fun tearDown() { Dispatchers.resetMain() }

    private fun vm() = ThemeViewModel(themeDataStore, billing)

    @Test
    fun `premium theme applies when plan unlocks it`() = runTest {
        every { themeDataStore.theme } returns MutableStateFlow(AppTheme.ESPRESSO_PREMIUM)
        every { billing.observeStatus() } returns MutableStateFlow(status("pro"))

        vm().appTheme.test {
            assertThat(expectMostRecentItem()).isEqualTo(AppTheme.ESPRESSO_PREMIUM)
        }
    }

    @Test
    fun `premium theme falls back to default when plan is too low`() = runTest {
        every { themeDataStore.theme } returns MutableStateFlow(AppTheme.ESPRESSO_PREMIUM)
        every { billing.observeStatus() } returns MutableStateFlow(status("starter"))

        vm().appTheme.test {
            assertThat(expectMostRecentItem()).isEqualTo(AppTheme.Default)
        }
    }

    @Test
    fun `growth plan unlocks brand forward but not espresso`() = runTest {
        every { themeDataStore.theme } returns MutableStateFlow(AppTheme.BRAND_FORWARD)
        every { billing.observeStatus() } returns MutableStateFlow(status("growth"))

        val viewModel = vm()
        viewModel.appTheme.test {
            assertThat(expectMostRecentItem()).isEqualTo(AppTheme.BRAND_FORWARD)
        }
        viewModel.planTier.test {
            assertThat(expectMostRecentItem()).isEqualTo(PlanTier.GROWTH)
        }
    }

    @Test
    fun `theme downgrades live when the subscription lapses`() = runTest {
        val st = MutableStateFlow<BillingStatus?>(status("pro"))
        every { themeDataStore.theme } returns MutableStateFlow(AppTheme.ESPRESSO_PREMIUM)
        every { billing.observeStatus() } returns st

        vm().appTheme.test {
            assertThat(expectMostRecentItem()).isEqualTo(AppTheme.ESPRESSO_PREMIUM)
            st.value = null // status gone → STARTER
            assertThat(awaitItem()).isEqualTo(AppTheme.Default)
        }
    }

    @Test
    fun `billing stream error degrades to starter tier instead of crashing`() = runTest {
        every { themeDataStore.theme } returns MutableStateFlow(AppTheme.BRAND_FORWARD)
        every { billing.observeStatus() } returns flow { throw RuntimeException("boom") }

        val viewModel = vm()
        viewModel.planTier.test {
            assertThat(expectMostRecentItem()).isEqualTo(PlanTier.STARTER)
        }
        viewModel.appTheme.test {
            assertThat(expectMostRecentItem()).isEqualTo(AppTheme.Default)
        }
    }
}
