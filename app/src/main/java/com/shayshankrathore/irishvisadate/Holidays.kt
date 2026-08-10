package com.shayshankrathore.irishvisadate

import java.time.LocalDate

// Verify floating holidays (Islamic, Hindu lunar, Chinese lunar) against an
// official calendar each year and update this constant when you do.
const val HOLIDAYS_LAST_UPDATED = "2026-08-10"

// ── Irish public holidays (shared base for all embassies) ─────────────────────

private val IRISH_2026: Set<LocalDate> = setOf(
    LocalDate.of(2026, 1, 1),   // New Year's Day
    LocalDate.of(2026, 2, 2),   // St. Brigid's Day
    LocalDate.of(2026, 3, 17),  // St. Patrick's Day
    LocalDate.of(2026, 4, 3),   // Good Friday
    LocalDate.of(2026, 4, 6),   // Easter Monday
    LocalDate.of(2026, 5, 4),   // May Bank Holiday
    LocalDate.of(2026, 6, 1),   // June Bank Holiday
    LocalDate.of(2026, 8, 3),   // August Bank Holiday
    LocalDate.of(2026, 10, 26), // October Bank Holiday
    LocalDate.of(2026, 12, 25), // Christmas Day
    LocalDate.of(2026, 12, 26), // St. Stephen's Day
)

private val IRISH_2027: Set<LocalDate> = setOf(
    LocalDate.of(2027, 1, 1),
    LocalDate.of(2027, 2, 1),   // St. Brigid's Day
    LocalDate.of(2027, 3, 17),
    LocalDate.of(2027, 3, 26),  // Good Friday
    LocalDate.of(2027, 3, 29),  // Easter Monday
    LocalDate.of(2027, 5, 3),
    LocalDate.of(2027, 6, 7),
    LocalDate.of(2027, 8, 2),
    LocalDate.of(2027, 10, 25),
    LocalDate.of(2027, 12, 25),
    LocalDate.of(2027, 12, 26),
    LocalDate.of(2027, 12, 27), // Christmas substitute (Mon)
    LocalDate.of(2027, 12, 28), // St. Stephen's substitute (Mon)
)

private val IRISH = IRISH_2026 + IRISH_2027

// ── India (Embassy: New Delhi) ────────────────────────────────────────────────

private val INDIA_2026: Set<LocalDate> = setOf(
    LocalDate.of(2026, 1, 26),  // Republic Day
    LocalDate.of(2026, 3, 4),   // Holi (verify)
    LocalDate.of(2026, 3, 20),  // Eid-ul-Fitr (verify)
    LocalDate.of(2026, 5, 11),  // Buddha Purnima (verify)
    LocalDate.of(2026, 5, 27),  // Eid-ul-Adha (verify)
    LocalDate.of(2026, 8, 13),  // Irish Embassy closure for relocation
    LocalDate.of(2026, 8, 14),  // Irish Embassy closure for relocation
    LocalDate.of(2026, 8, 15),  // Independence Day
    LocalDate.of(2026, 8, 16),  // Janmashtami (verify)
    LocalDate.of(2026, 8, 17),  // Irish Embassy closure for relocation
    LocalDate.of(2026, 8, 28),  // Raksha Bandhan (verify)
    LocalDate.of(2026, 10, 2),  // Gandhi Jayanti
    LocalDate.of(2026, 10, 20), // Dussehra (verify)
    LocalDate.of(2026, 11, 8),  // Diwali (verify)
    LocalDate.of(2026, 11, 23), // Guru Nanak Jayanti (verify)
)

private val INDIA_2027: Set<LocalDate> = setOf(
    LocalDate.of(2027, 1, 26),
    LocalDate.of(2027, 3, 9),   // Eid-ul-Fitr (verify)
    LocalDate.of(2027, 3, 22),  // Holi (verify)
    LocalDate.of(2027, 5, 1),   // Buddha Purnima (verify)
    LocalDate.of(2027, 5, 16),  // Eid-ul-Adha (verify)
    LocalDate.of(2027, 8, 5),   // Janmashtami (verify)
    LocalDate.of(2027, 8, 15),
    LocalDate.of(2027, 8, 17),  // Raksha Bandhan (verify)
    LocalDate.of(2027, 10, 2),
    LocalDate.of(2027, 10, 9),  // Dussehra (verify)
    LocalDate.of(2027, 10, 28), // Diwali (verify)
    LocalDate.of(2027, 11, 12), // Guru Nanak Jayanti (verify)
)

