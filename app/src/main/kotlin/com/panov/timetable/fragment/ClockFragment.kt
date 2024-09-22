package com.panov.timetable.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.google.android.material.materialswitch.MaterialSwitch
import com.panov.timetable.R
import com.panov.timetable.Storage
import com.panov.util.UiUtils

class ClockFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val fragment = inflater.inflate(R.layout.fragment_clock, container, false)

        fragment.findViewById<Button>(R.id.button_clock_mode_clock).setOnClickListener { switchClockMode(fragment, false, animate = true) }
        fragment.findViewById<Button>(R.id.button_clock_mode_timer).setOnClickListener { switchClockMode(fragment, true, animate = true) }
        fragment.findViewById<Button>(R.id.button_action).setOnClickListener { saveSettings(fragment) }
        readSettings(fragment)

        return fragment
    }

    private fun switchClockMode(view: View, mode: Boolean, animate: Boolean = false) {
        view.findViewById<Button>(R.id.button_clock_mode_clock).isEnabled = mode
        view.findViewById<Button>(R.id.button_clock_mode_timer).isEnabled = !mode
        view.findViewById<MaterialSwitch>(R.id.switch_display_date_time).text = getString(if (mode) R.string.display_date_time else R.string.display_date)
        if (animate) {
            UiUtils.setViewVisibility(view.findViewById(R.id.switch_not_display_next_time), if (mode) View.VISIBLE else View.GONE)
        } else {
            UiUtils.setViewVisibility(view.findViewById(R.id.switch_not_display_next_time), if (mode) View.VISIBLE else View.GONE, 0)
        }
    }

    private fun readSettings(view: View) {
        switchClockMode(view, Storage.settings.getBoolean(Storage.Clock.DISPLAY_TIMER, true))
        view.findViewById<MaterialSwitch>(R.id.switch_display_on_lockscreen).isChecked = Storage.settings.getBoolean(Storage.Clock.DISPLAY_ON_LOCKSCREEN)
        view.findViewById<MaterialSwitch>(R.id.switch_display_headers).isChecked = Storage.settings.getBoolean(Storage.Clock.DISPLAY_HEADERS)
        view.findViewById<MaterialSwitch>(R.id.switch_display_date_time).isChecked = Storage.settings.getBoolean(Storage.Clock.DISPLAY_DATE_TIME)
        view.findViewById<MaterialSwitch>(R.id.switch_display_current_lesson).isChecked = Storage.settings.getBoolean(Storage.Clock.DISPLAY_CURRENT_LESSON)
        view.findViewById<MaterialSwitch>(R.id.switch_display_next_lesson).isChecked = Storage.settings.getBoolean(Storage.Clock.DISPLAY_NEXT_LESSON)
        view.findViewById<MaterialSwitch>(R.id.switch_not_display_next_time).isChecked = Storage.settings.getBoolean(Storage.Clock.NOT_DISPLAY_NEXT_TIME)
    }

    private fun saveSettings(view: View) {
        Storage.settings.setBoolean(Storage.Clock.DISPLAY_TIMER, view.findViewById<Button>(R.id.button_clock_mode_clock).isEnabled)
        Storage.settings.setBoolean(Storage.Clock.DISPLAY_ON_LOCKSCREEN, view.findViewById<MaterialSwitch>(R.id.switch_display_on_lockscreen).isChecked)
        Storage.settings.setBoolean(Storage.Clock.DISPLAY_HEADERS, view.findViewById<MaterialSwitch>(R.id.switch_display_headers).isChecked)
        Storage.settings.setBoolean(Storage.Clock.DISPLAY_DATE_TIME, view.findViewById<MaterialSwitch>(R.id.switch_display_date_time).isChecked)
        Storage.settings.setBoolean(Storage.Clock.DISPLAY_CURRENT_LESSON, view.findViewById<MaterialSwitch>(R.id.switch_display_current_lesson).isChecked)
        Storage.settings.setBoolean(Storage.Clock.DISPLAY_NEXT_LESSON, view.findViewById<MaterialSwitch>(R.id.switch_display_next_lesson).isChecked)
        Storage.settings.setBoolean(Storage.Clock.NOT_DISPLAY_NEXT_TIME, view.findViewById<MaterialSwitch>(R.id.switch_not_display_next_time).isChecked)
        Storage.settings.save()

        UiUtils.showToast(requireContext(), R.string.message_applied)
        UiUtils.clearFocus(requireView())
        readSettings(view)
    }
}