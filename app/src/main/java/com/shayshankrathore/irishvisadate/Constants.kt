package com.shayshankrathore.irishvisadate

// ── Processing times (working days) ──────────────────────────────────────────
// Edit these when official processing-time guidance changes.

const val SHORT_STAY_MIN_DAYS = 20   // ~4 working weeks
const val SHORT_STAY_MAX_DAYS = 40   // ~8 working weeks

const val STUDY_MIN_DAYS = 20        // ~4 working weeks
const val STUDY_MAX_DAYS = 40        // ~8 working weeks

const val JOIN_FAMILY_MIN_DAYS = 130 // ~6 months
const val JOIN_FAMILY_MAX_DAYS = 260 // ~12 months

// ── VAC transit (working days, VFS Global → Irish Embassy) ───────────────────
const val VAC_DELHI_TRANSIT_DAYS = 1
const val VAC_OTHER_TRANSIT_DAYS = 2   // Mumbai, Bengaluru, Chennai, Kolkata

// ── Return courier (informational — not included in the decision window calc) ─
const val COURIER_RETURN_DAYS = 3

// ── External URLs ─────────────────────────────────────────────────────────────
const val URL_DECISIONS_PAGE =
    "https://www.ireland.ie/en/india/newdelhi/services/visas/processing-times-and-decisions/"
const val URL_APPEALS_INFO =
    "https://www.irishimmigration.ie"
