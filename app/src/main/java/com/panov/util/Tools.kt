package com.panov.util

import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.net.Uri
import android.view.View
import android.view.View.MeasureSpec
import android.view.ViewGroup.MarginLayoutParams
import android.widget.Toast
import kotlin.math.abs

object Tools {
    fun showToast(context: Context, resId: Int) {
        Toast.makeText(context, resId, Toast.LENGTH_SHORT).show()
    }

    fun showToast(context: Context, text: CharSequence) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    }

    fun openURL(context: Context, url: String) {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    fun setViewVisibility(view: View, visibility: Int, duration: Long = 250) {
        val start = if (visibility == View.VISIBLE) 0 else 1
        val offset = getPxFromDp(view.context, 32)
        val animator = view.animate()

        animator.cancel()
        animator.setDuration(duration)
        animator.setUpdateListener { valueAnimator ->
            val value = abs(start - valueAnimator.animatedValue as Float)
            val params = view.layoutParams as MarginLayoutParams
            params.bottomMargin = (-view.measuredHeight * (1 - value)).toInt()
            view.layoutParams = params
            view.translationY = offset * (1 - value)
            view.scaleX = 0.8f + value * 0.2f
            view.scaleY = 0.8f + value * 0.2f
            view.alpha = value
        }
        animator.withStartAction {
            if (visibility == View.VISIBLE) {
                val measureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
                view.measure(measureSpec, measureSpec)
                view.visibility = View.VISIBLE
            }
        }
        animator.withEndAction { view.visibility = visibility }
        animator.start()
    }


    fun getPxFromDp(context: Context, dp: Int): Int {
        return (dp * (context.resources.displayMetrics.densityDpi / 160f)).toInt()
    }

    fun getTwoDigitNumber(number: Int): String {
        if (number > 9) return number.toString()
        return "0$number"
    }

    fun getDateText(calendar: Calendar): String {
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH) + 1
        val year = calendar.get(Calendar.YEAR)
        val week = calendar.get(Calendar.WEEK_OF_YEAR)
        return "${getTwoDigitNumber(day)}.${getTwoDigitNumber(month)}.$year (${getTwoDigitNumber(week)})"
    }
}