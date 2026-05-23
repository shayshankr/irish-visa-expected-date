package com.shayshankrathore.irishvisadate

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class WorkingDaysTest {

    // ── addWorkingDays ────────────────────────────────────────────────────────

    @Test
    fun `addWorkingDays zero returns same date`() {
        val date = LocalDate.of(2026, 6, 1) // Monday
        assertEquals(date, date.addWorkingDays(0))
    }

    @Test
    fun `addWorkingDays one on a working day returns same date`() {
        // Day 1 is the start date itself, so +1 = same date
        val monday = LocalDate.of(2026, 6, 1)
        assertEquals(monday, monday.addWorkingDays(1))
    }

    @Test
    fun `addWorkingDays skips Saturday and Sunday`() {
        // Friday Jun 5 2026 + 2 working days → Friday (day 1) + Monday (day 2)
        val friday = LocalDate.of(2026, 6, 5)
        val expected = LocalDate.of(2026, 6, 8) // Monday
        assertEquals(expected, friday.addWorkingDays(2))
    }

    @Test
    fun `addWorkingDays skips a known 2026 Irish holiday`() {
        // St. Patrick's Day 2026 is Tue Mar 17. Starting Mon Mar 16 + 2 days:
        // day 1 = Mon Mar 16, day 2 = skip Tue (holiday) → Wed Mar 18
        val mon = LocalDate.of(2026, 3, 16)
        val expected = LocalDate.of(2026, 3, 18)
        assertEquals(expected, mon.addWorkingDays(2))
    }

    @Test
    fun `addWorkingDays skips a known 2026 Indian holiday`() {
        // Republic Day 2026 is Mon Jan 26. Starting Fri Jan 23 + 2 working days:
        // day 1 = Fri Jan 23, skip Sat, skip Sun, skip Mon (holiday) → day 2 = Tue Jan 27
        val fri = LocalDate.of(2026, 1, 23)
        val expected = LocalDate.of(2026, 1, 27)
        assertEquals(expected, fri.addWorkingDays(2))
    }

    @Test
    fun `addWorkingDays skips weekend AND holiday in same stretch`() {
        // Easter weekend 2026: Good Friday Apr 3, Easter Mon Apr 6
        // Start Thu Apr 2 + 2 working days:
        // day 1 = Thu Apr 2, day 2 = skip Fri Apr 3 (holiday), skip Sat Apr 4,
        //         skip Sun Apr 5, skip Mon Apr 6 (holiday) → Tue Apr 7
        val thu = LocalDate.of(2026, 4, 2)
        val expected = LocalDate.of(2026, 4, 7)
        assertEquals(expected, thu.addWorkingDays(2))
    }

    // ── workingDaysBetween ────────────────────────────────────────────────────

    @Test
    fun `workingDaysBetween same date returns 1`() {
        val date = LocalDate.of(2026, 6, 8) // Monday, not a holiday
        assertEquals(1L, workingDaysBetween(date, date))
    }

    @Test
    fun `workingDaysBetween to before from returns 0`() {
        val from = LocalDate.of(2026, 6, 5)
        val to = LocalDate.of(2026, 6, 1)
        assertEquals(0L, workingDaysBetween(from, to))
    }

    @Test
    fun `workingDaysBetween excludes weekend days`() {
        // Mon Jun 8 to Fri Jun 12 2026 = 5 working days (no holidays this week)
        val from = LocalDate.of(2026, 6, 8)
        val to = LocalDate.of(2026, 6, 12)
        assertEquals(5L, workingDaysBetween(from, to))
    }

    @Test
    fun `workingDaysBetween excludes holiday within range`() {
        // Mon Jun 1 to Tue Jun 2 = 2 days normally.
        // St Patrick week: Mon Mar 16 to Wed Mar 18 spans holiday Tue Mar 17
        // Expected: Mon (1) + Wed (2) = 2 working days
        val from = LocalDate.of(2026, 3, 16)
        val to = LocalDate.of(2026, 3, 18)
        assertEquals(2L, workingDaysBetween(from, to))
    }
}
