package com.zpw.myplayground.robolectric

import android.util.Patterns

class EmailVerification {
    fun isEmailAddress(text: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(text).matches()
    }
}