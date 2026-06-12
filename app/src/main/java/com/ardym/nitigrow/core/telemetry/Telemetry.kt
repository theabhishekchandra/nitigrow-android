package com.ardym.nitigrow.core.telemetry

/**
 * Single funnel for events + crashes. Features depend on this; impls swap freely.
 */
interface Telemetry {
    fun event(name: String, params: Map<String, Any> = emptyMap())
    fun setUser(userId: String?, tenantId: String?)
    fun breadcrumb(message: String)
    fun nonFatal(t: Throwable, extras: Map<String, String> = emptyMap())
}

/** Canonical event names. Add as features need. */
object Events {
    const val LOGIN_OTP_REQUESTED = "login_otp_requested"
    const val LOGIN_OTP_VERIFIED = "login_otp_verified"
    const val LOGIN_EMAIL_VERIFIED = "login_email_verified"
    const val MESSAGE_SENT = "message_sent"
    const val CAMPAIGN_CREATED = "campaign_created"
    const val CHECKOUT_STARTED = "checkout_started"
    const val CHECKOUT_SUCCEEDED = "checkout_succeeded"
    const val CHECKOUT_FAILED = "checkout_failed"
    const val CONTACT_CREATED = "contact_created"
    const val LEAD_STAGE_MOVED = "lead_stage_moved"
    const val LOGOUT = "logout"
    const val DEEP_LINK_OPENED = "deep_link_opened"
}
