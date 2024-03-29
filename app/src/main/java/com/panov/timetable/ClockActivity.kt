package com.panov.timetable

import android.content.res.Configuration
import android.icu.util.Calendar
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.Group
import org.json.JSONObject

class ClockActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            finish()
            return
        }
        setContentView(R.layout.activity_clock)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {}
        })

        val data: JSONObject
        try {
            data = JSONObject(this.getSharedPreferences("SavedTimetable", 0).getString("Json", "") ?: "")
        } catch (e: Exception) {
            findViewById<TextView>(R.id.text_time).text = resources.getString(R.string.error)
            findViewById<Group>(R.id.title).visibility = View.INVISIBLE
            findViewById<Group>(R.id.other).visibility = View.INVISIBLE
            findViewById<TextView>(R.id.text_time_ago).visibility = View.INVISIBLE
            findViewById<TextView>(R.id.text_date_time).visibility = View.INVISIBLE
            findViewById<TextView>(R.id.title_time).visibility = View.INVISIBLE
            return
        }
        val savedData = this.getSharedPreferences("SavedClock", 0)
        val times = data.getJSONArray("times")
        val lessons = data.getJSONArray("lessons")
        val showDate = savedData.getBoolean("ShowDate", false)
        val showTitle = savedData.getBoolean("ShowTitle", false)
        val showOther = savedData.getBoolean("ShowOther", false)

        if (!showDate) findViewById<TextView>(R.id.text_date_time).visibility = View.INVISIBLE
        if (!showTitle) {
            findViewById<TextView>(R.id.title_time).visibility = View.INVISIBLE
            findViewById<Group>(R.id.title).visibility = View.INVISIBLE
        }
        if (!showOther) {
            findViewById<Group>(R.id.other).visibility = View.INVISIBLE
            findViewById<Group>(R.id.title).visibility = View.INVISIBLE
        }

        val handler = Handler(Looper.getMainLooper())
        handler.post(object : Runnable {
            override fun run() {
                val date = Calendar.getInstance()
                date.firstDayOfWeek = java.util.Calendar.MONDAY
                date.minimalDaysInFirstWeek = 4

                val dateDayOfWeek = if (date.get(Calendar.DAY_OF_WEEK) > 1) date.get(Calendar.DAY_OF_WEEK) - 2 else 6
                val dateWeekOddOrEven = if (date.get(Calendar.WEEK_OF_YEAR) % 2 == 0) "even" else "odd"
                val dateHour = Tools.getTwoDigitNumber(date.get(Calendar.HOUR_OF_DAY))
                val dateMinute = Tools.getTwoDigitNumber(date.get(Calendar.MINUTE))
                val dateSecond = Tools.getTwoDigitNumber(date.get(Calendar.SECOND))

                val time = (dateHour.toInt() * 60 + dateMinute.toInt()) * 60 + dateSecond.toInt()
                val timetableData = Tools.getTimetableData(data, time, dateDayOfWeek, dateWeekOddOrEven)

                val lessonTimes = times.getJSONObject(timetableData.currentNumber)
                val lessonTime =
                    ((lessonTimes.getInt("endHour") * 60 + lessonTimes.getInt("endMinute")) * 60 + (86400 * timetableData.currentDays)) - time

                findViewById<TextView>(R.id.text_time).text = Tools.getTime(lessonTime.toDouble())
                findViewById<TextView>(R.id.text_time_ago).visibility = if (lessonTime < 0) View.VISIBLE else View.INVISIBLE

                if (showDate) {
                    val dateDay = Tools.getTwoDigitNumber(date.get(Calendar.DAY_OF_MONTH))
                    val dateMonth = Tools.getTwoDigitNumber(date.get(Calendar.MONTH) + 1)
                    val dateYear = date.get(Calendar.YEAR).toString()
                    findViewById<TextView>(R.id.text_date_time).text = resources.getString(
                        R.string.placeholder_date_time, dateDay, dateMonth, dateYear, dateHour, dateMinute, dateSecond
                    )
                }
                if (showTitle) {
                    findViewById<TextView>(R.id.title_time).text = if (lessonTime < 0) resources.getString(R.string.earlier_time)
                    else resources.getString(R.string.now_time)
                    findViewById<TextView>(R.id.title_current).text = if (lessonTime < 0) resources.getString(R.string.earlier)
                    else resources.getString(R.string.now)
                }
                if (showOther) {
                    val currentData = lessons.getJSONArray(timetableData.currentId)
                    val nextData = lessons.getJSONArray(timetableData.nextId)
                    findViewById<TextView>(R.id.text_current).text = currentData.getString(0)
                    findViewById<TextView>(R.id.text_current_other).text = currentData.getString(2).split("|").joinToString(" ")
                    findViewById<TextView>(R.id.text_next).text = nextData.getString(0)
                }

                handler.postDelayed(this, (1001 - date.get(Calendar.MILLISECOND)).toLong())
            }
        })
    }
}