package com.panov.util

import android.content.Context
import android.content.Intent
import android.net.Uri

object WebUtils {
    fun openURL(context: Context, url: String) {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }
}