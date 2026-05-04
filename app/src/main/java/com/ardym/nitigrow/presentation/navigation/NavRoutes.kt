package com.ardym.nitigrow.presentation.navigation

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

    const val CAMPAIGN_CREATE = "campaign/new"
    const val CAMPAIGN_DETAIL = "campaign/{campaignId}"
    fun campaignDetail(id: String) = "campaign/$id"

    const val BILLING = "billing"
    const val PROFILE_EDIT = "profile/edit"
    const val TEAM = "team"
}
