package com.panov.timetable

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.widget.RemoteViews
import org.json.JSONObject
import kotlin.math.abs
import kotlin.math.floor

class TimetableWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)

        val action = intent!!.action ?: ""

        if (context != null && action == "updateWidget"){
            updateWidgets(context)
        }
    }

    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.timetable_widget)
        views.setOnClickPendingIntent(R.id.buttonUpdate, pendingIntent(context, "updateWidget"))


        val savedData = context.getSharedPreferences("SavedData", 0)
        val jsonDataString = savedData.getString("Json", "")
        if (jsonDataString.isNullOrEmpty()) {
            appWidgetManager.updateAppWidget(appWidgetId, views)
            return
        }
        val jsonData = JSONObject(jsonDataString)
        val date = Calendar.getInstance()
        date.firstDayOfWeek = Calendar.MONDAY
        date.minimalDaysInFirstWeek = 4

        val times       = jsonData.getJSONArray("times")
        val lessons     = jsonData.getJSONArray("lessons")
        val modHour     = savedData.getInt("ModifierHour", 1)
        val modMinute   = savedData.getInt("ModifierMinute", 1)
        val modSecond   = savedData.getInt("ModifierSecond", 1)

        val weekdays    = arrayOf( context.getString(R.string.weekday_monday), context.getString(R.string.weekday_tuesday), context.getString(R.string.weekday_wednesday), context.getString(R.string.weekday_thursday), context.getString(R.string.weekday_friday), context.getString(R.string.weekday_saturday), context.getString(R.string.weekday_sunday) )
        val dateWeekDay = if (date.get(Calendar.DAY_OF_WEEK) > 1) { date.get(Calendar.DAY_OF_WEEK) - 2 } else { 6 }
        val dateWeek    = getTwoDigitNumber(date.get(Calendar.WEEK_OF_YEAR))
        val dateDay     = getTwoDigitNumber(date.get(Calendar.DAY_OF_MONTH))
        val dateMonth   = getTwoDigitNumber(date.get(Calendar.MONTH) + 1)
        val dateHour    = if (modHour > 0)
        { getTwoDigitNumber((date.get(Calendar.HOUR_OF_DAY) / modHour) * modHour) } else
        { getTwoDigitNumber(abs(modHour)) }
        val dateMinute  = if (modMinute > 0)
        { getTwoDigitNumber((date.get(Calendar.MINUTE) / modMinute) * modMinute) } else
        { getTwoDigitNumber(abs(modMinute)) }
        val dateSecond  = if (modSecond > 0)
        { getTwoDigitNumber((date.get(Calendar.SECOND) / modSecond) * modSecond) } else
        { getTwoDigitNumber(abs(modSecond)) }

        val weekEvenOdd = if (date.get(Calendar.WEEK_OF_YEAR) % 2 == 0) { "even" } else { "odd" }
        val timesMax = times.length() - 1
        val nowTime = (dateHour.toInt() * 60 + dateMinute.toInt()) * 60 + dateSecond.toInt()


        var nowLesson = -1
        var thenLesson = 0
        for (i in 0 .. timesMax) {
            val savedTimeJson = times.getJSONObject(i)
            val savedTime = (savedTimeJson.getInt("startHour") * 60 + savedTimeJson.getInt("startMinute")) * 60
            if (nowTime > savedTime) {
                nowLesson = i
                thenLesson = i + 1
            }
        }

        var nowDayTimetable = jsonData.getJSONArray(weekEvenOdd).getJSONArray(dateWeekDay)
        var nowWeekEvenOdd = weekEvenOdd
        var nowDateWeekDay = dateWeekDay
        var nowDaysCount = 0
        var nowWhileCount = 14 * (timesMax + 1)
        while (nowWhileCount > 0) {
            if (nowLesson < 0) {
                nowDaysCount--

                if (nowDateWeekDay < 1) {
                    nowWeekEvenOdd = if (nowWeekEvenOdd == "odd") { "even" } else { "odd" }
                    nowDateWeekDay = 6
                } else {
                    nowDateWeekDay--
                }

                nowDayTimetable = jsonData.getJSONArray(nowWeekEvenOdd).getJSONArray(nowDateWeekDay)
                nowLesson = timesMax
            }

            if (nowDayTimetable.getInt(nowLesson) > 0) {
                nowWhileCount = 0
            } else {
                nowWhileCount--
                if (nowWhileCount > 0) { nowLesson-- }
            }
        }

        var thenDayTimetable = jsonData.getJSONArray(weekEvenOdd).getJSONArray(dateWeekDay)
        var thenWeekEvenOdd = weekEvenOdd
        var thenDateWeekDay = dateWeekDay
        var thenDaysCount = 0
        var thenWhileCount = 14 * (timesMax + 1)
        while (thenWhileCount > 0) {
            if (thenLesson > timesMax) {
                thenDaysCount++

                if (thenDateWeekDay > 5) {
                    thenWeekEvenOdd = if (thenWeekEvenOdd == "odd") { "even" } else { "odd" }
                    thenDateWeekDay = 0
                } else {
                    thenDateWeekDay++
                }

                thenDayTimetable = jsonData.getJSONArray(thenWeekEvenOdd).getJSONArray(thenDateWeekDay)
                thenLesson = 0
            }

            if (thenDayTimetable.getInt(thenLesson) > 0) {
                thenWhileCount = 0
            } else {
                thenWhileCount--
                if (thenWhileCount > 0) { thenLesson++ }
            }
        }

        val nowLessonTimes = times.getJSONObject(nowLesson)
        val endTime = ((nowLessonTimes.getInt("endHour") * 60 + nowLessonTimes.getInt("endMinute")) * 60) + (86400 * nowDaysCount) - nowTime
        val thenLessonTimes = times.getJSONObject(thenLesson)
        val startTime = ((thenLessonTimes.getInt("startHour") * 60 + thenLessonTimes.getInt("startMinute")) * 60) + (86400 * thenDaysCount) - nowTime


        if (endTime < 0) {
            views.setTextViewText(R.id.textWidgetNowTimeTitle, context.getString(R.string.widget_end))
        } else {
            views.setTextViewText(R.id.textWidgetNowTimeTitle, context.getString(R.string.widget_end_in))
        }

        views.setTextViewText(R.id.textWidgetDayOfWeek, weekdays[dateWeekDay])
        views.setTextViewText(R.id.textWidgetDate, "$dateDay.$dateMonth ($dateWeek)")
        views.setTextViewText(R.id.textWidgetTime, "$dateHour:$dateMinute:$dateSecond")

        views.setTextViewText(R.id.textWidgetNowText, lessons.getJSONArray(nowDayTimetable.getInt(nowLesson)).getString(0))
        views.setTextViewText(R.id.textWidgetNowSubText, "(${lessons.getJSONArray(nowDayTimetable.getInt(nowLesson)).getString(1)})")
        views.setTextViewText(R.id.textWidgetNowTimeText, getNiceTime(context, endTime))

        views.setTextViewText(R.id.textWidgetThenText, lessons.getJSONArray(thenDayTimetable.getInt(thenLesson)).getString(0))
        views.setTextViewText(R.id.textWidgetThenSubText, "(${lessons.getJSONArray(thenDayTimetable.getInt(thenLesson)).getString(1)})")
        views.setTextViewText(R.id.textWidgetThenTimeText, getNiceTime(context, startTime))

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun pendingIntent(context: Context, action: String): PendingIntent? {
        val intent = Intent(context, javaClass)
        intent.action = action

        return PendingIntent.getBroadcast(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }

    private fun updateWidgets(context: Context) {
        val manager = AppWidgetManager.getInstance(context)
        val ids = manager.getAppWidgetIds(ComponentName(context, javaClass))

        ids.forEach { id -> updateAppWidget(context, manager, id) }
    }


    private fun getTwoDigitNumber(number: Int): String {
        if (number < 10) { return "0$number" }
        return number.toString()
    }

    private fun getNiceTime(context: Context, timeSource: Int): String {
        val postfix = if (timeSource < 0) { "  ${context.getString(R.string.time_ago)}" } else { "" }
        val time    = if (timeSource < 0) { abs(timeSource) } else { timeSource }

        val hours   = floor(time.toDouble() / 3600)
        val minutes = floor((time.toDouble() - (hours * 3600)) / 60)
        val seconds = time - (hours * 60 + minutes) * 60

        val hoursText   = if (hours > 0)
        { "${hours.toInt()} ${getNumberText(hours, context.getString(R.string.time_hours_single), context.getString(R.string.time_hours_small), context.getString(R.string.time_hours_large))}  " } else
        { "" }
        val minutesText = if (hours > 0)
        { "${minutes.toInt()} ${context.getString(R.string.time_minutes)}" } else
        { "${minutes.toInt()} ${getNumberText(minutes, context.getString(R.string.time_minutes_single), context.getString(R.string.time_minutes_small), context.getString(R.string.time_minutes_large))}" }
        val secondsText = if (hours > 0)
        { "${seconds.toInt()} ${context.getString(R.string.time_seconds)}" } else
        { "${seconds.toInt()} ${getNumberText(seconds, context.getString(R.string.time_seconds_single), context.getString(R.string.time_seconds_small), context.getString(R.string.time_seconds_large))}" }

        return "$hoursText$minutesText  $secondsText$postfix"
    }

    private fun getNumberText(number: Double, textSingle: String, textSmall: String, textLarge: String): String {
        if (floor(number / 10) % 10 != 1.0) {
            if (number % 10 == 1.0) {
                return textSingle
            }
            if (number % 10 == 2.0 || number % 10 == 3.0 || number % 10 == 4.0) {
                return textSmall
            }
        }
        return textLarge
    }
}