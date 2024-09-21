package com.panov.timetable

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.forEach
import androidx.core.view.updateLayoutParams
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.panov.timetable.fragment.ClockFragment
import com.panov.timetable.fragment.SettingsFragment
import com.panov.timetable.fragment.TimetableFragment
import com.panov.util.Converter

class MainActivity : AppCompatActivity() {
    override fun attachBaseContext(context: Context) {
        super.attachBaseContext(AppUtils.getLocalizedContext(context, Storage.settings))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val defaultItem = if (Storage.timetable != null) R.id.menu_timetable else R.id.menu_settings
        val selectedItem = savedInstanceState?.getInt("selected_item", defaultItem) ?: defaultItem

        if (selectedItem == R.id.menu_clock && resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            startActivity(Intent(baseContext, ClockActivity::class.java))
        }

        val shadowStatusBar = findViewById<View>(R.id.shadow_status_bar)
        val navigationSeparator = findViewById<View>(R.id.navigation_separator)
        val navigationMain = findViewById<BottomNavigationView>(R.id.navigation_main)
        val navigationSystem = findViewById<View>(R.id.navigation_system)

        navigationMain.setOnItemSelectedListener { item -> selectItem(item.itemId) }
        navigationMain.findViewById<View>(selectedItem).performClick()
        navigationMain.menu.forEach { item -> navigationMain.findViewById<View>(item.itemId).setOnLongClickListener { resetItem(item.itemId) } }

        ViewCompat.setOnApplyWindowInsetsListener(window.decorView.rootView) { view, insets ->
            val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val keyboardInsets = insets.getInsets(WindowInsetsCompat.Type.ime())

            if (insets.isVisible(WindowInsetsCompat.Type.ime())) {
                navigationSeparator.visibility = if (keyboardInsets.bottom > 0) View.VISIBLE else View.GONE
                navigationMain.visibility = View.GONE
                navigationSystem.visibility = if (keyboardInsets.bottom > 0) View.GONE else View.VISIBLE
            } else {
                navigationSeparator.visibility = View.VISIBLE
                navigationMain.visibility = View.VISIBLE
                navigationSystem.visibility = View.VISIBLE
            }

            view.setPadding(0, 0, 0, keyboardInsets.bottom)
            findViewById<View>(R.id.layout_container)?.setPadding(0, systemBarsInsets.top, 0, Converter.getPxFromDp(baseContext, 48))
            shadowStatusBar.updateLayoutParams { height = systemBarsInsets.top * 2 }
            navigationSystem.updateLayoutParams { height = systemBarsInsets.bottom }

            WindowInsetsCompat.CONSUMED
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (findViewById<View>(R.id.menu_clock).isSelected) {
            outState.putInt("selected_item", R.id.menu_clock)
        } else if (findViewById<View>(R.id.menu_timetable).isSelected) {
            outState.putInt("selected_item", R.id.menu_timetable)
        } else if (findViewById<View>(R.id.menu_settings).isSelected) {
            outState.putInt("selected_item", R.id.menu_settings)
        }
    }

    private fun selectItem(item: Int): Boolean {
        requestedOrientation = if (item == R.id.menu_clock) ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        supportFragmentManager.beginTransaction().replace(
            R.id.view_main, supportFragmentManager.findFragmentByTag(item.toString()) ?: when (item) {
                R.id.menu_clock -> ClockFragment()
                R.id.menu_timetable -> TimetableFragment()
                else -> SettingsFragment()
            }, item.toString()
        ).commitNow()
        return true
    }

    private fun resetItem(item: Int): Boolean {
        if (findViewById<View>(item).isSelected) {
            val fragment = supportFragmentManager.findFragmentByTag(item.toString())
            if (fragment != null) {
                supportFragmentManager.beginTransaction().remove(fragment).commitNow()
                selectItem(item)
            }
        }
        return true
    }
}