val INDIA_HOLIDAYS: Set<LocalDate> = IRISH + INDIA_2026 + INDIA_2027

// ── Russia (Embassy: Moscow) ──────────────────────────────────────────────────

private val RUSSIA_2026: Set<LocalDate> = setOf(
    LocalDate.of(2026, 1, 2),   // New Year holiday block
    LocalDate.of(2026, 1, 3),
    LocalDate.of(2026, 1, 4),
    LocalDate.of(2026, 1, 5),
    LocalDate.of(2026, 1, 6),
    LocalDate.of(2026, 1, 7),   // Orthodox Christmas
    LocalDate.of(2026, 1, 8),
    LocalDate.of(2026, 2, 23),  // Defender of the Fatherland Day
    LocalDate.of(2026, 3, 8),   // International Women's Day
    LocalDate.of(2026, 5, 1),   // Spring and Labour Day
    LocalDate.of(2026, 5, 9),   // Victory Day
    LocalDate.of(2026, 6, 12),  // Russia Day
    LocalDate.of(2026, 11, 4),  // National Unity Day
)

private val RUSSIA_2027: Set<LocalDate> = setOf(
    LocalDate.of(2027, 1, 2),
    LocalDate.of(2027, 1, 3),
    LocalDate.of(2027, 1, 4),
    LocalDate.of(2027, 1, 5),
    LocalDate.of(2027, 1, 6),
    LocalDate.of(2027, 1, 7),   // Orthodox Christmas
    LocalDate.of(2027, 1, 8),
    LocalDate.of(2027, 2, 23),
    LocalDate.of(2027, 3, 8),
    LocalDate.of(2027, 5, 1),
    LocalDate.of(2027, 5, 9),
    LocalDate.of(2027, 6, 12),
    LocalDate.of(2027, 11, 4),
)

val RUSSIA_HOLIDAYS: Set<LocalDate> = IRISH + RUSSIA_2026 + RUSSIA_2027

// ── UK (Embassy: London) — England & Wales bank holidays ─────────────────────

private val UK_2026: Set<LocalDate> = setOf(
    LocalDate.of(2026, 1, 1),
    LocalDate.of(2026, 4, 3),   // Good Friday
    LocalDate.of(2026, 4, 6),   // Easter Monday
    LocalDate.of(2026, 5, 4),   // Early May Bank Holiday
    LocalDate.of(2026, 5, 25),  // Spring Bank Holiday (last Mon May)
    LocalDate.of(2026, 8, 31),  // Summer Bank Holiday (last Mon Aug)
    LocalDate.of(2026, 12, 25),
    LocalDate.of(2026, 12, 26), // Boxing Day
)

private val UK_2027: Set<LocalDate> = setOf(
    LocalDate.of(2027, 1, 1),
    LocalDate.of(2027, 3, 26),  // Good Friday
    LocalDate.of(2027, 3, 29),  // Easter Monday
    LocalDate.of(2027, 5, 3),   // Early May Bank Holiday
    LocalDate.of(2027, 5, 31),  // Spring Bank Holiday
    LocalDate.of(2027, 8, 30),  // Summer Bank Holiday
    LocalDate.of(2027, 12, 27), // Christmas substitute
    LocalDate.of(2027, 12, 28), // Boxing Day substitute
)

val UK_HOLIDAYS: Set<LocalDate> = IRISH + UK_2026 + UK_2027

// ── China (Embassy: Beijing) ──────────────────────────────────────────────────
// Spring Festival, Dragon Boat, Mid-Autumn dates are approximate — verify each year.

