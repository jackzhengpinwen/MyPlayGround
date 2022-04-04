package com.zpw.myapplication

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class LibraryActivityA: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_library)
        var count = 0
        for(i in 0..10) {
            count += i
        }
        findViewById<TextView>(R.id.result).text = count.toString()
    }
}