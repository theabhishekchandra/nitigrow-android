package com.ardym.nitigrow.presentation.feature.billing

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ardym.nitigrow.core.payments.RazorpayLauncher
import com.ardym.nitigrow.domain.model.PaymentRecord
import com.ardym.nitigrow.domain.model.PaymentStatus
import com.ardym.nitigrow.domain.model.Plan
import com.ardym.nitigrow.domain.model.Subscription
import com.ardym.nitigrow.domain.model.SubscriptionStatus
import com.ardym.nitigrow.presentation.feature.billing.components.PlanCard
import com.ardym.nitigrow.ui.theme.NitiGrowTheme
import com.ardym.nitigrow.ui.theme.Theme
import kotlinx.coroutines.flow.collectLatest
import java.text.NumberFormat
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

// ─────────────────────────────────────────────────────────────────────────────
// BillingScreen — "Billing & plan" (design: Billing screen).
//
//   ◀  Billing & plan                          ← Fraunces 21sp
//   ┌─ espresso plan card ────────────────────┐
//   │ GROWTH PLAN                     (ACTIVE)│  ← gold caps + turmeric pill
//   │ ₹1,499 / month                          │  ← Fraunces 36sp
//   │ Renews 8 Jul 2026                       │
//   │ [        Change plan        ]           │  → "Choose a plan" sheet
//   └─────────────────────────────────────────┘
//   ┌─ PAYMENT HISTORY ───────────────────────┐
//   │ June 2026 · Growth · ₹1,499      (PAID) │
//   └─────────────────────────────────────────┘
//
// The plans sheet keeps the existing Razorpay checkout flow: tapping a plan
// calls viewModel.onBuy → LaunchCheckout effect → RazorpayLauncher.
// ─────────────────────────────────────────────────────────────────────────────

private val nf = NumberFormat.getInstance(Locale("en", "IN"))
private val renewFmt = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH).withZone(ZoneId.systemDefault())
private val monthFmt = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH).withZone(ZoneId.systemDefault())

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillingScreen(
    onBack: () -> Unit,
    viewModel: BillingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val context = LocalContext.current
    var plansOpen by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { e ->
            when (e) {
                is BillingEffect.LaunchCheckout -> {
                    plansOpen = false
                    val activity = context as? Activity
                    activity?.let { RazorpayLauncher.open(it, e.order) }
                }
                BillingEffect.PaymentSucceeded -> snackbar.showSnackbar("Payment successful")
                is BillingEffect.PaymentFailed -> snackbar.showSnackbar("Payment failed: ${e.message}")
            }
        }
    }

    val colors = Theme.colors
    val busy = state.phase == CheckoutPhase.CREATING_ORDER ||
        state.phase == CheckoutPhase.AWAITING_PAYMENT ||
        state.phase == CheckoutPhase.VERIFYING

    Scaffold(
        containerColor = colors.paper,
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            BillingHeader(onBack = onBack)
            PullToRefreshBox(
                isRefreshing = state.isRefreshing,
                onRefresh = viewModel::refresh,
                modifier = Modifier.fillMaxSize(),
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 8.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    item {
                        CurrentPlanCard(
                            subscription = state.subscription,
                            plan = state.plans.firstOrNull { it.id == state.subscription?.planId },
                            onChangePlan = { plansOpen = true },
                        )
                    }

                    state.error?.let { err ->
                        item { BillingErrorBanner(message = err) }
                    }

                    if (state.payments.isNotEmpty()) {
                        item { PaymentHistoryCard(payments = state.payments) }
                    }
                }
            }
        }
    }

    if (plansOpen) {
        PlansSheet(
            state = state,
            busy = busy,
            onPick = viewModel::onBuy,
            onDismiss = { if (!busy) plansOpen = false },
        )
    }
}

// ── Header ──────────────────────────────────────────────────────────────────

