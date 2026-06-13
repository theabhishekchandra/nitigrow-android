package com.websbaba.nitigrow.presentation.feature.referrals

import com.websbaba.nitigrow.core.network.ApiResult
import com.websbaba.nitigrow.domain.model.LoyaltyProgram
import com.websbaba.nitigrow.domain.model.ReferralFunnel
import com.websbaba.nitigrow.domain.model.ReferralLeader
import com.websbaba.nitigrow.domain.model.ReferralProgram
import com.websbaba.nitigrow.domain.model.SaasReferral
import com.websbaba.nitigrow.domain.repository.ReferralsRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ReferralsViewModelTest {

    private val repo: ReferralsRepository = mockk()

    private val program = ReferralProgram(
        enabled = false, rewardType = "loyalty_points",
        referrerReward = 100, refereeReward = 50, qualifyOn = "first_purchase", shareMessage = ""
    )

    @Before fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        coEvery { repo.getProgram() } returns ApiResult.Success(program)
        coEvery { repo.getLoyalty() } returns ApiResult.Success(LoyaltyProgram(true, 1.0, 10, 100))
        coEvery { repo.getFunnel() } returns ApiResult.Success(ReferralFunnel(1, 2, 3, 0, 6))
        coEvery { repo.getLeaderboard() } returns ApiResult.Success(listOf(ReferralLeader("Riya", "+9199", 3)))
        coEvery { repo.getSaas() } returns ApiResult.Success(SaasReferral("BIZ-1", "https://x/?ref=BIZ-1", 50000, 2, 1, 50000))
    }

    @After fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `load populates state from the repository`() = runTest {
        val vm = ReferralsViewModel(repo)
        val s = vm.state.value
        assertThat(s.isLoading).isFalse()
        assertThat(s.program?.referrerReward).isEqualTo(100)
        assertThat(s.funnel?.total).isEqualTo(6)
        assertThat(s.leaders).hasSize(1)
        assertThat(s.saas?.code).isEqualTo("BIZ-1")
    }

    @Test
    fun `setEnabled updates the program from the response`() = runTest {
        coEvery { repo.setEnabled(true) } returns ApiResult.Success(program.copy(enabled = true))
        val vm = ReferralsViewModel(repo)

        vm.setEnabled(true)

        assertThat(vm.state.value.program?.enabled).isTrue()
        assertThat(vm.state.value.isToggling).isFalse()
    }

    @Test
    fun `load surfaces an error when the program call fails`() = runTest {
        coEvery { repo.getProgram() } returns ApiResult.Error(message = "Server down")
        val vm = ReferralsViewModel(repo)
        assertThat(vm.state.value.error).isEqualTo("Server down")
    }
}
