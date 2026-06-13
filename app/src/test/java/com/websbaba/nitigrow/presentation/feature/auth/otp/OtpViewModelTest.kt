package com.websbaba.nitigrow.presentation.feature.auth.otp

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.websbaba.nitigrow.core.network.ApiResult
import com.websbaba.nitigrow.core.storage.TokenDataStore
import com.websbaba.nitigrow.core.telemetry.Events
import com.websbaba.nitigrow.core.telemetry.Telemetry
import com.websbaba.nitigrow.domain.model.User
import com.websbaba.nitigrow.domain.model.UserRole
import com.websbaba.nitigrow.domain.repository.PushTokenRepository
import com.websbaba.nitigrow.domain.usecase.auth.RequestOtpUseCase
import com.websbaba.nitigrow.domain.usecase.auth.VerifyOtpUseCase
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OtpViewModelTest {

    private val verifyOtp: VerifyOtpUseCase = mockk()
    private val requestOtp: RequestOtpUseCase = mockk()
    private val tokenStore: TokenDataStore = mockk(relaxed = true)
    private val pushTokenRepo: PushTokenRepository = mockk(relaxed = true)
    private val telemetry: Telemetry = mockk(relaxed = true)
    private lateinit var vm: OtpViewModel

    private val user = User(
        id = "user-1",
        tenantId = "tenant-1",
        name = "Test Owner",
        email = "owner@websbaba.in",
        phone = PHONE,
        role = UserRole.OWNER
    )

    @Before fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        vm = OtpViewModel(
            savedState = SavedStateHandle(mapOf("phone" to PHONE)),
            verifyOtp = verifyOtp,
            requestOtp = requestOtp,
            tokenStore = tokenStore,
            pushTokenRepo = pushTokenRepo,
            telemetry = telemetry
        )
    }

    @After fun tearDown() { Dispatchers.resetMain() }

    // --- Initial state ---

    @Test
    fun `state seeds phone from saved state and starts the resend cooldown`() {
        assertThat(vm.state.value.phone).isEqualTo(PHONE)
        assertThat(vm.state.value.resendSeconds).isEqualTo(30)
        assertThat(vm.state.value.canResend).isFalse()
    }

    // --- Input validation / filtering ---

    @Test
    fun `onCodeChange accepts up to six digits`() = runTest {
        vm.onCodeChange("123456")
        assertThat(vm.state.value.code).isEqualTo("123456")
        assertThat(vm.state.value.canSubmit).isTrue()
    }

    @Test
    fun `onCodeChange rejects non-digit input`() = runTest {
        vm.onCodeChange("12a456")
        assertThat(vm.state.value.code).isEmpty()
    }

    @Test
    fun `onCodeChange rejects input longer than six digits`() = runTest {
        vm.onCodeChange("1234567")
        assertThat(vm.state.value.code).isEmpty()
    }

    @Test
    fun `onSubmit does nothing until six digits are entered`() = runTest {
        vm.onCodeChange("12345")

        vm.onSubmit()

        coVerify(exactly = 0) { verifyOtp(any(), any()) }
    }

    // --- Verify ---

    @Test
    fun `submit success emits NavigateToHome, tags telemetry and registers push token`() = runTest {
        coEvery { verifyOtp(PHONE, "123456") } returns ApiResult.Success(user)
        vm.onCodeChange("123456")

        vm.effects.test {
            vm.onSubmit()
            assertThat(awaitItem()).isEqualTo(OtpEffect.NavigateToHome)
        }
        assertThat(vm.state.value.isLoading).isFalse()
        verify { telemetry.event(Events.LOGIN_OTP_VERIFIED) }
        verify { telemetry.setUser("user-1", "tenant-1") }
        coVerify { pushTokenRepo.registerCurrentToken() }
    }

    @Test
    fun `submit with wrong code surfaces error and stops loading`() = runTest {
        coEvery { verifyOtp(PHONE, "000000") } returns ApiResult.Error(message = "Invalid OTP")
        vm.onCodeChange("000000")

        vm.onSubmit()

        assertThat(vm.state.value.error).isEqualTo("Invalid OTP")
        assertThat(vm.state.value.isLoading).isFalse()
        coVerify(exactly = 0) { pushTokenRepo.registerCurrentToken() }
    }

    @Test
    fun `typing after an error clears it`() = runTest {
        coEvery { verifyOtp(any(), any()) } returns ApiResult.Error(message = "Invalid OTP")
        vm.onCodeChange("000000")
        vm.onSubmit()

        vm.onCodeChange("00000")

        assertThat(vm.state.value.error).isNull()
    }

    // --- Resend ---

    @Test
    fun `onResend is blocked while the cooldown is running`() = runTest {
        vm.onResend()

        coVerify(exactly = 0) { requestOtp(any()) }
    }

    @Test
    fun `onResend after cooldown requests a new otp and restarts the timer`() = runTest {
        coEvery { requestOtp(PHONE) } returns ApiResult.Success(Unit)
        advanceTimeBy(30_000)
        runCurrent()
        assertThat(vm.state.value.canResend).isTrue()

        vm.effects.test {
            vm.onResend()
            assertThat(awaitItem()).isEqualTo(OtpEffect.ShowMessage("OTP resent"))
        }
        assertThat(vm.state.value.isLoading).isFalse()
        assertThat(vm.state.value.resendSeconds).isEqualTo(30)
        coVerify { requestOtp(PHONE) }
    }

    @Test
    fun `onResend failure surfaces error and leaves the cooldown expired`() = runTest {
        coEvery { requestOtp(PHONE) } returns ApiResult.Error(message = "Too many attempts")
        advanceTimeBy(30_000)
        runCurrent()

        vm.onResend()

        assertThat(vm.state.value.error).isEqualTo("Too many attempts")
        assertThat(vm.state.value.isLoading).isFalse()
        assertThat(vm.state.value.canResend).isTrue()
    }

    // --- Biometric opt-in ---

    @Test
    fun `enableBiometric persists the choice`() = runTest {
        vm.enableBiometric(true)

        coVerify { tokenStore.setBiometricEnabled(true) }
    }

    private companion object { const val PHONE = "9876543210" }
}
