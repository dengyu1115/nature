package org.nature.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout.LayoutParams;
import org.nature.html.NativeManager;

import java.util.List;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

/**
 * 上下文工具
 * @author nature
 * @version 1.0.0
 * @since 2025/12/20
 */
public class CtxUtil {

    @SuppressLint("StaticFieldLeak")
    private static Context context;

    private static final NativeManager manager = new NativeManager();

    @SuppressLint("StaticFieldLeak")
    private static WebView webview;

    /**
     * 初始化
     * @param context 上下文
     */
    public static void init(Context context) {
        CtxUtil.context = context;
        webview = buildWebview();
    }

    /**
     * 获取上下文
     * @return 上下文
     */
    public static Context get() {
        return CtxUtil.context;
    }

    public static View getView() {
        return webview;
    }

    public static void onBack() {
        webview.evaluateJavascript("closePage();", null);
    }

    public static void callback(String id, String res) {
        webview.post(() -> {
            String res_str = res.replace("\\", "\\\\").replace("'", "\\'");
            webview.evaluateJavascript("asyncCallback('" + id + "', '" + res_str + "');", null);
        });
    }

    public static void closePage() {
        ((Activity) context).moveTaskToBack(true);
    }

    public static void show() {
        webview.loadUrl("file:///android_asset/index.html");
        webview.clearHistory();
    }

    public static void refresh() {
        webview.post(CtxUtil::show);
    }

    public static void startService(Class<?> clazz) {
        context.startService(new Intent(context, clazz));
    }

    public static void stopService(Class<?> clazz) {
        context.stopService(new Intent(context, clazz));
    }

    @SuppressWarnings("deprecation")
    public static boolean isServiceRunning(Class<?> clazz) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (manager == null) {
            return false;
        }
        List<RunningServiceInfo> services = manager.getRunningServices(Integer.MAX_VALUE);
        for (RunningServiceInfo i : services) {
            if (clazz.getName().equals(i.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


    @SuppressLint("SetJavaScriptEnabled")
    @SuppressWarnings("deprecation")
    private static WebView buildWebview() {
        WebView webView = new WebView(context);
        webView.setLayoutParams(new LayoutParams(MATCH_PARENT, MATCH_PARENT));
        webView.setBackgroundColor(Color.TRANSPARENT);
        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient());
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setAllowFileAccessFromFileURLs(true);
        settings.setAllowUniversalAccessFromFileURLs(true);
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        // 页面内容查询接口
        webView.addJavascriptInterface(manager, "native");
        return webView;
    }

}
