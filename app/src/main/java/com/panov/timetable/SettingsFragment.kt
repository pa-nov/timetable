package com.panov.timetable

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText

class SettingsFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val fragment = inflater.inflate(R.layout.fragment_settings, container, false)
        val savedData = requireActivity().getSharedPreferences("SavedTimetable", 0)

        val inputModifierHour = fragment.findViewById<TextInputEditText>(R.id.input_modifier_hour)
        val inputModifierMinute = fragment.findViewById<TextInputEditText>(R.id.input_modifier_minute)
        val inputModifierSecond = fragment.findViewById<TextInputEditText>(R.id.input_modifier_second)
        val inputInitialIndex = fragment.findViewById<TextInputEditText>(R.id.input_initial_index)
        val inputJson = fragment.findViewById<TextInputEditText>(R.id.input_json)

        inputModifierHour.setText(savedData.getInt("ModifierHour", 1).toString())
        inputModifierMinute.setText(savedData.getInt("ModifierMinute", 1).toString())
        inputModifierSecond.setText(savedData.getInt("ModifierSecond", 1).toString())
        inputInitialIndex.setText(savedData.getInt("InitialIndex", 1).toString())
        inputJson.setText(savedData.getString("Json", ""))

        fragment.findViewById<Button>(R.id.button_action).setOnClickListener {
            try {
                val editor = savedData.edit()
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