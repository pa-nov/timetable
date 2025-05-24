package com.panov.timetable.util

import android.content.Context
import android.content.res.Configuration
import android.icu.util.Calendar
import java.util.Locale

object ApplicationUtils {
    fun getLocalizedContext(context: Context): Context {
        val language = Storage.settings.getString(Storage.Application.LANGUAGE, Locale.getDefault().language)
        val locale = Locale(language)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)

        return context.createConfigurationContext(config)
    }

    fun getCalendar(): Calendar {
        val calendar = Calendar.getInstance()
        calendar.firstDayOfWeek = Calendar.MONDAY
        calendar.minimalDaysInFirstWeek = 4
        return calendar
    }

    fun getModifiedCalendar(): Calendar {
        val calendar = getCalendar()

        val modifierHours = Storage.settings.getInt(Storage.Widgets.MODIFIER_HOURS, 1)
        val modifierMinutes = Storage.settings.getInt(Storage.Widgets.MODIFIER_MINUTES, 1)
        val modifierSeconds = Storage.settings.getInt(Storage.Widgets.MODIFIER_SECONDS, 1)

        calendar.set(Calendar.HOUR_OF_DAY, if (modifierHours > 0) calendar.get(Calendar.HOUR_OF_DAY) / modifierHours * modifierHours else -modifierHours)
        calendar.set(Calendar.MINUTE, if (modifierMinutes > 0) calendar.get(Calendar.MINUTE) / modifierMinutes * modifierMinutes else -modifierMinutes)
        calendar.set(Calendar.SECOND, if (modifierSeconds > 0) calendar.get(Calendar.SECOND) / modifierSeconds * modifierSeconds else -modifierSeconds)

        return calendar
    }

    fun getTimetableData(jsonString: String): TimetableData? {
        return try {
            TimetableData(jsonString)
        } catch (_: Exception) {
            null
        }
    }
}