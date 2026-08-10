package com.shayshankrathore.irishvisadate

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import java.util.Locale

private val Context.dataStore by preferencesDataStore(name = "app_prefs")

object LocalizationManager {
    data class Language(
        val code: String,
        val displayName: String,
        val nativeName: String,
    )

    val SUPPORTED_LANGUAGES = listOf(
        Language("en", "English", "English"),
        Language("hi", "हिंदी", "हिंदी"),
        Language("ur", "اردو", "اردو"),
        Language("ar", "العربية", "العربية"),
        Language("ru", "Русский", "Русский"),
        Language("bn", "বাংলা", "বাংলা"),
        Language("zh", "中文", "中文"),
    )

    fun setAppLanguage(context: Context, languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = Configuration()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale)
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
        }

        @Suppress("DEPRECATION")
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }

    fun applyLanguageOnStartup(context: Context) {
        val languageCode = runBlocking {
            val key = stringPreferencesKey("app_language")
            context.dataStore.data.map { it[key] ?: "en" }.first()
        }
        setAppLanguage(context, languageCode)
    }

    fun getDisplayName(languageCode: String): String {
        return SUPPORTED_LANGUAGES.find { it.code == languageCode }?.displayName ?: "English"
    }

    fun getNativeName(languageCode: String): String {
        return SUPPORTED_LANGUAGES.find { it.code == languageCode }?.nativeName ?: "English"
    }

    fun restartApp(activity: Activity) {
        val intent = activity.intent
        activity.finish()
        activity.startActivity(intent)
    }
}
