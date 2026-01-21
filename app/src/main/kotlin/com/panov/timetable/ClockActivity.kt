package com.panov.timetable

import android.app.ActivityManager
import android.app.KeyguardManager
import android.content.Context
import android.content.res.Configuration
import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.format.DateUtils
import android.view.View
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.TextView
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import com.panov.timetable.util.ApplicationUtils
import com.panov.timetable.util.Storage
import com.panov.util.Converter
import com.panov.util.UiUtils

class ClockActivity : AppCompatActivity() {
    private val timetable = Storage.timetable
    private val displayTimer = Storage.settings.getBoolean(Storage.Clock.DISPLAY_TIMER, true)
    private val displayOnLockscreen = Storage.settings.getBoolean(Storage.Clock.DISPLAY_ON_LOCKSCREEN)
    private val displayHeaders = Storage.settings.getBoolean(Storage.Clock.DISPLAY_HEADERS)
    private val displayDateTime = Storage.settings.getBoolean(Storage.Clock.DISPLAY_DATE_TIME)
    private val displayCurrentLesson = Storage.settings.getBoolean(Storage.Clock.DISPLAY_CURRENT_LESSON)
    private val displayNextLesson = Storage.settings.getBoolean(Storage.Clock.DISPLAY_NEXT_LESSON)
    private val notDisplayNextTime = Storage.settings.getBoolean(Storage.Clock.NOT_DISPLAY_NEXT_TIME)

    override fun attachBaseContext(context: Context) {
        super.attachBaseContext(ApplicationUtils.getLocalizedContext(context))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_clock)
        setTaskDescription(ActivityManager.TaskDescription(getString(R.string.title_clock), null, getColor(R.color.accent)))

