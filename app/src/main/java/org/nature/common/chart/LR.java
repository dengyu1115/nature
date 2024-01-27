package org.nature.common.chart;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 矩形内容
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/22
 */
public class LR<T> extends BR<T> {

    /**
     * 字段取值函数
     */
    public final List<C<T>> cs;

    public LR(int scale, int weight, List<C<T>> cs, Function<Double, String> formatter) {
        super(scale, weight, formatter);
        this.cs = cs;
    }

    @Override
    protected List<Function<T, Double>> fs() {
        return this.cs.stream().map(i->i.func).collect(Collectors.toList());
    }

}
