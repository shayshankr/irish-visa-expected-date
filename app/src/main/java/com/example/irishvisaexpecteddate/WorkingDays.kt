package com.example.irishvisaexpecteddate

import java.time.DayOfWeek
import java.time.LocalDate

fun LocalDate.isWorkingDay(): Boolean =
    dayOfWeek != DayOfWeek.SATURDAY &&
    dayOfWeek != DayOfWeek.SUNDAY &&
    this !in ALL_HOLIDAYS

/** Returns the date that is [days] working days after [this], inclusive of [this] as day 1. */
fun LocalDate.addWorkingDays(days: Int): LocalDate {
    if (days <= 0) return this
    var remaining = days - 1  // day 1 is the start date itself
    var current = this
    while (remaining > 0) {
        current = current.plusDays(1)
        if (current.isWorkingDay()) remaining--
    }
    return current
}

/**
 * Counts the number of working days from [from] up to and including [to].
 * Returns 0 if [to] is before [from].
 */
fun workingDaysBetween(from: LocalDate, to: LocalDate): Long {
    if (!to.isAfter(from.minusDays(1))) return 0L
    var count = 0L
    var current = from
    while (!current.isAfter(to)) {
        if (current.isWorkingDay()) count++
        current = current.plusDays(1)
    }
    return count
}
