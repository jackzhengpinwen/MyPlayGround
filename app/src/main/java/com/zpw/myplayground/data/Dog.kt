package com.zpw.myplayground.data

import javax.inject.Inject


class Dog @Inject constructor(): Creature {
    override fun shout(): String {
        return "Woof"
    }
}