package com.panov.timetable

import android.os.Bundle
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import org.json.JSONObject
import java.util.Calendar

class TimetableFragment : Fragment() {
    private var date = Calendar.getInstance()
    private var data = JSONObject()
    private var initialIndex = 1
    private var tempPosition = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_timetable, container, false)
        val fragment: TimetableFragment = this
        date.firstDayOfWeek = Calendar.MONDAY
        date.minimalDaysInFirstWeek = 4

        try {
            val savedData = requireActivity().getSharedPreferences("SavedTimetable", 0)
            data = JSONObject(savedData.getString("Json", "") ?: "")
            initialIndex = savedData.getInt("InitialIndex", 1)
        } catch (e: Exception) {
            view.findViewById<TextView>(R.id.buttonAction).text = resources.getString(R.string.error)
            return view
        }

        val pages = view.findViewById<ViewPager2>(R.id.pages)
        pages.adapter = TimetablePageAdapter(fragment)
        pages.setCurrentItem(1, false)
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
                    pages.adapter = TimetablePageAdapter(fragment)
                    pages.setCurrentItem(1, false)
                }
            }
        })

        return view
    }

    fun updatePage(layout: LinearLayout, position: Int) {
        val tempDate = date.clone() as Calendar
        tempDate.add(Calendar.DAY_OF_YEAR, position - 1)

        val dateDayOfWeek = if (tempDate.get(Calendar.DAY_OF_WEEK) > 1) tempDate.get(Calendar.DAY_OF_WEEK) - 2 else 6
        val dateWeekOddOrEven = if (tempDate.get(Calendar.WEEK_OF_YEAR) % 2 == 0) "even" else "odd"
        val currentTimetable = data.getJSONArray(dateWeekOddOrEven).getJSONArray(dateDayOfWeek)
        val anotherTimetable = data.getJSONArray(if (dateWeekOddOrEven == "odd") "even" else "odd").getJSONArray(dateDayOfWeek)

        layout.findViewById<TextView>(R.id.title).text = resources.getStringArray(R.array.weekdays)[dateDayOfWeek]

        for (i: Int in 0 until data.getJSONArray("times").length()) {

        }
    }

    fun updateDate() {
        val dateWeek = Tools.getTwoDigitNumber(date.get(Calendar.WEEK_OF_YEAR))
        val dateText = requireView().findViewById<TextView>(R.id.buttonAction)

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
}