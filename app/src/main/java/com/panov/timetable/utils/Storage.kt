package com.panov.timetable.utils

import android.content.SharedPreferences

object Storage {
    lateinit var settings: SharedPreferences
    var timetable: TimetableData? = null
}