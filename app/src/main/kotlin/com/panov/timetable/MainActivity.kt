package com.panov.timetable

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
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
import com.panov.timetable.util.ApplicationUtils
import com.panov.timetable.util.Storage
import com.panov.timetable.util.WidgetUtils
import com.panov.util.Converter

class MainActivity : AppCompatActivity() {
    private val unlockReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null && intent.action == Intent.ACTION_USER_PRESENT) {
                if (findViewById<View>(R.id.menu_clock).isSelected) startClockActivity()
            }
        }
    }

    override fun attachBaseContext(context: Context) {
        super.attachBaseContext(ApplicationUtils.getLocalizedContext(context))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        onBackPressedDispatcher.addCallback { finish() }

        val defaultItem = if (Storage.timetable != null) R.id.menu_timetable else R.id.menu_settings
        val selectedItem = savedInstanceState?.getInt("selected_item", defaultItem) ?: defaultItem
        if (selectedItem == R.id.menu_clock) startClockActivity()

        val shadowStatusBar = findViewById<View>(R.id.shadow_status_bar)
        val navigationSeparator = findViewById<View>(R.id.navigation_separator)
        val navigationMain = findViewById<BottomNavigationView>(R.id.navigation_main)
        val navigationSystem = findViewById<View>(R.id.navigation_system)

        navigationMain.setOnItemSelectedListener { item -> selectItem(item.itemId) }
        navigationMain.findViewById<View>(selectedItem).performClick()
        navigationMain.menu.forEach { item -> navigationMain.findViewById<View>(item.itemId).setOnLongClickListener { resetItem(item.itemId) } }

        ViewCompat.setOnApplyWindowInsetsListener(window.decorView.rootView) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val keyboard = insets.getInsets(WindowInsetsCompat.Type.ime())

            if (insets.isVisible(WindowInsetsCompat.Type.ime())) {
                navigationSeparator.visibility = if (keyboard.bottom > 0) View.VISIBLE else View.GONE
                navigationMain.visibility = View.GONE
                navigationSystem.visibility = if (keyboard.bottom > 0) View.GONE else View.VISIBLE
            } else {
                navigationSeparator.visibility = View.VISIBLE
                navigationMain.visibility = View.VISIBLE
                navigationSystem.visibility = View.VISIBLE
            }

            view.setPadding(0, 0, 0, keyboard.bottom)
            findViewById<View>(R.id.layout_container)?.setPadding(0, systemBars.top, 0, Converter.getPxFromDp(baseContext, 48))
            shadowStatusBar.updateLayoutParams { height = systemBars.top * 2 }
            navigationSystem.updateLayoutParams { height = systemBars.bottom }

            WindowInsetsCompat.CONSUMED
        }

        registerReceiver(unlockReceiver, IntentFilter(Intent.ACTION_USER_PRESENT))

        WidgetUtils.startWidgetService(applicationContext)

        if (Build.VERSION.SDK_INT >= 33) {
            val isGranted = checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
            if (isGranted != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 0)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(
            "selected_item", if (findViewById<View>(R.id.menu_clock).isSelected) {
                R.id.menu_clock
            } else if (findViewById<View>(R.id.menu_timetable).isSelected) {
                R.id.menu_timetable
            } else {
                R.id.menu_settings
            }
        )
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        unregisterReceiver(unlockReceiver)
        super.onDestroy()
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

    private fun startClockActivity() {
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            startActivity(Intent(applicationContext, ClockActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP))
        }
    }
}