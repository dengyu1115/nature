package org.nature.func.job.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import androidx.annotation.Nullable;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.nature.common.constant.Const;
import org.nature.common.util.NotifyUtil;

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
    private static final AtomicInteger counter = new AtomicInteger();
    /**
     * 定时器
     */
    private static volatile ScheduledExecutorService service;
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
        this.startForeground(NotifyUtil.NOTIFICATION_ID, NotifyUtil.notification("NATURE正在运行", "服务前台启动..."));
        this.acquireWakeLock();
        synchronized (JobService.class) {  // 保证逻辑只启动一次
            if (service != null) {
                return;
            }
        }
        this.getService().scheduleAtFixedRate(this.task(), this.calculateDelay(), PERIOD, TimeUnit.MILLISECONDS);
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
        this.stopForeground(true);
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
     * @return TimerTask
     */
    private Runnable task() {
        return () -> {
            try {
                String date = DateFormatUtils.format(new Date(), Const.FORMAT_DATETIME);
                String s = String.format("%s:%s", date, counter.incrementAndGet());
                NotifyUtil.notify("NATURE正在运行", s);
//                service.execute(taskManager::execute);
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
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
    }

}
