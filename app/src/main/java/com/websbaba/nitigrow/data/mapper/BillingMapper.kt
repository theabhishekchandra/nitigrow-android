package com.websbaba.nitigrow.data.mapper

import com.websbaba.nitigrow.data.local.entity.PaymentEntity
import com.websbaba.nitigrow.data.local.entity.PlanEntity
import com.websbaba.nitigrow.data.local.entity.SubscriptionEntity
import com.websbaba.nitigrow.data.remote.dto.PaymentDto
import com.websbaba.nitigrow.data.remote.dto.PlanDto
import com.websbaba.nitigrow.data.remote.dto.SubscriptionDto
import com.websbaba.nitigrow.domain.model.PaymentRecord
import com.websbaba.nitigrow.domain.model.PaymentStatus
import com.websbaba.nitigrow.domain.model.Plan
import com.websbaba.nitigrow.domain.model.Subscription
import com.websbaba.nitigrow.domain.model.SubscriptionStatus
import java.time.Instant
import java.time.format.DateTimeParseException

private fun parseInstantOpt(iso: String?): Long? = iso?.let {
    try { Instant.parse(it).toEpochMilli() } catch (_: DateTimeParseException) { null }
}

private fun parseInstant(iso: String): Long =
    try { Instant.parse(iso).toEpochMilli() }
    catch (_: DateTimeParseException) { System.currentTimeMillis() }

fun PlanDto.toEntity() = PlanEntity(
    id = id,
    name = name,
    priceInr = priceInr,
    periodDays = periodDays,
    featuresCsv = features.joinToString("|"),
    isPopular = isPopular
)

fun PlanEntity.toDomain() = Plan(
    id = id,
    name = name,
    priceInr = priceInr,
    periodDays = periodDays,
    features = if (featuresCsv.isBlank()) emptyList()
               else featuresCsv.split('|').filter { it.isNotBlank() },
    isPopular = isPopular
)

fun SubscriptionDto.toEntity() = SubscriptionEntity(
    id = 0,
    planId = planId,
    planName = planName,
    status = status.uppercase(),
    renewsAtEpochMs = parseInstantOpt(renewsAt),
    cancelledAtEpochMs = parseInstantOpt(cancelledAt)
)

fun SubscriptionEntity.toDomain() = Subscription(
    planId = planId,
    planName = planName,
    status = SubscriptionStatus.safeValueOf(status),
    renewsAt = renewsAtEpochMs?.let(Instant::ofEpochMilli),
    cancelledAt = cancelledAtEpochMs?.let(Instant::ofEpochMilli)
)

fun PaymentDto.toEntity() = PaymentEntity(
    id = id,
    orderId = orderId,
    amountInr = amountInr,
    status = status.uppercase(),
    method = method,
    planName = planName,
    createdAtEpochMs = parseInstant(createdAt)
)

fun PaymentEntity.toDomain() = PaymentRecord(
    id = id,
    orderId = orderId,
    amountInr = amountInr,
    status = PaymentStatus.safeValueOf(status),
    method = method,
    createdAt = Instant.ofEpochMilli(createdAtEpochMs),
    planName = planName
)