@Composable
private fun BillingHeader(onBack: () -> Unit) {
    val colors = Theme.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 6.dp, end = 14.dp, top = 12.dp, bottom = 8.dp),
    ) {
        IconButton(onClick = onBack) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = colors.ink,
            )
        }
        Text(
            text = "Billing & plan",
            style = MaterialTheme.typography.headlineMedium,
            fontSize = 21.sp,
            color = colors.ink,
            modifier = Modifier.weight(1f),
        )
    }
}

// ── Espresso plan card ──────────────────────────────────────────────────────

@Composable
private fun CurrentPlanCard(
    subscription: Subscription?,
    plan: Plan?,
    onChangePlan: () -> Unit,
) {
    val colors = Theme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(colors.sidebarBg)
            .padding(18.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (subscription != null) "${subscription.planName.uppercase()} PLAN" else "NO ACTIVE PLAN",
                style = MaterialTheme.typography.labelSmall,
                color = colors.sidebarTextActive,
                modifier = Modifier.weight(1f),
            )
            subscription?.let { StatusOnEspressoPill(status = it.status) }
        }

        if (plan != null) {
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(top = 10.dp),
            ) {
                Text(
                    text = "₹${nf.format(plan.priceInr)}",
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 36.sp, lineHeight = 40.sp),
                    color = colors.sidebarInk,
                )
                Text(
                    text = if (plan.periodDays in 28..31) "/ month" else "/ ${plan.periodDays} days",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.sidebarInk.copy(alpha = 0.55f),
                    modifier = Modifier.padding(bottom = 6.dp),
                )
            }
        }

        subscription?.renewsAt?.let { renews ->
            Text(
                text = "Renews ${renewFmt.format(renews)}",
                style = MaterialTheme.typography.bodySmall,
                color = colors.sidebarInk.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 6.dp),
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, colors.sidebarInk.copy(alpha = 0.14f), RoundedCornerShape(12.dp))
                .clickable(onClick = onChangePlan)
                .padding(vertical = 11.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = if (subscription != null) "Change plan" else "Choose a plan",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = colors.sidebarInk,
            )
        }
    }
}

@Composable
private fun StatusOnEspressoPill(status: SubscriptionStatus) {
    val colors = Theme.colors
    val active = status == SubscriptionStatus.ACTIVE || status == SubscriptionStatus.GRACE
    val bg = if (active) colors.turmeric.copy(alpha = 0.14f) else colors.sidebarInk.copy(alpha = 0.14f)
    val fg = if (active) colors.sidebarTextActive else colors.sidebarInk.copy(alpha = 0.7f)
    Text(
        text = status.name,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color = fg,
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 3.dp),
    )
}

// ── Payment history (invoice-style rows) ────────────────────────────────────

@Composable
private fun PaymentHistoryCard(payments: List<PaymentRecord>) {
    val colors = Theme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.card)
            .border(1.dp, colors.border, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp),
    ) {
        Text(
            text = "PAYMENT HISTORY",
            style = MaterialTheme.typography.labelSmall,
            color = colors.muted,
            modifier = Modifier.padding(top = 12.dp, bottom = 8.dp),
        )
        payments.forEach { p ->
            HorizontalDivider(thickness = 1.dp, color = colors.border2)
            PaymentRow(p)
        }
    }
}

@Composable
private fun PaymentRow(p: PaymentRecord) {
    val colors = Theme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = monthFmt.format(p.createdAt),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = colors.ink,
            )
            Text(
                text = buildString {
                    append(p.planName ?: "Payment")
                    append(" · ₹${nf.format(p.amountInr)}")
                    p.method?.let { append(" · $it") }
                },
                fontSize = 11.sp,
                color = colors.muted,
            )
        }
        PaymentStatusPill(status = p.status)
    }
}

