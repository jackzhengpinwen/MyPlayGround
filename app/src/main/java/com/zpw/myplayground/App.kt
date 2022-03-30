package com.zpw.myplayground

import android.app.Application

class App: Application() {
    val TAG = App::class.java.canonicalName

    override fun onCreate() {
        super.onCreate()
    }
}