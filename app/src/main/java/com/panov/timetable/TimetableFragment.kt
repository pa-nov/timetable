package com.panov.timetable

import android.icu.util.Calendar
import android.os.Bundle
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import org.json.JSONObject

class TimetableFragment : Fragment() {
    private var date = Calendar.getInstance()
    private var timetable = JSONObject()
    private var initialIndex = 1
    private var tempPosition = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val fragment = inflater.inflate(R.layout.fragment_timetable, container, false)
        val fragmentClass: TimetableFragment = this
        date.firstDayOfWeek = Calendar.MONDAY
        date.minimalDaysInFirstWeek = 4

        try {
            val savedData = requireActivity().getSharedPreferences("Timetable", 0)
            timetable = JSONObject(savedData.getString("Json", "") ?: "")
            initialIndex = savedData.getInt("InitialIndex", 1)
        } catch (e: Exception) {
            fragment.findViewById<TextView>(R.id.button_action).text = resources.getString(R.string.error)
            return fragment
        }

        val pages = fragment.findViewById<ViewPager2>(R.id.pages)
        pages.adapter = TimetablePageAdapter(fragmentClass)
        pages.post { pages.setCurrentItem(1, false) }
        pages.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                tempPosition = position - 1
            }

            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
                if (state == ViewPager2.SCROLL_STATE_IDLE) {
                    date.add(Calendar.DAY_OF_YEAR, tempPosition)
                    tempPosition = 0
                    pages.adapter = TimetablePageAdapter(fragmentClass)
                    pages.setCurrentItem(1, false)
                }
            }
        })

        return fragment
    }

    fun resetDate() {
        date = Calendar.getInstance()
        date.firstDayOfWeek = Calendar.MONDAY
        date.minimalDaysInFirstWeek = 4
        val view = view
        if (view != null && view.findViewById<TextView>(R.id.button_action).text != resources.getString(R.string.error)) {
            tempPosition = 0
            val pages = view.findViewById<ViewPager2>(R.id.pages)
            pages.adapter = TimetablePageAdapter(this)
            pages.setCurrentItem(1, false)
        }
    }

    fun updatePage(layout: LinearLayout, position: Int) {
        val tempDate = date.clone() as Calendar
        tempDate.add(Calendar.DAY_OF_YEAR, position - 1)

        val dateDayOfWeek = if (tempDate.get(Calendar.DAY_OF_WEEK) > 1) tempDate.get(Calendar.DAY_OF_WEEK) - 2 else 6
        val dateWeekOddOrEven = if (tempDate.get(Calendar.WEEK_OF_YEAR) % 2 == 0) "even" else "odd"
        val currentDay = timetable.getJSONArray(dateWeekOddOrEven).getJSONArray(dateDayOfWeek)
        val anotherDay = timetable.getJSONArray(if (dateWeekOddOrEven == "odd") "even" else "odd").getJSONArray(dateDayOfWeek)

        layout.findViewById<TextView>(R.id.title).text = resources.getStringArray(R.array.weekdays)[dateDayOfWeek]

        for (i: Int in 0 until timetable.getJSONArray("times").length()) {
            val date = arrayOf(dateWeekOddOrEven, dateDayOfWeek.toString(), i.toString())
            fillLesson(layout, i, currentDay.getInt(i), anotherDay.getInt(i), date, isNow = false, isInfo = false)
        }
    }

    fun updateDate() {
        val dateWeek = Tools.getTwoDigitNumber(date.get(Calendar.WEEK_OF_YEAR))
        val dateText = requireView().findViewById<TextView>(R.id.button_action)

        if (DateUtils.isToday(date.timeInMillis + DateUtils.DAY_IN_MILLIS)) {
            dateText.text =
                resources.getString(R.string.placeholder_date_text, resources.getString(R.string.day_yesterday), dateWeek)
            return
        }
        if (DateUtils.isToday(date.timeInMillis)) {
            dateText.text =
                resources.getString(R.string.placeholder_date_text, resources.getString(R.string.day_today), dateWeek)
            return
        }
        if (DateUtils.isToday(date.timeInMillis - DateUtils.DAY_IN_MILLIS)) {
            dateText.text =
                resources.getString(R.string.placeholder_date_text, resources.getString(R.string.day_tomorrow), dateWeek)
            return
        }

        val dateDay = Tools.getTwoDigitNumber(date.get(Calendar.DAY_OF_MONTH))
        val dateMonth = Tools.getTwoDigitNumber(date.get(Calendar.MONTH) + 1)
        val dateYear = date.get(Calendar.YEAR).toString()

        dateText.text = resources.getString(R.string.placeholder_date_number, dateDay, dateMonth, dateYear, dateWeek)
    }

    private fun fillLesson(layout: LinearLayout, lessonNumber: Int, currentId: Int, anotherId: Int, date: Array<String>, isNow: Boolean, isInfo: Boolean) {
        val lessonView = layoutInflater.inflate(R.layout.timetable_lesson, null)
        val times = timetable.getJSONArray("times")
        val lessons = timetable.getJSONArray("lessons")
        val lessonData = lessons.getJSONArray(currentId)

        val time = times.getJSONObject(lessonNumber)
        val startHour = Tools.getTwoDigitNumber(time.getInt("startHour"))
        val startMinute = Tools.getTwoDigitNumber(time.getInt("startMinute"))
        val endHour = Tools.getTwoDigitNumber(time.getInt("endHour"))
        val endMinute = Tools.getTwoDigitNumber(time.getInt("endMinute"))

        val name = lessonView.findViewById<TextView>(R.id.text_name)
        val teacher = lessonView.findViewById<TextView>(R.id.text_teacher)
        val room = lessonView.findViewById<TextView>(R.id.text_room)

        var isWeekChangeable = currentId != anotherId
        if (isWeekChangeable && lessonData.getString(3).isNotBlank()) {
            lessonData.getString(3).split("|").forEach {
                if (anotherId == it.toInt()) isWeekChangeable = false
            }
        }
        if (isWeekChangeable) lessonView.findViewById<FrameLayout>(R.id.line_right)
            .setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.red))
        if (isNow) lessonView.findViewById<FrameLayout>(R.id.line_left)
            .setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.green))

        lessonView.findViewById<TextView>(R.id.text_index).text =
            resources.getString(R.string.placeholder, (lessonNumber + initialIndex).toString())
        lessonView.findViewById<TextView>(R.id.text_times).text =
            resources.getString(R.string.placeholder_times, startHour, startMinute, endHour, endMinute)

        if (currentId > 0) {
            val teachers = lessonData.getString(2).split("|")
            name.text = lessonData.getString(0)
            teacher.text = resources.getString(R.string.placeholder_teacher, teachers[0], teachers[1][0], teachers[2][0])
            room.text = resources.getString(R.string.placeholder_room, lessonData.getString(1))
            if (!isInfo) lessonView.setOnClickListener {
                showInfo(lessonView, currentId, date)
            }
        } else {
            name.visibility = View.INVISIBLE
            teacher.visibility = View.INVISIBLE
            room.visibility = View.INVISIBLE
        }

        layout.addView(lessonView)
    }

    private fun showInfo(view: View, lessonId: Int, date: Array<String>) {
        val popupView = layoutInflater.inflate(R.layout.timetable_info, null)
        val layoutEven = popupView.findViewById<LinearLayout>(R.id.frame_week_even)
        val layoutOdd = popupView.findViewById<LinearLayout>(R.id.frame_week_odd)

        val times = timetable.getJSONArray("times")
        val lessons = timetable.getJSONArray("lessons")
        val weekEven = timetable.getJSONArray("even")
        val weekOdd = timetable.getJSONArray("odd")
        val lessonData = lessons.getJSONArray(lessonId)
        val weekdays = resources.getStringArray(R.array.weekdays)

        val lessonIds = ArrayList<Int>()
        lessonIds.add(lessonId)
        if (lessonData.getString(3).isNotBlank()) {
            lessonData.getString(3).split("|").forEach { lessonIds.add(it.toInt()) }
        }

        val teachers = ArrayList<String>()
        val rooms = ArrayList<String>()
        teachers.add(lessonData.getString(2).split("|").joinToString(" "))
        rooms.add(resources.getString(R.string.placeholder_room, lessonData.getString(1)))

        if (lessonIds.count() > 1) {
            lessonIds.forEach { otherId ->
                val lesson = lessons.getJSONArray(otherId)
                val teacher = lesson.getString(2).split("|").joinToString(" ")
                val room = resources.getString(R.string.placeholder_room, lesson.getString(1))

                var isOtherTeacher = true
                teachers.forEach { if (teacher == it) isOtherTeacher = false }
                if (isOtherTeacher) teachers.add(teacher)

                var isOtherRoom = true
                rooms.forEach { if (room == it) isOtherRoom = false }
                if (isOtherRoom) rooms.add(room)
            }

            if (teachers.count() > 1) popupView.findViewById<TextView>(R.id.title_teacher).text =
                resources.getString(R.string.info_teachers)
            if (rooms.count() > 1) popupView.findViewById<TextView>(R.id.title_room).text =
                resources.getString(R.string.info_rooms)
        }

        popupView.findViewById<TextView>(R.id.text_name).text = lessonData.getString(0)
        popupView.findViewById<TextView>(R.id.text_teacher).text = teachers.joinToString(",\n")
        popupView.findViewById<TextView>(R.id.text_room).text = rooms.joinToString(",\n")

        var isDifferent = false

        var dayEven = -1
        for (d: Int in 0 until 7) {
            for (l: Int in 0 until times.length()) {
                lessonIds.forEach {
                    if (weekEven.getJSONArray(d).getInt(l) == it) {
                        if (dayEven < d) {
                            dayEven = d
                            val dayName = layoutInflater.inflate(R.layout.timetable_page, null)
                            dayName.findViewById<TextView>(R.id.title).text = weekdays[d]
                            layoutEven.addView(dayName)
                        }
                        if (weekOdd.getJSONArray(d).getInt(l) != it) isDifferent = true
                        val isNow = date.contentEquals(arrayOf("even", d.toString(), l.toString()))
                        fillLesson(layoutEven, l, it, weekOdd.getJSONArray(d).getInt(l), date, isNow, true)
                    }
                }
            }
        }
        if (layoutEven.childCount < 2) layoutEven.visibility = View.GONE

        var dayOdd = -1
        for (d: Int in 0 until 7) {
            for (l: Int in 0 until times.length()) {
                lessonIds.forEach {
                    if (weekOdd.getJSONArray(d).getInt(l) == it) {
                        if (dayOdd < d) {
                            dayOdd = d
                            val dayName = layoutInflater.inflate(R.layout.timetable_page, null)
                            dayName.findViewById<TextView>(R.id.title).text = weekdays[d]
                            layoutOdd.addView(dayName)
                        }
                        if (weekEven.getJSONArray(d).getInt(l) != it) isDifferent = true
                        val isNow = date.contentEquals(arrayOf("odd", d.toString(), l.toString()))
                        fillLesson(layoutOdd, l, it, weekEven.getJSONArray(d).getInt(l), date, isNow, true)
                    }
                }
            }
        }
        if (layoutOdd.childCount < 2) layoutOdd.visibility = View.GONE

        if (!isDifferent) if (date[0] == "odd") {
            layoutEven.visibility = View.GONE
            popupView.findViewById<TextView>(R.id.title_week_odd).text = resources.getString(R.string.info_week_every)
        } else {
            layoutOdd.visibility = View.GONE
            popupView.findViewById<TextView>(R.id.title_week_even).text = resources.getString(R.string.info_week_every)
        }

        val frameMain = requireView().findViewById<FrameLayout>(R.id.frame_main)
        val popupWindow = PopupWindow(popupView, frameMain.width, frameMain.height / 2, true)
        popupWindow.showAsDropDown(view)
    }
}