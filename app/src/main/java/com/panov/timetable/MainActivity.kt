package com.panov.timetable

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private val clockFragment = ClockFragment()
    private val timetableFragment = TimetableFragment()
    private val settingsFragment = SettingsFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mainMenu = findViewById<BottomNavigationView>(R.id.mainMenu)
        mainMenu.menu.forEach {
            mainMenu.findViewById<View>(it.itemId).setOnLongClickListener { true }
        }
        mainMenu.selectedItemId = R.id.menu_timetable
        openFragment(timetableFragment)

        mainMenu.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.menu_clock -> openFragment(clockFragment)
                R.id.menu_timetable -> openFragment(timetableFragment)
                R.id.menu_settings -> openFragment(settingsFragment)
            }
            true
        }
    }

    private fun openFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.mainView, fragment).commit()
    }
}