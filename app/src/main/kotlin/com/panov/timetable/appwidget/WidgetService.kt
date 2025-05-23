package com.panov.timetable.appwidget

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.icu.util.Calendar
import android.os.Handler
import android.os.IBinder
import android.text.format.DateUtils
import androidx.core.app.NotificationCompat
import com.panov.timetable.R
import com.panov.timetable.util.ApplicationUtils
import com.panov.timetable.util.SettingsData
import com.panov.timetable.util.Storage
import com.panov.timetable.util.WidgetUtils
import com.panov.util.Converter

class WidgetService : Service() {
    companion object {
        const val CHANNEL_ID = "background_service"
    }

    private lateinit var handler: Handler
    private var updateOnUnlock = false
    private var timetable = emptyArray<Int>()
    private var timer = 0L

    private val unlockReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (context != null && intent != null && intent.action == Intent.ACTION_USER_PRESENT) {
                WidgetUtils.updateWidgets(context)
            }
        }
    }

    private val timetableUpdater = object : Runnable {
        override fun run() {
            WidgetUtils.updateWidgets(applicationContext)
            val calendar = ApplicationUtils.getCalendar()
            val seconds = Converter.getSecondsInDay(calendar)

            for (index in timetable.indices) {
                if (timetable[index] > seconds) {
                    val delay = (timetable[index] - seconds) * 1000 - calendar.get(Calendar.MILLISECOND).toLong()
                    handler.postDelayed(this, delay)
                    return
                }
            }

            val delay = DateUtils.DAY_IN_MILLIS + (timetable[0] - seconds) * 1000 - calendar.get(Calendar.MILLISECOND)
            handler.postDelayed(this, delay)
        }
    }

    private val timerUpdater = object : Runnable {
        override fun run() {
            WidgetUtils.updateWidgets(applicationContext)
            val calendar = ApplicationUtils.getCalendar()
            val minute = timer / 60 % 60
            val second = timer % 60

            var delay = timer
            if (timer >= 3600 && minute == 0L) delay -= calendar.get(Calendar.MINUTE)
            if (timer >= 60 && second == 0L) delay -= calendar.get(Calendar.SECOND)
            delay *= 1000
            delay -= calendar.get(Calendar.MILLISECOND)
            handler.postDelayed(this, delay)
        }
    }

    override fun attachBaseContext(context: Context) {
        super.attachBaseContext(ApplicationUtils.getLocalizedContext(context))
    }

    override fun onCreate() {
        super.onCreate()
        val settings = SettingsData(applicationContext)
        handler = Handler(mainLooper)
        updateOnUnlock = settings.getBoolean(Storage.Widgets.UPDATE_ON_UNLOCK)
        timetable = if (settings.getBoolean(Storage.Widgets.UPDATE_BY_TIMETABLE)) {
            val timetableData = ApplicationUtils.getTimetableData(settings.getString(Storage.Timetable.JSON))
            if (timetableData != null) {
                val timetableList = arrayListOf<Int>()
                for (index in 0 until timetableData.getLessonsCount()) {
                    timetableList.add(timetableData.getLessonTimeStart(index))
                    timetableList.add(timetableData.getLessonTimeEnd(index))
                }
                timetableList.sort()
                timetableList.toTypedArray()
            } else {
                emptyArray<Int>()
            }
        } else {
            emptyArray<Int>()
        }
        timer = settings.getInt(Storage.Widgets.UPDATE_TIMER).toLong()

        if (updateOnUnlock) registerReceiver(unlockReceiver, IntentFilter(Intent.ACTION_USER_PRESENT))
        if (timetable.isNotEmpty()) handler.post(timetableUpdater)
        if (timer > 0) handler.post(timerUpdater)

        val notificationChannel = NotificationChannel(CHANNEL_ID, getString(R.string.title_notification_background), NotificationManager.IMPORTANCE_LOW)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(notificationChannel)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
        notification.setSmallIcon(R.drawable.icon_logo)
        notification.setContentTitle(getString(R.string.title_notification_background))
        notification.setContentText(getString(R.string.description_notification_background))
        startForeground(startId, notification.build())
        return START_STICKY
    }

    override fun onDestroy() {
        if (updateOnUnlock) unregisterReceiver(unlockReceiver)
        if (timetable.isNotEmpty()) handler.removeCallbacks(timetableUpdater)
        if (timer > 0) handler.removeCallbacks(timerUpdater)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}