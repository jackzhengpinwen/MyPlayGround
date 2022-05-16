package com.zpw.myplayground.coroutines

import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.flow.*
import kotlin.coroutines.CoroutineContext
import kotlin.system.measureTimeMillis

private val TAG = CoroutineActivity.TAG

val flow1 = flow {
    listOf(9, 5, 2, 7).forEach {
        delay(500)
        emit(it)
    }
}

fun flow1() = runBlocking {
    flow1.collect {
        Log.d(TAG, "play something $it")
    }
}

fun flow2() {
    val job = GlobalScope.launch {
        flow1.collect {
            Log.d(TAG, "play something $it")
        }
    }
    runBlocking {
        delay(1100)
        job.cancel()
        Log.d(TAG, "scope cancel")
        delay(1000)
        Log.d(TAG, "process finish")
    }
}

fun flowDemo() {
    val flowMap = flow {
        listOf(9, 5, 2, 7).forEach {
            emit(it)
        }
    }
    runBlocking {
        flowMap.map {
            "become a string $it"
        }.collect {
            Log.d(TAG, "$it")
        }

        flowMap.transform<Int, String> {
            emit("become a string 1 $it")
            emit("become a string 2 $it")
        }.collect {
            Log.d(TAG, "$it")
        }

        flowMap.take(2).collect {
            Log.d(TAG, "$it")
        }

        flowMap.filter {
            it == 2
        }.collect {
            Log.d(TAG, "$it")
        }
        
        flowMap.filterNot { 
            it == 2
        }.collect {
            Log.d(TAG, "$it")
        }
    }

    val flowPrintThread = flow {
        listOf(9, 5, 2, 7).forEach {
            Log.d(TAG, "[${Thread.currentThread().name}] emit $it")
            emit(it)
        }
    }

    runBlocking {
        flowPrintThread.flowOn(Dispatchers.IO).collect {
            Log.d(TAG, "[${Thread.currentThread().name}] collect $it")
        }
    }

    val customDispatcher = object : CoroutineDispatcher() {
        override fun dispatch(context: CoroutineContext, block: Runnable) {
            Thread(block).start()
        }
    }

    runBlocking {
        flowPrintThread.flowOn(customDispatcher)
            .transform {
                Log.d(TAG, "[${Thread.currentThread().name}] transform $it")
                emit(it)
        }.flowOn(Dispatchers.IO)
            .map {
                Log.d(TAG, "[${Thread.currentThread().name}] map $it")
                it
            }.flowOn(customDispatcher)
            .collect {
                Log.d(TAG, "[${Thread.currentThread().name}] collect $it")
            }
    }

    val flowBuffer = flow {
        listOf(9, 5, 2, 7).forEach {
            delay(1000)
            emit(it)
        }
    }

    runBlocking {
        launch(Dispatchers.IO) {
            for(i in 1 .. 8) {
                delay(1000)
                Log.d(TAG, "${i}s pass")
            }
        }
        flowBuffer.buffer()
            .collect {
                delay(1000)
                Log.d(TAG, "collect $it")
            }
    }

    Dispatchers.Main
}

fun flowLifeCycleDemo() {
    var step = 0
    CoroutineScope(context = Dispatchers.Main.immediate).launch {
        System.out.println("launch")
        doAction(++step)
        flowOf("Hey")
            .onEach {
                System.out.println("onEach 1")
                doAction(++step)
            }
            .map {
                System.out.println("map")
                it.length
            }
            .onStart {
                System.out.println("onStart")
                doAction(++step)
            }
            .flowOn(Dispatchers.Default)
            .flatMapMerge {
                System.out.println("flatMapMerge")
                doAction(++step)
                flowOf(1)
                    .flowOn(Dispatchers.Main)
                    .onEach {
                        System.out.println("onEach 2")
                        doAction(++step)
                    }
            }
            .flowOn(Dispatchers.IO)
            .collect {
                System.out.println("collect")
                doAction(++step)
            }
    }
}

fun doAction(step: Int) {
    System.out.println("step ${step}, current thread is ${Thread.currentThread()}")
}