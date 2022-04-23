package com.zpw.myplayground.hilt

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DataModule {
    @Singleton
    @Provides
    fun provideMusicDB(): MusicDatabase {
        return MusicDatabase(mutableMapOf(Pair("1", "china")))
    }
}

data class MusicDatabase(
    val map: Map<String, String>
)