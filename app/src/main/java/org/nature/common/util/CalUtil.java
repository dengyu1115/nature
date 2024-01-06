package org.nature.common.util;

import java.util.List;
import java.util.function.Function;

/**
 * 计算工具类
 * @author nature
 * @version 1.0.0
 * @since 2019/11/6 23:14
 */
public class CalUtil {

    /**
     * 极大值
     * @param data     data
     * @param len      范围
     * @param function function
     * @param <T>      T
     * @return double
     */
    public static <T> double max(List<T> data, int len, Function<T, Double> function) {
        int size = data.size();
        if (size < len) throw new RuntimeException("size is little than " + len);
        List<T> list = data.subList(size - len, size);
        return max(list, function);
    }

    /**
     * 极大值
     * @param list     data
     * @param function function
     * @param <T>      T
     * @return double
     */
    public static <T> Double max(List<T> list, Function<T, Double> function) {
        return list.stream().map(function).max(Double::compareTo).orElse(function.apply(list.get(0)));
    }

    /**
     * 极小值
     * @param data     data
     * @param len      范围
     * @param function function
     * @param <T>      T
     * @return double
     */
    public static <T> double min(List<T> data, int len, Function<T, Double> function) {
        int size = data.size();
        if (size < len) throw new RuntimeException("size is little than " + len);
        List<T> list = data.subList(size - len, size);
        return min(list, function);
    }

    /**
     * 极小值
     * @param list     data
     * @param function function
     * @param <T>      T
     * @return double
     */
    public static <T> Double min(List<T> list, Function<T, Double> function) {
        return list.stream().map(function).min(Double::compareTo).orElse(function.apply(list.get(0)));
    }

}
