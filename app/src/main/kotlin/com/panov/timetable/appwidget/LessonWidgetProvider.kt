package com.panov.timetable.appwidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.panov.timetable.AppUtils
import com.panov.timetable.R
import com.panov.timetable.Storage
import com.panov.util.Converter
import com.panov.util.SettingsData

class LessonWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateWidget(sourceContext: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val settings = SettingsData(sourceContext)
        val calendar = AppUtils.getModifiedCalendar(settings)
        val context = AppUtils.getLocalizedContext(sourceContext, settings)
        val views = RemoteViews(context.packageName, R.layout.widget_lesson)
        val day = Converter.getDayOfWeek(calendar)


        views.setTextViewText(R.id.title_weekday, context.resources.getStringArray(R.array.weekdays)[day])
        views.setTextViewText(R.id.text_date, Converter.getDateText(calendar, true))
        views.setTextViewText(R.id.text_time, Converter.getTimeText(calendar))

        val timetable = AppUtils.getTimetableData(settings.getString(Storage.Timetable.JSON))

        if (timetable != null) {
            val offset = timetable.getOffset(calendar)
            val seconds = Converter.getSecondsInDay(calendar)
            val currentLessonEnd = timetable.getLessonTimeEnd(offset.currentLessonIndex)

            if (offset.currentDaysOffset == 0 && currentLessonEnd > seconds) {
                val currentLessonId = timetable.getLessonId(offset.currentWeek, offset.currentDay, offset.currentLessonIndex)
                views.setTextViewText(R.id.title_current_lesson, context.getString(R.string.timer_now))
                views.setTextViewText(R.id.text_current_lesson, timetable.getLessonShortTitle(currentLessonId))
                views.setTextViewText(R.id.text_current_classroom, timetable.getClassroomText(currentLessonId))
            } else {
                val nextLessonId = timetable.getLessonId(offset.nextWeek, offset.nextDay, offset.nextLessonIndex)
                views.setTextViewText(R.id.title_current_lesson, context.getString(R.string.timer_then))
                views.setTextViewText(R.id.text_current_lesson, timetable.getLessonShortTitle(nextLessonId))
                views.setTextViewText(R.id.text_current_classroom, timetable.getClassroomText(nextLessonId))
            }
        } else {
            views.setTextViewText(R.id.title_current_lesson, context.getString(R.string.message_error))
            views.setTextViewText(R.id.text_current_lesson, "")
            views.setTextViewText(R.id.text_current_classroom, "")
        }


        val intent = Intent(context, LessonWidgetProvider::class.java)
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, arrayOf(appWidgetId).toIntArray())
        val pendingIntent = PendingIntent.getBroadcast(context, appWidgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        views.setOnClickPendingIntent(R.id.layout_background, pendingIntent)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}