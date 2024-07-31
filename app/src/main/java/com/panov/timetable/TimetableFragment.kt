package com.panov.timetable

import android.app.DatePickerDialog
import android.icu.util.Calendar
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.panov.timetable.utils.TimetableAdapter
import com.panov.timetable.utils.Tools
import kotlin.math.abs

class TimetableFragment : Fragment() {
    private var tempPosition = 0

    val calendar: Calendar = Calendar.getInstance()

    init {
        calendar.firstDayOfWeek = Calendar.MONDAY
        calendar.minimalDaysInFirstWeek = 4
        resetCalendar()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val fragment = inflater.inflate(R.layout.fragment_timetable, container, false)

        val transformer = CompositePageTransformer()
        transformer.addTransformer { page, position ->
            val offset = 1 - abs(position)
            page.translationX = position * Tools.convertDpToPx(requireContext(), -40)
            page.scaleX = 0.8f + offset * 0.2f
            page.scaleY = 0.8f + offset * 0.2f
            page.alpha = offset
        }

        val viewTimetable = fragment.findViewById<ViewPager2>(R.id.layout_container)
        viewTimetable.getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        viewTimetable.adapter = TimetableAdapter(this)
        viewTimetable.setCurrentItem(1, false)
        viewTimetable.setPageTransformer(transformer)
        viewTimetable.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                if (position < 1 && positionOffset <= 0) {
                    viewTimetable.setCurrentItem(1, false)
                    calendar.add(Calendar.DAY_OF_MONTH, -1)
                    calendar.get(Calendar.DAY_OF_YEAR)
                }
                if (position > 1) {
                    viewTimetable.setCurrentItem(1, false)
                    calendar.add(Calendar.DAY_OF_MONTH, 1)
                    calendar.get(Calendar.DAY_OF_YEAR)
                }

                tempPosition = if (position < 1) if (positionOffset < 0.5f) -1 else 0 else if (positionOffset > 0.5f) 1 else 0
                val tempCalendar = calendar.clone() as Calendar
                tempCalendar.add(Calendar.DAY_OF_MONTH, tempPosition)
                fragment.findViewById<Button>(R.id.button_action).text = Tools.getDateText(tempCalendar)
            }

            override fun onPageSelected(position: Int) {
                updateView(fragment)
            }
        })

        fragment.findViewById<Button>(R.id.button_action).setOnClickListener {
            val tempCalendar = calendar.clone() as Calendar
            tempCalendar.add(Calendar.DAY_OF_MONTH, tempPosition)
            DatePickerDialog(requireContext(), R.style.Theme_DatePickerDialog, { _, year, month, day ->
                run {
                    calendar.set(Calendar.YEAR, year)
                    calendar.set(Calendar.MONTH, month)
                    calendar.set(Calendar.DAY_OF_MONTH, day)
                    calendar.get(Calendar.DAY_OF_YEAR)
                    updateView(fragment)
                }
            }, tempCalendar.get(Calendar.YEAR), tempCalendar.get(Calendar.MONTH), tempCalendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        return fragment
    }

    private fun resetCalendar() {
        val temp = Calendar.getInstance()
        calendar.clear()
        calendar.set(Calendar.YEAR, temp.get(Calendar.YEAR))
        calendar.set(Calendar.MONTH, temp.get(Calendar.MONTH))
        calendar.set(Calendar.DAY_OF_MONTH, temp.get(Calendar.DAY_OF_MONTH))
        calendar.get(Calendar.DAY_OF_YEAR)
    }

    private fun updateView(view: View) {
        val viewAdapter = view.findViewById<ViewPager2>(R.id.layout_container).adapter as TimetableAdapter
        viewAdapter.notifyItemChanged(0)
        viewAdapter.notifyItemChanged(1)
        viewAdapter.notifyItemChanged(2)
        view.findViewById<Button>(R.id.button_action).text = Tools.getDateText(calendar)
    }
}