package com.panov.util

import android.content.Context
import android.icu.util.Calendar

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

    fun getPxFromDp(context: Context, dp: Int): Int {
        val dpi = context.resources.displayMetrics.densityDpi
        return (dp * (dpi / 160f)).toInt()
    }
}