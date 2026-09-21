package com.websbaba.nitigrow.presentation.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import com.websbaba.nitigrow.core.ui.theme.NitiIcons

enum class MainTab(val route: String, val label: String, val icon: ImageVector) {
    DASHBOARD(NavRoutes.DASHBOARD, "Home", NitiIcons.Home),
    INBOX(NavRoutes.INBOX, "Inbox", NitiIcons.Chat),
    BROADCASTS(NavRoutes.BROADCASTS, "Broadcasts", NitiIcons.Megaphone),
    CONTACTS(NavRoutes.CONTACTS, "Contacts", NitiIcons.Contacts),
    SETTINGS(NavRoutes.SETTINGS, "Settings", NitiIcons.Sliders);

    companion object {
        fun fromRoute(route: String?): MainTab? = entries.firstOrNull { it.route == route }
    }
}
