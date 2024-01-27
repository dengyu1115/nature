package org.nature.common.chart;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * K线矩形内容
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/22
 */
public class KR<T> extends BR<T> {

    /**
     * 字段取值函数
     */
    public final Function<T, Double> latest;
    /**
     * 字段取值函数
     */
    public final Function<T, Double> open;
    /**
     * 字段取值函数
     */
    public final Function<T, Double> high;
    /**
     * 字段取值函数
     */
    public final Function<T, Double> low;
    /**
     * 字段取值函数
     */
    public final List<C<T>> cs;

    public KR(int scale, int weight, Function<T, Double> latest, Function<T, Double> open,
              Function<T, Double> high, Function<T, Double> low,
              List<C<T>> cs, Function<Double, String> formatter) {
        super(scale, weight, formatter);
        this.latest = latest;
        this.open = open;
        this.high = high;
        this.low = low;
        this.cs = cs;
    }

    @Override
    protected List<Function<T, Double>> fs() {
        List<Function<T, Double>> list = new ArrayList<>(List.of(latest, open, high, low));
        list.addAll(this.cs.stream().map(i -> i.func).collect(Collectors.toList()));
        return list;
    }

}
