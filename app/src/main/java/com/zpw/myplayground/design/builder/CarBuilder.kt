package com.zpw.myplayground.design.builder

interface CarBuilder {
    fun build(): Car
    fun setColor(color: String): CarBuilder
    fun setWheels(wheels: Int): CarBuilder
}