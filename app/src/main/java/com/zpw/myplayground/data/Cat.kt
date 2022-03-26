package com.zpw.myplayground.data

import javax.inject.Inject


class Cat @Inject constructor(): Creature {

    override fun shout(): String {
        return "Miao"
    }
}