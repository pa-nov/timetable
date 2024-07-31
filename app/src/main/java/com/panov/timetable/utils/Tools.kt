package com.panov.timetable.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
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
        if (visibility == View.VISIBLE) view.visibility = View.VISIBLE
        val start = if (visibility == View.VISIBLE) 0 else 1
        val margin = convertDpToPx(view.context, 16)
        val animator = view.animate()

        animator.cancel()
        animator.setDuration(duration)
        animator.setUpdateListener { valueAnimator ->
            val value = abs(start - valueAnimator.animatedValue as Float)
            val layoutParams = view.layoutParams as MarginLayoutParams
            layoutParams.topMargin = -(margin * value).toInt()
            layoutParams.bottomMargin = -((view.height + margin) * (1 - value)).toInt()
            view.layoutParams = layoutParams
            view.alpha = value
        }
        animator.withEndAction { view.visibility = visibility }
        animator.start()
    }


    fun convertDpToPx(context: Context, dp: Int): Int {
        return (dp * (context.resources.displayMetrics.densityDpi / 160f)).toInt()
    }

    fun getTwoDigitNumber(number: Int): String {
        if (number > 9) return number.toString()
        return "0$number"
    }
}