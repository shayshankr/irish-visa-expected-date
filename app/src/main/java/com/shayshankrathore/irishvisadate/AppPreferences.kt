package com.shayshankrathore.irishvisadate

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "app_prefs")

object AppPreferences {
    private val KEY_EMBASSY_ID = stringPreferencesKey("embassy_id")
    private val KEY_VAC_LABEL  = stringPreferencesKey("vac_label")
    private val KEY_SUBMISSION = stringPreferencesKey("submission_date")
    private val KEY_VISA_TYPE  = stringPreferencesKey("visa_type")

    data class SavedState(
        val embassyId: String?,
        val vacLabel: String?,
        val submissionDate: String?,
        val visaTypeName: String?,
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
}