        onBackPressedDispatcher.addCallback {
            if ((getSystemService(KEYGUARD_SERVICE) as KeyguardManager).isKeyguardLocked) {
                finish()
            } else {
                UiUtils.showToast(baseContext, R.string.description_clock_close)
            }
        }

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        if (Build.VERSION.SDK_INT >= 30) {
            window.insetsController?.apply {
                systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                hide(WindowInsetsCompat.Type.systemBars())
            }
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }
        if (displayOnLockscreen) {
            if (Build.VERSION.SDK_INT >= 27) {
                setShowWhenLocked(true)
            } else {
                window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
            }
        }

        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            if ((getSystemService(KEYGUARD_SERVICE) as KeyguardManager).isKeyguardLocked) {
                if (displayOnLockscreen) {
                    Handler(mainLooper).postDelayed({
                        if (!isDestroyed && resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                            finish()
                        }
                    }, 250)
                    return hideUI()
                } else {
                    return finish()
                }
            } else {
                return finish()
            }
        }

        if (!displayHeaders) {
            findViewById<View>(R.id.title_current_lesson).visibility = View.GONE
            findViewById<View>(R.id.title_time).visibility = View.GONE
            findViewById<View>(R.id.title_next_lesson).visibility = View.GONE
        }

        if (!displayDateTime) {
            findViewById<View>(R.id.text_date_time).updateLayoutParams { height = 0 }
        }

        if (!displayCurrentLesson) {
            findViewById<View>(R.id.title_current_lesson).visibility = View.GONE
            findViewById<View>(R.id.text_current_lesson).visibility = View.GONE
        }

        if (!displayNextLesson) {
            findViewById<View>(R.id.title_next_lesson).visibility = View.GONE
            findViewById<View>(R.id.text_next_lesson).visibility = View.GONE
        }

        if (!notDisplayNextTime || !displayTimer) {
            findViewById<View>(R.id.text_ago).visibility = View.GONE
        }

        if (!displayDateTime && !displayCurrentLesson && !displayNextLesson && resources.configuration.screenWidthDp >= 720) {
            findViewById<TextView>(R.id.title_time).textSize = 32f
            findViewById<TextView>(R.id.text_time).textSize = 160f
            findViewById<TextView>(R.id.text_ago).textSize = 32f
        }

        if (displayTimer) {
            startTimer()
        } else {
            startClock()
        }
    }

    private fun hideUI() {
        findViewById<View>(R.id.title_current_lesson).visibility = View.GONE
        findViewById<View>(R.id.title_time).visibility = View.GONE
        findViewById<View>(R.id.title_next_lesson).visibility = View.GONE
        findViewById<View>(R.id.text_current_lesson).visibility = View.GONE
        findViewById<View>(R.id.text_time).visibility = View.GONE
        findViewById<View>(R.id.text_ago).visibility = View.GONE
        findViewById<View>(R.id.text_next_lesson).visibility = View.GONE
        findViewById<View>(R.id.text_date_time).visibility = View.GONE
    }

    private fun startClock() {
        val titleCurrentLesson = findViewById<TextView>(R.id.title_current_lesson)
        val titleTime = findViewById<TextView>(R.id.title_time)
        val textCurrentLesson = findViewById<TextView>(R.id.text_current_lesson)
        val textTime = findViewById<TextView>(R.id.text_time)
        val textAgo = findViewById<TextView>(R.id.text_ago)
        val textNextLesson = findViewById<TextView>(R.id.text_next_lesson)
        val textDateTime = findViewById<TextView>(R.id.text_date_time)

        titleTime.text = getString(R.string.timer_time)
        textAgo.visibility = View.GONE

        if (timetable != null) {
            val handler = Handler(mainLooper)
            handler.post(object : Runnable {
                override fun run() {
                    if (isDestroyed) return
                    val calendar = ApplicationUtils.getCalendar()
                    handler.postDelayed(this, (1000 - calendar.get(Calendar.MILLISECOND)).toLong())
                    val seconds = Converter.getSecondsInDay(calendar)
                    val offset = timetable.getOffset(calendar)

                    textTime.text = Converter.getTimeText(calendar)

                    if (displayCurrentLesson) {
                        val currentLessonId = timetable.getLessonId(offset.currentWeek, offset.currentDay, offset.currentLessonIndex)
                        val currentLessonEnd = timetable.getLessonTimeEnd(offset.currentLessonIndex)
                        textCurrentLesson.text = timetable.getLessonFullTitle(currentLessonId)
                        titleCurrentLesson.text = if (offset.currentDaysOffset == 0 && currentLessonEnd > seconds) {
                            getString(R.string.timer_now)
                        } else {
                            getString(R.string.timer_earlier)
                        }
                    }

                    if (displayNextLesson) {
                        val nextLessonId = timetable.getLessonId(offset.nextWeek, offset.nextDay, offset.nextLessonIndex)
                        textNextLesson.text = timetable.getLessonFullTitle(nextLessonId)
                    }

                    if (displayDateTime) {
                        textDateTime.text = Converter.getDateText(calendar)
                    }
                }
            })
        } else {
            textCurrentLesson.text = getString(R.string.message_error)
            textNextLesson.text = getString(R.string.message_error)

            val handler = Handler(mainLooper)
            handler.post(object : Runnable {
                override fun run() {
                    if (isDestroyed) return
                    val calendar = ApplicationUtils.getCalendar()
                    handler.postDelayed(this, (1000 - calendar.get(Calendar.MILLISECOND)).toLong())

                    textTime.text = Converter.getTimeText(calendar)

                    if (displayDateTime) {
                        textDateTime.text = Converter.getDateText(calendar)
                    }
                }
            })
        }
    }

    private fun startTimer() {
        val titleCurrentLesson = findViewById<TextView>(R.id.title_current_lesson)
        val titleTime = findViewById<TextView>(R.id.title_time)
        val textCurrentLesson = findViewById<TextView>(R.id.text_current_lesson)
        val textTime = findViewById<TextView>(R.id.text_time)
        val textAgo = findViewById<TextView>(R.id.text_ago)
        val textNextLesson = findViewById<TextView>(R.id.text_next_lesson)
        val textDateTime = findViewById<TextView>(R.id.text_date_time)

        if (timetable != null) {
            val handler = Handler(mainLooper)
            handler.post(object : Runnable {
                override fun run() {
                    if (isDestroyed) return
                    val calendar = ApplicationUtils.getCalendar()
                    handler.postDelayed(this, (1000 - calendar.get(Calendar.MILLISECOND)).toLong())
                    val seconds = Converter.getSecondsInDay(calendar)
                    val offset = timetable.getOffset(calendar)

                    val currentLessonEnd = timetable.getLessonTimeEnd(offset.currentLessonIndex)
                    val nextLessonStart = timetable.getLessonTimeStart(offset.nextLessonIndex)
                    val daySeconds = (DateUtils.DAY_IN_MILLIS / 1000).toInt()
                    textTime.text = if (offset.currentDaysOffset == 0 && currentLessonEnd > seconds) {
                        titleTime.text = getString(R.string.timer_ends_in)
                        textAgo.visibility = View.GONE
                        Converter.getTimeText(currentLessonEnd - seconds)
                    } else {
                        if (notDisplayNextTime) {
                            titleTime.text = getString(R.string.timer_ended)
                            textAgo.visibility = View.VISIBLE
                            Converter.getTimeText(-(offset.currentDaysOffset * daySeconds + currentLessonEnd - seconds))
                        } else {
                            titleTime.text = getString(R.string.timer_starts_in)
                            textAgo.visibility = View.GONE
                            Converter.getTimeText(offset.nextDaysOffset * daySeconds + nextLessonStart - seconds)
                        }
                    }

                    if (displayCurrentLesson) {
                        val currentLessonId = timetable.getLessonId(offset.currentWeek, offset.currentDay, offset.currentLessonIndex)
                        textCurrentLesson.text = timetable.getLessonFullTitle(currentLessonId)
                        titleCurrentLesson.text = if (offset.currentDaysOffset == 0 && currentLessonEnd > seconds) {
                            getString(R.string.timer_now)
                        } else {
                            getString(R.string.timer_earlier)
                        }
                    }

                    if (displayNextLesson) {
                        val nextLessonId = timetable.getLessonId(offset.nextWeek, offset.nextDay, offset.nextLessonIndex)
                        textNextLesson.text = timetable.getLessonFullTitle(nextLessonId)
                    }

                    if (displayDateTime) {
                        textDateTime.text = "${Converter.getDateText(calendar)}   ${Converter.getTimeText(calendar)}"
                    }
                }
            })
        } else {
            textCurrentLesson.text = getString(R.string.message_error)
            textTime.text = getString(R.string.message_error)
            textNextLesson.text = getString(R.string.message_error)
            textAgo.visibility = View.GONE

            if (displayDateTime) {
                val handler = Handler(mainLooper)
                handler.post(object : Runnable {
                    override fun run() {
                        if (isDestroyed) return
                        val calendar = ApplicationUtils.getCalendar()
                        handler.postDelayed(this, (1000 - calendar.get(Calendar.MILLISECOND)).toLong())
                        textDateTime.text = "${Converter.getDateText(calendar)}   ${Converter.getTimeText(calendar)}"
                    }
                })
            }
        }
    }
}