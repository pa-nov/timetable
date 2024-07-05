package com.panov.timetable

import android.content.Intent
import android.net.Uri
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
    private var widgetModifierHourMode = 0
    private var widgetModifierMinuteMode = 0
    private var widgetModifierSecondMode = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val fragment = inflater.inflate(R.layout.fragment_settings, container, false)

        // Application settings
        fragment.findViewById<Button>(R.id.button_theme_system).setOnClickListener { setTheme(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM, fragment) }
        fragment.findViewById<Button>(R.id.button_theme_dark).setOnClickListener { setTheme(AppCompatDelegate.MODE_NIGHT_YES, fragment) }
        fragment.findViewById<Button>(R.id.button_theme_light).setOnClickListener { setTheme(AppCompatDelegate.MODE_NIGHT_NO, fragment) }
        setTheme(Storage.settings.getInt("app_theme", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM), fragment)

        val inputInitialIndex = fragment.findViewById<TextInputEditText>(R.id.input_initial_index)
        inputInitialIndex.setText(Storage.settings.getInt("app_initial_index", 1).toString())

        // Timetable settings
        val inputTimetableJson = fragment.findViewById<TextInputEditText>(R.id.input_timetable_json)
        inputTimetableJson.setText(Storage.settings.getString("timetable_json", ""))

        // Widget settings
        val inputModHour = fragment.findViewById<TextInputEditText>(R.id.input_mod_hour)
        val widgetModifierHour = Storage.settings.getInt("widget_mod_hour", 1)
        widgetModifierHourMode = getModeFromModifier(widgetModifierHour)
        inputModHour.setText(
            when (widgetModifierHourMode) {
                2 -> widgetModifierHour.toString()
                1 -> (-widgetModifierHour).toString()
                else -> "1"
            }
        )
        fragment.findViewById<Button>(R.id.button_mod_hour_off).setOnClickListener { setWidgetModifierHourMode(0, fragment) }
        fragment.findViewById<Button>(R.id.button_mod_hour_set).setOnClickListener { setWidgetModifierHourMode(1, fragment) }
        fragment.findViewById<Button>(R.id.button_mod_hour_round).setOnClickListener { setWidgetModifierHourMode(2, fragment) }
        setWidgetModifierHourMode(widgetModifierHourMode, fragment)

        val inputModMinute = fragment.findViewById<TextInputEditText>(R.id.input_mod_minute)
        val widgetModifierMinute = Storage.settings.getInt("widget_mod_minute", 1)
        widgetModifierMinuteMode = getModeFromModifier(widgetModifierMinute)
        inputModMinute.setText(
            when (widgetModifierMinuteMode) {
                2 -> widgetModifierMinute.toString()
                1 -> (-widgetModifierMinute).toString()
                else -> "1"
            }
        )
        fragment.findViewById<Button>(R.id.button_mod_minute_off).setOnClickListener { setWidgetModifierMinuteMode(0, fragment) }
        fragment.findViewById<Button>(R.id.button_mod_minute_set).setOnClickListener { setWidgetModifierMinuteMode(1, fragment) }
        fragment.findViewById<Button>(R.id.button_mod_minute_round).setOnClickListener { setWidgetModifierMinuteMode(2, fragment) }
        setWidgetModifierMinuteMode(widgetModifierMinuteMode, fragment)

        val inputModSecond = fragment.findViewById<TextInputEditText>(R.id.input_mod_second)
        val widgetModifierSecond = Storage.settings.getInt("widget_mod_second", 1)
        widgetModifierSecondMode = getModeFromModifier(widgetModifierSecond)
        inputModSecond.setText(
            when (widgetModifierSecondMode) {
                2 -> widgetModifierSecond.toString()
                1 -> (-widgetModifierSecond).toString()
                else -> "1"
            }
        )
        fragment.findViewById<Button>(R.id.button_mod_second_off).setOnClickListener { setWidgetModifierSecondMode(0, fragment) }
        fragment.findViewById<Button>(R.id.button_mod_second_set).setOnClickListener { setWidgetModifierSecondMode(1, fragment) }
        fragment.findViewById<Button>(R.id.button_mod_second_round).setOnClickListener { setWidgetModifierSecondMode(2, fragment) }
        setWidgetModifierSecondMode(widgetModifierSecondMode, fragment)

        // Buttons
        fragment.findViewById<Button>(R.id.button_action).setOnClickListener {
            val editor = Storage.settings.edit()

            editor.putInt("app_initial_index", getIntFromInput(inputInitialIndex, 1))

            editor.putString("timetable_json", inputTimetableJson.text.toString())

            editor.putInt(
                "widget_mod_hour", when (widgetModifierHourMode) {
                    2 -> getIntFromInput(inputModHour, 1)
                    1 -> -getIntFromInput(inputModHour, 0)
                    else -> 1
                }
            )
            editor.putInt(
                "widget_mod_minute", when (widgetModifierMinuteMode) {
                    2 -> getIntFromInput(inputModMinute, 1)
                    1 -> -getIntFromInput(inputModMinute, 0)
                    else -> 1
                }
            )
            editor.putInt(
                "widget_mod_second", when (widgetModifierSecondMode) {
                    2 -> getIntFromInput(inputModSecond, 1)
                    1 -> -getIntFromInput(inputModSecond, 0)
                    else -> 1
                }
            )

            editor.apply()
            Tools.showToast(requireContext(), R.string.message_applied)
        }

        fragment.findViewById<Button>(R.id.button_source_code).setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/pa-nov/Timetable")))
        }

        return fragment
    }

    private fun setTheme(theme: Int, fragment: View) {
        AppCompatDelegate.setDefaultNightMode(theme)
        Storage.settings.edit().putInt("app_theme", theme).apply()
        fragment.findViewById<Button>(R.id.button_theme_dark).isEnabled = theme != AppCompatDelegate.MODE_NIGHT_YES
        fragment.findViewById<Button>(R.id.button_theme_light).isEnabled = theme != AppCompatDelegate.MODE_NIGHT_NO
        fragment.findViewById<Button>(R.id.button_theme_system).isEnabled =
            theme == AppCompatDelegate.MODE_NIGHT_YES || theme == AppCompatDelegate.MODE_NIGHT_NO
    }

    private fun setWidgetModifierHourMode(mode: Int, fragment: View) {
        widgetModifierHourMode = mode
        fragment.findViewById<Button>(R.id.button_mod_hour_off).isEnabled = mode != 0
        fragment.findViewById<Button>(R.id.button_mod_hour_set).isEnabled = mode != 1
        fragment.findViewById<Button>(R.id.button_mod_hour_round).isEnabled = mode != 2
    }

    private fun setWidgetModifierMinuteMode(mode: Int, fragment: View) {
        widgetModifierMinuteMode = mode
        fragment.findViewById<Button>(R.id.button_mod_minute_off).isEnabled = mode != 0
        fragment.findViewById<Button>(R.id.button_mod_minute_set).isEnabled = mode != 1
        fragment.findViewById<Button>(R.id.button_mod_minute_round).isEnabled = mode != 2
    }

    private fun setWidgetModifierSecondMode(mode: Int, fragment: View) {
        widgetModifierSecondMode = mode
        fragment.findViewById<Button>(R.id.button_mod_second_off).isEnabled = mode != 0
        fragment.findViewById<Button>(R.id.button_mod_second_set).isEnabled = mode != 1
        fragment.findViewById<Button>(R.id.button_mod_second_round).isEnabled = mode != 2
    }

    private fun getIntFromInput(input: TextInputEditText, default: Int): Int {
        val number = input.text.toString().toIntOrNull()

        if (number != null) {
            return number
        } else {
            input.setText(default.toString())
            return default
        }
    }

    private fun getModeFromModifier(modifier: Int): Int {
        if (modifier > 1) return 2
        if (modifier < 1) return 1
        return 0
    }
}