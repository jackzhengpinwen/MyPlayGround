package com.zpw.myplayground.mvvm.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.zpw.myplayground.mvvm.model.BaseRepository

abstract class BaseAndroidViewModel<M: BaseRepository> constructor(
    var app: Application,
    private val repository: M
): AndroidViewModel(app) {
}