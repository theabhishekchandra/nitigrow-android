package com.ardym.nitigrow.presentation.dummy

import com.ardym.nitigrow.domain.model.Campaign
import com.ardym.nitigrow.domain.model.CampaignStatus
import com.ardym.nitigrow.domain.model.Contact
import com.ardym.nitigrow.domain.model.Conversation
import com.ardym.nitigrow.domain.model.DashboardStats
import com.ardym.nitigrow.domain.model.Lead
import com.ardym.nitigrow.domain.model.LeadStage
import com.ardym.nitigrow.domain.model.Message
import com.ardym.nitigrow.domain.model.MessageStatus
import com.ardym.nitigrow.domain.model.MessageType
import com.ardym.nitigrow.domain.model.PaymentRecord
import com.ardym.nitigrow.domain.model.PaymentStatus
import com.ardym.nitigrow.domain.model.Plan
import com.ardym.nitigrow.domain.model.Subscription
import com.ardym.nitigrow.domain.model.SubscriptionStatus
import com.ardym.nitigrow.domain.model.TeamMember
import com.ardym.nitigrow.domain.model.Template
import com.ardym.nitigrow.domain.model.Tenant
import com.ardym.nitigrow.domain.model.User
import com.ardym.nitigrow.domain.model.UserRole
import com.ardym.nitigrow.domain.model.WabaStatus
import java.time.Instant
import java.time.temporal.ChronoUnit

// TODO: This is dummy data we need to delete when development is complete and connect with real APIs.
// All screens seed their initial UiState from this file so the UI renders realistic content
// before the backend is wired. Every consumer site repeats the TODO comment at the call site,
// so a project-wide grep for "DummyData." returns every place that needs removal.
object DummyData {

    private val now: Instant get() = Instant.now()
    private fun minsAgo(m: Long) = now.minus(m, ChronoUnit.MINUTES)
    private fun hoursAgo(h: Long) = now.minus(h, ChronoUnit.HOURS)
    private fun daysAgo(d: Long) = now.minus(d, ChronoUnit.DAYS)

    // ── Dashboard ──────────────────────────────────────────────────────────────
    fun dashboardStats() = DashboardStats(
        messagesSent      = 12_840,
        messagesDelivered = 12_512,
        messagesRead      = 9_376,
        leadsTotal        = 248,
        leadsNew          = 32,
        activeCampaigns   = 4,
        revenueInr        = 84_500,
        deliveryRate      = 0.974f,
        readRate          = 0.749f,
        updatedAt         = minsAgo(2)
    )

