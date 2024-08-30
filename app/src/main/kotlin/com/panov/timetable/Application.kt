package com.panov.timetable

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.panov.util.SettingsData

class Application : Application() {
    override fun onCreate() {
        super.onCreate()
        Storage.settings = SettingsData(applicationContext)
        Storage.setTimetable(Storage.settings.getString(Storage.Timetable.JSON))
        AppCompatDelegate.setDefaultNightMode(Storage.settings.getInt(Storage.Application.THEME, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM))
    }
}