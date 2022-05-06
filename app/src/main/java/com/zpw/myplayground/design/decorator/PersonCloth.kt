package com.zpw.myplayground.design.decorator

open class PersonCloth(val person: Person): Person() {
    override fun dressed() {
        person.dressed()
    }
}