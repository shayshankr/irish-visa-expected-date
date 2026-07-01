package com.shayshankrathore.irishvisadate

import org.junit.Assert.assertEquals
import org.junit.Test

class CommunitySubmissionRepositoryTest {

    // ── calendarDays ──────────────────────────────────────────────────────────

    @Test
    fun `calendarDays computes difference between submission and decision`() {
        assertEquals(30, CommunitySubmissionRepository.calendarDays("2026-01-01", "2026-01-31"))
    }

    @Test
    fun `calendarDays is zero for same-day submission and decision`() {
        assertEquals(0, CommunitySubmissionRepository.calendarDays("2026-01-01", "2026-01-01"))
    }

    // ── versionBucketFrom ─────────────────────────────────────────────────────

    @Test
    fun `versionBucketFrom keeps only major and minor`() {
        assertEquals("1.7.x", CommunitySubmissionRepository.versionBucketFrom("1.7.4"))
    }

    @Test
    fun `versionBucketFrom handles two-part version`() {
        assertEquals("2.0.x", CommunitySubmissionRepository.versionBucketFrom("2.0"))
    }

    @Test
    fun `versionBucketFrom falls back for single-part version`() {
        assertEquals("5.x", CommunitySubmissionRepository.versionBucketFrom("5"))
    }
}
