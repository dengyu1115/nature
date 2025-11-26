package org.nature.common.util;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.Toast;
import org.nature.common.exception.Warn;

import java.util.function.Consumer;
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
        view.setOnClickListener(v -> ClickUtil.exec(v, () -> {
            runnable.run();
            return null;
        }, null));
    }

    /**
     * 给view设置点击事件，并处理点击事件（主线程执行）
     * @param view     view
     * @param runnable 执行逻辑
     * @param handled  执行完毕后下一步执行
     */
    public static void onClick(View view, Runnable runnable, Runnable handled) {
        view.setOnClickListener(v -> ClickUtil.exec(v, () -> {
            runnable.run();
            return null;
        }, handled));
    }

    /**
     * 给view设置点击事件，并处理点击事件（主线程执行）
     * @param view     view
     * @param supplier 执行逻辑
     */
    public static void onAsyncClick(View view, Supplier<String> supplier) {
        view.setOnClickListener(v -> ClickUtil.asyncExec(v, supplier,
                i -> Toast.makeText(view.getContext(), i, Toast.LENGTH_LONG).show()));
    }

    /**
     * 给view设置点击事件，并处理点击事件（主线程执行）
     * @param view     view
     * @param supplier 执行逻辑
     * @param handled  执行完毕后下一步执行
     */
    public static void onAsyncClick(View view, Supplier<String> supplier, Runnable handled) {
        view.setOnClickListener(v -> ClickUtil.asyncExec(v, supplier, i -> {
            Toast.makeText(view.getContext(), i, Toast.LENGTH_LONG).show();
            handled.run();
        }));
    }


    /**
     * 执行
     * @param view     view
     * @param supplier supplier
     * @param handled  handled
     */
    public static void exec(View view, Supplier<String> supplier, Runnable handled) {
        try {
            view.setClickable(false);
            String message = supplier.get();
            // 有处理结果信息则提示
            if (message != null) {
                Toast.makeText(view.getContext(), message, Toast.LENGTH_LONG).show();
            }
            // 通知执行后续步骤
            if (handled != null) {
                handled.run();
            }
        } catch (Warn e) {
            // 进行消息提示
            Toast.makeText(view.getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            // 进行消息提示
            Toast.makeText(view.getContext(), "系统错误:" + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            view.setClickable(true);
        }
    }

    /**
     * 点击处理（异步执行）
     * @param view     view
     * @param supplier 执行逻辑
     * @param handled  执行完毕后下一步执行
     */
    @SuppressWarnings("unchecked")
    public static <T> void asyncExec(View view, Supplier<T> supplier, Consumer<T> handled) {
        // 异步事件处理器
        Handler handler = new Handler(Looper.myLooper(), msg -> {
            if (msg.what == 0) {
                handled.accept((T) msg.obj);
            } else if (msg.what == 1) {
                Toast.makeText(view.getContext(), (String) msg.obj, Toast.LENGTH_LONG).show();
            } else {
                view.setClickable(true);
            }
            return false;
        });
        view.setClickable(false);
        new Thread(() -> {
            Looper.prepare();
            try {
                T t = supplier.get();
                handler.sendMessage(build(0, t));
            } catch (Warn e) {
                handler.sendMessage(build(1, e.getMessage()));
            } catch (Exception e) {
                handler.sendMessage(build(1, "系统错误:" + e.getMessage()));
            } finally {
                handler.sendMessage(build(2, null));
            }
        }).start();
    }

    /**
     * 消息构建
     * @param what 类型
     * @param obj  数据
     * @return Message
     */
    private static Message build(int what, Object obj) {
        Message msg = new Message();
        msg.what = what;
        msg.obj = obj;
        return msg;
    }

}
