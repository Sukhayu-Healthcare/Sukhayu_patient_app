package com.sukhayu.utils

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

/**
 * Small helper to set app locale at runtime.
 *
 * Usage:
 *   val newContext = LocaleHelper.setLocale(context, "mr")
 *
 * This returns a Context wrapped with the requested Locale's Configuration.
 */
object LocaleHelper {

    /**
     * Set the current locale for the provided [context] and return a wrapped Context
     * using the updated Configuration.
     *
     * This uses modern APIs (no deprecated updateConfiguration) and calls
     * Locale.setDefault(locale) before creating the configuration context.
     *
     * @param context base Context
     * @param languageCode ISO language code, e.g. "en", "mr" or variants like "en_US"
     * @return Context wrapped with the requested Locale configuration
     */
    fun setLocale(context: Context, languageCode: String): Context {
        val locale: Locale = when {
            languageCode.contains('_') || languageCode.contains('-') -> {
                // support language_country like en_US or en-US
                val parts = languageCode.replace('-', '_').split('_')
                if (parts.size >= 2) Locale(parts[0], parts[1]) else Locale(languageCode)
            }
            else -> Locale(languageCode)
        }

        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)

        return context.createConfigurationContext(config)
    }
}

