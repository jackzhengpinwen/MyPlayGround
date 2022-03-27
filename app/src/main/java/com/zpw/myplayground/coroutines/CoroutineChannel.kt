package com.zpw.myplayground.coroutines

import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.produce
import kotlin.system.measureTimeMillis

private val TAG = CoroutineActivity.TAG

val channelForResult1 = Channel<String>()
val channelForResult3 = Channel<String>()

fun someMethod() {
    GlobalScope.launch(Dispatchers.IO) {
        val time = measureTimeMillis {
            val result1 = channelForResult1.receive()
            val result3 = channelForResult3.receive()
            val result2 = async { sampleDemoSuspendFunction("$result1 result 2 ") }
            val result4 = async { sampleDemoSuspendFunction("$result3 result 4 ") }
            Log.d(TAG, "${result2.await()} ${result4.await()}")
        }
        Log.d(TAG, "spend time:$time")
    }
    GlobalScope.launch(Dispatchers.IO) {
        delay(1000)
        channelForResult1.send("result 1")
        delay(1000)
        channelForResult3.send("result 3")
    }
    runBlocking {
        delay(3000)
    }
}

suspend fun sampleDemoSuspendFunction(content: String): String {
    delay(1000)
    return content
}

val channel = Channel<Int>(10)

fun someMethod2() {
    GlobalScope.launch {
        for(i in 1 .. 10) {
            channel.send(i)
        }
        Log.d(TAG, "send finish")
    }
    GlobalScope.launch {
        while (true) {
            delay(100)
            val result = channel.receive()
            Log.d(TAG, "left side print : $result")
        }
    }
    GlobalScope.launch {
        while(true){
            delay(100)
            val result = channel.receive()
            Log.d(TAG, "right side print : $result")
        }
    }
    runBlocking {
        delay(3000)
    }
    channel.close()
}

val channel2 = Channel<Int>(1)

fun someMethod3() {
    val offerResult = channel2.offer(1)
    Log.d(TAG, "is offer result success: $offerResult")

    val pollResult = channel2.poll()
    Log.d(TAG, "is poll result success: $pollResult")
}

val channel3 = GlobalScope.produce<Int> {

}