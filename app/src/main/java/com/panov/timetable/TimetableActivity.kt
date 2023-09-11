package com.panov.timetable

import android.annotation.SuppressLint
import android.icu.util.Calendar
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import org.json.JSONObject

class TimetableActivity : AppCompatActivity() {
    private var date = Calendar.getInstance()
    private var jsonData = JSONObject()
    private var initialIndex = 1
    private var tempPosition = 0


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timetable)
        date.firstDayOfWeek         = Calendar.MONDAY
        date.minimalDaysInFirstWeek = 4
        val timetableActivity: TimetableActivity = this

        val savedData      = getSharedPreferences("SavedData", 0)
        val jsonDataString = savedData.getString("Json", "")
        if (jsonDataString.isNullOrEmpty()) { this.finish() }
        jsonData           = JSONObject(jsonDataString!!)
        initialIndex       = savedData.getInt("InitialIndex", 1)

        findViewById<ImageButton>(R.id.buttonReturn).setOnClickListener { this.finish() }

        val viewPager = findViewById<ViewPager2>(R.id.viewPager)
        viewPager.adapter = TimetablePageAdapter(timetableActivity)
        viewPager.setCurrentItem(1, false)
        viewPager.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                tempPosition = position - 1
            }

            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
                if (state == ViewPager2.SCROLL_STATE_IDLE){
                    date.add(Calendar.DAY_OF_YEAR, tempPosition)
                    tempPosition = 0
                    viewPager.adapter = null
                    viewPager.adapter = TimetablePageAdapter(timetableActivity)
                    viewPager.setCurrentItem(1, false)
                }
            }
        })
        val editor = this.getSharedPreferences("SavedData", 0).edit()
        editor.putInt("LastPage", 2)
        editor.apply()
    }
    override fun onDestroy() {
        val editor = this.getSharedPreferences("SavedData", 0).edit()
        editor.putInt("LastPage", 0)
        editor.apply()
        super.onDestroy()
    }


    @SuppressLint("SetTextI18n", "InflateParams")
    private fun updateView(linearLayout: LinearLayout, position: Int) {
        if (linearLayout.childCount > 2) { linearLayout.removeViewsInLayout(2, linearLayout.childCount - 2) }
        val tempDate = date.clone() as Calendar
        tempDate.add(Calendar.DAY_OF_YEAR, position - 1)

        val weekdays          = this.resources.getStringArray(R.array.weekdays)
        val dateDayOfWeek     = if (tempDate.get(Calendar.DAY_OF_WEEK) > 1) { tempDate.get(Calendar.DAY_OF_WEEK) - 2 } else { 6 }
        val dateWeekOddOrEven = if (tempDate.get(Calendar.WEEK_OF_YEAR) % 2 == 0) { "even" } else { "odd" }

        val times             = jsonData.getJSONArray("times")
        val lessons           = jsonData.getJSONArray("lessons")
        val currentTimetable  = jsonData.getJSONArray(dateWeekOddOrEven).getJSONArray(dateDayOfWeek)
        val anotherTimetable  = jsonData.getJSONArray(if (dateWeekOddOrEven == "odd") { "even" } else { "odd" }).getJSONArray(dateDayOfWeek)

        linearLayout.findViewById<TextView>(R.id.textDayOfWeek).text = weekdays[dateDayOfWeek]

        for (i: Int in 0 until times.length()) {
            val timetableLesson = layoutInflater.inflate(R.layout.timetable_lesson, null)

            val currentTime = times.getJSONObject(i)
            val startHour   = Tools.getTwoDigitNumber(currentTime.getInt("startHour"))
            val startMinute = Tools.getTwoDigitNumber(currentTime.getInt("startMinute"))
            val endHour     = Tools.getTwoDigitNumber(currentTime.getInt("endHour"))
            val endMinute   = Tools.getTwoDigitNumber(currentTime.getInt("endMinute"))

            timetableLesson.findViewById<TextView>(R.id.textIndex).text = "${i + initialIndex}"
            timetableLesson.findViewById<TextView>(R.id.textTime).text  = "${startHour}:${startMinute} ${endHour}:${endMinute}"

            val textName    = timetableLesson.findViewById<TextView>(R.id.textName)
            val textTeacher = timetableLesson.findViewById<TextView>(R.id.textTeacher)
            val textRoom    = timetableLesson.findViewById<TextView>(R.id.textRoom)

            val currentLesson = lessons.getJSONArray(currentTimetable.getInt(i))
            val anotherLesson = lessons.getJSONArray(anotherTimetable.getInt(i))

            var isWeekChangeable = currentLesson != anotherLesson
            if (isWeekChangeable and currentLesson.getString(3).isNotEmpty()) {
                val otherLessons = currentLesson.getString(3).split("|")
                for (l: Int in otherLessons.indices) {
                    if (anotherTimetable.getInt(i) == otherLessons[l].toInt()) {
                        isWeekChangeable = false
                    }
                }
            }
            if (isWeekChangeable) { timetableLesson.setBackgroundColor(ContextCompat.getColor(this, R.color.black)) }

            if (currentTimetable.getInt(i) > 0) {
                val currentTeacher = currentLesson.getString(2).split("|")
                textName.text      = currentLesson.getString(0)
                textTeacher.text   = "${currentTeacher[0]} ${currentTeacher[1].substring(0, 1)}. ${currentTeacher[2].substring(0, 1)}."
                textRoom.text      = "(${currentLesson.getString(1)})"
            } else {
                textName.visibility    = View.INVISIBLE
                textTeacher.visibility = View.INVISIBLE
                textRoom.visibility    = View.INVISIBLE
            }

            linearLayout.addView(timetableLesson)
        }
    }
    @SuppressLint("SetTextI18n")
    private fun updateMainView() {
        if (DateUtils.isToday(date.timeInMillis + DateUtils.DAY_IN_MILLIS)) {
            findViewById<TextView>(R.id.textDate).text = this.getString(R.string.day_yesterday)
            return
        }
        if (DateUtils.isToday(date.timeInMillis)) {
            findViewById<TextView>(R.id.textDate).text = this.getString(R.string.day_today)
            return
        }
        if (DateUtils.isToday(date.timeInMillis - DateUtils.DAY_IN_MILLIS)) {
            findViewById<TextView>(R.id.textDate).text = this.getString(R.string.day_tomorrow)
            return
        }

        val dateDay   = Tools.getTwoDigitNumber(date.get(Calendar.DAY_OF_MONTH))
        val dateMonth = Tools.getTwoDigitNumber(date.get(Calendar.MONTH) + 1)
        val dateYear  = date.get(Calendar.YEAR)
        val dateWeek  = Tools.getTwoDigitNumber(date.get(Calendar.WEEK_OF_YEAR))

        findViewById<TextView>(R.id.textDate).text = "$dateDay.$dateMonth.$dateYear ($dateWeek)"
    }


    class TimetablePageAdapter( private val timetableActivity: TimetableActivity) : RecyclerView.Adapter<TimetablePageAdapter.TimetablePage>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimetablePage {
            return TimetablePage(LayoutInflater.from(parent.context).inflate(R.layout.timetable_page, parent, false))
        }

        override fun getItemCount(): Int {
            return 3
        }

        override fun onBindViewHolder(holder: TimetablePage, position: Int) {
            timetableActivity.updateView(holder.itemView.findViewById(R.id.linearLayout), position)
            if (position == 1) { timetableActivity.updateMainView() }
        }

        class TimetablePage(itemView: View) : RecyclerView.ViewHolder(itemView)
    }
}