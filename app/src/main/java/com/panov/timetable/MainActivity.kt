package com.panov.timetable

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.forEach
import androidx.core.view.updateLayoutParams
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    companion object {
        private var selectedItem = R.id.menu_timetable
        private var clockFragment = ClockFragment()
        private var timetableFragment = TimetableFragment()
        private var settingsFragment = SettingsFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navigation = findViewById<BottomNavigationView>(R.id.navigation_main)
        navigation.setOnItemSelectedListener { item -> selectItem(item.itemId) }
        navigation.findViewById<View>(selectedItem).performClick()
        navigation.menu.forEach { item -> findViewById<View>(item.itemId).setOnLongClickListener { resetItem(item.itemId) } }

        val shadowStatusBar = findViewById<View>(R.id.shadow_status_bar)
        val navigationSystem = findViewById<View>(R.id.navigation_system)

        ViewCompat.setOnApplyWindowInsetsListener(window.decorView.rootView) { view, insets ->
            val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val keyboardInsets = insets.getInsets(WindowInsetsCompat.Type.ime())

            if (insets.isVisible(WindowInsetsCompat.Type.ime())) {
                navigation.visibility = View.GONE
                navigationSystem.visibility = View.GONE
                view.setPadding(0, 0, 0, keyboardInsets.bottom)
            } else {
                navigation.visibility = View.VISIBLE
                navigationSystem.visibility = View.VISIBLE
                view.setPadding(0, 0, 0, 0)
            }

            findViewById<View>(R.id.layout_container)?.setPadding(0, systemBarsInsets.top, 0, 0)
            shadowStatusBar.updateLayoutParams { height = systemBarsInsets.top * 2 }
            navigationSystem.updateLayoutParams { height = systemBarsInsets.bottom }

            WindowInsetsCompat.CONSUMED
        }
    }

    private fun selectItem(item: Int): Boolean {
        selectedItem = item
        requestedOrientation = if (item == R.id.menu_clock) ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        supportFragmentManager.beginTransaction().replace(
            R.id.view_main, when (item) {
                R.id.menu_clock -> clockFragment
                R.id.menu_settings -> settingsFragment
                else -> timetableFragment
            }
        ).commit()
        return true
    }

    private fun resetItem(item: Int): Boolean {
        if (item == selectedItem) {
            when (item) {
                R.id.menu_clock -> clockFragment = ClockFragment()
                R.id.menu_timetable -> timetableFragment = TimetableFragment()
                R.id.menu_settings -> settingsFragment = SettingsFragment()
            }
            findViewById<View>(item).performClick()
        }
        return true
    }
}