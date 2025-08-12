package org.nature.func.test.page;

import android.annotation.SuppressLint;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.LinearLayout;
import org.nature.common.ioc.annotation.PageView;
import org.nature.common.page.Page;
import org.nature.common.util.ClickUtil;
import org.nature.common.util.NotifyUtil;
import org.nature.common.view.Button;
import org.nature.common.view.Input;
import org.nature.common.view.ViewTemplate;

/**
 * 测试功能
 * @author Nature
 * @version 1.0.0
 * @since 2024/9/14
 */
@PageView(name = "测试功能", group = "基础", col = 2, row = 2)
public class TestPage extends Page {

    private static final String URL = "'https://push2his.eastmoney.com/api/qt/stock/kline/get?secid=%s.%s&fields1=f1,f2,f3,f4,f5&fields2=f51,f52,f53,f54,f55,f56,f57&klt=101&fqt=1&beg=20250807&end=20250811'";
    public static final String SCRIPT = "(function() { " +
            "const url = " + URL + ";\n" +
            "        fetch(url).then(response => {\n" +
            "            if (!response.ok) {\n" +
            "              throw new Error(`HTTP error! Status: ${response.status}`);\n" +
            "            }\n" +
            "            return response.json();\n" +
            "          }).then(data => {\n" +
            "            console.log('调用成功，返回结果：',JSON.stringify(data));\n" +
            "            native.callback(JSON.stringify(data));\n" +
            "          }).catch(error => {\n" +
            "            console.error('调用失败：', error.message);\n" +
            "          });" +
            " })();";
    private Button ttsBtn;

    private Button webBtn;

    @Override
    protected void makeStructure() {
        page.setOrientation(LinearLayout.VERTICAL);
        page.addView(ttsBtn = template.button("tts", 10, 7));
        page.addView(webBtn = template.button("web", 10, 7));
    }


    @SuppressLint("SetJavaScriptEnabled")
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
            WebView webView = new WebView(this.context);
            webView.getSettings().setJavaScriptEnabled(true);
            webView.addJavascriptInterface(new Callback(template), "native");
            webView.evaluateJavascript(String.format(SCRIPT, "0", "159941"), null);
            webView.evaluateJavascript(String.format(SCRIPT, "0", "000001"), null);
        });
    }

    public static class Callback {

        private final ViewTemplate template;

        public Callback(ViewTemplate template) {
            this.template = template;
        }

        @JavascriptInterface
        public void callback(String html) {
            this.template.alert("html: " + html);
        }
    }

}
