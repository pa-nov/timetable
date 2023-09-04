package com.panov.timetable

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton

class Clock : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clock)
        val returnButton = findViewById<ImageButton>(R.id.returnButton)

        returnButton.setOnClickListener {
            val editor = applicationContext.getSharedPreferences("SavedData", 0).edit()
            editor.putInt("LastPage", 0)
            editor.apply()
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}