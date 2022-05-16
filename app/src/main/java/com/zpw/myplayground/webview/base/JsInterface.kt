package com.zpw.myplayground.webview.base

import android.webkit.JavascriptInterface
import com.zpw.myplayground.webview.utils.log
import com.zpw.myplayground.webview.utils.showToast

class JsInterface {

    @JavascriptInterface
    fun showToastByAndroid(log: String) {
        log("showToastByAndroid:$log")
        showToast(log)
    }

}