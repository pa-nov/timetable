package com.panov.timetable.appwidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.format.DateUtils
import android.widget.RemoteViews
import com.panov.timetable.AppUtils
import com.panov.timetable.R
import com.panov.timetable.Storage
import com.panov.util.Converter
import com.panov.util.SettingsData

class ClockWidgetProvider : AppWidgetProvider() {
    override fun onAppWidgetOptionsChanged(context: Context?, appWidgetManager: AppWidgetManager?, appWidgetId: Int, newOptions: Bundle?) {
        if (context != null && appWidgetManager != null && newOptions != null) {
            AppUtils.resizeWidget(context, appWidgetManager, appWidgetId, newOptions)
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateWidget(sourceContext: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val settings = SettingsData(sourceContext)
        val calendar = AppUtils.getModifiedCalendar(settings)
        val context = AppUtils.getLocalizedContext(sourceContext, settings)
        val views = RemoteViews(context.packageName, R.layout.widget_clock)
        val day = Converter.getDayOfWeek(calendar)
        val combineBackground = settings.getBoolean(Storage.Widgets.COMBINE_BACKGROUND)


        views.setInt(R.id.layout_background, "setBackgroundResource", if (combineBackground) R.drawable.square_rounded_small else 0)
        views.setTextViewText(R.id.title_weekday, context.resources.getStringArray(R.array.weekdays)[day])
        views.setTextViewText(R.id.text_date, Converter.getDateText(calendar, true))
        views.setTextViewText(R.id.text_time, Converter.getTimeText(calendar))

        val timetable = AppUtils.getTimetableData(settings.getString(Storage.Timetable.JSON))

        if (timetable != null) {
            val offset = timetable.getOffset(calendar)
            val seconds = Converter.getSecondsInDay(calendar)
            val daySeconds = (DateUtils.DAY_IN_MILLIS / 1000).toInt()
            val timerAgo = "  " + context.getString(R.string.timer_ago)


            val currentLessonEnd = timetable.getLessonTimeEnd(offset.currentLessonIndex)
            val currentLessonId = timetable.getLessonId(offset.currentWeek, offset.currentDay, offset.currentLessonIndex)
            val isCurrentLessonNow = (offset.currentDaysOffset == 0 && currentLessonEnd > seconds)
            val currentLessonTime = if (isCurrentLessonNow) Converter.getTimeText(currentLessonEnd - seconds, context)
            else Converter.getTimeText(-(offset.currentDaysOffset * daySeconds + currentLessonEnd - seconds), context) + timerAgo

            views.setTextViewText(R.id.title_current_lesson, context.getString(if (isCurrentLessonNow) R.string.timer_now else R.string.timer_earlier))
            views.setTextViewText(R.id.text_current_lesson, timetable.getLessonShortTitle(currentLessonId))
            views.setTextViewText(R.id.text_current_classroom, timetable.getClassroomText(currentLessonId))
            views.setTextViewText(R.id.title_current_time, context.getString(if (isCurrentLessonNow) R.string.timer_ends_in else R.string.timer_ended))
            views.setTextViewText(R.id.text_current_time, currentLessonTime)

            val currentLessonColor = context.getColor(if (isCurrentLessonNow) R.color.text else R.color.title)
            views.setTextColor(R.id.text_current_lesson, currentLessonColor)
            views.setTextColor(R.id.text_current_classroom, currentLessonColor)
            views.setTextColor(R.id.text_current_time, currentLessonColor)


            val nextLessonStart = timetable.getLessonTimeStart(offset.nextLessonIndex)
            val nextLessonId = timetable.getLessonId(offset.nextWeek, offset.nextDay, offset.nextLessonIndex)
            val isNextLessonToday = offset.nextDaysOffset == 0
            val nextLessonTime = Converter.getTimeText(offset.nextDaysOffset * daySeconds + nextLessonStart - seconds, context)

            views.setTextViewText(R.id.title_next_lesson, context.getString(R.string.timer_then))
            views.setTextViewText(R.id.text_next_lesson, timetable.getLessonShortTitle(nextLessonId))
            views.setTextViewText(R.id.text_next_classroom, timetable.getClassroomText(nextLessonId))
            views.setTextViewText(R.id.title_next_time, context.getString(R.string.timer_starts_in))
            views.setTextViewText(R.id.text_next_time, nextLessonTime)

            val nextLessonColor = context.getColor(if (!isCurrentLessonNow && isNextLessonToday) R.color.text else R.color.title)
            views.setTextColor(R.id.text_next_lesson, nextLessonColor)
            views.setTextColor(R.id.text_next_classroom, nextLessonColor)
            views.setTextColor(R.id.text_next_time, nextLessonColor)
        } else {
            views.setTextViewText(R.id.title_current_lesson, context.getString(R.string.message_error))
            views.setTextViewText(R.id.text_current_lesson, "")
            views.setTextViewText(R.id.text_current_classroom, "")
            views.setTextViewText(R.id.title_current_time, "")
            views.setTextViewText(R.id.text_current_time, "")

            views.setTextViewText(R.id.title_next_lesson, "")
            views.setTextViewText(R.id.text_next_lesson, "")
            views.setTextViewText(R.id.text_next_classroom, "")
            views.setTextViewText(R.id.title_next_time, "")
            views.setTextViewText(R.id.text_next_time, "")
        }


        val intent = Intent(context, ClockWidgetProvider::class.java)
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, arrayOf(appWidgetId).toIntArray())
        val pendingIntent = PendingIntent.getBroadcast(context, appWidgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        views.setOnClickPendingIntent(R.id.layout_background, pendingIntent)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}