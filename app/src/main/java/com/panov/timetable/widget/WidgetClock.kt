package com.panov.timetable.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import com.panov.timetable.R
import com.panov.timetable.Tools
import org.json.JSONObject
import kotlin.math.abs

class WidgetClock : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) updateClockWidget(context, appWidgetManager, appWidgetId)
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        if (context != null && intent != null) {
            val action = (intent.action ?: "").split("|")
            if (action[0] == "update") updateClockWidget(context, AppWidgetManager.getInstance(context), action[1].toInt())
        }
    }

    private fun updateClockWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.widget_normal)
        val intent = Intent(context, WidgetClock::class.java)
        intent.action = "update|${appWidgetId}"
        views.setOnClickPendingIntent(
            R.id.background,
            PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
        )

        try {
            val data = context.getSharedPreferences("Timetable", 0)
            val timetable = JSONObject(data.getString("Json", "") ?: "")
            val date = Calendar.getInstance()
            date.firstDayOfWeek = Calendar.MONDAY
            date.minimalDaysInFirstWeek = 4

            val times = timetable.getJSONArray("times")
            val lessons = timetable.getJSONArray("lessons")
            val modHour = data.getInt("ModifierHour", 1)
            val modMinute = data.getInt("ModifierMinute", 1)
            val modSecond = data.getInt("ModifierSecond", 1)

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

            val timetableData = Tools.getTimetableData(timetable, time, dateDayOfWeek, dateWeekOddOrEven)

            val currentTimes = times.getJSONObject(timetableData.currentNumber)
            val currentTime = ((currentTimes.getInt("endHour") * 60 + currentTimes.getInt("endMinute")) * 60 +
                    (86400 * timetableData.currentDays)) - time
            val nextTimes = times.getJSONObject(timetableData.nextNumber)
            val nextTime = ((nextTimes.getInt("startHour") * 60 + nextTimes.getInt("startMinute")) * 60 +
                    (86400 * timetableData.nextDays)) - time

            views.setTextViewText(R.id.text_day, context.resources.getStringArray(R.array.weekdays)[dateDayOfWeek])
            views.setTextViewText(R.id.text_date, context.getString(R.string.placeholder_date, dateDay, dateMonth, dateWeek))
            views.setTextViewText(R.id.text_time, context.getString(R.string.placeholder_time, dateHour, dateMinute, dateSecond))

            views.setTextViewText(R.id.title_current, context.getString(if (currentTime < 0) R.string.earlier else R.string.now))
            views.setTextViewText(R.id.title_current_time, context.getString(if (currentTime < 0) R.string.earlier_time else R.string.now_time))

            val currentColor = ContextCompat.getColor(context, if (currentTime > 0) R.color.text else R.color.title)
            val nextColor = ContextCompat.getColor(context, if (currentTime > 0 || timetableData.nextDays > 0) R.color.title else R.color.text)

            views.setTextColor(R.id.text_current, currentColor)
            views.setTextColor(R.id.text_current_other, currentColor)
            views.setTextColor(R.id.text_current_time, currentColor)
            views.setTextColor(R.id.text_next, nextColor)
            views.setTextColor(R.id.text_next_other, nextColor)
            views.setTextColor(R.id.text_next_time, nextColor)

            val currentLesson = lessons.getJSONArray(timetableData.currentId)
            views.setTextViewText(R.id.text_current, currentLesson.getString(0))
            views.setTextViewText(R.id.text_current_other, context.getString(R.string.placeholder_room, currentLesson.getString(1)))
            views.setTextViewText(R.id.text_current_time, Tools.getTimeText(currentTime.toDouble(), context.resources))

            val nextLesson = lessons.getJSONArray(timetableData.nextId)
            views.setTextViewText(R.id.text_next, nextLesson.getString(0))
            views.setTextViewText(R.id.text_next_other, context.getString(R.string.placeholder_room, nextLesson.getString(1)))
            views.setTextViewText(R.id.text_next_time, Tools.getTimeText(nextTime.toDouble(), context.resources))
        } catch (e: Exception) {
            views.setTextViewText(R.id.text_day, context.getString(R.string.error))
        }

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}