    // ── Inbox / Conversations ──────────────────────────────────────────────────
    fun conversations(): List<Conversation> = listOf(
        Conversation(
            id = "c-001", contactId = "k-001",
            contactName = "Priya Sharma", contactPhone = "+91 98201 11122",
            avatarUrl = null,
            lastMessage = "Great, I'll send the catalog now. 📋",
            lastMessageAt = minsAgo(2),
            lastMessageStatus = MessageStatus.READ,
            lastMessageOutbound = true,
            unreadCount = 0, isPinned = true, isMuted = false
        ),
        Conversation(
            id = "c-002", contactId = "k-002",
            contactName = "Rahul Verma", contactPhone = "+91 98765 43210",
            avatarUrl = null,
            lastMessage = "Is the wholesale price still ₹450/kg?",
            lastMessageAt = minsAgo(9),
            lastMessageStatus = MessageStatus.DELIVERED,
            lastMessageOutbound = false,
            unreadCount = 2, isPinned = false, isMuted = false
        ),
        Conversation(
            id = "c-003", contactId = "k-003",
            contactName = "Anjali Mehta", contactPhone = "+91 99887 76655",
            avatarUrl = null,
            lastMessage = "Payment received. Thank you! 🙏",
            lastMessageAt = minsAgo(31),
            lastMessageStatus = MessageStatus.DELIVERED,
            lastMessageOutbound = false,
            unreadCount = 1, isPinned = false, isMuted = false
        ),
        Conversation(
            id = "c-004", contactId = "k-004",
            contactName = "Vikram Singh", contactPhone = "+91 91234 56789",
            avatarUrl = null,
            lastMessage = "Order #4821 shipped via Delhivery.",
            lastMessageAt = hoursAgo(2),
            lastMessageStatus = MessageStatus.READ,
            lastMessageOutbound = true,
            unreadCount = 0, isPinned = false, isMuted = false
        ),
        Conversation(
            id = "c-005", contactId = "k-005",
            contactName = "Sneha Iyer", contactPhone = "+91 98123 45678",
            avatarUrl = null,
            lastMessage = "Voice note (0:24)",
            lastMessageAt = hoursAgo(4),
            lastMessageStatus = MessageStatus.SENT,
            lastMessageOutbound = true,
            unreadCount = 0, isPinned = false, isMuted = true
        ),
        Conversation(
            id = "c-006", contactId = "k-006",
            contactName = "Rohan Kapoor", contactPhone = "+91 97540 22113",
            avatarUrl = null,
            lastMessage = "Can we schedule a demo for tomorrow?",
            lastMessageAt = hoursAgo(7),
            lastMessageStatus = MessageStatus.DELIVERED,
            lastMessageOutbound = false,
            unreadCount = 3, isPinned = false, isMuted = false
        ),
        Conversation(
            id = "c-007", contactId = "k-007",
            contactName = "Kavya Reddy", contactPhone = "+91 96543 88997",
            avatarUrl = null,
            lastMessage = "Diwali offer template approved ✓",
            lastMessageAt = daysAgo(1),
            lastMessageStatus = MessageStatus.READ,
            lastMessageOutbound = true,
            unreadCount = 0, isPinned = false, isMuted = false
        ),
        Conversation(
            id = "c-008", contactId = "k-008",
            contactName = "Arjun Nair", contactPhone = "+91 99002 31144",
            avatarUrl = null,
            lastMessage = "Send the GST invoice please.",
            lastMessageAt = daysAgo(2),
            lastMessageStatus = MessageStatus.DELIVERED,
            lastMessageOutbound = false,
            unreadCount = 0, isPinned = false, isMuted = false
        ),
    )

    // ── Chat messages for a single conversation ────────────────────────────────
    // Emitted newest-first so the chat LazyColumn (reverseLayout = true) places
    // the most recent message at the bottom of the screen — matching the Meta
    // Cloud API paging contract that returns messages in descending sentAt order.
    fun messages(conversationId: String): List<Message> = listOf(
        Message(
            id = "$conversationId-m1", conversationId = conversationId,
            text = "Hi! Are the saffron threads still in stock?",
            sentAt = hoursAgo(2), outbound = false,
            status = MessageStatus.DELIVERED, type = MessageType.TEXT
        ),
        Message(
            id = "$conversationId-m2", conversationId = conversationId,
            text = "Yes ma'am, fresh stock arrived this morning. ₹1,200 for 10g.",
            sentAt = hoursAgo(2), outbound = true,
            status = MessageStatus.READ, type = MessageType.TEXT
        ),
        Message(
            id = "$conversationId-m3", conversationId = conversationId,
            text = "Could you share a photo and the GST invoice format?",
            sentAt = hoursAgo(1), outbound = false,
            status = MessageStatus.DELIVERED, type = MessageType.TEXT
        ),
        Message(
            id = "$conversationId-m4", conversationId = conversationId,
            text = "Sharing now.",
            sentAt = minsAgo(45), outbound = true,
            status = MessageStatus.READ, type = MessageType.TEXT
        ),
        Message(
            id = "$conversationId-m5", conversationId = conversationId,
            text = "Order Confirmation — Saffron 10g × 2",
            sentAt = minsAgo(40), outbound = true,
            status = MessageStatus.READ, type = MessageType.TEMPLATE
        ),
        Message(
            id = "$conversationId-m6", conversationId = conversationId,
            text = "Perfect, please confirm UPI: priya@okhdfc",
            sentAt = minsAgo(8), outbound = false,
            status = MessageStatus.DELIVERED, type = MessageType.TEXT
        ),
        Message(
            id = "$conversationId-m7", conversationId = conversationId,
            text = "Great, I'll send the catalog now. 📋",
            sentAt = minsAgo(2), outbound = true,
            status = MessageStatus.READ, type = MessageType.TEXT
        ),
    ).sortedByDescending { it.sentAt }

