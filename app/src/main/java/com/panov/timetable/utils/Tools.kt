package com.panov.timetable.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

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


    fun convertDpToPx(context: Context, dp: Int): Int {
        return (dp * (context.resources.displayMetrics.densityDpi / 160f)).toInt()
    }

    fun getTwoDigitNumber(number: Int): String {
        if (number > 9) return number.toString()
        return "0$number"
    }
}