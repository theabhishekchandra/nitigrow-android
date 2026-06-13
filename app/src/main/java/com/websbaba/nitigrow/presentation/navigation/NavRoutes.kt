package com.websbaba.nitigrow.presentation.navigation

/**
 * Centralized routes. String-based for now; migrate to type-safe routes
 * (Navigation Compose 2.8 @Serializable) when feature graphs settle.
 */
object NavRoutes {
    // top-level graphs
    const val GRAPH_ROOT = "root"
    const val GRAPH_AUTH = "graph_auth"
    const val GRAPH_MAIN = "graph_main"

    // screens
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"

    // auth
    const val LOGIN = "login"
    const val OTP = "otp/{phone}"
    fun otp(phone: String) = "otp/$phone"
    const val FORGOT_PASSWORD = "forgot"

    // main bottom-nav
    const val DASHBOARD = "dashboard"
    const val INBOX = "inbox"
    const val CONTACTS = "contacts"
    const val BROADCASTS = "broadcasts"
    const val SETTINGS = "settings"

    // detail screens
    const val CHAT = "chat/{conversationId}"
    fun chat(conversationId: String) = "chat/$conversationId"

    const val LEADS = "leads"
    const val LEADS_KANBAN = "leads/kanban"

    const val CAMPAIGN_CREATE = "campaign/new"
    const val CAMPAIGN_DETAIL = "campaign/{campaignId}"
    fun campaignDetail(id: String) = "campaign/$id"

    const val BILLING = "billing"
    const val PROFILE_EDIT = "profile/edit"
    const val TEAM = "team"
    const val REFERRALS = "referrals"

    // Phase-3 round-out screens
    const val TEMPLATES = "templates"
    const val TEMPLATE_CREATE = "templates/new"
    const val ANALYTICS = "analytics"
    const val PAYMENT_LINK = "payments/link"
    const val SETTINGS_BUSINESS = "settings/business"
    const val SETTINGS_WABA = "settings/waba"
    const val SETTINGS_AUTOREPLY = "settings/autoreply"
    const val SETTINGS_APPEARANCE = "settings/appearance"
    const val CONFLICTS = "conflicts"
}
