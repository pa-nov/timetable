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
        val view = inflater.inflate(R.layout.fragment_settings, container, false)
        val savedData = requireActivity().getSharedPreferences("SavedData", 0)

        val inputModifierHour = view.findViewById<TextInputEditText>(R.id.inputModifierHour)
        val inputModifierMinute = view.findViewById<TextInputEditText>(R.id.inputModifierMinute)
        val inputModifierSecond = view.findViewById<TextInputEditText>(R.id.inputModifierSecond)
        val inputInitialIndex = view.findViewById<TextInputEditText>(R.id.inputInitialIndex)
        val inputJson = view.findViewById<TextInputEditText>(R.id.inputJson)

        inputModifierHour.setText(savedData.getInt("ModifierHour", 1).toString())
        inputModifierMinute.setText(savedData.getInt("ModifierMinute", 1).toString())
        inputModifierSecond.setText(savedData.getInt("ModifierSecond", 1).toString())
        inputInitialIndex.setText(savedData.getInt("InitialIndex", 1).toString())
        inputJson.setText(savedData.getString("Json", ""))

        view.findViewById<Button>(R.id.buttonAction).setOnClickListener {
            try {
                val editor = savedData.edit()
                editor.putInt("ModifierHour", inputModifierHour.text.toString().toInt())
                editor.putInt("ModifierMinute", inputModifierMinute.text.toString().toInt())
                editor.putInt("ModifierSecond", inputModifierSecond.text.toString().toInt())
                editor.putInt("InitialIndex", inputInitialIndex.text.toString().toInt())
                editor.putString("Json", inputJson.text.toString())
                editor.apply()

                Toast.makeText(requireActivity(), R.string.applied, Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(requireActivity(), R.string.error, Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }
}