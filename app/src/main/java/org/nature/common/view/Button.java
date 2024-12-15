package org.nature.common.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import org.nature.R;
import org.nature.common.exception.Warn;

import java.util.function.Supplier;

import static org.nature.common.constant.Const.PAD;

/**
 * 输入框
 * @author Nature
 * @version 1.0.0
 * @since 2024/12/15
 */
@SuppressLint({"ViewConstructor", "UseCompatLoadingForDrawables", "DefaultLocale", "ResourceType"})
public class Button extends LinearLayout {

    private final Context context;
    private final int width, height;
    private final TextView valueView;

    /**
     * 异步事件处理器
     */
    private final Handler handler;

    private Long millis;

    public Button(Context context, String name, int width, int height) {
        super(context);
        this.context = context;
        this.width = width;
        this.height = height;
        this.setLayoutParams(new LayoutParams(width, height));
        this.setPadding(PAD, PAD, PAD, PAD);
        this.addView(valueView = this.buildTextView(name));
        this.handler = this.buildHandler(context);
    }

    public void setText(String value) {
        this.valueView.setText(value);
    }

    public void onClick(Runnable runnable) {
        this.valueView.setOnClickListener(view -> {
            this.doClick(() -> {
                runnable.run();
                return null;
            });
        });
    }

    public void onAsyncClick(Supplier<String> supplier) {
        this.valueView.setOnClickListener(view -> new Thread(() -> this.doClick(supplier)).start());
    }

    public void setClickable(boolean clickable) {
        this.handler.sendMessage(this.message(clickable ? 0 : 1, null));
    }

    public void setBtnBackground(Drawable background) {
        this.valueView.setBackground(background);
    }

    private TextView buildTextView(String name) {
        TextView view = new TextView(context);
        view.setText(name);
        view.setLayoutParams(new LayoutParams(width - PAD * 2, height - PAD * 2));
        view.setBackground(context.getDrawable(R.drawable.bg_btn_primary));
        view.setGravity(Gravity.CENTER);
        view.setPadding(1, 1, 1, 1);
        return view;
    }

    private Handler buildHandler(Context context) {
        return new Handler(Looper.myLooper(), msg -> {
            Bundle data = msg.getData();
            String message = data.getString("data");
            if (message != null) {
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            }
            boolean clickable = data.getInt("handle") != 1;
            super.setClickable(clickable);
            this.valueView.setClickable(clickable);
            return false;
        });
    }

    /**
     * 消息构建
     * @param handle  操作
     * @param content 消息内容
     * @return Message
     */
    private Message message(int handle, String content) {
        Message msg = new Message();
        msg.getData().putInt("handle", handle);
        msg.getData().putString("data", content);
        return msg;
    }


    private void doClick(Supplier<String> supplier) {
        try {
            handler.sendMessage(this.message(1, null));
            if (this.getMillis() != null) {
                throw new Warn("重复点击");
            }
            this.setMillis(System.currentTimeMillis());
            String s = supplier.get();
            if (s != null) {
                // 有处理结果信息则提示
                handler.sendMessage(this.message(1, s));
            }
            // 通知执行后续步骤
            handler.sendMessage(this.message(0, null));
        } catch (Warn e) {
            // 进行消息提示
            handler.sendMessage(this.message(0, e.getMessage()));
        } catch (Exception e) {
            // 进行消息提示
            handler.sendMessage(this.message(0, "系统错误:" + e));
        } finally {
            handler.sendMessage(this.message(0, null));
            this.setMillis(null);
        }
    }

    private synchronized void setMillis(Long millis) {
        this.millis = millis;
    }

    private synchronized Long getMillis() {
        return this.millis;
    }

}
