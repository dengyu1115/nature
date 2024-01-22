package org.nature.common.chart;

import java.util.function.Function;

/**
 * 内容
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/22
 */
public class C<T> {
    /**
     * 颜色
     */
    public final int color;
    /**
     * 字段取值函数
     */
    public final Function<T, Double> func;

    public C(int color, Function<T, Double> func) {
        this.color = color;
        this.func = func;
    }
}
