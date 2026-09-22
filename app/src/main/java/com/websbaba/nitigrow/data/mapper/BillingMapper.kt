package com.websbaba.nitigrow.data.mapper

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.websbaba.nitigrow.data.local.entity.BillingStatusEntity
import com.websbaba.nitigrow.data.local.entity.InvoiceEntity
import com.websbaba.nitigrow.data.remote.dto.BillingStatusDto
import com.websbaba.nitigrow.data.remote.dto.InvoiceDto
import com.websbaba.nitigrow.data.remote.dto.UsageMeterDto
import com.websbaba.nitigrow.domain.model.BillingStatus
import com.websbaba.nitigrow.domain.model.Invoice
import com.websbaba.nitigrow.domain.model.SubscriptionInfo
import com.websbaba.nitigrow.domain.model.Usage
import com.websbaba.nitigrow.domain.model.UsageMeter
import java.time.Instant

private val gson = Gson()
private val pricesType = object : TypeToken<Map<String, Int>>() {}.type

// Subscription dates are ISO-8601 strings (Mongo dates). Invoice timestamps are unix seconds.
private fun UsageMeterDto?.usedOr0(): Int = this?.used ?: 0
private fun UsageMeterDto?.limitOr0(): Int = this?.limit ?: 0

fun BillingStatusDto.toEntity(): BillingStatusEntity {
    val sub = subscription
    val u = usage
    return BillingStatusEntity(
        id = 0,
        plan = plan ?: "trial",
        accountStatus = status,
        subStatus = sub?.status,
        trialEndsAtEpochMs = parseInstantOrNull(sub?.trialEndsAt),
        periodStartEpochMs = parseInstantOrNull(sub?.currentPeriodStart),
        periodEndEpochMs = parseInstantOrNull(sub?.currentPeriodEnd),
        gatewaySubscriptionId = sub?.gatewaySubscriptionId,
        cancelAtPeriodEnd = sub?.cancelAtPeriodEnd ?: false,
        billingCycle = sub?.billingCycle,
        msgUsed = u?.messages.usedOr0(),
        msgLimit = u?.messages.limitOr0(),
        aiUsed = u?.ai.usedOr0(),
        aiLimit = u?.ai.limitOr0(),
        contactsUsed = u?.contacts.usedOr0(),
        contactsLimit = u?.contacts.limitOr0(),
        usersUsed = u?.users.usedOr0(),
        usersLimit = u?.users.limitOr0(),
        pricesJson = gson.toJson(prices ?: emptyMap<String, Int>()),
        referralCreditPaise = referralCreditPaise ?: 0,
        branding = branding ?: false,
    )
}

fun BillingStatusEntity.toDomain(): BillingStatus {
    val prices: Map<String, Int> =
        runCatching { gson.fromJson<Map<String, Int>>(pricesJson, pricesType) }.getOrNull() ?: emptyMap()
    val sub = if (subStatus == null && gatewaySubscriptionId == null && trialEndsAtEpochMs == null) {
        null
    } else {
        SubscriptionInfo(
            status = subStatus,
            trialEndsAt = trialEndsAtEpochMs?.let(Instant::ofEpochMilli),
            currentPeriodStart = periodStartEpochMs?.let(Instant::ofEpochMilli),
            currentPeriodEnd = periodEndEpochMs?.let(Instant::ofEpochMilli),
            gatewaySubscriptionId = gatewaySubscriptionId,
            cancelAtPeriodEnd = cancelAtPeriodEnd,
            billingCycle = billingCycle,
        )
    }
    return BillingStatus(
        plan = plan,
        accountStatus = accountStatus,
        subscription = sub,
        usage = Usage(
            messages = UsageMeter(msgUsed, msgLimit),
            ai = UsageMeter(aiUsed, aiLimit),
            contacts = UsageMeter(contactsUsed, contactsLimit),
            users = UsageMeter(usersUsed, usersLimit),
        ),
        prices = prices,
        referralCreditPaise = referralCreditPaise,
        branding = branding,
    )
}

// Caller supplies a stable fallback id (the list index) because the invoices table
// is cleared and re-inserted on every refresh.
fun InvoiceDto.toEntity(fallbackId: String): InvoiceEntity {
    val epochSec = paidAt ?: date ?: createdAt
    return InvoiceEntity(
        id = id ?: invoiceNumber ?: receipt ?: fallbackId,
        number = invoiceNumber ?: receipt,
        status = status,
        amountPaise = amountPaid ?: amount ?: 0L,
        paidAtEpochMs = epochSec?.let { it * 1000 },
    )
}

fun InvoiceEntity.toDomain() = Invoice(
    id = id,
    number = number,
    status = status,
    amountPaise = amountPaise,
    paidAt = paidAtEpochMs?.let(Instant::ofEpochMilli),
)
