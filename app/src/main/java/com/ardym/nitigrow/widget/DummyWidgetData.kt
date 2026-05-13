package com.ardym.nitigrow.widget

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Lightweight value-types kept in the widget package so the Glance composables
 * can stay self-contained. Once the backend exposes saved segments + a festival
 * calendar these will be replaced with domain models.
 */
data class WidgetSegment(
    val id: String,
    val name: String,
    val count: Int
)

data class Festival(
    val name: String,
    val date: LocalDate,
    val daysUntil: Int
)

// TODO: This is dummy data we need to delete when development is complete and connect with real APIs.
// Seeds [QuickBroadcastWidget] and [FestivalSendWidget] until the backend exposes
// `/api/segments/top` and `/api/calendar/festivals`.
object DummyWidgetData {

    // Hardcoded major Indian festivals — months/days are deliberately fixed.
    // When the real calendar API lands, this list is the swap target.
    private val FESTIVAL_CALENDAR: List<Pair<String, LocalDate>> = run {
        val year = LocalDate.now().year
        listOf(
            "Pongal" to LocalDate.of(year, 1, 14),
            "Holi"   to LocalDate.of(year, 3, 17),
            "Eid"    to LocalDate.of(year, 4, 11),
            "Onam"   to LocalDate.of(year, 9, 5),
            "Diwali" to LocalDate.of(year, 10, 29)
        )
    }

    // TODO: This is dummy data we need to delete when development is complete and connect with real APIs.
    fun segments(): List<WidgetSegment> = listOf(
        WidgetSegment(id = "seg-customer",  name = "Customer",  count = 245),
        WidgetSegment(id = "seg-wholesale", name = "Wholesale", count = 88),
        WidgetSegment(id = "seg-vip",       name = "VIP",       count = 42)
    )

    // TODO: This is dummy data we need to delete when development is complete and connect with real APIs.
    fun upcomingFestival(): Festival {
        val today = LocalDate.now()
        val nextEntry = FESTIVAL_CALENDAR
            .map { (name, date) ->
                val anchored = if (date.isBefore(today)) date.plusYears(1) else date
                name to anchored
            }
            .minBy { (_, date) -> ChronoUnit.DAYS.between(today, date) }
        val (name, date) = nextEntry
        return Festival(
            name = name,
            date = date,
            daysUntil = ChronoUnit.DAYS.between(today, date).toInt()
        )
    }
}
