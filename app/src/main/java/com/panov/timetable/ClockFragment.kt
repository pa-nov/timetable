package com.panov.timetable

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast
import androidx.fragment.app.Fragment

class ClockFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val fragment = inflater.inflate(R.layout.fragment_clock, container, false)
        val savedData = requireActivity().getSharedPreferences("Clock", 0)

        val inputDate = fragment.findViewById<CheckBox>(R.id.input_date)
        val inputTitle = fragment.findViewById<CheckBox>(R.id.input_title)
        val inputOther = fragment.findViewById<CheckBox>(R.id.input_other)

        inputDate.isChecked = savedData.getBoolean("ShowDate", false)
        inputTitle.isChecked = savedData.getBoolean("ShowTitle", false)
        inputOther.isChecked = savedData.getBoolean("ShowOther", false)

        fragment.findViewById<Button>(R.id.button_action).setOnClickListener {
            val editor = savedData.edit()
            editor.putBoolean("ShowDate", inputDate.isChecked)
            editor.putBoolean("ShowTitle", inputTitle.isChecked)
            editor.putBoolean("ShowOther", inputOther.isChecked)
            editor.apply()

            Toast.makeText(requireContext(), R.string.applied, Toast.LENGTH_SHORT).show()
        }

        return fragment
    }
}