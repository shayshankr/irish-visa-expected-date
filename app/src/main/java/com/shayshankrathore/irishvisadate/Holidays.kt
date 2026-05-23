package com.shayshankrathore.irishvisadate

import java.time.LocalDate

// ── Holiday list metadata ─────────────────────────────────────────────────────
// Verify floating holidays (Islamic, Hindu lunar) against an official calendar
// each year and update this constant when you do.
const val HOLIDAYS_LAST_UPDATED = "2026-05-23"

// ── 2026 ──────────────────────────────────────────────────────────────────────
// Irish public holidays: fixed dates + computed floating dates
// Easter 2026: Sunday Apr 5  (computed via Gregorian algorithm)
// Indian public holidays: Embassy in New Delhi closes on these in addition to Irish ones.
// Islamic/Hindu dates are approximate — verify before each year begins.
val HOLIDAYS_2026: Set<LocalDate> = setOf(
    // ── Irish ────────────────────────────────────────────────────────────────
    LocalDate.of(2026, 1, 1),   // New Year's Day
    LocalDate.of(2026, 2, 2),   // St. Brigid's Day (first Mon in Feb)
    LocalDate.of(2026, 3, 17),  // St. Patrick's Day
    LocalDate.of(2026, 4, 3),   // Good Friday (embassy observes)
    LocalDate.of(2026, 4, 6),   // Easter Monday
    LocalDate.of(2026, 5, 4),   // May Bank Holiday (first Mon in May)
    LocalDate.of(2026, 6, 1),   // June Bank Holiday (first Mon in Jun)
    LocalDate.of(2026, 8, 3),   // August Bank Holiday (first Mon in Aug)
    LocalDate.of(2026, 10, 26), // October Bank Holiday (last Mon in Oct)
    LocalDate.of(2026, 12, 25), // Christmas Day
    LocalDate.of(2026, 12, 26), // St. Stephen's Day

    // ── Indian ───────────────────────────────────────────────────────────────
    LocalDate.of(2026, 1, 26),  // Republic Day
    LocalDate.of(2026, 3, 4),   // Holi (~Phalguna Purnima; verify)
    LocalDate.of(2026, 3, 20),  // Eid-ul-Fitr (~end of Ramadan; verify)
    LocalDate.of(2026, 5, 11),  // Buddha Purnima (~Vaishakha Purnima; verify)
    LocalDate.of(2026, 5, 27),  // Eid-ul-Adha / Bakrid (verify)
    LocalDate.of(2026, 8, 15),  // Independence Day
    LocalDate.of(2026, 8, 16),  // Janmashtami (verify)
    LocalDate.of(2026, 8, 28),  // Raksha Bandhan (verify)
    LocalDate.of(2026, 10, 2),  // Gandhi Jayanti
    LocalDate.of(2026, 10, 20), // Dussehra / Vijayadashami (verify)
    LocalDate.of(2026, 11, 8),  // Diwali (verify)
    LocalDate.of(2026, 11, 23), // Guru Nanak Jayanti (verify)
)

// ── 2027 ──────────────────────────────────────────────────────────────────────
// Easter 2027: Sunday Mar 28  (computed via Gregorian algorithm)
// Dec 25 falls on Saturday → Dec 27 Monday observed; Dec 26 Sun → Dec 28 Mon observed.
val HOLIDAYS_2027: Set<LocalDate> = setOf(
    // ── Irish ────────────────────────────────────────────────────────────────
    LocalDate.of(2027, 1, 1),   // New Year's Day
    LocalDate.of(2027, 2, 1),   // St. Brigid's Day (first Mon in Feb)
    LocalDate.of(2027, 3, 17),  // St. Patrick's Day
    LocalDate.of(2027, 3, 26),  // Good Friday
    LocalDate.of(2027, 3, 29),  // Easter Monday
    LocalDate.of(2027, 5, 3),   // May Bank Holiday (first Mon in May)
    LocalDate.of(2027, 6, 7),   // June Bank Holiday (first Mon in Jun)
    LocalDate.of(2027, 8, 2),   // August Bank Holiday (first Mon in Aug)
    LocalDate.of(2027, 10, 25), // October Bank Holiday (last Mon in Oct)
    LocalDate.of(2027, 12, 25), // Christmas Day (Sat — weekend, but listed)
    LocalDate.of(2027, 12, 26), // St. Stephen's Day (Sun — weekend, but listed)
    LocalDate.of(2027, 12, 27), // Christmas substitute (Mon)
    LocalDate.of(2027, 12, 28), // St. Stephen's substitute (Mon)

    // ── Indian ───────────────────────────────────────────────────────────────
    LocalDate.of(2027, 1, 26),  // Republic Day
    LocalDate.of(2027, 3, 9),   // Eid-ul-Fitr (verify)
    LocalDate.of(2027, 3, 22),  // Holi (verify)
    LocalDate.of(2027, 5, 1),   // Buddha Purnima (verify)
    LocalDate.of(2027, 5, 16),  // Eid-ul-Adha / Bakrid (verify)
    LocalDate.of(2027, 8, 5),   // Janmashtami (verify)
    LocalDate.of(2027, 8, 15),  // Independence Day
    LocalDate.of(2027, 8, 17),  // Raksha Bandhan (verify)
    LocalDate.of(2027, 10, 2),  // Gandhi Jayanti
    LocalDate.of(2027, 10, 9),  // Dussehra / Vijayadashami (verify)
    LocalDate.of(2027, 10, 28), // Diwali (verify)
    LocalDate.of(2027, 11, 12), // Guru Nanak Jayanti (verify)
)

val ALL_HOLIDAYS: Set<LocalDate> = HOLIDAYS_2026 + HOLIDAYS_2027
