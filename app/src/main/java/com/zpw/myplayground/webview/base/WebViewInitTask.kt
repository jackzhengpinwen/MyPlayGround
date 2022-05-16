package com.zpw.myplayground.webview.base

import android.app.Application
import com.tencent.smtt.export.external.TbsCoreSettings
import com.tencent.smtt.sdk.QbSdk
import com.zpw.myplayground.webview.utils.log
import com.zpw.myplayground.webview.utils.showToast

object WebViewInitTask {
    fun init(application: Application) {
        initWebView(application)
        WebViewCacheHolder.init(application)
        WebViewInterceptRequestProxy.init(application)
    }

    private fun initWebView(context: Application) {
        QbSdk.setDownloadWithoutWifi(true)
        val map = mutableMapOf<String, Any>()
        map[TbsCoreSettings.TBS_SETTINGS_USE_PRIVATE_CLASSLOADER] = true
        map[TbsCoreSettings.TBS_SETTINGS_USE_SPEEDY_CLASSLOADER] = true
        map[TbsCoreSettings.TBS_SETTINGS_USE_DEXLOADER_SERVICE] = true
        QbSdk.initTbsSettings(map)
        val cb : QbSdk.PreInitCallback = object : QbSdk.PreInitCallback {
            override fun onCoreInitFinished() {
                log("onCoreInitFinished")
            }

            override fun onViewInitFinished(arg0: Boolean) {
                showToast("onViewInitFinished: $arg0")
                log("onViewInitFinished: $arg0")
            }
        }
        QbSdk.initX5Environment(context, cb)
    }
}