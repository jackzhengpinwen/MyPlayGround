package com.zpw.myplayground.dagger.component

import com.zpw.myplayground.dagger.DaggerActivity
import com.zpw.myplayground.dagger.module.CreatureModule
import dagger.Component

@Component(modules = [CreatureModule::class])
interface CreatureComponent {
    fun inject(activity: DaggerActivity)
}