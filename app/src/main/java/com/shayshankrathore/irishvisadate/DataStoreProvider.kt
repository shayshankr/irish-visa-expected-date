package com.shayshankrathore.irishvisadate

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

private val Context.dataStore by preferencesDataStore(name = "app_prefs")

object DataStoreProvider {
    fun getDataStore(context: Context) = context.dataStore
}
