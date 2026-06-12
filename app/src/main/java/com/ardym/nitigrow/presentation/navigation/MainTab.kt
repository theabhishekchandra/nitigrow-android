package com.ardym.nitigrow.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

enum class MainTab(val route: String, val label: String, val icon: ImageVector) {
    DASHBOARD(NavRoutes.DASHBOARD, "Home", Icons.Filled.Home),
    INBOX(NavRoutes.INBOX, "Inbox", Icons.Filled.ChatBubble),
    BROADCASTS(NavRoutes.BROADCASTS, "Broadcasts", Icons.Filled.Campaign),
    CONTACTS(NavRoutes.CONTACTS, "Contacts", Icons.Filled.People),
    SETTINGS(NavRoutes.SETTINGS, "Settings", Icons.Filled.Settings);

    companion object {
        fun fromRoute(route: String?): MainTab? = entries.firstOrNull { it.route == route }
    }
}
