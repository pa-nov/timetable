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
        val view = inflater.inflate(R.layout.fragment_clock, container, false)
        val savedData = requireActivity().getSharedPreferences("SavedClock", 0)

        val inputDate = view.findViewById<CheckBox>(R.id.inputDate)
        val inputTitle = view.findViewById<CheckBox>(R.id.inputTitle)
        val inputOther = view.findViewById<CheckBox>(R.id.inputOther)

        inputDate.isChecked = savedData.getBoolean("ShowDate", false)
        inputTitle.isChecked = savedData.getBoolean("ShowTitle", false)
        inputOther.isChecked = savedData.getBoolean("ShowOther", false)

        view.findViewById<Button>(R.id.buttonAction).setOnClickListener {
            val editor = savedData.edit()
            editor.putBoolean("ShowDate", inputDate.isChecked)
            editor.putBoolean("ShowTitle", inputTitle.isChecked)
            editor.putBoolean("ShowOther", inputOther.isChecked)
            editor.apply()

            Toast.makeText(requireContext(), R.string.applied, Toast.LENGTH_SHORT).show()
        }

        return view
    }
}