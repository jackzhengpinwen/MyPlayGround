package com.zpw.myplayground.mvvm.view

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.zpw.myplayground.utils.Logger
import com.zpw.myplayground.utils.TAG_FWK
import com.zpw.myplayground.utils.log

abstract class BaseActivity: AppCompatActivity() {
    private val TAG: String = TAG_FWK + javaClass.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.log(TAG, "[onCreate]")
    }

    override fun onStart() {
        super.onStart()
        Logger.log(TAG, "[onStart]")
    }

    override fun onRestart() {
        super.onRestart()
        Logger.log(TAG, "[onRestart]")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Logger.log(TAG, "[onSaveInstanceState]")
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent != null) {
            Logger.log(TAG, "[onNewIntent]$intent")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Logger.log(
            TAG, "[onActivityResult] requestCode:" + requestCode + ";"
                    + "resultCode:" + requestCode
        )
    }

    override fun onResume() {
        super.onResume()
        Logger.log(TAG, "[onResume]")
    }

    override fun onPause() {
        super.onPause()
        Logger.log(TAG, "[onPause]")
    }

    override fun onStop() {
        super.onStop()
        Logger.log(TAG, "[onStop]")
    }

    override fun onDestroy() {
        super.onDestroy()
        Logger.log(TAG, "[onDestroy]")
    }
}