private val CHINA_2026: Set<LocalDate> = setOf(
    LocalDate.of(2026, 1, 1),
    // Spring Festival (~Feb 17 — verify exact adjustment dates)
    LocalDate.of(2026, 2, 15),
    LocalDate.of(2026, 2, 16),
    LocalDate.of(2026, 2, 17),
    LocalDate.of(2026, 2, 18),
    LocalDate.of(2026, 2, 19),
    LocalDate.of(2026, 2, 20),
    LocalDate.of(2026, 2, 21),
    // Qingming (Tomb-Sweeping) Festival
    LocalDate.of(2026, 4, 4),
    LocalDate.of(2026, 4, 5),
    LocalDate.of(2026, 4, 6),
    // Labour Day Golden Week
    LocalDate.of(2026, 5, 1),
    LocalDate.of(2026, 5, 2),
    LocalDate.of(2026, 5, 3),
    LocalDate.of(2026, 5, 4),
    LocalDate.of(2026, 5, 5),
    // Dragon Boat Festival (~Jun 19, verify)
    LocalDate.of(2026, 6, 19),
    LocalDate.of(2026, 6, 20),
    LocalDate.of(2026, 6, 21),
    // National Day / Mid-Autumn Golden Week
    LocalDate.of(2026, 10, 1),
    LocalDate.of(2026, 10, 2),
    LocalDate.of(2026, 10, 3),
    LocalDate.of(2026, 10, 4),
    LocalDate.of(2026, 10, 5),
    LocalDate.of(2026, 10, 6),
    LocalDate.of(2026, 10, 7),
)

private val CHINA_2027: Set<LocalDate> = setOf(
    LocalDate.of(2027, 1, 1),
    // Spring Festival (~Feb 6, verify)
    LocalDate.of(2027, 2, 4),
    LocalDate.of(2027, 2, 5),
    LocalDate.of(2027, 2, 6),
    LocalDate.of(2027, 2, 7),
    LocalDate.of(2027, 2, 8),
    LocalDate.of(2027, 2, 9),
    LocalDate.of(2027, 2, 10),
    // Qingming
    LocalDate.of(2027, 4, 4),
    LocalDate.of(2027, 4, 5),
    LocalDate.of(2027, 4, 6),
    // Labour Day
    LocalDate.of(2027, 5, 1),
    LocalDate.of(2027, 5, 2),
    LocalDate.of(2027, 5, 3),
    // Dragon Boat Festival (~Jun 8, verify)
    LocalDate.of(2027, 6, 8),
    LocalDate.of(2027, 6, 9),
    LocalDate.of(2027, 6, 10),
    // National Day
    LocalDate.of(2027, 10, 1),
    LocalDate.of(2027, 10, 2),
    LocalDate.of(2027, 10, 3),
    LocalDate.of(2027, 10, 4),
    LocalDate.of(2027, 10, 5),
    LocalDate.of(2027, 10, 6),
    LocalDate.of(2027, 10, 7),
)

val CHINA_HOLIDAYS: Set<LocalDate> = IRISH + CHINA_2026 + CHINA_2027

// ── Turkey (Embassy: Ankara) ──────────────────────────────────────────────────
// Eid dates are approximate — verify each year.

private val TURKEY_2026: Set<LocalDate> = setOf(
    LocalDate.of(2026, 1, 1),
    LocalDate.of(2026, 3, 20),  // Eid al-Fitr Day 1 (verify)
    LocalDate.of(2026, 3, 21),
    LocalDate.of(2026, 3, 22),
    LocalDate.of(2026, 4, 23),  // National Sovereignty & Children's Day
    LocalDate.of(2026, 5, 1),   // Labour Day
    LocalDate.of(2026, 5, 19),  // Atatürk Commemoration Day
    LocalDate.of(2026, 5, 27),  // Eid al-Adha Day 1 (verify)
    LocalDate.of(2026, 5, 28),
    LocalDate.of(2026, 5, 29),
    LocalDate.of(2026, 5, 30),
    LocalDate.of(2026, 7, 15),  // Democracy and National Unity Day
    LocalDate.of(2026, 8, 30),  // Victory Day
    LocalDate.of(2026, 10, 29), // Republic Day
)

