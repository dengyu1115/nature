package org.nature.biz.etf.job;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.nature.biz.common.mapper.KlineMapper;
import org.nature.biz.common.model.Kline;
import org.nature.biz.etf.manager.RuleManager;
import org.nature.common.exception.Warn;
import org.nature.common.ioc.annotation.Injection;
import org.nature.common.ioc.annotation.JobExec;
import org.nature.common.util.NotifyUtil;
import org.nature.common.util.TextUtil;
import org.nature.func.job.protocol.Job;
import org.nature.func.workday.manager.WorkdayManager;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static org.nature.common.constant.Const.EMPTY;
import static org.nature.common.constant.Const.HYPHEN;

/**
 * 同步K线数据（涉及频繁的网络请求，借用webview模拟浏览器请求，防止被服务端封禁）
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/7
 */
@JobExec(code = "sync_kline_job", name = "同步K线")
public class SyncKlineJob implements Job {

    private static final String URL = "https://push2his.eastmoney.com/api/qt/stock/kline/get?secid=%s.%s&fields1=f1,f2,f3,f4,f5&fields2=f51,f52,f53,f54,f55,f56,f57&klt=101&fqt=1&beg=%s&end=%s";
    private static final String SCRIPT = "(function() { " +
            "const url = '" + URL + "';\n" +
            "        // 使用fetch API调用URL\n" +
            "        fetch(url).then(response => {\n" +
            "            if (!response.ok) {\n" +
            "              throw new Error(`HTTP error! Status: ${response.status}`);\n" +
            "            }\n" +
            "            return response.json();\n" +
            "          }).then(data => {\n" +
            "            native.callSuccess(JSON.stringify(data));\n" +
            "          }).catch(error => {\n" +
            "            native.callFailure('调用失败：' + error.message);\n" +
            "          });" +
            " })();";
    private static boolean running;

    private final Handler handler = new Handler(Looper.getMainLooper());

    private WebView webView;

    @Injection
    private RuleManager ruleManager;
    @Injection
    private KlineMapper klineMapper;
    @Injection
    private WorkdayManager workdayManager;

    @Override
    public void exec(Date date) {
        this.doExec(() -> {
            // 执行延时1秒以上废弃任务
            if (System.currentTimeMillis() - date.getTime() > 1000) {
                return;
            }
            // 非工作日不处理
            if (!workdayManager.isWorkday()) {
                return;
            }
            this.exec();
        });
    }

    /**
     * etf最新操作数据处理
     */
    private void exec() {
        // 查询最新处理条件进行处理
        this.handle(ruleManager.latestConditions());
    }

    /**
     * 串行执行
     * @param runnable 执行逻辑
     */
    private void doExec(Runnable runnable) {
        synchronized (this) {
            // 已经有执行中任务，不再执行
            if (running) {
                return;
            }
            // 标记正在执行
            running = true;
        }
        // 执行任务
        try {
            runnable.run();
        } finally {
            // 执行完毕，标记为未执行
            running = false;
        }
    }

    /**
     * 处理数据
     * @param conditions 条件列表
     */
    @SuppressLint("SetJavaScriptEnabled")
    private void handle(List<String> conditions) {
        handler.post(() -> {
            this.buildWebView();
            for (String i : conditions) {
                String[] arr = i.split(":");
                webView.evaluateJavascript(String.format(SCRIPT, arr[1], arr[0], arr[2], arr[3]), null);
            }
        });
    }

    /**
     * 构建webview实例
     */
    @SuppressLint("SetJavaScriptEnabled")
    private void buildWebView() {
        if (webView != null) {
            return;
        }
        this.webView = new WebView(NotifyUtil.getContext());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.addJavascriptInterface(this, "native");
    }

    /**
     * 处理html数据
     * @param html html数据
     */
    @JavascriptInterface
    public void callSuccess(String html) {
        try {
            // 解析返回数据，转换为json对象
            JSONObject json = JSON.parseObject(html);
            // 获取所需字段
            JSONObject data = json.getJSONObject("data");
            if (data == null) {
                throw new Warn("历史K线数据缺失");
            }
            String type = data.getString("market");
            String code = data.getString("code");
            JSONArray ks = data.getJSONArray("klines");
            if (ks == null) {
                throw new Warn("历史K线数据缺失：" + code + ":" + type);
            }
            // 转换为Kline对象
            List<Kline> list = ks.stream().map(i -> this.buildKline(code, type, (String) i))
                    .collect(Collectors.toList());
            klineMapper.batchMerge(list);
        } catch (Warn e) {
            NotifyUtil.notifyOne("同步K线数据异常", e.getMessage());
        }
    }

    /**
     * 处理异常
     * @param message 异常信息
     */
    @JavascriptInterface
    public void callFailure(String message) {
        NotifyUtil.notifyOne("同步K线数据异常", message);
    }

    /**
     * 生成K线
     * @param code 项目编号
     * @param type 项目类型
     * @param line K线String数据
     * @return Kline
     */
    private Kline buildKline(String code, String type, String line) {
        String[] s = line.split(",");
        Kline kline = new Kline();
        kline.setCode(code);
        kline.setType(type);
        kline.setDate(s[0].replace(HYPHEN, EMPTY));
        kline.setOpen(TextUtil.decimal(s[1]));
        kline.setLatest(TextUtil.decimal(s[2]));
        kline.setHigh(TextUtil.decimal(s[3]));
        kline.setLow(TextUtil.decimal(s[4]));
        kline.setShare(TextUtil.decimal(s[5]));
        kline.setAmount(TextUtil.decimal(s[6]));
        return kline;
    }

}
