package org.nature.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import org.nature.util.ExecUtil;
import org.nature.util.JobUtil;
import org.nature.util.NotifyUtil;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
    private static final int PERIOD = 1000, DELAY = 0;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss");

    /**
     * 定时器
     */
    private static volatile ScheduledExecutorService service;
    /**
     * 计数器
     */
    private final AtomicInteger counter = new AtomicInteger();

    private final Map<String, String> lockMap = new ConcurrentHashMap<>();
    private final long startTime = System.currentTimeMillis();
    /**
     * 唤醒锁
     */
    private PowerManager.WakeLock wl;

    private PyObject date_module;

    /**
     * 创建服务
     */
    @SuppressLint("DiscouragedApi")
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
            date_module = Python.getInstance().getModule("datetime").get("datetime");
        }
        this.getService().scheduleAtFixedRate(this::task, DELAY, PERIOD, TimeUnit.MILLISECONDS);
    }

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
            LocalDateTime now = LocalDateTime.now();
            String date = now.format(DATE_FORMATTER);
            long currTime = now.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            long times = (currTime - startTime) / 1000;
            int count = counter.getAndIncrement();
            String s = String.format("时间:%s 成功:%s 失败:%s", date, count, times - count);
            NotifyUtil.notify("NATURE正在运行", s);
            this.exec(currTime);
        } catch (Exception e) {
            NotifyUtil.notifyOne("NATURE异常", e.getMessage());
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


    private void exec(Long currTime) {
        PyObject date = date_module.callAttr("fromtimestamp", currTime / 1000.0d);
        Map<String, PyObject> jobMap = JobUtil.jobs();
        jobMap.forEach((name, func) -> {
            String val = lockMap.putIfAbsent(name, name);
            if (val != null) {
                return;
            }
            ExecUtil.submit(() -> {
                try {
                    func.call(date);
                } catch (Exception e) {
                    NotifyUtil.notifyOne("任务执行失败：" + name, e.getMessage());
                } finally {
                    lockMap.remove(name);
                }
            });
        });
    }

}
