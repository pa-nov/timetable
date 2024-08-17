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

    object Application {
        private const val CORE = "application"
        const val THEME = "$CORE-theme"
        const val LANGUAGE = "$CORE-language"
        const val INITIAL_INDEX = "$CORE-initial_index"
    }

    object Timetable {
        private const val CORE = "timetable"
        const val JSON = "$CORE-json"
    }

    object Widget {
        private const val CORE = "widget"
        const val MODIFIER_HOUR = "$CORE-modifier_hour"
        const val MODIFIER_MINUTE = "$CORE-modifier_minute"
        const val MODIFIER_SECOND = "$CORE-modifier_second"
    }
}