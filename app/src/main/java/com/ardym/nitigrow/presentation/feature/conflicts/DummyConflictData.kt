package com.ardym.nitigrow.presentation.feature.conflicts

import java.time.Instant
import java.time.temporal.ChronoUnit

// TODO: This is dummy data we need to delete when development is complete and connect with real APIs.
// Until the offline-sync engine + server-side conflict detector are wired, the
// Conflicts screen seeds itself from this object so the UI is reviewable.
// A project-wide grep for "DummyConflictData." yields every call site to clean.
object DummyConflictData {

    private val now: Instant get() = Instant.now()
    private fun minsAgo(m: Long) = now.minus(m, ChronoUnit.MINUTES)
    private fun hoursAgo(h: Long) = now.minus(h, ChronoUnit.HOURS)

    // TODO: This is dummy data we need to delete when development is complete and connect with real APIs.
    fun recent(): List<SyncConflict> = listOf(
        SyncConflict(
            id = "cf-1",
            entityType = "contact",
            entityName = "Priya Sharma",
            detectedAt = hoursAgo(2),
            description = "You edited tags offline. Web also changed tags at the same time.",
            localValue = "Tags: Lead, Hot",
            serverValue = "Tags: Lead, VIP",
            rule = ConflictRule.SERVER_WINS,
        ),
        SyncConflict(
            id = "cf-2",
            entityType = "message",
            entityName = "Anand Mehta — Invoice draft",
            detectedAt = minsAgo(35),
            description = "You drafted 'Send invoice' offline. The chat was archived on web.",
            localValue = "Draft: \"Hi Anand, your invoice for ₹12,400 is attached.\"",
            serverValue = "Chat archived 28 minutes ago by Rahul.",
            rule = ConflictRule.ASK_USER,
        ),
        SyncConflict(
            id = "cf-3",
            entityType = "campaign",
            entityName = "Diwali Offer 2026",
            detectedAt = hoursAgo(6),
            description = "Campaign was renamed on web while you edited the name offline.",
            localValue = "Name: \"Diwali Bumper Sale 2026\"",
            serverValue = "Name: \"Diwali Premium Offer 2026\"",
            rule = ConflictRule.SERVER_WINS,
        ),
        SyncConflict(
            id = "cf-4",
            entityType = "contact",
            entityName = "Rohit Verma",
            detectedAt = hoursAgo(18),
            description = "You added a custom field offline. The contact was deleted on web.",
            localValue = "Added field: GST = 27AABCU9603R1ZM",
            serverValue = "Contact deleted by admin at 09:14 IST.",
            rule = ConflictRule.SERVER_WINS,
        ),
    )
}
