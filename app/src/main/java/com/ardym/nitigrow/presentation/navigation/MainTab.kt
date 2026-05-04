package com.ardym.nitigrow.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

enum class MainTab(val route: String, val label: String, val icon: ImageVector) {
    DASHBOARD(NavRoutes.DASHBOARD, "Home", Icons.Filled.Dashboard),
    INBOX(NavRoutes.INBOX, "Inbox", Icons.Filled.Chat),
    BROADCASTS(NavRoutes.BROADCASTS, "Broadcasts", Icons.Filled.Campaign),
    CONTACTS(NavRoutes.CONTACTS, "Contacts", Icons.Filled.Contacts),
    SETTINGS(NavRoutes.SETTINGS, "Settings", Icons.Filled.Settings);

    companion object {
        fun fromRoute(route: String?): MainTab? = entries.firstOrNull { it.route == route }
    }
}
