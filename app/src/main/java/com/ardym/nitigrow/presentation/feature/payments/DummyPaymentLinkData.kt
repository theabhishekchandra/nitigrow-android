package com.ardym.nitigrow.presentation.feature.payments

import java.time.Instant
import java.time.temporal.ChronoUnit

// TODO: This is dummy data we need to delete when development is complete and connect with real APIs.
//
// DummyPaymentLinkData — sample recent payment links shown on the Payments
// screen before the backend endpoint (GET /payments/links) exists. Names and
// amounts mirror DummyData.contacts() so the rest of the app stays internally
// consistent (a link to "Priya Sharma" matches the inbox conversation with
// Priya, etc.). Indian SMB spread — repair tickets at ₹250, retail invoices
// in the few-thousand bracket, wholesale at ₹40-50k.
object DummyPaymentLinkData {

    private val now: Instant get() = Instant.now()
    private fun minsAgo(m: Long) = now.minus(m, ChronoUnit.MINUTES)
    private fun hoursAgo(h: Long) = now.minus(h, ChronoUnit.HOURS)
    private fun daysAgo(d: Long) = now.minus(d, ChronoUnit.DAYS)

    // TODO: This is dummy data we need to delete when development is complete and connect with real APIs.
    fun recent(): List<SentPaymentLink> = listOf(
        SentPaymentLink(
            id = "pl-001",
            contactName = "Priya Sharma",
            amountInr = 12_000,
            status = SentLinkStatus.PAID,
            sentAt = minsAgo(18),
        ),
        SentPaymentLink(
            id = "pl-002",
            contactName = "Rahul Verma",
            amountInr = 48_500,
            status = SentLinkStatus.PENDING,
            sentAt = hoursAgo(2),
        ),
        SentPaymentLink(
            id = "pl-003",
            contactName = "Anjali Mehta",
            amountInr = 3_250,
            status = SentLinkStatus.PAID,
            sentAt = hoursAgo(5),
        ),
        SentPaymentLink(
            id = "pl-004",
            contactName = "Vikram Singh",
            amountInr = 750,
            status = SentLinkStatus.FAILED,
            sentAt = hoursAgo(9),
        ),
        SentPaymentLink(
            id = "pl-005",
            contactName = "Sneha Iyer",
            amountInr = 18_900,
            status = SentLinkStatus.PAID,
            sentAt = daysAgo(1),
        ),
        SentPaymentLink(
            id = "pl-006",
            contactName = "Rohan Kapoor",
            amountInr = 2_400,
            status = SentLinkStatus.EXPIRED,
            sentAt = daysAgo(2),
        ),
        SentPaymentLink(
            id = "pl-007",
            contactName = "Kavya Reddy",
            amountInr = 50_000,
            status = SentLinkStatus.PENDING,
            sentAt = daysAgo(3),
        ),
        SentPaymentLink(
            id = "pl-008",
            contactName = "Bhavna Joshi",
            amountInr = 1_100,
            status = SentLinkStatus.PAID,
            sentAt = daysAgo(4),
        ),
        SentPaymentLink(
            id = "pl-009",
            contactName = "Manoj Bhatia",
            amountInr = 250,
            status = SentLinkStatus.PAID,
            sentAt = daysAgo(5),
        ),
    )
}