@Composable
private fun PaymentStatusPill(status: PaymentStatus) {
    val colors = Theme.colors
    val (bg, fg, label) = when (status) {
        PaymentStatus.CAPTURED -> Triple(colors.brandSoft, colors.brand, "PAID")
        PaymentStatus.FAILED -> Triple(colors.danger.copy(alpha = 0.12f), colors.danger, "FAILED")
        PaymentStatus.REFUNDED -> Triple(colors.paper2, colors.muted, "REFUNDED")
        PaymentStatus.CREATED, PaymentStatus.AUTHORIZED ->
            Triple(colors.turmericSoft, colors.turmericInk, "PENDING")
    }
    Text(
        text = label,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color = fg,
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(bg)
            .padding(horizontal = 9.dp, vertical = 3.dp),
    )
}

// ── Plans sheet ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlansSheet(
    state: BillingUiState,
    busy: Boolean,
    onPick: (planId: String) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = Theme.colors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.paper,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = { SheetDragHandle() },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 24.dp),
        ) {
            Text(
                text = "Choose a plan",
                style = MaterialTheme.typography.headlineMedium,
                fontSize = 19.sp,
                color = colors.ink,
                modifier = Modifier.padding(bottom = 14.dp),
            )
            if (state.plans.isEmpty()) {
                Text(
                    text = "No plans available right now — pull to refresh and try again.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.muted,
                    modifier = Modifier.padding(vertical = 24.dp),
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    state.plans.forEach { plan ->
                        PlanCard(
                            plan = plan,
                            isCurrent = state.subscription?.planId == plan.id,
                            enabled = !busy,
                            onClick = { onPick(plan.id) },
                        )
                    }
                }
            }
            if (busy) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 14.dp),
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = colors.brand,
                    )
                    Text(
                        text = "Opening checkout…",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.muted,
                    )
                }
            }
        }
    }
}

@Composable
private fun SheetDragHandle() {
    Box(
        modifier = Modifier
            .padding(top = 12.dp, bottom = 4.dp)
            .width(38.dp)
            .height(4.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(Theme.colors.muted3)
    )
}

// ── Error banner ────────────────────────────────────────────────────────────

@Composable
private fun BillingErrorBanner(message: String) {
    val colors = Theme.colors
    Text(
        text = message,
        style = MaterialTheme.typography.bodySmall,
        color = colors.danger,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.danger.copy(alpha = 0.12f))
            .padding(12.dp),
    )
}

// ── Previews ────────────────────────────────────────────────────────────────

private val previewPlan = Plan(
    id = "growth", name = "Growth", priceInr = 1_499, periodDays = 30,
    features = listOf("2,500 conversations", "5 seats", "leads + analytics"),
    isPopular = true,
)

@Preview(showBackground = true, name = "CurrentPlanCard — active")
@Composable
private fun PreviewCurrentPlanCard() {
    NitiGrowTheme {
        CurrentPlanCard(
            subscription = Subscription(
                planId = "growth",
                planName = "Growth",
                status = SubscriptionStatus.ACTIVE,
                renewsAt = java.time.Instant.now().plusSeconds(60L * 60 * 24 * 26),
                cancelledAt = null,
            ),
            plan = previewPlan,
            onChangePlan = {},
        )
    }
}

@Preview(showBackground = true, name = "PaymentHistoryCard")
@Composable
private fun PreviewPaymentHistoryCard() {
    NitiGrowTheme {
        PaymentHistoryCard(
            payments = listOf(
                PaymentRecord(
                    id = "pay1", orderId = "ord1", amountInr = 1_499,
                    status = PaymentStatus.CAPTURED, method = "upi",
                    createdAt = java.time.Instant.now(), planName = "Growth",
                ),
                PaymentRecord(
                    id = "pay2", orderId = "ord2", amountInr = 999,
                    status = PaymentStatus.REFUNDED, method = null,
                    createdAt = java.time.Instant.now().minusSeconds(60L * 60 * 24 * 31),
                    planName = "Starter",
                ),
            )
        )
    }
}
