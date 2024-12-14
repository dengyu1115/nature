package org.nature.common.view;

import android.annotation.SuppressLint;
import android.app.TimePickerDialog;
import android.content.Context;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import org.nature.R;
import org.nature.common.constant.Const;
import org.nature.common.util.DateUtil;

import java.util.Calendar;
import java.util.Date;

@SuppressLint({"ViewConstructor", "UseCompatLoadingForDrawables", "DefaultLocale", "ResourceType"})
public class TimeSelector extends LinearLayout {

    private final Context context;
    private final int width, height;
    private final TextView textView;

    public TimeSelector(Context context, int width, int height) {
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
            Date date = s.isEmpty() ? new Date() : DateUtil.parse(s, Const.FORMAT_TIME);
            Calendar c = Calendar.getInstance();
            c.setTime(date);
            TimePickerDialog picker = new TimePickerDialog(context, 3,
                    (view, hour, min) -> textView.setText(this.getTime(view)),
                    c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true);
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
        view.setImageDrawable(context.getDrawable(R.drawable.icon_time));
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
     * 获取时间
     * @param view view
     * @return String
     */
    private String getTime(TimePicker view) {
        return String.format("%02d:%02d:00", view.getHour(), view.getMinute());
    }


}
