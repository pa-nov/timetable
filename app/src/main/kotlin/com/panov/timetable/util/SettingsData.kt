package com.panov.timetable.util

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import androidx.core.content.edit

class SettingsData(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("Settings", Context.MODE_PRIVATE)
    private var editor: Editor? = null


    fun getInt(key: String, default: Int = 0): Int {
        return sharedPreferences.getInt(key, default)
    }

    fun setInt(key: String, value: Int) {
        if (editor == null) editor = sharedPreferences.edit()
        editor?.putInt(key, value)
    }

    fun saveInt(key: String, value: Int) {
        sharedPreferences.edit { putInt(key, value) }
        editor?.putInt(key, value)
    }


    fun getString(key: String, default: String = ""): String {
        return sharedPreferences.getString(key, default) ?: default
    }

    fun setString(key: String, value: String) {
        if (editor == null) editor = sharedPreferences.edit()
        editor?.putString(key, value)
    }

    fun saveString(key: String, value: String) {
        sharedPreferences.edit { putString(key, value) }
        editor?.putString(key, value)
    }


    fun getBoolean(key: String, default: Boolean = false): Boolean {
        return sharedPreferences.getBoolean(key, default)
    }

    fun setBoolean(key: String, value: Boolean) {
        if (editor == null) editor = sharedPreferences.edit()
        editor?.putBoolean(key, value)
    }

    fun saveBoolean(key: String, value: Boolean) {
        sharedPreferences.edit { putBoolean(key, value) }
        editor?.putBoolean(key, value)
    }


    fun save() {
        editor?.apply()
        editor = null
    }
}