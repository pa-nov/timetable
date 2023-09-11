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

class TimetableWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        if (context != null && intent != null && (intent.action ?: "") == "updateWidget") {
            updateWidgets(context)
        }
    }

    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.timetable_widget)
        views.setOnClickPendingIntent(R.id.buttonUpdate, pendingIntent(context, "updateWidget"))
        try {
            val savedData      = context.getSharedPreferences("SavedData", 0)
            val jsonDataString = savedData.getString("Json", "")
            val jsonData       = JSONObject(jsonDataString!!)
            val date           = Calendar.getInstance()
            val weekdays       = context.resources.getStringArray(R.array.weekdays)
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
            val dateHour          = if (modHour > 0)
                { Tools.getTwoDigitNumber((date.get(Calendar.HOUR_OF_DAY) / modHour) * modHour) } else
                { Tools.getTwoDigitNumber(abs(modHour)) }
            val dateMinute        = if (modMinute > 0)
                { Tools.getTwoDigitNumber((date.get(Calendar.MINUTE) / modMinute) * modMinute) } else
                { Tools.getTwoDigitNumber(abs(modMinute)) }
            val dateSecond        = if (modSecond > 0)
                { Tools.getTwoDigitNumber((date.get(Calendar.SECOND) / modSecond) * modSecond) } else
                { Tools.getTwoDigitNumber(abs(modSecond)) }
            val currentTime       = (dateHour.toInt() * 60 + dateMinute.toInt()) * 60 + dateSecond.toInt()

            val timetable = Tools.getTimetable(jsonData, currentTime, dateWeekOddOrEven, dateDayOfWeek)


            val nowTimes = times.getJSONObject(timetable.nowNumber)
            val nowTime = ((nowTimes.getInt("endHour") * 60 + nowTimes.getInt("endMinute")) * 60) + (86400 * timetable.nowDays) - currentTime
            val thenTimes = times.getJSONObject(timetable.thenNumber)
            val thenTime = ((thenTimes.getInt("startHour") * 60 + thenTimes.getInt("startMinute")) * 60) + (86400 * timetable.thenDays) - currentTime

            if (nowTime < 0) {
                views.setTextViewText(R.id.textWidgetNowTimeTitle, context.getString(R.string.widget_end))
            } else {
                views.setTextViewText(R.id.textWidgetNowTimeTitle, context.getString(R.string.widget_end_in))
            }

            views.setTextViewText(R.id.textWidgetDayOfWeek, weekdays[dateDayOfWeek])
            views.setTextViewText(R.id.textWidgetDate, "$dateDay.$dateMonth ($dateWeek)")
            views.setTextViewText(R.id.textWidgetTime, "$dateHour:$dateMinute:$dateSecond")

            views.setTextViewText(R.id.textWidgetNowText, lessons.getJSONArray(timetable.nowId).getString(0))
            views.setTextViewText(R.id.textWidgetNowSubText, "(${lessons.getJSONArray(timetable.nowId).getString(1)})")
            views.setTextViewText(R.id.textWidgetNowTimeText, Tools.getNiceTime(context.resources, nowTime.toDouble()))

            views.setTextViewText(R.id.textWidgetThenText, lessons.getJSONArray(timetable.thenId).getString(0))
            views.setTextViewText(R.id.textWidgetThenSubText, "(${lessons.getJSONArray(timetable.thenId).getString(1)})")
            views.setTextViewText(R.id.textWidgetThenTimeText, Tools.getNiceTime(context.resources, thenTime.toDouble()))

            appWidgetManager.updateAppWidget(appWidgetId, views)
        } catch (error: Error) {
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
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
}