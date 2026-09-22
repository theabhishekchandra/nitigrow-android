package com.websbaba.nitigrow.core.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/** True when this instant falls on [today] in [zone] (device defaults). */
fun Instant.isToday(
    today: LocalDate = LocalDate.now(),
    zone: ZoneId = ZoneId.systemDefault(),
): Boolean = atZone(zone).toLocalDate() == today
