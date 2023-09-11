package com.panov.timetable

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText

class SettingsActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val inputModifierHour   = findViewById<TextInputEditText>(R.id.inputModifierHour)
        val inputModifierMinute = findViewById<TextInputEditText>(R.id.inputModifierMinute)
        val inputModifierSecond = findViewById<TextInputEditText>(R.id.inputModifierSecond)
        val inputInitialIndex   = findViewById<TextInputEditText>(R.id.inputInitialIndex)
        val inputJson           = findViewById<TextInputEditText>(R.id.inputJson)

        val savedData = this.getSharedPreferences("SavedData", 0)
        val sdModifierHour      = savedData.getInt("ModifierHour", 1)
        val sdModifierMinute    = savedData.getInt("ModifierMinute", 1)
        val sdModifierSecond    = savedData.getInt("ModifierSecond", 1)
        val sdInitialIndex      = savedData.getInt("InitialIndex", 1)
        val sdJson              = savedData.getString("Json", "")

        inputModifierHour.setText(sdModifierHour.toString())
        inputModifierMinute.setText(sdModifierMinute.toString())
        inputModifierSecond.setText(sdModifierSecond.toString())
        inputInitialIndex.setText(sdInitialIndex.toString())
        inputJson.setText(sdJson)

        findViewById<Button>(R.id.buttonApply).setOnClickListener {
            val editor = savedData.edit()
            editor.putInt("ModifierHour",   inputModifierHour.text.toString().toInt())
            editor.putInt("ModifierMinute", inputModifierMinute.text.toString().toInt())
            editor.putInt("ModifierSecond", inputModifierSecond.text.toString().toInt())
            editor.putInt("InitialIndex",   inputInitialIndex.text.toString().toInt())
            editor.putString("Json",        inputJson.text.toString())
            editor.apply()
            Toast.makeText(this, R.string.app_applied, Toast.LENGTH_SHORT).show()
        }

        findViewById<ImageButton>(R.id.buttonReturn).setOnClickListener { this.finish() }

        val editor = this.getSharedPreferences("SavedData", 0).edit()
        editor.putInt("LastPage", 3)
        editor.apply()
    }

    override fun onDestroy() {
        val editor = this.getSharedPreferences("SavedData", 0).edit()
        editor.putInt("LastPage", 0)
        editor.apply()
        super.onDestroy()
    }
}