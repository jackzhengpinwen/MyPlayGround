package com.zpw.myplayground

import android.app.Application
import com.zpw.myplayground.webview.base.WebViewInitTask
import com.zpw.myplayground.webview.utils.ContextHolder
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
        ContextHolder.application = this
        WebViewInitTask.init(this)
    }
}