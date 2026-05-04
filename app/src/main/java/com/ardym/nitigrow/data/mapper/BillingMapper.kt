package com.ardym.nitigrow.data.mapper

import com.ardym.nitigrow.data.local.entity.PaymentEntity
import com.ardym.nitigrow.data.local.entity.PlanEntity
import com.ardym.nitigrow.data.local.entity.SubscriptionEntity
import com.ardym.nitigrow.data.remote.dto.PaymentDto
import com.ardym.nitigrow.data.remote.dto.PlanDto
import com.ardym.nitigrow.data.remote.dto.SubscriptionDto
import com.ardym.nitigrow.domain.model.PaymentRecord
import com.ardym.nitigrow.domain.model.PaymentStatus
import com.ardym.nitigrow.domain.model.Plan
import com.ardym.nitigrow.domain.model.Subscription
import com.ardym.nitigrow.domain.model.SubscriptionStatus
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
