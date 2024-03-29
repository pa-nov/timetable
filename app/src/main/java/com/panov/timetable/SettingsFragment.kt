package com.panov.timetable

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText

class SettingsFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val fragment = inflater.inflate(R.layout.fragment_settings, container, false)
        val timetable = requireActivity().getSharedPreferences("Timetable", 0)
        val settings = requireActivity().getSharedPreferences("Settings", 0)

        val buttonSystem = fragment.findViewById<Button>(R.id.button_system)
        val buttonDark = fragment.findViewById<Button>(R.id.button_dark)
        val buttonLight = fragment.findViewById<Button>(R.id.button_light)
        when (settings.getInt("Theme", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)) {
            AppCompatDelegate.MODE_NIGHT_YES -> buttonDark.isEnabled = false
            AppCompatDelegate.MODE_NIGHT_NO -> buttonLight.isEnabled = false
            else -> buttonSystem.isEnabled = false
        }

        buttonSystem.setOnClickListener {
            settings.edit().putInt("Theme", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM).apply()
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            buttonSystem.isEnabled = false
            buttonDark.isEnabled = true
            buttonLight.isEnabled = true
        }
        buttonDark.setOnClickListener {
            settings.edit().putInt("Theme", AppCompatDelegate.MODE_NIGHT_YES).apply()
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            buttonSystem.isEnabled = true
            buttonDark.isEnabled = false
            buttonLight.isEnabled = true
        }
        buttonLight.setOnClickListener {
            settings.edit().putInt("Theme", AppCompatDelegate.MODE_NIGHT_NO).apply()
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            buttonSystem.isEnabled = true
            buttonDark.isEnabled = true
            buttonLight.isEnabled = false
        }

        val inputModifierHour = fragment.findViewById<TextInputEditText>(R.id.input_modifier_hour)
        val inputModifierMinute = fragment.findViewById<TextInputEditText>(R.id.input_modifier_minute)
        val inputModifierSecond = fragment.findViewById<TextInputEditText>(R.id.input_modifier_second)
        val inputInitialIndex = fragment.findViewById<TextInputEditText>(R.id.input_initial_index)
        val inputJson = fragment.findViewById<TextInputEditText>(R.id.input_json)

        inputModifierHour.setText(timetable.getInt("ModifierHour", 1).toString())
        inputModifierMinute.setText(timetable.getInt("ModifierMinute", 1).toString())
        inputModifierSecond.setText(timetable.getInt("ModifierSecond", 1).toString())
        inputInitialIndex.setText(timetable.getInt("InitialIndex", 1).toString())
        inputJson.setText(timetable.getString("Json", ""))

        fragment.findViewById<Button>(R.id.button_action).setOnClickListener {
            try {
                val editor = timetable.edit()
                editor.putInt("ModifierHour", inputModifierHour.text.toString().toInt())
                editor.putInt("ModifierMinute", inputModifierMinute.text.toString().toInt())
                editor.putInt("ModifierSecond", inputModifierSecond.text.toString().toInt())
                editor.putInt("InitialIndex", inputInitialIndex.text.toString().toInt())
                editor.putString("Json", inputJson.text.toString())
                editor.apply()

                Toast.makeText(requireContext(), R.string.applied, Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), R.string.error, Toast.LENGTH_SHORT).show()
            }
        }

        return fragment
    }
}