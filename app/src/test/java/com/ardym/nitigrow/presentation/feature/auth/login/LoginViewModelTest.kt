package com.ardym.nitigrow.presentation.feature.auth.login

import app.cash.turbine.test
import com.ardym.nitigrow.core.network.ApiResult
import com.ardym.nitigrow.domain.usecase.auth.RequestOtpUseCase
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
class LoginViewModelTest {

    private val requestOtp: RequestOtpUseCase = mockk()
    private lateinit var vm: LoginViewModel

    @Before fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        vm = LoginViewModel(requestOtp)
    }

    @After fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `onPhoneChange filters non-digits`() = runTest {
        vm.onPhoneChange("98a76b543210")
        assertThat(vm.state.value.phone).isEqualTo("9876543210")
        assertThat(vm.state.value.isPhoneValid).isTrue()
    }

    @Test
    fun `onSubmit success emits NavigateToOtp`() = runTest {
        coEvery { requestOtp(any()) } returns ApiResult.Success(Unit)
        vm.onPhoneChange("9876543210")

        vm.effects.test {
            vm.onSubmit()
            val effect = awaitItem()
            assertThat(effect).isInstanceOf(LoginEffect.NavigateToOtp::class.java)
            assertThat((effect as LoginEffect.NavigateToOtp).phone).isEqualTo("9876543210")
        }
        assertThat(vm.state.value.isLoading).isFalse()
    }

    @Test
    fun `onSubmit error sets error state`() = runTest {
        coEvery { requestOtp(any()) } returns ApiResult.Error(message = "Server down")
        vm.onPhoneChange("9876543210")
        vm.onSubmit()

        assertThat(vm.state.value.error).isEqualTo("Server down")
        assertThat(vm.state.value.isLoading).isFalse()
    }
}
