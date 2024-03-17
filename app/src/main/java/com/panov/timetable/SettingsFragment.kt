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

        val inputModifierHour = view.findViewById<TextInputEditText>(R.id.inputModifierHour)
        val inputModifierMinute = view.findViewById<TextInputEditText>(R.id.inputModifierMinute)
        val inputModifierSecond = view.findViewById<TextInputEditText>(R.id.inputModifierSecond)
        val inputInitialIndex = view.findViewById<TextInputEditText>(R.id.inputInitialIndex)
        val inputJson = view.findViewById<TextInputEditText>(R.id.inputJson)

        val savedData = requireActivity().getSharedPreferences("SavedData", 0)
        val sdModifierHour = savedData.getInt("ModifierHour", 1)
        val sdModifierMinute = savedData.getInt("ModifierMinute", 1)
        val sdModifierSecond = savedData.getInt("ModifierSecond", 1)
        val sdInitialIndex = savedData.getInt("InitialIndex", 1)
        val sdJson = savedData.getString("Json", "")

        inputModifierHour.setText(sdModifierHour.toString())
        inputModifierMinute.setText(sdModifierMinute.toString())
        inputModifierSecond.setText(sdModifierSecond.toString())
        inputInitialIndex.setText(sdInitialIndex.toString())
        inputJson.setText(sdJson)

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