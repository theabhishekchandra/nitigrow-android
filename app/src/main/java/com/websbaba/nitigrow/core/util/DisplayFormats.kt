package com.websbaba.nitigrow.core.util

import java.text.NumberFormat
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

// Display formats shared across screens. English locale is deliberate: month and
// weekday names stay Latin even under the Hindi/Marathi app locale, matching the design.

private val indianNumberFormat: NumberFormat = NumberFormat.getInstance(Locale("en", "IN"))

/** Indian digit grouping, e.g. 140000 → "1,40,000". NumberFormat isn't thread-safe, hence the lock. */
fun formatIndian(value: Number): String = synchronized(indianNumberFormat) { indianNumberFormat.format(value) }

private fun pattern(pattern: String): DateTimeFormatter =
    DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH).withZone(ZoneId.systemDefault())

/** "Mon, 21 Sep" */
val DayFormat: DateTimeFormatter = pattern("EEE, d MMM")

/** "2:30 PM" */
val TimeOfDayFormat: DateTimeFormatter = pattern("h:mm a")

/** "Mon, 21 Sep · 2:30 PM" */
val DayTimeFormat: DateTimeFormatter = pattern("EEE, d MMM · h:mm a")

/** "21 Sep · 2:30 PM" */
val ShortDayTimeFormat: DateTimeFormatter = pattern("d MMM · h:mm a")
