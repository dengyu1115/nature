package org.nature.common.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import org.jetbrains.annotations.NotNull;
import org.nature.common.ioc.holder.InstanceHolder;
import org.nature.common.ioc.starter.ComponentStarter;
import org.nature.common.util.CtxUtil;
import org.nature.common.util.NotifyUtil;
import org.nature.html.manager.NativeManager;

import java.util.Stack;

import static android.Manifest.permission.*;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;

/**
 * 应用入口（对其他组件使用单例模式加载）
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/5
 */
public class MainActivity extends AppCompatActivity {
    /**
     * 请求全局存储权限
     */
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    /**
     * 全局页面对象
     */
    private LinearLayout view;

    private NativeManager nativeManager;

    private final Stack<WebView> viewStack = new Stack<>();

    @RequiresApi(api = Build.VERSION_CODES.R)
    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 初始化处理
        super.onCreate(savedInstanceState);
        //  请求全局存储权限
        this.verifyStoragePermissions(this);
        // 启动组件（控制只执行一次）
        if ((this.getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            return;
        }
        // 单例组件加载
        ComponentStarter.getInstance().start(this);
        CtxUtil.init(this);
        // 通知工具初始化
        NotifyUtil.init();
        // 全局页面初始化
        nativeManager = InstanceHolder.get(NativeManager.class);
        view = new LinearLayout(this);
        this.getWindow().setFlags(FLAG_LAYOUT_NO_LIMITS, FLAG_LAYOUT_NO_LIMITS);
        this.setContentView(view);
        this.show();
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 控制按返回时候让应用后台运行或者执行关闭当前操作页面回上个页面
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            int size = viewStack.size();
            if (size == 1) {
                return this.moveTaskToBack(true);
            }
            ;
            WebView view = viewStack.pop();
            this.view.removeView(view);
            this.view.addView(viewStack.peek());
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private void verifyStoragePermissions(Activity activity) {
        // 检测是否有写的权限
        int permission = ActivityCompat.checkSelfPermission(activity, MANAGE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // 没有写的权限，去申请写的权限，会弹出对话框
            String[] permissions = {MANAGE_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE};
            ActivityCompat.requestPermissions(activity, permissions, REQUEST_EXTERNAL_STORAGE);
        }
    }

    private void show() {
        WebView view = this.buildWebview();
        this.viewStack.push(view);
        this.view.addView(view);
        view.loadUrl("file:///android_asset/index.html?id=main");
    }

    @SuppressLint("SetJavaScriptEnabled")
    @SuppressWarnings("deprecation")
    private WebView buildWebview() {
        WebView webView = new WebView(this);
        webView.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        webView.setBackgroundColor(Color.TRANSPARENT);
        webView.setWebViewClient(this.buildClient());
        webView.setWebChromeClient(new WebChromeClient());
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setAllowFileAccessFromFileURLs(true);
        settings.setAllowUniversalAccessFromFileURLs(true);
        // 页面内容查询接口
        webView.addJavascriptInterface(nativeManager, "native");
        return webView;
    }

    @SuppressWarnings("deprecation")
    @NotNull
    private WebViewClient buildClient() {
        return new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                WebView webview = MainActivity.this.buildWebview();
                MainActivity.this.view.removeView(view);
                MainActivity.this.view.addView(webview);
                viewStack.push(webview);
                webview.loadUrl(url);
                return true;
            }
        };
    }

}
