package com.panov.timetable

import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.forEach
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    companion object {
        private var selectedItem = R.id.menu_timetable
        private var clockFragment = ClockFragment()
        private var timetableFragment = TimetableFragment()
        private var settingsFragment = SettingsFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val menuMain = findViewById<BottomNavigationView>(R.id.menu_main)
        menuMain.menu.forEach { menuMain.findViewById<View>(it.itemId).setOnLongClickListener { true } }
        menuMain.setOnItemSelectedListener { selectItem(it.itemId) }
        menuMain.findViewById<View>(selectedItem).performClick()

        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            startActivity(Intent(applicationContext, ClockActivity::class.java))
        }
    }

    private fun selectItem(item: Int): Boolean {
        if (item == selectedItem) when (item) {
            R.id.menu_clock -> clockFragment = ClockFragment()
            R.id.menu_settings -> settingsFragment = SettingsFragment()
            else -> timetableFragment = TimetableFragment()
        }
        selectedItem = item
        requestedOrientation = if (item == R.id.menu_clock) ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        supportFragmentManager.beginTransaction().replace(
            R.id.view_main, when (item) {
                R.id.menu_clock -> clockFragment
                R.id.menu_settings -> settingsFragment
                else -> timetableFragment
            }
        ).commit()
        return true
    }
}