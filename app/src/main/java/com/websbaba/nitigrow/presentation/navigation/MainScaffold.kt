package com.websbaba.nitigrow.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationRailDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.websbaba.nitigrow.core.realtime.RealtimeClient
import com.websbaba.nitigrow.domain.usecase.inbox.ObserveConversationsUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import com.websbaba.nitigrow.presentation.RealtimeLifecycle
import com.websbaba.nitigrow.presentation.components.NotificationPermissionEffect
import com.websbaba.nitigrow.presentation.components.OfflineBanner
import androidx.compose.foundation.layout.Column
import com.websbaba.nitigrow.presentation.feature.campaigns.list.CampaignsScreen
import com.websbaba.nitigrow.presentation.feature.contacts.list.ContactsScreen
import com.websbaba.nitigrow.presentation.feature.dashboard.DashboardScreen
import com.websbaba.nitigrow.presentation.feature.inbox.list.InboxScreen
import com.websbaba.nitigrow.presentation.feature.settings.SettingsHubScreen
import com.websbaba.nitigrow.core.ui.theme.Niti
import com.websbaba.nitigrow.core.ui.theme.NitiType
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainScaffoldViewModel @Inject constructor(
    val realtime: RealtimeClient,
    observeConversations: ObserveConversationsUseCase
) : ViewModel() {
    /** Conversations with unread customer messages — badge on the Inbox tab. */
    val unreadConversations: StateFlow<Int> = observeConversations("")
        .map { list -> list.count { it.unreadCount > 0 } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)
}

@Composable
fun MainScaffold(
    rootNav: NavHostController,
    widthSizeClass: WindowWidthSizeClass,
    vm: MainScaffoldViewModel = hiltViewModel()
) {
    RealtimeLifecycle(realtime = vm.realtime)
    NotificationPermissionEffect()

    val tabNav = rememberNavController()
    val backEntry by tabNav.currentBackStackEntryAsState()
    val currentTab = MainTab.fromRoute(backEntry?.destination?.route) ?: MainTab.DASHBOARD

    val unreadConversations by vm.unreadConversations.collectAsStateWithLifecycle()

    val isCompact = widthSizeClass == WindowWidthSizeClass.Compact

    if (isCompact) {
        Scaffold(
            bottomBar = {
                BottomNav(currentTab, unreadConversations) { tab -> tabNav.navigateToTab(tab) }
            }
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                OfflineBanner()
                Box(modifier = Modifier.fillMaxSize()) { TabHost(tabNav, rootNav) }
            }
        }
    } else {
        Row(modifier = Modifier.fillMaxSize()) {
            SideRail(currentTab, unreadConversations) { tab -> tabNav.navigateToTab(tab) }
            Column(modifier = Modifier.fillMaxSize()) {
                OfflineBanner()
                Box(modifier = Modifier.fillMaxSize()) { TabHost(tabNav, rootNav) }
            }
        }
    }
}

