package org.nature.common.util;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.widget.Toast;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 弹窗工具
 * @author nature
 * @version 1.0.0
 * @since 2020/6/6 11:00
 */
public class PopUtil {

    /**
     * 提示消息
     * @param context 上下文
     * @param message 消息
     */
    public static void alert(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    /**
     * 确认框
     * @param context  context
     * @param title    标题
     * @param message  提示消息
     * @param runnable 执行逻辑
     */
    public static void confirm(Context context, String title, String message, Runnable runnable) {
        PopUtil.buildAlertDialog(context, title, builder -> builder.setMessage(message), runnable);
    }

    /**
     * 确认框
     * @param context  context
     * @param title    标题
     * @param view     自定义的页面
     * @param runnable 执行逻辑
     */
    public static void confirm(Context context, String title, View view, Runnable runnable) {
        PopUtil.buildAlertDialog(context, title, builder -> builder.setView(view), runnable);
    }

    /**
     * 确认框
     * @param context  context
     * @param title    标题
     * @param message  提示消息
     * @param supplier 执行逻辑
     */
    public static void confirmAsync(Context context, String title, String message, Supplier<String> supplier) {
        PopUtil.buildAsyncDialog(context, title, builder -> builder.setMessage(message), supplier);
    }

    /**
     * 确认框
     * @param context  context
     * @param title    标题
     * @param view     自定义的页面
     * @param supplier 执行逻辑
     */
    public static void confirmAsync(Context context, String title, View view, Supplier<String> supplier) {
        PopUtil.buildAsyncDialog(context, title, builder -> builder.setView(view), supplier);
    }

    /**
     * 构建确认框
     * @param context  context
     * @param title    标题
     * @param consumer 框处理
     * @param runnable 执行逻辑
     */
    private static void buildAlertDialog(Context context, String title, Consumer<AlertDialog.Builder> consumer,
                                         Runnable runnable) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        consumer.accept(builder);
        builder.setPositiveButton("确定", (di, i) -> {
            runnable.run();
            di.dismiss();
        });
        builder.setNegativeButton("取消", (di, i) -> di.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * 构建确认框
     * @param context  context
     * @param title    标题
     * @param consumer 框处理
     * @param supplier 执行逻辑
     */
    private static void buildAsyncDialog(Context context, String title, Consumer<AlertDialog.Builder> consumer,
                                         Supplier<String> supplier) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        consumer.accept(builder);
        builder.setPositiveButton("确定", null);
        builder.setNegativeButton("取消", (di, i) -> {
        });
        AlertDialog dialog = builder.create();
        dialog.show();
        ClickUtil.onAsyncClick(dialog.getButton(AlertDialog.BUTTON_POSITIVE), supplier, dialog::cancel);
    }
}
