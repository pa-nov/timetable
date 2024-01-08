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

class TimetableWidgetSmall : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        if (context != null && intent != null) {
            val action = (intent.action ?: "").split("|")

            if (action[0] == "updateSmallWidget") {
                updateWidget(context, AppWidgetManager.getInstance(context), action[1].toInt())
            }
        }
    }

    private fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.timetable_widget_small)
        views.setOnClickPendingIntent(R.id.widgetUpdate, pendingIntent(context, "updateSmallWidget|$appWidgetId"))
        try {
            val savedData      = context.getSharedPreferences("SavedData", 0)
            val jsonDataString = savedData.getString("Json", "")
            val jsonData       = JSONObject(jsonDataString!!)
            val date           = Calendar.getInstance()
            date.firstDayOfWeek         = Calendar.MONDAY
            date.minimalDaysInFirstWeek = 4

            val times     = jsonData.getJSONArray("times")
            val lessons   = jsonData.getJSONArray("lessons")
            val modHour   = savedData.getInt("ModifierHour", 1)
            val modMinute = savedData.getInt("ModifierMinute", 1)
            val modSecond = savedData.getInt("ModifierSecond", 1)

            val dateWeekOddOrEven = if (date.get(Calendar.WEEK_OF_YEAR) % 2 == 0) { "even" } else { "odd" }
            val dateDayOfWeek     = if (date.get(Calendar.DAY_OF_WEEK) > 1) { date.get(Calendar.DAY_OF_WEEK) - 2 } else { 6 }
            val dateDay           = Tools.getTwoDigitNumber(date.get(Calendar.DAY_OF_MONTH))
            val dateMonth         = Tools.getTwoDigitNumber(date.get(Calendar.MONTH) + 1)
            val dateWeek          = Tools.getTwoDigitNumber(date.get(Calendar.WEEK_OF_YEAR))

            val dateHour =
                if (modHour > 0) { Tools.getTwoDigitNumber((date.get(Calendar.HOUR_OF_DAY) / modHour) * modHour) }
                else { Tools.getTwoDigitNumber(abs(modHour)) }
            val dateMinute =
                if (modMinute > 0) { Tools.getTwoDigitNumber((date.get(Calendar.MINUTE) / modMinute) * modMinute) }
                else { Tools.getTwoDigitNumber(abs(modMinute)) }
            val dateSecond =
                if (modSecond > 0) { Tools.getTwoDigitNumber((date.get(Calendar.SECOND) / modSecond) * modSecond) }
                else { Tools.getTwoDigitNumber(abs(modSecond)) }
            val currentTime = (dateHour.toInt() * 60 + dateMinute.toInt()) * 60 + dateSecond.toInt()

            val timetable = Tools.getTimetable(jsonData, currentTime, dateWeekOddOrEven, dateDayOfWeek)

            val nowTimes = times.getJSONObject(timetable.nowNumber)
            val nowTime  = ((nowTimes.getInt("endHour") * 60 + nowTimes.getInt("endMinute")) * 60) + (86400 * timetable.nowDays) - currentTime


            views.setTextViewText(R.id.widgetDayOfWeek, context.resources.getStringArray(R.array.weekdays)[dateDayOfWeek])
            views.setTextViewText(R.id.widgetDate, "$dateDay.$dateMonth ($dateWeek)")
            views.setTextViewText(R.id.widgetTime, "$dateHour:$dateMinute:$dateSecond")

            views.setTextViewText(R.id.widgetNowTitle, if (nowTime > 0) { context.getString(R.string.widget_now) } else { context.getString(R.string.widget_then) })

            val lessonId = if (nowTime > 0) { timetable.nowId } else { timetable.thenId }

            views.setTextViewText(R.id.widgetNowText, lessons.getJSONArray(lessonId).getString(0))
            views.setTextViewText(R.id.widgetNowSubText, "(${lessons.getJSONArray(lessonId).getString(1)})")
        } catch (_: Exception) { }
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun pendingIntent(context: Context, action: String): PendingIntent? {
        val intent = Intent(context, javaClass)
        intent.action = action

        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
    }
}