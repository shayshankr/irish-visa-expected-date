package com.shayshankrathore.irishvisadate

import android.content.Context
import com.google.firebase.Firebase
import com.google.firebase.firestore.AggregateField
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.temporal.ChronoUnit

object CommunitySubmissionRepository {
    private const val COLLECTION = "decisionSubmissions"
    private const val MIN_SAMPLE_SIZE = 4

    data class AggregateStats(
        val count: Long,
        val avgDays: Double,
        val granted: Long,
        val refused: Long,
    )

    suspend fun submit(
        context: Context,
        app: AppPreferences.SavedApplication,
        decisionDate: String,
        status: String,
    ): Result<Unit> = runCatching {
        val data = hashMapOf(
            "embassyId" to app.embassyId,
            "visaTypeName" to app.visaTypeName,
            "vacLabel" to app.vacLabel,
            "submissionDate" to app.submissionDate,
            "decisionDate" to decisionDate,
            "calendarDays" to calendarDays(app.submissionDate, decisionDate),
            "status" to status,
            "appVersionBucket" to appVersionBucket(context),
            "submittedAt" to FieldValue.serverTimestamp(),
        )
        Firebase.firestore.collection(COLLECTION).add(data).await()
        Unit
    }

    /** Returns null only when there are too few community submissions to show (< [MIN_SAMPLE_SIZE]). Throws on network/query failure — callers must distinguish the two. */
    suspend fun fetchAggregateStats(embassyId: String, visaTypeName: String): AggregateStats? {
        val base = Firebase.firestore.collection(COLLECTION)
            .whereEqualTo("embassyId", embassyId)
            .whereEqualTo("visaTypeName", visaTypeName)

        val total = base.count().get(AggregateSource.SERVER).await().count
        if (total < MIN_SAMPLE_SIZE) return null

        val avgField = AggregateField.average("calendarDays")
        val avg = base.aggregate(avgField).get(AggregateSource.SERVER).await().get(avgField) ?: 0.0
        val granted = base.whereEqualTo("status", "GRANTED")
            .count().get(AggregateSource.SERVER).await().count

        return AggregateStats(count = total, avgDays = avg, granted = granted, refused = total - granted)
    }

    internal fun calendarDays(submissionDate: String, decisionDate: String): Int =
        ChronoUnit.DAYS.between(LocalDate.parse(submissionDate), LocalDate.parse(decisionDate)).toInt()

    internal fun appVersionBucket(context: Context): String = runCatching {
        val versionName = context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "0.0.0"
        versionBucketFrom(versionName)
    }.getOrElse { "unknown" }

    internal fun versionBucketFrom(versionName: String): String {
        val parts = versionName.split(".")
        return if (parts.size >= 2) "${parts[0]}.${parts[1]}.x" else "$versionName.x"
    }
}
