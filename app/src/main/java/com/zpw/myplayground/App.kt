package com.zpw.myplayground

import android.app.Application
import com.zpw.myplayground.webview.base.WebViewInitTask
import com.zpw.myplayground.webview.utils.ContextHolder
import dagger.hilt.android.HiltAndroidApp
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.FlutterEngineCache
import io.flutter.embedding.engine.dart.DartExecutor
import timber.log.Timber

@HiltAndroidApp
class App: Application() {
    val TAG = App::class.java.canonicalName

    lateinit var flutterEngine : FlutterEngine

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        ContextHolder.application = this
        WebViewInitTask.init(this)

        // Instantiate a FlutterEngine.
        flutterEngine = FlutterEngine(this)

        // Start executing Dart code to pre-warm the FlutterEngine.
        flutterEngine.dartExecutor.executeDartEntrypoint(
            DartExecutor.DartEntrypoint.createDefault()
        )

        // Cache the FlutterEngine to be used by FlutterActivity.
        FlutterEngineCache
            .getInstance()
            .put("nps_flutter_engine_name", flutterEngine)
    }
}