package org.nature.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.PowerManager
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import org.nature.util.ExecUtil
import org.nature.util.JobUtil
import org.nature.util.NotifyUtil
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * 定时任务服务（运行于前台，可以在锁屏状态执行，定时器逻辑）
 * @author nature
 * @version 1.0.0
 * @since 2020/1/4 12:20
 */
class JobService : Service() {

    /**
     * 执行间隔
     */
    private val PERIOD = 1000
    private val DELAY = 0

    private val DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss")

    /**
     * 定时器
     */
    private var service: ScheduledExecutorService? = null

    /**
     * 计数器
     */
    private val counter = AtomicInteger()

    private val lockMap = ConcurrentHashMap<String, String>()
    private val startTime = System.currentTimeMillis()

    /**
     * 唤醒锁
     */
    private var wl: PowerManager.WakeLock? = null

    private var date_module: PyObject? = null

    /**
     * 创建服务
     */
    @SuppressLint("DiscouragedApi")
    override fun onCreate() {
        super.onCreate()
        NotifyUtil.notify("NATURE正在运行", "服务初始化...")
        // 设置为前台进程，降低oom_adj，提高进程优先级，提高存活机率
        val notification = NotifyUtil.notification("NATURE正在运行", "服务前台启动...")
        this.startForeground(NotifyUtil.NOTIFICATION_ID, notification)
        this.acquireWakeLock()
        synchronized(JobService::class.java) {  // 保证逻辑只启动一次
            if (service != null) {
                return
            }
            date_module = Python.getInstance().getModule("datetime").get("datetime")
        }
        this.getService().scheduleAtFixedRate({ task() }, DELAY.toLong(), PERIOD.toLong(), TimeUnit.MILLISECONDS)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    /**
     * 服务启动后执行逻辑
     * @param intent  intent
     * @param flags   flags
     * @param startId startId
     * @return int
     */
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    /**
     * 服务终止调用
     */
    override fun onDestroy() {
        super.onDestroy()
        this.stopForeground(STOP_FOREGROUND_REMOVE)
        this.releaseWakeLock()
        service!!.shutdown()
        service = null
    }

    /**
     * 获取timer
     * @return timer
     */
    private fun getService(): ScheduledExecutorService {
        if (service == null) {
            synchronized(JobService::class.java) {
                if (service == null) {
                    service = ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors())
                }
            }
        }
        return service!!
    }

    /**
     * 定时任务执行的逻辑
     */
    private fun task() {
        try {
            val now = LocalDateTime.now()
            val date = now.format(DATE_FORMATTER)
            val currTime = now.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val times = (currTime - startTime) / 1000
            val count = counter.getAndIncrement()
            val s = "时间:$date 成功:$count 失败:${times - count}"
            NotifyUtil.notify("NATURE正在运行", s)
            this.exec(currTime)
        } catch (e: Exception) {
            NotifyUtil.notifyOne("NATURE异常", e.message!!)
        }
    }

    /**
     * 获取唤醒锁
     */
    @SuppressLint("WakelockTimeout", "InvalidWakeLockTag")
    private fun acquireWakeLock() {
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, JobService::class.java.name)
        wl!!.acquire()   // 获取唤醒锁
    }

    /**
     * 释放唤醒锁
     */
    private fun releaseWakeLock() {
        wl!!.release()
        wl = null
    }

    private fun exec(currTime: Long) {
        val date = date_module!!.callAttr("fromtimestamp", currTime / 1000.0)
        val jobMap = JobUtil.jobs()
        jobMap.forEach { (name, func) ->
            // 如果任务正在执行，跳过本次执行
            if (lockMap.putIfAbsent(name, name) != null) {
                return@forEach
            }
            ExecUtil.submit {
                try {
                    func.call(date)
                } catch (e: Exception) {
                    NotifyUtil.notifyOne("任务执行失败：$name", e.message!!)
                } finally {
                    lockMap.remove(name)
                }
            }
        }
    }
}
