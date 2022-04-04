package com.zpw.myplayground.mvvm.utils

import android.os.Handler
import android.os.Looper
import com.zpw.myplayground.utils.Logger
import com.zpw.myplayground.utils.TAG_FWK
import com.zpw.myplayground.utils.log
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.ReentrantLock

open class AppExecutors {
    private val TAG: String = TAG_FWK + AppExecutors::class.java.simpleName

    companion object {
        val APP_EXECUTORS = AppExecutors()
        open val KEEP_ALIVE = 30L
        open val MAX_PRIORITY = 10
        open val MIN_PRIORITY = 0
        open val MIDDLE_PRIORITY = 5
    }

    private var isPause = false
    private var poolExecutor: ThreadPoolExecutor
    private val lock = ReentrantLock()
    private var pauseCondition: Condition
    private var mainHandler: Handler

    constructor() {
        mainHandler = Handler(Looper.getMainLooper())
        pauseCondition = lock.newCondition()
        val cpuCount = Runtime.getRuntime().availableProcessors()
        val corePoolSize = cpuCount + 1
        val maxPoolSize = Int.MAX_VALUE
        val blockingQueue = PriorityBlockingQueue<Runnable>()

        val seq = AtomicLong()

        val factory = ThreadFactory { runnable: Runnable? ->
            val thread = Thread(runnable)
            thread.name = "FWK_TASK#" + seq.getAndIncrement()
            thread
        }

        poolExecutor = object : ThreadPoolExecutor(
            corePoolSize,
            maxPoolSize,
            KEEP_ALIVE,
            TimeUnit.SECONDS,
            blockingQueue,
            factory
        ) {
            override fun beforeExecute(t: Thread, r: Runnable) {
                super.beforeExecute(t, r)
                if (isPause) {
                    lock.lock()
                    try {
                        pauseCondition.await()
                    } catch (exception: InterruptedException) {
                        Logger.log(TAG, "beforeExecute:$exception")
                    } finally {
                        lock.unlock()
                    }
                }
            }

            override fun afterExecute(r: Runnable, t: Throwable) {
                super.afterExecute(r, t)
                Logger.log(TAG, "this thread priority is " + (r as PriorityRunnable).priority)
            }
        }
    }

    fun get(): AppExecutors = APP_EXECUTORS

    fun execute(priority: Int, runnable: Runnable) {
        poolExecutor.execute(PriorityRunnable(priority, runnable))
    }

    fun execute(runnable: Runnable) {
        execute(MIDDLE_PRIORITY, runnable)
    }

    fun post(runnable: Runnable) {
        mainHandler.post(runnable)
    }

    fun postDelay(runnable: Runnable, delayMills: Long) {
        mainHandler.postDelayed(runnable, delayMills)
    }

    fun postRemove(runnable: Runnable) {
        mainHandler.removeCallbacks(runnable)
    }

    fun pause() {
        lock.lock()
        try {
            isPause = true
            Logger.log(TAG, "thread pool is paused")
        } finally {
            lock.unlock()
        }
    }

    fun resume() {
        lock.lock()
        try {
            isPause = false
            pauseCondition.signalAll()
        } finally {
            lock.unlock()
        }
        Logger.log(TAG, "thread pool is resumed")
    }

    private class MainThreadExecutor : Executor {
        private val mainThreadHandler = Handler(Looper.getMainLooper())
        fun removeCallbacksAndMessages(token: Any?) {
            mainThreadHandler.removeCallbacksAndMessages(token)
        }

        override fun execute(runnable: Runnable) {
            mainThreadHandler.post(runnable)
        }
    }

    private abstract class Callable<T> : Runnable {
        private val mainThreadHandler = Handler(Looper.getMainLooper())
        override fun run() {
            mainThreadHandler.post(Runnable { onPrepare() })
            val background = onBackground()
            mainThreadHandler.removeCallbacksAndMessages(null)
            mainThreadHandler.post(Runnable { onCompleted(background) })
        }

        protected fun onPrepare() {
            // do nothing.
        }

        protected abstract fun onBackground(): T
        protected abstract fun onCompleted(background: T)
    }

    class PriorityRunnable(val priority: Int, private val runnable: Runnable) : Runnable, Comparable<PriorityRunnable?> {

        override fun run() {
            runnable.run()
        }

        override fun compareTo(runnable: PriorityRunnable?): Int {
            return Integer.compare(runnable?.priority!!, this.priority)
        }
    }
}