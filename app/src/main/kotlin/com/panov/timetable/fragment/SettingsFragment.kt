package com.panov.timetable.fragment

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.textfield.TextInputEditText
import com.panov.timetable.AppUtils
import com.panov.timetable.R
import com.panov.timetable.Storage
import com.panov.timetable.WidgetService
import com.panov.util.Converter
import com.panov.util.UiUtils
import com.panov.util.WebUtils
import java.util.Locale
import kotlin.math.abs

class SettingsFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val fragment = inflater.inflate(R.layout.fragment_settings, container, false)

        // Application
        fragment.findViewById<Button>(R.id.button_theme_system).setOnClickListener { setTheme(fragment, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) }
        fragment.findViewById<Button>(R.id.button_theme_light).setOnClickListener { setTheme(fragment, AppCompatDelegate.MODE_NIGHT_NO) }
        fragment.findViewById<Button>(R.id.button_theme_dark).setOnClickListener { setTheme(fragment, AppCompatDelegate.MODE_NIGHT_YES) }
        setTheme(fragment, Storage.settings.getInt(Storage.Application.THEME, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM), true)

        fragment.findViewById<Button>(R.id.button_language_english).setOnClickListener { setLanguage(fragment, "en") }
        fragment.findViewById<Button>(R.id.button_language_russian).setOnClickListener { setLanguage(fragment, "ru") }
        setLanguage(fragment, Storage.settings.getString(Storage.Application.LANGUAGE, Locale.getDefault().language), true)

        // Timetable
        UiUtils.setupButtonGroup(arrayOf(fragment.findViewById(R.id.button_initial_index_zero), fragment.findViewById(R.id.button_initial_index_one)))

        // Widgets
        fragment.findViewById<MaterialSwitch>(R.id.switch_update_by_timer).setOnCheckedChangeListener { _, isChecked ->
            UiUtils.setViewVisibility(fragment.findViewById(R.id.layout_update_by_timer), if (isChecked) View.VISIBLE else View.GONE)
        }
        fragment.findViewById<MaterialSwitch>(R.id.switch_modifiers).setOnCheckedChangeListener { _, isChecked ->
            UiUtils.setViewVisibility(fragment.findViewById(R.id.layout_modifiers), if (isChecked) View.VISIBLE else View.GONE)
        }
        UiUtils.setupButtonGroup(arrayOf(fragment.findViewById(R.id.button_modifier_hours_round), fragment.findViewById(R.id.button_modifier_hours_set)))
        UiUtils.setupButtonGroup(arrayOf(fragment.findViewById(R.id.button_modifier_minutes_round), fragment.findViewById(R.id.button_modifier_minutes_set)))
        UiUtils.setupButtonGroup(arrayOf(fragment.findViewById(R.id.button_modifier_seconds_round), fragment.findViewById(R.id.button_modifier_seconds_set)))

        // About
        fragment.findViewById<Button>(R.id.button_source_code).setOnClickListener {
            WebUtils.openURL(requireContext(), "https://github.com/pa-nov/Timetable")
        }
        fragment.findViewById<Button>(R.id.button_timetable_editor).setOnClickListener {
            WebUtils.openURL(requireContext(), "https://github.com/pa-nov/TimetableEditor")
        }
        fragment.findViewById<TextView>(R.id.text_app_version).text = getString(
            R.string.app_version, requireContext().packageManager.getPackageInfo(requireContext().packageName, PackageManager.GET_ACTIVITIES).versionName
        )

        // Read and Save
        readSettings(fragment)
        fragment.findViewById<Button>(R.id.button_action).setOnClickListener { saveSettings(fragment) }

        return fragment
    }

    private fun setTheme(view: View, theme: Int, onlyRead: Boolean = false) {
        if (!onlyRead) {
            Storage.settings.saveInt(Storage.Application.THEME, theme)
            AppCompatDelegate.setDefaultNightMode(theme)
        }
        view.findViewById<Button>(R.id.button_theme_system).isEnabled = theme != AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        view.findViewById<Button>(R.id.button_theme_light).isEnabled = theme != AppCompatDelegate.MODE_NIGHT_NO
        view.findViewById<Button>(R.id.button_theme_dark).isEnabled = theme != AppCompatDelegate.MODE_NIGHT_YES
    }

    private fun setLanguage(view: View, language: String, onlyRead: Boolean = false) {
        if (!onlyRead) {
            Storage.settings.saveString(Storage.Application.LANGUAGE, language)
            requireActivity().recreate()
        }
        view.findViewById<Button>(R.id.button_language_english).isEnabled = language != "en"
        view.findViewById<Button>(R.id.button_language_russian).isEnabled = language != "ru"
    }

    private fun readSettings(view: View) {
        val initialIndex = Storage.settings.getInt(Storage.Timetable.INITIAL_INDEX, 1)
        view.findViewById<Button>(if (initialIndex > 0) R.id.button_initial_index_one else R.id.button_initial_index_zero).performClick()
        view.findViewById<TextInputEditText>(R.id.input_timetable_json).setText(Storage.settings.getString(Storage.Timetable.JSON))

        view.findViewById<MaterialSwitch>(R.id.switch_combine_background).isChecked = Storage.settings.getBoolean(Storage.Widgets.COMBINE_BACKGROUND)
        view.findViewById<MaterialSwitch>(R.id.switch_update_on_unlock).isChecked = Storage.settings.getBoolean(Storage.Widgets.UPDATE_ON_UNLOCK)
        view.findViewById<MaterialSwitch>(R.id.switch_update_by_timetable).isChecked = Storage.settings.getBoolean(Storage.Widgets.UPDATE_BY_TIMETABLE)

        val updateTimer = Storage.settings.getInt(Storage.Widgets.UPDATE_TIMER)
        view.findViewById<MaterialSwitch>(R.id.switch_update_by_timer).isChecked = updateTimer > 0
        view.findViewById<TextInputEditText>(R.id.input_timer_hours).setText((updateTimer / 3600).toString())
        view.findViewById<TextInputEditText>(R.id.input_timer_minutes).setText((updateTimer / 60 % 60).toString())
        view.findViewById<TextInputEditText>(R.id.input_timer_seconds).setText((updateTimer % 60).toString())

        val modifierHours = Storage.settings.getInt(Storage.Widgets.MODIFIER_HOURS, 1)
        val modifierMinutes = Storage.settings.getInt(Storage.Widgets.MODIFIER_MINUTES, 1)
        val modifierSeconds = Storage.settings.getInt(Storage.Widgets.MODIFIER_SECONDS, 1)

        view.findViewById<MaterialSwitch>(R.id.switch_modifiers).isChecked = !(modifierHours == 1 && modifierMinutes == 1 && modifierSeconds == 1)
        view.findViewById<TextInputEditText>(R.id.input_modifier_hours).setText(abs(modifierHours).toString())
        view.findViewById<TextInputEditText>(R.id.input_modifier_minutes).setText(abs(modifierMinutes).toString())
        view.findViewById<TextInputEditText>(R.id.input_modifier_seconds).setText(abs(modifierSeconds).toString())

        view.findViewById<Button>(if (modifierHours > 0) R.id.button_modifier_hours_round else R.id.button_modifier_hours_set).performClick()
        view.findViewById<Button>(if (modifierMinutes > 0) R.id.button_modifier_minutes_round else R.id.button_modifier_minutes_set).performClick()
        view.findViewById<Button>(if (modifierSeconds > 0) R.id.button_modifier_seconds_round else R.id.button_modifier_seconds_set).performClick()
    }

    private fun saveSettings(view: View) {
        val initialIndex = if (view.findViewById<Button>(R.id.button_initial_index_one).isEnabled) 0 else 1
        Storage.settings.setInt(Storage.Timetable.INITIAL_INDEX, initialIndex)

        val timetableJson = view.findViewById<TextInputEditText>(R.id.input_timetable_json).text.toString()
        Storage.settings.setString(Storage.Timetable.JSON, timetableJson)

        Storage.settings.setBoolean(Storage.Widgets.COMBINE_BACKGROUND, view.findViewById<MaterialSwitch>(R.id.switch_combine_background).isChecked)
        Storage.settings.setBoolean(Storage.Widgets.UPDATE_ON_UNLOCK, view.findViewById<MaterialSwitch>(R.id.switch_update_on_unlock).isChecked)
        Storage.settings.setBoolean(Storage.Widgets.UPDATE_BY_TIMETABLE, view.findViewById<MaterialSwitch>(R.id.switch_update_by_timetable).isChecked)

        if (view.findViewById<MaterialSwitch>(R.id.switch_update_by_timer).isChecked) {
            val timerHours = Converter.getIntFromInput(view.findViewById(R.id.input_timer_hours))
            val timerMinutes = Converter.getIntFromInput(view.findViewById(R.id.input_timer_minutes))
            val timerSeconds = Converter.getIntFromInput(view.findViewById(R.id.input_timer_seconds))
            Storage.settings.setInt(Storage.Widgets.UPDATE_TIMER, timerHours * 3600 + timerMinutes * 60 + timerSeconds)
        } else {
            Storage.settings.setInt(Storage.Widgets.UPDATE_TIMER, 0)
        }

        if (view.findViewById<MaterialSwitch>(R.id.switch_modifiers).isChecked) {
            val modifierHours = Converter.getIntFromInput(view.findViewById(R.id.input_modifier_hours), 1)
            val modifierMinutes = Converter.getIntFromInput(view.findViewById(R.id.input_modifier_minutes), 1)
            val modifierSeconds = Converter.getIntFromInput(view.findViewById(R.id.input_modifier_seconds), 1)

            Storage.settings.setInt(
                Storage.Widgets.MODIFIER_HOURS,
                if (view.findViewById<Button>(R.id.button_modifier_hours_round).isEnabled) -modifierHours else modifierHours
            )
            Storage.settings.setInt(
                Storage.Widgets.MODIFIER_MINUTES,
                if (view.findViewById<Button>(R.id.button_modifier_minutes_round).isEnabled) -modifierMinutes else modifierMinutes
            )
            Storage.settings.setInt(
                Storage.Widgets.MODIFIER_SECONDS,
                if (view.findViewById<Button>(R.id.button_modifier_seconds_round).isEnabled) -modifierSeconds else modifierSeconds
            )
        } else {
            Storage.settings.setInt(Storage.Widgets.MODIFIER_HOURS, 1)
            Storage.settings.setInt(Storage.Widgets.MODIFIER_MINUTES, 1)
            Storage.settings.setInt(Storage.Widgets.MODIFIER_SECONDS, 1)
        }

        Storage.settings.save()
        UiUtils.showToast(requireContext(), R.string.message_applied)
        UiUtils.clearFocus(requireView())
        readSettings(view)

        Storage.timetable = AppUtils.getTimetableData(timetableJson)
        requireContext().stopService(Intent(requireContext(), WidgetService::class.java))
        AppUtils.startWidgetService(requireContext())
    }
}