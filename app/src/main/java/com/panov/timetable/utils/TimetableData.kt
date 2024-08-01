package com.panov.timetable.utils

import org.json.JSONArray
import org.json.JSONObject

class TimetableData(jsonString: String?) {
    private val times: Array<TimetableTime>
    private val lessons: Array<TimetableLesson>
    private val even: Array<Array<Int>>
    private val odd: Array<Array<Int>>

    init {
        val jsonObject = JSONObject(jsonString ?: "")
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
                evenDayList.add(jsonEven.getJSONArray(day).getInt(time))
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

    fun getLessonTime(lessonIndex: Int): String {
        return times[lessonIndex].toString()
    }

    fun getLessonStart(lessonIndex: Int): Int {
        return times[lessonIndex].getStartSeconds()
    }

    fun getLessonEnd(lessonIndex: Int): Int {
        return times[lessonIndex].getEndSeconds()
    }


    fun getLessonShortTitle(lessonId: Int): String {
        return lessons[lessonId].getShortTitle()
    }

    fun getLessonFullTitle(lessonId: Int): String {
        return lessons[lessonId].getFullTitle()
    }

    fun getLessonClassroom(lessonId: Int): String {
        return lessons[lessonId].getClassroom()
    }

    fun getLessonClassroomText(lessonId: Int): String {
        return lessons[lessonId].getClassroomText()
    }

    fun getTeacherShortName(lessonId: Int): String {
        return lessons[lessonId].getTeacherShortName()
    }

    fun getTeacherFullName(lessonId: Int): String {
        return lessons[lessonId].getTeacherFullName()
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
        val start = "${Tools.getTwoDigitNumber(startHour)}:${Tools.getTwoDigitNumber(startMinute)}"
        val end = "${Tools.getTwoDigitNumber(endHour)}:${Tools.getTwoDigitNumber(endMinute)}"
        return "$start\n$end"
    }
}

class TimetableLesson(jsonArray: JSONArray) {
    private val titleShort: String
    private val titleFull: String
    private val classroom: String = jsonArray.getString(1).trim()
    private val teacherLastName: String
    private val teacherFirstName: String
    private val teacherMiddleName: String
    private val other: Array<Int>

    init {
        val titles = jsonArray.getString(0).split("|")
        titleShort = titles[0].trim()
        titleFull = if (titles.count() > 1) titles[1].trim() else titles[0].trim()

        val teacherNames = jsonArray.getString(2).split("|")
        teacherLastName = teacherNames[0].trim()
        teacherFirstName = teacherNames[1].trim()
        teacherMiddleName = teacherNames[2].trim()

        val others = jsonArray.getString(3).split("|")
        val otherList = arrayListOf<Int>()
        for (index in 0 until others.count()) {
            val number = others[index].toIntOrNull() ?: 0
            if (number > 0) otherList.add(number)
        }
        other = otherList.toTypedArray()
    }

    fun getShortTitle(): String {
        return titleShort
    }

    fun getFullTitle(): String {
        return titleFull
    }

    fun getClassroom(): String {
        return classroom
    }

    fun getClassroomText(): String {
        return "($classroom)"
    }

    fun getTeacherShortName(): String {
        if (teacherLastName.isNotEmpty()) {
            var teacherName = teacherLastName
            if (teacherFirstName.isNotEmpty()) {
                teacherName += " ${teacherFirstName[0]}."
                if (teacherMiddleName.isNotEmpty()) {
                    teacherName += " ${teacherMiddleName[0]}."
                }
            }
            return teacherName
        } else {
            return "-"
        }
    }

    fun getTeacherFullName(): String {
        var teacherName = ""
        if (teacherLastName.isNotEmpty()) {
            teacherName += "$teacherLastName "
        }
        if (teacherFirstName.isNotEmpty()) {
            teacherName += "$teacherFirstName "
            if (teacherMiddleName.isNotEmpty()) {
                teacherName += teacherMiddleName
            }
        }
        return teacherName.trim().ifEmpty { "-" }
    }

    fun getOtherIds(): Array<Int> {
        return other
    }
}