    // ── Contacts ───────────────────────────────────────────────────────────────
    fun contacts(): List<Contact> = listOf(
        Contact("k-001", "Priya Sharma",  "+91 98201 11122", "priya@hindustanmart.in",   null, listOf("Lead", "Hot"),  null, daysAgo(120), minsAgo(2),  false),
        Contact("k-002", "Rahul Verma",   "+91 98765 43210", "rahul@verma-traders.com",  null, listOf("Wholesale"),    null, daysAgo(90),  minsAgo(9),  false),
        Contact("k-003", "Anjali Mehta",  "+91 99887 76655", "anjali@mehtagrocery.in",   null, listOf("Customer"),     null, daysAgo(60),  minsAgo(31), false),
        Contact("k-004", "Vikram Singh",  "+91 91234 56789", null,                       null, listOf("Customer"),     null, daysAgo(45),  hoursAgo(2), false),
        Contact("k-005", "Sneha Iyer",    "+91 98123 45678", "sneha.iyer@gmail.com",     null, listOf("VIP"),          null, daysAgo(30),  hoursAgo(4), false),
        Contact("k-006", "Rohan Kapoor",  "+91 97540 22113", "rohan@kapoorfoods.in",     null, listOf("Lead"),         null, daysAgo(14),  hoursAgo(7), false),
        Contact("k-007", "Kavya Reddy",   "+91 96543 88997", "kavya@reddyspices.com",    null, listOf("Wholesale", "VIP"), null, daysAgo(220), daysAgo(1), false),
        Contact("k-008", "Arjun Nair",    "+91 99002 31144", "arjun.nair@outlook.com",   null, listOf("Customer"),     null, daysAgo(10),  daysAgo(2), false),
        Contact("k-009", "Bhavna Joshi",  "+91 90099 88770", null,                       null, listOf("Lead", "Warm"), null, daysAgo(7),   daysAgo(3), false),
        Contact("k-010", "Manoj Bhatia",  "+91 98989 11223", "manoj@bhatiamart.com",     null, listOf("Customer"),     null, daysAgo(180), daysAgo(5), false),
        Contact("k-011", "Deepika Rao",   "+91 97121 88776", "deepika@raosaree.in",      null, listOf("Lead"),         null, daysAgo(4),   daysAgo(4), false),
        Contact("k-012", "Suresh Kumar",  "+91 96111 22330", null,                       null, listOf("Customer"),     null, daysAgo(75),  daysAgo(6), false),
    )

    // ── Templates ──────────────────────────────────────────────────────────────
    fun templates(): List<Template> = listOf(
        Template(
            id = "t-001", name = "diwali_offer_2026", language = "en", category = "MARKETING",
            status = "APPROVED",
            body = "Hi {{1}}, celebrate Diwali with {{2}}% off. Reply SHOP to order.",
            variableCount = 2, updatedAt = daysAgo(4)
        ),
        Template(
            id = "t-002", name = "festive_catalog", language = "en", category = "MARKETING",
            status = "APPROVED",
            body = "{{1}}, check our festive catalog: {{2}}",
            variableCount = 2, updatedAt = daysAgo(2)
        ),
        Template(
            id = "t-003", name = "saffron_restock", language = "hi", category = "MARKETING",
            status = "APPROVED",
            body = "नमस्ते {{1}}, केसर वापस स्टॉक में है। ऑर्डर के लिए ORDER भेजें।",
            variableCount = 1, updatedAt = daysAgo(6)
        ),
        Template(
            id = "t-004", name = "order_status", language = "en", category = "UTILITY",
            status = "APPROVED",
            body = "Order #{{1}} is now {{2}}. Track: {{3}}",
            variableCount = 3, updatedAt = daysAgo(10)
        ),
        Template(
            id = "t-005", name = "abandoned_cart", language = "en", category = "MARKETING",
            status = "PENDING",
            body = "Hi {{1}}, your cart is waiting. Complete checkout here: {{2}}",
            variableCount = 2, updatedAt = hoursAgo(20)
        ),
    )

