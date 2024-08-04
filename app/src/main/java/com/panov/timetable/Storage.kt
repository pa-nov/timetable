package com.panov.timetable

import android.content.SharedPreferences
import com.panov.util.TimetableData

object Storage {
    lateinit var settings: SharedPreferences
    var timetable: TimetableData? = null
}