private val TURKEY_2027: Set<LocalDate> = setOf(
    LocalDate.of(2027, 1, 1),
    LocalDate.of(2027, 3, 9),   // Eid al-Fitr Day 1 (verify)
    LocalDate.of(2027, 3, 10),
    LocalDate.of(2027, 3, 11),
    LocalDate.of(2027, 4, 23),
    LocalDate.of(2027, 5, 1),
    LocalDate.of(2027, 5, 16),  // Eid al-Adha Day 1 (verify)
    LocalDate.of(2027, 5, 17),
    LocalDate.of(2027, 5, 18),
    LocalDate.of(2027, 5, 19),  // Atatürk Day (may coincide)
    LocalDate.of(2027, 7, 15),
    LocalDate.of(2027, 8, 30),
    LocalDate.of(2027, 10, 29),
)

val TURKEY_HOLIDAYS: Set<LocalDate> = IRISH + TURKEY_2026 + TURKEY_2027

// ── UAE (Embassy: Abu Dhabi) ──────────────────────────────────────────────────
// Islamic holiday dates are approximate — verify each year.

private val UAE_2026: Set<LocalDate> = setOf(
    LocalDate.of(2026, 1, 1),
    LocalDate.of(2026, 3, 20),  // Eid al-Fitr Day 1 (verify)
    LocalDate.of(2026, 3, 21),
    LocalDate.of(2026, 3, 22),
    LocalDate.of(2026, 5, 26),  // Arafat Day (verify)
    LocalDate.of(2026, 5, 27),  // Eid al-Adha Day 1 (verify)
    LocalDate.of(2026, 5, 28),
    LocalDate.of(2026, 5, 29),
    LocalDate.of(2026, 6, 16),  // Islamic New Year (verify)
    LocalDate.of(2026, 8, 25),  // Prophet's Birthday (verify)
    LocalDate.of(2026, 12, 2),  // UAE National Day
    LocalDate.of(2026, 12, 3),
)

private val UAE_2027: Set<LocalDate> = setOf(
    LocalDate.of(2027, 1, 1),
    LocalDate.of(2027, 3, 9),   // Eid al-Fitr Day 1 (verify)
    LocalDate.of(2027, 3, 10),
    LocalDate.of(2027, 3, 11),
    LocalDate.of(2027, 5, 15),  // Arafat Day (verify)
    LocalDate.of(2027, 5, 16),  // Eid al-Adha Day 1 (verify)
    LocalDate.of(2027, 5, 17),
    LocalDate.of(2027, 5, 18),
    LocalDate.of(2027, 6, 5),   // Islamic New Year (verify)
    LocalDate.of(2027, 8, 14),  // Prophet's Birthday (verify)
    LocalDate.of(2027, 12, 2),
    LocalDate.of(2027, 12, 3),
)

val UAE_HOLIDAYS: Set<LocalDate> = IRISH + UAE_2026 + UAE_2027

// ── Pakistan (Embassy: Islamabad) ─────────────────────────────────────────────
// Islamic holiday dates are approximate — verify each year.

private val PAKISTAN_2026: Set<LocalDate> = setOf(
    LocalDate.of(2026, 2, 5),   // Kashmir Solidarity Day
    LocalDate.of(2026, 3, 20),  // Eid ul-Fitr Day 1 (verify)
    LocalDate.of(2026, 3, 21),
    LocalDate.of(2026, 3, 22),
    LocalDate.of(2026, 3, 23),  // Pakistan Day
    LocalDate.of(2026, 5, 1),   // Labour Day
    LocalDate.of(2026, 5, 27),  // Eid ul-Adha Day 1 (verify)
    LocalDate.of(2026, 5, 28),
    LocalDate.of(2026, 5, 29),
    LocalDate.of(2026, 8, 14),  // Independence Day
    LocalDate.of(2026, 9, 6),   // Defence Day
    LocalDate.of(2026, 11, 9),  // Iqbal Day
    LocalDate.of(2026, 12, 25), // Quaid-e-Azam Day / Christmas
)

private val PAKISTAN_2027: Set<LocalDate> = setOf(
    LocalDate.of(2027, 2, 5),
    LocalDate.of(2027, 3, 9),   // Eid ul-Fitr Day 1 (verify)
    LocalDate.of(2027, 3, 10),
    LocalDate.of(2027, 3, 11),
    LocalDate.of(2027, 3, 23),  // Pakistan Day
    LocalDate.of(2027, 5, 1),
    LocalDate.of(2027, 5, 16),  // Eid ul-Adha Day 1 (verify)
    LocalDate.of(2027, 5, 17),
    LocalDate.of(2027, 5, 18),
    LocalDate.of(2027, 8, 14),
    LocalDate.of(2027, 9, 6),
    LocalDate.of(2027, 11, 9),
    LocalDate.of(2027, 12, 25),
)

