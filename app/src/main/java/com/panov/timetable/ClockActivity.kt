package com.panov.timetable

import android.content.res.Configuration
import android.icu.util.Calendar
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.TextView
import org.json.JSONObject

class ClockActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) { this.finish() }
        setContentView(R.layout.activity_clock)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

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
            findViewById<TextView>(R.id.appClock_DateTime).visibility = View.GONE
        }
        if (!sdTitle) {
            findViewById<TextView>(R.id.appClock_TimeTitle).visibility = View.GONE
            findViewById<FrameLayout>(R.id.layer_title).visibility = View.GONE
        }
        if (!sdOther) {
            findViewById<FrameLayout>(R.id.layer_other).visibility = View.GONE
            findViewById<FrameLayout>(R.id.layer_title).visibility = View.GONE
        }

        val handler = Handler(Looper.getMainLooper())
        handler.post(object : Runnable { override fun run() {
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

            findViewById<TextView>(R.id.appClock_TimeText).text = Tools.getTime(nowTime.toDouble())
            findViewById<TextView>(R.id.appClock_TimeSubText).visibility = if (nowTime < 0) { View.VISIBLE } else { View.INVISIBLE }

            if (sdDate) {
                val dateDay   = Tools.getTwoDigitNumber(date.get(Calendar.DAY_OF_MONTH))
                val dateMonth = Tools.getTwoDigitNumber(date.get(Calendar.MONTH) + 1)
                val dateYear  = date.get(Calendar.YEAR).toString()
                findViewById<TextView>(R.id.appClock_DateTime).text = resources.getString(R.string.placeholder_date_time, dateDay, dateMonth, dateYear, dateHour, dateMinute, dateSecond)
            }
            if (sdTitle) {
                findViewById<TextView>(R.id.appClock_TimeTitle).text = if (nowTime < 0) { resources.getString(R.string.widget_earlier_time) } else { resources.getString(R.string.widget_now_time) }
                findViewById<TextView>(R.id.appClock_NowTitle).text = if (nowTime < 0) { resources.getString(R.string.widget_earlier) } else { resources.getString(R.string.widget_now) }
            }
            if (sdOther) {
                findViewById<TextView>(R.id.appClock_NowText).text = lessons.getJSONArray(timetable.nowId).getString(0)
                findViewById<TextView>(R.id.appClock_NowSubText).text = lessons.getJSONArray(timetable.nowId).getString(2).split("|").joinToString(" ")
                findViewById<TextView>(R.id.appClock_ThenText).text = lessons.getJSONArray(timetable.thenId).getString(0)
            }

            handler.postDelayed(this, (1001 - date.get(Calendar.MILLISECOND)).toLong())
        } } )
    }
}