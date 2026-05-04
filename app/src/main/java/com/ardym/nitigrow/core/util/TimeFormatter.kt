package com.ardym.nitigrow.core.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * WhatsApp-style relative timestamps for conversation list.
 *  - same day → "14:32"
 *  - prior day → "Yesterday"
 *  - within 6 days → "Mon"
 *  - older → "12/03/2026"
 */
object TimeFormatter {
    private val time = DateTimeFormatter.ofPattern("HH:mm")
    private val weekday = DateTimeFormatter.ofPattern("EEE")
    private val date = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    private val zone = ZoneId.systemDefault()

    fun listLabel(instant: Instant, now: Instant = Instant.now()): String {
        val msgDate = instant.atZone(zone).toLocalDate()
        val today = now.atZone(zone).toLocalDate()
        val days = today.toEpochDay() - msgDate.toEpochDay()
        return when {
            days == 0L -> time.format(instant.atZone(zone))
            days == 1L -> "Yesterday"
            days in 2..6 -> weekday.format(instant.atZone(zone))
            else -> date.format(instant.atZone(zone))
        }
    }

    fun chatHeader(date: LocalDate, today: LocalDate = LocalDate.now()): String {
        val days = today.toEpochDay() - date.toEpochDay()
        return when {
            days == 0L -> "Today"
            days == 1L -> "Yesterday"
            days in 2..6 -> weekday.format(date)
            else -> this.date.format(date)
        }
    }
}
