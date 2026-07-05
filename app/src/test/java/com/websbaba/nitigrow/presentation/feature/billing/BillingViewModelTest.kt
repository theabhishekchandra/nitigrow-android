package com.websbaba.nitigrow.presentation.feature.billing

import com.websbaba.nitigrow.core.network.ApiResult
import com.websbaba.nitigrow.domain.model.BillingStatus
import com.websbaba.nitigrow.domain.model.Invoice
import com.websbaba.nitigrow.domain.model.Usage
import com.websbaba.nitigrow.domain.model.UsageMeter
import com.websbaba.nitigrow.domain.repository.BillingRepository
import com.websbaba.nitigrow.domain.usecase.billing.CancelSubscriptionUseCase
import com.websbaba.nitigrow.domain.usecase.billing.ObserveBillingStatusUseCase
import com.websbaba.nitigrow.domain.usecase.billing.ObserveInvoicesUseCase
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
class BillingViewModelTest {

    private val observeStatus: ObserveBillingStatusUseCase = mockk()
    private val observeInvoices: ObserveInvoicesUseCase = mockk()
    private val repo: BillingRepository = mockk()
    private val cancelSubscription: CancelSubscriptionUseCase = mockk()

    private val sampleStatus = BillingStatus(
        plan = "growth",
        accountStatus = "active",
        subscription = null,
        usage = Usage(
            messages = UsageMeter(10, 100),
            ai = UsageMeter(0, 50),
            contacts = UsageMeter(5, 1000),
            users = UsageMeter(1, 5),
        ),
        prices = mapOf("growth" to 2499),
        referralCreditPaise = 0,
        branding = false,
    )

    @Before fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        every { observeStatus() } returns MutableStateFlow<BillingStatus?>(sampleStatus)
        every { observeInvoices() } returns MutableStateFlow<List<Invoice>>(emptyList())
        coEvery { repo.refreshStatus() } returns ApiResult.Success(Unit)
        coEvery { repo.refreshInvoices() } returns ApiResult.Success(Unit)
    }

    @After fun tearDown() { Dispatchers.resetMain() }

    private fun vm() = BillingViewModel(observeStatus, observeInvoices, repo, cancelSubscription)

    @Test
    fun `init streams status and refreshes from the network`() = runTest {
        val viewModel = vm()
        assertThat(viewModel.state.value.status).isEqualTo(sampleStatus)
        assertThat(viewModel.state.value.isRefreshing).isFalse()
        coVerify(exactly = 1) { repo.refreshStatus() }
        coVerify(exactly = 1) { repo.refreshInvoices() }
    }

    @Test
    fun `refresh surfaces a network error`() = runTest {
        coEvery { repo.refreshStatus() } returns ApiResult.Error(message = "offline")
        val viewModel = vm()
        assertThat(viewModel.state.value.error).isEqualTo("offline")
    }

    @Test
    fun `cancel success sets a confirmation message and stops working`() = runTest {
        coEvery { cancelSubscription() } returns ApiResult.Success("Cancelled at period end.")
        val viewModel = vm()

        viewModel.cancel()

        assertThat(viewModel.state.value.message).isEqualTo("Cancelled at period end.")
        assertThat(viewModel.state.value.isWorking).isFalse()
    }

    @Test
    fun `cancel error sets error and stops working`() = runTest {
        coEvery { cancelSubscription() } returns ApiResult.Error(message = "no active subscription")
        val viewModel = vm()

        viewModel.cancel()

        assertThat(viewModel.state.value.error).isEqualTo("no active subscription")
        assertThat(viewModel.state.value.isWorking).isFalse()
    }
}
