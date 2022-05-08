package com.zpw.myplayground

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import de.hdodenhof.circleimageview.CircleImageView

//import com.zpw.myapplication.LibraryActivityA

class MainActivity : AppCompatActivity() {
    private val TAG = MainActivity::class.java.canonicalName
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        startActivity(Intent(this, LibraryActivityA::class.java))
        Log.d(TAG, "onCreate: ")
        findViewById<CircleImageView>(R.id.image).setOnClickListener(object: View.OnClickListener {
            override fun onClick(v: View?) {
                Log.d(TAG, "click!!!")
            }
        })
    }
}