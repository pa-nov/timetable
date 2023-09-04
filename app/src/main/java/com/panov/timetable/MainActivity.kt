package com.panov.timetable

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val activities = arrayOf(
                Intent(this, MainActivity::class.java),
                Intent(this, Clock::class.java),
                Intent(this, Timetable::class.java),
                Intent(this, Settings::class.java)
            )

        val openClockButton = findViewById<ImageButton>(R.id.openClockButton)
        val openTimetableButton = findViewById<ImageButton>(R.id.openTimetableButton)
        val openSettingsButton = findViewById<ImageButton>(R.id.openSettingsButton)

        val savedData = applicationContext.getSharedPreferences("SavedData", 0)
        val lastPage = savedData.getInt("LastPage", 0)
        if (lastPage > 0) { startActivity(activities[lastPage]) }

        openClockButton.setOnClickListener {
            val editor = savedData.edit()
            editor.putInt("LastPage", 1)
            editor.apply()
            startActivity(activities[1])
        }
        openTimetableButton.setOnClickListener {
            val editor = savedData.edit()
            editor.putInt("LastPage", 2)
            editor.apply()
            startActivity(activities[2])
        }
        openSettingsButton.setOnClickListener {
            val editor = savedData.edit()
            editor.putInt("LastPage", 3)
            editor.apply()
            startActivity(activities[3])
        }
    }
}