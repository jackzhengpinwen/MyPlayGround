package com.zpw.myplayground.dagger.data

import javax.inject.Inject


class Cat @Inject constructor(): Creature {

    override fun shout(): String {
        return "Miao"
    }
}