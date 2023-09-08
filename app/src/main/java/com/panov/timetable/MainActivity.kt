package com.panov.timetable

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val lastPage = applicationContext.getSharedPreferences("SavedData", 0).getInt("LastPage", 0)
        if (lastPage > 0) { openActivity(lastPage) }

        findViewById<ImageButton>(R.id.buttonOpenClock).setOnClickListener { openActivity(1) }
        findViewById<ImageButton>(R.id.buttonOpenTimetable).setOnClickListener { openActivity(2) }
        findViewById<ImageButton>(R.id.buttonOpenSettings).setOnClickListener { openActivity(3) }
    }

    private fun openActivity(index: Int) {
        val activities = arrayOf(
            Intent(this, MainActivity::class.java),
            Intent(this, ClockActivity::class.java),
            Intent(this, TimetableActivity::class.java),
            Intent(this, SettingsActivity::class.java)
        )

        val editor = applicationContext.getSharedPreferences("SavedData", 0).edit()
        editor.putInt("LastPage", index)
        editor.apply()
        startActivity(activities[index])
    }
}