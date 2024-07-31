package com.panov.timetable.utils

import android.icu.util.Calendar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        val offset = position - 1
        val calendar = fragment.calendar.clone() as Calendar
        calendar.add(Calendar.DAY_OF_MONTH, offset)

        holder.itemView.findViewById<TextView>(R.id.text_title).text = Tools.getDateText(calendar)
    }
}


class TimetableViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)