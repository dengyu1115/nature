package org.nature.common.view;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import android.widget.LinearLayout.LayoutParams;
import org.nature.R;
import org.nature.common.constant.Const;
import org.nature.common.util.ClickUtil;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static android.text.InputType.*;

/**
 * view模板
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/21
 */
@SuppressLint({"UseCompatLoadingForDrawables", "ResourceType", "DefaultLocale"})
public class ViewTemplate {

    private static final Map<Context, ViewTemplate> MAP = new ConcurrentHashMap<>();
    /**
     * 默认内边距
     */
    private static final int PAD = 10;
    /**
     * context
     */
    private final Context context;
    /**
     * 宽度、高度
     */
    private final float wd, hd;

    private ViewTemplate(Context context) {
        this.context = context;
        this.wd = Const.PAGE_WIDTH / 100f;
        this.hd = Const.PAGE_HEIGHT / 100f;
    }

    public static ViewTemplate build(Context context) {
        return MAP.computeIfAbsent(context, ViewTemplate::new);
    }

    /**
     * 按钮
     * @param w 宽
     * @param h 高
     * @return Button
     */
    public Button button(int w, int h) {
        Button button = new Button(context);
        button.setLayoutParams(new LayoutParams(this.getWidth(w), this.getHeight(h)));
        button.setGravity(Gravity.CENTER);
        button.setPadding(PAD, PAD, PAD, PAD);
        button.setBackground(this.background("primary"));
        return button;
    }

    /**
     * 按钮
     * @param name 名称
     * @param w    宽
     * @param h    高
     * @return Button
     */
    public Button button(String name, int w, int h) {
        Button button = this.button(w, h);
        button.setText(name);
        return button;
    }

    /**
     * 文本框
     * @param name 名称
     * @param w    宽
     * @param h    高
     * @return TextView
     */
    public TextView text(String name, int w, int h) {
        TextView text = new TextView(context);
        text.setText(name);
        text.setLayoutParams(new LayoutParams(this.getWidth(w), this.getHeight(h)));
        text.setPadding(PAD, PAD, PAD, PAD);
        text.setGravity(Gravity.CENTER_VERTICAL | Gravity.END);
        return text;
    }

    /**
     * 编辑框
     * @param w 宽
     * @param h 高
     * @return EditText
     */
    public EditText input(int w, int h) {
        EditText text = new EditText(context);
        text.setTextSize(12);
        text.setLayoutParams(new LayoutParams(this.getWidth(w), this.getHeight(h)));
        text.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
        text.setPadding(PAD, PAD, PAD, PAD);
        text.setBackground(this.background("normal"));
        return text;
    }

    /**
     * 数字框
     * @param w 宽
     * @param h 高
     * @return EditText
     */
    public EditText decimal(int w, int h) {
        EditText editText = this.input(w, h);
        editText.setInputType(TYPE_CLASS_NUMBER | TYPE_NUMBER_FLAG_DECIMAL | TYPE_NUMBER_FLAG_SIGNED);
        return editText;
    }

    /**
     * 数字框
     * @param w 宽
     * @param h 高
     * @return EditText
     */
    public EditText number(int w, int h) {
        EditText editText = this.input(w, h);
        editText.setInputType(TYPE_CLASS_NUMBER | TYPE_NUMBER_FLAG_SIGNED);
        return editText;
    }

    /**
     * 文本域
     * @param w 宽
     * @param h 高
     * @return EditText
     */
    public EditText textArea(int w, int h) {
        EditText text = this.input(w, h);
        text.setGravity(Gravity.TOP | Gravity.START);
        return text;
    }

    /**
     * 选择器
     * @param w 宽
     * @param h 高
     * @return Selector
     */
    public <T> Selector<T> selector(float w, float h) {
        return new Selector<>(context, this.getWidth(w), this.getHeight(h));
    }


    public <T> Table<T> table(float w, float h, int rows, int columns) {
        return new Table<>(context, this.getWidth(w), this.getHeight(h), rows, columns);
    }

    /**
     * 行
     * @param w     宽
     * @param h     高
     * @param views 下级view集合
     * @return LinearLayout
     */
    public LinearLayout line(int w, int h, View... views) {
        LinearLayout line = new LinearLayout(context);
        line.setGravity(Gravity.CENTER);
        line.setLayoutParams(new LayoutParams(this.getWidth(w), this.getHeight(h)));
        Arrays.stream(views).forEach(line::addView);
        return line;
    }