@Composable
private fun BottomNav(current: MainTab, unread: Int, onSelect: (MainTab) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Niti.colors.navigationBar)
    ) {
        HorizontalDivider(thickness = 1.dp, color = Niti.colors.outlineVariant)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(NavigationBarDefaults.windowInsets)
                .padding(start = 4.dp, top = 12.dp, end = 4.dp, bottom = 16.dp)
                .selectableGroup()
        ) {
            MainTab.entries.forEach { tab ->
                PillTabItem(
                    tab = tab,
                    selected = current == tab,
                    badgeCount = if (tab == MainTab.INBOX) unread else 0,
                    onClick = { onSelect(tab) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun SideRail(current: MainTab, unread: Int, onSelect: (MainTab) -> Unit) {
    Row(modifier = Modifier.fillMaxHeight()) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .background(Niti.colors.navigationBar)
                .windowInsetsPadding(NavigationRailDefaults.windowInsets)
                .width(80.dp)
                .padding(horizontal = 4.dp, vertical = 12.dp)
                .selectableGroup(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MainTab.entries.forEach { tab ->
                PillTabItem(
                    tab = tab,
                    selected = current == tab,
                    badgeCount = if (tab == MainTab.INBOX) unread else 0,
                    onClick = { onSelect(tab) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        VerticalDivider(thickness = 1.dp, color = Niti.colors.outlineVariant)
    }
}

// One tab of the M3 navigation bar: a 64×32dp indicator pill (primary container
// when active) holding a 24dp icon, with a 12sp label below. Shared by the
// bottom bar and the tablet side rail so both get identical treatment.
@Composable
private fun PillTabItem(
    tab: MainTab,
    selected: Boolean,
    badgeCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = Niti.colors
    val iconTint = if (selected) colors.primaryTone.onContainer else colors.onSurfaceVariant
    val labelTint = if (selected) colors.onSurface else colors.onSurfaceVariant
    Column(
        modifier = modifier
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(16.dp))
            .selectable(selected = selected, role = Role.Tab, onClick = onClick)
            .semantics {
                if (badgeCount > 0) stateDescription = "$badgeCount unread"
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(width = 64.dp, height = 32.dp)
                .background(
                    if (selected) colors.primaryTone.container else Color.Transparent,
                    RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = tab.icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
            if (badgeCount > 0) {
                Text(
                    text = if (badgeCount > 99) "99+" else badgeCount.toString(),
                    color = colors.onBadge,
                    style = NitiType.caption.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-8).dp, y = (-2).dp)
                        .defaultMinSize(minWidth = 18.dp, minHeight = 18.dp)
                        .background(colors.badge, CircleShape)
                        .padding(horizontal = 5.dp)
                )
            }
        }
        Text(
            text = tab.label,
            color = labelTint,
            style = NitiType.caption.copy(
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
            ),
            maxLines = 1
        )
    }
}

@Composable
private fun TabHost(tabNav: NavHostController, rootNav: NavHostController) {
    NavHost(navController = tabNav, startDestination = MainTab.DASHBOARD.route) {
        composable(MainTab.DASHBOARD.route) {
            DashboardScreen(
                onOpenInbox = { tabNav.navigateToTab(MainTab.INBOX) },
                onNewBroadcast = { rootNav.navigate(NavRoutes.CAMPAIGN_CREATE) },
                onPayLink = { rootNav.navigate(NavRoutes.PAYMENT_LINK) },
                onLeads = { rootNav.navigate(NavRoutes.LEADS_KANBAN) },
                onAddContact = { tabNav.navigateToTab(MainTab.CONTACTS) }
            )
        }
        composable(MainTab.INBOX.route) {
            InboxScreen(
                onConversationClick = { id -> rootNav.navigate(NavRoutes.chat(id)) },
                onStartBroadcast = { rootNav.navigate(NavRoutes.CAMPAIGN_CREATE) }
            )
        }
        composable(MainTab.BROADCASTS.route) {
            CampaignsScreen(
                onCreate = { rootNav.navigate(NavRoutes.CAMPAIGN_CREATE) },
                onCampaignClick = { id -> rootNav.navigate(NavRoutes.campaignDetail(id)) },
                onTemplates = { rootNav.navigate(NavRoutes.TEMPLATES) },
                onAnalytics = { rootNav.navigate(NavRoutes.ANALYTICS) }
            )
        }
        composable(MainTab.CONTACTS.route) {
            ContactsScreen(onLeadsBoard = { rootNav.navigate(NavRoutes.LEADS_KANBAN) })
        }
        composable(MainTab.SETTINGS.route) {
            SettingsHubScreen(
                onProfileEdit = { rootNav.navigate(NavRoutes.PROFILE_EDIT) },
                onTeam = { rootNav.navigate(NavRoutes.TEAM) },
                onBilling = { rootNav.navigate(NavRoutes.BILLING) },
                onTemplates = { rootNav.navigate(NavRoutes.TEMPLATES) },
                onAnalytics = { rootNav.navigate(NavRoutes.ANALYTICS) },
                onPaymentLink = { rootNav.navigate(NavRoutes.PAYMENT_LINK) },
                onBusinessProfile = { rootNav.navigate(NavRoutes.SETTINGS_BUSINESS) },
                onWabaNumber = { rootNav.navigate(NavRoutes.SETTINGS_WABA) },
                onAutoReply = { rootNav.navigate(NavRoutes.SETTINGS_AUTOREPLY) },
                onAppearance = { rootNav.navigate(NavRoutes.SETTINGS_APPEARANCE) },
                onLeadsKanban = { rootNav.navigate(NavRoutes.LEADS_KANBAN) },
                onLeadsList = { rootNav.navigate(NavRoutes.LEADS) },
                onReferrals = { rootNav.navigate(NavRoutes.REFERRALS) },
                onCommerce = { rootNav.navigate(NavRoutes.COMMERCE) },
                onFlows = { rootNav.navigate(NavRoutes.FLOWS) },
                onConflicts = { rootNav.navigate(NavRoutes.CONFLICTS) },
                onLogout = {
                    rootNav.navigate(NavRoutes.GRAPH_AUTH) {
                        popUpTo(NavRoutes.GRAPH_ROOT) { inclusive = true }
                    }
                }
            )
        }
    }
}

private fun NavHostController.navigateToTab(tab: MainTab) {
    navigate(tab.route) {
        popUpTo(graph.startDestinationId) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
