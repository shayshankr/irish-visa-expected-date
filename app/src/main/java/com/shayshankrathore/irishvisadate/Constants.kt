package com.shayshankrathore.irishvisadate

// ── Processing times (working days, same across all embassies) ───────────────
// Edit these when official processing-time guidance changes.

const val SHORT_STAY_MIN_DAYS = 20   // ~4 working weeks
const val SHORT_STAY_MAX_DAYS = 40   // ~8 working weeks

const val STUDY_MIN_DAYS = 20        // ~4 working weeks
const val STUDY_MAX_DAYS = 40        // ~8 working weeks

const val JOIN_FAMILY_MIN_DAYS = 130 // ~6 months
const val JOIN_FAMILY_MAX_DAYS = 260 // ~12 months

const val WORK_PERMIT_MIN_DAYS = 20  // Critical Skills / Work
const val WORK_PERMIT_MAX_DAYS = 60

const val WORKING_HOLIDAY_MIN_DAYS = 20
const val WORKING_HOLIDAY_MAX_DAYS = 40

const val TRANSIT_MIN_DAYS = 5
const val TRANSIT_MAX_DAYS = 15

// ── External URLs ─────────────────────────────────────────────────────────────
// Per-embassy decisions URLs live in Embassy.kt.
const val URL_APPEALS_INFO = "https://www.irishimmigration.ie"

// TODO: replace with the hosted privacy policy URL before enabling community sharing in production.
const val PRIVACY_POLICY_URL = "https://www.irishimmigration.ie"
