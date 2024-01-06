package org.nature.common.util;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.widget.Toast;
import org.nature.common.exception.Warn;

import java.util.function.Consumer;

/**
 * 弹窗工具
 * @author nature
 * @version 1.0.0
 * @since 2020/6/6 11:00
 */
public class PopUtil {

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
        buildAlertDialog(context, title, builder -> builder.setMessage(message), runnable);
    }

    /**
     * 确认框
     * @param context  context
     * @param title    标题
     * @param view     自定义的页面
     * @param runnable 执行逻辑
     */
    public static void confirm(Context context, String title, View view, Runnable runnable) {
        buildAlertDialog(context, title, builder -> builder.setView(view), runnable);
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
        builder.setPositiveButton("确定", null);
        builder.setNegativeButton("取消", (di, i) -> {
        });
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            try {
                runnable.run();
                dialog.dismiss();
            } catch (Warn e) {
                alert(context, e.getMessage());
            } catch (Exception e) {
                alert(context, "系统错误");
            }
        });
    }
}
