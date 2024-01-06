package org.nature.common.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import org.nature.R;


/**
 * 之前都是写在Activity中，感觉代码好多，还是封装到这里好
 */
@SuppressLint("UseCompatLoadingForDrawables")
public class ViewUtil {

    public static Button button(Context context) {
        Button button = new Button(context);
        Drawable drawable = context.getDrawable(R.drawable.common_background);
        float density = context.getResources().getDisplayMetrics().density;
        int width = (int) (60 * density + 0.5f);
        int height = (int) (30 * density + 0.5f);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(width, height);
        button.setLayoutParams(params);
        button.setGravity(Gravity.CENTER);
        button.setPadding(0, 0, 0, 0);
        button.setBackground(drawable);
        return button;
    }

    public static Button button(Context context, String name) {
        Button button = button(context);
        button.setText(name);
        return button;
    }

    public static TextView textView(Context context, String name) {
        TextView text = new TextView(context);
        text.setText(name);
        float density = context.getResources().getDisplayMetrics().density;
        int width = (int) (80 * density + 0.5f);
        int height = (int) (30 * density + 0.5f);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(width, height);
        text.setLayoutParams(params);
        text.setPadding(10, 10, 10, 10);
        text.setGravity(Gravity.CENTER_VERTICAL | Gravity.END);
        return text;
    }

    public static EditText editText(Context context) {
        EditText editText = new EditText(context);
        Drawable drawable = context.getDrawable(R.drawable.common_background);
        float density = context.getResources().getDisplayMetrics().density;
        int width = (int) (100 * density + 0.5f);
        int height = (int) (30 * density + 0.5f);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(width, height);
        editText.setLayoutParams(params);
        editText.setGravity(Gravity.CENTER);
        editText.setPadding(0, 0, 0, 0);
        editText.setBackground(drawable);
        return editText;
    }

    public static void initActivity(AppCompatActivity context) {
//        context.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }
}
