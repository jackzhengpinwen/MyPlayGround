package com.zpw.myplayground.koin.module

import com.zpw.myplayground.data.Cat
import com.zpw.myplayground.data.Creature
import com.zpw.myplayground.data.Dog
import org.koin.core.qualifier.named
import org.koin.dsl.module

const val DOG: Int = 0
const val CAT: Int = 1

val creatureModule = module {
    factory { (creatureType: Int) ->
        when(creatureType) {
            DOG -> Dog()
            CAT -> Cat()
            else -> Dog()
        }
    }
    scope(named("DogSession")) {
        scoped {
            Dog()
        }
    }
}