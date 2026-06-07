package com.ardym.nitigrow.presentation.navigation

import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.ardym.nitigrow.presentation.feature.analytics.AnalyticsScreen
import com.ardym.nitigrow.presentation.feature.auth.login.LoginScreen
import com.ardym.nitigrow.presentation.feature.auth.otp.OtpScreen
import com.ardym.nitigrow.presentation.feature.billing.BillingScreen
import com.ardym.nitigrow.presentation.feature.campaigns.create.CreateCampaignScreen
import com.ardym.nitigrow.presentation.feature.campaigns.detail.CampaignDetailScreen
import com.ardym.nitigrow.presentation.feature.conflicts.ConflictsScreen
import com.ardym.nitigrow.presentation.feature.inbox.chat.ChatScreen
import com.ardym.nitigrow.presentation.feature.leads.LeadsScreen
import com.ardym.nitigrow.presentation.feature.leads.detail.LeadDetailScreen
import com.ardym.nitigrow.presentation.feature.leads.kanban.LeadsKanbanScreen
import com.ardym.nitigrow.presentation.feature.onboarding.OnboardingScreen
import com.ardym.nitigrow.presentation.feature.referrals.ReferralsScreen
import com.ardym.nitigrow.presentation.feature.payments.PaymentLinkScreen
import com.ardym.nitigrow.presentation.feature.settings.autoreply.AutoReplyScreen
import com.ardym.nitigrow.presentation.feature.settings.business.BusinessProfileScreen
import com.ardym.nitigrow.presentation.feature.settings.profile.ProfileEditScreen
import com.ardym.nitigrow.presentation.feature.settings.team.TeamScreen
import com.ardym.nitigrow.presentation.feature.settings.waba.WabaNumberScreen
import com.ardym.nitigrow.presentation.feature.splash.SplashScreen
import com.ardym.nitigrow.presentation.feature.templates.CreateTemplateScreen
import com.ardym.nitigrow.presentation.feature.templates.TemplatesScreen

// Lead-detail route. Declared here (not in NavRoutes) so the leadId arg lives
// next to the only graph that consumes it; NavRoutes stays the source of truth
// for cross-feature routes.
private const val LEAD_DETAIL_ROUTE = "leads/detail/{leadId}"

private fun leadDetailRoute(leadId: String) = "leads/detail/$leadId"

@Composable
fun NitiGrowNavGraph(
    widthSizeClass: WindowWidthSizeClass,
    navController: NavHostController = rememberNavController(),
    startDestination: String = NavRoutes.SPLASH,
    deepLinkRoute: String? = null,
    onDeepLinkConsumed: () -> Unit = {}
) {
    LaunchedEffect(deepLinkRoute) {
        if (deepLinkRoute != null) {
            navController.navigate(deepLinkRoute)
            onDeepLinkConsumed()
        }
    }
    NavHost(
        navController = navController,
        startDestination = startDestination,
        route = NavRoutes.GRAPH_ROOT
    ) {
        composable(NavRoutes.SPLASH) {
            SplashScreen(onNavigate = { route ->
                navController.navigate(route) {
                    popUpTo(NavRoutes.SPLASH) { inclusive = true }
                }
            })
        }
        composable(NavRoutes.ONBOARDING) {
            OnboardingScreen(onFinish = {
                navController.navigate(NavRoutes.GRAPH_AUTH) {
                    popUpTo(NavRoutes.ONBOARDING) { inclusive = true }
                }
            })
        }
        authGraph(navController)
        composable(NavRoutes.GRAPH_MAIN) {
            MainScaffold(rootNav = navController, widthSizeClass = widthSizeClass)
        }
        composable(
            route = NavRoutes.CHAT,
            arguments = listOf(navArgument("conversationId") { type = NavType.StringType })
        ) {
            ChatScreen(onBack = { navController.popBackStack() })
        }
        composable(NavRoutes.LEADS) {
            LeadsScreen(onBack = { navController.popBackStack() })
        }
        composable(NavRoutes.LEADS_KANBAN) {
            LeadsKanbanScreen(
                onLeadClick = { leadId -> navController.navigate(leadDetailRoute(leadId)) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = LEAD_DETAIL_ROUTE,
            arguments = listOf(navArgument("leadId") { type = NavType.StringType })
        ) {
            LeadDetailScreen(onBack = { navController.popBackStack() })
        }
        composable(NavRoutes.CAMPAIGN_CREATE) {
            CreateCampaignScreen(
                onBack = { navController.popBackStack() },
                onCreated = { navController.popBackStack() }
            )
        }
        composable(
            route = NavRoutes.CAMPAIGN_DETAIL,
            arguments = listOf(navArgument("campaignId") { type = NavType.StringType })
        ) {
            CampaignDetailScreen(onBack = { navController.popBackStack() })
        }
        composable(NavRoutes.BILLING) {
            BillingScreen(onBack = { navController.popBackStack() })
        }
        composable(NavRoutes.PROFILE_EDIT) {
            ProfileEditScreen(onBack = { navController.popBackStack() })
        }
        composable(NavRoutes.TEAM) {
            TeamScreen(onBack = { navController.popBackStack() })
        }
        composable(NavRoutes.REFERRALS) {
            ReferralsScreen(onBack = { navController.popBackStack() })
        }

        // ── Phase-3 round-out screens ──────────────────────────────────────────
        composable(NavRoutes.TEMPLATES) {
            TemplatesScreen(
                onCreate = { navController.navigate(NavRoutes.TEMPLATE_CREATE) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(NavRoutes.TEMPLATE_CREATE) {
            CreateTemplateScreen(onBack = { navController.popBackStack() })
        }
        composable(NavRoutes.ANALYTICS) {
            AnalyticsScreen(onBack = { navController.popBackStack() })
        }
        composable(NavRoutes.PAYMENT_LINK) {
            PaymentLinkScreen(onBack = { navController.popBackStack() })
        }
        composable(NavRoutes.SETTINGS_BUSINESS) {
            BusinessProfileScreen(onBack = { navController.popBackStack() })
        }
        composable(NavRoutes.SETTINGS_WABA) {
            WabaNumberScreen(onBack = { navController.popBackStack() })
        }
        composable(NavRoutes.SETTINGS_AUTOREPLY) {
            AutoReplyScreen(onBack = { navController.popBackStack() })
        }
        composable(NavRoutes.CONFLICTS) {
            ConflictsScreen(onBack = { navController.popBackStack() })
        }
    }
}

private fun NavGraphBuilder.authGraph(nav: NavHostController) {
    navigation(startDestination = NavRoutes.LOGIN, route = NavRoutes.GRAPH_AUTH) {
        composable(NavRoutes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    nav.navigate(NavRoutes.GRAPH_MAIN) {
                        popUpTo(NavRoutes.GRAPH_AUTH) { inclusive = true }
                    }
                }
            )
        }
        composable(
            route = NavRoutes.OTP,
            arguments = listOf(navArgument("phone") { type = NavType.StringType })
        ) {
            OtpScreen(onAuthSuccess = {
                nav.navigate(NavRoutes.GRAPH_MAIN) {
                    popUpTo(NavRoutes.GRAPH_AUTH) { inclusive = true }
                }
            })
        }
    }
}
