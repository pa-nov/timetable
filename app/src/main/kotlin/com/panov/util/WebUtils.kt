package com.panov.util

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri

object WebUtils {
    fun openURL(context: Context, url: String) {
        context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
    }
}