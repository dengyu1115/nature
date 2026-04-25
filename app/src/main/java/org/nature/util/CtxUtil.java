package org.nature.util;

import android.annotation.SuppressLint;
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
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import org.nature.html.manager.NativeManager;

import java.util.List;
import java.util.Stack;

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

    /**
     * 全局页面对象
     */
    @SuppressLint("StaticFieldLeak")
    private static LinearLayout view;

    private static final NativeManager manager = new NativeManager();

    private static final Stack<WebView> viewStack = new Stack<>();

    /**
     * 初始化
     * @param context 上下文
     */
    public static void init(Context context) {
        CtxUtil.context = context;
        view = new LinearLayout(context);
    }

    /**
     * 获取上下文
     * @return 上下文
     */
    public static Context get() {
        return CtxUtil.context;
    }

    public static View getView() {
        return view;
    }

    public static boolean onBack() {
        int size = viewStack.size();
        if (size == 1) {
            return true;
        }
        WebView view = viewStack.pop();
        view.destroy();
        CtxUtil.view.removeView(view);
        CtxUtil.view.addView(viewStack.peek());
        return false;
    }

    public static void show() {
        WebView view = buildWebview();
        viewStack.push(view);
        CtxUtil.view.addView(view);
        view.loadUrl("file:///android_asset/index.html?id=main");
    }

    public static void refresh() {
        view.post(() -> {
            viewStack.clear();
            view.removeAllViews();
            show();
        });
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
        webView.setWebViewClient(buildClient());
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

    @SuppressWarnings("deprecation")
    private static WebViewClient buildClient() {
        return new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                WebView webview = buildWebview();
                CtxUtil.view.removeView(view);
                CtxUtil.view.addView(webview);
                viewStack.push(webview);
                webview.post(() -> {
                    webview.loadUrl(url);
                });
                return true;
            }
        };
    }

}
