package com.panov.timetable

import android.content.Context
import android.content.res.Configuration
import android.icu.util.Calendar
import com.panov.util.SettingsData
import java.util.Locale

object AppUtils {
    fun getLocalizedContext(context: Context, settings: SettingsData): Context {
        val language = settings.getString(Storage.Application.LANGUAGE, Locale.getDefault().language)
        val locale = Locale(language)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)

        return context.createConfigurationContext(config)
    }
}