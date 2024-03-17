package com.panov.timetable

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
}