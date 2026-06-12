package com.ardym.nitigrow.presentation.feature.dashboard

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ardym.nitigrow.R
import com.ardym.nitigrow.domain.model.DashboardStats
import com.ardym.nitigrow.presentation.feature.dashboard.components.DeliveryReadCard
import com.ardym.nitigrow.presentation.feature.dashboard.components.StatCard
import com.ardym.nitigrow.presentation.feature.dashboard.components.StatCardRow
import com.ardym.nitigrow.ui.theme.Fraunces
import com.ardym.nitigrow.ui.theme.NitiGrowTheme
import com.ardym.nitigrow.ui.theme.Theme
import java.text.NumberFormat
import java.time.Instant
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNotificationsClick: () -> Unit = {},
    onOpenInbox: () -> Unit = {},
    onNewBroadcast: () -> Unit = {},
    onPayLink: () -> Unit = {},
    onLeads: () -> Unit = {},
    onAddContact: () -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(containerColor = Theme.colors.paper) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            DashboardHeader(
                businessName = state.businessName,
                firstName = state.firstName,
                showUnreadDot = state.hasUnreadConversations,
                onBellClick = onNotificationsClick
            )

            val ptrState = rememberPullToRefreshState()
            PullToRefreshBox(
                isRefreshing = state.isRefreshing,
                onRefresh = viewModel::refresh,
                state = ptrState,
                indicator = {
                    PullToRefreshDefaults.Indicator(
                        state = ptrState,
                        isRefreshing = state.isRefreshing,
                        containerColor = Theme.colors.brandSoft,
                        color = Theme.colors.brand,
                        modifier = Modifier.align(Alignment.TopCenter)
                    )
                },
                modifier = Modifier.fillMaxSize()
            ) {
                val stats = state.stats
                when {
                    state.isInitialLoading -> DashboardSkeleton()
                    stats != null -> DashboardContent(
                        stats = stats,
                        error = state.error,
                        expiringWindowCount = state.expiringWindowCount,
                        onOpenInbox = onOpenInbox,
                        onNewBroadcast = onNewBroadcast,
                        onPayLink = onPayLink,
                        onLeads = onLeads,
                        onAddContact = onAddContact
                    )
                    state.error != null -> DashboardErrorState(
                        message = state.error!!,
                        onRetry = viewModel::refresh
                    )
                    else -> DashboardSkeleton()
                }
            }
        }
    }
}

// ── Header ───────────────────────────────────────────────────────────────────

@Composable
private fun DashboardHeader(
    businessName: String?,
    firstName: String?,
    showUnreadDot: Boolean,
    onBellClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 18.dp, end = 10.dp, top = 14.dp, bottom = 12.dp)
    ) {
        // Same mark the splash screen uses.
        Image(
            painter = painterResource(R.mipmap.ic_launcher_foreground),
            contentDescription = "NitiGrow logo",
            modifier = Modifier.size(40.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            if (!businessName.isNullOrBlank()) {
                Text(
                    text = businessName.uppercase(),
                    fontSize = 10.sp,
                    lineHeight = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.6.sp,
                    color = Theme.colors.muted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = firstName?.let { "Namaste, $it" } ?: "Namaste",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 21.sp,
                    letterSpacing = (-0.3).sp
                ),
                color = Theme.colors.ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Box {
            IconButton(onClick = onBellClick) {
                Icon(
                    imageVector = Icons.Filled.Notifications,
                    contentDescription = "Notifications",
                    tint = Theme.colors.ink2,
                    modifier = Modifier.size(22.dp)
                )
            }
            if (showUnreadDot) {
                // 8dp accent dot ringed in paper so it reads against the bell.
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-11).dp, y = 11.dp)
                        .size(10.dp)
                        .background(Theme.colors.paper, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Theme.colors.accent, CircleShape)
                    )
                }
            }
        }
    }
}

// ── Content ──────────────────────────────────────────────────────────────────

