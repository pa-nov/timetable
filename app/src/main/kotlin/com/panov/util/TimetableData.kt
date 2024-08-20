package com.panov.util

import android.icu.util.Calendar
import org.json.JSONArray
import org.json.JSONObject

class TimetableData(jsonString: String) {
    private val times: Array<TimetableTime>
    private val lessons: Array<TimetableLesson>
    private val even: Array<Array<Int>>
    private val odd: Array<Array<Int>>

    init {
        val jsonObject = JSONObject(jsonString)
        val jsonTimes = jsonObject.getJSONArray("times")
        val jsonLessons = jsonObject.getJSONArray("lessons")
        val jsonEven = jsonObject.getJSONArray("even")
        val jsonOdd = jsonObject.getJSONArray("odd")

        val timesList = arrayListOf<TimetableTime>()
        for (index in 0 until jsonTimes.length()) {
            timesList.add(TimetableTime(jsonTimes.getJSONArray(index)))
        }
        times = timesList.toTypedArray()

        val lessonsList = arrayListOf<TimetableLesson>()
        for (index in 0 until jsonLessons.length()) {
            lessonsList.add(TimetableLesson(jsonLessons.getJSONArray(index)))
        }
        lessons = lessonsList.toTypedArray()

        val evenList = arrayListOf<Array<Int>>()
        val oddList = arrayListOf<Array<Int>>()
        for (day in 0 until 7) {
            val evenDayList = arrayListOf<Int>()
            val oddDayList = arrayListOf<Int>()
            for (time in 0 until times.count()) {
                lessons[jsonEven.getJSONArray(day).getInt(time)]
                evenDayList.add(jsonEven.getJSONArray(day).getInt(time))
                lessons[jsonOdd.getJSONArray(day).getInt(time)]
                oddDayList.add(jsonOdd.getJSONArray(day).getInt(time))
            }
            evenList.add(evenDayList.toTypedArray())
            oddList.add(oddDayList.toTypedArray())
        }
        even = evenList.toTypedArray()
        odd = oddList.toTypedArray()
    }


    fun getLessonsCount(): Int {
        return times.count()
    }

    fun getLessonTimeText(lessonIndex: Int): String {
        return times[lessonIndex].toString().replace(" ", "\n")
    }

    fun getLessonTimeStart(lessonIndex: Int): Int {
        return times[lessonIndex].getStartSeconds()
    }

    fun getLessonTimeEnd(lessonIndex: Int): Int {
        return times[lessonIndex].getEndSeconds()
    }


    fun getLessonFullTitle(lessonId: Int): String {
        return lessons[lessonId].getFullTitle()
    }

    fun getLessonShortTitle(lessonId: Int): String {
        return lessons[lessonId].getShortTitle()
    }

    fun getClassroom(lessonId: Int): String {
        return lessons[lessonId].getClassroom()
    }

    fun getClassroomText(lessonId: Int): String {
        return lessons[lessonId].getClassroomText()
    }

    fun getTeacherFullName(lessonId: Int): String {
        return lessons[lessonId].getTeacherFullName()
    }

    fun getTeacherShortName(lessonId: Int): String {
        return lessons[lessonId].getTeacherShortName()
    }

    fun getLessonOtherIds(lessonId: Int): Array<Int> {
        return lessons[lessonId].getOtherIds()
    }


    fun getLessonId(week: String, day: Int, lessonIndex: Int): Int {
        if (week == "even") return even[day][lessonIndex]
        if (week == "odd") return odd[day][lessonIndex]
        return 0
    }
}

class TimetableTime(jsonArray: JSONArray) {
    private val startHour: Int = jsonArray.getString(0).toIntOrNull() ?: 0
    private val startMinute: Int = jsonArray.getString(1).toIntOrNull() ?: 0
    private val endHour: Int = jsonArray.getString(2).toIntOrNull() ?: 0
    private val endMinute: Int = jsonArray.getString(3).toIntOrNull() ?: 0

    fun getStartSeconds(): Int {
        return ((startHour * 60) + startMinute) * 60
    }

    fun getEndSeconds(): Int {
        return ((endHour * 60) + endMinute) * 60
    }

    override fun toString(): String {
        val start = "${Converter.getTwoDigitNumber(startHour)}:${Converter.getTwoDigitNumber(startMinute)}"
        val end = "${Converter.getTwoDigitNumber(endHour)}:${Converter.getTwoDigitNumber(endMinute)}"
        return "$start $end"
    }
}

class TimetableLesson(jsonArray: JSONArray) {
    private val titleFull: String
    private val titleShort: String
    private val classroom: String = jsonArray.getString(1).trim()
    private val teacherLastName: String
    private val teacherFirstName: String
    private val teacherMiddleName: String
    private val other: Array<Int>

