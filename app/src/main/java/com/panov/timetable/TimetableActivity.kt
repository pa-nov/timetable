package com.panov.timetable

import android.annotation.SuppressLint
import android.icu.util.Calendar
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.PopupWindow
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
        val tempDate = date.clone() as Calendar
        tempDate.add(Calendar.DAY_OF_YEAR, position - 1)

        val weekdays          = this.resources.getStringArray(R.array.weekdays)
        val dateDayOfWeek     = if (tempDate.get(Calendar.DAY_OF_WEEK) > 1) { tempDate.get(Calendar.DAY_OF_WEEK) - 2 } else { 6 }
        val dateWeekOddOrEven = if (tempDate.get(Calendar.WEEK_OF_YEAR) % 2 == 0) { "even" } else { "odd" }
        val currentTimetable  = jsonData.getJSONArray(dateWeekOddOrEven).getJSONArray(dateDayOfWeek)
        val anotherTimetable  = jsonData.getJSONArray(if (dateWeekOddOrEven == "odd") { "even" } else { "odd" }).getJSONArray(dateDayOfWeek)

        linearLayout.findViewById<TextView>(R.id.textDayOfWeek).text = weekdays[dateDayOfWeek]

        for (i: Int in 0 until jsonData.getJSONArray("times").length()) {
            fillLessonView(linearLayout, false, i, currentTimetable.getInt(i), anotherTimetable.getInt(i))
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

    @SuppressLint("SetTextI18n", "InflateParams")
    private fun fillLessonView(background: LinearLayout, isInfo: Boolean, lessonNumber: Int, currentLessonId: Int, anotherLessonId: Int) {
        val timetableLesson = layoutInflater.inflate(R.layout.timetable_lesson, null)
        val times           = jsonData.getJSONArray("times")
        val lessons         = jsonData.getJSONArray("lessons")
        val currentLesson   = lessons.getJSONArray(currentLessonId)

        val currentTime = times.getJSONObject(lessonNumber)
        val startHour   = Tools.getTwoDigitNumber(currentTime.getInt("startHour"))
        val startMinute = Tools.getTwoDigitNumber(currentTime.getInt("startMinute"))
        val endHour     = Tools.getTwoDigitNumber(currentTime.getInt("endHour"))
        val endMinute   = Tools.getTwoDigitNumber(currentTime.getInt("endMinute"))

        val textName    = timetableLesson.findViewById<TextView>(R.id.textName)
        val textTeacher = timetableLesson.findViewById<TextView>(R.id.textTeacher)
        val textRoom    = timetableLesson.findViewById<TextView>(R.id.textRoom)

        var isWeekChangeable = currentLessonId != anotherLessonId
        if (isWeekChangeable and currentLesson.getString(3).isNotEmpty()) {
            val otherLessons = currentLesson.getString(3).split("|")
            for (i: Int in otherLessons.indices) {
                if (anotherLessonId == otherLessons[i].toInt()) {
                    isWeekChangeable = false
                }
            }
        }
        if (isWeekChangeable) { timetableLesson.findViewById<FrameLayout>(R.id.frameViewRight).setBackgroundColor(ContextCompat.getColor(this, R.color.red)) }

        timetableLesson.findViewById<TextView>(R.id.textIndex).text = "${lessonNumber + initialIndex}"
        timetableLesson.findViewById<TextView>(R.id.textTime).text  = "${startHour}:${startMinute} ${endHour}:${endMinute}"

        if (currentLessonId > 0) {
            val currentTeacher     = currentLesson.getString(2).split("|")
            textName.text          = currentLesson.getString(0)
            textTeacher.text       = "${currentTeacher[0]} ${currentTeacher[1].substring(0, 1)}. ${currentTeacher[2].substring(0, 1)}."
            textRoom.text          = "(${currentLesson.getString(1)})"
        } else {
            textName.visibility    = View.INVISIBLE
            textTeacher.visibility = View.INVISIBLE
            textRoom.visibility    = View.INVISIBLE
        }

        if (currentLessonId > 0 && !isInfo) {
            timetableLesson.setOnClickListener {
                showLessonInfo(timetableLesson, currentLessonId)
            }
        }
        if (isInfo) {
            textTeacher.visibility = View.INVISIBLE
        }

        background.addView(timetableLesson)
    }
    @SuppressLint("SetTextI18n", "InflateParams")
    private fun showLessonInfo(background: View, lessonId: Int) {
        val popupView        = layoutInflater.inflate(R.layout.timetable_lesson_info, null)
        val linearLayoutEven = popupView.findViewById<LinearLayout>(R.id.infoWeekEven)
        val linearLayoutOdd  = popupView.findViewById<LinearLayout>(R.id.infoWeekOdd)

        val times            = jsonData.getJSONArray("times")
        val lessons          = jsonData.getJSONArray("lessons")
        val timetableEven    = jsonData.getJSONArray("even")
        val timetableOdd     = jsonData.getJSONArray("odd")
        val currentLesson    = lessons.getJSONArray(lessonId)
        val weekdays         = this.resources.getStringArray(R.array.weekdays)

        val lessonIds = ArrayList<Int>()
        lessonIds.add(lessonId)
        if (currentLesson.getString(3).isNotEmpty()) {
            val otherLessons = currentLesson.getString(3).split("|")
            for (i: Int in otherLessons.indices) {
                lessonIds.add(otherLessons[i].toInt())
            }
        }


        val teachers = ArrayList<String>()
        val rooms    = ArrayList<String>()
        teachers.add(currentLesson.getString(2).split("|").joinToString(" "))
        rooms.add("(${currentLesson.getString(1)})")

        if (lessonIds.count() > 1) {
            for (i: Int in lessonIds.indices) {
                val otherLesson    = lessons.getJSONArray(lessonIds[i])
                val otherTeacher   = otherLesson.getString(2).split("|").joinToString(" ")
                val otherRoom      = "(${otherLesson.getString(1)})"

                var isOtherTeacher = true
                for (t: Int in teachers.indices) {
                    if (otherTeacher == teachers[t]) {
                        isOtherTeacher = false
                    }
                }
                if (isOtherTeacher) { teachers.add(otherTeacher) }

                var isOtherRoom    = true
                for (r: Int in rooms.indices) {
                    if (otherRoom == rooms[r]) {
                        isOtherRoom = false
                    }
                }
                if (isOtherRoom) { rooms.add(otherRoom) }
            }

            if (teachers.count() > 1) { popupView.findViewById<TextView>(R.id.infoTeacherTitle).text = this.getString(R.string.info_teachers) }
            if (rooms.count() > 1)    { popupView.findViewById<TextView>(R.id.infoRoomTitle).text    = this.getString(R.string.info_rooms) }
        }

        popupView.findViewById<TextView>(R.id.infoTitle).text   = currentLesson.getString(0)
        popupView.findViewById<TextView>(R.id.infoTeacher).text = teachers.joinToString(", \n")
        popupView.findViewById<TextView>(R.id.infoRoom).text    = rooms.joinToString(", \n")

        var dayEven = -1
        for (d: Int in 0 until 7) {
            for (l: Int in 0 until times.length()) {
                for (i: Int in lessonIds.indices) {
                    if (timetableEven.getJSONArray(d).getInt(l) == lessonIds[i]) {
                        if (dayEven < d) { dayEven = d
                            val dayName = layoutInflater.inflate(R.layout.timetable_page, null)
                            dayName.findViewById<TextView>(R.id.textDayOfWeek).text = weekdays[d]
                            linearLayoutEven.addView(dayName)
                        }
                        fillLessonView(linearLayoutEven, true, l, lessonIds[i], timetableOdd.getJSONArray(d).getInt(l))
                    }
                }
            }
        }

        var dayOdd = -1
        for (d: Int in 0 until 7) {
            for (l: Int in 0 until times.length()) {
                for (i: Int in lessonIds.indices) {
                    if (timetableOdd.getJSONArray(d).getInt(l) == lessonIds[i]) {
                        if (dayOdd < d) { dayOdd = d
                            val dayName = layoutInflater.inflate(R.layout.timetable_page, null)
                            dayName.findViewById<TextView>(R.id.textDayOfWeek).text = weekdays[d]
                            linearLayoutOdd.addView(dayName)
                        }
                        fillLessonView(linearLayoutOdd, true, l, lessonIds[i], timetableEven.getJSONArray(d).getInt(l))
                    }
                }
            }
        }


        val frameLayout = findViewById<FrameLayout>(R.id.frameView)
        val popupWindow = PopupWindow(popupView, frameLayout.width, frameLayout.height / 2, true)
        popupWindow.showAsDropDown(background)
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