@Composable
private fun DashboardContent(
    stats: DashboardStats,
    error: String?,
    expiringWindowCount: Int,
    onOpenInbox: () -> Unit,
    onNewBroadcast: () -> Unit,
    onPayLink: () -> Unit,
    onLeads: () -> Unit,
    onAddContact: () -> Unit
) {
    val nf = remember { NumberFormat.getInstance(Locale("en", "IN")) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 2.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        error?.let { item { InlineErrorBanner(message = it) } }

        if (expiringWindowCount > 0) {
            item { WindowExpiryBanner(count = expiringWindowCount, onClick = onOpenInbox) }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCardRow(
                    cards = listOf(
                        { mod ->
                            StatCard(
                                label = "Messages sent",
                                value = nf.format(stats.messagesSent),
                                modifier = mod,
                                pillText = if (stats.messagesSent > 0) {
                                    "${(stats.deliveryRate * 100).toInt()}% del."
                                } else null
                            )
                        },
                        { mod ->
                            StatCard(
                                label = "Leads",
                                value = nf.format(stats.leadsTotal),
                                modifier = mod,
                                pillText = if (stats.leadsNew > 0) {
                                    "+${nf.format(stats.leadsNew)} new"
                                } else null
                            )
                        }
                    )
                )
                StatCardRow(
                    cards = listOf(
                        { mod ->
                            StatCard(
                                label = "Active campaigns",
                                value = stats.activeCampaigns.toString(),
                                modifier = mod
                            )
                        },
                        { mod ->
                            StatCard(
                                label = "Revenue · 30d",
                                value = "₹${nf.format(stats.revenueInr)}",
                                modifier = mod
                            )
                        }
                    )
                )
            }
        }

        item {
            DeliveryReadCard(
                deliveryRate = stats.deliveryRate,
                readRate = stats.readRate
            )
        }

        item {
            QuickActions(
                onNewBroadcast = onNewBroadcast,
                onPayLink = onPayLink,
                onLeads = onLeads,
                onAddContact = onAddContact
            )
        }
    }
}

// ── Window-expiry alert banner ───────────────────────────────────────────────

@Composable
private fun WindowExpiryBanner(count: Int, onClick: () -> Unit) {
    val shape = RoundedCornerShape(14.dp)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Theme.colors.turmericSoft)
            .border(1.dp, Theme.colors.turmeric.copy(alpha = 0.45f), shape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Schedule,
            contentDescription = null,
            tint = Theme.colors.turmericInk,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(
                        if (count == 1) "1 chat window expires in under 3 hours."
                        else "$count chat windows expire in under 3 hours."
                    )
                }
                append(" Reply now to keep the free session open.")
            },
            fontSize = 13.sp,
            lineHeight = 18.sp,
            color = Theme.colors.turmericInk,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = "Open inbox",
            tint = Theme.colors.turmericInk,
            modifier = Modifier.size(16.dp)
        )
    }
}

// ── Quick actions ────────────────────────────────────────────────────────────

@Composable
private fun QuickActions(
    onNewBroadcast: () -> Unit,
    onPayLink: () -> Unit,
    onLeads: () -> Unit,
    onAddContact: () -> Unit
) {
    Column {
        SectionLabel("QUICK ACTIONS")
        Spacer(Modifier.height(9.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            QuickActionCard(
                label = "Broadcast",
                iconBg = Theme.colors.brandSoft,
                onClick = onNewBroadcast,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Filled.Campaign,
                    contentDescription = null,
                    tint = Theme.colors.brand,
                    modifier = Modifier.size(19.dp)
                )
            }
            QuickActionCard(
                label = "Pay link",
                iconBg = Theme.colors.turmericSoft,
                onClick = onPayLink,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "₹",
                    fontFamily = Fraunces,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Theme.colors.turmericInk
                )
            }
            QuickActionCard(
                label = "Leads",
                iconBg = Theme.colors.accentSoft,
                onClick = onLeads,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                    contentDescription = null,
                    tint = Theme.colors.accent,
                    modifier = Modifier.size(19.dp)
                )
            }
            QuickActionCard(
                label = "Contact",
                iconBg = Theme.colors.brandSoft,
                onClick = onAddContact,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Filled.PersonAdd,
                    contentDescription = null,
                    tint = Theme.colors.brand,
                    modifier = Modifier.size(19.dp)
                )
            }
        }
    }
}

