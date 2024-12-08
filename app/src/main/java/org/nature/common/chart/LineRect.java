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
public class LineRect<T> extends BaseRect<T> {

    /**
     * 字段取值函数
     */
    public final List<Content<T>> contents;

    public LineRect(int scale, int weight, List<Content<T>> contents, Function<Double, String> formatter) {
        super(scale, weight, formatter);
        this.contents = contents;
    }

    @Override
    protected List<Function<T, Double>> fs() {
        return this.contents.stream().map(i -> i.func).collect(Collectors.toList());
    }

}
