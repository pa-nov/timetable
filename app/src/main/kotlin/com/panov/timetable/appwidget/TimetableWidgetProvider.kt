package com.panov.timetable.appwidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.RemoteViews
import com.panov.timetable.AppUtils
import com.panov.timetable.R
import com.panov.timetable.Storage
import com.panov.timetable.widget.Timetable
import com.panov.util.Converter
import com.panov.util.SettingsData

class TimetableWidgetProvider : AppWidgetProvider() {
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
        val views = RemoteViews(context.packageName, R.layout.widget_timetable)
        val day = Converter.getDayOfWeek(calendar)
        val combineBackground = settings.getBoolean(Storage.Widgets.COMBINE_BACKGROUND)


        views.setInt(R.id.layout_background, "setBackgroundResource", if (combineBackground) R.drawable.square_rounded_small else 0)
        views.setTextViewText(R.id.title_weekday, context.resources.getStringArray(R.array.weekdays)[day])
        views.setTextViewText(R.id.text_date, Converter.getDateText(calendar, true))
        views.setTextViewText(R.id.text_time, Converter.getTimeText(calendar))

        views.setRemoteAdapter(R.id.layout_container, Intent(context, Timetable.RemoteListService::class.java))
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.layout_container)


        val intent = Intent(context, TimetableWidgetProvider::class.java)
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, arrayOf(appWidgetId).toIntArray())
        val pendingIntent = PendingIntent.getBroadcast(context, appWidgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        views.setOnClickPendingIntent(R.id.layout_background, pendingIntent)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}