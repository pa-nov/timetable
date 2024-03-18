package com.panov.timetable

import android.content.res.Resources
import kotlin.math.abs
import kotlin.math.floor

class Tools {
    companion object {
        fun getTwoDigitNumber(number: Int): String {
            if (number < 10) return "0$number"
            return number.toString()
        }

        fun getNumberText(number: Double, single: String, small: String, large: String): String {
            if (floor(number / 10) % 10 != 1.0) {
                if (number % 10 == 1.0) return single
                if (number % 10 in arrayOf(2.0, 3.0, 4.0)) return small
            }
            return large
        }

        fun getTimeText(timeSource: Double, res: Resources): String {
            val postfix = if (timeSource < 0) res.getString(R.string.time_ago) else ""
            val time = abs(timeSource)

            val hours = floor(time / 3600)
            val minutes = floor((time - (hours * 3600)) / 60)
            val seconds = time - (hours * 60 + minutes) * 60

            val hoursText = if (hours > 0) "${hours.toInt()} ${
                getNumberText(
                    hours,
                    res.getString(R.string.time_hours_single),
                    res.getString(R.string.time_hours_small),
                    res.getString(R.string.time_hours_large)
                )
            }" else ""
            val minutesText = if (hours > 0) res.getString(R.string.time_minutes) else
                getNumberText(
                    minutes,
                    res.getString(R.string.time_minutes_single),
                    res.getString(R.string.time_minutes_small),
                    res.getString(R.string.time_minutes_large)
                )
            val secondsText = if (hours > 0) res.getString(R.string.time_seconds) else
                getNumberText(
                    seconds,
                    res.getString(R.string.time_seconds_single),
                    res.getString(R.string.time_seconds_small),
                    res.getString(R.string.time_seconds_large)
                )

            return "$hoursText  ${minutes.toInt()} $minutesText  ${seconds.toInt()} $secondsText  $postfix".trim()
        }

        fun getTime(timeSource: Double): String {
            val time = abs(timeSource)

            val hours = floor(time / 3600)
            val minutes = floor((time - (hours * 3600)) / 60)
            val seconds = time - (hours * 60 + minutes) * 60

            return "${getTwoDigitNumber(hours.toInt())}:${getTwoDigitNumber(minutes.toInt())}:${getTwoDigitNumber(seconds.toInt())}"
        }
    }
}

class TimetableData(currentI: Int, currentN: Int, currentD: Int, nextI: Int, nextN: Int, nextD: Int) {
    val currentId = currentI;
    val currentNumber = currentN;
    val currentDays = currentD;
    val nextId = nextI;
    val nextNumber = nextN;
    val nextDays = nextD;
}