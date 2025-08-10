package org.nature.biz.etf.job;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.webkit.WebView;
import android.webkit.WebViewClient;
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

import java.util.*;
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

    public static final String URL = "https://push2his.eastmoney.com/api/qt/stock/kline/get?secid=%s.%s&fields1=f1,f2,f3,f4,f5&fields2=f51,f52,f53,f54,f55,f56,f57&klt=101&fqt=1&beg=%s&end=%s";

    private static boolean running;

    private final Handler handler = new Handler(Looper.getMainLooper());

    private final Map<String, WebView> viewMap = new HashMap<>();

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

    @SuppressLint("SetJavaScriptEnabled")
    private void handle(List<String> conditions) {
        Set<String> keys = conditions.stream().map(i -> {
            String[] arr = i.split(":");
            return String.join(":", arr[1], arr[0]);
        }).collect(Collectors.toSet());
        // 清理无需处理的webview实例
        viewMap.keySet().removeIf(k -> !keys.contains(k));
        for (String i : conditions) {
            String[] arr = i.split(":");
            handler.post(() -> {
                viewMap.computeIfAbsent(String.join(":", arr[1], arr[0]),
                        k -> this.buildWebView()).loadUrl(String.format(URL, arr[1], arr[0], arr[2], arr[3]));
            });
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private WebView buildWebView() {
        WebView webView = new WebView(NotifyUtil.getContext());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(this.buildClient());
        return webView;
    }

    private WebViewClient buildClient() {
        return new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                view.evaluateJavascript(
                        "(function() { return document.body.innerText; })();",
                        html -> {
                            html = html.replaceAll("^\"|\"$", "") // 去除首尾双引号
                                    .replace("\\\\n", "\n")    // 恢复换行符
                                    .replace("\\\\t", "\t")    // 恢复制表符
                                    .replace("\\\"", "\"")
                                    .replace("\\'", "'");
                            try {
                                SyncKlineJob.this.handleHtml(html);
                            } catch (Exception e) {
                                NotifyUtil.notifyOne("数据解析异常", e.getMessage());
                            }
                        }
                );
            }
        };
    }

    private void handleHtml(String html) {
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
