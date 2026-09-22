package com.websbaba.nitigrow.presentation.feature.campaigns.create

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

/** A one-tap send time such as "Tomorrow, 10:00 AM". */
data class SchedulePreset(val label: String, val at: Instant)

/** Send-time helpers for the wizard's Schedule step. */
object SchedulePresets {

    private val SIX_PM: LocalTime = LocalTime.of(18, 0)
    private val NINE_AM: LocalTime = LocalTime.of(9, 0)
    private val TEN_AM: LocalTime = LocalTime.of(10, 0)

    /** Tomorrow at 10:00 AM — the pre-filled time when the user picks "Schedule for later". */
    fun defaultLater(now: Instant, zone: ZoneId = ZoneId.systemDefault()): Instant =
        combine(now.atZone(zone).toLocalDate().plusDays(1), TEN_AM, zone)

    /**
     * Quick picks: "Today, 6:00 PM" (only while it's still ahead), "Tomorrow, 9:00 AM"
     * and "Tomorrow, 10:00 AM".
     */
    fun quickPicks(now: Instant, zone: ZoneId = ZoneId.systemDefault()): List<SchedulePreset> {
        val today = now.atZone(zone).toLocalDate()
        val tomorrow = today.plusDays(1)
        return buildList {
            val evening = combine(today, SIX_PM, zone)
            if (evening.isAfter(now)) add(SchedulePreset("Today, 6:00 PM", evening))
            add(SchedulePreset("Tomorrow, 9:00 AM", combine(tomorrow, NINE_AM, zone)))
            add(SchedulePreset("Tomorrow, 10:00 AM", combine(tomorrow, TEN_AM, zone)))
        }
    }

    fun combine(date: LocalDate, time: LocalTime, zone: ZoneId = ZoneId.systemDefault()): Instant =
        date.atTime(time).atZone(zone).toInstant()
}
