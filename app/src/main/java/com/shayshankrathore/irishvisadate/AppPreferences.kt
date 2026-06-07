package com.shayshankrathore.irishvisadate

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.util.UUID

private val Context.dataStore by preferencesDataStore(name = "app_prefs")

object AppPreferences {
    private val KEY_EMBASSY_ID      = stringPreferencesKey("embassy_id")
    private val KEY_VAC_LABEL       = stringPreferencesKey("vac_label")
    private val KEY_SUBMISSION      = stringPreferencesKey("submission_date")
    private val KEY_VISA_TYPE       = stringPreferencesKey("visa_type")
    private val KEY_PASSPORT_EXPIRY = stringPreferencesKey("passport_expiry")
    private val KEY_BIOMETRIC_DATE  = stringPreferencesKey("biometric_date")
    private val KEY_SAVED_APPS      = stringPreferencesKey("saved_applications")
    private val KEY_ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
    private val KEY_NOTIF_WINDOW    = booleanPreferencesKey("notif_window_opens")
    private val KEY_NOTIF_MIDPOINT  = booleanPreferencesKey("notif_midpoint")
    private val KEY_NOTIF_OVERDUE   = booleanPreferencesKey("notif_overdue")
    private val KEY_NOTIF_WATCHDOG  = booleanPreferencesKey("notif_watchdog")
    private val KEY_DOCUMENTS       = stringPreferencesKey("tracked_documents")
    private val KEY_WATCHDOG_HASHES = stringPreferencesKey("watchdog_hashes")

    // ── Tracker state ─────────────────────────────────────────────────────────

    data class SavedState(
        val embassyId: String?,
        val vacLabel: String?,
        val submissionDate: String?,
        val visaTypeName: String?,
        val passportExpiry: String?,
        val biometricDate: String?,
    )

    fun flow(context: Context): Flow<SavedState> =
        context.dataStore.data.map { prefs ->
            SavedState(
                embassyId      = prefs[KEY_EMBASSY_ID],
                vacLabel       = prefs[KEY_VAC_LABEL],
                submissionDate = prefs[KEY_SUBMISSION],
                visaTypeName   = prefs[KEY_VISA_TYPE],
                passportExpiry = prefs[KEY_PASSPORT_EXPIRY],
                biometricDate  = prefs[KEY_BIOMETRIC_DATE],
            )
        }

    suspend fun save(
        context: Context,
        embassyId: String,
        vacLabel: String,
        submissionDate: String?,
        visaTypeName: String,
        passportExpiry: String? = null,
        biometricDate: String? = null,
    ) {
        context.dataStore.edit { prefs ->
            prefs[KEY_EMBASSY_ID] = embassyId
            prefs[KEY_VAC_LABEL]  = vacLabel
            if (submissionDate != null) prefs[KEY_SUBMISSION] = submissionDate
            else prefs.remove(KEY_SUBMISSION)
            prefs[KEY_VISA_TYPE] = visaTypeName
            if (passportExpiry != null) prefs[KEY_PASSPORT_EXPIRY] = passportExpiry
            else prefs.remove(KEY_PASSPORT_EXPIRY)
            if (biometricDate != null) prefs[KEY_BIOMETRIC_DATE] = biometricDate
            else prefs.remove(KEY_BIOMETRIC_DATE)
        }
    }

    // ── Notification settings ─────────────────────────────────────────────────

    data class NotificationSettings(
        val windowOpens: Boolean = true,
        val midpoint: Boolean    = true,
        val overdue: Boolean     = true,
        val watchdog: Boolean    = false,
    )

    fun notificationSettingsFlow(context: Context): Flow<NotificationSettings> =
        context.dataStore.data.map { prefs ->
            NotificationSettings(
                windowOpens = prefs[KEY_NOTIF_WINDOW]   ?: true,
                midpoint    = prefs[KEY_NOTIF_MIDPOINT] ?: true,
                overdue     = prefs[KEY_NOTIF_OVERDUE]  ?: true,
                watchdog    = prefs[KEY_NOTIF_WATCHDOG] ?: false,
            )
        }

