package com.zpw.myplayground.design.factory

class AudiCarFactory: AudiFactory() {
    override fun <T : AudiCar> createAudiCar(clazz: Class<T>): T {
        var car: AudiCar? = null
        try {
            car = Class.forName(clazz.name).newInstance() as AudiCar
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return car as T
    }
}