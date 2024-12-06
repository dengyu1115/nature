package org.nature.common.view;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import android.widget.LinearLayout.LayoutParams;
import org.nature.R;
import org.nature.common.constant.Const;
import org.nature.common.util.DateUtil;

import java.util.*;

import static android.text.InputType.*;

/**
 * view模板
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/21
 */
@SuppressLint({"UseCompatLoadingForDrawables", "ResourceType", "DefaultLocale"})
public class ViewTemplate {

    private static final Map<Context, ViewTemplate> MAP = new HashMap<>();
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
        ViewTemplate instance = MAP.get(context);
        if (instance == null) {
            MAP.put(context, instance = new ViewTemplate(context));
        }
        return instance;
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
        button.setBackground(context.getDrawable(R.drawable.common_background));
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
        text.setBackground(context.getDrawable(R.drawable.common_background));
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
        button.setTextColor(Color.DKGRAY);
        button.setHint("");
        button.setOnClickListener(i -> {
            int color = button.getCurrentTextColor();
            button.setTextColor(color == Color.RED ? Color.DKGRAY : Color.RED);
            button.setHint(color == Color.RED ? "" : "1");
        });
        return button;
    }

    /**
     * 日期选择器
     * @param w 宽
     * @param h 高
     * @return Button
     */
    public Button datePiker(int w, int h) {
        Button button = this.button(w, h);
        button.setOnClickListener(l -> {
            String s = button.getText().toString();
            Date date = s.isEmpty() ? new Date() : DateUtil.parse(s, Const.FORMAT_DAY);
            Calendar c = Calendar.getInstance();
            c.setTime(date);
            DatePickerDialog picker = new DatePickerDialog(context, 3,
                    (view, year, month, dayOfMonth) -> button.setText(this.getDate(view)),
                    c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
            picker.show();
        });
        button.setOnLongClickListener(v -> {
            button.setText("");
            return true;
        });
        return button;
    }

    /**
     * 时间选择器
     * @param w 宽
     * @param h 高
     * @return Button
     */
    public Button timePiker(int w, int h) {
        Button button = this.button(w, h);
        button.setOnClickListener(l -> {
            String s = button.getText().toString();
            Date date = s.isEmpty() ? new Date() : DateUtil.parse(s, Const.FORMAT_TIME);
            Calendar c = Calendar.getInstance();
            c.setTime(date);
            TimePickerDialog picker = new TimePickerDialog(context, 3,
                    (view, hour, min) -> button.setText(this.getTime(view)),
                    c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true);
            picker.show();
        });
        button.setOnLongClickListener(v -> {
            button.setText("");
            return true;
        });
        return button;
    }

    /**
     * 获取日期
     * @param view view
     * @return String
     */
    private String getDate(DatePicker view) {
        return String.format("%04d%02d%02d", view.getYear(), view.getMonth() + 1, view.getDayOfMonth());
    }

    /**
     * 获取时间
     * @param view view
     * @return String
     */
    private String getTime(TimePicker view) {
        return String.format("%02d:%02d:00", view.getHour(), view.getMinute());
    }


    private int getWidth(float w) {
        return (int) (w * wd + 05f);
    }

    private int getHeight(float h) {
        return (int) (h * hd + 0.5f);
    }

}
