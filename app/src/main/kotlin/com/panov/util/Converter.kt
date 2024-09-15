package com.panov.util

import android.content.Context
import android.icu.util.Calendar
import com.google.android.material.textfield.TextInputEditText
import com.panov.timetable.R

object Converter {
    fun getTwoDigitNumber(number: Int): String {
        if (number < 10) return "0$number"
        return "$number"
    }

    fun getNumberText(number: Int, single: String, small: String, large: String): String {
        if ((number / 10) % 10 != 1) {
            if (number % 10 == 1) return single
            if (number % 10 == 2 || number % 10 == 3 || number % 10 == 4) return small
        }
        return large
    }

    fun getDateText(calendar: Calendar, twoDigitYear: Boolean = false): String {
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH) + 1
        val year = if (twoDigitYear) getTwoDigitNumber(calendar.get(Calendar.YEAR) % 100) else calendar.get(Calendar.YEAR).toString()
        return "${getTwoDigitNumber(day)}.${getTwoDigitNumber(month)}.$year"
    }

    fun getTimeText(calendar: Calendar): String {
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val second = calendar.get(Calendar.SECOND)
        return "${getTwoDigitNumber(hour)}:${getTwoDigitNumber(minute)}:${getTwoDigitNumber(second)}"
    }

    fun getTimeText(seconds: Int): String {
        val hour = seconds / 3600
        val minute = seconds / 60 % 60
        val second = seconds % 60
        return "${getTwoDigitNumber(hour)}:${getTwoDigitNumber(minute)}:${getTwoDigitNumber(second)}"
    }

    fun getTimeText(seconds: Int, context: Context): String {
        val hour = seconds / 3600
        val minute = seconds / 60 % 60
        val second = seconds % 60

        val hourText = getNumberText(
            hour,
            context.getString(R.string.timer_hour_single),
            context.getString(R.string.timer_hour_small),
            context.getString(R.string.timer_hour_large)
        )
        val minuteText = getNumberText(
            minute,
            context.getString(R.string.timer_minute_single),
            context.getString(R.string.timer_minute_small),
            context.getString(R.string.timer_minute_large)
        )
        val secondText = getNumberText(
            second,
            context.getString(R.string.timer_second_single),
            context.getString(R.string.timer_second_small),
            context.getString(R.string.timer_second_large)
        )

        return "${if (hour > 0) "$hour $hourText  " else ""}$minute $minuteText  $second $secondText"
    }

    fun getWeek(calendar: Calendar): String {
        return if (calendar.get(Calendar.WEEK_OF_YEAR) % 2 == 0) "even" else "odd"
    }

    fun getDayOfWeek(calendar: Calendar): Int {
        return if (calendar.get(Calendar.DAY_OF_WEEK) > 1) calendar.get(Calendar.DAY_OF_WEEK) - 2 else 6
    }

    fun getSecondsInDay(calendar: Calendar): Int {
        return calendar.get(Calendar.MILLISECONDS_IN_DAY) / 1000
    }

    fun getPxFromDp(context: Context, dp: Int): Int {
        val dpi = context.resources.displayMetrics.densityDpi
        return (dp * (dpi / 160f)).toInt()
    }

    fun getIntFromInput(input: TextInputEditText, default: Int = 0): Int {
        return input.text.toString().toIntOrNull() ?: default
    }
}