val PAKISTAN_HOLIDAYS: Set<LocalDate> = IRISH + PAKISTAN_2026 + PAKISTAN_2027

// ── Germany (Embassy: Berlin) — federal public holidays ──────────────────────

private val GERMANY_2026: Set<LocalDate> = setOf(
    LocalDate.of(2026, 1, 1),   // New Year's Day
    LocalDate.of(2026, 4, 3),   // Good Friday
    LocalDate.of(2026, 4, 6),   // Easter Monday
    LocalDate.of(2026, 5, 1),   // Labour Day
    LocalDate.of(2026, 5, 14),  // Ascension Day (Christi Himmelfahrt)
    LocalDate.of(2026, 5, 25),  // Whit Monday (Pfingstmontag)
    LocalDate.of(2026, 10, 3),  // Day of German Unity
    LocalDate.of(2026, 12, 25),
    LocalDate.of(2026, 12, 26), // Second Christmas Day
)

private val GERMANY_2027: Set<LocalDate> = setOf(
    LocalDate.of(2027, 1, 1),
    LocalDate.of(2027, 3, 26),  // Good Friday
    LocalDate.of(2027, 3, 29),  // Easter Monday
    LocalDate.of(2027, 5, 1),   // Labour Day
    LocalDate.of(2027, 5, 6),   // Ascension Day
    LocalDate.of(2027, 5, 17),  // Whit Monday
    LocalDate.of(2027, 10, 3),  // Day of German Unity
    LocalDate.of(2027, 12, 25),
    LocalDate.of(2027, 12, 26),
)

val GERMANY_HOLIDAYS: Set<LocalDate> = IRISH + GERMANY_2026 + GERMANY_2027

// ── France (Embassy: Paris) ───────────────────────────────────────────────────

private val FRANCE_2026: Set<LocalDate> = setOf(
    LocalDate.of(2026, 1, 1),   // New Year's Day
    LocalDate.of(2026, 4, 6),   // Easter Monday
    LocalDate.of(2026, 5, 1),   // Fête du Travail
    LocalDate.of(2026, 5, 8),   // Victoire 1945 (VE Day)
    LocalDate.of(2026, 5, 14),  // Ascension Day
    LocalDate.of(2026, 5, 25),  // Whit Monday (Lundi de Pentecôte)
    LocalDate.of(2026, 7, 14),  // Bastille Day
    LocalDate.of(2026, 8, 15),  // Assumption of Mary
    LocalDate.of(2026, 11, 1),  // All Saints' Day (Toussaint)
    LocalDate.of(2026, 11, 11), // Armistice Day
    LocalDate.of(2026, 12, 25),
)

private val FRANCE_2027: Set<LocalDate> = setOf(
    LocalDate.of(2027, 1, 1),
    LocalDate.of(2027, 3, 29),  // Easter Monday
    LocalDate.of(2027, 5, 1),
    LocalDate.of(2027, 5, 6),   // Ascension Day
    LocalDate.of(2027, 5, 8),
    LocalDate.of(2027, 5, 17),  // Whit Monday
    LocalDate.of(2027, 7, 14),
    LocalDate.of(2027, 8, 15),
    LocalDate.of(2027, 11, 1),
    LocalDate.of(2027, 11, 11),
    LocalDate.of(2027, 12, 25),
)

val FRANCE_HOLIDAYS: Set<LocalDate> = IRISH + FRANCE_2026 + FRANCE_2027

// ── Nigeria (Embassy: Abuja) ──────────────────────────────────────────────────
// Islamic holiday dates are approximate — verify each year.

