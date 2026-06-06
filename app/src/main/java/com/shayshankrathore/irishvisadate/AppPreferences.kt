package com.shayshankrathore.irishvisadate

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

private val Context.dataStore by preferencesDataStore(name = "app_prefs")

object AppPreferences {
    private val KEY_EMBASSY_ID = stringPreferencesKey("embassy_id")
    private val KEY_VAC_LABEL  = stringPreferencesKey("vac_label")
    private val KEY_SUBMISSION = stringPreferencesKey("submission_date")
    private val KEY_VISA_TYPE  = stringPreferencesKey("visa_type")
    private val KEY_SAVED_APPS = stringPreferencesKey("saved_applications")

    data class SavedState(
        val embassyId: String?,
        val vacLabel: String?,
        val submissionDate: String?,
        val visaTypeName: String?,
    )

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
    )

    fun flow(context: Context): Flow<SavedState> =
        context.dataStore.data.map { prefs ->
            SavedState(
                embassyId      = prefs[KEY_EMBASSY_ID],
                vacLabel       = prefs[KEY_VAC_LABEL],
                submissionDate = prefs[KEY_SUBMISSION],
                visaTypeName   = prefs[KEY_VISA_TYPE],
            )
        }

    suspend fun save(
        context: Context,
        embassyId: String,
        vacLabel: String,
        submissionDate: String?,
        visaTypeName: String,
    ) {
        context.dataStore.edit { prefs ->
            prefs[KEY_EMBASSY_ID] = embassyId
            prefs[KEY_VAC_LABEL]  = vacLabel
            if (submissionDate != null) prefs[KEY_SUBMISSION] = submissionDate
            else prefs.remove(KEY_SUBMISSION)
            prefs[KEY_VISA_TYPE] = visaTypeName
        }
    }

    // ── Saved applications ────────────────────────────────────────────────────

    fun savedApplicationsFlow(context: Context): Flow<List<SavedApplication>> =
        context.dataStore.data.map { prefs ->
            prefs[KEY_SAVED_APPS]?.let { parseApps(it) } ?: emptyList()
        }

    suspend fun saveApplication(context: Context, app: SavedApplication) {
        context.dataStore.edit { prefs ->
            val current = prefs[KEY_SAVED_APPS]?.let { parseApps(it) } ?: emptyList()
            val updated = current.filterNot { it.id == app.id } + app
            prefs[KEY_SAVED_APPS] = serializeApps(updated)
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
            val updated = current.map { if (it.id == id) it.copy(status = status) else it }
            prefs[KEY_SAVED_APPS] = serializeApps(updated)
        }
    }

    fun newApplicationId(): String = UUID.randomUUID().toString()

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
            append("\"savedAt\":\"${a.savedAt}\"")
            append("}")
        }
        append("]")
    }

    private fun escape(s: String) = s.replace("\\", "\\\\").replace("\"", "\\\"")

    private fun parseApps(json: String): List<SavedApplication> = runCatching {
        val trimmed = json.trim().removePrefix("[").removeSuffix("]")
        if (trimmed.isBlank()) return emptyList()
        val objects = splitJsonObjects(trimmed)
        objects.mapNotNull { obj ->
            val fields = parseJsonObject(obj)
            SavedApplication(
                id             = fields["id"]            ?: return@mapNotNull null,
                embassyId      = fields["embassyId"]     ?: return@mapNotNull null,
                embassyLabel   = fields["embassyLabel"]  ?: "",
                embassyFlag    = fields["embassyFlag"]   ?: "",
                vacLabel       = fields["vacLabel"]      ?: "",
                submissionDate = fields["submissionDate"]?: return@mapNotNull null,
                visaTypeName   = fields["visaTypeName"]  ?: return@mapNotNull null,
                visaTypeLabel  = fields["visaTypeLabel"] ?: "",
                status         = fields["status"]        ?: "PENDING",
                savedAt        = fields["savedAt"]       ?: "",
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
        val map = mutableMapOf<String, String>()
        val inner = obj.trim().removePrefix("{").removeSuffix("}")
        val regex = Regex(""""(\w+)"\s*:\s*"((?:[^"\\]|\\.)*)"""")
        regex.findAll(inner).forEach { match ->
            map[match.groupValues[1]] = match.groupValues[2]
                .replace("\\\"", "\"").replace("\\\\", "\\")
        }
        return map
    }
}
