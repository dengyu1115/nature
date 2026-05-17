package org.nature.util

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import android.widget.LinearLayout.LayoutParams
import org.nature.html.NativeManager
import java.util.*

/**
 * 上下文工具
 * @author nature
 * @version 1.0.0
 * @since 2025/12/20
 */
@SuppressLint("StaticFieldLeak")
object CtxUtil {

    private var context: Context? = null

    /**
     * 全局页面对象
     */
    private var view: LinearLayout? = null

    private val manager = NativeManager()

    private val viewStack = Stack<WebView>()

    /**
     * 初始化
     * @param context 上下文
     */
    @JvmStatic
    fun init(context: Context) {
        CtxUtil.context = context
        view = LinearLayout(context)
    }

    /**
     * 获取上下文
     * @return 上下文
     */
    @JvmStatic
    fun get(): Context {
        return context!!
    }

    @JvmStatic
    fun getView(): View {
        return view!!
    }

    @JvmStatic
    fun onBack(): Boolean {
        val size = viewStack.size
        if (size == 1) {
            return true
        }
        val view = viewStack.pop()
        view.destroy()
        CtxUtil.view!!.removeView(view)
        CtxUtil.view!!.addView(viewStack.peek())
        return false
    }

    @JvmStatic
    fun show() {
        val view = buildWebview()
        viewStack.push(view)
        CtxUtil.view!!.addView(view)
        view.loadUrl("file:///android_asset/index.html?id=main")
    }

    @JvmStatic
    fun callback(cript: String) {
        val webView = viewStack.peek()
        webView.post { webView.evaluateJavascript(cript, null) }
    }

    @JvmStatic
    fun refresh() {
        view!!.post {
            viewStack.clear()
            view!!.removeAllViews()
            show()
        }
    }

    @JvmStatic
    fun startService(clazz: Class<*>) {
        context!!.startService(Intent(context, clazz))
    }

    @JvmStatic
    fun stopService(clazz: Class<*>) {
        context!!.stopService(Intent(context, clazz))
    }

    @Suppress("DEPRECATION")
    @JvmStatic
    fun isServiceRunning(clazz: Class<*>): Boolean {
        val manager = context!!.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val services = manager.getRunningServices(Int.MAX_VALUE)
        for (i in services) {
            if (clazz.name == i.service.className) {
                return true
            }
        }
        return false
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Suppress("DEPRECATION")
    private fun buildWebview(): WebView {
        val webView = WebView(context!!)
        webView.layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
        webView.setBackgroundColor(Color.TRANSPARENT)
        webView.webViewClient = buildClient()
        webView.webChromeClient = WebChromeClient()
        val settings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.allowFileAccess = true
        settings.allowContentAccess = true
        settings.allowFileAccessFromFileURLs = true
        settings.allowUniversalAccessFromFileURLs = true
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        // 页面内容查询接口
        webView.addJavascriptInterface(manager, "native")
        return webView
    }


    private fun buildClient(): WebViewClient {
        return object : WebViewClient() {

            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                val webview = buildWebview()
                CtxUtil.view!!.removeView(view)
                CtxUtil.view!!.addView(webview)
                viewStack.push(webview)
                webview.post {
                    webview.loadUrl(url)
                }
                return true
            }
        }
    }
}
