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
            Intent(this, ClockActivity::class.java),
            Intent(this, TimetableActivity::class.java),
            Intent(this, SettingsActivity::class.java)
        )

        val lastPage = this.getSharedPreferences("SavedData", 0).getInt("LastPage", 0)
        if (lastPage > 0) { startActivity(activities[lastPage]) }

        findViewById<ImageButton>(R.id.buttonOpenClock).setOnClickListener { startActivity(activities[1]) }
        findViewById<ImageButton>(R.id.buttonOpenTimetable).setOnClickListener { startActivity(activities[2]) }
        findViewById<ImageButton>(R.id.buttonOpenSettings).setOnClickListener { startActivity(activities[3]) }
    }
}