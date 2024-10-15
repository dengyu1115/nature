package org.nature.common.util;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.speech.tts.TextToSpeech;
import androidx.core.app.NotificationCompat;
import org.nature.R;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * 提示消息工具
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/5
 */
public class NotifyUtil {

    /**
     * 启动notification的id，两次启动应是同一个id
     */
    public final static int NOTIFICATION_ID = 1;
    /**
     * 服务通道id
     */
    private final static String CHANNEL_ID = "NATURE_CHANNEL";
    /**
     * 服务通道name
     */
    private final static String CHANNEL_NAME = "NATURE服务通道";

    /**
     * TTS语音处理
     */
    private static TextToSpeech tts;

    @SuppressLint("StaticFieldLeak")
    private static Ctx ctx;

    /**
     * 初始化
     * @param context 安卓上下文
     */
    public static void init(Context context) {
        NotificationManager manager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        if (manager == null) {
            throw new RuntimeException("there is no notification manager");
        }
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
        manager.createNotificationChannel(channel);
        NotifyUtil.tts = new TextToSpeech(context, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = NotifyUtil.tts.setLanguage(Locale.CHINESE);
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    throw new RuntimeException("set language failed");
                }
            }
        }, "com.google.android.tts");
        AtomicInteger counter = new AtomicInteger(1);
        NotifyUtil.ctx = new Ctx(context, manager, NotifyUtil.tts, counter);
    }

    /**
     * 通知
     * @param title   标题
     * @param content 内容
     */
    public static void notify(String title, String content) {
        ctx.manager.notify(NOTIFICATION_ID, NotifyUtil.notification(title, content));
    }

    /**
     * 通知
     * @param title   标题
     * @param content 内容
     */
    public static void notifyOne(String title, String content) {
        ctx.manager.notify(ctx.counter.incrementAndGet(), NotifyUtil.notification(title, content));
    }

    /**
     * 语音提示
     * @param text 文本
     */
    public static void speak(String text) {
        ctx.tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
    }

    /**
     * 创建通知对象
     * @return Notification
     */
    public static Notification notification(String title, String content) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx.context, CHANNEL_ID);
        builder.setSmallIcon(R.drawable.app_icon).setContentTitle(title).setContentText(content);
        return builder.build();
    }

    private static class Ctx {
        /**
         * 安卓上下文对象
         */
        private final Context context;
        /**
         * 消息发送管理器
         */
        private final NotificationManager manager;
        /**
         * TTS语音处理
         */
        private final TextToSpeech tts;
        /**
         * ID计数器
         */
        private final AtomicInteger counter;

        public Ctx(Context context, NotificationManager manager, TextToSpeech tts, AtomicInteger counter) {
            this.context = context;
            this.manager = manager;
            this.tts = tts;
            this.counter = counter;
        }

    }
}
