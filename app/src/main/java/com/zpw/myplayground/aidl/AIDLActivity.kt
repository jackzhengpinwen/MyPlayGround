package com.zpw.myplayground.aidl

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.zpw.myplayground.R

class AIDLActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }

    private fun startService() {
        // start swu
        val startServiceIntent = Intent(this, SimpleService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(startServiceIntent)
        } else {
            startService(startServiceIntent)
        }
    }
}