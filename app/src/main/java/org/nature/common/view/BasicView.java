package org.nature.common.view;

import android.content.Context;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.widget.LinearLayout;

/**
 * 自定义view基类
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/5
 */
public class BasicView extends LinearLayout {
    /**
     * 宽度/高度 填满上级
     */
    protected static final int MATCH_PARENT = LayoutParams.MATCH_PARENT;
    /**
     * 宽度/高度 由下级填充内容决定
     */
    protected static final int WRAP_CONTENT = LayoutParams.WRAP_CONTENT;
    /**
     * 背景颜色
     */
    protected static final int BG_COLOR = Color.parseColor("#ff99cc00");
    /**
     * 密度
     */
    protected final float DENSITY;
    /**
     * 上下文对象
     */
    protected Context context;

    /**
     * 构造函数
     * @param context 上下文对象
     */
    public BasicView(Context context) {
        super(context);
        this.context = context;
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        this.DENSITY = displayMetrics.density;
    }

    /**
     * dp转px
     * @param dp dp
     * @return px
     */
    int dpToPx(float dp) {
        return (int) (dp * DENSITY + 0.5f);
    }

    /**
     * px转dp
     * @param px px
     * @return px
     */
    float pxToDp(float px) {
        return px / DENSITY;
    }

}
