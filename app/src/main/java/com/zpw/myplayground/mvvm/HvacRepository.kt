package com.zpw.myplayground.mvvm

import android.text.TextUtils
import com.fwk.sdk.hvac.HvacManager
import com.fwk.sdk.hvac.IHvacCallback
import com.zpw.myplayground.mvvm.model.BaseRepository
import com.zpw.myplayground.utils.Logger
import com.zpw.myplayground.utils.TAG_FWK
import com.zpw.myplayground.utils.log

class HvacRepository(): BaseRepository() {
    private val TAG: String = TAG_FWK + HvacRepository::class.java.simpleName

    private lateinit var hvacManager: HvacManager
    private var hvacViewModelCallback: HvacCallback? = null

    private val mHvacCallback: IHvacCallback = object : IHvacCallback {
        override fun onTemperatureChanged(temp: Double) {
            hvacViewModelCallback?.let {
                // 处理远程数据，将它转换为应用中需要的数据格式或内容
                val value = temp.toString()
                hvacViewModelCallback?.onTemperatureChanged(value)
            }
        }
    }

    constructor(hvacManager: HvacManager) : this() {
        this.hvacManager = hvacManager
        this.hvacManager.registerCallback(mHvacCallback)
    }

    fun release() {
        hvacManager.unregisterCallback(mHvacCallback)
    }

    fun requestTemperature() {
        Logger.log(TAG, "[requestTemperature]")
        HvacManager.getInstance().requestTemperature()
    }

    fun setTemperature(temperature: String?) {
        Logger.log(TAG, "[setTemperature] $temperature")
        if (temperature == null || TextUtils.isEmpty(temperature)) {
            return
        }
        hvacManager.setTemperature(temperature.toInt())
    }

    fun setHvacListener(callback: HvacCallback) {
        Logger.log(TAG, "[setHvacListener] $callback")
        hvacViewModelCallback = callback
    }

    fun removeHvacListener(callback: HvacCallback) {
        Logger.log(TAG, "[removeHvacListener] $callback")
        hvacViewModelCallback = null
    }
}