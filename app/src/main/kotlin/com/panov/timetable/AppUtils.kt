package com.panov.timetable

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.res.Configuration
import android.icu.util.Calendar
import android.os.Bundle
import com.panov.timetable.appwidget.ClockWidgetProvider
import com.panov.timetable.appwidget.LessonWidgetProvider
import com.panov.timetable.appwidget.TimetableWidgetProvider
import com.panov.util.SettingsData
import com.panov.util.TimetableData
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

    fun getModifiedCalendar(settings: SettingsData): Calendar {
        val calendar = Calendar.getInstance()
        calendar.firstDayOfWeek = Calendar.MONDAY
        calendar.minimalDaysInFirstWeek = 4

        val modifierHour = settings.getInt(Storage.Widgets.MODIFIER_HOUR, 1)
        val modifierMinute = settings.getInt(Storage.Widgets.MODIFIER_MINUTE, 1)
        val modifierSecond = settings.getInt(Storage.Widgets.MODIFIER_SECOND, 1)

        calendar.set(Calendar.HOUR_OF_DAY, if (modifierHour > 0) calendar.get(Calendar.HOUR_OF_DAY) / modifierHour * modifierHour else -modifierHour)
        calendar.set(Calendar.MINUTE, if (modifierMinute > 0) calendar.get(Calendar.MINUTE) / modifierMinute * modifierMinute else -modifierMinute)
        calendar.set(Calendar.SECOND, if (modifierSecond > 0) calendar.get(Calendar.SECOND) / modifierSecond * modifierSecond else -modifierSecond)

        return calendar
    }

    fun getTimetableData(jsonString: String): TimetableData? {
        return try {
            TimetableData(jsonString)
        } catch (_: Exception) {
            null
        }
    }

    fun resizeWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int, options: Bundle) {
        val height = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)
        if (height >= 160) {
            TimetableWidgetProvider().onUpdate(context, appWidgetManager, arrayOf(appWidgetId).toIntArray())
        } else if (height >= 80) {
            ClockWidgetProvider().onUpdate(context, appWidgetManager, arrayOf(appWidgetId).toIntArray())
        } else {
            LessonWidgetProvider().onUpdate(context, appWidgetManager, arrayOf(appWidgetId).toIntArray())
        }
    }
}