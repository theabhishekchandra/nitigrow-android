package com.websbaba.nitigrow.data.mapper

import com.websbaba.nitigrow.domain.model.MessageStatus
import java.time.Instant
import java.time.format.DateTimeParseException

/** ISO-8601 → epoch millis, or null when [iso] is absent or malformed. */
internal fun parseInstantOrNull(iso: String?): Long? =
    if (iso.isNullOrBlank()) null
    else try { Instant.parse(iso).toEpochMilli() } catch (_: DateTimeParseException) { null }

/** ISO-8601 → epoch millis; falls back to now so a bad timestamp never drops a row. */
internal fun parseInstant(iso: String?): Long = parseInstantOrNull(iso) ?: System.currentTimeMillis()

/** Backend delivery status → [MessageStatus]; unknown values read as SENT. */
internal fun messageStatusOf(value: String): MessageStatus =
    runCatching { MessageStatus.valueOf(value.uppercase()) }.getOrDefault(MessageStatus.SENT)
