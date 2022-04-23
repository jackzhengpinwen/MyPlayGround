package com.zpw.myplayground.hilt

import timber.log.Timber
import javax.inject.Inject

class MusicPlayer @Inject constructor(val musicDatabase: MusicDatabase) {
    fun play(id: String) {
        Timber.d("MusicPlayer play ${musicDatabase.map[id]}")
    }
}