    // ── Campaigns ──────────────────────────────────────────────────────────────
    fun campaigns(): List<Campaign> = listOf(
        Campaign(
            id = "cm-001", name = "Diwali Offer 2026", templateId = "t-001", templateName = "diwali_offer_2026",
            audienceTags = listOf("VIP", "Wholesale"), audienceSize = 1_240,
            status = CampaignStatus.RUNNING, scheduledAt = null,
            sentCount = 980, deliveredCount = 951, readCount = 712, failedCount = 7,
            createdAt = hoursAgo(3)
        ),
        Campaign(
            id = "cm-002", name = "Festive Catalog Drop", templateId = "t-002", templateName = "festive_catalog",
            audienceTags = listOf("Customer"), audienceSize = 4_512,
            status = CampaignStatus.SCHEDULED, scheduledAt = hoursAgo(-6),
            sentCount = 0, deliveredCount = 0, readCount = 0, failedCount = 0,
            createdAt = hoursAgo(8)
        ),
        Campaign(
            id = "cm-003", name = "New Stock — Saffron", templateId = "t-003", templateName = "saffron_restock",
            audienceTags = listOf("Lead"), audienceSize = 320,
            status = CampaignStatus.COMPLETED, scheduledAt = null,
            sentCount = 320, deliveredCount = 311, readCount = 256, failedCount = 9,
            createdAt = daysAgo(2)
        ),
        Campaign(
            id = "cm-004", name = "Order Status Sync", templateId = "t-004", templateName = "order_status",
            audienceTags = listOf("Customer"), audienceSize = 88,
            status = CampaignStatus.DRAFT, scheduledAt = null,
            sentCount = 0, deliveredCount = 0, readCount = 0, failedCount = 0,
            createdAt = daysAgo(1)
        ),
    )

    // ── Leads ──────────────────────────────────────────────────────────────────
    fun leads(): List<Lead> = listOf(
        Lead("l-01", "k-001", "Priya Sharma",  "+91 98201 11122", "Instagram Ad",      LeadStage.NEW,         12_000, "You",       null, daysAgo(3),  minsAgo(2)),
        Lead("l-02", "k-006", "Rohan Kapoor",  "+91 97540 22113", "Website Form",      LeadStage.NEW,          8_500, "You",       null, daysAgo(2),  hoursAgo(7)),
        Lead("l-03", "k-009", "Bhavna Joshi",  "+91 90099 88770", "Referral",          LeadStage.CONTACTED,   15_000, "Sneha",     "Sent intro deck", daysAgo(5), daysAgo(1)),
        Lead("l-04", "k-002", "Rahul Verma",   "+91 98765 43210", "Trade Show",        LeadStage.CONTACTED,   42_000, "You",       null, daysAgo(7),  daysAgo(2)),
        Lead("l-05", "k-011", "Deepika Rao",   "+91 97121 88776", "WhatsApp Inbound",  LeadStage.QUALIFIED,    9_800, "Sneha",     "Needs catalog", daysAgo(4), daysAgo(1)),
        Lead("l-06", "k-007", "Kavya Reddy",   "+91 96543 88997", "Cold Outreach",     LeadStage.PROPOSAL,   125_000, "You",       "Sent proposal v2", daysAgo(12), daysAgo(2)),
        Lead("l-07", "k-005", "Sneha Iyer",    "+91 98123 45678", "Referral",          LeadStage.WON,         34_500, "Sneha",     "Closed via UPI", daysAgo(20), daysAgo(5)),
        Lead("l-08", "k-004", "Vikram Singh",  "+91 91234 56789", "Instagram Ad",      LeadStage.WON,         18_200, "You",       null, daysAgo(28), daysAgo(8)),
        Lead("l-09", "k-008", "Arjun Nair",    "+91 99002 31144", "Website Form",      LeadStage.LOST,        25_000, "You",       "Budget mismatch", daysAgo(40), daysAgo(15)),
    )

