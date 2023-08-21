package com.panov.timetable

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val applyButton = findViewById<Button>(R.id.applyButton)
        val jsonText = findViewById<TextInputEditText>(R.id.jsonText)

        applyButton.setOnClickListener( View.OnClickListener {
            val settings = applicationContext.getSharedPreferences("SavedTimetable", 0)
            val editor = settings.edit()
            editor.putString("Data", jsonText.text.toString())
            editor.apply()

            Toast.makeText(this, R.string.app_applied, Toast.LENGTH_SHORT).show()
        })
    }
}