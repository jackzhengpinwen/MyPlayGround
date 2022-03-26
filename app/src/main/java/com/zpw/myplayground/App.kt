package com.zpw.myplayground

import android.app.Application
import com.zpw.myplayground.koin.module.creatureModule
import org.koin.core.context.GlobalContext.startKoin

class App: Application() {
    override fun onCreate() {
        super.onCreate()

    }
}