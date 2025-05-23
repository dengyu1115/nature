package org.nature.func.job.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import androidx.annotation.Nullable;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.nature.common.constant.Const;
import org.nature.common.ioc.holder.InstanceHolder;
import org.nature.common.util.NotifyUtil;
import org.nature.func.job.manager.ExecManager;

import java.util.Date;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 定时任务服务（运行于前台，可以在锁屏状态执行，定时器逻辑）
 * @author nature
 * @version 1.0.0
 * @since 2020/1/4 12:20
 */
public class JobService extends Service {

    /**
     * 执行间隔
     */
    private static final int PERIOD = 1000;
    /**
     * 定时器
     */
    private static volatile ScheduledExecutorService service;
    /**
     * 计数器
     */
    private final AtomicInteger counter = new AtomicInteger();
    private final long startTime = System.currentTimeMillis();
    /**
     * 唤醒锁
     */
    private PowerManager.WakeLock wl;

    /**
     * 创建服务
     */
    @Override
    public void onCreate() {
        super.onCreate();
        NotifyUtil.notify("NATURE正在运行", "服务初始化...");
        // 设置为前台进程，降低oom_adj，提高进程优先级，提高存活机率
        Notification notification = NotifyUtil.notification("NATURE正在运行", "服务前台启动...");
        this.startForeground(NotifyUtil.NOTIFICATION_ID, notification);
        this.acquireWakeLock();
        synchronized (JobService.class) {  // 保证逻辑只启动一次
            if (service != null) {
                return;
            }
        }
        this.getService().scheduleAtFixedRate(this::task, this.calculateDelay(), PERIOD, TimeUnit.MILLISECONDS);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * 服务启动后执行逻辑
     * @param intent  intent
     * @param flags   flags
     * @param startId startId
     * @return int
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 计算延迟执行时间
     * @return int
     */
    private long calculateDelay() {
        return 0;
    }

    /**
     * 服务终止调用
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        this.stopForeground(STOP_FOREGROUND_REMOVE);
        this.releaseWakeLock();
        service.shutdown();
        service = null;
    }

    /**
     * 获取timer
     * @return timer
     */
    private ScheduledExecutorService getService() {
        if (service == null) {
            synchronized (JobService.class) {
                if (service == null) {
                    service = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors());
                }
            }
        }
        return service;
    }

    /**
     * 定时任务执行的逻辑
     */
    private void task() {
        try {
            Date now = new Date();
            String date = DateFormatUtils.format(now, Const.FORMAT_DATETIME);
            long times = (System.currentTimeMillis() - startTime) / 1000;
            int count = counter.getAndIncrement();
            String s = String.format("时间:%s 次数:%s 漏执行:%s", date, count, times - count);
            NotifyUtil.notify("NATURE正在运行", s);
            ExecManager execManager = InstanceHolder.get(ExecManager.class);
            if (execManager != null) {
                execManager.exec(now);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取唤醒锁
     */
    @SuppressLint({"WakelockTimeout", "InvalidWakeLockTag"})
    private void acquireWakeLock() {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        assert pm != null;
        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, JobService.class.getName());
        wl.acquire();   // 获取唤醒锁
    }

    /**
     * 释放唤醒锁
     */
    private void releaseWakeLock() {
        wl.release();
        wl = null;
    }

}
