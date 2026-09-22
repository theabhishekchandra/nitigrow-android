package com.websbaba.nitigrow.presentation.feature.referrals

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.websbaba.nitigrow.core.ui.theme.Niti
import com.websbaba.nitigrow.domain.model.LoyaltyProgram
import com.websbaba.nitigrow.domain.model.ReferralFunnel
import com.websbaba.nitigrow.domain.model.ReferralLeader
import com.websbaba.nitigrow.domain.model.ReferralProgram
import com.websbaba.nitigrow.domain.model.SaasReferral
import com.websbaba.nitigrow.presentation.components.ErrorBanner
import com.websbaba.nitigrow.presentation.feature.inbox.list.components.Avatar
import com.websbaba.nitigrow.presentation.feature.settings.components.NgToggle
import com.websbaba.nitigrow.presentation.feature.settings.components.SubScreenHeader
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

// ─────────────────────────────────────────────────────────────────────────────
// ReferralsScreen — Settings ▸ Refer & earn.
//
//   ‹ Refer & earn
//   ┌ espresso: YOUR CODE · ANITA50 · [Copy code][Share on WhatsApp] ┐
//   ┌ 3 BUSINESSES JOINED ┐ ┌ ₹1,500 CREDIT EARNED ┐
//   ┌ Customer referral program [toggle] ┐  (existing functionality)
//   ┌ REFERRAL FUNNEL ┐ ┌ LOYALTY ┐ ┌ TOP REFERRERS rows ┐
// ─────────────────────────────────────────────────────────────────────────────

private fun rupees(paise: Long): String =
    "₹" + NumberFormat.getIntegerInstance(Locale.ENGLISH).format(paise / 100)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReferralsScreen(
    onBack: () -> Unit,
    viewModel: ReferralsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = Niti.colors
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = colors.surface,
        topBar = { SubScreenHeader(title = "Refer & earn", onBack = onBack) },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        if (state.isLoading && state.program == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = colors.primary) }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            state.error?.let { err ->
                item { ErrorBanner(message = err) }
            }

            state.saas?.let { s ->
                item {
                    CodeCard(
                        saas = s,
                        onCopy = {
                            clipboard.setText(AnnotatedString(s.code))
                            scope.launch { snackbar.showSnackbar("Code ${s.code} copied") }
                        },
                        onShare = {
                            val msg =
                                "Join NitiGrow with my code ${s.code} and we both get credit: ${s.signupLink}"
                            val send = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, msg)
                            }
                            try {
                                context.startActivity(
                                    Intent(send).setPackage("com.whatsapp")
                                )
                            } catch (_: ActivityNotFoundException) {
                                context.startActivity(
                                    Intent.createChooser(send, "Share invite")
                                )
                            }
                        }
                    )
                }
                item { SaasStatsRow(s) }
            }

            // Customer-gets-customer program (existing functionality, restyled).
            item {
                ProgramCard(
                    program = state.program,
                    isToggling = state.isToggling,
                    onToggle = viewModel::setEnabled
                )
            }

            state.funnel?.let { f ->
                item { FunnelCard(f) }
            }

            state.loyalty?.let { l ->
                item { LoyaltyCard(l) }
            }

            if (state.leaders.isNotEmpty()) {
                item { LeadersCard(state.leaders) }
            }
        }
    }
}

// ── espresso code card ───────────────────────────────────────────────────────

@Composable
private fun CodeCard(saas: SaasReferral, onCopy: () -> Unit, onShare: () -> Unit) {
    val colors = Niti.colors
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(colors.hero)
            .padding(horizontal = 18.dp, vertical = 22.dp)
    ) {
        Text(
            "YOUR CODE",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.6.sp,
            color = colors.onHero.copy(alpha = 0.55f)
        )
        Text(
            saas.code.uppercase(Locale.ROOT),
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = 38.sp,
                lineHeight = 44.sp,
                letterSpacing = 3.sp
            ),
            color = colors.onHero,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
        Text(
            "You get ${rupees(saas.creditPerReferralPaise)} credit for every business that subscribes with your code.",
            fontSize = 12.5.sp,
            lineHeight = 19.sp,
            color = colors.onHero.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 10.dp)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(9.dp),
            modifier = Modifier.fillMaxWidth().padding(top = 18.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, colors.onHero.copy(alpha = 0.14f), RoundedCornerShape(12.dp))
                    .clickable(role = Role.Button, onClick = onCopy)
                    .padding(vertical = 12.dp)
            ) {
                Text(
                    "Copy code",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onHero
                )
            }
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.primary)
                    .clickable(role = Role.Button, onClick = onShare)
                    .padding(vertical = 12.dp)
            ) {
                Text(
                    "Share on WhatsApp",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (colors.isLight) colors.surface else colors.primaryTone.onContainer
                )
            }
        }
    }
}