    suspend fun saveNotificationSettings(context: Context, settings: NotificationSettings) {
        context.dataStore.edit { prefs ->
            prefs[KEY_NOTIF_WINDOW]   = settings.windowOpens
            prefs[KEY_NOTIF_MIDPOINT] = settings.midpoint
            prefs[KEY_NOTIF_OVERDUE]  = settings.overdue
            prefs[KEY_NOTIF_WATCHDOG] = settings.watchdog
        }
    }

    // ── Onboarding ────────────────────────────────────────────────────────────

    fun onboardingDoneFlow(context: Context): Flow<Boolean> =
        context.dataStore.data.map { prefs -> prefs[KEY_ONBOARDING_DONE] ?: false }

    suspend fun markOnboardingDone(context: Context) {
        context.dataStore.edit { prefs -> prefs[KEY_ONBOARDING_DONE] = true }
    }

    // ── Saved applications ────────────────────────────────────────────────────

    data class SavedApplication(
        val id: String,
        val embassyId: String,
        val embassyLabel: String,
        val embassyFlag: String,
        val vacLabel: String,
        val submissionDate: String,
        val visaTypeName: String,
        val visaTypeLabel: String,
        val status: String = "PENDING",
        val savedAt: String,
        val statusHistory: String = "",
        val notes: String = "",
        val decisionDate: String = "",
    )

    fun savedApplicationsFlow(context: Context): Flow<List<SavedApplication>> =
        context.dataStore.data.map { prefs ->
            prefs[KEY_SAVED_APPS]?.let { parseApps(it) } ?: emptyList()
        }

    suspend fun saveApplication(context: Context, app: SavedApplication) {
        context.dataStore.edit { prefs ->
            val current  = prefs[KEY_SAVED_APPS]?.let { parseApps(it) } ?: emptyList()
            val isNew    = current.none { it.id == app.id }
            val finalApp = if (isNew && app.statusHistory.isBlank())
                app.copy(statusHistory = historyEntry("Saved"))
            else app
            prefs[KEY_SAVED_APPS] = serializeApps(current.filterNot { it.id == app.id } + finalApp)
        }
    }

