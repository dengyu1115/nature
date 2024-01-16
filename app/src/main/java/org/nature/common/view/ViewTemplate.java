package org.nature.common.view;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.InputType;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.LinearLayout.LayoutParams;
import org.nature.R;
import org.nature.common.constant.Const;
import org.nature.common.util.CommonUtil;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

public class ViewTemplate {

    public static final int PAD = 10;
    private final Context context;

    private ViewTemplate(Context context) {
        this.context = context;
    }

    public static ViewTemplate build(Context context) {
        return new ViewTemplate(context);
    }

    public Button button(int w, int h) {
        Button button = new Button(context);
        float density = context.getResources().getDisplayMetrics().density;
        int width = (int) (w * density + 0.5f);
        int height = (int) (h * density + 0.5f);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(width, height);
        button.setLayoutParams(params);
        button.setGravity(Gravity.CENTER);
        button.setPadding(PAD, PAD, PAD, PAD);
        @SuppressLint("UseCompatLoadingForDrawables")
        Drawable drawable = context.getDrawable(R.drawable.common_background);
        button.setBackground(drawable);
        return button;
    }

    public Button button(String name, int w, int h) {
        Button button = this.button(w, h);
        button.setText(name);
        return button;
    }

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

    public EditText numeric(int w, int h) {
        EditText editText = this.editText(w, h);
        editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL
                | InputType.TYPE_NUMBER_FLAG_SIGNED);
        return editText;
    }

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
        @SuppressLint("UseCompatLoadingForDrawables")
        Drawable drawable = context.getDrawable(R.drawable.common_background);
        editText.setBackground(drawable);
        return editText;
    }

    public EditText areaText(int w, int h) {
        EditText text = new EditText(context);
        float density = context.getResources().getDisplayMetrics().density;
        int width = (int) (w * density + 0.5f);
        int height = (int) (h * density + 0.5f);
        LayoutParams param = new LayoutParams(width, height);
        text.setPadding(PAD, PAD, PAD, PAD);
        text.setLayoutParams(param);
        text.setGravity(Gravity.TOP | Gravity.START);
        @SuppressLint("UseCompatLoadingForDrawables")
        Drawable drawable = context.getDrawable(R.drawable.common_background);
        text.setBackground(drawable);
        return text;
    }

    public <T> Selector<T> selector(int w, int h) {
        return new Selector<>(context, w, h);
    }

    public LinearLayout line(int w, int h) {
        LinearLayout line = new LinearLayout(context);
        line.setGravity(Gravity.CENTER);
        float density = context.getResources().getDisplayMetrics().density;
        int width = (int) (w * density + 0.5f);
        int height = (int) (h * density + 0.5f);
        LayoutParams param = new LayoutParams(width, height);
        line.setLayoutParams(param);
        return line;
    }

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

    public LinearLayout linearPage() {
        LinearLayout page = new LinearLayout(context);
        page.setOrientation(LinearLayout.VERTICAL);
        return page;
    }

    public LinearLayout checkbox(Map<String, String> map) {
        LinearLayout page = new LinearLayout(context);
        page.setOrientation(LinearLayout.VERTICAL);
        map.forEach((k, v) -> {
            CheckBox checkBox = new CheckBox(context);
            checkBox.setHint(k);
            checkBox.setText(v);
            page.addView(checkBox);
        });
        return page;
    }

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

    public Button datePiker(int w, int h) {
        Button button = this.button(w, h);
        button.setOnClickListener(l -> {
            String s = button.getText().toString();
            Date date = s.isEmpty() ? new Date() : CommonUtil.parseDate(s, Const.FORMAT_DAY);
            Calendar c = Calendar.getInstance();
            c.setTime(date);
            @SuppressLint("ResourceType")
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

    public Button timePiker(int w, int h) {
        Button button = this.button(w, h);
        button.setOnClickListener(l -> {
            String s = button.getText().toString();
            Date date = s.isEmpty() ? new Date() : CommonUtil.parseDate(s, Const.FORMAT_TIME);
            Calendar c = Calendar.getInstance();
            c.setTime(date);
            @SuppressLint("ResourceType")
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

    @SuppressLint("DefaultLocale")
    private String getDate(DatePicker view) {
        return String.format("%04d%02d%02d", view.getYear(), view.getMonth() + 1, view.getDayOfMonth());
    }

    @SuppressLint("DefaultLocale")
    private String getTime(TimePicker view) {
        return String.format("%02d:%02d:00", view.getHour(), view.getMinute());
    }
}
