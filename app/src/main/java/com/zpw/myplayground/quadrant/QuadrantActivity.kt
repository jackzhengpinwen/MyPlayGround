package com.zpw.myplayground.quadrant

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.zpw.myplayground.R

class QuadrantActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quadrant)
        findViewById<View>(R.id.jump_to_libraty)?.setOnClickListener {
            val intent = Intent()
//            intent.setClass(this, QuadrantConstants.LIBRARY_ACTIVITY_A)
            startActivity(intent)
        }
    }
}