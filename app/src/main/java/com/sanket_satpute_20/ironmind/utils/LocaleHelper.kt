package com.sanket_satpute_20.ironmind.utils

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LocaleHelper {

    fun applyLanguage(context: Context, languageCode: String): Context {
        val locale = Locale.forLanguageTag(languageCode)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)

        return context.createConfigurationContext(config)
    }

    fun isRtl(languageCode: String): Boolean {
        // Arabic and Urdu are RTL
        return languageCode in listOf("ar", "ur")
    }
}