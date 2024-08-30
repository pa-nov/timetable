package com.panov.timetable.widget

import android.app.Activity
import android.content.Context
import android.graphics.Outline
import android.icu.util.Calendar
import android.view.LayoutInflater
import android.view.View
import android.view.View.MeasureSpec
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.panov.timetable.R
import com.panov.timetable.Storage
import com.panov.timetable.fragment.TimetableFragment
import com.panov.util.Converter
import com.panov.util.TimetableData

class Timetable {
    companion object {
        private fun fillLessonView(view: LinearLayout, timetable: TimetableData, week: String, day: Int, lessonIndex: Int, isNow: Boolean = false) {
            val lessonId = timetable.getLessonId(week, day, lessonIndex)
            val lessonNumber = Storage.settings.getInt(Storage.Application.INITIAL_INDEX, 1) + lessonIndex
            val otherLessonId = timetable.getLessonId(if (week == "odd") "even" else "odd", day, lessonIndex)
            val otherLessonDiffers = !(otherLessonId == lessonId || otherLessonId in timetable.getLessonOtherIds(lessonId))

            val textNumber = view.getChildAt(0) as TextView
            val lineLeft = view.getChildAt(1) as FrameLayout
            val textTime = view.getChildAt(2) as TextView
            val lineRight = view.getChildAt(3) as FrameLayout
            val textTitle = (view.getChildAt(4) as LinearLayout).getChildAt(0) as TextView
            val textTeacher = ((view.getChildAt(4) as LinearLayout).getChildAt(1) as LinearLayout).getChildAt(0) as TextView
            val textClassroom = ((view.getChildAt(4) as LinearLayout).getChildAt(1) as LinearLayout).getChildAt(1) as TextView

            textNumber.text = lessonNumber.toString()
            textTime.text = timetable.getLessonTimeText(lessonIndex)
            lineLeft.setBackgroundColor(view.context.getColor(if (isNow) R.color.green else R.color.line))
            lineRight.setBackgroundColor(view.context.getColor(if (otherLessonDiffers) R.color.red else R.color.line))

            if (lessonId > 0) {
                textTitle.text = timetable.getLessonShortTitle(lessonId)
                textTeacher.text = timetable.getTeacherShortName(lessonId)
                textClassroom.text = timetable.getClassroomText(lessonId)
            } else {
                textTitle.text = ""
                textTeacher.text = ""
                textClassroom.text = ""
            }
        }

        private fun showLessonInfoPopup(item: View, timetable: TimetableData, week: String, day: Int, lessonIndex: Int) {
            val view = LayoutInflater.from(item.context).inflate(R.layout.popup_lesson_info, null, false)
            view.clipToOutline = true
            view.outlineProvider = object : ViewOutlineProvider() {
                private val radius = Converter.getPxFromDp(item.context, 16).toFloat()
                override fun getOutline(view: View, outline: Outline) {
                    outline.setRoundRect(0, 0, view.width, view.height, radius)
                }
            }

            val lessonIds = arrayListOf(timetable.getLessonId(week, day, lessonIndex))
            lessonIds.addAll(timetable.getLessonOtherIds(lessonIds[0]))
            lessonIds.sort()


            val titleTitle = view.findViewById<TextView>(R.id.title_title)
            val titleTeacher = view.findViewById<TextView>(R.id.title_teacher)
            val titleClassroom = view.findViewById<TextView>(R.id.title_classroom)
            val textTitle = view.findViewById<TextView>(R.id.text_title)
            val textTeacher = view.findViewById<TextView>(R.id.text_teacher)
            val textClassroom = view.findViewById<TextView>(R.id.text_classroom)

            val titles = arrayListOf<String>()
            val teachers = arrayListOf<String>()
            val classrooms = arrayListOf<String>()
            for (lessonId in lessonIds) {
                val title = timetable.getLessonFullTitle(lessonId)
                val teacher = timetable.getTeacherFullName(lessonId)
                val classroom = timetable.getClassroom(lessonId)

                if (title !in titles) titles.add(title)
                if (teacher !in teachers) teachers.add(teacher)
                if (classroom !in classrooms) classrooms.add(classroom)
            }

            if (titles.count() > 1) titleTitle.text = item.resources.getString(R.string.info_titles)
            if (teachers.count() > 1) titleTeacher.text = item.resources.getString(R.string.info_teachers)
            if (classrooms.count() > 1) titleClassroom.text = item.resources.getString(R.string.info_classrooms)
            textTitle.text = titles.joinToString(",\n")
            textTeacher.text = teachers.joinToString(",\n")
            textClassroom.text = classrooms.joinToString(",\n")


            val layoutEven = view.findViewById<LinearLayout>(R.id.layout_even)
            val layoutOdd = view.findViewById<LinearLayout>(R.id.layout_odd)
            val titleEven = view.findViewById<TextView>(R.id.title_even)
            val titleOdd = view.findViewById<TextView>(R.id.title_odd)

            var isDifferent = false
            for (tempDay in 0 until 7) {
                var tempLayoutEven: LinearLayout? = null
                var tempLayoutOdd: LinearLayout? = null

                for (tempIndex in 0 until timetable.getLessonsCount()) {
                    val lessonEvenId = timetable.getLessonId("even", tempDay, tempIndex)
                    val lessonOddId = timetable.getLessonId("odd", tempDay, tempIndex)

                    if (lessonEvenId in lessonIds) {
                        if (tempLayoutEven == null) tempLayoutEven = createDayLayout(item.context, tempDay)
                        val tempItem = LayoutInflater.from(item.context).inflate(R.layout.item_lesson, null, false) as LinearLayout
                        val isNow = "even" == week && tempDay == day && tempIndex == lessonIndex
                        fillLessonView(tempItem, timetable, "even", tempDay, tempIndex, isNow)
                        tempLayoutEven.addView(tempItem)
                    }

                    if (lessonOddId in lessonIds) {
                        if (tempLayoutOdd == null) tempLayoutOdd = createDayLayout(item.context, tempDay)
                        val tempItem = LayoutInflater.from(item.context).inflate(R.layout.item_lesson, null, false) as LinearLayout
                        val isNow = "odd" == week && tempDay == day && tempIndex == lessonIndex
                        fillLessonView(tempItem, timetable, "odd", tempDay, tempIndex, isNow)
                        tempLayoutOdd.addView(tempItem)
                    }

                    if (lessonEvenId != lessonOddId && (lessonEvenId in lessonIds || lessonOddId in lessonIds)) isDifferent = true
                }

                if (tempLayoutEven != null) layoutEven.addView(tempLayoutEven)
                if (tempLayoutOdd != null) layoutOdd.addView(tempLayoutOdd)
            }

            if (!isDifferent) {
                (if (week == "even") layoutOdd else layoutEven).visibility = View.GONE
                (if (week == "even") titleEven else titleOdd).text = item.resources.getString(R.string.week_every)
            }

            if (layoutEven.childCount < 2) layoutEven.visibility = View.GONE
            if (layoutOdd.childCount < 2) layoutOdd.visibility = View.GONE


            val containerHeight = getVerticalLocation((item.context as Activity).findViewById(R.id.navigation_system))
            view.measure(MeasureSpec.makeMeasureSpec(item.width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(containerHeight / 2, MeasureSpec.AT_MOST))
            val direction = if (getVerticalLocation(item) + view.measuredHeight < containerHeight) -1 else 1
            PopupWindow(view, view.measuredWidth, view.measuredHeight, true).showAsDropDown(item, 0, item.height * direction)
        }

        private fun createDayLayout(context: Context, day: Int): LinearLayout {
            val layout = LayoutInflater.from(context).inflate(R.layout.page_timetable, null, false) as LinearLayout

            val title = layout.findViewById<TextView>(R.id.title_weekday)
            title.text = context.resources.getStringArray(R.array.weekdays)[day]
            title.textSize = 20f

            return layout
        }

        private fun getVerticalLocation(view: View): Int {
            val location = IntArray(2)
            view.getLocationInWindow(location)
            return location[1]
        }
    }


    class RecyclerViewAdapter(private val fragment: TimetableFragment) : RecyclerView.Adapter<RecyclerViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.page_timetable, parent, false)