    init {
        val titles = jsonArray.getString(0).split("|")
        titleFull = titles[0].trim()
        titleShort = if (titles.count() > 1) titles[1].trim() else titleFull

        val teacherNames = jsonArray.getString(2).split("|")
        teacherLastName = teacherNames[0].trim()
        teacherFirstName = if (teacherNames.count() > 1) teacherNames[1].trim() else ""
        teacherMiddleName = if (teacherNames.count() > 2) teacherNames[2].trim() else ""

        val others = jsonArray.getString(3).split("|")
        val otherList = arrayListOf<Int>()
        for (index in 0 until others.count()) {
            val number = others[index].toIntOrNull() ?: 0
            if (number > 0) otherList.add(number)
        }
        other = otherList.toTypedArray()
    }

    fun getFullTitle(): String {
        return titleFull
    }

    fun getShortTitle(): String {
        return titleShort
    }

    fun getClassroom(): String {
        return classroom
    }

    fun getClassroomText(): String {
        return "($classroom)"
    }

    fun getTeacherFullName(): String {
        var teacherName = "$teacherLastName "
        if (teacherFirstName.isNotEmpty()) {
            teacherName += "$teacherFirstName "
            if (teacherMiddleName.isNotEmpty()) {
                teacherName += teacherMiddleName
            }
        }
        return teacherName.trim().ifEmpty { "-" }
    }

    fun getTeacherShortName(): String {
        var teacherName = ""
        if (teacherLastName.isNotEmpty()) {
            teacherName += teacherLastName
            if (teacherFirstName.isNotEmpty()) {
                teacherName += " ${teacherFirstName[0]}."
                if (teacherMiddleName.isNotEmpty()) {
                    teacherName += " ${teacherMiddleName[0]}."
                }
            }
        }
        return teacherName.ifEmpty { "-" }
    }

    fun getOtherIds(): Array<Int> {
        return other
    }
}

class TimetableOffset(timetable: TimetableData, calendar: Calendar) {
    val currentLessonIndex: Int
    val currentDay: Int
    val currentWeek: String
    val currentDaysOffset: Int

    val nextLessonIndex: Int
    val nextDay: Int
    val nextWeek: String
    val nextDaysOffset: Int

    init {
        val day = if (calendar.get(Calendar.DAY_OF_WEEK) > 1) calendar.get(Calendar.DAY_OF_WEEK) - 2 else 6
        val week = if (calendar.get(Calendar.WEEK_OF_YEAR) % 2 == 0) "even" else "odd"
        val seconds = calendar.get(Calendar.MILLISECONDS_IN_DAY) / 1000
        val lessonsCount = timetable.getLessonsCount()

        var tempLessonIndex = -1
        var tempDay = day
        var tempWeek = week
        var tempDaysOffset = 0

        for (index in 0 until lessonsCount) {
            if (timetable.getLessonTimeStart(index) > seconds) break
            tempLessonIndex = index
        }
        var tempOffset = lessonsCount * 14
        while (tempOffset >= 0) {
            if (tempLessonIndex < 0) {
                tempLessonIndex = lessonsCount - 1
                tempDaysOffset--
                tempDay--
                if (tempDay < 0) {
                    tempDay = 6
                    tempWeek = if (tempWeek == "odd") "even" else "odd"
                }
            }
            if (timetable.getLessonId(tempWeek, tempDay, tempLessonIndex) > 0) {
                tempOffset = 0
            } else {
                if (tempOffset > 0) {
                    tempLessonIndex--
                }
            }
            tempOffset--
        }

        currentLessonIndex = tempLessonIndex
        currentDay = tempDay
        currentWeek = tempWeek
        currentDaysOffset = tempDaysOffset
    }

    init {
        val day = if (calendar.get(Calendar.DAY_OF_WEEK) > 1) calendar.get(Calendar.DAY_OF_WEEK) - 2 else 6
        val week = if (calendar.get(Calendar.WEEK_OF_YEAR) % 2 == 0) "even" else "odd"
        val seconds = calendar.get(Calendar.MILLISECONDS_IN_DAY) / 1000
        val lessonsCount = timetable.getLessonsCount()

        var tempLessonIndex = 0
        var tempDay = day
        var tempWeek = week
        var tempDaysOffset = 0

        for (index in 0 until lessonsCount) {
            if (timetable.getLessonTimeStart(index) > seconds) break
            tempLessonIndex = index + 1
        }
        var tempOffset = lessonsCount * 14
        while (tempOffset >= 0) {
            if (tempLessonIndex >= lessonsCount) {
                tempLessonIndex = 0
                tempDaysOffset++
                tempDay++
                if (tempDay > 6) {
                    tempDay = 0
                    tempWeek = if (tempWeek == "odd") "even" else "odd"
                }
            }
            if (timetable.getLessonId(tempWeek, tempDay, tempLessonIndex) > 0) {
                tempOffset = 0
            } else {
                if (tempOffset > 0) {
                    tempLessonIndex++
                }
            }
            tempOffset--
        }

        nextLessonIndex = tempLessonIndex
        nextDay = tempDay
        nextWeek = tempWeek
        nextDaysOffset = tempDaysOffset
    }
}