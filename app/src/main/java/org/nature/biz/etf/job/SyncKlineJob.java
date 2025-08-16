package org.nature.biz.etf.job;

import android.os.Handler;
import android.os.Looper;
import android.webkit.WebView;
import org.nature.biz.common.http.KlineHttp;
import org.nature.biz.common.mapper.KlineMapper;
import org.nature.biz.common.model.Kline;
import org.nature.biz.etf.manager.RuleManager;
import org.nature.common.ioc.annotation.Injection;
import org.nature.common.ioc.annotation.JobExec;
import org.nature.func.job.protocol.Job;
import org.nature.func.workday.manager.WorkdayManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 同步K线数据（涉及频繁的网络请求，借用webview模拟浏览器请求，防止被服务端封禁）
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/7
 */
@JobExec(code = "sync_kline_job", name = "同步K线")
public class SyncKlineJob implements Job {

    private static boolean running;

    private final Handler handler = new Handler(Looper.getMainLooper());

    private WebView webView;

    @Injection
    private RuleManager ruleManager;
    @Injection
    private KlineMapper klineMapper;
    @Injection
    private KlineHttp klineHttp;
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
     * etf最新操作数据处理
     */
    private void exec() {
        // 查询最新处理条件进行处理
        List<String> conditions = ruleManager.latestConditions();
        List<Kline> list = new ArrayList<>();
        for (String i : conditions) {
            String[] arr = i.split(":");
            list.add(klineHttp.latest(arr[0], arr[1]));
        }
        klineMapper.batchMerge(list);
    }

}
