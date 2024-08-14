package com.panov.timetable

import com.panov.util.SettingsData
import com.panov.util.TimetableData

object Storage {
    lateinit var settings: SettingsData
    var timetable: TimetableData? = null

    fun setTimetable(jsonString: String) {
        timetable = try {
            TimetableData(jsonString)
        } catch (_: Exception) {
            null
        }
    }
}