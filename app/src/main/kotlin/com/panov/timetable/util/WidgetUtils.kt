package com.panov.timetable.util

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.panov.timetable.appwidget.ClockWidgetProvider
import com.panov.timetable.appwidget.LessonWidgetProvider
import com.panov.timetable.appwidget.TimetableWidgetProvider
import com.panov.timetable.appwidget.WidgetService

object WidgetUtils {
    fun startWidgetService(context: Context, settings: SettingsData = SettingsData(context)) {
        val updateOnUnlock = settings.getBoolean(Storage.Widgets.UPDATE_ON_UNLOCK)
        val updateByTimetable = settings.getBoolean(Storage.Widgets.UPDATE_BY_TIMETABLE)
        val updateByTimer = settings.getInt(Storage.Widgets.UPDATE_TIMER) > 0

        if (updateOnUnlock || updateByTimetable || updateByTimer) {
            context.startForegroundService(Intent(context, WidgetService::class.java))
        }
    }

    fun updateWidgets(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)

        val lessonWidgetIds = appWidgetManager.getAppWidgetIds(ComponentName(context, LessonWidgetProvider::class.java))
        val clockWidgetIds = appWidgetManager.getAppWidgetIds(ComponentName(context, ClockWidgetProvider::class.java))
        val timetableWidgetIds = appWidgetManager.getAppWidgetIds(ComponentName(context, TimetableWidgetProvider::class.java))

        LessonWidgetProvider().onUpdate(context, appWidgetManager, lessonWidgetIds)
        ClockWidgetProvider().onUpdate(context, appWidgetManager, clockWidgetIds)
        TimetableWidgetProvider().onUpdate(context, appWidgetManager, timetableWidgetIds)
    }
}