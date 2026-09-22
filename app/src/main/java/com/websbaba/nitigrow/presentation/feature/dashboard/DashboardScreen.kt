package com.websbaba.nitigrow.presentation.feature.dashboard

import com.websbaba.nitigrow.core.util.formatIndian
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.websbaba.nitigrow.domain.model.DashboardStats
import com.websbaba.nitigrow.presentation.feature.dashboard.components.EngagementCard
import com.websbaba.nitigrow.presentation.feature.dashboard.components.HomeGreeting
import com.websbaba.nitigrow.presentation.feature.dashboard.components.HomeHeader
import com.websbaba.nitigrow.presentation.feature.dashboard.components.QuickActionRow
import com.websbaba.nitigrow.presentation.feature.dashboard.components.RevenueHero
import com.websbaba.nitigrow.presentation.feature.dashboard.components.SnapshotTiles
import com.websbaba.nitigrow.presentation.feature.dashboard.components.SummaryCard
import com.websbaba.nitigrow.presentation.feature.dashboard.components.SummaryRow
import com.websbaba.nitigrow.core.ui.theme.Niti
import com.websbaba.nitigrow.core.ui.theme.NitiGrowTheme
import com.websbaba.nitigrow.core.ui.theme.NitiIcons
import com.websbaba.nitigrow.core.ui.theme.NitiStatusBar
import com.websbaba.nitigrow.core.ui.theme.NitiType
import java.time.Instant
import kotlin.math.roundToInt

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
    val colors = Niti.colors

    // Header sits flush on the page colour, so the status bar matches it.
    NitiStatusBar(color = colors.surface, darkIcons = colors.isLight)

    Column(modifier = Modifier.fillMaxSize().background(colors.surface)) {
        HomeHeader(
            businessName = state.businessName,
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
                    containerColor = colors.primaryTone.container,
                    color = colors.primary,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            },
            modifier = Modifier.fillMaxSize()
        ) {
            val stats = state.stats
            when {
                state.isInitialLoading -> DashboardSkeleton(state.firstName, state.pendingReplies)
                stats != null -> DashboardContent(
                    stats = stats,
                    firstName = state.firstName,
                    error = state.error,
                    expiringWindowCount = state.expiringWindowCount,
                    pendingReplies = state.pendingReplies,
                    conversationsToday = state.conversationsToday,
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
                else -> DashboardSkeleton(state.firstName, state.pendingReplies)
            }
        }
    }
}

// ── Content ──────────────────────────────────────────────────────────────────

@Composable
private fun DashboardContent(
    stats: DashboardStats,
    firstName: String?,
    error: String?,
    expiringWindowCount: Int,
    pendingReplies: Int,
    conversationsToday: Int,
    onOpenInbox: () -> Unit,
    onNewBroadcast: () -> Unit,
    onPayLink: () -> Unit,
    onLeads: () -> Unit,
    onAddContact: () -> Unit
) {
    val colors = Niti.colors

    val summaryRows = listOf(
        SummaryRow(
            title = "Messages sent",
            subtitle = if (stats.messagesSent > 0) {
                "${(stats.deliveryRate * 100).roundToInt()}% delivered"
            } else "No messages yet",
            value = formatIndian(stats.messagesSent),
            icon = NitiIcons.Send,
            tone = colors.primaryTone
        ),
        SummaryRow(
            title = "Leads",
            subtitle = if (stats.leadsNew > 0) "${formatIndian(stats.leadsNew)} new" else "No new leads",
            value = formatIndian(stats.leadsTotal),
            icon = NitiIcons.Leads,
            tone = colors.tertiaryTone
        ),
        SummaryRow(
            title = "Active broadcasts",
            subtitle = if (stats.activeCampaigns > 0) "Sending now" else "None running",
            value = stats.activeCampaigns.toString(),
            icon = NitiIcons.Megaphone,
            tone = colors.secondaryTone
        )
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item { HomeGreeting(firstName = firstName, pendingReplies = pendingReplies) }

        error?.let { item { InlineErrorBanner(message = it) } }

        if (expiringWindowCount > 0) {
            item { WindowExpiryBanner(count = expiringWindowCount, onClick = onOpenInbox) }
        }

        item {
            QuickActionRow(
                onBroadcast = onNewBroadcast,
                onContact = onAddContact,
                onLeads = onLeads,
                onPayLink = onPayLink
            )
        }
        item {
            RevenueHero(
                amount = "₹${formatIndian(stats.revenueInr)}",
                onSendPaymentLink = onPayLink
            )
        }
        item {
            SnapshotTiles(
                conversationsToday = formatIndian(conversationsToday),
                pendingReplies = formatIndian(pendingReplies),
                onOpenInbox = onOpenInbox
            )
        }
        item { EngagementCard(deliveryRate = stats.deliveryRate, readRate = stats.readRate) }
        item { SummaryCard(rows = summaryRows) }
    }
}

