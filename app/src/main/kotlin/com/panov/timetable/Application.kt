package com.panov.timetable

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.panov.timetable.util.ApplicationUtils
import com.panov.timetable.util.SettingsData
import com.panov.timetable.util.Storage
import com.panov.timetable.util.WidgetUtils

class Application : Application() {
    override fun onCreate() {
        super.onCreate()
        Storage.settings = SettingsData(applicationContext)
        Storage.timetable = ApplicationUtils.getTimetableData(Storage.settings.getString(Storage.Timetable.JSON))
        AppCompatDelegate.setDefaultNightMode(Storage.settings.getInt(Storage.Application.THEME, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM))
        WidgetUtils.startWidgetService(applicationContext, Storage.settings)
    }
}