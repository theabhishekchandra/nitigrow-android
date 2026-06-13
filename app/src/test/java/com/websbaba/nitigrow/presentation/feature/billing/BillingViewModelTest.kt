package com.websbaba.nitigrow.presentation.feature.billing

import app.cash.turbine.test
import com.websbaba.nitigrow.core.network.ApiResult
import com.websbaba.nitigrow.core.payments.RazorpayBridge
import com.websbaba.nitigrow.core.payments.RazorpayResult
import com.websbaba.nitigrow.domain.model.CheckoutOrder
import com.websbaba.nitigrow.domain.model.PaymentRecord
import com.websbaba.nitigrow.domain.model.PaymentStatus
import com.websbaba.nitigrow.domain.model.Plan
import com.websbaba.nitigrow.domain.model.Subscription
import com.websbaba.nitigrow.domain.model.SubscriptionStatus
import com.websbaba.nitigrow.domain.repository.BillingRepository
import com.websbaba.nitigrow.domain.usecase.billing.ObservePaymentsUseCase
import com.websbaba.nitigrow.domain.usecase.billing.ObservePlansUseCase
import com.websbaba.nitigrow.domain.usecase.billing.ObserveSubscriptionUseCase
import com.websbaba.nitigrow.domain.usecase.billing.ReportPaymentFailureUseCase
import com.websbaba.nitigrow.domain.usecase.billing.StartCheckoutUseCase
import com.websbaba.nitigrow.domain.usecase.billing.VerifyPaymentUseCase
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class BillingViewModelTest {

    private val plansFlow = MutableStateFlow<List<Plan>>(emptyList())
    private val subscriptionFlow = MutableStateFlow<Subscription?>(null)
    private val paymentsFlow = MutableStateFlow<List<PaymentRecord>>(emptyList())
    private val razorpayEvents = MutableSharedFlow<RazorpayResult>(extraBufferCapacity = 4)

    private val observePlans: ObservePlansUseCase = mockk()
    private val observeSubscription: ObserveSubscriptionUseCase = mockk()
    private val observePayments: ObservePaymentsUseCase = mockk()
    private val repo: BillingRepository = mockk()
    private val startCheckout: StartCheckoutUseCase = mockk()
    private val verifyPayment: VerifyPaymentUseCase = mockk()
    private val reportFailure: ReportPaymentFailureUseCase = mockk()
    private val razorpay: RazorpayBridge = mockk()
    private lateinit var vm: BillingViewModel

    private val plan = Plan(
        id = "plan-growth",
        name = "Growth",
        priceInr = 999,
        periodDays = 30,
        features = listOf("Unlimited contacts"),
        isPopular = true
    )

    private val subscription = Subscription(
        planId = "plan-growth",
        planName = "Growth",
        status = SubscriptionStatus.ACTIVE,
        renewsAt = Instant.parse("2026-07-13T00:00:00Z"),
        cancelledAt = null
    )

    private val payment = PaymentRecord(
        id = "pay-rec-1",
        orderId = "order_1",
        amountInr = 999,
        status = PaymentStatus.CAPTURED,
        method = "upi",
        createdAt = Instant.parse("2026-06-01T00:00:00Z"),
        planName = "Growth"
    )

    private val order = CheckoutOrder(
        razorpayOrderId = "order_1",
        keyId = "rzp_test_key",
        amountPaise = 99_900,
        currency = "INR",
        name = "NitiGrow",
        description = "Growth plan",
        prefillEmail = "owner@websbaba.in",
        prefillContact = "9876543210"
    )

    @Before fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        every { observePlans() } returns plansFlow
        every { observeSubscription() } returns subscriptionFlow
        every { observePayments() } returns paymentsFlow
        every { razorpay.events } returns razorpayEvents
        coEvery { repo.refreshPlans() } returns ApiResult.Success(Unit)
        coEvery { repo.refreshSubscription() } returns ApiResult.Success(Unit)
        coEvery { repo.refreshPayments() } returns ApiResult.Success(Unit)
        vm = BillingViewModel(
            observePlans, observeSubscription, observePayments,
            repo, startCheckout, verifyPayment, reportFailure, razorpay
        )
    }

    @After fun tearDown() { Dispatchers.resetMain() }

    // --- Plan loading ---

    @Test
    fun `init refreshes all billing data and clears the refreshing flag`() = runTest {
        coVerify { repo.refreshPlans() }
        coVerify { repo.refreshSubscription() }
        coVerify { repo.refreshPayments() }
        assertThat(vm.state.value.isRefreshing).isFalse()
    }

    @Test
    fun `plans subscription and payments stream into state`() = runTest {
        plansFlow.value = listOf(plan)
        subscriptionFlow.value = subscription
        paymentsFlow.value = listOf(payment)

        val s = vm.state.value
        assertThat(s.plans).containsExactly(plan)
        assertThat(s.subscription).isEqualTo(subscription)
        assertThat(s.payments).containsExactly(payment)
    }

    // --- Checkout start ---

    @Test
    fun `onBuy success stores the pending order and emits LaunchCheckout`() = runTest {
        coEvery { startCheckout("plan-growth") } returns ApiResult.Success(order)

        vm.effects.test {
            vm.onBuy("plan-growth")
            assertThat(awaitItem()).isEqualTo(BillingEffect.LaunchCheckout(order))
        }
        assertThat(vm.state.value.phase).isEqualTo(CheckoutPhase.AWAITING_PAYMENT)
        assertThat(vm.state.value.pendingOrder).isEqualTo(order)
    }

    @Test
    fun `onBuy failure marks the phase FAILED without launching checkout`() = runTest {
        coEvery { startCheckout("plan-growth") } returns ApiResult.Error(message = "Could not create order")

        vm.effects.test {
            vm.onBuy("plan-growth")
            expectNoEvents()
        }
        assertThat(vm.state.value.phase).isEqualTo(CheckoutPhase.FAILED)
        assertThat(vm.state.value.error).isEqualTo("Could not create order")
    }

    // --- Razorpay payment success ---

    @Test
    fun `razorpay success verifies on server and emits PaymentSucceeded`() = runTest {
        coEvery { startCheckout("plan-growth") } returns ApiResult.Success(order)
        coEvery { verifyPayment("order_1", "pay_1", "sig_1") } returns ApiResult.Success(Unit)

        vm.effects.test {
            vm.onBuy("plan-growth")
            assertThat(awaitItem()).isEqualTo(BillingEffect.LaunchCheckout(order))

            razorpayEvents.emit(RazorpayResult.Success("pay_1", "order_1", "sig_1"))
            assertThat(awaitItem()).isEqualTo(BillingEffect.PaymentSucceeded)
        }
        assertThat(vm.state.value.phase).isEqualTo(CheckoutPhase.SUCCESS)
        assertThat(vm.state.value.pendingOrder).isNull()
    }

    @Test
    fun `razorpay success without orderId falls back to the pending order id`() = runTest {
        coEvery { startCheckout("plan-growth") } returns ApiResult.Success(order)
        coEvery { verifyPayment("order_1", "pay_1", "sig_1") } returns ApiResult.Success(Unit)
        vm.onBuy("plan-growth")

        razorpayEvents.emit(RazorpayResult.Success("pay_1", orderId = null, signature = "sig_1"))

        coVerify { verifyPayment("order_1", "pay_1", "sig_1") }
        assertThat(vm.state.value.phase).isEqualTo(CheckoutPhase.SUCCESS)
    }

    @Test
    fun `server verification failure marks FAILED and emits PaymentFailed`() = runTest {
        coEvery { startCheckout("plan-growth") } returns ApiResult.Success(order)
        coEvery { verifyPayment("order_1", "pay_1", "sig_1") } returns
            ApiResult.Error(message = "Signature mismatch")

        vm.effects.test {
            vm.onBuy("plan-growth")
            assertThat(awaitItem()).isEqualTo(BillingEffect.LaunchCheckout(order))

            razorpayEvents.emit(RazorpayResult.Success("pay_1", "order_1", "sig_1"))
            assertThat(awaitItem()).isEqualTo(BillingEffect.PaymentFailed("Signature mismatch"))
        }
        assertThat(vm.state.value.phase).isEqualTo(CheckoutPhase.FAILED)
        assertThat(vm.state.value.error).isEqualTo("Signature mismatch")
    }

    // --- Razorpay payment failure ---

    @Test
    fun `razorpay failure reports to server, clears the order and emits PaymentFailed`() = runTest {
        coEvery { startCheckout("plan-growth") } returns ApiResult.Success(order)
        coEvery { reportFailure("order_1", "Payment cancelled") } returns ApiResult.Success(Unit)

        vm.effects.test {
            vm.onBuy("plan-growth")
            assertThat(awaitItem()).isEqualTo(BillingEffect.LaunchCheckout(order))

            razorpayEvents.emit(RazorpayResult.Failure(code = 2, message = "Payment cancelled", orderId = null))
            assertThat(awaitItem()).isEqualTo(BillingEffect.PaymentFailed("Payment cancelled"))
        }
        coVerify { reportFailure("order_1", "Payment cancelled") }
        assertThat(vm.state.value.phase).isEqualTo(CheckoutPhase.FAILED)
        assertThat(vm.state.value.error).isEqualTo("Payment cancelled")
        assertThat(vm.state.value.pendingOrder).isNull()
    }

    @Test
    fun `razorpay failure without any order id skips the server report`() = runTest {
        vm.effects.test {
            razorpayEvents.emit(RazorpayResult.Failure(code = 0, message = "Network error", orderId = null))
            assertThat(awaitItem()).isEqualTo(BillingEffect.PaymentFailed("Network error"))
        }
        coVerify(exactly = 0) { reportFailure(any(), any()) }
        assertThat(vm.state.value.phase).isEqualTo(CheckoutPhase.FAILED)
    }

    // --- Phase reset ---

    @Test
    fun `resetPhase returns to IDLE and clears the error`() = runTest {
        coEvery { startCheckout("plan-growth") } returns ApiResult.Error(message = "Could not create order")
        vm.onBuy("plan-growth")

        vm.resetPhase()

        assertThat(vm.state.value.phase).isEqualTo(CheckoutPhase.IDLE)
        assertThat(vm.state.value.error).isNull()
    }
}
