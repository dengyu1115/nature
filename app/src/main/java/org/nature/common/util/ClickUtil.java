package org.nature.common.util;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import org.apache.commons.lang3.StringUtils;
import org.nature.common.exception.Warn;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * 点击工具
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/5
 */
public class ClickUtil {
    /**
     * view-时间 map
     */
    private static final Map<View, Long> VIEW_TIME_MAP = new ConcurrentHashMap<>();
    /**
     * 上次点击时间记录
     */
    private static long millis;

    /**
     * 给view设置点击事件，并处理点击事件（主线程执行）
     * @param view     view
     * @param runnable 执行逻辑
     */
    public static void onClick(View view, Runnable runnable) {
        view.setOnClickListener(v -> ClickUtil.click(v, runnable));
    }

    /**
     * 给view设置点击事件，并处理点击事件（主线程执行）
     * @param view     view
     * @param runnable 执行逻辑
     * @param handled  执行完毕后下一步执行
     */
    public static void onClick(View view, Runnable runnable, Runnable handled) {
        view.setOnClickListener(v -> ClickUtil.click(v, runnable, handled));
    }

    /**
     * 给view设置点击事件，并处理点击事件（主线程执行）
     * @param view     view
     * @param supplier 执行逻辑
     * @param handled  执行完毕后下一步执行
     */
    public static void onAsyncClick(View view, Supplier<String> supplier, Runnable handled) {
        view.setOnClickListener(v -> ClickUtil.asyncClick(v, supplier, handled));
    }

    /**
     * 给view设置点击事件，并处理点击事件（主线程执行）
     * @param view     view
     * @param supplier 执行逻辑
     */
    public static void onAsyncClick(View view, Supplier<String> supplier) {
        view.setOnClickListener(v -> ClickUtil.asyncClick(v, supplier));
    }

    /**
     * 给view设置点击弹确认框事件
     * @param view     view
     * @param title    标题
     * @param content  内容
     * @param supplier 处理逻辑
     */
    public static void onPopConfirm(View view, String title, String content, Supplier<String> supplier) {
        ClickUtil.onClick(view, () -> PopUtil.confirmAsync(view.getContext(), title, content, supplier));
    }

    /**
     * 点击处理（主线程执行）
     * @param view     view
     * @param runnable 执行逻辑
     */
    public static void click(View view, Runnable runnable) {
        ClickUtil.click(view, runnable, () -> {
        });
    }

    /**
     * 点击处理（主线程执行）
     * @param view     view
     * @param runnable 执行逻辑
     * @param handled  执行完毕后下一步执行
     */
    public static void click(View view, Runnable runnable, Runnable handled) {
        // 设置view不可点击
        try {
            view.setClickable(false);
            long currMillis = System.currentTimeMillis();
            if (currMillis - millis < 1000) {
                throw new Warn("点击过于频繁");
            }
            runnable.run();
            handled.run();
        } catch (Warn e) {
            // 弹出提示
            PopUtil.alert(view.getContext(), e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            // 弹出提示
            PopUtil.alert(view.getContext(), "系统错误");
        } finally {
            // 恢复view可点击
            view.setClickable(true);
            // 上次点击时间重置
            millis = System.currentTimeMillis();
        }
    }

    /**
     * 点击处理（异步执行）
     * @param view     view
     * @param supplier 执行逻辑
     */
    public static void asyncClick(View view, Supplier<String> supplier) {
        ClickUtil.asyncClick(view, supplier, () -> {
        });
    }

    /**
     * 点击处理（异步执行）
     * @param view     view
     * @param runnable 执行逻辑
     * @param handled  执行完毕后下一步执行
     */
    public static void asyncClick(View view, Runnable runnable, Runnable handled) {
        ClickUtil.asyncClick(view, () -> {
            runnable.run();
            return null;
        }, handled);
    }

    /**
     * 点击处理（异步执行）
     * @param view     view
     * @param supplier 执行逻辑
     * @param handled  执行完毕后下一步执行
     */
    private static void asyncClick(View view, Supplier<String> supplier, Runnable handled) {
        // 异步事件处理器
        Handler handler = new Handler(msg -> {
            String message = msg.getData().getString("data");
            if (message != null) {
                PopUtil.alert(view.getContext(), message);
            } else {
                handled.run();
            }
            return false;
        });
        new Thread(() -> {
            try {
                view.setClickable(false);
                Long millis = VIEW_TIME_MAP.get(view);
                if (millis != null) {
                    throw new Warn("重复点击");
                }
                VIEW_TIME_MAP.put(view, System.currentTimeMillis());
                String s = supplier.get();
                if (s != null) {
                    // 有处理结果信息则提示
                    handler.sendMessage(message(s));
                }
                // 通知执行后续步骤
                handler.sendMessage(new Message());
            } catch (Warn e) {
                // 进行消息提示
                handler.sendMessage(message(e.getMessage()));
            } catch (Exception e) {
                // 进行消息提示
                handler.sendMessage(message("系统错误:" + e));
            } finally {
                view.setClickable(true);
                VIEW_TIME_MAP.remove(view);
            }
        }).start();
    }

    /**
     * 消息构建
     * @param content 消息内容
     * @return Message
     */
    private static Message message(String content) {
        Message msg = new Message();
        Bundle data = new Bundle();
        data.putString("data", StringUtils.isBlank(content) ? "未知错误" : content);
        msg.setData(data);
        return msg;
    }
}
