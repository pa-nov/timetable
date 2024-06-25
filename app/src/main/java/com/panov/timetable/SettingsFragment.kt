package com.panov.timetable

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.panov.timetable.utils.Storage
import com.panov.timetable.utils.Tools

class SettingsFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val fragment = inflater.inflate(R.layout.fragment_settings, container, false)

        // Application settings
        fragment.findViewById<Button>(R.id.button_theme_system).setOnClickListener { setTheme(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM, fragment) }
        fragment.findViewById<Button>(R.id.button_theme_dark).setOnClickListener { setTheme(AppCompatDelegate.MODE_NIGHT_YES, fragment) }
        fragment.findViewById<Button>(R.id.button_theme_light).setOnClickListener { setTheme(AppCompatDelegate.MODE_NIGHT_NO, fragment) }
        setTheme(Storage.settings.getInt("application_theme", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM), fragment)

        val inputInitialIndex = fragment.findViewById<TextInputEditText>(R.id.input_initial_index)
        inputInitialIndex.setText(Storage.settings.getInt("application_initial_index", 1).toString())

        // Timetable settings
        val inputTimetableJson = fragment.findViewById<TextInputEditText>(R.id.input_timetable_json)
        inputTimetableJson.setText(Storage.settings.getString("timetable_json", ""))

        // Widget settings
        val inputModifierHour = fragment.findViewById<TextInputEditText>(R.id.input_modifier_hour)
        inputModifierHour.setText(Storage.settings.getInt("widget_modifier_hour", 1).toString())
        val inputModifierMinute = fragment.findViewById<TextInputEditText>(R.id.input_modifier_minute)
        inputModifierMinute.setText(Storage.settings.getInt("widget_modifier_minute", 1).toString())
        val inputModifierSecond = fragment.findViewById<TextInputEditText>(R.id.input_modifier_second)
        inputModifierSecond.setText(Storage.settings.getInt("widget_modifier_second", 1).toString())

        // Apply button
        fragment.findViewById<Button>(R.id.button_action).setOnClickListener {
            if (inputInitialIndex.text.toString().toIntOrNull() == null) inputInitialIndex.setText("1")
            if (inputModifierHour.text.toString().toIntOrNull() == null) inputModifierHour.setText("1")
            if (inputModifierMinute.text.toString().toIntOrNull() == null) inputModifierMinute.setText("1")
            if (inputModifierSecond.text.toString().toIntOrNull() == null) inputModifierSecond.setText("1")

            val editor = Storage.settings.edit()
            editor.putInt("application_initial_index", inputInitialIndex.text.toString().toInt())
            editor.putString("timetable_json", inputTimetableJson.text.toString())
            editor.putInt("widget_modifier_hour", inputModifierHour.text.toString().toInt())
            editor.putInt("widget_modifier_minute", inputModifierMinute.text.toString().toInt())
            editor.putInt("widget_modifier_second", inputModifierSecond.text.toString().toInt())
            editor.apply()

            Tools.showToast(requireContext(), R.string.message_applied)
        }

        return fragment
    }

    private fun setTheme(theme: Int, fragment: View) {
        AppCompatDelegate.setDefaultNightMode(theme)
        Storage.settings.edit().putInt("application_theme", theme).apply()
        fragment.findViewById<Button>(R.id.button_theme_dark).isEnabled = theme != AppCompatDelegate.MODE_NIGHT_YES
        fragment.findViewById<Button>(R.id.button_theme_light).isEnabled = theme != AppCompatDelegate.MODE_NIGHT_NO
        fragment.findViewById<Button>(R.id.button_theme_system).isEnabled =
            theme == AppCompatDelegate.MODE_NIGHT_YES || theme == AppCompatDelegate.MODE_NIGHT_NO
    }
}