    /**
     * 线性布局
     * @param gravity 排布方式
     * @param views   子级view集合
     * @return LinearLayout
     */
    public LinearLayout block(int gravity, View... views) {
        LinearLayout block = new LinearLayout(context);
        block.setOrientation(LinearLayout.VERTICAL);
        block.setGravity(gravity);
        Arrays.stream(views).forEach(block::addView);
        return block;
    }

    /**
     * 单选按钮
     * @param name 名称
     * @param w    宽
     * @param h    高
     * @return Button
     */
    public Button radio(String name, int w, int h) {
        Button button = this.button(name, w, h);
        button.setHint("");
        button.setOnClickListener(i -> {
            String hint = (String) button.getHint();
            button.setHint("".equals(hint) ? "1" : "");
            button.setBackground("".equals(hint) ? this.background("success") : this.background("primary"));
        });
        return button;
    }

    /**
     * 日期选择器
     * @param w 宽
     * @param h 高
     * @return Button
     */
    public DateSelector datePiker(int w, int h) {
        return new DateSelector(context, this.getWidth(w), this.getHeight(h));
    }

    /**
     * 时间选择器
     * @param w 宽
     * @param h 高
     * @return Button
     */
    public TimeSelector timePiker(int w, int h) {
        return new TimeSelector(context, this.getWidth(w), this.getHeight(h));
    }


    public Drawable background(String name) {
        if ("success".equals(name)) {
            return context.getDrawable(R.drawable.bg_btn_success);
        }
        if ("primary".equals(name)) {
            return context.getDrawable(R.drawable.bg_btn_primary);
        }
        return context.getDrawable(R.drawable.bg_normal);
    }

    /**
     * 提示消息
     * @param message 消息
     */
    public void alert(String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    /**
     * 确认框
     * @param title    标题
     * @param message  提示消息
     * @param runnable 执行逻辑
     */
    public void confirm(String title, String message, Runnable runnable) {
        this.buildAlertDialog(title, builder -> builder.setMessage(message), runnable);
    }

    /**
     * 确认框
     * @param title    标题
     * @param view     自定义的页面
     * @param runnable 执行逻辑
     */
    public void confirm(String title, View view, Runnable runnable) {
        this.buildAlertDialog(title, builder -> builder.setView(view), runnable);
    }

    /**
     * 确认框
     * @param title    标题
     * @param message  提示消息
     * @param supplier 执行逻辑
     */
    public void confirmAsync(String title, String message, Supplier<String> supplier) {
        this.buildAsyncDialog(title, builder -> builder.setMessage(message), supplier);
    }

    /**
     * 确认框
     * @param t   操作对象数据
     * @param del 删除操作
     * @param upd 修改操作
     */
    public <T> void handle(T t, Consumer<T> del, Consumer<T> upd) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("请选择你要的操作");
        ViewTemplate template = ViewTemplate.build(context);
        Button delBtn = template.button("删除", 10, 7);
        Button updBtn = template.button("修改", 10, 7);
        LinearLayout line = template.line(200, 30, delBtn, updBtn);
        builder.setView(line);
        builder.setNegativeButton("取消", (di, i) -> di.dismiss());
        AlertDialog dialog = builder.create();
        ClickUtil.onClick(delBtn, () -> {
            dialog.dismiss();
            del.accept(t);
        });
        ClickUtil.onClick(updBtn, () -> {
            dialog.dismiss();
            upd.accept(t);
        });
        dialog.show();
    }

    /**
     * 构建确认框
     * @param title    标题
     * @param consumer 框处理
     * @param runnable 执行逻辑
     */
    private void buildAlertDialog(String title, Consumer<AlertDialog.Builder> consumer, Runnable runnable) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        TextView titleView = this.text(title, 20, 7);
        titleView.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        builder.setCustomTitle(titleView);
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
     * @param title    标题
     * @param consumer 框处理
     * @param supplier 执行逻辑
     */
    private void buildAsyncDialog(String title, Consumer<AlertDialog.Builder> consumer, Supplier<String> supplier) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        TextView titleView = this.text(title, 20, 7);
        titleView.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        builder.setCustomTitle(titleView);
        consumer.accept(builder);
        builder.setPositiveButton("确定", null);
        builder.setNegativeButton("取消", (di, i) -> {
        });
        AlertDialog dialog = builder.create();
        dialog.show();
        ClickUtil.onAsyncClick(dialog.getButton(AlertDialog.BUTTON_POSITIVE), supplier, dialog::cancel);
    }

    private int getWidth(float w) {
        return (int) (w * wd + 05f);
    }

    private int getHeight(float h) {
        return (int) (h * hd + 0.5f);
    }

}
