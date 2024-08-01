package com.panov.timetable.utils

import android.icu.util.Calendar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.panov.timetable.R
import com.panov.timetable.TimetableFragment

class TimetableAdapter(private val fragment: TimetableFragment) : RecyclerView.Adapter<TimetableViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimetableViewHolder {
        return TimetableViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.page_timetable, parent, false))
    }

    override fun getItemCount(): Int {
        return 3
    }

    override fun onBindViewHolder(holder: TimetableViewHolder, position: Int) {
        val view = holder.itemView as LinearLayout
        val offset = position - 1
        val calendar = fragment.calendar.clone() as Calendar
        val timetable = Storage.timetable
        calendar.add(Calendar.DAY_OF_MONTH, offset)

        if (timetable != null) {
            val week = if (calendar.get(Calendar.WEEK_OF_YEAR) % 2 == 0) "even" else "odd"
            val day = if (calendar.get(Calendar.DAY_OF_WEEK) > 1) calendar.get(Calendar.DAY_OF_WEEK) - 2 else 6
            view.findViewById<TextView>(R.id.text_title).text = view.context.resources.getStringArray(R.array.weekdays)[day]

            while (view.childCount > timetable.getLessonsCount() + 1) {
                view.removeViewAt(1)
            }

            while (view.childCount < timetable.getLessonsCount() + 1) {
                view.addView(LayoutInflater.from(view.context).inflate(R.layout.item_lesson, null, false))
            }

            for (index in 1 until view.childCount) {
                val item = view.getChildAt(index) as LinearLayout
                fillLessonView(item, timetable, index - 1, day, week)
            }
        } else {
            view.findViewById<TextView>(R.id.text_title).text = view.context.getString(R.string.message_error)

            while (view.childCount > 1) {
                view.removeViewAt(1)
            }
        }
    }


    private fun fillLessonView(view: LinearLayout, timetable: TimetableData, lessonIndex: Int, day: Int, week: String, isNow: Boolean = false) {
        val lessonId = timetable.getLessonId(week, day, lessonIndex)
        val lessonNumber = Storage.settings.getInt("app_initial_index", 1) + lessonIndex
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
        textTime.text = timetable.getLessonTime(lessonIndex)
        lineLeft.setBackgroundColor(view.context.getColor(if (isNow) R.color.green else R.color.line))
        lineRight.setBackgroundColor(view.context.getColor(if (otherLessonDiffers) R.color.red else R.color.line))

        if (lessonId > 0) {
            textTitle.text = timetable.getLessonShortTitle(lessonId)
            textTeacher.text = timetable.getTeacherShortName(lessonId)
            textClassroom.text = timetable.getLessonClassroomText(lessonId)
        } else {
            textTitle.text = ""
            textTeacher.text = ""
            textClassroom.text = ""
        }
    }
}


class TimetableViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)