            val padding = Converter.getPxFromDp(parent.context, 16)
            view.setPadding(padding, 0, padding, 0)

            return RecyclerViewHolder(view)
        }

        override fun getItemCount(): Int {
            return 3
        }

        override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
            val view = holder.itemView as LinearLayout
            val offset = position - 1
            val calendar = fragment.calendar.clone() as Calendar
            val timetable = Storage.timetable
            calendar.add(Calendar.DAY_OF_MONTH, offset)

            if (timetable != null) {
                val week = if (calendar.get(Calendar.WEEK_OF_YEAR) % 2 == 0) "even" else "odd"
                val day = if (calendar.get(Calendar.DAY_OF_WEEK) > 1) calendar.get(Calendar.DAY_OF_WEEK) - 2 else 6
                view.findViewById<TextView>(R.id.title_weekday).text = view.resources.getStringArray(R.array.weekdays)[day]

                while (view.childCount > timetable.getLessonsCount() + 1) {
                    view.removeViewAt(view.childCount - 1)
                }

                while (view.childCount < timetable.getLessonsCount() + 1) {
                    view.addView(LayoutInflater.from(view.context).inflate(R.layout.item_lesson, null, false))
                }

                for (index in 1 until view.childCount) {
                    val item = view.getChildAt(index) as LinearLayout
                    val lessonIndex = index - 1

                    fillLessonView(item, timetable, week, day, lessonIndex)

                    if (timetable.getLessonId(week, day, lessonIndex) > 0) {
                        item.setOnClickListener { showLessonInfoPopup(item, timetable, week, day, lessonIndex) }
                    } else {
                        item.setOnClickListener(null)
                    }
                }
            } else {
                view.findViewById<TextView>(R.id.title_weekday).text = view.resources.getString(R.string.message_error)

                while (view.childCount > 1) {
                    view.removeViewAt(view.childCount - 1)
                }
            }
        }
    }


    class RecyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}