package com.panov.timetable.fragment

import android.content.pm.PackageManager
import android.os.Build
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
import com.panov.timetable.R
import com.panov.timetable.util.ApplicationUtils
import com.panov.timetable.util.Storage
import com.panov.timetable.util.WidgetUtils
import com.panov.util.Converter
import com.panov.util.UiUtils
import com.panov.util.WebUtils
import java.util.Locale
import kotlin.math.abs

class SettingsFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val fragment = inflater.inflate(R.layout.fragment_settings, container, false)

        fragment.findViewById<Button>(R.id.button_theme_system).setOnClickListener {
            setTheme(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
        fragment.findViewById<Button>(R.id.button_theme_light).setOnClickListener {
            setTheme(AppCompatDelegate.MODE_NIGHT_NO)
        }
        fragment.findViewById<Button>(R.id.button_theme_dark).setOnClickListener {
            setTheme(AppCompatDelegate.MODE_NIGHT_YES)
        }
        fragment.findViewById<Button>(R.id.button_language_english).setOnClickListener {
            setLanguage("en")
        }
        fragment.findViewById<Button>(R.id.button_language_russian).setOnClickListener {
            setLanguage("ru")
        }

        UiUtils.setupButtonGroup(
            arrayOf(fragment.findViewById(R.id.button_initial_index_zero), fragment.findViewById(R.id.button_initial_index_one))
        )

        fragment.findViewById<MaterialSwitch>(R.id.switch_update_by_timer).setOnCheckedChangeListener { _, isChecked ->
            UiUtils.setViewVisibility(fragment.findViewById(R.id.layout_update_by_timer), if (isChecked) View.VISIBLE else View.GONE)
        }
        fragment.findViewById<MaterialSwitch>(R.id.switch_modifiers).setOnCheckedChangeListener { _, isChecked ->
            UiUtils.setViewVisibility(fragment.findViewById(R.id.layout_modifiers), if (isChecked) View.VISIBLE else View.GONE)
        }
        UiUtils.setupButtonGroup(
            arrayOf(fragment.findViewById(R.id.button_modifier_hours_round), fragment.findViewById(R.id.button_modifier_hours_set))
        )
        UiUtils.setupButtonGroup(
            arrayOf(fragment.findViewById(R.id.button_modifier_minutes_round), fragment.findViewById(R.id.button_modifier_minutes_set))
        )
        UiUtils.setupButtonGroup(
            arrayOf(fragment.findViewById(R.id.button_modifier_seconds_round), fragment.findViewById(R.id.button_modifier_seconds_set))
        )

        fragment.findViewById<Button>(R.id.button_source_code).setOnClickListener {
            WebUtils.openURL(requireContext(), "https://github.com/pa-nov/timetable")
        }
        fragment.findViewById<Button>(R.id.button_timetable_editor).setOnClickListener {
            WebUtils.openURL(requireContext(), "https://github.com/pa-nov/timetable-editor")
        }
        fragment.findViewById<TextView>(R.id.text_app_version).text = getString(
            R.string.app_version, requireContext().packageManager.getPackageInfo(requireContext().packageName, 0).versionName
        )

        fragment.findViewById<Button>(R.id.button_action).setOnClickListener {
            saveSettings()
        }

        return fragment
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTheme(Storage.settings.getInt(Storage.Application.THEME, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM), true)
        setLanguage(Storage.settings.getString(Storage.Application.LANGUAGE, Locale.getDefault().language), true)
        readSettings()
    }

    private fun setTheme(theme: Int, onlyRead: Boolean = false) {
        val fragment = requireView()

        if (!onlyRead) {
            Storage.settings.saveInt(Storage.Application.THEME, theme)
            AppCompatDelegate.setDefaultNightMode(theme)
        }

        fragment.findViewById<Button>(R.id.button_theme_system).isEnabled = theme != AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        fragment.findViewById<Button>(R.id.button_theme_light).isEnabled = theme != AppCompatDelegate.MODE_NIGHT_NO
        fragment.findViewById<Button>(R.id.button_theme_dark).isEnabled = theme != AppCompatDelegate.MODE_NIGHT_YES
    }

    private fun setLanguage(language: String, onlyRead: Boolean = false) {
        val fragment = requireView()

        if (!onlyRead) {
            Storage.settings.saveString(Storage.Application.LANGUAGE, language)
            requireActivity().recreate()
        }

        fragment.findViewById<Button>(R.id.button_language_english).isEnabled = language != "en"
        fragment.findViewById<Button>(R.id.button_language_russian).isEnabled = language != "ru"
    }

    private fun readSettings() {
        val fragment = requireView()

        val initialIndex = Storage.settings.getInt(Storage.Timetable.INITIAL_INDEX, 1)
        val timetableJson = Storage.settings.getString(Storage.Timetable.JSON)
        val combineBackground = Storage.settings.getBoolean(Storage.Widgets.COMBINE_BACKGROUND)
        val updateOnUnlock = Storage.settings.getBoolean(Storage.Widgets.UPDATE_ON_UNLOCK)
        val updateByTimetable = Storage.settings.getBoolean(Storage.Widgets.UPDATE_BY_TIMETABLE)
        val updateTimer = Storage.settings.getInt(Storage.Widgets.UPDATE_TIMER)
        val modifierHours = Storage.settings.getInt(Storage.Widgets.MODIFIER_HOURS, 1)
        val modifierMinutes = Storage.settings.getInt(Storage.Widgets.MODIFIER_MINUTES, 1)
        val modifierSeconds = Storage.settings.getInt(Storage.Widgets.MODIFIER_SECONDS, 1)

        fragment.findViewById<Button>(if (initialIndex > 0) R.id.button_initial_index_one else R.id.button_initial_index_zero).performClick()
        fragment.findViewById<TextInputEditText>(R.id.input_timetable_json).setText(timetableJson)
        fragment.findViewById<MaterialSwitch>(R.id.switch_combine_background).isChecked = combineBackground
        fragment.findViewById<MaterialSwitch>(R.id.switch_update_on_unlock).isChecked = updateOnUnlock
        fragment.findViewById<MaterialSwitch>(R.id.switch_update_by_timetable).isChecked = updateByTimetable

        fragment.findViewById<MaterialSwitch>(R.id.switch_update_by_timer).isChecked = updateTimer > 0
        fragment.findViewById<TextInputEditText>(R.id.input_timer_hours).setText((updateTimer / 3600).toString())
        fragment.findViewById<TextInputEditText>(R.id.input_timer_minutes).setText((updateTimer / 60 % 60).toString())
        fragment.findViewById<TextInputEditText>(R.id.input_timer_seconds).setText((updateTimer % 60).toString())

        fragment.findViewById<MaterialSwitch>(R.id.switch_modifiers).isChecked =
            !(modifierHours == 1 && modifierMinutes == 1 && modifierSeconds == 1)
        fragment.findViewById<TextInputEditText>(R.id.input_modifier_hours).setText(abs(modifierHours).toString())
        fragment.findViewById<TextInputEditText>(R.id.input_modifier_minutes).setText(abs(modifierMinutes).toString())
        fragment.findViewById<TextInputEditText>(R.id.input_modifier_seconds).setText(abs(modifierSeconds).toString())
        fragment.findViewById<Button>(if (modifierHours > 0) R.id.button_modifier_hours_round else R.id.button_modifier_hours_set)
            .performClick()
        fragment.findViewById<Button>(if (modifierMinutes > 0) R.id.button_modifier_minutes_round else R.id.button_modifier_minutes_set)
            .performClick()
        fragment.findViewById<Button>(if (modifierSeconds > 0) R.id.button_modifier_seconds_round else R.id.button_modifier_seconds_set)
            .performClick()
    }

    private fun saveSettings() {
        val fragment = requireView()

        val initialIndex = if (fragment.findViewById<Button>(R.id.button_initial_index_one).isEnabled) 0 else 1
        val timetableJson = fragment.findViewById<TextInputEditText>(R.id.input_timetable_json).text.toString()
        val combineBackground = fragment.findViewById<MaterialSwitch>(R.id.switch_combine_background).isChecked
        val updateOnUnlock = fragment.findViewById<MaterialSwitch>(R.id.switch_update_on_unlock).isChecked
        val updateByTimetable = fragment.findViewById<MaterialSwitch>(R.id.switch_update_by_timetable).isChecked
        val updateTimer = if (fragment.findViewById<MaterialSwitch>(R.id.switch_update_by_timer).isChecked) {
            val timerHours = Converter.getIntFromInput(fragment.findViewById(R.id.input_timer_hours))
            val timerMinutes = Converter.getIntFromInput(fragment.findViewById(R.id.input_timer_minutes))
            val timerSeconds = Converter.getIntFromInput(fragment.findViewById(R.id.input_timer_seconds))
            ((timerHours * 60) + timerMinutes) * 60 + timerSeconds
        } else 0
        val modifierHours = if (fragment.findViewById<MaterialSwitch>(R.id.switch_modifiers).isChecked) {
            val modifier = Converter.getIntFromInput(fragment.findViewById(R.id.input_modifier_hours), 1)
            val isSet = fragment.findViewById<Button>(R.id.button_modifier_hours_round).isEnabled
            if (isSet) -modifier else modifier
        } else 1
        val modifierMinutes = if (fragment.findViewById<MaterialSwitch>(R.id.switch_modifiers).isChecked) {
            val modifier = Converter.getIntFromInput(fragment.findViewById(R.id.input_modifier_minutes), 1)
            val isSet = fragment.findViewById<Button>(R.id.button_modifier_minutes_round).isEnabled
            if (isSet) -modifier else modifier
        } else 1
        val modifierSeconds = if (fragment.findViewById<MaterialSwitch>(R.id.switch_modifiers).isChecked) {
            val modifier = Converter.getIntFromInput(fragment.findViewById(R.id.input_modifier_seconds), 1)
            val isSet = fragment.findViewById<Button>(R.id.button_modifier_seconds_round).isEnabled
            if (isSet) -modifier else modifier
        } else 1

        Storage.settings.setInt(Storage.Timetable.INITIAL_INDEX, initialIndex)
        Storage.settings.setString(Storage.Timetable.JSON, timetableJson)
        Storage.settings.setBoolean(Storage.Widgets.COMBINE_BACKGROUND, combineBackground)
        Storage.settings.setBoolean(Storage.Widgets.UPDATE_ON_UNLOCK, updateOnUnlock)
        Storage.settings.setBoolean(Storage.Widgets.UPDATE_BY_TIMETABLE, updateByTimetable)
        Storage.settings.setInt(Storage.Widgets.UPDATE_TIMER, updateTimer)
        Storage.settings.setInt(Storage.Widgets.MODIFIER_HOURS, modifierHours)
        Storage.settings.setInt(Storage.Widgets.MODIFIER_MINUTES, modifierMinutes)
        Storage.settings.setInt(Storage.Widgets.MODIFIER_SECONDS, modifierSeconds)

        Storage.settings.save()
        UiUtils.showToast(requireContext(), R.string.message_applied)
        UiUtils.clearFocus(fragment)
        readSettings()

        Storage.timetable = ApplicationUtils.getTimetableData(timetableJson)
        WidgetUtils.updateWidgets(requireContext())
        WidgetUtils.stopWidgetService(requireContext())
        WidgetUtils.startWidgetService(requireContext())

        if (Build.VERSION.SDK_INT >= 33) {
            val updateOnUnlock = Storage.settings.getBoolean(Storage.Widgets.UPDATE_ON_UNLOCK)
            val updateByTimetable = Storage.settings.getBoolean(Storage.Widgets.UPDATE_BY_TIMETABLE)
            val updateByTimer = Storage.settings.getInt(Storage.Widgets.UPDATE_TIMER) > 0

            if (updateOnUnlock || updateByTimetable || updateByTimer) {
                val isGranted = requireContext().checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                if (isGranted != PackageManager.PERMISSION_GRANTED) {
                    requireActivity().requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 0)
                }
            }
        }
    }
}