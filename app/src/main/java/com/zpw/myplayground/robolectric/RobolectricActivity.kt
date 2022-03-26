package com.zpw.myplayground.robolectric

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.zpw.myplayground.R

class RobolectricActivity : AppCompatActivity() {
    var listener: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_robolectric)
        findViewById<TextView>(R.id.text_jump).setOnClickListener {
            startActivity(Intent(this, RobolectricAActivity::class.java))
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            findViewById<TextView>(R.id.text_jump).text = "Android Device Version is beyond Marshmallow."
        } else {
            findViewById<TextView>(R.id.text_jump).text = "Android Device Version is under Marshmallow."
        }
    }

    override fun onResume() {
        super.onResume()
        listener = "listener"
    }

    override fun onPause() {
        super.onPause()
        listener = null
    }
}