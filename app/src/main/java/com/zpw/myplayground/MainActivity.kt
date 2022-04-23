package com.zpw.myplayground

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

//import com.zpw.myapplication.LibraryActivityA

class MainActivity : AppCompatActivity() {
    private val TAG = MainActivity::class.java.canonicalName
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        startActivity(Intent(this, LibraryActivityA::class.java))
        Log.d(TAG, "onCreate: ")
    }
}