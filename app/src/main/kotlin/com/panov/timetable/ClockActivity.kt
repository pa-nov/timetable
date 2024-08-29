package com.panov.timetable

import android.content.Context
import android.content.res.Configuration
import android.icu.util.Calendar
import android.os.Bundle
import android.os.Handler
import android.text.format.DateUtils
import android.view.View
import android.view.WindowInsetsController
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsCompat
import com.panov.util.Converter
import com.panov.util.UiUtils
import java.util.Locale
import kotlin.math.abs

class ClockActivity : AppCompatActivity() {
    private val timetable = Storage.timetable
    private val displayTimer = Storage.settings.getBoolean(Storage.Clock.DISPLAY_TIMER, true)
    private val displayHeaders = Storage.settings.getBoolean(Storage.Clock.DISPLAY_HEADERS)
    private val displayDateTime = Storage.settings.getBoolean(Storage.Clock.DISPLAY_DATE_TIME)
    private val displayCurrentLesson = Storage.settings.getBoolean(Storage.Clock.DISPLAY_CURRENT_LESSON)
    private val displayNextLesson = Storage.settings.getBoolean(Storage.Clock.DISPLAY_NEXT_LESSON)
    private val notDisplayNextTime = Storage.settings.getBoolean(Storage.Clock.NOT_DISPLAY_NEXT_TIME)

    override fun attachBaseContext(context: Context) {
        val language = Storage.settings.getString(Storage.Application.LANGUAGE, Locale.getDefault().language)
        val locale = Locale(language)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)

        super.attachBaseContext(context.createConfigurationContext(config))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clock)
        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) return finish()
        window.insetsController?.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        window.insetsController?.hide(WindowInsetsCompat.Type.systemBars())
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                UiUtils.showToast(applicationContext, R.string.description_clock_close)
            }
        })

        val titleCurrentLesson = findViewById<TextView>(R.id.title_current_lesson)
        val titleTime = findViewById<TextView>(R.id.title_time)
        val titleNextLesson = findViewById<TextView>(R.id.title_next_lesson)

        val textCurrentLesson = findViewById<TextView>(R.id.text_current_lesson)
        val textTime = findViewById<TextView>(R.id.text_time)
        val textAgo = findViewById<TextView>(R.id.text_ago)
        val textNextLesson = findViewById<TextView>(R.id.text_next_lesson)
        val textDateTime = findViewById<TextView>(R.id.text_date_time)

        if (timetable == null) {
            titleCurrentLesson.visibility = View.GONE
            titleTime.visibility = View.GONE
            titleNextLesson.visibility = View.GONE

            textCurrentLesson.visibility = View.GONE
            textAgo.visibility = View.GONE
            textNextLesson.visibility = View.GONE
            textDateTime.visibility = View.GONE

            textTime.textSize = 160f
            return
        }

        if (!displayTimer || !notDisplayNextTime) {
            textAgo.visibility = View.GONE
        }

        if (!displayHeaders) {
            titleCurrentLesson.visibility = View.GONE
            titleTime.visibility = View.GONE
            titleNextLesson.visibility = View.GONE
        }

        if (!displayDateTime) {
            textDateTime.visibility = View.GONE
        }

        if (!displayCurrentLesson) {
            titleCurrentLesson.visibility = View.GONE
            textCurrentLesson.visibility = View.GONE
        }

        if (!displayNextLesson) {
            titleNextLesson.visibility = View.GONE
            textNextLesson.visibility = View.GONE
        }

        if (!displayDateTime && !displayCurrentLesson && !displayNextLesson) {
            titleTime.textSize = 32f
            textTime.textSize = 160f
            textAgo.textSize = 32f
        }

        val handler = Handler(mainLooper)
        handler.post(object : Runnable {
            override fun run() {
                if (isDestroyed) return
                val calendar = Calendar.getInstance()
                handler.postDelayed(this, (1000 - calendar.get(Calendar.MILLISECOND)).toLong())
                val seconds = calendar.get(Calendar.MILLISECONDS_IN_DAY) / 1000
                val offset = timetable.getOffset(calendar)

                if (displayTimer) {
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
                            Converter.getTimeText(abs(offset.currentDaysOffset * daySeconds + currentLessonEnd - seconds))
                        } else {
                            titleTime.text = getString(R.string.timer_starts_in)
                            textAgo.visibility = View.GONE
                            Converter.getTimeText(offset.nextDaysOffset * daySeconds + nextLessonStart - seconds)
                        }
                    }
                } else {
                    titleTime.text = getString(R.string.timer_time)
                    textTime.text = Converter.getTimeText(calendar)
                }

                if (displayDateTime) {
                    textDateTime.text = if (displayTimer) {
                        Converter.getDateText(calendar) + "   " + Converter.getTimeText(calendar)
                    } else {
                        Converter.getDateText(calendar)
                    }
                }

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
            }
        })
    }
}