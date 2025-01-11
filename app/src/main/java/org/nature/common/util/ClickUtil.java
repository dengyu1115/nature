package org.nature.common.util;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.Toast;
import org.nature.common.exception.Warn;

import java.util.function.Supplier;

/**
 * 点击工具
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/5
 */
public class ClickUtil {

    /**
     * 给view设置点击事件，并处理点击事件（主线程执行）
     * @param view     view
     * @param runnable 执行逻辑
     */
    public static void onClick(View view, Runnable runnable) {
        view.setOnClickListener(v -> ClickUtil.exec(v, runnable, () -> {
        }));
    }

    /**
     * 给view设置点击事件，并处理点击事件（主线程执行）
     * @param view     view
     * @param runnable 执行逻辑
     * @param handled  执行完毕后下一步执行
     */
    public static void onClick(View view, Runnable runnable, Runnable handled) {
        view.setOnClickListener(v -> ClickUtil.exec(v, runnable, handled));
    }

    /**
     * 给view设置点击事件，并处理点击事件（主线程执行）
     * @param view     view
     * @param supplier 执行逻辑
     */
    public static void onAsyncClick(View view, Supplier<String> supplier) {
        view.setOnClickListener(v -> ClickUtil.asyncExec(v, supplier, () -> {
        }));
    }

    /**
     * 给view设置点击事件，并处理点击事件（主线程执行）
     * @param view     view
     * @param supplier 执行逻辑
     * @param handled  执行完毕后下一步执行
     */
    public static void onAsyncClick(View view, Supplier<String> supplier, Runnable handled) {
        view.setOnClickListener(v -> ClickUtil.asyncExec(v, supplier, handled));
    }

    /**
     * 点击处理（主线程执行）
     * @param view     view
     * @param runnable 执行逻辑
     * @param handled  执行完毕后下一步执行
     */
    private static void exec(View view, Runnable runnable, Runnable handled) {
        // 设置view不可点击
        try {
            view.setClickable(false);
            runnable.run();
            handled.run();
        } catch (Warn e) {
            // 弹出提示
            Toast.makeText(view.getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            // 弹出提示
            Toast.makeText(view.getContext(), "系统错误", Toast.LENGTH_LONG).show();
        } finally {
            // 恢复view可点击
            view.setClickable(true);
        }
    }

    /**
     * 点击处理（异步执行）
     * @param view     view
     * @param supplier 执行逻辑
     * @param handled  执行完毕后下一步执行
     */
    private static void asyncExec(View view, Supplier<String> supplier, Runnable handled) {
        // 异步事件处理器
        Handler handler = new Handler(Looper.myLooper(), msg -> {
            Bundle data = msg.getData();
            String message = data.getString("message");
            if (message != null) {
                Toast.makeText(view.getContext(), message, Toast.LENGTH_LONG).show();
            }
            if (data.getBoolean("handled") && handled != null) {
                handled.run();
            }
            view.setClickable(data.getBoolean("clickable"));
            return false;
        });
        new Thread(() -> {
            try {
                handler.sendMessage(message(false, false, null));
                String s = supplier.get();
                // 有处理结果信息则提示
                handler.sendMessage(message(false, false, s));
                // 通知执行后续步骤
                handler.sendMessage(message(false, true, null));
            } catch (Warn e) {
                // 进行消息提示
                handler.sendMessage(message(false, false, e.getMessage()));
            } catch (Exception e) {
                // 进行消息提示
                handler.sendMessage(message(false, false, "系统错误:" + e));
            } finally {
                handler.sendMessage(message(true, false, null));
            }
        }).start();
    }


    /**
     * 消息构建
     * @param clickable 是否可点击
     * @param handled   执行已处理后置逻辑
     * @param message   消息内容
     * @return Message
     */
    private static Message message(boolean clickable, boolean handled, String message) {
        Message msg = new Message();
        msg.getData().putBoolean("clickable", clickable);
        msg.getData().putBoolean("handled", handled);
        msg.getData().putString("message", message);
        return msg;
    }
}
