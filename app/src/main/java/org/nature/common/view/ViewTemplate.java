package org.nature.common.view;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.LinearLayout.LayoutParams;
import org.nature.R;
import org.nature.common.constant.Const;
import org.nature.common.util.DateUtil;

import java.util.Calendar;
import java.util.Date;

import static android.text.InputType.*;

/**
 * view模板
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/21
 */
@SuppressLint({"UseCompatLoadingForDrawables", "ResourceType", "DefaultLocale"})
public class ViewTemplate {

    /**
     * 默认内边距
     */
    private static final int PAD = 10;
    /**
     * context
     */
    private final Context context;

    private ViewTemplate(Context context) {
        this.context = context;
    }

    public static ViewTemplate build(Context context) {
        return new ViewTemplate(context);
    }

    /**
     * 按钮
     * @param w 宽
     * @param h 高
     * @return Button
     */
    public Button button(int w, int h) {
        Button button = new Button(context);
        float density = context.getResources().getDisplayMetrics().density;
        int width = (int) (w * density + 0.5f);
        int height = (int) (h * density + 0.5f);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(width, height);
        button.setLayoutParams(params);
        button.setGravity(Gravity.CENTER);
        button.setPadding(PAD, PAD, PAD, PAD);
        Drawable drawable = context.getDrawable(R.drawable.common_background);
        button.setBackground(drawable);
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
    public TextView textView(String name, int w, int h) {
        TextView text = new TextView(context);
        text.setText(name);
        float density = context.getResources().getDisplayMetrics().density;
        int width = (int) (w * density + 0.5f);
        int height = (int) (h * density + 0.5f);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(width, height);
        text.setLayoutParams(params);
        text.setPadding(PAD, PAD, PAD, PAD);
        text.setGravity(Gravity.CENTER_VERTICAL | Gravity.END);
        return text;
    }

    /**
     * 数字框
     * @param w 宽
     * @param h 高
     * @return EditText
     */
    public EditText numeric(int w, int h) {
        EditText editText = this.editText(w, h);
        editText.setInputType(TYPE_CLASS_NUMBER | TYPE_NUMBER_FLAG_DECIMAL | TYPE_NUMBER_FLAG_SIGNED);
        return editText;
    }

    /**
     * 数字框
     * @param w 宽
     * @param h 高
     * @return EditText
     */
    public EditText integer(int w, int h) {
        EditText editText = this.editText(w, h);
        editText.setInputType(TYPE_CLASS_NUMBER | TYPE_NUMBER_FLAG_SIGNED);
        return editText;
    }

    /**
     * 编辑框
     * @param w 宽
     * @param h 高
     * @return EditText
     */
    public EditText editText(int w, int h) {
        EditText editText = new EditText(context);
        float density = context.getResources().getDisplayMetrics().density;
        int width = (int) (w * density + 0.5f);
        int height = (int) (h * density + 0.5f);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(width, height);
        editText.setTextSize(12);
        editText.setLayoutParams(params);
        editText.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
        editText.setPadding(PAD, PAD, PAD, PAD);
        Drawable drawable = context.getDrawable(R.drawable.common_background);
        editText.setBackground(drawable);
        return editText;
    }

    /**
     * 文本域
     * @param w 宽
     * @param h 高
     * @return EditText
     */
    public EditText areaText(int w, int h) {
        EditText text = new EditText(context);
        float density = context.getResources().getDisplayMetrics().density;
        int width = (int) (w * density + 0.5f);
        int height = (int) (h * density + 0.5f);
        LayoutParams param = new LayoutParams(width, height);
        text.setPadding(PAD, PAD, PAD, PAD);
        text.setLayoutParams(param);
        text.setGravity(Gravity.TOP | Gravity.START);
        Drawable drawable = context.getDrawable(R.drawable.common_background);
        text.setBackground(drawable);
        return text;
    }

    /**
     * 选择器
     * @param w 宽
     * @param h 高
     * @return Selector
     */
    public <T> Selector<T> selector(int w, int h) {
        return new Selector<>(context, w, h);
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
        float density = context.getResources().getDisplayMetrics().density;
        int width = (int) (w * density + 0.5f);
        int height = (int) (h * density + 0.5f);
        LayoutParams param = new LayoutParams(width, height);
        line.setLayoutParams(param);
        for (View view : views) {
            line.addView(view);
        }
        return line;
    }

    /**
     * 块
     * @param w 宽
     * @param h 高
     * @return LinearLayout
     */
    public LinearLayout block(int w, int h) {
        LinearLayout line = new LinearLayout(context);
        line.setGravity(Gravity.CENTER);
        float density = context.getResources().getDisplayMetrics().density;
        int width = (int) (w * density + 0.5f);
        int height = (int) (h * density + 0.5f);
        LayoutParams param = new LayoutParams(width, height);
        line.setLayoutParams(param);
        line.setOrientation(LinearLayout.VERTICAL);
        return line;
    }

    /**
     * 线性布局
     * @param gravity 排布方式
     * @param views   子级view集合
     * @return LinearLayout
     */
    public LinearLayout linearPage(int gravity, View... views) {
        LinearLayout page = new LinearLayout(context);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setGravity(gravity);
        for (View view : views) {
            page.addView(view);
        }
        return page;
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

}
