package com.panov.timetable

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.widget.RemoteViews

class WidgetNormal : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) updateAppWidget(context, appWidgetManager, appWidgetId)
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        if (context != null && intent != null) {
            val action = (intent.action ?: "").split("|")
            if (action[0] == "update") updateAppWidget(context, AppWidgetManager.getInstance(context), action[1].toInt())
        }
    }
}

internal fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
    val views = RemoteViews(context.packageName, R.layout.widget_normal)
    val intent = Intent(context, WidgetNormal::class.java)
    intent.action = "update|${appWidgetId}"
    views.setOnClickPendingIntent(
        R.id.background,
        PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
    )

    try {
        val date = Calendar.getInstance()
        views.setTextViewText(R.id.test, date.get(Calendar.MILLISECONDS_IN_DAY).toString())
    } catch (_: Exception) { }

    appWidgetManager.updateAppWidget(appWidgetId, views)
}