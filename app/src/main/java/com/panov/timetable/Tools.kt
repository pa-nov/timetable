package com.panov.timetable

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
                if (number % 10 == 2.0 || number % 10 == 3.0 || number % 10 == 4.0) return small
            }
            return large
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