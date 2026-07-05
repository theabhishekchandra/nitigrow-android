package com.websbaba.nitigrow

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.websbaba.nitigrow.presentation.navigation.NavRoutes
import com.websbaba.nitigrow.presentation.navigation.NitiGrowNavGraph
import com.websbaba.nitigrow.presentation.theme.ThemeViewModel
import com.websbaba.nitigrow.ui.theme.NitiGrowTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    // Activity-scoped so one instance drives NitiGrowTheme for the whole
    // composition; it resolves the persisted AppTheme gated by the plan tier.
    private val themeViewModel: ThemeViewModel by viewModels()

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val appTheme by themeViewModel.appTheme.collectAsStateWithLifecycle()
            NitiGrowTheme(appTheme = appTheme) {
                val windowSize = calculateWindowSizeClass(this)
                var pendingDeepLink by remember { mutableStateOf(parseDeepLink(intent)) }

                LaunchedEffect(Unit) {
                    addOnNewIntentListener { newIntent ->
                        pendingDeepLink = parseDeepLink(newIntent)
                    }
                }

                NitiGrowNavGraph(
                    widthSizeClass = windowSize.widthSizeClass,
                    deepLinkRoute = pendingDeepLink,
                    onDeepLinkConsumed = { pendingDeepLink = null }
                )
            }
        }
    }

    private fun parseDeepLink(intent: Intent?): String? {
        val uri = intent?.data ?: return null
        val segs = uri.pathSegments
        return when {
            // nitigrow://chat/<id> or nitigrow://campaign/<id>
            uri.scheme == "nitigrow" -> when (uri.host) {
                "chat" -> segs.firstOrNull()?.let { NavRoutes.chat(it) }
                "campaign" -> segs.firstOrNull()?.let { NavRoutes.campaignDetail(it) }
                else -> null
            }
            // https://app.nitigrow.in/chat/<id> or /campaign/<id>
            uri.scheme == "https" && uri.host == "app.nitigrow.in" -> {
                val type = segs.getOrNull(0)
                val id = segs.getOrNull(1)
                when {
                    type == "chat" && id != null -> NavRoutes.chat(id)
                    type == "campaign" && id != null -> NavRoutes.campaignDetail(id)
                    else -> null
                }
            }
            else -> null
        }
    }
}
