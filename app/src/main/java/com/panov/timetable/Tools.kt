package com.panov.timetable

import android.content.res.Resources
import org.json.JSONObject
import kotlin.math.abs
import kotlin.math.floor

class Tools {
    companion object {
        fun getTwoDigitNumber(number: Int): String {
            if (number < 10) { return "0$number" }
            return number.toString()
        }

        fun getNumberText(number: Double, textSingle: String, textSmall: String, textLarge: String): String {
            if (floor(number / 10) % 10 != 1.0) {
                if (number % 10 == 1.0) {
                    return textSingle
                }
                if (number % 10 == 2.0 || number % 10 == 3.0 || number % 10 == 4.0) {
                    return textSmall
                }
            }
            return textLarge
        }

        fun getNiceTime(res: Resources, timeSource: Double): String {
            val postfix = if (timeSource < 0) { "  ${res.getString(R.string.time_ago)}" } else { "" }
            val time    = abs(timeSource)

            val hours   = floor(time / 3600)
            val minutes = floor((time - (hours * 3600)) / 60)
            val seconds = time - (hours * 60 + minutes) * 60

            val hoursText   = if (hours > 0)
            { "${hours.toInt()} ${getNumberText(hours, res.getString(R.string.time_hours_single), res.getString(R.string.time_hours_small), res.getString(R.string.time_hours_large))}  " } else
            { "" }
            val minutesText = if (hours > 0)
            { "${minutes.toInt()} ${res.getString(R.string.time_minutes)}" } else
            { "${minutes.toInt()} ${getNumberText(minutes, res.getString(R.string.time_minutes_single), res.getString(R.string.time_minutes_small), res.getString(R.string.time_minutes_large))}" }
            val secondsText = if (hours > 0)
            { "${seconds.toInt()} ${res.getString(R.string.time_seconds)}" } else
            { "${seconds.toInt()} ${getNumberText(seconds, res.getString(R.string.time_seconds_single), res.getString(R.string.time_seconds_small), res.getString(R.string.time_seconds_large))}" }

            return "$hoursText$minutesText  $secondsText$postfix"
        }

        fun getTime(timeSource: Double): String {
            val time    = abs(timeSource)

            val hours   = floor(time / 3600)
            val minutes = floor((time - (hours * 3600)) / 60)
            val seconds = time - (hours * 60 + minutes) * 60

            return "${getTwoDigitNumber(hours.toInt())}:${getTwoDigitNumber(minutes.toInt())}:${getTwoDigitNumber(seconds.toInt())}"
        }

        fun getTimetable(jsonData: JSONObject, currentTime: Int, dateWeekOddOrEven: String, dateDayOfWeek: Int): Timetable {
            val times      = jsonData.getJSONArray("times")
            val timesMax   = times.length()
            val lastLesson = times.length() - 1


            var nowLesson = -1
            var thenLesson = 0
            for (i in 0 until timesMax) {
                val savedTimeJson = times.getJSONObject(i)
                val savedTime = (savedTimeJson.getInt("startHour") * 60 + savedTimeJson.getInt("startMinute")) * 60
                if (currentTime > savedTime) {
                    nowLesson = i
                    thenLesson = i + 1
                }
            }

            var nowTimetable = jsonData.getJSONArray(dateWeekOddOrEven).getJSONArray(dateDayOfWeek)
            var nowDaysCount = 0
            var nowWeekOddOrEven = dateWeekOddOrEven
            var nowDayOfWeek = dateDayOfWeek
            var nowWhileCount = 14 * (timesMax)
            while (nowWhileCount > 0) {
                if (nowLesson < 0) {
                    nowDaysCount--
                    nowLesson = lastLesson

                    if (nowDayOfWeek < 1) {
                        nowWeekOddOrEven = if (nowWeekOddOrEven == "odd") { "even" } else { "odd" }
                        nowDayOfWeek = 6
                    } else {
                        nowDayOfWeek--
                    }

                    nowTimetable = jsonData.getJSONArray(nowWeekOddOrEven).getJSONArray(nowDayOfWeek)
                }

                if (nowTimetable.getInt(nowLesson) > 0) {
                    nowWhileCount = 0
                } else {
                    nowWhileCount--
                    if (nowWhileCount > 0) { nowLesson-- }
                }
            }

            var thenTimetable = jsonData.getJSONArray(dateWeekOddOrEven).getJSONArray(dateDayOfWeek)
            var thenDaysCount = 0
            var thenWeekOddOrEven = dateWeekOddOrEven
            var thenDayOfWeek = dateDayOfWeek
            var thenWhileCount = 14 * (timesMax)
            while (thenWhileCount > 0) {
                if (thenLesson > lastLesson) {
                    thenDaysCount++
                    thenLesson = 0

                    if (thenDayOfWeek > 5) {
                        thenWeekOddOrEven = if (thenWeekOddOrEven == "odd") { "even" } else { "odd" }
                        thenDayOfWeek = 0
                    } else {
                        thenDayOfWeek++
                    }

                    thenTimetable = jsonData.getJSONArray(thenWeekOddOrEven).getJSONArray(thenDayOfWeek)
                }

                if (thenTimetable.getInt(thenLesson) > 0) {
                    thenWhileCount = 0
                } else {
                    thenWhileCount--
                    if (thenWhileCount > 0) { thenLesson++ }
                }
            }


            return Timetable(
                nowTimetable.getInt(nowLesson),
                nowLesson,
                nowDaysCount,
                thenTimetable.getInt(thenLesson),
                thenLesson,
                thenDaysCount
            )
        }
    }

    class Timetable(nowI: Int, nowN: Int, nowD: Int, thenI: Int, thenN: Int, thenD: Int) {
        val nowId      = nowI
        val nowNumber  = nowN
        val nowDays    = nowD
        val thenId     = thenI
        val thenNumber = thenN
        val thenDays   = thenD
    }
}