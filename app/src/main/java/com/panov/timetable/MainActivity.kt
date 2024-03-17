package com.panov.timetable

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.forEach
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    companion object {
        private var selectedItem = R.id.menu_timetable
        private val clockFragment = ClockFragment()
        private val timetableFragment = TimetableFragment()
        private val settingsFragment = SettingsFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mainMenu = findViewById<BottomNavigationView>(R.id.mainMenu)
        mainMenu.menu.forEach { mainMenu.findViewById<View>(it.itemId).setOnLongClickListener { true } }
        mainMenu.setOnItemSelectedListener { selectItem(it.itemId) }
        mainMenu.findViewById<View>(selectedItem).performClick()
    }

    private fun selectItem(item: Int): Boolean {
        selectedItem = item
        val fragment = when (item) {
            R.id.menu_clock -> clockFragment
            R.id.menu_settings -> settingsFragment
            else -> timetableFragment
        }
        supportFragmentManager.beginTransaction().replace(R.id.mainView, fragment).commit()
        return true
    }
}