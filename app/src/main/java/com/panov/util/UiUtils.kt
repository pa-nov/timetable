package com.panov.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.view.View.MeasureSpec
import android.view.ViewGroup.MarginLayoutParams
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.Toast
import kotlin.math.abs

object UiUtils {
    fun openURL(context: Context, url: String) {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    fun showToast(context: Context, resId: Int) {
        Toast.makeText(context, resId, Toast.LENGTH_SHORT).show()
    }

    fun showToast(context: Context, text: CharSequence) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    }

    fun clearFocus(context: Context, view: View) {
        val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
        view.clearFocus()
    }

    fun setupButtonGroup(buttons: Array<Button>) {
        for (button in 0 until buttons.count()) {
            buttons[button].setOnClickListener {
                for (index in 0 until buttons.count()) {
                    buttons[index].isEnabled = index != button
                }
            }
        }
    }

    fun setViewVisibility(view: View, visibility: Int, duration: Long = 250) {
        val start = if (visibility == View.VISIBLE) 0 else 1
        val offset = Converter.getPxFromDp(view.context, 32)
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
}