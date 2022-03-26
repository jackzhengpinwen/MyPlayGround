package com.zpw.myplayground.robolectric

import android.content.Context
import com.zpw.myplayground.R

class MassageStick {
    companion object {
        const val STRONG = 0
        const val MEDIUM = 1
        const val WEAK = 2
    }

    fun setStrength(context: Context, strength: Int): String {
        return when(strength) {
            STRONG -> context.getString(R.string.strong)
            MEDIUM -> context.getString(R.string.medium)
            WEAK -> context.getString(R.string.weak)
            else -> context.getString(R.string.weak)
        }
    }
}