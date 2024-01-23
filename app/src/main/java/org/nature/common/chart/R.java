package org.nature.common.chart;

import android.annotation.SuppressLint;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Function;

/**
 * 矩形内容
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/22
 */
public class R<T> {
    /**
     * 小数位数
     */
    public final int scale;
    /**
     * 字体
     */
    public final int weight;
    /**
     * 字段取值函数
     */
    public final List<C<T>> cs;
    /**
     * 字段文案格式化函数
     */
    public final Function<Double, String> formatter;
    /**
     * Y起始
     */
    public int sy;
    /**
     * Y终止
     */
    public int ey;
    /**
     * 单位长度
     */
    public int interval;
    /**
     * 单位
     */
    public float unit;
    /**
     * 最小值
     */
    public Double min;
    /**
     * 文案集合
     */
    public List<String> texts;

    public R(int scale, int weight, List<C<T>> cs, Function<Double, String> formatter) {
        this.scale = scale;
        this.weight = weight;
        this.cs = cs;
        this.formatter = formatter;
    }

    public void fix(int sy, int ey) {
        this.sy = sy;
        this.ey = ey;
    }

    /**
     * 计算参数
     * @param data 数据
     */
    public void calcParams(List<T> data) {
        SortedSet<Double> amounts = new TreeSet<>();
        for (T d : data) {
            for (C<T> c : cs) {
                this.addDoubles(amounts, c.func.apply(d));
            }
        }
        double min = amounts.first() * scale, max = amounts.last() * scale, count = 2d;
        // 最大值与最小值相等，特殊处理
        if (max == min) {
            max = max * 2;
            min = 0;
        }
        double v = Math.ceil((max - min) / count), first = Math.floor(min), last = first + v * (count - 1);
        if (last < max) {
            count = 3d;
        }
        this.texts = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            this.texts.add(formatter.apply((first + v * i) / scale));
        }
        this.min = first / scale;
        max = (first + (count - 1) * v) / scale;
        this.interval = (int) ((this.ey - this.sy) / (double) (this.texts.size() - 1) + 0.5d);
        this.unit = (float) ((this.ey - this.sy) / (max - this.min));
    }

    /**
     * 添加数值
     * @param doubles 集合
     * @param d       数值
     */
    private void addDoubles(SortedSet<Double> doubles, Double d) {
        if (d != null) {
            doubles.add(d);
        }
    }

}
