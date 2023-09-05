package com.panov.timetable

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText

class Settings : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val savedData = applicationContext.getSharedPreferences("SavedData", 0)
        val applyButton = findViewById<Button>(R.id.applyButton)
        val returnButton  = findViewById<ImageButton>(R.id.returnButton)

        val textJson           = findViewById<TextInputEditText>(R.id.textJson)
        val textModifierHour   = findViewById<TextInputEditText>(R.id.textModifierHour)
        val textModifierMinute = findViewById<TextInputEditText>(R.id.textModifierMinute)
        val textModifierSecond = findViewById<TextInputEditText>(R.id.textModifierSecond)
        val textInitialIndex   = findViewById<TextInputEditText>(R.id.textInitialIndex)

        val sdJson             = savedData.getString("Json", "")
        val sdModifierHour     = savedData.getInt("ModifierHour", 1)
        val sdModifierMinute   = savedData.getInt("ModifierMinute", 1)
        val sdModifierSecond   = savedData.getInt("ModifierSecond", 1)
        val sdInitialIndex     = savedData.getInt("InitialIndex", 1)

        textJson.setText(sdJson)
        textModifierHour.setText(sdModifierHour.toString())
        textModifierMinute.setText(sdModifierMinute.toString())
        textModifierSecond.setText(sdModifierSecond.toString())
        textInitialIndex.setText(sdInitialIndex.toString())

        applyButton.setOnClickListener {
            val editor = savedData.edit()
            editor.putString("Json",        textJson.text.toString())
            editor.putInt("ModifierHour",   textModifierHour.text.toString().toInt())
            editor.putInt("ModifierMinute", textModifierMinute.text.toString().toInt())
            editor.putInt("ModifierSecond", textModifierSecond.text.toString().toInt())
            editor.putInt("InitialIndex",   textInitialIndex.text.toString().toInt())
            editor.apply()
            Toast.makeText(this, R.string.app_applied, Toast.LENGTH_SHORT).show()
        }

        returnButton.setOnClickListener {
            val editor = savedData.edit()
            editor.putInt("LastPage", 0)
            editor.apply()
            this.finish()
        }
    }
}