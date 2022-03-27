package com.zpw.myplayground.coroutines

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.zpw.myplayground.R
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext
import kotlin.system.measureTimeMillis

open class CoroutineActivity : AppCompatActivity(), CoroutineScope by MainScope() {
    companion object {
        open val TAG = CoroutineActivity::class.java.canonicalName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coroutine)
        val textView = findViewById<TextView>(R.id.text_count_down)
//        val job = GlobalScope.launch(Dispatchers.Main) {
//            for(i in 10 downTo 1) {
//                textView.text = "count down $i ..."
//                Log.d(TAG, "count down $i ...")
//                delay(1000)
//            }
//            textView.text = "Done!"
//        }
//        GlobalScope.launch(Dispatchers.Main) {
//            delay(5000)
//            job.cancel()
//        }
//        launch(Dispatchers.Main) {
//            reasSomething()
//            job.cancel()
//            Log.d(TAG, "job is canceled")
//        }
//
//        val text = async(Dispatchers.IO) {
//            delay(3000)
//            "result"
//        }
//        launch(Dispatchers.Main) {
//            val result = text.await()
//            Log.d(TAG, "result is $result")
//        }
//
//        launch(Dispatchers.Main) {
//            val time = measureTimeMillis {
//                val one = async { doSomething1() }
//                val two = async { doSomething2() }
//                Log.d(TAG, "The answer is ${one.await() + two.await()}")
//            }
//            Log.d(TAG, "Completed in $time ms")
//        }
//
//        someMethod()
//        someMethod2()

//        flow1()
//        flow2()
        flowDemo()
        Log.d(TAG, "onCreate: end!!")
    }

    suspend fun doSomething1(): Int {
        delay(1000)
        return 1
    }

    suspend fun doSomething2(): Int {
        delay(1000)
        return 2
    }

    suspend fun reasSomething() = withContext(Dispatchers.IO) {
        delay(5 * 1000)
    }

    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }
}