package org.nature.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.speech.tts.TextToSpeech
import org.nature.R
import java.util.Locale
import java.util.concurrent.atomic.AtomicInteger

/**
 * 提示消息工具
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/5
 */
object NotifyUtil {

    /**
     * 启动notification的id，两次启动应是同一个id
     */
    const val NOTIFICATION_ID = 1
    
    /**
     * 消息发送管理器
     */
    private var manager: NotificationManager? = null
    
    /**
     * ID计数器
     */
    private var counter: AtomicInteger? = null

    /**
     * 初始化
     */
    @JvmStatic
    fun init() {
        manager = CtxUtil.get().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (manager == null) {
            throw RuntimeException("there is no notification manager")
        }
        val channel = NotificationChannel(
            org.nature.config.Config.CHANNEL_ID,
            org.nature.config.Config.CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        manager!!.createNotificationChannel(channel)
        counter = AtomicInteger(1)
    }

    /**
     * 通知
     * @param title   标题
     * @param content 内容
     */
    @JvmStatic
    fun notify(title: String, content: String) {
        manager!!.notify(NOTIFICATION_ID, notification(title, content))
    }

    /**
     * 通知
     * @param title   标题
     * @param content 内容
     */
    @JvmStatic
    fun notifyOne(title: String, content: String) {
        manager!!.notify(counter!!.incrementAndGet(), notification(title, content))
    }

    /**
     * 语音提示
     * @param text 文本
     */
    @JvmStatic
    fun speak(text: String) {
        TTS(CtxUtil.get(), text)
    }

    /**
     * 创建通知对象
     * @return Notification
     */
    @JvmStatic
    fun notification(title: String, content: String): Notification {
        val builder = Notification.Builder(CtxUtil.get(), org.nature.config.Config.CHANNEL_ID)
        builder.setSmallIcon(R.drawable.app_icon).setContentTitle(title).setContentText(content)
        return builder.build()
    }

    private class TTS(context: Context, text: String) {

        /**
         * tts实例
         */
        private var tts: TextToSpeech? = null

        init {
            // 创建实例
            tts = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    // 实例初始化成功，设置语言类型
                    val result = tts!!.setLanguage(Locale.CHINESE)
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        throw RuntimeException("tts set language failed")
                    }
                    // 语言设置成功后输入语音
                    tts!!.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
                } else {
                    throw RuntimeException("tts init failed:$status")
                }
            }
        }
    }
}
