package org.nature.common.util;

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
     * 消息发送管理器
     */
    private static NotificationManager manager;
    /**
     * ID计数器
     */
    private static AtomicInteger counter;

    /**
     * 初始化
     */
    public static void init() {
        NotifyUtil.manager = (NotificationManager) CtxUtil.get().getSystemService(NOTIFICATION_SERVICE);
        if (manager == null) {
            throw new RuntimeException("there is no notification manager");
        }
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
        manager.createNotificationChannel(channel);
        NotifyUtil.counter = new AtomicInteger(1);
    }

    /**
     * 通知
     * @param title   标题
     * @param content 内容
     */
    public static void notify(String title, String content) {
        manager.notify(NOTIFICATION_ID, NotifyUtil.notification(title, content));
    }

    /**
     * 通知
     * @param title   标题
     * @param content 内容
     */
    public static void notifyOne(String title, String content) {
        manager.notify(counter.incrementAndGet(), NotifyUtil.notification(title, content));
    }

    /**
     * 语音提示
     * @param text 文本
     */
    public static void speak(String text) {
        new TTS(CtxUtil.get(), text);
    }

    /**
     * 创建通知对象
     * @return Notification
     */
    public static Notification notification(String title, String content) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(CtxUtil.get(), CHANNEL_ID);
        builder.setSmallIcon(R.drawable.app_icon).setContentTitle(title).setContentText(content);
        return builder.build();
    }

    private static class TTS {

        /**
         * tts实例
         */
        private TextToSpeech tts;

        public TTS(Context context, String text) {
            // 创建实例
            tts = new TextToSpeech(context, status -> {
                if (status == TextToSpeech.SUCCESS) {
                    // 实例初始化成功，设置语言类型
                    int result = tts.setLanguage(Locale.CHINESE);
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        throw new RuntimeException("tts set language failed");
                    }
                    // 语言设置成功后输入语音
                    tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
                } else {
                    throw new RuntimeException("tts init failed:" + status);
                }
            });
        }

    }
}
