package com.zpw.myplayground.mvvm.viewmodel

import androidx.lifecycle.ViewModel
import com.zpw.myplayground.mvvm.model.BaseRepository

abstract class BaseViewModel<M: BaseRepository> constructor(
    protected val repository: M
) : ViewModel() {
}