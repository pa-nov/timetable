package com.panov.timetable

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.panov.util.SettingsData
import java.util.Locale

class TimetableApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        Storage.settings = SettingsData(applicationContext)
        Storage.setTimetable(Storage.settings.getString(Storage.Timetable.JSON))
        AppCompatDelegate.setDefaultNightMode(Storage.settings.getInt(Storage.Application.THEME, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM))
        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags(
                Storage.settings.getString(Storage.Application.LANGUAGE, Locale.getDefault().language)
            )
        )
    }
}