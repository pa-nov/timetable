package com.panov.timetable

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.widget.RemoteViews
import org.json.JSONObject
import kotlin.math.abs

class WidgetSmall : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) updateSmallWidget(context, appWidgetManager, appWidgetId)
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        if (context != null && intent != null) {
            val action = (intent.action ?: "").split("|")
            if (action[0] == "update") updateSmallWidget(context, AppWidgetManager.getInstance(context), action[1].toInt())
        }
    }
}

internal fun updateSmallWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
    val views = RemoteViews(context.packageName, R.layout.widget_small)
    val intent = Intent(context, WidgetSmall::class.java)
    intent.action = "update|${appWidgetId}"
    views.setOnClickPendingIntent(
        R.id.background,
        PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
    )

    try {
        val savedData = context.getSharedPreferences("SavedTimetable", 0)
        val data = JSONObject(savedData.getString("Json", "") ?: "")
        val date = Calendar.getInstance()
        date.firstDayOfWeek = Calendar.MONDAY
        date.minimalDaysInFirstWeek = 4

        val times = data.getJSONArray("times")
        val lessons = data.getJSONArray("lessons")
        val modHour = savedData.getInt("ModifierHour", 1)
        val modMinute = savedData.getInt("ModifierMinute", 1)
        val modSecond = savedData.getInt("ModifierSecond", 1)

        val dateDayOfWeek = if (date.get(Calendar.DAY_OF_WEEK) > 1) date.get(Calendar.DAY_OF_WEEK) - 2 else 6
        val dateWeekOddOrEven = if (date.get(Calendar.WEEK_OF_YEAR) % 2 == 0) "even" else "odd"
        val dateDay = Tools.getTwoDigitNumber(date.get(Calendar.DAY_OF_MONTH))
        val dateMonth = Tools.getTwoDigitNumber(date.get(Calendar.MONTH) + 1)
        val dateWeek = Tools.getTwoDigitNumber(date.get(Calendar.WEEK_OF_YEAR))

        val dateHour = if (modHour <= 0) Tools.getTwoDigitNumber(abs(modHour))
        else Tools.getTwoDigitNumber((date.get(Calendar.HOUR_OF_DAY) / modHour) * modHour)
        val dateMinute = if (modMinute <= 0) Tools.getTwoDigitNumber(abs(modMinute))
        else Tools.getTwoDigitNumber((date.get(Calendar.MINUTE) / modMinute) * modMinute)
        val dateSecond = if (modSecond <= 0) Tools.getTwoDigitNumber(abs(modSecond))
        else Tools.getTwoDigitNumber((date.get(Calendar.SECOND) / modSecond) * modSecond)
        val time = (dateHour.toInt() * 60 + dateMinute.toInt()) * 60 + dateSecond.toInt()

        val timetableData = Tools.getTimetableData(data, time, dateDayOfWeek, dateWeekOddOrEven)

        val currentTimes = times.getJSONObject(timetableData.currentNumber)
        val currentTime =
            ((currentTimes.getInt("endHour") * 60 + currentTimes.getInt("endMinute")) * 60 + (86400 * timetableData.currentDays)) - time

        views.setTextViewText(R.id.text_day, context.resources.getStringArray(R.array.weekdays)[dateDayOfWeek])
        views.setTextViewText(R.id.text_date, context.getString(R.string.placeholder_date, dateDay, dateMonth, dateWeek))
        views.setTextViewText(R.id.text_time, context.getString(R.string.placeholder_time, dateHour, dateMinute, dateSecond))

        views.setTextViewText(R.id.title_current, context.getString(if (currentTime > 0) R.string.now else R.string.then))

        val lessonId = lessons.getJSONArray(if (currentTime > 0) timetableData.currentId else timetableData.nextId)
        views.setTextViewText(R.id.text_current, lessonId.getString(0))
        views.setTextViewText(R.id.text_current_other, context.getString(R.string.placeholder_room, lessonId.getString(1)))
    } catch (e: Exception) {
        views.setTextViewText(R.id.text_day, context.getString(R.string.error))
    }

    appWidgetManager.updateAppWidget(appWidgetId, views)
}