private val NIGERIA_2026: Set<LocalDate> = setOf(
    LocalDate.of(2026, 1, 1),   // New Year's Day
    LocalDate.of(2026, 3, 20),  // Eid al-Fitr Day 1 (verify)
    LocalDate.of(2026, 3, 21),  // Eid al-Fitr Day 2
    LocalDate.of(2026, 4, 3),   // Good Friday
    LocalDate.of(2026, 4, 6),   // Easter Monday
    LocalDate.of(2026, 5, 1),   // Workers' Day
    LocalDate.of(2026, 5, 27),  // Eid al-Adha Day 1 (verify)
    LocalDate.of(2026, 5, 28),  // Eid al-Adha Day 2
    LocalDate.of(2026, 6, 12),  // Democracy Day
    LocalDate.of(2026, 10, 1),  // Independence Day
    LocalDate.of(2026, 12, 25),
    LocalDate.of(2026, 12, 26), // Boxing Day
)

private val NIGERIA_2027: Set<LocalDate> = setOf(
    LocalDate.of(2027, 1, 1),
    LocalDate.of(2027, 3, 9),   // Eid al-Fitr Day 1 (verify)
    LocalDate.of(2027, 3, 10),
    LocalDate.of(2027, 3, 26),  // Good Friday
    LocalDate.of(2027, 3, 29),  // Easter Monday
    LocalDate.of(2027, 5, 1),
    LocalDate.of(2027, 5, 16),  // Eid al-Adha Day 1 (verify)
    LocalDate.of(2027, 5, 17),
    LocalDate.of(2027, 6, 12),
    LocalDate.of(2027, 10, 1),
    LocalDate.of(2027, 12, 25),
    LocalDate.of(2027, 12, 26),
)

val NIGERIA_HOLIDAYS: Set<LocalDate> = IRISH + NIGERIA_2026 + NIGERIA_2027

// ── South Africa (Embassy: Pretoria) ─────────────────────────────────────────
// Sunday holidays shift to Monday per the Public Holidays Act.

private val SOUTH_AFRICA_2026: Set<LocalDate> = setOf(
    LocalDate.of(2026, 1, 1),   // New Year's Day
    LocalDate.of(2026, 3, 21),  // Human Rights Day (Saturday — no weekday impact)
    LocalDate.of(2026, 4, 3),   // Good Friday
    LocalDate.of(2026, 4, 6),   // Family Day (Easter Monday)
    LocalDate.of(2026, 4, 27),  // Freedom Day
    LocalDate.of(2026, 5, 1),   // Workers' Day
    LocalDate.of(2026, 6, 16),  // Youth Day
    LocalDate.of(2026, 8, 9),   // National Women's Day (Sunday)
    LocalDate.of(2026, 8, 10),  // National Women's Day substitute (Monday)
    LocalDate.of(2026, 9, 24),  // Heritage Day
    LocalDate.of(2026, 12, 16), // Day of Reconciliation
    LocalDate.of(2026, 12, 25),
    LocalDate.of(2026, 12, 26), // Day of Goodwill
)

private val SOUTH_AFRICA_2027: Set<LocalDate> = setOf(
    LocalDate.of(2027, 1, 1),
    LocalDate.of(2027, 3, 21),  // Human Rights Day (Sunday)
    LocalDate.of(2027, 3, 22),  // Human Rights Day substitute (Monday)
    LocalDate.of(2027, 3, 26),  // Good Friday
    LocalDate.of(2027, 3, 29),  // Family Day (Easter Monday)
    LocalDate.of(2027, 4, 27),  // Freedom Day
    LocalDate.of(2027, 5, 1),   // Workers' Day (Saturday)
    LocalDate.of(2027, 5, 3),   // Workers' Day substitute (Monday)
    LocalDate.of(2027, 6, 16),  // Youth Day
    LocalDate.of(2027, 8, 9),   // National Women's Day
    LocalDate.of(2027, 9, 24),  // Heritage Day
    LocalDate.of(2027, 12, 16),
    LocalDate.of(2027, 12, 25),
    LocalDate.of(2027, 12, 26),
)

val SOUTH_AFRICA_HOLIDAYS: Set<LocalDate> = IRISH + SOUTH_AFRICA_2026 + SOUTH_AFRICA_2027

// ── Philippines (Embassy: Manila) ─────────────────────────────────────────────