@Composable
private fun SaasStatsRow(saas: SaasReferral) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        StatCard(
            value = "${saas.signedUp}",
            label = "BUSINESSES JOINED",
            modifier = Modifier.weight(1f)
        )
        StatCard(
            value = rupees(saas.creditPaise),
            label = "CREDIT EARNED",
            valueColor = Niti.colors.primary,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCard(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    valueColor: androidx.compose.ui.graphics.Color = Niti.colors.onSurface
) {
    val colors = Niti.colors
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surfaceLow)
            .border(1.dp, colors.outlineVariant, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Text(value, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = valueColor)
        Text(
            label,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.8.sp,
            color = colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

// ── customer referral program / funnel / loyalty (existing data) ────────────

@Composable
private fun ReferralCard(content: @Composable () -> Unit) {
    val colors = Niti.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surfaceLow)
            .border(1.dp, colors.outlineVariant, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) { content() }
}

@Composable
private fun ProgramCard(
    program: ReferralProgram?,
    isToggling: Boolean,
    onToggle: (Boolean) -> Unit
) {
    val colors = Niti.colors
    ReferralCard {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Customer referral program",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onSurface
                )
                Text(
                    "Customers share a code; both earn a reward",
                    fontSize = 11.5.sp,
                    color = colors.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            NgToggle(
                checked = program?.enabled == true,
                enabled = !isToggling && program != null,
                onCheckedChange = onToggle
            )
        }
        program?.let { p ->
            Text(
                "Reward: ${p.referrerReward} to referrer / ${p.refereeReward} to referee · qualifies on ${p.qualifyOn.replace('_', ' ')}",
                fontSize = 11.5.sp,
                color = colors.onSurfaceVariant,
                modifier = Modifier.padding(top = 10.dp)
            )
        }
    }
}

@Composable
private fun FunnelCard(f: ReferralFunnel) {
    val colors = Niti.colors
    ReferralCard {
        Text(
            "REFERRAL FUNNEL",
            style = MaterialTheme.typography.labelSmall,
            color = colors.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 10.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            FunnelStat("Pending", f.pending)
            FunnelStat("Qualified", f.qualified)
            FunnelStat("Rewarded", f.rewarded)
            FunnelStat("Expired", f.expired)
        }
    }
}

@Composable
private fun FunnelStat(label: String, value: Int) {
    val colors = Niti.colors
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("$value", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = colors.onSurface)
        Text(label, fontSize = 10.5.sp, color = colors.onSurfaceVariant, modifier = Modifier.padding(top = 2.dp))
    }
}

@Composable
private fun LoyaltyCard(l: LoyaltyProgram) {
    val colors = Niti.colors
    ReferralCard {
        Text(
            "Loyalty program",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.onSurface
        )
        Text(
            if (l.enabled)
                "On · ${l.pointsPerRupee} pt/₹ · ${l.minRedeemPoints} pts to redeem"
            else "Off",
            fontSize = 11.5.sp,
            color = colors.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

// ── top referrers ────────────────────────────────────────────────────────────

@Composable
private fun LeadersCard(leaders: List<ReferralLeader>) {
    val colors = Niti.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surfaceLow)
            .border(1.dp, colors.outlineVariant, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Text(
            "TOP REFERRERS",
            style = MaterialTheme.typography.labelSmall,
            color = colors.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
        )
        leaders.forEachIndexed { i, leader ->
            if (i > 0) HorizontalDivider(color = colors.outlineVariant)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
            ) {
                Avatar(name = leader.name ?: "?", url = null, sizeDp = 36)
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        leader.name ?: "—",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.onSurface,
                        maxLines = 1
                    )
                    leader.phone?.let {
                        Text(it, fontSize = 11.sp, color = colors.onSurfaceVariant, maxLines = 1)
                    }
                }
                Text(
                    "${leader.rewarded} rewarded",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.primary
                )
            }
        }
    }
}
