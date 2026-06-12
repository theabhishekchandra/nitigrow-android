package com.ardym.nitigrow.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ardym.nitigrow.core.realtime.RealtimeClient
import com.ardym.nitigrow.presentation.RealtimeLifecycle
import com.ardym.nitigrow.presentation.components.NotificationPermissionEffect
import com.ardym.nitigrow.presentation.components.OfflineBanner
import androidx.compose.foundation.layout.Column
import com.ardym.nitigrow.presentation.feature.campaigns.list.CampaignsScreen
import com.ardym.nitigrow.presentation.feature.contacts.list.ContactsScreen
import com.ardym.nitigrow.presentation.feature.dashboard.DashboardScreen
import com.ardym.nitigrow.presentation.feature.inbox.list.InboxScreen
import com.ardym.nitigrow.presentation.feature.settings.SettingsHubScreen
import com.ardym.nitigrow.ui.theme.Theme
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainScaffoldViewModel @Inject constructor(
    val realtime: RealtimeClient
) : ViewModel()

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

    val isCompact = widthSizeClass == WindowWidthSizeClass.Compact

    if (isCompact) {
        Scaffold(
            bottomBar = { BottomNav(currentTab) { tab -> tabNav.navigateToTab(tab) } }
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                OfflineBanner()
                Box(modifier = Modifier.fillMaxSize()) { TabHost(tabNav, rootNav) }
            }
        }
    } else {
        Row(modifier = Modifier.fillMaxSize()) {
            SideRail(currentTab) { tab -> tabNav.navigateToTab(tab) }
            Column(modifier = Modifier.fillMaxSize()) {
                OfflineBanner()
                Box(modifier = Modifier.fillMaxSize()) { TabHost(tabNav, rootNav) }
            }
        }
    }
}

@Composable
private fun BottomNav(current: MainTab, onSelect: (MainTab) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Theme.colors.card)
    ) {
        HorizontalDivider(thickness = 1.dp, color = Theme.colors.border)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(NavigationBarDefaults.windowInsets)
                .padding(start = 4.dp, top = 6.dp, end = 4.dp, bottom = 8.dp)
                .selectableGroup()
        ) {
            MainTab.entries.forEach { tab ->
                PillTabItem(
                    tab = tab,
                    selected = current == tab,
                    onClick = { onSelect(tab) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun SideRail(current: MainTab, onSelect: (MainTab) -> Unit) {
    Row(modifier = Modifier.fillMaxHeight()) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .background(Theme.colors.card)
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
                    onClick = { onSelect(tab) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        VerticalDivider(thickness = 1.dp, color = Theme.colors.border)
    }
}

// One tab of the M3-pill nav: 52×28dp pill (brandSoft when active) holding a
// 20dp icon, with a 10.5sp label below. Shared by the bottom bar and the
// tablet side rail so both get identical active/inactive treatment.
@Composable
private fun PillTabItem(
    tab: MainTab,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tint = if (selected) Theme.colors.brand else Theme.colors.muted
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .selectable(selected = selected, role = Role.Tab, onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Box(
            modifier = Modifier
                .size(width = 52.dp, height = 28.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(if (selected) Theme.colors.brandSoft else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = tab.icon,
                contentDescription = tab.label,
                tint = tint,
                modifier = Modifier.size(20.dp)
            )
        }
        Text(
            text = tab.label,
            color = tint,
            fontSize = 10.5.sp,
            lineHeight = 13.sp,
            fontWeight = if (selected) FontWeight.W700 else FontWeight.W500,
            letterSpacing = 0.2.sp,
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
                onLeadsKanban = { rootNav.navigate(NavRoutes.LEADS_KANBAN) },
                onLeadsList = { rootNav.navigate(NavRoutes.LEADS) },
                onReferrals = { rootNav.navigate(NavRoutes.REFERRALS) },
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

@Composable
private fun Placeholder(label: String) {
    Box(modifier = Modifier.fillMaxSize()) { Text(label) }
}
