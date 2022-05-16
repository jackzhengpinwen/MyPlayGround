package com.zpw.myplayground.rxjava

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import de.hdodenhof.circleimageview.CircleImageView
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class RxjavaActivity : AppCompatActivity() {
    private val TAG = RxjavaActivity::class.java.canonicalName
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var step = 0
        Observable.just("Hey")
            .subscribeOn(Schedulers.io())
            .map(String::length)
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {
                // 1
                System.out.println("doOnSubscribe 1")
                doAction(++step)
            }.flatMap {
                // 2
                System.out.println("flatMap 1 it is ${it}")
                doAction(++step)
                Observable.interval(1, TimeUnit.SECONDS)
                    .subscribeOn(Schedulers.single())
                    .doOnSubscribe {
                        // 3
                        System.out.println("doOnSubscribe 2")
                        doAction(++step)
                    }
            }.subscribe {
                // 4
                System.out.println("subscribe it is ${it}")
                doAction(++step)
            }
    }

    private fun doAction(step: Int) {
        System.out.println("step ${step}, current thread is ${Thread.currentThread()}")
    }
}