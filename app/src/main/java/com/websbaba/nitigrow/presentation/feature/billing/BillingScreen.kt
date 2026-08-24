package com.websbaba.nitigrow.presentation.feature.billing

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.websbaba.nitigrow.domain.model.BillingStatus
import com.websbaba.nitigrow.domain.model.Invoice
import com.websbaba.nitigrow.domain.model.UsageMeter
import com.websbaba.nitigrow.ui.theme.Theme
import java.text.NumberFormat
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val nf = NumberFormat.getInstance(Locale("en", "IN"))
private val dateFmt =
    DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH).withZone(ZoneId.systemDefault())

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillingScreen(
    onBack: () -> Unit,
    viewModel: BillingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var confirmCancel by remember { mutableStateOf(false) }
    val colors = Theme.colors

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbar.showSnackbar(it)
            viewModel.consumeMessage()
        }
    }

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
                    contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 8.dp, bottom = 28.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    state.status?.let { st ->
                        item { CurrentPlanCard(status = st) }
                        item { UsageCard(status = st) }
                    }

                    state.error?.let { err -> item { BannerCard(message = err, danger = true) } }

                    item {
                        BannerCard(
                            message = "To upgrade or change your plan, manage your subscription on the NitiGrow web dashboard.",
                            danger = false,
                        )
                    }

                    if (state.invoices.isNotEmpty()) {
                        item { InvoicesCard(invoices = state.invoices) }
                    }

                    val sub = state.status?.subscription
                    if (sub?.isActive == true) {
                        item {
                            if (sub.cancelAtPeriodEnd) {
                                Text(
                                    text = "Your plan is set to cancel at the end of the current period.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.muted,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp),
                                )
                            } else {
                                CancelRow(
                                    enabled = !state.isWorking,
                                    onCancel = { confirmCancel = true },
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (confirmCancel) {
        AlertDialog(
            onDismissRequest = { confirmCancel = false },
            title = { Text("Cancel subscription?") },
            text = {
                Text(
                    "Your plan stays active until the end of the current billing period, then it " +
                        "will not renew. You can re-subscribe anytime on the web."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmCancel = false
                    viewModel.cancel()
                }) { Text("Cancel plan", color = colors.danger) }
            },
            dismissButton = {
                TextButton(onClick = { confirmCancel = false }) { Text("Keep plan") }
            },
        )
    }
}

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
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = colors.ink)
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

@Composable
private fun CurrentPlanCard(status: BillingStatus) {
    val colors = Theme.colors
    val price = status.prices[status.plan] ?: 0
    val sub = status.subscription
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(colors.sidebarBg)
            .padding(18.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "${status.planLabel.uppercase()} PLAN",
                style = MaterialTheme.typography.labelSmall,
                color = colors.sidebarTextActive,
                modifier = Modifier.weight(1f),
            )
            StatusPill(text = (sub?.status ?: status.accountStatus ?: "—").uppercase())
        }

        if (price > 0) {
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(top = 10.dp),
            ) {
                Text(
                    text = "₹${nf.format(price)}",
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 34.sp, lineHeight = 38.sp),
                    color = colors.sidebarInk,
                )
                Text(
                    text = if (sub?.billingCycle == "annual") "/ year" else "/ month",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.sidebarInk.copy(alpha = 0.55f),
                    modifier = Modifier.padding(bottom = 6.dp),
                )
            }
        }

        val periodLine = when {
            sub?.currentPeriodEnd != null && sub.cancelAtPeriodEnd ->
                "Cancels ${dateFmt.format(sub.currentPeriodEnd)}"
            sub?.currentPeriodEnd != null -> "Renews ${dateFmt.format(sub.currentPeriodEnd)}"
            sub?.trialEndsAt != null -> "Trial ends ${dateFmt.format(sub.trialEndsAt)}"
            else -> null
        }
        periodLine?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = colors.sidebarInk.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

@Composable
private fun StatusPill(text: String) {
    val colors = Theme.colors
    val active = text == "ACTIVE" || text == "TRIAL"
    val bg = if (active) colors.turmericSoft else colors.sidebarInk.copy(alpha = 0.14f)
    val fg = if (active) colors.turmericInk else colors.sidebarInk.copy(alpha = 0.7f)
    Text(
        text = text,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color = fg,
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 3.dp),
    )
}

@Composable
private fun UsageCard(status: BillingStatus) {
    val colors = Theme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.card)
            .border(1.dp, colors.border, RoundedCornerShape(16.dp))
            .padding(16.dp),
    ) {
        Text(
            text = "THIS MONTH'S USAGE",
            style = MaterialTheme.typography.labelSmall,
            color = colors.muted,
            modifier = Modifier.padding(bottom = 12.dp),
        )
        UsageMeterRow("Conversations", status.usage.messages)
        Spacer(Modifier.height(12.dp))
        UsageMeterRow("AI replies", status.usage.ai)
        Spacer(Modifier.height(12.dp))
        UsageMeterRow("Contacts", status.usage.contacts)
        Spacer(Modifier.height(12.dp))
        UsageMeterRow("Team seats", status.usage.users)
    }
}

@Composable
private fun UsageMeterRow(label: String, meter: UsageMeter) {
    val colors = Theme.colors
    val valueText = if (meter.isUnlimited) {
        "${nf.format(meter.used)} / Unlimited"
    } else {
        "${nf.format(meter.used)} / ${nf.format(meter.limit)}"
    }
    val over = !meter.isUnlimited && meter.limit > 0 && meter.used >= meter.limit
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.ink,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = valueText,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (over) colors.danger else colors.muted,
            )
        }
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(colors.border2),
        ) {
            if (meter.fraction > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(meter.fraction)
                        .height(6.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(if (over) colors.danger else colors.brand),
                )
            }
        }
    }
}

@Composable
private fun InvoicesCard(invoices: List<Invoice>) {
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
            modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
        )
        invoices.forEach { inv ->
            HorizontalDivider(thickness = 1.dp, color = colors.border2)
            InvoiceRow(inv)
        }
    }
}

@Composable
private fun InvoiceRow(inv: Invoice) {
    val colors = Theme.colors
    val rupees = inv.amountPaise / 100
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = inv.paidAt?.let { dateFmt.format(it) } ?: (inv.number ?: "Invoice"),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = colors.ink,
            )
            inv.number?.let {
                Text(text = it, fontSize = 11.sp, color = colors.muted)
            }
        }
        Text(
            text = "₹${nf.format(rupees)}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = colors.ink,
        )
        StatusPill(text = (inv.status ?: "PAID").uppercase())
    }
}

@Composable
private fun CancelRow(enabled: Boolean, onCancel: () -> Unit) {
    val colors = Theme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, colors.danger.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .then(if (enabled) Modifier else Modifier)
            .padding(vertical = 11.dp),
        contentAlignment = Alignment.Center,
    ) {
        TextButton(onClick = onCancel, enabled = enabled) {
            Text("Cancel subscription", color = colors.danger, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun BannerCard(message: String, danger: Boolean) {
    val colors = Theme.colors
    Text(
        text = message,
        style = MaterialTheme.typography.bodySmall,
        color = if (danger) colors.danger else colors.muted,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (danger) colors.danger.copy(alpha = 0.12f) else colors.card)
            .border(1.dp, if (danger) colors.danger.copy(alpha = 0.2f) else colors.border, RoundedCornerShape(12.dp))
            .padding(12.dp),
    )
}
