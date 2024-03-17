package com.panov.timetable

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class TimetablePageAdapter(private val timetable: TimetableFragment) : RecyclerView.Adapter<TimetablePage>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimetablePage {
        return TimetablePage(LayoutInflater.from(parent.context).inflate(R.layout.timetable_page, parent, false))
    }

    override fun getItemCount(): Int {
        return 3
    }

    override fun onBindViewHolder(holder: TimetablePage, position: Int) {
        try { timetable.updatePage(holder.itemView.findViewById(R.id.layout), position) } catch (_: Exception) { }
        if (position == 1) timetable.updateDate()
    }
}

class TimetablePage(itemView: View) : RecyclerView.ViewHolder(itemView)