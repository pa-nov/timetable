package com.panov.timetable

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.icu.util.Calendar
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import org.json.JSONObject

class ClockActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (this.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            setContentView(R.layout.activity_clock)

            val inputDate  = findViewById<CheckBox>(R.id.inputDate)
            val inputTitle = findViewById<CheckBox>(R.id.inputTitle)
            val inputOther = findViewById<CheckBox>(R.id.inputOther)

            val savedData  = this.getSharedPreferences("SavedData", 0)
            val sdDate     = savedData.getBoolean("ClockDate", true)
            val sdTitle    = savedData.getBoolean("ClockTitle", false)
            val sdOther    = savedData.getBoolean("ClockOther", false)

            inputDate.isChecked  = sdDate
            inputTitle.isChecked = sdTitle
            inputOther.isChecked = sdOther

            findViewById<Button>(R.id.buttonApply).setOnClickListener {
                val editor = savedData.edit()
                editor.putBoolean("ClockDate", inputDate.isChecked)
                editor.putBoolean("ClockTitle", inputTitle.isChecked)
                editor.putBoolean("ClockOther", inputOther.isChecked)
                editor.apply()
                Toast.makeText(this, R.string.app_applied, Toast.LENGTH_SHORT).show()
            }

            findViewById<ImageButton>(R.id.buttonReturn).setOnClickListener { this.finish() }
        } else {
            setContentView(R.layout.activity_clock_fullscreen)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN

            val savedData      = this.getSharedPreferences("SavedData", 0)
            val jsonDataString = savedData.getString("Json", "")
            if (jsonDataString.isNullOrEmpty()) { this.finish() }
            val jsonData       = JSONObject(jsonDataString!!)
            val times          = jsonData.getJSONArray("times")
            val lessons        = jsonData.getJSONArray("lessons")
            val sdDate         = savedData.getBoolean("ClockDate", true)
            val sdTitle        = savedData.getBoolean("ClockTitle", false)
            val sdOther        = savedData.getBoolean("ClockOther", false)

            if (!sdDate) {
                findViewById<TextView>(R.id.textDateTime).visibility = View.GONE
            }
            if (!sdTitle) {
                findViewById<TextView>(R.id.textTimeTitle).visibility = View.GONE
                findViewById<FrameLayout>(R.id.layer_title).visibility = View.GONE
            }
            if (!sdOther) {
                findViewById<FrameLayout>(R.id.layer_other).visibility = View.GONE
                findViewById<FrameLayout>(R.id.layer_title).visibility = View.GONE
            }

            val handler = Handler(Looper.getMainLooper())
            handler.post(object : Runnable { @SuppressLint("SetTextI18n") override fun run() {
                val date = Calendar.getInstance()
                date.firstDayOfWeek         = Calendar.MONDAY
                date.minimalDaysInFirstWeek = 4

                val dateDayOfWeek     = if (date.get(Calendar.DAY_OF_WEEK) > 1) { date.get(Calendar.DAY_OF_WEEK) - 2 } else { 6 }
                val dateWeekOddOrEven = if (date.get(Calendar.WEEK_OF_YEAR) % 2 == 0) { "even" } else { "odd" }
                val dateHour          = Tools.getTwoDigitNumber(date.get(Calendar.HOUR_OF_DAY))
                val dateMinute        = Tools.getTwoDigitNumber(date.get(Calendar.MINUTE))
                val dateSecond        = Tools.getTwoDigitNumber(date.get(Calendar.SECOND))

                val currentTime = (dateHour.toInt() * 60 + dateMinute.toInt()) * 60 + dateSecond.toInt()
                val timetable   = Tools.getTimetable(jsonData, currentTime, dateWeekOddOrEven, dateDayOfWeek)

                val nowTimes = times.getJSONObject(timetable.nowNumber)
                val nowTime  = ((nowTimes.getInt("endHour") * 60 + nowTimes.getInt("endMinute")) * 60) + (86400 * timetable.nowDays) - currentTime

                findViewById<TextView>(R.id.textTime).text = Tools.getTime(nowTime.toDouble())
                findViewById<TextView>(R.id.textTimeBack).visibility = if (nowTime < 0) { View.VISIBLE } else { View.INVISIBLE }

                if (sdDate) {
                    val dateDay   = Tools.getTwoDigitNumber(date.get(Calendar.DAY_OF_MONTH))
                    val dateMonth = Tools.getTwoDigitNumber(date.get(Calendar.MONTH) + 1)
                    val dateYear  = date.get(Calendar.YEAR)
                    findViewById<TextView>(R.id.textDateTime).text = "$dateDay.$dateMonth.$dateYear   $dateHour:$dateMinute:$dateSecond"
                }
                if (sdTitle) {
                    findViewById<TextView>(R.id.textTimeTitle).text = if (nowTime < 0) { resources.getString(R.string.widget_end) } else { resources.getString(R.string.widget_end_in) }
                }
                if (sdOther) {
                    findViewById<TextView>(R.id.textNowName).text = lessons.getJSONArray(timetable.nowId).getString(0)
                    findViewById<TextView>(R.id.textNowTeacher).text = lessons.getJSONArray(timetable.nowId).getString(2).split("|").joinToString(" ")
                    findViewById<TextView>(R.id.textThenName).text = lessons.getJSONArray(timetable.thenId).getString(0)
                }

                handler.postDelayed(this, 500)
            } } )
        }

        val editor = this.getSharedPreferences("SavedData", 0).edit()
        editor.putInt("LastPage", 1)
        editor.apply()
    }

    override fun onDestroy() {
        val editor = this.getSharedPreferences("SavedData", 0).edit()
        editor.putInt("LastPage", 0)
        editor.apply()
        super.onDestroy()
    }
}