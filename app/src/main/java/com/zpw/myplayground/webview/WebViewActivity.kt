package com.zpw.myplayground.webview

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.zpw.myplayground.R
import com.zpw.myplayground.webview.base.RobustWebView
import com.zpw.myplayground.webview.base.WebViewCacheHolder
import com.zpw.myplayground.webview.base.WebViewListener
import com.zpw.myplayground.webview.utils.showToast
import de.hdodenhof.circleimageview.CircleImageView

class WebViewActivity : AppCompatActivity() {
    private val TAG = WebViewActivity::class.java.canonicalName

    private val url1 = "https://juejin.cn/user/923245496518439/posts"

    private val url2 = "https://www.bilibili.com/"

    private val url3 = "https://p26-passport.byteacctimg.com/img/user-avatar/6019f80db5be42d33c31c98adaf3fa8c~300x300.image"

    private val webViewContainer by lazy {
        findViewById<ViewGroup>(R.id.webViewContainer)
    }

    private val tvTitle by lazy {
        findViewById<TextView>(R.id.tvTitle)
    }

    private val tvProgress by lazy {
        findViewById<TextView>(R.id.tvProgress)
    }

    private lateinit var webview: RobustWebView

    private val webViewListener = object : WebViewListener {
        override fun onProgressChanged(webview: RobustWebView, progress: Int) {
            tvProgress.text = progress.toString()
        }

        override fun onReceivedTitle(webview: RobustWebView, title: String) {
            tvTitle.text = title
        }

        override fun onPageFinished(webview: RobustWebView, url: String) {
            super.onPageFinished(webview, url)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)
        webview = WebViewCacheHolder.acquireWebViewInternal(this)
        webview.webViewListener = webViewListener

        val layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        webViewContainer.addView(webview, layoutParams)

        findViewById<View>(R.id.tvBack).setOnClickListener {
            onBackPressed()
        }
        findViewById<View>(R.id.btnOpenUrl1).setOnClickListener {
            webview.loadUrl(url1)
        }
        findViewById<View>(R.id.btnOpenUrl2).setOnClickListener {
            webview.loadUrl(url2)
        }
        findViewById<View>(R.id.btnOpenUrl3).setOnClickListener {
            webview.toLoadUrl(url3, "")
        }
        findViewById<View>(R.id.btnReload).setOnClickListener {
            webview.reload()
        }
        findViewById<View>(R.id.btnOpenHtml).setOnClickListener {
            webview.loadUrl("""file:/android_asset/javascript.html""")
        }
        findViewById<View>(R.id.btnCallJsByAndroid).setOnClickListener {
            val parameter = "\"业志陈\""
            webview.evaluateJavascript("javascript:callJsByAndroid(${parameter})") {
                showToast("evaluateJavascript: $it")
            }
//            webView.loadUrl("javascript:callJsByAndroid(${parameter})")
        }
        findViewById<View>(R.id.btnShowToastByAndroid).setOnClickListener {
            webview.loadUrl("javascript:showToastByAndroid()")
        }
        findViewById<View>(R.id.btnCallJsPrompt).setOnClickListener {
            webview.loadUrl("javascript:callJsPrompt()")
        }
    }

    override fun onBackPressed() {
        if (webview.canGoBack()) {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        WebViewCacheHolder.prepareWebView()
    }
}