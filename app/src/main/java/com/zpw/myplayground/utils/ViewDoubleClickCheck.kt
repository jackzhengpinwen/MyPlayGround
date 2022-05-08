package com.zpw.myplayground.utils

import android.util.Log
import android.view.View

object ViewDoubleClickCheck {

    private const val MIN_DURATION = 700L

    private var lastClickTime = 0L

    private var clickIndex = 1

    @JvmStatic
    fun canClick(view: View): Boolean {
        log("canClick " + view + " " + clickIndex++)
        val currentTime = System.currentTimeMillis()
        if (lastClickTime == 0L || currentTime - lastClickTime > MIN_DURATION) {
            lastClickTime = currentTime
            log("canClick yes")
            return true
        }
        log("canClick no:  " + (currentTime - lastClickTime))
        return false
    }

    private fun log(log: String) {
        Log.e("DoubleClick", log)
    }

}