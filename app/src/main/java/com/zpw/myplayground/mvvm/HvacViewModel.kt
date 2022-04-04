package com.zpw.myplayground.mvvm

import android.view.View
import androidx.lifecycle.MutableLiveData
import com.zpw.myplayground.mvvm.utils.AppExecutors
import com.zpw.myplayground.mvvm.viewmodel.BaseViewModel
import com.zpw.myplayground.utils.Logger
import com.zpw.myplayground.utils.TAG_FWK
import com.zpw.myplayground.utils.log

class HvacViewModel(
    private val repo: HvacRepository,
    private val executors: AppExecutors
    ): BaseViewModel<HvacRepository>(repo) {
    private val TAG: String = TAG_FWK + HvacViewModel::class.java.simpleName

    private var tempLive: MutableLiveData<String>? = null

    private val hvacCallback: HvacCallback = object : HvacCallback {
        override fun onTemperatureChanged(temp: String) {
            Logger.log(TAG, "[onTemperatureChanged] $temp")
            getTempLive().postValue(temp)
        }
    }

    init {
        repo.setHvacListener(hvacCallback)
    }

    override fun onCleared() {
        super.onCleared()
        repository.removeHvacListener(hvacCallback)
        repository.release()
    }

    /**
     * 请求页面数据
     */
    fun requestTemperature() {
        repository.requestTemperature()
    }

    /**
     * 将温度数据设定到Service中
     *
     * @param view
     */
    fun setTemperature(view: View?) {
        repository.setTemperature(getTempLive().value)
    }

    fun getTempLive(): MutableLiveData<String> {
        if (tempLive == null) {
            tempLive = MutableLiveData<String>()
        }
        return tempLive!!
    }
}