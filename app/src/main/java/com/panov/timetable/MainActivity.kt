package com.panov.timetable

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<BottomNavigationView>(R.id.menu).selectedItemId = R.id.timetable
        openFragment(TimetableFragment())

        findViewById<BottomNavigationView>(R.id.menu).setOnItemSelectedListener {
            when(it.itemId) {
                R.id.clock     -> openFragment(ClockFragment())
                R.id.timetable -> openFragment(TimetableFragment())
                R.id.settings  -> openFragment(SettingsFragment())
                else -> { }
            }
            true
        }
    }

    private fun openFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.container, fragment).commit()
    }
}