    suspend fun deleteApplication(context: Context, id: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[KEY_SAVED_APPS]?.let { parseApps(it) } ?: emptyList()
            prefs[KEY_SAVED_APPS] = serializeApps(current.filterNot { it.id == id })
        }
    }

    suspend fun updateApplicationStatus(context: Context, id: String, status: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[KEY_SAVED_APPS]?.let { parseApps(it) } ?: emptyList()
            val updated = current.map { app ->
                if (app.id == id) {
                    val event = when (status) {
                        "GRANTED" -> "Visa granted"
                        "REFUSED" -> "Visa refused"
                        else      -> "Status: $status"
                    }
                    app.copy(
                        status        = status,
                        statusHistory = appendHistory(app.statusHistory, event),
                    )
                } else app
            }
            prefs[KEY_SAVED_APPS] = serializeApps(updated)
        }
    }

    suspend fun updateApplicationNotes(context: Context, id: String, notes: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[KEY_SAVED_APPS]?.let { parseApps(it) } ?: emptyList()
            prefs[KEY_SAVED_APPS] = serializeApps(current.map { app ->
                if (app.id == id) app.copy(notes = notes) else app
            })
        }
    }

    suspend fun setApplicationDecisionDate(context: Context, id: String, decisionDate: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[KEY_SAVED_APPS]?.let { parseApps(it) } ?: emptyList()
            prefs[KEY_SAVED_APPS] = serializeApps(current.map { app ->
                if (app.id == id) app.copy(
                    decisionDate  = decisionDate,
                    statusHistory = appendHistory(app.statusHistory, "Decision recorded: $decisionDate"),
                ) else app
            })
        }
    }

    suspend fun exportApps(context: Context): String =
        context.dataStore.data.map { prefs -> prefs[KEY_SAVED_APPS] ?: "[]" }.first()

    suspend fun importApps(context: Context, json: String): Int {
        val imported = parseApps(json)
        if (imported.isEmpty()) return 0
        val current = context.dataStore.data
            .map { prefs -> prefs[KEY_SAVED_APPS]?.let { parseApps(it) } ?: emptyList() }
            .first()
        val newOnes = imported.filterNot { imp -> current.any { it.id == imp.id } }
        if (newOnes.isNotEmpty()) {
            context.dataStore.edit { prefs ->
                prefs[KEY_SAVED_APPS] = serializeApps(current + newOnes)
            }
        }
        return newOnes.size
    }

    fun newApplicationId(): String = UUID.randomUUID().toString()

    private fun historyEntry(event: String): String = "${LocalDate.now()}: $event"

    private fun appendHistory(existing: String, event: String): String {
        val entry = historyEntry(event)
        return if (existing.isBlank()) entry else "$existing|$entry"
    }

    // ── Document tracker ──────────────────────────────────────────────────────

    data class TrackedDocument(
        val id: String,
        val name: String,
        val category: String,
        val expiryDate: String,
        val reminderDays: Int = 30,
    )

    fun trackedDocumentsFlow(context: Context): Flow<List<TrackedDocument>> =
        context.dataStore.data.map { prefs ->
            prefs[KEY_DOCUMENTS]?.let { parseDocs(it) } ?: emptyList()
        }

    suspend fun saveDocument(context: Context, doc: TrackedDocument) {
        context.dataStore.edit { prefs ->
            val current = prefs[KEY_DOCUMENTS]?.let { parseDocs(it) } ?: emptyList()
            prefs[KEY_DOCUMENTS] = serializeDocs(current.filterNot { it.id == doc.id } + doc)
        }
    }

    suspend fun deleteDocument(context: Context, id: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[KEY_DOCUMENTS]?.let { parseDocs(it) } ?: emptyList()
            prefs[KEY_DOCUMENTS] = serializeDocs(current.filterNot { it.id == id })
        }
    }

    // ── Watchdog hashes ───────────────────────────────────────────────────────

    suspend fun getWatchdogHash(context: Context, embassyId: String): String? {
        val raw = context.dataStore.data.map { it[KEY_WATCHDOG_HASHES] ?: "" }.first()
        return raw.split(",").firstOrNull { it.startsWith("$embassyId::") }?.substringAfter("::")
    }

    suspend fun setWatchdogHash(context: Context, embassyId: String, hash: String) {
        context.dataStore.edit { prefs ->
            val raw     = prefs[KEY_WATCHDOG_HASHES] ?: ""
            val entries = raw.split(",").filter { it.isNotBlank() && !it.startsWith("$embassyId::") }
            prefs[KEY_WATCHDOG_HASHES] = (entries + "$embassyId::$hash").joinToString(",")
        }
    }

    // ── Serialization (apps) ──────────────────────────────────────────────────

    private fun serializeApps(apps: List<SavedApplication>): String = buildString {
        append("[")
        apps.forEachIndexed { i, a ->
            if (i > 0) append(",")
            append("{")
            append("\"id\":\"${a.id}\",")
            append("\"embassyId\":\"${a.embassyId}\",")
            append("\"embassyLabel\":\"${escape(a.embassyLabel)}\",")
            append("\"embassyFlag\":\"${escape(a.embassyFlag)}\",")
            append("\"vacLabel\":\"${escape(a.vacLabel)}\",")
            append("\"submissionDate\":\"${a.submissionDate}\",")
            append("\"visaTypeName\":\"${a.visaTypeName}\",")
            append("\"visaTypeLabel\":\"${escape(a.visaTypeLabel)}\",")
            append("\"status\":\"${a.status}\",")
            append("\"savedAt\":\"${a.savedAt}\",")
            append("\"statusHistory\":\"${escape(a.statusHistory)}\",")
            append("\"notes\":\"${escape(a.notes)}\",")
            append("\"decisionDate\":\"${a.decisionDate}\"")
            append("}")
        }
        append("]")
    }

    private fun escape(s: String) = s.replace("\\", "\\\\").replace("\"", "\\\"")

    private fun parseApps(json: String): List<SavedApplication> = runCatching {
        val trimmed = json.trim().removePrefix("[").removeSuffix("]")
        if (trimmed.isBlank()) return emptyList()
        splitJsonObjects(trimmed).mapNotNull { obj ->
            val f = parseJsonObject(obj)
            SavedApplication(
                id             = f["id"]            ?: return@mapNotNull null,
                embassyId      = f["embassyId"]     ?: return@mapNotNull null,
                embassyLabel   = f["embassyLabel"]  ?: "",
                embassyFlag    = f["embassyFlag"]   ?: "",
                vacLabel       = f["vacLabel"]      ?: "",
                submissionDate = f["submissionDate"]?: return@mapNotNull null,
                visaTypeName   = f["visaTypeName"]  ?: return@mapNotNull null,
                visaTypeLabel  = f["visaTypeLabel"] ?: "",
                status         = f["status"]        ?: "PENDING",
                savedAt        = f["savedAt"]       ?: "",
                statusHistory  = f["statusHistory"] ?: "",
                notes          = f["notes"]         ?: "",
                decisionDate   = f["decisionDate"]  ?: "",
            )
        }
    }.getOrElse { emptyList() }

    // ── Serialization (documents) ─────────────────────────────────────────────

    private fun serializeDocs(docs: List<TrackedDocument>): String = buildString {
        append("[")
        docs.forEachIndexed { i, d ->
            if (i > 0) append(",")
            append("{")
            append("\"id\":\"${d.id}\",")
            append("\"name\":\"${escape(d.name)}\",")
            append("\"category\":\"${d.category}\",")
            append("\"expiryDate\":\"${d.expiryDate}\",")
            append("\"reminderDays\":\"${d.reminderDays}\"")
            append("}")
        }
        append("]")
    }

    private fun parseDocs(json: String): List<TrackedDocument> = runCatching {
        val trimmed = json.trim().removePrefix("[").removeSuffix("]")
        if (trimmed.isBlank()) return emptyList()
        splitJsonObjects(trimmed).mapNotNull { obj ->
            val f = parseJsonObject(obj)
            TrackedDocument(
                id           = f["id"]           ?: return@mapNotNull null,
                name         = f["name"]         ?: "",
                category     = f["category"]     ?: "other",
                expiryDate   = f["expiryDate"]   ?: return@mapNotNull null,
                reminderDays = f["reminderDays"]?.toIntOrNull() ?: 30,
            )
        }
    }.getOrElse { emptyList() }

    private fun splitJsonObjects(s: String): List<String> {
        val result = mutableListOf<String>()
        var depth = 0; var start = -1
        s.forEachIndexed { i, c ->
            when (c) {
                '{' -> { if (depth++ == 0) start = i }
                '}' -> { if (--depth == 0 && start >= 0) { result.add(s.substring(start, i + 1)); start = -1 } }
            }
        }
        return result
    }

    private fun parseJsonObject(obj: String): Map<String, String> {
        val map   = mutableMapOf<String, String>()
        val inner = obj.trim().removePrefix("{").removeSuffix("}")
        val regex = Regex(""""(\w+)"\s*:\s*"((?:[^"\\]|\\.)*)"""")
        regex.findAll(inner).forEach { match ->
            map[match.groupValues[1]] = match.groupValues[2]
                .replace("\\\"", "\"").replace("\\\\", "\\")
        }
        return map
    }
}
