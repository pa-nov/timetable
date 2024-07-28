package com.panov.timetable

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.panov.timetable.utils.Storage
import java.util.Locale

class TimetableApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Storage.settings = getSharedPreferences("Settings", 0)
        AppCompatDelegate.setDefaultNightMode(Storage.settings.getInt("app_theme", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM))
        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags(Storage.settings.getString("app_language", Locale.getDefault().language) ?: Locale.getDefault().language)
        )
    }
}