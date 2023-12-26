package com.panov.timetable

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast

class ClockFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_clock, container, false)

        val inputDate  = view.findViewById<CheckBox>(R.id.inputDate)
        val inputTitle = view.findViewById<CheckBox>(R.id.inputTitle)
        val inputOther = view.findViewById<CheckBox>(R.id.inputOther)

        val savedData = requireActivity().getSharedPreferences("SavedData", 0)
        val sdDate    = savedData.getBoolean("ClockDate", true)
        val sdTitle   = savedData.getBoolean("ClockTitle", false)
        val sdOther   = savedData.getBoolean("ClockOther", false)

        inputDate.isChecked  = sdDate
        inputTitle.isChecked = sdTitle
        inputOther.isChecked = sdOther

        view.findViewById<Button>(R.id.buttonApply).setOnClickListener {
            val editor = savedData.edit()
            editor.putBoolean("ClockDate",  inputDate.isChecked)
            editor.putBoolean("ClockTitle", inputTitle.isChecked)
            editor.putBoolean("ClockOther", inputOther.isChecked)
            editor.apply()

            Toast.makeText(requireContext(), R.string.app_applied, Toast.LENGTH_SHORT).show()
            startActivity(Intent(requireContext(), ClockActivity::class.java))
        }

        return view
    }
}