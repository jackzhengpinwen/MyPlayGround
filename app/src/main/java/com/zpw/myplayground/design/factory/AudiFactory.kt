package com.zpw.myplayground.design.factory

abstract class AudiFactory {
    abstract fun <T: AudiCar> createAudiCar(clazz: Class<T>): T
}