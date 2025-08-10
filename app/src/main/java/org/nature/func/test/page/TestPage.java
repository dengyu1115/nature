package org.nature.func.test.page;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.nature.common.ioc.annotation.PageView;
import org.nature.common.page.Page;
import org.nature.common.util.ClickUtil;
import org.nature.common.util.NotifyUtil;
import org.nature.common.view.Button;
import org.nature.common.view.Input;

/**
 * 测试功能
 * @author Nature
 * @version 1.0.0
 * @since 2024/9/14
 */
@PageView(name = "测试功能", group = "基础", col = 2, row = 2)
public class TestPage extends Page {

    private Button ttsBtn;

    private Button webBtn;

    @Override
    protected void makeStructure() {
        page.setOrientation(LinearLayout.VERTICAL);
        page.addView(ttsBtn = template.button("tts", 10, 7));
        page.addView(webBtn = template.button("web", 10, 7));
    }

    private final Handler uiHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onShow() {
        ClickUtil.onClick(ttsBtn, () -> {
            Input ttsText = template.textArea(40, 40);
            LinearLayout line = template.line(50, 50, template.text("内容", 5, 7), ttsText);
            template.confirm("请输入要转语音的文本", line, () -> {
                String text = ttsText.getValue();
                NotifyUtil.speak(text);
            });
        });
        ClickUtil.onClick(webBtn, () -> {

            System.out.println(Thread.currentThread().getName() + ":1");

            Thread thread = new Thread(() -> {
                System.out.println(Thread.currentThread().getName() + ":5");
                System.out.println(handle());
                System.out.println(Thread.currentThread().getName() + ":6");
            });
            thread.start();
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println(Thread.currentThread().getName() + ":2");
        });
    }

    @SuppressLint("SetJavaScriptEnabled")
    private String handle() {
        System.out.println(Thread.currentThread().getName() + ":3");
        StringBuilder builder = new StringBuilder();
        uiHandler.post(() -> {
            WebView webView = new WebView(this.context);
            webView.getSettings().setJavaScriptEnabled(true);
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {
                    webView.evaluateJavascript(
                            "(function() { return document.body.innerText; })();",
                            html -> {
                                html = html.replaceAll("^\"|\"$", "") // 去除首尾双引号
                                        .replace("\\\\n", "\n")    // 恢复换行符
                                        .replace("\\\\t", "\t")    // 恢复制表符
                                        .replace("\\\"", "\"")
                                        .replace("\\'", "'");
                                System.out.println(Thread.currentThread().getName() + ":4");
                                JSONObject json = JSON.parseObject(html);
                                builder.append(json);
                            }
                    );
                }
            });
            webView.loadUrl("https://push2his.eastmoney.com/api/qt/stock/kline/get?secid=0.159941&fields1=f1,f2,f3,f4,f5&fields2=f51,f52,f53,f54,f55,f56,f57&klt=101&fqt=1&beg=20250807&end=20250807");
        });
        return builder.toString();
    }

}
