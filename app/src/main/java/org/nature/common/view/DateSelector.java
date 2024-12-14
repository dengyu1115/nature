package org.nature.common.view;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.view.Gravity;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.nature.R;
import org.nature.common.constant.Const;
import org.nature.common.util.DateUtil;

import java.util.Calendar;
import java.util.Date;

@SuppressLint({"ViewConstructor", "UseCompatLoadingForDrawables", "DefaultLocale", "ResourceType"})
public class DateSelector extends LinearLayout {

    private final Context context;
    private final int width, height;
    private final TextView textView;

    public DateSelector(Context context, int width, int height) {
        super(context);
        this.context = context;
        this.width = width;
        this.height = height;
        this.setLayoutParams(new LayoutParams(width, height));
        this.setBackground(context.getDrawable(R.drawable.bg_normal));
        this.addView(this.buildImage());
        this.addView(textView = this.buildTextView());
        this.setOnClickListener(l -> {
            String s = textView.getText().toString();
            Date date = s.isEmpty() ? new Date() : DateUtil.parse(s, Const.FORMAT_DAY);
            Calendar c = Calendar.getInstance();
            c.setTime(date);
            DatePickerDialog picker = new DatePickerDialog(context, 3,
                    (view, year, month, dayOfMonth) -> textView.setText(this.getDate(view)),
                    c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
            picker.show();
        });
        this.setOnLongClickListener(v -> {
            textView.setText("");
            return true;
        });
    }

    public void setValue(String value) {
        this.textView.setText(value);
    }

    public String getValue() {
        return textView.getText().toString();
    }

    private ImageView buildImage() {
        ImageView view = new ImageView(context);
        view.setLayoutParams(new LayoutParams(30, height));
        view.setImageDrawable(context.getDrawable(R.drawable.date));
        return view;
    }

    private TextView buildTextView() {
        TextView textView = new TextView(context);
        textView.setLayoutParams(new LayoutParams(width - 30, height - 2));
        textView.setGravity(Gravity.START | Gravity.CENTER);
        textView.setPadding(1, 1, 1, 1);
        return textView;
    }

    /**
     * 获取日期
     * @param view view
     * @return String
     */
    private String getDate(DatePicker view) {
        return String.format("%04d%02d%02d", view.getYear(), view.getMonth() + 1, view.getDayOfMonth());
    }

}