    // ── Billing — Plans / Subscription / Payments ──────────────────────────────
    fun plans(): List<Plan> = listOf(
        Plan(
            id = "plan-starter", name = "Starter", priceInr = 999, periodDays = 30,
            features = listOf(
                "1 WhatsApp number",
                "1,000 service conversations/mo",
                "Up to 3 team members",
                "Standard templates"
            ),
            isPopular = false
        ),
        Plan(
            id = "plan-growth", name = "Growth", priceInr = 2_499, periodDays = 30,
            features = listOf(
                "1 WhatsApp number",
                "10,000 service conversations/mo",
                "Up to 10 team members",
                "Broadcasts + segmentation",
                "AI auto-reply (Phase 4)"
            ),
            isPopular = true
        ),
        Plan(
            id = "plan-scale", name = "Scale", priceInr = 6_999, periodDays = 30,
            features = listOf(
                "Multi-number support",
                "Unlimited conversations",
                "Unlimited team",
                "Priority support",
                "Custom integrations"
            ),
            isPopular = false
        ),
    )

    fun subscription() = Subscription(
        planId      = "plan-growth",
        planName    = "Growth",
        status      = SubscriptionStatus.ACTIVE,
        renewsAt    = daysAgo(-18),
        cancelledAt = null
    )

    fun payments(): List<PaymentRecord> = listOf(
        PaymentRecord("p-001", "order_001", 2_499, PaymentStatus.CAPTURED, "UPI",  daysAgo(12), "Growth"),
        PaymentRecord("p-002", "order_002", 2_499, PaymentStatus.CAPTURED, "UPI",  daysAgo(42), "Growth"),
        PaymentRecord("p-003", "order_003", 2_499, PaymentStatus.CAPTURED, "Card", daysAgo(72), "Growth"),
        PaymentRecord("p-004", "order_004",   999, PaymentStatus.CAPTURED, "UPI",  daysAgo(102), "Starter"),
        PaymentRecord("p-005", "order_005",   999, PaymentStatus.REFUNDED, "Card", daysAgo(132), "Starter"),
    )

    // ── Profile / Tenant / Team ────────────────────────────────────────────────
    fun profile() = User(
        id      = "u-001",
        tenantId = "t-ardym",
        name    = "Pankaj Jain",
        email   = "pankaj@ardym.in",
        phone   = "+91 98100 00001",
        role    = UserRole.OWNER
    )

    fun tenant() = Tenant(
        id          = "t-ardym",
        name        = "ARDYM Trading Co.",
        wabaPhone   = "+91 98100 00001",
        wabaStatus  = WabaStatus.ACTIVE,
        planName    = "Growth",
        createdAtMs = System.currentTimeMillis() - 1000L * 60 * 60 * 24 * 220
    )

    fun team(): List<TeamMember> = listOf(
        TeamMember("u-001", "Pankaj Jain",     "pankaj@ardym.in",      UserRole.OWNER, isOwner = true,  joinedAtMs = System.currentTimeMillis() - 1000L * 60 * 60 * 24 * 220),
        TeamMember("u-002", "Sneha Iyer",      "sneha@ardym.in",       UserRole.ADMIN, isOwner = false, joinedAtMs = System.currentTimeMillis() - 1000L * 60 * 60 * 24 * 90),
        TeamMember("u-003", "Karan Malhotra",  "karan@ardym.in",       UserRole.AGENT, isOwner = false, joinedAtMs = System.currentTimeMillis() - 1000L * 60 * 60 * 24 * 45),
        TeamMember("u-004", "Riya Bhatt",      "riya.bhatt@ardym.in",  UserRole.AGENT, isOwner = false, joinedAtMs = System.currentTimeMillis() - 1000L * 60 * 60 * 24 * 14),
    )
}
