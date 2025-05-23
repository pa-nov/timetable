package com.panov.timetable.util

object Storage {
    lateinit var settings: SettingsData
    var timetable: TimetableData? = null

    object Clock {
        private const val CORE = "clock"
        const val DISPLAY_ON_LOCKSCREEN = "$CORE-display_on_lockscreen"
        const val DISPLAY_TIMER = "$CORE-display_timer"
        const val DISPLAY_HEADERS = "$CORE-display_headers"
        const val DISPLAY_DATE_TIME = "$CORE-display_date_time"
        const val DISPLAY_CURRENT_LESSON = "$CORE-display_current_lesson"
        const val DISPLAY_NEXT_LESSON = "$CORE-display_next_lesson"
        const val NOT_DISPLAY_NEXT_TIME = "$CORE-not_display_next_time"
    }

    object Application {
        private const val CORE = "application"
        const val THEME = "$CORE-theme"
        const val LANGUAGE = "$CORE-language"
    }

    object Timetable {
        private const val CORE = "timetable"
        const val INITIAL_INDEX = "$CORE-initial_index"
        const val JSON = "$CORE-json"
    }

    object Widgets {
        private const val CORE = "widgets"
        const val COMBINE_BACKGROUND = "$CORE-combine_background"
        const val MODIFIER_HOURS = "$CORE-modifier_hours"
        const val MODIFIER_MINUTES = "$CORE-modifier_minutes"
        const val MODIFIER_SECONDS = "$CORE-modifier_seconds"
        const val UPDATE_ON_UNLOCK = "$CORE-update_on_unlock"
        const val UPDATE_BY_TIMETABLE = "$CORE-update_by_timetable"
        const val UPDATE_TIMER = "$CORE-update_timer"
    }
}