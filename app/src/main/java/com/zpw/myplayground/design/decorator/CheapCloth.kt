package com.zpw.myplayground.design.decorator

class CheapCloth(person: Person): PersonCloth(person) {
    private fun dressShorts() = println("穿件短裤")

    override fun dressed() {
        super.dressed()
        dressShorts()
    }
}