private val PHILIPPINES_2026: Set<LocalDate> = setOf(
    LocalDate.of(2026, 1, 1),   // New Year's Day
    LocalDate.of(2026, 4, 2),   // Maundy Thursday
    LocalDate.of(2026, 4, 3),   // Good Friday
    LocalDate.of(2026, 4, 9),   // Araw ng Kagitingan (Day of Valor)
    LocalDate.of(2026, 5, 1),   // Labour Day
    LocalDate.of(2026, 6, 12),  // Independence Day
    LocalDate.of(2026, 8, 31),  // National Heroes Day (last Monday of August)
    LocalDate.of(2026, 11, 1),  // All Saints' Day
    LocalDate.of(2026, 11, 30), // Bonifacio Day
    LocalDate.of(2026, 12, 25),
    LocalDate.of(2026, 12, 30), // Rizal Day
)

private val PHILIPPINES_2027: Set<LocalDate> = setOf(
    LocalDate.of(2027, 1, 1),
    LocalDate.of(2027, 3, 25),  // Maundy Thursday
    LocalDate.of(2027, 3, 26),  // Good Friday
    LocalDate.of(2027, 4, 9),   // Araw ng Kagitingan
    LocalDate.of(2027, 5, 1),   // Labour Day (Saturday)
    LocalDate.of(2027, 5, 3),   // Labour Day substitute (Monday)
    LocalDate.of(2027, 6, 12),
    LocalDate.of(2027, 8, 30),  // National Heroes Day (last Monday of August)
    LocalDate.of(2027, 11, 1),
    LocalDate.of(2027, 11, 30),
    LocalDate.of(2027, 12, 25),
    LocalDate.of(2027, 12, 30),
)

val PHILIPPINES_HOLIDAYS: Set<LocalDate> = IRISH + PHILIPPINES_2026 + PHILIPPINES_2027

// ── Bangladesh (Embassy: Dhaka) ───────────────────────────────────────────────
// Islamic holiday dates are approximate — verify each year.

private val BANGLADESH_2026: Set<LocalDate> = setOf(
    LocalDate.of(2026, 2, 21),  // International Mother Language Day
    LocalDate.of(2026, 3, 17),  // Birth anniversary of Sheikh Mujibur Rahman
    LocalDate.of(2026, 3, 20),  // Eid ul-Fitr Day 1 (verify)
    LocalDate.of(2026, 3, 21),  // Eid ul-Fitr Day 2
    LocalDate.of(2026, 3, 22),  // Eid ul-Fitr Day 3
    LocalDate.of(2026, 3, 26),  // Independence Day
    LocalDate.of(2026, 4, 14),  // Pahela Baishakh (Bengali New Year)
    LocalDate.of(2026, 5, 1),   // May Day
    LocalDate.of(2026, 5, 27),  // Eid ul-Adha Day 1 (verify)
    LocalDate.of(2026, 5, 28),  // Eid ul-Adha Day 2
    LocalDate.of(2026, 5, 29),  // Eid ul-Adha Day 3
    LocalDate.of(2026, 8, 15),  // National Mourning Day
    LocalDate.of(2026, 12, 16), // Victory Day
    LocalDate.of(2026, 12, 25), // Christmas Day
)

private val BANGLADESH_2027: Set<LocalDate> = setOf(
    LocalDate.of(2027, 2, 21),
    LocalDate.of(2027, 3, 9),   // Eid ul-Fitr Day 1 (verify)
    LocalDate.of(2027, 3, 10),
    LocalDate.of(2027, 3, 11),
    LocalDate.of(2027, 3, 17),
    LocalDate.of(2027, 3, 26),
    LocalDate.of(2027, 4, 14),
    LocalDate.of(2027, 5, 1),
    LocalDate.of(2027, 5, 16),  // Eid ul-Adha Day 1 (verify)
    LocalDate.of(2027, 5, 17),
    LocalDate.of(2027, 5, 18),
    LocalDate.of(2027, 8, 15),
    LocalDate.of(2027, 12, 16),
    LocalDate.of(2027, 12, 25),
)

val BANGLADESH_HOLIDAYS: Set<LocalDate> = IRISH + BANGLADESH_2026 + BANGLADESH_2027

// ── Backward compatibility ────────────────────────────────────────────────────
// WorkingDays functions default to this set; unit tests rely on India + Irish holidays.
val ALL_HOLIDAYS: Set<LocalDate> = INDIA_HOLIDAYS