// ── Banners ──────────────────────────────────────────────────────────────────

/** 24h-window alert — taps through to the inbox. */
@Composable
private fun WindowExpiryBanner(count: Int, onClick: () -> Unit) {
    val tone = Niti.colors.secondaryTone
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp, top = 12.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(tone.container)
            .clickable(role = Role.Button, onClick = onClick)
            .heightIn(min = 48.dp)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Icon(
            imageVector = NitiIcons.Clock,
            contentDescription = null,
            tint = tone.onContainer,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
                    append(
                        if (count == 1) "1 chat window expires in under 3 hours."
                        else "$count chat windows expire in under 3 hours."
                    )
                }
                append(" Reply now to keep the free session open.")
            },
            style = NitiType.label,
            color = tone.onContainer,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = "Open inbox",
            tint = tone.onContainer,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun InlineErrorBanner(message: String) {
    val colors = Niti.colors
    Text(
        text = message,
        style = NitiType.label,
        color = colors.error,
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp, top = 12.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(colors.error.copy(alpha = 0.12f))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

// ── Loading skeleton ─────────────────────────────────────────────────────────

@Composable
private fun DashboardSkeleton(firstName: String?, pendingReplies: Int) {
    val colors = Niti.colors
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

    Column(modifier = Modifier.fillMaxSize()) {
        HomeGreeting(firstName = firstName, pendingReplies = pendingReplies)
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp)
                .alpha(pulse)
        ) {
            Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                repeat(4) {
                    Box(
                        modifier = Modifier
                            .size(width = 64.dp, height = 56.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(colors.surfaceContainer)
                    )
                }
            }
            SkeletonBlock(height = 150.dp, radius = 28.dp)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SkeletonBlock(height = 110.dp, radius = 24.dp, modifier = Modifier.weight(1f))
                SkeletonBlock(height = 110.dp, radius = 24.dp, modifier = Modifier.weight(1f))
            }
            SkeletonBlock(height = 190.dp, radius = 24.dp)
        }
    }
}

@Composable
private fun SkeletonBlock(
    height: Dp,
    radius: Dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(radius))
            .background(Niti.colors.surfaceContainer)
    )
}

// ── Error state ──────────────────────────────────────────────────────────────

@Composable
private fun DashboardErrorState(message: String, onRetry: () -> Unit) {
    val colors = Niti.colors
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 40.dp, end = 40.dp, bottom = 80.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(110.dp)
                .background(colors.tertiaryTone.container, CircleShape)
        ) {
            Icon(
                imageVector = NitiIcons.Warning,
                contentDescription = null,
                tint = colors.tertiaryTone.onContainer,
                modifier = Modifier.size(44.dp)
            )
        }
        Spacer(Modifier.height(20.dp))
        Text(
            text = "Couldn't load your dashboard",
            style = NitiType.title,
            color = colors.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = message,
            style = NitiType.body,
            color = colors.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(20.dp))
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .heightIn(min = 48.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(colors.primary)
                .clickable(role = Role.Button, onClick = onRetry)
                .padding(horizontal = 28.dp)
        ) {
            Text(
                text = "Retry",
                style = NitiType.label.copy(fontWeight = FontWeight.SemiBold),
                color = colors.onPrimary
            )
        }
    }
}

// ── Previews ─────────────────────────────────────────────────────────────────

private val PreviewStats = DashboardStats(
    messagesSent = 12_480,
    messagesDelivered = 11_980,
    messagesRead = 8_860,
    leadsTotal = 148,
    leadsNew = 12,
    activeCampaigns = 3,
    revenueInr = 248_600,
    deliveryRate = 0.96f,
    readRate = 0.71f,
    updatedAt = Instant.now()
)

@Composable
private fun HomePreviewBody() {
    Column(modifier = Modifier.fillMaxSize().background(Niti.colors.surface)) {
        HomeHeader(businessName = "Sharma Sweets & Caterers", showUnreadDot = true, onBellClick = {})
        DashboardContent(
            stats = PreviewStats,
            firstName = "Anita",
            error = null,
            expiringWindowCount = 2,
            pendingReplies = 9,
            conversationsToday = 46,
            onOpenInbox = {},
            onNewBroadcast = {},
            onPayLink = {},
            onLeads = {},
            onAddContact = {}
        )
    }
}

@Preview(name = "Home · light", showBackground = true, heightDp = 1240, widthDp = 390)
@Composable
private fun HomeLightPreview() {
    NitiGrowTheme(darkTheme = false) { HomePreviewBody() }
}

@Preview(name = "Home · dark", showBackground = true, heightDp = 1240, widthDp = 390)
@Composable
private fun HomeDarkPreview() {
    NitiGrowTheme(darkTheme = true) { HomePreviewBody() }
}
