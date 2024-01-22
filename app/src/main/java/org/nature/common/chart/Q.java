package org.nature.common.chart;

import java.util.function.Function;

/**
 * 指标
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/22
 */
public class Q<T> {
    /**
     * 标题
     */
    public final String title;
    /**
     * 文案获取函数
     */
    public final Function<T, String> text;
    /**
     * 颜色
     */
    public final int color;
    /**
     * x起始
     */
    public int sx;
    /**
     * x终止
     */
    public int ex;
    /**
     * y位置
     */
    public int y;

    public Q(String title, Function<T, String> text, int color) {
        this.title = title;
        this.text = text;
        this.color = color;
    }

    /**
     * 获取文案
     * @param d 数据
     * @return String
     */
    public String content(T d) {
        if (d == null) {
            return "";
        }
        String s = text.apply(d);
        if (s == null) {
            return "";
        }
        // 取到值不为null才返回该值，否则返回空字符串
        return s;
    }

    /**
     * 位置固定
     * @param sx x起始
     * @param ex x终止
     * @param y  y位置
     */
    public void fix(int sx, int ex, int y) {
        this.sx = sx;
        this.ex = ex;
        this.y = y;
    }

}
