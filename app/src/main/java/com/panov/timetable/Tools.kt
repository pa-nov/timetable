package com.panov.timetable

import android.content.res.Resources
import org.json.JSONObject
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

            val hoursText = if (hours > 0) "${hours.toInt()} ${getNumberText(
                hours,
                res.getString(R.string.time_hours_single),
                res.getString(R.string.time_hours_small),
                res.getString(R.string.time_hours_large)
            )
            }" else ""
            val minutesText = if (hours > 0) res.getString(R.string.time_minutes) else getNumberText(
                minutes,
                res.getString(R.string.time_minutes_single),
                res.getString(R.string.time_minutes_small),
                res.getString(R.string.time_minutes_large)
            )
            val secondsText = if (hours > 0) res.getString(R.string.time_seconds) else getNumberText(
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

        fun getTimetableData(data: JSONObject, time: Int, dateDayOfWeek: Int, dateWeekOddOrEven: String): TimetableData {
            val times = data.getJSONArray("times")
            val timesMax = times.length()
            val lastLesson = timesMax - 1

            var currentLesson = -1
            var nextLesson = 0
            for (i in 0 until timesMax) {
                val tempTimes = times.getJSONObject(i)
                val tempTime = (tempTimes.getInt("startHour") * 60 + tempTimes.getInt("startMinute")) * 60
                if (time > tempTime) {
                    currentLesson = i
                    nextLesson = i + 1
                }
            }

            var currentDay = data.getJSONArray(dateWeekOddOrEven).getJSONArray(dateDayOfWeek)
            var currentDays = 0
            var currentDayOfWeek = dateDayOfWeek
            var currentWeekOddOrEven = dateWeekOddOrEven
            var currentWhileCount = 14 * timesMax
            while (currentWhileCount > 0) {
                if (currentLesson < 0) {
                    currentDays--
                    currentLesson = lastLesson
                    if (currentDayOfWeek < 1) {
                        currentWeekOddOrEven = if (currentWeekOddOrEven == "odd") "even" else "odd"
                        currentDayOfWeek = 6
                    } else currentDayOfWeek--
                    currentDay = data.getJSONArray(currentWeekOddOrEven).getJSONArray(currentDayOfWeek)
                }

                if (currentDay.getInt(currentLesson) > 0) currentWhileCount = 0 else {
                    currentWhileCount--
                    if (currentWhileCount > 0) currentLesson--
                }
            }

            var nextDay = data.getJSONArray(dateWeekOddOrEven).getJSONArray(dateDayOfWeek)
            var nextDays = 0
            var nextDayOfWeek = dateDayOfWeek
            var nextWeekOddOrEven = dateWeekOddOrEven
            var nextWhileCount = 14 * timesMax
            while (nextWhileCount > 0) {
                if (nextLesson > lastLesson) {
                    nextDays++
                    nextLesson = 0
                    if (nextDayOfWeek > 5) {
                        nextWeekOddOrEven = if (nextWeekOddOrEven == "odd") "even" else "odd"
                        nextDayOfWeek = 0
                    } else nextDayOfWeek++
                    nextDay = data.getJSONArray(nextWeekOddOrEven).getJSONArray(nextDayOfWeek)
                }

                if (nextDay.getInt(nextLesson) > 0) nextWhileCount = 0 else {
                    nextWhileCount--
                    if (nextWhileCount > 0) nextLesson++
                }
            }

            return TimetableData(
                currentDay.getInt(currentLesson),
                currentLesson,
                currentDays,
                nextDay.getInt(nextLesson),
                nextLesson,
                nextDays
            )
        }
    }
}

class TimetableData(currentI: Int, currentN: Int, currentD: Int, nextI: Int, nextN: Int, nextD: Int) {
    val currentId = currentI
    val currentNumber = currentN
    val currentDays = currentD
    val nextId = nextI
    val nextNumber = nextN
    val nextDays = nextD
}