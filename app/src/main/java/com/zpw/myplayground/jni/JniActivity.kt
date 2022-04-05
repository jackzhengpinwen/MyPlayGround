package com.zpw.myplayground.jni

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class JniActivity : AppCompatActivity() {
    private external fun helloFromCXX()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        System.loadLibrary("entrypoint")
        helloFromCXX()
    }
}