@Composable
private fun QuickActionCard(
    label: String,
    iconBg: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(14.dp)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(7.dp),
        modifier = modifier
            .clip(shape)
            .background(Theme.colors.card)
            .border(1.dp, Theme.colors.border, shape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }
        Text(
            text = label,
            fontSize = 10.5.sp,
            lineHeight = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Theme.colors.ink2,
            maxLines = 1
        )
    }
}

// ── Shared bits ──────────────────────────────────────────────────────────────

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = Theme.colors.muted
    )
}

@Composable
private fun InlineErrorBanner(message: String) {
    val shape = RoundedCornerShape(14.dp)
    Text(
        text = message,
        style = MaterialTheme.typography.bodyMedium,
        color = Theme.colors.danger,
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Theme.colors.danger.copy(alpha = 0.12f))
            .border(1.dp, Theme.colors.danger.copy(alpha = 0.25f), shape)
            .padding(horizontal = 14.dp, vertical = 12.dp)
    )
}

// ── Loading skeleton ─────────────────────────────────────────────────────────

@Composable
private fun DashboardSkeleton() {
    val pulse by rememberInfiniteTransition(label = "dashSkeleton")
        .animateFloat(
            initialValue = 1f,
            targetValue = 0.45f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 700, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "dashSkeletonAlpha"
        )

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 18.dp, end = 18.dp, top = 2.dp)
            .alpha(pulse)
    ) {
        // 2×2 KPI grid ghosts.
        repeat(2) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                repeat(2) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(84.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Theme.colors.paper3)
                    )
                }
            }
        }
        // Section label ghost.
        Box(
            modifier = Modifier
                .width(110.dp)
                .height(10.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Theme.colors.paper3)
        )
        // Quick-action ghosts.
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(4) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(88.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Theme.colors.paper2)
                )
            }
        }
    }
}

// ── Error state ──────────────────────────────────────────────────────────────

@Composable
private fun DashboardErrorState(message: String, onRetry: () -> Unit) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 40.dp, end = 40.dp, bottom = 80.dp)
    ) {
        Box(
            modifier = Modifier
                .size(110.dp)
                .clip(CircleShape)
                .background(Theme.colors.danger.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Warning,
                contentDescription = null,
                tint = Theme.colors.danger,
                modifier = Modifier.size(44.dp)
            )
        }
        Spacer(Modifier.height(20.dp))
        Text(
            text = "Couldn't load your dashboard",
            style = MaterialTheme.typography.headlineSmall,
            color = Theme.colors.ink
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = Theme.colors.muted,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(18.dp))
        Text(
            text = "Retry",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = Theme.colors.ink,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Theme.colors.card)
                .border(1.dp, Theme.colors.border, RoundedCornerShape(12.dp))
                .clickable(role = Role.Button, onClick = onRetry)
                .padding(horizontal = 26.dp, vertical = 11.dp)
        )
    }
}

// ── Preview ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, heightDp = 840)
@Composable
private fun DashboardPreview() {
    NitiGrowTheme {
        Column(modifier = Modifier.fillMaxSize().background(Theme.colors.paper)) {
            DashboardHeader(
                businessName = "Sharma Sweets & Caterers",
                firstName = "Anita",
                showUnreadDot = true,
                onBellClick = {}
            )
            DashboardContent(
                stats = DashboardStats(
                    messagesSent = 1240,
                    messagesDelivered = 1190,
                    messagesRead = 1004,
                    leadsTotal = 86,
                    leadsNew = 12,
                    activeCampaigns = 3,
                    revenueInr = 48_250,
                    deliveryRate = 0.96f,
                    readRate = 0.81f,
                    updatedAt = Instant.now()
                ),
                error = null,
                expiringWindowCount = 2,
                onOpenInbox = {},
                onNewBroadcast = {},
                onPayLink = {},
                onLeads = {},
                onAddContact = {}
            )
        }
    }
}
