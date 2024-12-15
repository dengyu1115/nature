package org.nature.common.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.LinearLayout;
import org.nature.R;

import static org.nature.common.constant.Const.PAD;

/**
 * 输入框
 * @author Nature
 * @version 1.0.0
 * @since 2024/12/15
 */
@SuppressLint({"ViewConstructor", "UseCompatLoadingForDrawables", "DefaultLocale", "ResourceType"})
public class Input extends LinearLayout {

    private final Context context;
    private final int width, height;
    private final EditText valueView;

    public Input(Context context, int width, int height) {
        super(context);
        this.context = context;
        this.width = width;
        this.height = height;
        this.setLayoutParams(new LayoutParams(width, height));
        this.setPadding(PAD, PAD, PAD, PAD);
        this.addView(valueView = this.buildTextView());
    }

    public void setValue(String value) {
        this.valueView.setText(value);
    }

    public String getValue() {
        return valueView.getText().toString();
    }

    public void setInputType(int inputType) {
        this.valueView.setInputType(inputType);
    }

    public void setTextGravity(int gravity) {
        this.valueView.setGravity(gravity);
    }

    private EditText buildTextView() {
        EditText view = new EditText(context);
        view.setLayoutParams(new LayoutParams(width - PAD * 2, height - PAD * 2));
        view.setBackground(context.getDrawable(R.drawable.bg_normal));
        view.setGravity(Gravity.START | Gravity.CENTER);
        view.setPadding(1, 1, 1, 1);
        return view;
    }

}
