package org.nature.common.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.nature.R;

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

    public Button(Context context, String name, int width, int height) {
        super(context);
        this.context = context;
        this.width = width;
        this.height = height;
        this.setLayoutParams(new LayoutParams(width, height));
        this.setPadding(PAD, PAD, PAD, PAD);
        this.addView(valueView = this.buildTextView(name));
    }

    public void setText(CharSequence text) {
        this.valueView.setText(text);
    }

    public void setOnClickListener(OnClickListener listener) {
        this.valueView.setOnClickListener(listener);
    }

    public void setClickable(boolean clickable) {
        this.valueView.setClickable(clickable);
    }

    public void setBackground(Drawable background) {
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

}
