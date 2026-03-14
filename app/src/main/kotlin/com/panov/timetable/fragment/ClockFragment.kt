package com.panov.timetable.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.google.android.material.materialswitch.MaterialSwitch
import com.panov.timetable.R
import com.panov.timetable.util.Storage
import com.panov.util.UiUtils

class ClockFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val fragment = inflater.inflate(R.layout.fragment_clock, container, false)

        fragment.findViewById<Button>(R.id.button_clock_mode_clock).setOnClickListener {
            setClockMode(mode = false, animate = true)
        }
        fragment.findViewById<Button>(R.id.button_clock_mode_timer).setOnClickListener {
            setClockMode(mode = true, animate = true)
        }
        fragment.findViewById<Button>(R.id.button_action).setOnClickListener {
            saveSettings()
        }

        return fragment
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        readSettings()
    }

    private fun setClockMode(mode: Boolean, animate: Boolean = false) {
        val fragment = requireView()

        fragment.findViewById<Button>(R.id.button_clock_mode_clock).isEnabled = mode
        fragment.findViewById<Button>(R.id.button_clock_mode_timer).isEnabled = !mode
        fragment.findViewById<MaterialSwitch>(R.id.switch_display_date_time).text = getString(
            if (mode) R.string.display_date_time else R.string.display_date
        )

        if (animate) {
            UiUtils.setViewVisibility(
                fragment.findViewById(R.id.switch_not_display_next_time), if (mode) View.VISIBLE else View.GONE
            )
        } else {
            UiUtils.setViewVisibility(
                fragment.findViewById(R.id.switch_not_display_next_time), if (mode) View.VISIBLE else View.GONE, 0
            )
        }
    }

    private fun readSettings() {
        val fragment = requireView()

        val displayTimer = Storage.settings.getBoolean(Storage.Clock.DISPLAY_TIMER, true)
        val displayOnLockscreen = Storage.settings.getBoolean(Storage.Clock.DISPLAY_ON_LOCKSCREEN)
        val displayHeaders = Storage.settings.getBoolean(Storage.Clock.DISPLAY_HEADERS)
        val displayDateTime = Storage.settings.getBoolean(Storage.Clock.DISPLAY_DATE_TIME)
        val displayCurrentLesson = Storage.settings.getBoolean(Storage.Clock.DISPLAY_CURRENT_LESSON)
        val displayNextLesson = Storage.settings.getBoolean(Storage.Clock.DISPLAY_NEXT_LESSON)
        val notDisplayNextTime = Storage.settings.getBoolean(Storage.Clock.NOT_DISPLAY_NEXT_TIME)

        setClockMode(displayTimer)
        fragment.findViewById<MaterialSwitch>(R.id.switch_display_on_lockscreen).isChecked = displayOnLockscreen
        fragment.findViewById<MaterialSwitch>(R.id.switch_display_headers).isChecked = displayHeaders
        fragment.findViewById<MaterialSwitch>(R.id.switch_display_date_time).isChecked = displayDateTime
        fragment.findViewById<MaterialSwitch>(R.id.switch_display_current_lesson).isChecked = displayCurrentLesson
        fragment.findViewById<MaterialSwitch>(R.id.switch_display_next_lesson).isChecked = displayNextLesson
        fragment.findViewById<MaterialSwitch>(R.id.switch_not_display_next_time).isChecked = notDisplayNextTime
    }

    private fun saveSettings() {
        val fragment = requireView()

        val displayTimer = fragment.findViewById<Button>(R.id.button_clock_mode_clock).isEnabled
        val displayOnLockscreen = fragment.findViewById<MaterialSwitch>(R.id.switch_display_on_lockscreen).isChecked
        val displayHeaders = fragment.findViewById<MaterialSwitch>(R.id.switch_display_headers).isChecked
        val displayDateTime = fragment.findViewById<MaterialSwitch>(R.id.switch_display_date_time).isChecked
        val displayCurrentLesson = fragment.findViewById<MaterialSwitch>(R.id.switch_display_current_lesson).isChecked
        val displayNextLesson = fragment.findViewById<MaterialSwitch>(R.id.switch_display_next_lesson).isChecked
        val notDisplayNextTime = fragment.findViewById<MaterialSwitch>(R.id.switch_not_display_next_time).isChecked

        Storage.settings.setBoolean(Storage.Clock.DISPLAY_TIMER, displayTimer)
        Storage.settings.setBoolean(Storage.Clock.DISPLAY_ON_LOCKSCREEN, displayOnLockscreen)
        Storage.settings.setBoolean(Storage.Clock.DISPLAY_HEADERS, displayHeaders)
        Storage.settings.setBoolean(Storage.Clock.DISPLAY_DATE_TIME, displayDateTime)
        Storage.settings.setBoolean(Storage.Clock.DISPLAY_CURRENT_LESSON, displayCurrentLesson)
        Storage.settings.setBoolean(Storage.Clock.DISPLAY_NEXT_LESSON, displayNextLesson)
        Storage.settings.setBoolean(Storage.Clock.NOT_DISPLAY_NEXT_TIME, notDisplayNextTime)

        Storage.settings.save()
        UiUtils.showToast(requireContext(), R.string.message_applied)
        UiUtils.clearFocus(fragment)
        readSettings()
    }
}