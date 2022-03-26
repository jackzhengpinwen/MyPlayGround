package com.zpw.myplayground.dagger.module

import com.zpw.myplayground.data.Cat
import com.zpw.myplayground.data.Creature
import com.zpw.myplayground.data.Dog
import dagger.Module
import dagger.Provides

@Module
class CreatureModule(private val creatureType: Int) {
    companion object {
        const val DOG: Int = 0
        const val CAT: Int = 1
    }

    @Provides
    fun provideCreature(): Creature {
        return when(creatureType) {
            CAT -> Cat()
            DOG -> Dog()
            else -> Dog()
        }
    }
}