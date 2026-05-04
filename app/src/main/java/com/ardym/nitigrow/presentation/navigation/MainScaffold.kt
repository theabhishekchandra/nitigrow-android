package com.ardym.nitigrow.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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
    NavigationBar {
        MainTab.entries.forEach { tab ->
            NavigationBarItem(
                selected = current == tab,
                onClick = { onSelect(tab) },
                icon = { Icon(tab.icon, contentDescription = tab.label) },
                label = { Text(tab.label) },
                alwaysShowLabel = false
            )
        }
    }
}

@Composable
private fun SideRail(current: MainTab, onSelect: (MainTab) -> Unit) {
    NavigationRail {
        MainTab.entries.forEach { tab ->
            NavigationRailItem(
                selected = current == tab,
                onClick = { onSelect(tab) },
                icon = { Icon(tab.icon, contentDescription = tab.label) },
                label = { Text(tab.label) }
            )
        }
    }
}

@Composable
private fun TabHost(tabNav: NavHostController, rootNav: NavHostController) {
    NavHost(navController = tabNav, startDestination = MainTab.DASHBOARD.route) {
        composable(MainTab.DASHBOARD.route) { DashboardScreen() }
        composable(MainTab.INBOX.route) {
            InboxScreen(onConversationClick = { id ->
                rootNav.navigate(NavRoutes.chat(id))
            })
        }
        composable(MainTab.BROADCASTS.route) {
            CampaignsScreen(
                onCreate = { rootNav.navigate(NavRoutes.CAMPAIGN_CREATE) },
                onCampaignClick = { id -> rootNav.navigate(NavRoutes.campaignDetail(id)) }
            )
        }
        composable(MainTab.CONTACTS.route) { ContactsScreen() }
        composable(MainTab.SETTINGS.route) {
            SettingsHubScreen(
                onProfileEdit = { rootNav.navigate(NavRoutes.PROFILE_EDIT) },
                onTeam = { rootNav.navigate(NavRoutes.TEAM) },
                onBilling = { rootNav.navigate(NavRoutes.BILLING) },
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
