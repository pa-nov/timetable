package com.panov.timetable

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class Timetable : Application() {
    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(
            getSharedPreferences("Settings", 0).getInt(
                "Theme", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            )
        )
    }
}