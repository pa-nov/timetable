package com.panov.timetable

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.panov.timetable.utils.Storage

class TimetableApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Storage.settings = getSharedPreferences("Settings", 0)
        AppCompatDelegate.setDefaultNightMode(
            Storage.settings.getInt("application_theme", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        )
    }
}