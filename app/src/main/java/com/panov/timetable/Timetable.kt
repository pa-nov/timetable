package com.panov.timetable

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView

class Timetable : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timetable)
        val returnButton = findViewById<ImageButton>(R.id.returnButton)

        returnButton.setOnClickListener {
            val editor = applicationContext.getSharedPreferences("SavedData", 0).edit()
            editor.putInt("LastPage", 0)
            editor.apply()
            this.finish()
        }

        //findViewById<ListView>(R.id.listView).adapter = CustomAdapter()
    }

    //class CustomAdapter : BaseAdapter() {
    //    override fun getCount(): Int {
    //        return 5
    //    }
    //
    //    override fun getItem(i: Int): Any {
    //        return 0
    //    }
    //
    //    override fun getItemId(i: Int): Long {
    //        return 0
    //    }
    //
    //    override fun getView(i: Int, viewOld: View?, viewGroup: ViewGroup?): View {
    //        val view: View = View.inflate(viewGroup!!.context, R.layout.timetable_lesson, null)
    //
    //        view.findViewById<TextView>(R.id.textTime).text = "Ti"
    //        view.findViewById<TextView>(R.id.textLesson).text = "Le"
    //        view.findViewById<TextView>(R.id.textTeacher).text = "Te"
    //        view.findViewById<TextView>(R.id.textRoom).text = "Ro"
    //
    //        return view
    //    }
    //
    //}
}