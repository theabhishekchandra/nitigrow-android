package com.websbaba.nitigrow.presentation.feature.auth.login

import app.cash.turbine.test
import com.websbaba.nitigrow.core.network.ApiResult
import com.websbaba.nitigrow.core.telemetry.Events
import com.websbaba.nitigrow.core.telemetry.Telemetry
import com.websbaba.nitigrow.domain.model.User
import com.websbaba.nitigrow.domain.model.UserRole
import com.websbaba.nitigrow.domain.repository.PushTokenRepository
import com.websbaba.nitigrow.domain.usecase.auth.LoginWithEmailUseCase
import com.websbaba.nitigrow.domain.usecase.auth.RequestOtpUseCase
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
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

    private val loginWithEmail: LoginWithEmailUseCase = mockk()
    private val requestOtp: RequestOtpUseCase = mockk()
    private val pushTokenRepo: PushTokenRepository = mockk(relaxed = true)
    private val telemetry: Telemetry = mockk(relaxed = true)
    private lateinit var vm: LoginViewModel

    private val user = User(
        id = "user-1",
        tenantId = "tenant-1",
        name = "Test Owner",
        email = "owner@websbaba.in",
        phone = "9876543210",
        role = UserRole.OWNER
    )

    @Before fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        vm = LoginViewModel(loginWithEmail, requestOtp, pushTokenRepo, telemetry)
    }

    @After fun tearDown() { Dispatchers.resetMain() }

    // --- Email mode (default) ---

    @Test
    fun `email mode is the default`() {
        assertThat(vm.state.value.isEmailMode).isTrue()
    }

    @Test
    fun `email submit with blank fields sets validation error without calling use case`() = runTest {
        vm.onSubmit()

        assertThat(vm.state.value.error).isEqualTo("Enter both email and password")
        coVerify(exactly = 0) { loginWithEmail(any(), any()) }
    }

    @Test
    fun `email submit success emits NavigateToHome, registers push token and tags telemetry`() = runTest {
        coEvery { loginWithEmail("owner@websbaba.in", "secret") } returns ApiResult.Success(user)
        vm.onEmailChange("owner@websbaba.in")
        vm.onPasswordChange("secret")

        vm.effects.test {
            vm.onSubmit()
            assertThat(awaitItem()).isEqualTo(LoginEffect.NavigateToHome)
        }
        assertThat(vm.state.value.isLoading).isFalse()
        verify { telemetry.event(Events.LOGIN_EMAIL_VERIFIED) }
        verify { telemetry.setUser("user-1", "tenant-1") }
        coVerify { pushTokenRepo.registerCurrentToken() }
    }

    @Test
    fun `email submit error surfaces message and stops loading`() = runTest {
        coEvery { loginWithEmail(any(), any()) } returns ApiResult.Error(message = "Invalid credentials")
        vm.onEmailChange("owner@websbaba.in")
        vm.onPasswordChange("wrong")

        vm.onSubmit()

        assertThat(vm.state.value.error).isEqualTo("Invalid credentials")
        assertThat(vm.state.value.isLoading).isFalse()
        coVerify(exactly = 0) { pushTokenRepo.registerCurrentToken() }
    }

    @Test
    fun `typing after an error clears it`() = runTest {
        coEvery { loginWithEmail(any(), any()) } returns ApiResult.Error(message = "Invalid credentials")
        vm.onEmailChange("owner@websbaba.in")
        vm.onPasswordChange("wrong")
        vm.onSubmit()

        vm.onPasswordChange("corrected")

        assertThat(vm.state.value.error).isNull()
    }

    // --- Phone-OTP mode ---

    @Test
    fun `onPhoneChange filters non-digits and caps at 10 digits`() = runTest {
        vm.onPhoneChange("98a76b5432109999")
        assertThat(vm.state.value.phone).isEqualTo("9876543210")
        assertThat(vm.state.value.isPhoneValid).isTrue()
    }

    @Test
    fun `phone submit with short number sets validation error without calling use case`() = runTest {
        vm.onToggleMode(isEmailMode = false)
        vm.onPhoneChange("98765")

        vm.onSubmit()

        assertThat(vm.state.value.error).isEqualTo("Enter a valid 10-digit WhatsApp number")
        coVerify(exactly = 0) { requestOtp(any()) }
    }

    @Test
    fun `phone submit success emits NavigateToOtp`() = runTest {
        coEvery { requestOtp("9876543210") } returns ApiResult.Success(Unit)
        vm.onToggleMode(isEmailMode = false)
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
    fun `phone submit error sets error state`() = runTest {
        coEvery { requestOtp(any()) } returns ApiResult.Error(message = "Server down")
        vm.onToggleMode(isEmailMode = false)
        vm.onPhoneChange("9876543210")

        vm.onSubmit()

        assertThat(vm.state.value.error).isEqualTo("Server down")
        assertThat(vm.state.value.isLoading).isFalse()
    }

    @Test
    fun `toggling mode clears a stale error`() = runTest {
        vm.onSubmit() // triggers email validation error

        vm.onToggleMode(isEmailMode = false)

        assertThat(vm.state.value.error).isNull()
    }
}
