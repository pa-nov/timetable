package com.panov.timetable.fragment

import android.app.DatePickerDialog
import android.icu.util.Calendar
import android.os.Bundle
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.panov.timetable.R
import com.panov.timetable.util.ApplicationUtils
import com.panov.timetable.widget.Timetable
import com.panov.util.Converter
import kotlin.math.abs

class TimetableFragment : Fragment() {
    val calendar: Calendar = ApplicationUtils.getCalendar()
    private var tempPosition = 0

    init {
        resetCalendar()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val fragment = inflater.inflate(R.layout.fragment_timetable, container, false)

        val transformer = CompositePageTransformer()
        transformer.addTransformer { page, position ->
            val offset = 1 - abs(position)
            page.translationX = position * Converter.getPxFromDp(requireContext(), -40)
            page.scaleX = 0.8f + offset * 0.2f
            page.scaleY = 0.8f + offset * 0.2f
            page.alpha = offset
        }

        val viewTimetable = fragment.findViewById<ViewPager2>(R.id.layout_container)
        viewTimetable.getChildAt(0).overScrollMode = View.OVER_SCROLL_NEVER
        viewTimetable.adapter = Timetable.RecyclerViewAdapter(this)
        viewTimetable.setCurrentItem(1, false)
        viewTimetable.setPageTransformer(transformer)
        viewTimetable.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                tempPosition = if (position < 1) if (positionOffset < 0.5f) -1 else 0 else if (positionOffset > 0.5f) 1 else 0
                if (position < 1 && positionOffset <= 0) selectPage(-1)
                if (position > 1) selectPage(1)
                updateDate()
            }
        })

        fragment.findViewById<Button>(R.id.button_action).setOnClickListener {
            val tempCalendar = getTempCalendar()
            DatePickerDialog(requireContext(), R.style.Theme_DatePickerDialog, { _, year, month, day ->
                run {
                    calendar.set(Calendar.YEAR, year)
                    calendar.set(Calendar.MONTH, month)
                    calendar.set(Calendar.DAY_OF_MONTH, day)
                    calendar.add(Calendar.DAY_OF_MONTH, -tempPosition)
                    updateView()
                }
            }, tempCalendar.get(Calendar.YEAR), tempCalendar.get(Calendar.MONTH), tempCalendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        return fragment
    }

    private fun selectPage(offset: Int) {
        val fragment = requireView()

        tempPosition = 0
        calendar.add(Calendar.DAY_OF_MONTH, offset)
        fragment.findViewById<ViewPager2>(R.id.layout_container).setCurrentItem(1, false)

        updateView()
    }

    private fun updateView() {
        val fragment = requireView()

        val viewAdapter = fragment.findViewById<ViewPager2>(R.id.layout_container).adapter as Timetable.RecyclerViewAdapter
        viewAdapter.notifyItemChanged(0)
        viewAdapter.notifyItemChanged(1)
        viewAdapter.notifyItemChanged(2)

        updateDate()
    }

    private fun updateDate() {
        val fragment = requireView()

        val button = fragment.findViewById<Button>(R.id.button_action)
        val tempCalendar = getTempCalendar()

        if (DateUtils.isToday(tempCalendar.timeInMillis + DateUtils.DAY_IN_MILLIS)) {
            button.text = getString(R.string.day_yesterday)
            return
        }
        if (DateUtils.isToday(tempCalendar.timeInMillis)) {
            button.text = getString(R.string.day_today)
            return
        }
        if (DateUtils.isToday(tempCalendar.timeInMillis - DateUtils.DAY_IN_MILLIS)) {
            button.text = getString(R.string.day_tomorrow)
            return
        }

        button.text = Converter.getDateText(tempCalendar)
    }

    private fun resetCalendar() {
        val tempCalendar = ApplicationUtils.getCalendar()
        calendar.clear()
        calendar.set(Calendar.YEAR, tempCalendar.get(Calendar.YEAR))
        calendar.set(Calendar.MONTH, tempCalendar.get(Calendar.MONTH))
        calendar.set(Calendar.DAY_OF_MONTH, tempCalendar.get(Calendar.DAY_OF_MONTH))
    }

    private fun getTempCalendar(): Calendar {
        val tempCalendar = calendar.clone() as Calendar
        tempCalendar.add(Calendar.DAY_OF_MONTH, tempPosition)
        return tempCalendar
    }
}