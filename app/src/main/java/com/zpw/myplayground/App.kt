package com.zpw.myplayground

import android.app.Application
import com.zpw.myapplication.Library1Utils
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class App: Application() {
    val TAG = App::class.java.canonicalName

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        Library1Utils.whoAmI()
//        Library2Utils.whoAmICompat()
    }
}